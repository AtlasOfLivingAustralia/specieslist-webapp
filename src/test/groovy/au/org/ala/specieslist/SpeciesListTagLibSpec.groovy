package au.org.ala.specieslist

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SpeciesListTagLib)
class SpeciesListTagLibSpec {

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
