package au.org.ala.bie.webapp2

import grails.converters.JSON
import org.ala.dto.ExtendedTaxonConceptDTO
import org.codehaus.groovy.grails.web.json.JSONException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.DeserializationConfig
import org.codehaus.groovy.grails.web.json.JSONElement

class BieService {

    def webService
    def grailsApplication

    def betterBulkLookup(list) {
        def url = grailsApplication.config.bie.baseURL + "/ws/species/guids/bulklookup.json"
        def data = webService.doPost(url, "", (list as JSON).toString())
        Map results = [:]
        data.resp.searchDTOList.each {item ->
            results.put item.guid, [
                    common: item.commonNameSingle,
                    image: [largeImageUrl: item.largeImageUrl,
                            smallImageUrl: item.smallImageUrl,
                            thumbnailUrl: item.thumbnailUrl,
                            imageMetadataUrl: item.imageMetadataUrl]]
        }
        return results
    }

    def findLsidByName(name) {
        //def url = grailsApplication.config.bie.baseURL + "/ws/species/guids/"
        //def data = webService.doJsonPost(url, "bulklookup.json", "", ([name] as JSON).toString())
        def url = grailsApplication.config.bie.baseURL + "/ws/guid/batch?q=" + name.encodeAsURL()
        def data = webService.getJson(url)
        log.debug "data => " + data
        def results = []
        data.getAt(name).each {item ->
            log.debug "item = " + item
            results.add(item.acceptedIdentifier)
        }
        log.debug "guid lookup => " + results
        return results.size() > 0 ? results[0] : ''
    }

    static bieNameGuidCache = [:]  // temp cache while services are made more efficient

    def getBieMetadata(name, guid) {
        // use guid if known
        def key = guid ?: name

        // check cache first
        if (bieNameGuidCache[name]) {
            return bieNameGuidCache[name]
        }
        def resp = getJson(grailsApplication.config.bie.baseURL + "/ws/species/" + key.encodeAsURL() + ".json")
        if (!resp || resp.error) {
            return [name: name, guid: guid]
        }
        def details = [name: resp?.taxonConcept?.nameString ?: name, guid: resp?.taxonConcept?.guid,
                common: extractBestCommonName(resp?.commonNames),
                image: extractPreferredImage(resp?.images)]
        bieNameGuidCache[name] = details
        return details
    }

    def getTaxonConceptDTO(guid) {
        if (!guid) {
            return null
        }

        log.debug "url = " + grailsApplication.config.bie.baseURL + "/ws/species/" + guid.replaceAll(/\s+/,'+') + ".json"
        def json = webService.get(grailsApplication.config.bie.baseURL + "/ws/species/" + guid.replaceAll(/\s+/,'+') + ".json")
        //log.debug "ETC json: " + json
        ObjectMapper mapper = new ObjectMapper()
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        try {
            ExtendedTaxonConceptDTO etc = mapper.readValue(json, ExtendedTaxonConceptDTO.class)
            return etc
        } catch (Exception e) {
            log.error("Error unmarshalling json " + e.message, e)
            return JSON.parse(json)
        }
    }

    def searchBie(SearchRequestParamsDTO requestObj) {
        def json = webService.get(grailsApplication.config.bie.baseURL + "/ws/search.json?" + requestObj.getQueryString())
        return JSON.parse(json)
    }

    def getSpeciesList(guid){
        if(!guid){
            return null
        }
        try{
            def json = webService.get(grailsApplication.config.speciesList.baseURL + "/ws/species/" + guid.replaceAll(/\s+/,'+'), true)
            return JSON.parse(json)
        }
        catch(Exception e){
            //handles the situation where time out exceptions etc occur.
            log.error("Error retrieving species list.", e)
            return []
        }
    }

    def getTaxonConcept(guid) {
        if (!guid) {
            return null
        }

        log.debug "url = " + grailsApplication.config.bie.baseURL + "/ws/species/" + guid.replaceAll(/\s+/,'+') + ".json"
        def json = webService.get(grailsApplication.config.bie.baseURL + "/ws/species/" + guid.replaceAll(/\s+/,'+') + ".json")
        //log.debug "ETC json: " + json
        try{
            JSON.parse(json)
        } catch (Exception e){
            log.warn "Problem retrieving information for Taxon: " + guid
            null
        }
    }

    def getExtraImages(tc) {
        def images = []

        if (tc?.taxonConcept?.rankID && tc?.taxonConcept?.rankID < 7000 /*&& tc?.taxonConcept?.rankID % 1000 == 0*/) {
            // only lookup for higher taxa of major ranks
            // /ws/higherTaxa/images
            images = webService.getJson(grailsApplication.config.bie.baseURL + "/ws/higherTaxa/images.json?scientificName=" + tc?.taxonConcept?.nameString + "&taxonRank=" + tc?.taxonConcept?.rankString)
        }

        if (images.hasProperty("error")) {
            images = []
        }
        log.debug "images = " + images

        return images
    }

    def getClassificationForGuid(guid) {
        String url = grailsApplication.config.bie?.baseURL + "/ws/classification/" + guid.replaceAll(/\s+/,'+')
        def json = webService.getJson(url)
        log.debug "json type = " + json
        if (json instanceof JSONObject && json.has("error")) {
            log.warn "classification request error: " + json.error
            return [:]
        } else {
            log.debug "classification json: " + json
            return json
        }
    }

    def getChildConceptsForGuid(guid) {
        String url = grailsApplication.config.bie?.baseURL + "/ws/childConcepts/" + guid.replaceAll(/\s+/,'+')
        def json = webService.getJson(url).sort() { it.rankId?:0 }

        if (json instanceof JSONObject && json.has("error")) {
            log.warn "child concepts request error: " + json.error
            return [:]
        } else {
            log.debug "child concepts json: " + json
            return json
        }
    }

    def getPreferredImage(name) {
        def resp = getJson(grailsApplication.config.bie.baseUrl + "/ws/species/${name.encodeAsURL()}.json")
        return extractPreferredImage(resp.images)
    }

    def extractPreferredImage(images) {
        if (images) {
            def preferred = images.findAll {it.preferred}
            // return first preferred name
            if (preferred) {
                return [repoLocation: preferred[0].repoLocation, thumbnail: preferred[0].thumbnail, rights: preferred[0].rights]
            }
            // else return first image
            return [repoLocation: images[0].repoLocation, thumbnail: images[0].thumbnail, rights: images[0].rights]
        }
        return null
    }

    def extractBestCommonName(names) {
        if (names) {
            def preferred = names.findAll {it.preferred}
            // return first preferred name
            if (preferred) { return preferred[0].nameString}
            // else return first name
            return names[0].nameString
        }
        return ""
    }

    def lookupCAABCodeForFamily(name) {
        return tempCache[name]?.CAABCode ?: ""
    }

    def lookupCommonNameForFamily(name) {
        return tempCache[name]?.preferredCommonName ?: ""
    }

    /**
     * Lookup against biocache for isAustralian property
     *
     * @param guid
     * @return
     */
    def getIsAustralian(guid) {
        Boolean isAustralian = null
        def ausTaxon = webService.getJson(grailsApplication.config.biocache.baseURL + "/ws/australian/taxon/" + guid.replaceAll(/\s+/,'+'))

        if (ausTaxon instanceof JSONObject && ausTaxon.containsKey("isAustralian")) {
            isAustralian = ausTaxon.get("isAustralian")
        }

        log.debug("isAustralian lookup: " + isAustralian)

        return isAustralian
    }
}