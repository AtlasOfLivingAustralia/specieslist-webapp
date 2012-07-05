/*
 * Copyright (C) 2012 Atlas of Living Australia
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

package au.org.ala.bie.webapp2

/**
 * Redirect web services URIs to the (old) BIE webapp
 */
class LegacyWebServicesController {
    def webService
    def utilityService
    def config = org.codehaus.groovy.grails.commons.ConfigurationHolder.config
    def bieBaseUrl = config.bie.baseURL?:"http://bie.ala.org.au"

    /**
     * Search requests for JSON.
     * E.g., /search.json
     */
    def searchJson = {
        //def bieBaseUrl = grailsApplication.config.bie.baseURL?:"http://bie.ala.org.au"
        def paramString = request.getQueryString()
        log.debug "searchJson with params = ${paramString}"
        //redirect(permanent: true, url:"${bieBaseUrl}/ws/search.json?${paramString}")
        def resp = webService.get("${bieBaseUrl}/ws/search.json?${paramString}")
        render(contentType: utilityService.getJsonMimeType(params), text: resp)
    }

    /**
     * Search requests for XML.
     * E.g., /search.xml
     */
    def searchXml = {
        //def bieBaseUrl = grailsApplication.config.bie.baseURL?:"http://bie.ala.org.au"
        def paramString = request.getQueryString()
        log.debug "searchXml with params = ${paramString}"
        //redirect(permanent: true, url:"${bieBaseUrl}/ws/search.xml?${paramString}")
        render(contentType: "text/xml", text: webService.get("${bieBaseUrl}/ws/search.xml?${paramString}"))
    }

    /**
     * Autocomplete service
     */
    def autoCompleteJson = {
        def jsonExt = (params.jsonp) ? "jsonp" : "json"
        def paramString = request.getQueryString()
        log.debug "autoCompleteJson with jsonExt = ${jsonExt}"
        def resp = webService.get("${bieBaseUrl}/ws/search/auto.${jsonExt}?${paramString}")
        log.debug "response = " + resp
        render(contentType: utilityService.getJsonMimeType(params), text: resp)
    }

    /**
     * Web service for image-search
     */
    def imageSearchJson = {
        def action = params.type?:"showSpecies"
        def paramString = request.getQueryString()
        log.debug "imageSearchJson with params = ${paramString}"
        log.debug "imageSearchJson with action = ${action}"
        def resp = webService.get("${bieBaseUrl}/ws/image-search/${action}.json?${paramString}")
        render(contentType: utilityService.getJsonMimeType(params), text: resp)
    }

    /**
     * Image ranking service
     */
    def imageRankJson = {
        log.debug "imageRankJson with withUser = ${params.withUser}"
        def action = params.withUser ? "rankTaxonImageWithUser" : "rankTaxonImage"
        def paramString = request.getQueryString()
        def resp = webService.get("${bieBaseUrl}/ws/${action}.json?${paramString}")
        render(contentType: utilityService.getJsonMimeType(params), text: resp)
    }

    /**
     * Common name ranking service
     */
    def nameRankJson = {
        log.debug "nameRankJson with withUser = ${params.withUser}"
        def action = params.withUser ? "rankTaxonCommonNameWithUser" : "rankTaxonCommonName"
        def paramString = request.getQueryString()
        def resp = webService.get("${bieBaseUrl}/ws/${action}.json?${paramString}")
        render(contentType: utilityService.getJsonMimeType(params), text: resp)
    }

    /**
     * Species page requests for JSON.
     * E.g., /species/${guid|name}.json, /species/shortProfile/{guid}.json, /species/info/{guid}.json,
     *   /species/document/{docId}.json, /species/status/{statusId}.json, /species/moreInfo/{guid}.json
     *   /species/namesFromGuid/{guid}.json, /species/image/{imageType}/{guid:.+},
     *   /species/synonymsForGuid/{guid}.json,
     */
    def speciesJson = {
        //def bieBaseUrl = grailsApplication.config.bie.baseURL?:"http://bie.ala.org.au"
        def guid = URLEncoder.encode(params.guid)
        log.debug "speciesJson with guid = ${guid}"
        log.debug "path1 = " + params.path1
        def optPath1 = params.path1 ? URLEncoder.encode(params.path1) + "/" : ""
        def optPath2 = params.path2 ? URLEncoder.encode(params.path2) + "/" : ""
        def optParams = request.getQueryString() ? "?" + request.getQueryString() : ""

        if (request.post && guid == "bulklookup") {
            // POST lookups for guids
            def url = bieBaseUrl + "/ws/species/" + optPath1 + "bulklookup.json"
            def body = request.reader.text
            def json = webService.doPost(url, "", "", body)
            render (contentType: "application/json", text: json.resp)
        } else {
            //redirect(permanent: true, url:"${bieBaseUrl}/ws/species/${optPath1}${optPath2}${guid}.json${optParams}")
            render(contentType: utilityService.getJsonMimeType(params), text: webService.get("${bieBaseUrl}/ws/species/${optPath1}${optPath2}${guid}.json${optParams}"))
        }

    }

    /**
     * Species page requests for XML.
     * E.g., /species/${guid|name}.xml
     */
    def speciesXml = {
        //def bieBaseUrl = grailsApplication.config.bie.baseURL?:"http://bie.ala.org.au"
        def guid = URLEncoder.encode(params.guid)
        log.debug "speciesXml with guid = ${guid}"
        //redirect(permanent: true, url:"${bieBaseUrl}/ws/species/${guid}.xml")
        render(contentType: "text/xml", text: webService.get("${bieBaseUrl}/ws/species/${guid}.xml"))
    }

    /**
     * Bulk lookups for names/guids using POST with JSON body
     */
    def speciesJsonPost = {
        log.debug "speciesJsonPost: guids = " + guids
        def body = request.reader.text
    }

    /**
     * Admin pages
     * E.g., /admin/${path}**
     */
    def admin = {
        def path = params.path
        log.debug "admin with path = ${path}"
        redirect(permanent: true, url:"${bieBaseUrl}/ws/admin/${path}")
    }
}
