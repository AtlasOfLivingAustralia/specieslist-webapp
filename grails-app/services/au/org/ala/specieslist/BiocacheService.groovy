package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder
import grails.web.JSONBuilder

class BiocacheService {

    def grailsApplication

    def performBatchSearchOrDownload(guids, action, title) {
        def http = new HTTPBuilder(grailsApplication.config.biocacheService.baseURL +"/occurrences/batchSearch")
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        def postBody = [field:'lsid',queries: guids.join(","),
                separator:',',redirectBase:grailsApplication.config.biocache.baseURL+"/occurrences/search",
                action:action, title:title]
        log.debug "action = " + action
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

    /**
     * WKT version of batch search
     *
     * @param guids
     * @param action
     * @param title
     * @param wkt
     * @return
     */
    def performBatchSearchOrDownload(guids, unMatchedNames, downloadDto, title, wkt) {
        def http = new HTTPBuilder(grailsApplication.config.biocacheService.baseURL +"/webportal/params")
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        def query

        if (guids) {
            query = "lsid:\"" + guids.join("\" OR lsid:\"") + "\""
        }

        if (unMatchedNames) {
            query += ((query) ? " OR " : "") + "raw_name:\"" + unMatchedNames.collect{ it.trim() }.join("\" OR raw_name:\"") + "\""
        }

        def postBody = [q:query, wkt: wkt, title: title]
        log.debug "postBody = " + postBody
        log.debug "action = " + downloadDto.type

        try{
            http.post(body: postBody, requestContentType:groovyx.net.http.ContentType.URLENC){ resp, reader->
                //return the location in the header
                log.debug(resp.headers)

                if(resp.status == 302){
                    return  resp.headers['location'].getValue()
                }
                else if (resp.status == 200) {
                    log.debug "200 OK reponse"
                    //log.debug "text = " + reader.getText()
                    def qid = reader.getText()
                    def returnUrl = grailsApplication.config.biocache.baseURL
                    switch ( downloadDto.type ) {
                        case "Search":
                            returnUrl += "/occurrences/search?q=qid:" + qid
                            break
                        case "Download":
                            returnUrl += "/ws/occurrences/download?q=qid:" + qid + "&file=" + downloadDto.file
                            returnUrl += "&reason=" + downloadDto.reasonTypeId + "&email=" + downloadDto.email
                            break
                    }

                    return returnUrl
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
        log.debug(result.toString(true))
        result.toString(false)
    }
}
