package au.org.ala.specieslist.service

import au.org.ala.checklist.lucene.CBIndexSearch
import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListKVP
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(HelperService)
@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
@Unroll
@Mock([SpeciesListItem, SpeciesList, SpeciesListKVP])
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

    def "insertSpeciesItem should not create a KVP record for a blank value"() {

        helperService.setCbIdxSearcher(Mock(CBIndexSearch))

        when:
        def speciesItem = helperService.insertSpeciesItem((String[]) [blank], "Dr1", -1, (String[]) ["Header1"],
                [:], 0)

        then:
        assert speciesItem.kvpValues == null || speciesItem.kvpValues.empty

        where:
        blank << [" ", "\t", "\n", "   ", ""]
    }

    def "insertSpeciesItem should create a KVP record for a non-blank value"() {

        helperService.setCbIdxSearcher(Mock(CBIndexSearch))

        when:
        def speciesItem = helperService.insertSpeciesItem((String[]) ["test"], "Dr1", -1, (String[]) ["Header1"],
                [:], 0)

        then:
        assert speciesItem.kvpValues.size() == 1
    }
}
