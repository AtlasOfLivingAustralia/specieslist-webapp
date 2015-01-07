package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(SecurityFilters)
@TestMixin([FiltersUnitTestMixin])
@Unroll
@Mock([SpeciesListItem, SpeciesList, SpeciesListKVP, SpeciesListItemController, AuthService, LocalAuthService])
class SecurityFiltersSpec extends Specification {

    def controller = Mock(SpeciesListItemController)

    def "list should return a HTTP 'not authorised' for private lists that the user cannot access"() {
        setup:

        defineBeans {
            authService(MockAuthService)
            localAuthService(MockLocalAuthService)
        }

        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)

        when:
        params.id = speciesList.dataResourceUid
        withFilters(controller: controllerName, action: action) {
            controller.action
        }

        then:
        assert response.status == HttpStatus.SC_UNAUTHORIZED

        where:
        controllerName      |   action
        "speciesListItem"   |   "list"
        "speciesListItem"   |   "listAuth"
        "speciesListItem"   |   "downloadList"
        "speciesList"       |   "deleteList"
    }

    def "list should return a HTTP 'OK' for private lists that the user can access"() {
        setup:

        defineBeans {
            authService(MockAuthService)
            localAuthService(MockLocalAuthService)
        }

        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 666)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)

        when:
        params.id = speciesList.dataResourceUid
        withFilters(controller: controllerName, action: action) {
            controller.action
        }

        then:
        assert response.status == HttpStatus.SC_OK

        where:
        controllerName      |   action
        "speciesListItem"   |   "list"
        "speciesListItem"   |   "listAuth"
        "speciesListItem"   |   "downloadList"
        "speciesList"       |   "deleteList"
    }

    public static class MockAuthService extends AuthService {
        @Override
        def getUserId() {
            666
        }
    }

    public static class MockLocalAuthService extends LocalAuthService {
        @Override
        def isAdmin() {
            false
        }
    }
}
