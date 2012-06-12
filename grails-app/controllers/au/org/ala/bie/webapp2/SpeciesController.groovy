/*
 * Copyright (C) 2012 Atlas of Living Australia
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

package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.ala.model.CommonName
import org.ala.dto.ExtendedTaxonConceptDTO
import org.ala.dto.SearchTaxonConceptDTO
import org.ala.dto.SearchResultsDTO
import org.codehaus.groovy.grails.web.json.JSONObject

class SpeciesController {
    def bieService
    def utilityService
    def authService

    def index = {

    }

    def show = {
        def guid = params.guid
        def etc

        if (!(guid.matches("(urn\\:lsid[a-zA-Z\\-0-9\\:\\.]*)") || guid.matches("([0-9]*)") || guid.startsWith("ALA_"))) {
            // doesn't look like a guid so assume a name sstring
            guid = bieService.findLsidByName(guid);
        }

        etc = bieService.getTaxonConcept(guid)

        if (etc instanceof JSONObject && etc.has("error")) {
            log.error "Error requesting taxon concept object: " + etc.error
            render(view: '../error', model: [message: etc.error])
        } else {
            render(view: 'show', model: [
                    tc: etc,
                    statusRegionMap: utilityService.getStatusRegionCodes(),
                    infoSources: bieService.getInfoSourcesForGuid(guid),
                    infoSourceMap: utilityService.getInfoSourcesForTc(etc), // fallback for bieService.getInfoSourcesForGuid(guid)
                    extraImages: bieService.getExtraImages(etc),
                    textProperties: utilityService.filterSimpleProperties(etc),
                    isRoleAdmin: authService.userInRole(ConfigurationHolder.config.auth.admin_role),
                    userName: authService.username(),
                    sortCommonNameSources: utilityService.getNamesAsSortedMap(etc.commonNames),
                    taxonHierarchy: bieService.getClassificationForGuid(guid),
                    childConcepts: bieService.getChildConceptsForGuid(guid)
                ]
            )
        }
    }

    def getClassification = { tc ->
        def classification

        if (tc.getLeft()) {
            SearchResultsDTO<SearchTaxonConceptDTO> taxonHierarchy = searchDao.getClassificationByLeftNS(tc.getLeft(), tc.getRight())
            //SearchResultsDTO<SearchTaxonConceptDTO> taxonHierarchy = searchDao.getClassificationByLeftNS(tc.getLeft());
            //model.addAttribute("taxonHierarchy", taxonHierarchy.getResults());
            classification = taxonHierarchy.getResults()
        }

        return classification
    }



    /**
     * Do logouts through this app so we can invalidate the session.
     *
     * @param casUrl the url for logging out of cas
     * @param appUrl the url to redirect back to after the logout
     */
    def logout = {
        session.invalidate()
        redirect(url:"${params.casUrl}?url=${params.appUrl}")
    }
}