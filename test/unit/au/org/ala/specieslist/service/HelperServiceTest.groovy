package au.org.ala.specieslist.service

import au.org.ala.specieslist.HelperService
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(HelperService)
@TestMixin(GrailsUnitTestMixin)
@Unroll
class HelperServiceTest extends Specification {

    def helperService = new HelperService()

    def setup() {
        helperService.grailsApplication = grailsApplication
    }

    def "addDataResourceForList should return a dummy url when collectory.enableSync is not true"() {
        when:
        grailsApplication.config.collectory.enableSync = item
        grailsApplication.config.collectory.baseURL = "http://blabla.com"

        then:
        String url = helperService.addDataResourceForList([:])
        assert url.contains("tmp/drt")

        where:
        item << [false, "false", " ", "", null]
    }

    def "updateDataResourceForList should return a dummy url when collectory.enableSync is not true"() {
        when:
        grailsApplication.config.collectory.enableSync = item
        grailsApplication.config.collectory.baseURL = "http://blabla.com"

        then:
        String url = helperService.updateDataResourceForList("id", [:])
        assert url.contains("tmp/drt")

        where:
        item << [false, "false", " ", "", null]
    }

}
