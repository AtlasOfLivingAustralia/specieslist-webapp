package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
@Mock([SpeciesList])
class SecurityUtilTest extends Specification {

    def authService = Mock(AuthService)
    def localAuthService = Mock(LocalAuthService)

    def "checkListAccess should return true for public lists"() {
        setup:
        authService.getUserId() >> 1234
        localAuthService.isAdmin() >> false

        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: false).save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a public list"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should always return true"
        assert canAccess

    }

    def "checkListAccess should return true for admin users"() {
        setup:
        authService.getUserId() >> 1234
        localAuthService.isAdmin() >> true
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true).save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a private list but the user is an admin"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should always return true"
        assert canAccess
    }

    def "checkListAccess should return true for owners of private lists"() {
        setup:
        authService.getUserId() >> 1234
        localAuthService.isAdmin() >> false
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234).save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a private list that the user owns"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should return true"
        assert canAccess
    }

    def "checkListAccess should return true for editors of private lists"() {
        setup:
        authService.getUserId() >> 9876
        localAuthService.isAdmin() >> false
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a private list that the user doesn't own but can edit"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should return true"
        assert canAccess
    }

    def "checkListAccess should return false for private lists when there is no authenticated user"() {
        setup:
        authService.getUserId() >> null
        localAuthService.isAdmin() >> false
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234).save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a private list when there is no logged in user"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should always return false"
        assert !canAccess
    }

    def "checkListAccess should return true for public lists even when there is no authenticated user"() {
        setup:
        authService.getUserId() >> null
        localAuthService.isAdmin() >> false
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: false).save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when: "checkListAccess is called for a public list when there is no logged in user"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should always return true"
        assert canAccess
    }

    def "checkListAccess should return false for private lists that the user cannot access"() {
        setup:
        authService.getUserId() >> 666
        localAuthService.isAdmin() >> false
        SecurityUtil util = new SecurityUtil(authService: authService, localAuthService: localAuthService)
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)
        def listId = speciesList.dataResourceUid

        when:
        "checkListAccess is called for a private list that the user does not own and cannot edit (and is not " +
                "an admin)"

        boolean canAccess = util.checkListAccess(listId)

        then: "it should return false"
        assert !canAccess
    }


}
