package au.org.ala.specieslist.controller

import au.org.ala.names.search.ALANameSearcher
import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.LocalAuthService
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListKVP
import au.org.ala.specieslist.UserDetailsService
import au.org.ala.specieslist.WebServiceController
import grails.test.spock.IntegrationSpec
import org.springframework.http.HttpStatus

import javax.servlet.http.Cookie

class WebServiceControllerIntegrationTest extends IntegrationSpec {

    WebServiceController controller = new WebServiceController()

    def "saveList version 1 should support JSON requests with a comma separated list of item names"() {
        String version1Json = "{\"listName\": \"list1\",  \"listType\": \"TEST\", \"listItems\": \"item1,item2,item3\"}"
        setup:
        HelperService helperService = new HelperService()
        helperService.cbIdxSearcher = Mock(ALANameSearcher)

        helperService.setUserDetailsService([getFullListOfUserDetailsByUsername : {return [a:"a"]}]
                as UserDetailsService)
        controller.setLocalAuthService([isValidUserName: {true}] as LocalAuthService)
        controller.setHelperService(helperService)

        when:
        controller.request.cookies = [new Cookie("ALA-Auth", "fred")]
        controller.request.json = version1Json
        controller.params.druid = "dr1"
        controller.saveList()

        then:
        assert controller.response.status == HttpStatus.CREATED.value()
        assert SpeciesList.list().size() == 1
        assert SpeciesListItem.list().size() == 3
    }

    def "saveList version 2 should support JSON requests with a structured items with KVP values"() {
        String version2Json =  """{
                                    "listName": "list1",
                                    "listType": "TEST",
                                    "listItems": [
                                        {
                                            "itemName": "item1",
                                            "kvpValues": [
                                                {
                                                    "key": "key1",
                                                    "value": "value1"
                                                },
                                                {
                                                    "key": "key2",
                                                    "value": "value2"
                                                }
                                            ]
                                        },
                                        {
                                            "itemName": "item2",
                                            "kvpValues": [
                                                {
                                                    "key": "key3",
                                                    "value": "value3"
                                                },
                                                {
                                                    "key": "key4",
                                                    "value": "value4"
                                                }
                                            ]
                                        }
                                    ]
                                }"""
        setup:
        HelperService helperService = new HelperService()
        helperService.cbIdxSearcher = Mock(ALANameSearcher)

        helperService.setUserDetailsService([getFullListOfUserDetailsByUsername : {return [a:"a"]}]
                as UserDetailsService)
        controller.setLocalAuthService([isValidUserName: {true}] as LocalAuthService)
        controller.setHelperService(helperService)

        when:
        controller.request.cookies = [new Cookie("ALA-Auth", "fred")]
        controller.request.json = version2Json
        controller.params.druid = "dr1"
        controller.saveList()

        then:
        assert controller.response.status == HttpStatus.CREATED.value()
        assert SpeciesList.list().size() == 1
        assert SpeciesListItem.list().size() == 2
        assert SpeciesListItem.list().get(0).kvpValues.size() == 2
        assert SpeciesListItem.list().get(1).kvpValues.size() == 2
    }


    def "listKeys should return a 400 (BAD_REQUEST) if no druid is provided"() {
        when:
        controller.listKeys()

        then:
        assert controller.response.status == 400
    }

    def "listKeys should return the unique set of keys from all DRUIDS in the request"() {
        setup:
        (1..2).each {
            (1..5).each {
                SpeciesListKVP kvp = new SpeciesListKVP(key: "key${it}", value: "value${it}", dataResourceUid: "dr${it < 3 ? '1' : '2'}")
                kvp.save(flush: true)
            }
        }

        when:
        controller.params.druid = "dr1,dr2"
        controller.listKeys()

        then:
        controller.response.status == 200
        controller.response.text == """["key1","key2","key3","key4","key5"]"""
    }

    def "listKeys should return the unique set of keys from requested DRUIDS only in the request"() {
        setup:
        (1..6).each {
            SpeciesListKVP kvp = new SpeciesListKVP(key: "key${it}", value: "value${it}", dataResourceUid: "dr${it <= 3 ? '1' : '2'}")
            kvp.save(flush: true, failOnError: true)
        }

        when:
        controller.params.druid = "dr2"
        controller.listKeys()

        then:
        controller.response.status == 200
        controller.response.text == """["key4","key5","key6"]"""
    }

    def "listItemsByKeys should return a 400 (BAD_REQUEST) if no druid parameter is provided"() {
        when:
        controller.params.keys = "test"
        controller.listItemsByKeys()

        then:
        controller.response.status == 400
    }

    def "listItemsByKeys should return a 400 (BAD_REQUEST) if no keys parameter is provided"() {
        when:
        controller.params.druid = "test"
        controller.listItemsByKeys()

        then:
        controller.response.status == 400
    }

    def "listItemsByKeys should only select the specified druids and keys - single values"() {
        setup:
        (1..3).each {
            SpeciesList list = new SpeciesList(listName: "list${it}", username: "fred", dataResourceUid: "dr${it}")
            list.save(flush: true, failOnError: true)

            SpeciesListItem item = new SpeciesListItem(rawScientificName: "scientificName${it}", dataResourceUid: "dr${it}", mylist: list, itemOrder: it)
            (1..5).each {
                SpeciesListKVP kvp = new SpeciesListKVP(key: "key${it}", value: "value${it}", dataResourceUid: item.dataResourceUid, itemOrder: it)
                kvp.save(flush: true, failOnError: true)
                item.addToKvpValues(kvp)
            }

            item.save(flush: true, failOnError: true)
        }

        when:
        controller.params.keys = "key1"
        controller.params.druid = "dr2"
        controller.listItemsByKeys()

        then:
        controller.response.status == 200
        controller.response.text == """{"scientificName2":[{"key":"key1","value":"value1"}]}"""
    }

    def "listItemsByKeys should only select the specified druids and keys - multiple values"() {
        setup:
        (1..3).each {
            SpeciesList list = new SpeciesList(listName: "list${it}", username: "fred", dataResourceUid: "dr${it}")
            list.save(flush: true, failOnError: true)

            SpeciesListItem item = new SpeciesListItem(rawScientificName: "scientificName${it}", dataResourceUid: "dr${it}", mylist: list, itemOrder: it)
            (1..5).each {
                SpeciesListKVP kvp = new SpeciesListKVP(id: it, key: "key${it}", value: "value${it}", dataResourceUid: item.dataResourceUid, itemOrder: it)
                kvp.save(flush: true, failOnError: true)
                item.addToKvpValues(kvp)
            }

            item.save(flush: true, failOnError: true)
        }

        when:
        controller.params.keys = "key1,key3"
        controller.params.druid = "dr1,dr3"
        controller.listItemsByKeys()

        then:
        controller.response.status == 200
        controller.response.text == """{"scientificName1":[{"key":"key1","value":"value1"},{"key":"key3","value":"value3"}],"scientificName3":[{"key":"key1","value":"value1"},{"key":"key3","value":"value3"}]}"""
    }

    def "listItemsByKeys should return CSV if the format parameter = csv"() {
        setup:
        (1..3).each {
            SpeciesList list = new SpeciesList(listName: "list${it}", username: "fred", dataResourceUid: "dr${it}")
            list.save(flush: true, failOnError: true)

            SpeciesListItem item = new SpeciesListItem(rawScientificName: "scientificName${it}", dataResourceUid: "dr${it}", mylist: list, itemOrder: it)
            (1..5).each {
                SpeciesListKVP kvp = new SpeciesListKVP(id: it, key: "key${it}", value: "value${it}", dataResourceUid: item.dataResourceUid, itemOrder: it)
                kvp.save(flush: true, failOnError: true)
                item.addToKvpValues(kvp)
            }

            item.save(flush: true, failOnError: true)
        }

        when:
        controller.params.format = "csv"
        controller.params.keys = "key3,key2"
        controller.params.druid = "dr1,dr3"
        controller.listItemsByKeys()

        then:
        controller.response.status == 200
        controller.response.contentType == "text/csv;charset=utf-8"
        controller.response.text == '"ScientificName","key2","key3"\n"scientificName1","value2","value3"\n"scientificName3","value2","value3"\n'
    }
}
