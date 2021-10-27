package au.org.ala.specieslist

class AdminController {

    def queryService
    def helperService
    def userDetailsService

    def index() { redirect(action: 'speciesLists') }

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
                         typeFacets: (params.listType) ? null : queryService.getTypeFacetCounts(params, hidePrivateLists, itemsIds),
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

    def updateListsWithUserIds() {
        Boolean successful = userDetailsService.updateSpeciesListUserDetails()
        render(status: successful ? 200 : 404, text: "Update of editors was ${(successful)?'':'not'} successful")
    }
}
