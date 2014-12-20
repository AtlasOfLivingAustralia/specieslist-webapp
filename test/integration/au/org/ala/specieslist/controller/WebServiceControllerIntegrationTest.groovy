package au.org.ala.specieslist.controller

import au.org.ala.checklist.lucene.CBIndexSearch
import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.LocalAuthService
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
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
        helperService.cbIdxSearcher = Mock(CBIndexSearch)

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
        helperService.cbIdxSearcher = Mock(CBIndexSearch)

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
}
