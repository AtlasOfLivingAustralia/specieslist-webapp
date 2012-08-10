package au.org.ala.specieslist

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*
import grails.converters.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class SpeciesListControllerTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testUserDetailsJson() {
        def ud = new UserDetailsCommand()
        ud.setName("Test List")
        println(new JSON(ud).toString(true))

        fail "Implement me"
    }
}
