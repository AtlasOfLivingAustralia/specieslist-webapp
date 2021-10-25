package au.org.ala.specieslist.controller

import au.org.ala.specieslist.QueryService
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListKVP
import au.org.ala.specieslist.WebServiceController
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class WebServiceControllerSpec extends Specification implements ControllerUnitTest<WebServiceController>, DataTest {

    void setupSpec() {
        mockDomains(SpeciesListItem, SpeciesList, SpeciesListKVP)
    }

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
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"commonName\":null,\"scientificName\":null,\"lsid\":\"guid\",\"dataResourceUid\":\"Dr1\",\"kvpValues\":[{\"key\":\"key1\",\"value\":\"value1\"}]}]"
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
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"commonName\":null,\"scientificName\":null,\"lsid\":\"guid\",\"dataResourceUid\":\"Dr1\"}]"
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
        assert response.text == "[{\"id\":1,\"name\":\"name1\",\"commonName\":null,\"scientificName\":null,\"lsid\":\"guid\",\"dataResourceUid\":\"Dr1\"}]"
    }

    def "listCommonKeys should fail when no druid is provided"(){
        when:
        controller.listCommonKeys()

        then:
        assert response.status ==  HttpStatus.SC_BAD_REQUEST
    }

    def "listCommonKeys should provide all keys that are common in the list provided"(){
        setup:
        SpeciesListKVP kvp1 = new SpeciesListKVP(key: "key 1", value: "value1", dataResourceUid: "Dr1")
        kvp1.save(failOnError: true)
        SpeciesListKVP kvp2 = new SpeciesListKVP(key: "key2", value: "value1", dataResourceUid: "Dr1")
        kvp2.save(failOnError: true)
        SpeciesListKVP kvp3 = new SpeciesListKVP(key: "key 1", value: "value1", dataResourceUid: "Dr2")
        kvp3.save(failOnError: true)
        SpeciesListKVP kvp4 = new SpeciesListKVP(key: "key4", value: "value1", dataResourceUid: "Dr2")
        kvp4.save(failOnError: true)

        SpeciesList dr1 = new SpeciesList(listName: "list1", username: "fred", dataResourceUid: "Dr1", kvpValues:[kvp1, kvp2])
        dr1.save(failOnError: true)
        SpeciesList dr2 = new SpeciesList(listName: "list2", username: "fred", dataResourceUid: "Dr2", kvpValues:[kvp3, kvp4])
        dr2.save(failOnError: true)

        when:
        params.druid="Dr1,Dr2"
        controller.listCommonKeys()

        then:
        assert response.status == HttpStatus.SC_OK
        assert response.text ==  "[\"key 1\"]"
    }

    void "test SpeciesLists"() {
        setup:
//        controller.queryService = Mock(QueryService)

        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1",
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2",
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3",
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)

        when:
        controller.queryService = Mock(QueryService)
        controller.getListDetails()

        then:
        assert controller.response.json.lists.size() == 3
    }

    void "test SpeciesLists user order, q filtering"() {
        setup:
        controller.queryService = Mock(QueryService)

        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1",
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2",
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3",
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)

        when:
        controller.params.user = 'a'
        controller.params.q = 'xyz'
        controller.params.sort = 'userId'
        controller.getListDetails()

        then:
        assert controller.response.json.lists.size() == 2
        assert controller.response.json.lists[0].dataResourceUid == '3'
        assert controller.response.json.lists[1].dataResourceUid == '1'
    }

    void "test SpeciesLists default order, q filter"() {
        setup:
        controller.queryService = Mock(QueryService)
        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1",
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2",
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3",
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)

        when:
        controller.params.q = 'first'
        controller.getListDetails()

        then:
        assert controller.response.json.lists.size() == 2
        assert controller.response.json.lists[0].dataResourceUid == '1'
        assert controller.response.json.lists[1].dataResourceUid == '2'
    }
}
