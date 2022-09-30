package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.testing.gorm.DataTest
import grails.testing.web.interceptor.InterceptorUnitTest
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SpeciesListDeleteInterceptorSpec extends Specification implements InterceptorUnitTest<SpeciesListDeleteInterceptor>, DataTest {

    def setupSpec(){
        mockDomains(SpeciesList)
    }

    def setup(){
        config.security.cas.bypass = false
        SpeciesListKVP.metaClass.static.executeQuery = { String query ->
            []
        }

        SpeciesListItem.metaClass.static.executeQuery = { String query, Collection params ->
            []
        }
        interceptor.localAuthService = Stub(LocalAuthService)
        interceptor.localAuthService.isAdmin() >> false
        interceptor.authService = Stub(AuthService)
        interceptor.authService.userId >> 666
    }

    def "list should return a HTTP 'not authorised' when current is not authorised to access a list - not a list owner and not in editor list"() {
        setup:

        // note a when a new SpeciesList is created, it will have have a `id` which will be sequential. Since the example  below is a first creation, the id  will be 1
        // current authenticated user id has been set to 666 in  setup.
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
            isPrivate: true, userId: 1234)
        speciesList.save(failOnError: true)
        speciesList.addToEditors("9876")
        when:
        params.id = "1"
        interceptor.before()
        withRequest(controller: controllerName, action: action)


        then:
        assert response.status == HttpStatus.SC_UNAUTHORIZED

        where:
        controllerName                                       |   action
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"
        "au.org.ala.specieslist.SpeciesListController"       |   "delete"
    }

    def "list should return a HTTP 'Ok' when current user is the list owner"() {
        setup:

        // note a when a new SpeciesList is created, it will have have a `id` which will be sequential. Since the example  below is a first creation, the id  will be 1
        // current authenticated user id has been set to 666 in  setup.
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
            isPrivate: true, userId: 666)
        speciesList.save(failOnError: true)
        speciesList.addToEditors("9876")
        when:
        params.id = "1"
        interceptor.before()
        withRequest(controller: controllerName, action: action)


        then:
        assert response.status == HttpStatus.SC_OK

        where:
        controllerName                                       |   action
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"
        "au.org.ala.specieslist.SpeciesListController"       |   "delete"
    }

    def "list should return a HTTP 'Ok' when current user is the list editors list"() {
        setup:

        // note a when a new SpeciesList is created, it will have have a `id` which will be sequential. Since the example  below is a first creation, the id  will be 1
        // current authenticated user id has been set to 666 in  setup.
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
            isPrivate: true, userId: 1313)
        speciesList.save(failOnError: true)
        speciesList.addToEditors("666")
        when:
        params.id = "1"
        interceptor.before()
        withRequest(controller: controllerName, action: action)


        then:
        assert response.status == HttpStatus.SC_OK

        where:
        controllerName                                       |   action
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"
        "au.org.ala.specieslist.SpeciesListController"       |   "delete"
    }
}
