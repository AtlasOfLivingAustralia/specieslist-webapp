package au.org.ala.specieslist.service

import au.com.bytecode.opencsv.CSVReader
import au.org.ala.checklist.lucene.CBIndexSearch
import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.LocalAuthService
import au.org.ala.specieslist.SpeciesList
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.specieslist.SpeciesListKVP
import au.org.ala.web.AuthService
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
        setup:
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
        setup:
        helperService.setCbIdxSearcher(Mock(CBIndexSearch))

        when:
        def speciesItem = helperService.insertSpeciesItem((String[]) ["test"], "Dr1", -1, (String[]) ["Header1"],
                [:], 0)

        then:
        assert speciesItem.kvpValues.size() == 1
    }

    def "insertSpeciesItem should not default the rawScientificName to the last column if the species index is -1"() {
        setup:
        helperService.setCbIdxSearcher(Mock(CBIndexSearch))

        when:
        def row = ["Col1", "Col2", "Col3"]
        def header = ["Header1", "Header2", "Header3"]
        def speciesItem = helperService.insertSpeciesItem((String[]) row, "Dr1", -1, (String[]) header,
                [:], 0)

        then:
        assert speciesItem.rawScientificName == null
    }

    def "insertSpeciesItem should set the rawScientificName to the specified column if the species index is not -1"() {
        setup:
        helperService.setCbIdxSearcher(Mock(CBIndexSearch))

        when:
        def row = ["Col1", "Col2", "Col3"]
        def header = ["Header1", "Header2", "Header3"]
        def speciesItem = helperService.insertSpeciesItem((String[]) row, "Dr1", 1, (String[]) header,
                [:], 0)

        then:
        assert speciesItem.rawScientificName == "Col2"
    }

    def "loadSpeciesList should not create a list item if the scientific name cannot be determined"() {
        setup:
        CSVReader reader = Mock(CSVReader)
        reader.readNext() >>> ["Col1, Col2, Col3", null]

        helperService.setCbIdxSearcher(Mock(CBIndexSearch))
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))

        when:
        def itemCounts = helperService.loadSpeciesList(reader, "Dr1", "listname", null, "description",
                "url", "listWkt", false, false, "region", "authority", "category", "generalistaion", "sdsType",
                (String[]) ["Header1", "Header2", "Header3"], [:])

        then:
        assert itemCounts.totalRecords == 1 && itemCounts.successfulItems == 0
    }

    def "loadSpeciesList should create a list item if the scientific name can be determined"() {
        setup:
        CSVReader reader = Mock(CSVReader)
        reader.readNext() >>> ["Col1, Col2, Col3", null]

        helperService.setCbIdxSearcher(Mock(CBIndexSearch))
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))

        when:
        def itemCounts = helperService.loadSpeciesList(reader, "Dr1", "listname", null, "description",
                "url", "listWkt", false, false, "region", "authority", "category", "generalistaion", "sdsType",
                (String[]) ["Header1", "scientificname", "Header3"], [:])

        then:
        assert itemCounts.totalRecords == 1 && itemCounts.successfulItems == 1
    }
}
