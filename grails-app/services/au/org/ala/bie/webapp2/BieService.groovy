package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON

class BieService {

    def webService

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
        def url = ConfigurationHolder.config.bie.baseURL + "/species/guids/bulklookup.json"
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

    static bieNameGuidCache = [:]  // temp cache while services are made more efficient

    def getBieMetadata(name, guid) {
        // use guid if known
        def key = guid ?: name

        // check cache first
        if (bieNameGuidCache[name]) {
            return bieNameGuidCache[name]
        }
        def resp = getJson(ConfigurationHolder.config.bie.baseURL + "/species/" + key + ".json")
        if (!resp || resp.error) {
            return [name: name, guid: guid]
        }
        def details = [name: resp?.taxonConcept?.nameString ?: name, guid: resp?.taxonConcept?.guid,
                common: extractBestCommonName(resp?.commonNames),
                image: extractPreferredImage(resp?.images)]
        bieNameGuidCache[name] = details
        return details
    }

    def getTaxonConcept(guid) {
        if (!guid) {
            return null
        }

        return webService.getJson(ConfigurationHolder.config.bie.baseURL + "/species/" + guid + ".json")
    }

    def getExtraImages(tc) {
        def images = []

        if (tc?.taxonConcept?.rankID < 7000 && tc?.taxonConcept?.rankID % 1000 == 0) {
            // only lookup for higher taxa of major ranks
            // /ws/higherTaxa/images
            images = webService.getJson(ConfigurationHolder.config.bie.baseURL + "/ws/higherTaxa/images?scientificName=" + tc?.taxonConcept?.nameString + "&taxonRank=" + tc?.taxonConcept?.rankString)
        }

        return images
    }

    def getInfoSourcesForGuid(guid) {
        def infoSources = webService.getJson(ConfigurationHolder.config.bie.baseURL + "/ws/infosources/" + guid)

        if (infoSources.error) {
            return [:]
        } else {
            return infoSources
        }
    }

    def getPreferredImage(name) {
        def resp = getJson(ConfigurationHolder.config.bie.baseUrl + "/species/${name}.json")
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