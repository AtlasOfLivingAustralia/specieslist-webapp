/*
 * Copyright (C) 2022 Atlas of Living Australia
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

class PublicController {

    def authService
    def queryService
    def localAuthService

    def index() {
        //redirect to the correct type of list based on whether or not the use is logged in
        try {
            def userId = authService.userId
            log.debug("userId: " + userId)
            redirect(action: 'speciesLists')
        } catch (Exception e){
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def speciesLists(){
        String searchTerm = null
        params.q = params.q?.trim()
        if (params.q && params.q.length() < 3) {
            searchTerm = params.q
            params.q = ""
        }
        params.max = Math.min(params.max ? params.int('max') : 25, 1000)
        params.sort = params.sort ?: "listName"
        if (params.isSDS){
            //to ensure backwards compatibility for a commonly used URL
            params.isSDS = "eq:true"
        }

        try {
            def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)

            // retrieve qualified SpeciesListItems for performance reason
            def itemsIds = queryService.getFilterSpeciesListItemsIds(params)
            def lists = queryService.getFilterListResult(params, hidePrivateLists, itemsIds)

            def model = [
                    isAdmin: localAuthService.isAdmin(),
                    isLoggedIn: (authService.userId) != null,
                    lists: lists,
                    total: lists.totalCount,
                    typeFacets: (params.listType) ? null : queryService.getTypeFacetCounts(params, hidePrivateLists, itemsIds),
                    tagFacets: queryService.getTagFacetCounts(params, hidePrivateLists, itemsIds),
                    selectedFacets: queryService.getSelectedFacets(params)]
            if (searchTerm) {
                params.q = searchTerm
                model.errors = "Error: Search terms must contain at least 3 characters"
            }
            render(view: 'specieslists', model: model)

        } catch(Exception e) {
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def sdsLists() {
        redirect(action:'speciesLists', params:["isSDS":"eq:true"])
    }
}
