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

class AdminController {

    def queryService
    def helperService
    def userDetailsService

    def index() { redirect(action: 'speciesLists') }


    // Access managed by AdminInterceptor
    def syncMetadataForLists(){
        SpeciesList.all.each { list ->
            //update the list metadata in the collectory
            helperService.updateDataResourceForList(list.dataResourceUid,
                    [
                     name: list.listName,
                     pubDescription: list.description,
                     websiteUrl: grailsApplication.config.serverName + request.contextPath + '/speciesListItem/list/' + list.dataResourceUid,
                     techDescription: "This list was first uploaded by " + list.firstName
                             + " " +list.surname + " on the " + list.lastUpdated
                             + "." + "It contains " + list.itemsCount + " taxa.",
                     resourceType : "species-list",
                     status : "dataAvailable",
                     contentTypes : '["species list"]'
                    ]
            )
        }
        render(status:201, text:'done')
    }

    // Access managed by AdminInterceptor
    def speciesLists(){
        String searchTerm = null
        params.q = params.q?.trim()
        if (params.q && params.q.length() < 3) {
            searchTerm = params.q
            params.q = ""
        }
        try {
            // retrieve qualified SpeciesListItems for performance reason
            def itemsIds = queryService.getFilterSpeciesListItemsIds(params)
            def lists = queryService.getFilterListResult(params, false, itemsIds)
            def model = [lists:lists,
                         total:lists.totalCount,
                         typeFacets: (params.listType) ? null : queryService.getTypeFacetCounts(params, false, itemsIds),
                         tagFacets: queryService.getTagFacetCounts(params, itemsIds),
                         selectedFacets:queryService.getSelectedFacets(params)]
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

    // Access managed by AdminInterceptor
    def updateListsWithUserIds() {
        Boolean successful = userDetailsService.updateSpeciesListUserDetails()
        render(status: successful ? 200 : 404, text: "Update of editors was ${(successful)?'':'not'} successful")
    }
}
