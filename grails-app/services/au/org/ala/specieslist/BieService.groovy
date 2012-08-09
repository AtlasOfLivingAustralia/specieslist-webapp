package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder

import grails.converters.JSON
import grails.web.JSONBuilder

class BieService {

    def grailsApplication


    def bulkLookupSpecies(list) {
        def http = new HTTPBuilder(grailsApplication.config.bie.baseURL +"/ws/species/bulklookup.json")
        def builder = new JSONBuilder()
        def map =[:]
        def jsonBody = "[\""
        jsonBody +=list.join("\",\"")//each{jsonBody += "\""+it+"\""}
                jsonBody += "\"]"
//        def jsonBody = list as JSON
        log.debug(jsonBody)
        try{
            def jsonResponse =  http.post(body: jsonBody, requestContentType:groovyx.net.http.ContentType.JSON)
            log.debug(jsonResponse)
            jsonResponse.getJSONArray("searchDTOList").toArray().each{
                def guid = it.guid
                def image = it.thumbnail
                def commonName = it.commonNameSingle
                map.put(guid, [image,commonName])
            }
            log.debug(map)
            return map
        }
        catch(ex){
            log.error("Unable to obtain species details from BIE", ex)
        }
//        http.post(body: jsonBody, requestContentType:groovyx.net.http.ContentType.JSON){ resp ->
//            if(resp?.statusLine?.statusCode == 200){
//                //we know the request was successful
//
//            }
//            else{
//
//            }
//         }
    }

    def generateFieldGuide(druid,guids){
        def title = "The field guide for " + druid
        def link = "http://natasha.ala.org.au:8080/specieslist-webapp/speciesListItems/list/"+druid

//        fg.guids.addAll(guids)
        try{
            def http = new HTTPBuilder(grailsApplication.config.fieldGuide.baseURL+"/generate")
            def response = http.post(body:  createJsonForFieldGuide(title, link, guids), requestContentType:groovyx.net.http.ContentType.JSON){ resp ->
                def responseURL = grailsApplication.config.fieldGuide.baseURL +"/guide/"+ resp.headers['fileId'].getValue()
                println(responseURL)
                return responseURL
            }
        }
        catch(ex){
            log.error("Unable to generate field guide " ,ex)
            return null
        }

    }

    def createJsonForFieldGuide(t, l, g){
        def builder = new JSONBuilder()

        def result = builder.build{
            title = t
            link = l
            guids = g

        }
//        println(result.toString(true))
        result.toString(false)
    }
}


