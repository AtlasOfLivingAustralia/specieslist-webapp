package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder
import grails.web.JSONBuilder

class BiocacheService {

    def grailsApplication

    def getQid(guids, unMatchedNames, title, wkt){
        def http = new HTTPBuilder(grailsApplication.config.biocacheService.baseURL +"/webportal/params")
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        def query = ""

        if (guids) {
            query = "lsid:" + guids.join(" OR lsid:")
        }

        if (unMatchedNames) {
            query += ((query) ? " OR " : "") + "raw_name:\"" + unMatchedNames.collect{ it.trim() }.join("\" OR raw_name:\"") + "\""
        }

        def postBody = [q:query, wkt: wkt, title: title]
        log.debug "postBody = " + postBody

        try {
            http.post(body: postBody, requestContentType:groovyx.net.http.ContentType.URLENC){ resp, reader ->
                //return the location in the header
                log.debug(resp.headers)
                if (resp.status == 302) {
                    log.debug "302 redirect response from biocache"
                    return [status:resp.status, result:resp.headers['location'].getValue()]
                } else if (resp.status == 200) {
                    log.debug "200 OK response from biocache"
                    return [status:resp.status, result:reader.getText()]
                } else {
                    return [status:500]
                }
            }
        } catch(ex) {
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

        def resp = getQid(guids, unMatchedNames, title, wkt)
        if(resp.status == 302){
            resp.result
        } else if (resp.status == 200) {
            log.debug "200 OK response"
            def qid = resp.result
            def returnUrl = grailsApplication.config.biocache.baseURL
            switch ( downloadDto.type ) {
                case "Search":
                    returnUrl += "/occurrences/search?q=qid:" + qid
                    break
                case "Download":
                    returnUrl += "/ws/occurrences/index/download?q=qid:" + qid + "&file=" + downloadDto.file
                    returnUrl += "&reason=" + downloadDto.reasonTypeId + "&email=" + downloadDto.email
                    break
            }
            returnUrl
        } else {
            null
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
