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
        try {
            def lists = queryService.getFilterListResult(params, false)
            render (view:'specieslists', model:[lists:lists,
                                                total:lists.totalCount,
                                                typeFacets:queryService.getTypeFacetCounts(params),
                                                tagFacets: queryService.getTagFacetCounts(params),
                                                selectedFacets:queryService.getSelectedFacets(params)
            ])
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
