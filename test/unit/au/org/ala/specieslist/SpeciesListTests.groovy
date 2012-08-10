package au.org.ala.specieslist



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(SpeciesList)
class SpeciesListTests {

    void testCreate() {
        mockDomain(SpeciesList)

        def sl = new SpeciesList()
        sl.listName = "List"
        sl.dataResourceUid="drxyz"
        sl.username = "testuser"
        sl.firstName = "test"
        sl.surname = "user"

        assertTrue 'no errors should have occurred', sl.validate()
    }
}
