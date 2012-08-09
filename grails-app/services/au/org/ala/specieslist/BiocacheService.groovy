package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder
import grails.web.JSONBuilder

class BiocacheService {

    def grailsApplication

    def performBatchSearchOrDownload(guids,action) {
        def http = new HTTPBuilder(grailsApplication.config.biocacheService.baseURL +"/occurrences/batchSearch")
        def postBody = [field:'lsid',queries: guids.join(","),
                separator:',',redirectBase:grailsApplication.config.biocache.baseURL+"/occurrences/search",
                action:action]
        try{
        http.post(body: postBody, requestContentType:groovyx.net.http.ContentType.URLENC){resp->
            //return the location in the header
            log.debug(resp.headers)

            if(resp.status == 302){
                return  resp.headers['location'].getValue()
            }
            else return null;
        }
        }
        catch(ex){
            log.error("Unable to get occurrences: " ,ex)
            return null;
        }


    }
    //Location	http://biocache.ala.org.au/occurrences/search?q=qid:1344230443917
    def createJsonForBatch(guids){
        def builder = new JSONBuilder()

        def result = builder.build{
            queries = guids.join(",")
            field = "lsid"
            separator = ","
            redirectBase = grailsApplication.config.biocache.baseURL
        }
        println(result.toString(true))
        result.toString(false)
    }
}
