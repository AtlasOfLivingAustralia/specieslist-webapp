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
import au.org.ala.specieslist.*
import au.org.ala.web.AuthService
import com.opencsv.CSVReader
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ColumnMatchingServiceSpec extends Specification implements ServiceUnitTest<ColumnMatchingService>, DataTest {

    def columnMatchingService = new ColumnMatchingService()

    void setupSpec() {
        mockDomains(SpeciesListItem, SpeciesList, SpeciesListKVP)
    }

    def setup() {
        grailsApplication.config.commonNameColumns="commonname,vernacularname"
        grailsApplication.config.ambiguousNameColumns="name"
        grailsApplication.config.speciesNameColumns = "scientificname,suppliedname,taxonname,species"
        columnMatchingService.setConfiguration(grailsApplication.config)
    }


    def "camel case column names should be split by spaces before each uppercase character"() {
        when:
        def result = columnMatchingService.parseHeader(
                ["species", "AnyReallyLongCamelCaseHeaderName", "ÖsterreichName", "conservationCode"] as String[])

        then:
        assert result?.header?.contains("scientific name")
        assert result?.header?.contains("Any Really Long Camel Case Header Name")
        assert result?.header?.contains("Österreich Name");
        assert result?.header?.contains("conservationCode")
    }

}
