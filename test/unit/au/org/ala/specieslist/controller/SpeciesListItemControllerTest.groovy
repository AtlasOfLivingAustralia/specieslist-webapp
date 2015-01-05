package au.org.ala.specieslist.controller

import au.org.ala.specieslist.LocalAuthService
import au.org.ala.specieslist.SecurityUtil
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListItemController
import au.org.ala.specieslist.SpeciesListKVP
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(SpeciesListItemController)
@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
@Unroll
@Mock([SpeciesListItem, SpeciesList, SpeciesListKVP])
class SpeciesListItemControllerTest extends Specification {

    def controller = new SpeciesListItemController()

    def "list should return a HTTP 'not authorised' for private lists that the user cannot access"() {
        setup:
        SecurityUtil util = Mock(SecurityUtil)
        util.checkListAccess(any()) >> false
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)
        def listId = speciesList.dataResourceUid
        controller.securityUtil = util
        controller.localAuthService = [isUserLoggedInViaCookie: false] as LocalAuthService

        when:

        params.id = listId
        controller.list()

        then:
        assert response.status == HttpStatus.SC_UNAUTHORIZED
    }

}
