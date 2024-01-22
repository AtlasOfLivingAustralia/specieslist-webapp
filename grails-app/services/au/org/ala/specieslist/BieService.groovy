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
import grails.converters.JSON
import grails.web.JSONBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.http.entity.ContentType

class BieService {

    def grailsApplication
    def webService
    def authService

    def bulkLookupSpecies(list) {
        Map map = [:]
        List jsonResponse = bulkSpeciesLookupWithGuids(list)
        jsonResponse.each {
            if (it && it.guid) {
                def guid = it.guid
                def image = it.smallImageUrl
                def commonName = it.commonNameSingle
                def scientificName = it.name
                def author = it.author
                map.put(guid, [image, commonName, scientificName, author])
            }
        }
        map
    }

    /**
     * TODO this functionality should use the ala-ws-plugin
     * @param list
     * @return
     */
    List bulkSpeciesLookupWithGuids(list) {
        def http = new HTTPBuilder(grailsApplication.config.bieService.baseURL + "/species/guids/bulklookup")
        http.getClient().getParams().setParameter("http.socket.timeout", grailsApplication.config.outboundhttp.timeout.toInteger())
        http.setHeaders(['User-Agent': "${grailsApplication.config.outboundhttp.useragent}"])
        def jsonBody = (list as JSON).toString()
        try {
            Map jsonResponse =  http.post(body: jsonBody, requestContentType:groovyx.net.http.ContentType.JSON)
            jsonResponse?.searchDTOList
        } catch(ex) {
            log.error("Unable to obtain species details from BIE - " + ex.getMessage(), ex)
            []
        }
    }

    def generateFieldGuide(druid,guids, email){
        def title = "The field guide for " + druid
        def link = grailsApplication.config.grails.serverURL + "/speciesListItem/list/" + druid
        try {
            def http = new HTTPBuilder(grailsApplication.config.fieldGuide.baseURL + "/generate" + "?email="+ email)
            http.setHeaders(['User-Agent': "${grailsApplication.config.outboundhttp.useragent}"])
            def response = http.post(body:  createJsonForFieldGuide(title, link, guids), requestContentType:ContentType.APPLICATION_JSON)
            return response
        } catch(ex) {
            log.error("Unable to generate field guide " ,ex)
            return null
        }
    }

    def createJsonForFieldGuide(t, l, g){
        def builder = new JSONBuilder()
        def result = builder.build {
            title = t
            link = l
            guids = g
        }
        result.toString(false)
    }
}
