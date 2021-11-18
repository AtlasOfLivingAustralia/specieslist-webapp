package au.org.ala.specieslist

import grails.gorm.transactions.Transactional
import au.org.ala.names.ws.api.NameMatchService
import au.org.ala.names.ws.api.NameSearch
import au.org.ala.names.ws.api.NameUsageMatch
import au.org.ala.names.ws.client.ALANameUsageMatchServiceClient
import au.org.ala.ws.ClientConfiguration
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import org.apache.commons.lang.StringUtils

/**
 * Service to get name match results from namematching-ws.ala.org.au
 */
@Transactional
class NameExplorerService implements GrailsConfigurationAware {
    private NameMatchService nameMatchService

    @Override
    void setConfiguration(Config config) {
        URL service = new URL(config.getProperty("namematching.serviceURL"))
        ClientConfiguration cc = ClientConfiguration.builder().baseUrl(service).build()
        this.nameMatchService = new ALANameUsageMatchServiceClient(cc)
    }

    NameUsageMatch find(NameSearch search) {
        return this.nameMatchService.match(search)
    }

    String searchForLsidByCommonName(String commonName) {
        NameUsageMatch result = nameMatchService.matchVernacular(StringUtils.trimToNull(commonName))
        return result.success? result.taxonConceptID : null
    }

    String searchForAcceptedLsidByScientificName(String scientificName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        NameUsageMatch result = find(builder.build())
        return result.success? result.taxonConceptID : null
    }

    NameUsageMatch searchForRecordByLsid(String lsid) {
        NameUsageMatch result = nameMatchService.get(StringUtils.trimToNull(lsid))
        return result.success? result : null
    }

    NameUsageMatch searchForRecordByNameFamily(String scientificName, String family) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        builder.family = StringUtils.trimToNull(family)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

    NameUsageMatch searchForRecordByScientificName(String scientificName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

    NameUsageMatch searchForRecordByCommonName(String commonName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.vernacularName = StringUtils.trimToNull(commonName)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

}
