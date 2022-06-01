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

package au.org.ala.specieslist

import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class SpeciesListTagLibSpec extends Specification implements TagLibUnitTest<SpeciesListTagLib> {

    void "test selectedFacetLink"() {
        given:
        def text = "http://localhost?q=Queensland&isSDS=eq:true"
        when:
        params.q = "Queensland"
        params.isSDS = "eq:true"
        params.isAuthoritative = "eq:true"
        def html = tagLib.selectedFacetLink([filter:"isAuthoritative"])
        then:
        html == text
    }

    void "test facetLink"() {
        given:
        def text = "http://localhost?q=Queensland&isSDS=eq:true"
        when:
        params.q = "Queensland"
        def html = tagLib.facetLink([filter:"isSDS=eq:true"])
        then:
        html == text
    }
}
