package au.org.ala.specieslist.controller

import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListKVP
import au.org.ala.specieslist.WebServiceController
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(WebServiceController)
@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
@Unroll
@Mock([SpeciesList, SpeciesListItem, SpeciesListKVP])
class WebServiceControllerTest extends Specification {

    def controller = new WebServiceController()

    def "getListItemDetails should retrieve KVP values when includeKVP is true"() {
        setup:
        SpeciesList list = new SpeciesList(listName: "list1", username: "fred", dataResourceUid: "Dr1")
        list.save(failOnError: true)
        SpeciesListKVP kvp1 = new SpeciesListKVP(key: "key1", value: "value1", dataResourceUid: "Dr1")
        kvp1.save(failOnError: true)

        mockDomain(SpeciesListItem, [[rawScientificName: "name1", dataResourceUid: "Dr1", itemOrder: 1, guid:
                "guid", mylist: list, kvpValues: [kvp1]]])

        when:
        params.includeKVP = true
        params.druid = "Dr1"
        controller.getListItemDetails()

        then:
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"lsid\":\"guid\",\"kvpValues\":[{\"key\":\"key1\",\"value\":\"value1\"}]}]"
    }

    def "getListItemDetails should not retrieve KVP values when includeKVP is false"() {
        setup:
        SpeciesList list = new SpeciesList(listName: "list1", username: "fred", dataResourceUid: "Dr1")
        list.save(failOnError: true)
        SpeciesListKVP kvp1 = new SpeciesListKVP(key: "key1", value: "value1", dataResourceUid: "Dr1")
        kvp1.save(failOnError: true)

        mockDomain(SpeciesListItem, [[rawScientificName: "name1", dataResourceUid: "Dr1", itemOrder: 1, guid:
                "guid", mylist: list, kvpValues: [kvp1]]])

        when:
        params.includeKVP = false
        params.druid = "Dr1"
        controller.getListItemDetails()

        then:
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"lsid\":\"guid\"}]"
    }

    def "getListItemDetails should not retrieve KVP values when includeKVP is not present"() {
        setup:
        SpeciesList list = new SpeciesList(listName: "list1", username: "fred", dataResourceUid: "Dr1")
        list.save(failOnError: true)
        SpeciesListKVP kvp1 = new SpeciesListKVP(key: "key1", value: "value1", dataResourceUid: "Dr1")
        kvp1.save(failOnError: true)

        mockDomain(SpeciesListItem, [[rawScientificName: "name1", dataResourceUid: "Dr1", itemOrder: 1, guid:
                "guid", mylist: list, kvpValues: [kvp1]]])

        when:
        params.druid = "Dr1"
        controller.getListItemDetails()

        then:
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"lsid\":\"guid\"}]"
    }

    def "listKeys should return a 400 (BAD_REQUEST) if no druid is provided"() {
        when:
        controller.listKeys()

        then:
        assert response.status == 400
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
        params.druid = "dr1,dr2"
        controller.listKeys()

        then:
        response.status == 200
        response.text == """["key1","key2","key3","key4","key5"]"""
    }

    def "listKeys should return the unique set of keys from requested DRUIDS only in the request"() {
        setup:
        (1..6).each {
            SpeciesListKVP kvp = new SpeciesListKVP(key: "key${it}", value: "value${it}", dataResourceUid: "dr${it <= 3 ? '1' : '2'}")
            kvp.save(flush: true, failOnError: true)
        }

        when:
        params.druid = "dr2"
        controller.listKeys()

        then:
        response.status == 200
        response.text == """["key4","key5","key6"]"""
    }

    def "listItemsByKeys should return a 400 (BAD_REQUEST) if no druid parameter is provided"() {
        when:
        params.keys = "test"
        controller.listItemsByKeys()

        then:
        response.status == 400
    }

    def "listItemsByKeys should return a 400 (BAD_REQUEST) if no keys parameter is provided"() {
        when:
        params.druid = "test"
        controller.listItemsByKeys()

        then:
        response.status == 400
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
        params.keys = "key1"
        params.druid = "dr2"
        controller.listItemsByKeys()

        then:
        response.status == 200
        response.text == """{"scientificName2":[{"key":"key1","value":"value1"}]}"""
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
        params.keys = "key1,key3"
        params.druid = "dr1,dr3"
        controller.listItemsByKeys()

        then:
        response.status == 200
        response.text == """{"scientificName1":[{"key":"key1","value":"value1"},{"key":"key3","value":"value3"}],"scientificName3":[{"key":"key1","value":"value1"},{"key":"key3","value":"value3"}]}"""
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
        params.format = "csv"
        params.keys = "key3,key2"
        params.druid = "dr1,dr3"
        controller.listItemsByKeys()

        then:
        response.status == 200
        response.contentType == "text/csv;charset=utf-8"
        response.text == '"ScientificName","key2","key3"\n"scientificName1","value2","value3"\n"scientificName3","value2","value3"\n'
    }
}
