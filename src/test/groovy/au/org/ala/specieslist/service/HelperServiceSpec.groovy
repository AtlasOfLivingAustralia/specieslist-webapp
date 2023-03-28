/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.specieslist.service

import au.org.ala.names.ws.api.NameUsageMatch
import au.org.ala.names.ws.api.SearchStyle
import au.org.ala.specieslist.*
import au.org.ala.web.AuthService
import au.org.ala.web.UserDetails
import com.opencsv.CSVReader
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class HelperServiceSpec extends Specification implements ServiceUnitTest<HelperService>, DataTest {

    def helperService = new HelperService()
    def columnMatchingService = new ColumnMatchingService()
    def nameExplorerService = Mock(NameExplorerService)

    void setupSpec() {
        mockDomains(SpeciesListItem, SpeciesList, SpeciesListKVP)
    }

    def setup() {
        grailsApplication.config.commonNameColumns="commonname,vernacularname"
        grailsApplication.config.ambiguousNameColumns="name"
        grailsApplication.config.speciesNameColumns = "scientificname,suppliedname,taxonname,species"
//        helperService.transactionManager = transactionManager
        columnMatchingService.setConfiguration(grailsApplication.config)
        helperService.columnMatchingService = columnMatchingService
        helperService.grailsApplication = grailsApplication
        helperService.init()
        helperService.nameExplorerService = nameExplorerService
    }

    def addFindResult() {
        def result = NameUsageMatch.builder()
                .success(true)
                .scientificName("Scientific name")
                .scientificNameAuthorship("Scientific Name Authorship")
                .taxonConceptID("aTaxonConceptID")
                .kingdom("Akingdom")
                .family("Afamily")
                .vernacularName(" A Vernacular Name")
                .build()
        nameExplorerService.find(_, _) >>> result
    }

    def "addDataResourceForList should return a dummy url when collectory.enableSync is not true - #item"() {
        when:
        grailsApplication.config.collectory.enableSync = item
        grailsApplication.config.collectory.baseURL = "http://blabla.com"

        then:
        String url = helperService.addDataResourceForList([:])
        assert url.contains("tmp/drt")

        where:
        item << [false, "false", " ", "", null]
    }

    def "updateDataResourceForList should return a dummy url when collectory.enableSync is not true - #item"() {
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
        when:
        def speciesList = new SpeciesList()
        this.addFindResult()
        def speciesItem = helperService.insertSpeciesItem((String[]) [blank], "Dr1", -1, (String[]) ["Header"], [:], 0, speciesList)

        then:
        assert speciesItem.kvpValues == null || speciesItem.kvpValues.empty

        where:
        blank << [" ", "\t", "\n", "   ", ""]
    }

    def "insertSpeciesItem should create a KVP record for a non-blank value"() {
        when:
        def speciesList = new SpeciesList()
        this.addFindResult()
        def speciesItem = helperService.insertSpeciesItem((String[]) ["test"], "Dr1", -1, (String[]) ["Header"], [:], 0, speciesList)

        then:
        assert speciesItem.kvpValues.size() == 1
    }

    def "insertSpeciesItem should not default the rawScientificName to the last column if the species index is -1"() {
        when:
        def speciesList = new SpeciesList()
        this.addFindResult()
        def row = ["Col1", "Col2", "Col3"]
        def header = ["Header1", "Header2", "Header3"]
        def speciesItem = helperService.insertSpeciesItem((String[]) row, "Dr1", -1, (String[]) header, [:], 0, speciesList)

        then:
        assert speciesItem.rawScientificName == null
    }

    def "insertSpeciesItem should set the rawScientificName to the specified column if the species index is not -1"() {
        when:
        def speciesList = new SpeciesList()
        this.addFindResult()
        def row = ["Col1", "Col2", "Col3"]
        def header = ["Header1", "Header2", "Header3"]
        def speciesItem = helperService.insertSpeciesItem((String[]) row, "Dr1", 1, (String[]) header, [:], 0, speciesList)

        then:
        assert speciesItem.rawScientificName == "Col2"
    }

    def "insertSpeciesItem should accept match requests"() {
        when:
        def speciesList = new SpeciesList()
        speciesList.looseSearch = true
        speciesList.searchStyle = SearchStyle.MATCH
        this.addFindResult()
        def row = ["Col1", "Col2", "Col3"]
        def header = ["Header1", "Header2", "Header3"]
        def speciesItem = helperService.insertSpeciesItem((String[]) row, "Dr1", 1, (String[]) header, [:], 0, speciesList)

        then:
        assert speciesItem.matchedName == "Scientific name"
    }

    def "loadSpeciesListFromCSV should not create a list item if the scientific name cannot be determined"() {
        setup:
        CSVReader reader = Mock(CSVReader)
        reader.readNext() >>> ["Col1, Col2, Col3", null]

        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))

        when:
        def itemCounts = helperService.loadSpeciesListFromCSV(reader, "Dr1", "listname", null, "description",
                "url", "listWkt", false, false, false, false, false, false, "region", "authority", "category", "generalistaion", "sdsType",
                false, SearchStyle.STRICT, (String[]) ["Header1", "Header2", "Header3"], [:])

        then:
        assert itemCounts.totalRecords == 1 && itemCounts.successfulItems == 0
    }

    def "loadSpeciesListFromCSV should create a list item if the scientific name can be determined"() {
        setup:
        CSVReader reader = Mock(CSVReader)
        reader.readNext() >>> ["Col1, Col2, Col3", null]

        NameExplorerService nameExplorerService = Mock(NameExplorerService)

        NameUsageMatch result = new NameUsageMatch(true, "scientificName", "scientificNameAuthorship",
                "taxonConceptID", null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                "family", null, null, null, null,
                null, null, null, null,
                null)
        nameExplorerService.searchForRecordByTerms(*_) >>> result

        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        helperService.setNameExplorerService(nameExplorerService)

        when:
        def itemCounts = helperService.loadSpeciesListFromCSV(reader, "Dr1", "listname", null, "description",
                "url", "listWkt", false, false, false, false, false, true, "region", "authority", "category", "generalistaion", "sdsType",
                false, SearchStyle.STRICT, (String[]) ["Header1", "scientificname", "Header3"], [:])

        then:
        assert itemCounts.totalRecords == 1 && itemCounts.successfulItems == 1
    }

    def "loadSpeciesListFromJSON should update an existing list when matching by Data Resource ID"() {
        setup:
        this.addFindResult()
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        UserDetailsService userDetailsService = Mock(UserDetailsService)
        userDetailsService.getCurrentUserDetails() >> Mock(UserDetails)
        helperService.setUserDetailsService(userDetailsService)
        String version1Json = "{\"listName\": \"list1\",  \"listType\": \"TEST\", \"listItems\": \"item1,item2\"}"

        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1")
        SpeciesListItem item1 = new SpeciesListItem(itemOrder: 1, rawScientificName: "item1", dataResourceUid: "dr1",
                mylist: speciesList)
        item1.save(failOnError: true)
        speciesList.addToItems(item1)
        speciesList.save(failOnError: true)

        mockDomain(SpeciesList, [speciesList])

        JSONObject json = new JSONObject(version1Json)
        json.username = "fred"

        when:
        Map list = helperService.loadSpeciesListFromJSON(json, "dr1")

        then:
        assert list.speciesList.is(speciesList) // instance equality: the existing entity should be used in this case
        assert list.speciesList.items.size() == 2 // there was 1 existing item, but the system REPLACES items rather than adding
    }

    def "loadSpeciesListFromJSON should create a new list if there is no match on the Data Resource ID"() {
        setup:
        this.addFindResult()
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        UserDetailsService userDetailsService = Mock(UserDetailsService)
        userDetailsService.getCurrentUserDetails() >> Mock(UserDetails)
        helperService.setUserDetailsService(userDetailsService)
        String version1Json = "{\"listName\": \"list1\",  \"listType\": \"TEST\", \"listItems\": \"item1,item2\"}"

        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1")
        SpeciesListItem item1 = new SpeciesListItem(itemOrder: 1, rawScientificName: "item1", dataResourceUid: "dr1",
                mylist: speciesList)
        item1.save(failOnError: true)
        speciesList.addToItems(item1)
        speciesList.save(failOnError: true)

        mockDomain(SpeciesList, [speciesList])

        JSONObject json = new JSONObject(version1Json)
        json.username = "fred"

        when:
        SpeciesList list = helperService.loadSpeciesListFromJSON(json, "noMatch")

        then:
        assert !list.is(speciesList) // the existing entity should NOT be used because the DR UIDs don't match
    }

    def "loadSpeciesListFromJSON should throw an exception when the item list structure is not recognised"() {
        setup:
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        helperService.setUserDetailsService(Mock(UserDetailsService))
        String unknownJson = "{\"listName\": \"list1\",  \"listType\": \"TEST\", \"SomethingNotRecognised\": \"\"}"

        when:
        helperService.loadSpeciesListFromJSON(new JSONObject(unknownJson), "noMatch", false)

        then:
        thrown UnsupportedOperationException
    }

    def "loadSpeciesListFromJSON for v2 should create list items with no KVP if the JSON request has no KVP details"() {
        setup:
        this.addFindResult()
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        UserDetailsService userDetailsService = Mock(UserDetailsService)
        userDetailsService.getCurrentUserDetails() >> {}
        userDetailsService.getCurrentUserDetails() >> {}
        helperService.setUserDetailsService(userDetailsService)
        String version2JsonWithNoKVP = """{
                                    "listName": "list1",
                                    "listType": "TEST",
                                    "listItems": [
                                        {
                                            "itemName": "item1",
                                            "kvpValues": []
                                        },
                                        {
                                            "itemName": "item2",
                                            "kvpValues": []
                                        }
                                    ]
                                }"""

        JSONObject json = new JSONObject(version2JsonWithNoKVP)
        json.username = "fred"

        when:
        Map list = helperService.loadSpeciesListFromJSON(json, "dr1")

        then:
        assert list.speciesList.items.size() == 2
        assert list.speciesList.items[0].kvpValues == null
        assert list.speciesList.items[1].kvpValues == null
    }

    def "loadSpeciesListFromJSON for v1 should throw an exception when there are no list items"() {
        setup:
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        helperService.setUserDetailsService(Mock(UserDetailsService))
        String version1Json = "{\"listName\": \"list1\",  \"listType\": \"TEST\", \"listItems\": \"\"}"

        when:
        helperService.loadSpeciesListFromJSON(new JSONObject(version1Json), "noMatch")

        then:
        thrown AssertionError
    }

    def "loadSpeciesListFromJSON for v2 should throw an exception when there are no list items"() {
        setup:
        helperService.setLocalAuthService(Mock(LocalAuthService))
        helperService.setAuthService(Mock(AuthService))
        helperService.setUserDetailsService(Mock(UserDetailsService))
        String version2Json = "{ \"listName\": \"list1\", \"listType\": \"TEST\", \"listItems\": [] }"

        when:
        helperService.loadSpeciesListFromJSON(new JSONObject(version2Json), "noMatch")

        then:
        thrown AssertionError
    }

    def "urls in submitted text should be turned into links"() {
        when:
        def result = helperService.parseRow(
                ["Banksia brownii", "pink/red <unknown>", "email.address@example.com",
                 "A rare species, see https://www.example.com/something/234325"])

        then:
        assert result.contains("Banksia brownii")
        assert result.contains("pink/red <unknown>")
        assert result.contains("email.address@example.com")
        assert result.contains('A rare species, see <a href="https://www.example.com/something/234325">' +
                'https://www.example.com/something/234325</a>')
    }
}
