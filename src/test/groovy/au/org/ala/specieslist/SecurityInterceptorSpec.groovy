package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.InterceptorUnitTestMixin
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin([InterceptorUnitTestMixin, GrailsUnitTestMixin])
@Unroll
@TestFor(SecurityInterceptor)
@Mock([SpeciesListItem, SpeciesList, SpeciesListKVP, SpeciesListItemController, AuthService, LocalAuthService])
class SecurityInterceptorSpec extends Specification {

    def setup(){
        SpeciesListKVP.metaClass.static.executeQuery = { String query ->
            []
        }

        SpeciesListItem.metaClass.static.executeQuery = { String query, Collection params ->
            []
        }
    }

    def setupSpec(){
        defineBeans {
            authService(MockAuthService)
            localAuthService(MockLocalAuthService)
            helperService(MockHelperService)
            queryService(MockQueryService)
        }
    }

    def "list should return a HTTP 'not authorised' for private lists that the user cannot access - #action"() {
        setup:
//        def controller = mockController(Class.forName(controllerName))
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)

        when:
        params.id = speciesList.dataResourceUid
//        mockInterceptor(SecurityInterceptor)
        interceptor.before()
        withRequest(controller: controllerName, action: action)
//        withInterceptors(action: action) {
//            controller."${action}"()
//        }

        then:
        assert response.status == HttpStatus.SC_UNAUTHORIZED

        where:
        controllerName      |   action
        "SpeciesListItemController"   |   "list"
        "SpeciesListItemController"   |   "listAuth"
        "SpeciesListItemController"   |   "downloadList"
        "SpeciesListController"       |   "deleteList"

//        "au.org.ala.specieslist.SpeciesListItemController"   |   "list"
//        "au.org.ala.specieslist.SpeciesListItemController"   |   "listAuth"
//        "au.org.ala.specieslist.SpeciesListItemController"   |   "downloadList"
//        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"
    }

    def "list should return a HTTP 'OK' for private lists that the user can access - #action"() {
        setup:
        def controller = mockController(Class.forName(controllerName))
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 666)
        speciesList.addToEditors("9876")
        speciesList.save(flush: true, failOnError: true)

        when:
        params.id = speciesList.dataResourceUid
        withInterceptors(controller: controllerName, action: action) {
            controller."$action"()
        }

        then:
        assert response.status == status

        where:
        controllerName                                       |   action         | status
        "au.org.ala.specieslist.SpeciesListItemController"   |   "list"         | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListItemController"   |   "listAuth"     | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListItemController"   |   "downloadList" | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"   | HttpStatus.SC_MOVED_TEMPORARILY
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

    public static class MockHelperService extends HelperService {
        @Override
        def deleteDataResourceForList(drId) {
            false
        }
    }

    public static class MockQueryService extends QueryService {
        @Override
        def constructWithFacets(String base, List facets, String dataResourceUid) {
            [ '', [] ]
        }
    }
}
