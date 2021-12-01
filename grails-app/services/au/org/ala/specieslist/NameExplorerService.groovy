/*
 * Copyright (C) 2021 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */
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

    /**
     * Find a NameUsageMatch by a NameSearch
     * @param search NameUsageMatch
     * @return NameSearch
     */
    NameUsageMatch find(NameSearch search) {
        return this.nameMatchService.match(search)
    }

    /**
     * Search LSID by the supplied common/vernacular name
     * @param commonName common/vernacular name
     * @return LSID or null if no match
     */
    String searchForLsidByCommonName(String commonName) {
        NameUsageMatch result = nameMatchService.matchVernacular(StringUtils.trimToNull(commonName))
        return result.success? result.taxonConceptID : null
    }

    /**
     * Search LSID by the supplied scientific name
     * @param scientificName scientific name
     * @return LSID or null  if no match
     */
    String searchForAcceptedLsidByScientificName(String scientificName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        NameUsageMatch result = find(builder.build())
        return result.success? result.taxonConceptID : null
    }

    /**
     * Search NameUsageMatch by LSID
     * @param lsid lsid
     * @return matched NameUsageMatch or null
     */
    NameUsageMatch searchForRecordByLsid(String lsid) {
        NameUsageMatch result = nameMatchService.get(StringUtils.trimToNull(lsid))
        return result.success? result : null
    }

    /**
     * Search NameUsageMatch by scientific name and family
     * @param scientificName scientific name
     * @param family family
     * @return matched NameUsageMatch or null
     */
    NameUsageMatch searchForRecordByNameFamily(String scientificName, String family) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        builder.family = StringUtils.trimToNull(family)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

    /**
     * Search NameUsageMatch by scientific name
     * @param scientificName scientific name
     * @return matched NameUsageMatch or null
     */
    NameUsageMatch searchForRecordByScientificName(String scientificName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.scientificName = StringUtils.trimToNull(scientificName)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

    /**
     * Search NameUsageMatch by common/vernacular name
     * @param commonName common/vernacular name
     * @return matched NameUsageMatch or null
     */
    NameUsageMatch searchForRecordByCommonName(String commonName) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        builder.vernacularName = StringUtils.trimToNull(commonName)
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }

    /**
     * Search NameUsageMatch by terms
     * @param scientificName
     * @param commonName
     * @param kingdom
     * @param phylum
     * @param clazz
     * @param order
     * @param family
     * @param genus
     * @param rank
     * @return matched NameUsageMatch or null
     */
    NameUsageMatch searchForRecordByTerms(String scientificName,
                                          String commonName,
                                          String kingdom,
                                          String phylum,
                                          String clazz,
                                          String order,
                                          String family,
                                          String genus,
                                          String rank) {
        NameSearch.NameSearchBuilder builder = new NameSearch.NameSearchBuilder()

        if (scientificName) {
            builder.scientificName = StringUtils.trimToNull(scientificName)
        }
        if (commonName) {
            builder.vernacularName = StringUtils.trimToNull(commonName)
        }
        if (kingdom) {
            builder.kingdom = StringUtils.trimToNull(kingdom)
        }
        if (phylum) {
            builder.phylum = StringUtils.trimToNull(phylum)
        }
        if (clazz) {
            builder.clazz = StringUtils.trimToNull(clazz)
        }
        if (order) {
            builder.order = StringUtils.trimToNull(order)
        }
        if (family) {
            builder.family = StringUtils.trimToNull(family)
        }
        if (genus) {
            builder.genus = StringUtils.trimToNull(genus)
        }
        if (rank) {
            builder.rank = StringUtils.trimToNull(rank)
        }
        NameUsageMatch result = find(builder.build())
        return result.success? result : null
    }
}
