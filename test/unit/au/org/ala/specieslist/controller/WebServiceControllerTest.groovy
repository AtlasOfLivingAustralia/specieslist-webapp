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
}
