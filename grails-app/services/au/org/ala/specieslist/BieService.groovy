package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder

import grails.converters.JSON
import grails.web.JSONBuilder

class BieService {

    def grailsApplication

    /** The path to the repository */
    protected String repositoryPath = "/data/bie/";
    /** The URL to the repository */
    protected String repositoryUrl = "http://bie.ala.org.au/repo/";

    def bulkLookupSpecies(list) {
        def http = new HTTPBuilder(grailsApplication.config.bieService.baseURL +"/species/guids/bulklookup.json")
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
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
                def image = it.smallImageUrl
                def commonName = it.commonNameSingle
                def scientificName = it.name
                def author = it.author
                map.put(guid, [image,commonName, scientificName,author])
            }
            log.debug(map)
            return map
        }
        catch(ex){
            log.error("Unable to obtain species details from BIE", ex)
            return map
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
        def link = grailsApplication.config.grails.serverURL + "/speciesListItems/list/"+druid//"http://natasha.ala.org.au:8080/specieslist-webapp/speciesListItems/list/"+druid

//        fg.guids.addAll(guids)
        try{
            def http = new HTTPBuilder(grailsApplication.config.fieldGuide.baseURL+"/generate")
            def response = http.post(body:  createJsonForFieldGuide(title, link, guids), requestContentType:groovyx.net.http.ContentType.JSON){ resp ->
                def responseURL = grailsApplication.config.fieldGuide.baseURL +"/guide/"+ resp.headers['fileId'].getValue()
                log.debug(responseURL)
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


