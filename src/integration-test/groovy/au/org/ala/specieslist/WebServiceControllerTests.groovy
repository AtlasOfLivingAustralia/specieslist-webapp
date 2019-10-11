package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@TestFor(WebServiceController)
@Integration
@Rollback
class WebServiceControllerTests extends Specification{
    AuthService authService = Mock(AuthService)
    LocalAuthService localAuthService = Mock(LocalAuthService)
    QueryService queryService = Mock(QueryService)

    void setup() {
        controller.queryService = queryService
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }

        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1",
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2",
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3",
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)
    }

    void tearDown() {
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }
    }

    void "test SpeciesLists"() {
        when:
        controller.getListDetails()

        then:
        assert controller.response.json.lists.size() == 3
    }

    void "test SpeciesLists user order, q filtering"() {
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
        when:
        controller.params.q = 'first'
        controller.getListDetails()

        then:
        assert controller.response.json.lists.size() == 2
        assert controller.response.json.lists[0].dataResourceUid == '1'
        assert controller.response.json.lists[1].dataResourceUid == '2'
    }
}
