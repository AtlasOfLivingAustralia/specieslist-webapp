package au.org.ala.specieslist
import grails.converters.JSON
import grails.web.JSONBuilder
import groovyx.net.http.HTTPBuilder

class BieService {

    def grailsApplication

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

    public List bulkSpeciesLookupWithGuids(list) {
        def http = new HTTPBuilder(grailsApplication.config.bieService.baseURL + "/species/guids/bulklookup.json")
        http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        def jsonBody = (list as JSON).toString()
        try {
            Map jsonResponse =  http.post(body: jsonBody, requestContentType:groovyx.net.http.ContentType.JSON)
            jsonResponse?.searchDTOList
        } catch(ex) {
            log.error("Unable to obtain species details from BIE - " + ex.getMessage(), ex)
            []
        }
    }

    def generateFieldGuide(druid,guids){
        def title = "The field guide for " + druid
        def link = grailsApplication.config.grails.serverURL + "/speciesListItems/list/" + druid
        try {
            def http = new HTTPBuilder(grailsApplication.config.fieldGuide.baseURL+"/generate")
            def response = http.post(body:  createJsonForFieldGuide(title, link, guids), requestContentType:groovyx.net.http.ContentType.JSON){ resp ->
                def responseURL = grailsApplication.config.fieldGuide.baseURL +"/guide/"+ resp.headers['fileId'].getValue()
                log.debug(responseURL)
                return responseURL
            }
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
