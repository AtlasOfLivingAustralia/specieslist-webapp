package au.org.ala.bie.webapp2
import grails.converters.JSON
import org.ala.dto.ExtendedTaxonConceptDTO
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.map.DeserializationConfig

class BieService {

    def webService
    def grailsApplication

    def injectGenusMetadata(list) {

        // build a list of genus guids to lookup
        def guids = []
        list.each { fam ->
            fam.genera.each { gen ->
                if (gen.guid) {
                    guids << gen.guid
                }
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { fam ->
            fam.genera.each { gen ->
                def data = md[gen.guid]
                if (data) {
                    gen.common = data.common
                    if (data.image && data.image.largeImageUrl?.toString() != "null") {
                        gen.image = data.image
                    }
                }
                else {
                    println "No metadata found for genus ${gen.name} (guid = ${gen.guid})"
                }
            }
        }

        return list
    }

    def injectSpeciesMetadata(list) {

        // build a list of guids to lookup
        def guids = []
        list.each { sp ->
            if (sp.guid) {
                guids << sp.guid
            }
        }

        // look up the metadata
        def md = betterBulkLookup(guids)

        // inject the metadata
        list.each { sp ->
            def data = md[sp.guid]
            if (data) {
                //sp.common = data.common  // don't override common name with name from bie as CMAR is more authoritative
                if (data.image && data.image.largeImageUrl?.toString() != "null") {
                    sp.image = data.image
                }
            }
            else {
                println "No metadata found for species ${sp.name} (guid = ${sp.guid})"
            }
        }

        return list
    }

    def betterBulkLookup(list) {
        def url = grailsApplication.config.bie.baseURL + "/species/guids/bulklookup.json"
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
        def url = grailsApplication.config.bie.baseURL + "/ws/guid/batch?q=" + URLEncoder.encode(name)
        def data = webService.getJson(url)
        log.debug "data => " + data
        def results = []
        data.getAt(name).each {item ->
            log.debug "item = " + item
            results.add(item.identifier)
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
        def resp = getJson(grailsApplication.config.bie.baseURL + "/species/" + key + ".json")
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

        log.debug "url = " + grailsApplication.config.bie.baseURL + "/species/" + guid + ".json"
        def json = webService.get(grailsApplication.config.bie.baseURL + "/species/" + guid + ".json")
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

    def getTaxonConcept(guid) {
        if (!guid) {
            return null
        }

        log.debug "url = " + grailsApplication.config.bie.baseURL + "/species/" + guid + ".json"
        def json = webService.get(grailsApplication.config.bie.baseURL + "/species/" + guid + ".json")
        //log.debug "ETC json: " + json
        return JSON.parse(json)
    }

    def getExtraImages(tc) {
        def images = []

        if (tc?.taxonConcept?.rankID < 7000 && tc?.taxonConcept?.rankID % 1000 == 0) {
            // only lookup for higher taxa of major ranks
            // /ws/higherTaxa/images
            images = webService.getJson(grailsApplication.config.bie.baseURL + "/ws/higherTaxa/images.json?scientificName=" + tc?.taxonConcept?.nameString + "&taxonRank=" + tc?.taxonConcept?.rankString)
        }

        if (images.error) {
            images = []
        }
        log.debug "images = " + images

        return images
    }

    def getInfoSourcesForGuid(guid) {
        def infoSources = webService.getJson(grailsApplication.config.bie.baseURL + "/ws/infosources/" + guid)

        if (infoSources.error) {
            return [:]
        } else {
            return infoSources
        }
    }

    def getClassificationForGuid(guid) {
        String url = grailsApplication.config.bie?.baseURL + "/ws/classification/" + guid
        def json = webService.getJson(url)

        if (json.hasProperty("error")) {
            log.warn "classification request error: " + json.error
            return [:]
        } else {
            log.debug "classification json: " + json
            return json
        }
    }

    def getChildConceptsForGuid(guid) {
        String url = grailsApplication.config.bie?.baseURL + "/ws/childConcepts/" + guid
        def json = webService.getJson(url)

        if (json.hasProperty("error")) {
            log.warn "classification request error: " + json.error
            return [:]
        } else {
            log.debug "classification json: " + json
            return json
        }
    }

    def getPreferredImage(name) {
        def resp = getJson(grailsApplication.config.bie.baseUrl + "/species/${name}.json")
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
}