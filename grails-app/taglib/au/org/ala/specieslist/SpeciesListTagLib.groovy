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

class SpeciesListTagLib {
    static namespace = 'sl'
    static returnObjectForTags = ['buildFqList', 'excludedFqList']
    def userDetailsService

    def getFullNameForUserId = { attrs, body ->
        def displayName = userDetailsService.getFullListOfUserDetails().get(attrs.userId)?.displayName
        out << "${displayName?:attrs.userId}"
    }

    /**
     * Generates a list of filter query strings including that identified by the fq parameter, if supplied.
     *
     * @attr fqs REQUIRED the current list of filter query strings
     * @attr fq the additional filter query string to be added
     */
    def buildFqList = { attrs, body ->
        ArrayList ret = []
        def fq = attrs.fq
        if (attrs.fqs) {
            attrs.fqs.each {
                if (!ret.contains(it) && it != "") {
                    ret << it
                }
            }
        }
        if (fq && !ret.contains(fq)) {
            ret << fq
        }
        ret
    }

    /**
     * Generates a list of filter query strings without that identified by the fq parameter
     *
     * @attr fqs REQUIRED
     * @attr fq REQUIRED
     */
    def excludedFqList = { attrs, body ->
        def fq = attrs.fq
        def remainingFq = attrs.fqs - fq
        remainingFq
    }

    /**
     * Generates an HTML id from a string
     *
     * @attr key REQUIRED the value to use as id;
     * spaces will be replaced with hyphens, brackets will be removed
     * @attr prefix a prefix to use in the returned value;
     * a hyphen will be used to separate the prefix from the key, if provided
     */
    def facetAsId = { attrs, body ->
        def prefix = attrs.prefix ? attrs.prefix + "-" : ""
        out << prefix + attrs.key.replaceAll(" ", "-")
                .replaceAll("\\(", "").replaceAll("\\)", "")
                .toLowerCase()
    }


    def selectedFacetLink = { attrs ->
        def query = params.q
        def queryUrl = "?"
        if (query){
            queryUrl += "q=" + params.q
        }

        params.each { key, value ->
            if (attrs.filter != key && !(key in ["controller","action", "q"])) {
                if (queryUrl.length() > 1){
                    queryUrl += "&"
                }
                queryUrl += ( key + "=" + value)
            }
        }
        out << request.getRequestURL().toString() + queryUrl
    }

    def facetLink = { attrs ->
        def query = params.q
        def queryUrl = "?"
        if (query){
            queryUrl += "q=" + params.q
        }
        
        params.each { key, value ->
            if(!(key in ["controller","action", "q", "offset"])) {
                queryUrl += ("&" + key + "=" + value)
            }
        }

        queryUrl += ("&" + attrs.filter)
        out << request.getRequestURL().toString() + queryUrl
    }
}
