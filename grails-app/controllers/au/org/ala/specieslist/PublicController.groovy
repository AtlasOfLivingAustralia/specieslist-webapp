package au.org.ala.specieslist

class PublicController {

    def authService
    def queryService

    def index() {
        //redirect to the correct type of list based on whether or not the use is logged in
        try{
            def userId = authService.userId
            log.debug("userId: " + userId)
            // Commented out to default species app to the list rather than the upload page
           /* if(userId && SpeciesList.countByUserId(userId)>0)
                redirect(controller: 'speciesList',action: 'upload')*/

            redirect(action: 'speciesLists')
        }
        catch(Exception e){
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def speciesLists(){
        params.max = Math.min(params.max ? params.int('max') : 25, 1000)
        params.sort = params.sort ?: "listName"
        log.info "params = " + params

        try{
            def lists = queryService.getFilterListResult(params)
            def facets = queryService.getFacetCounts(params)
            log.info "lists = ${lists.size()} || count = ${lists.totalCount}"
            render (view:'specieslists', model:[
                    lists:lists,
                    total:lists.totalCount,
                    facets:facets,
                    selectedFacets:queryService.getSelectedFacets(params)
            ])
        }
        catch(Exception e) {
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def sdsLists() {
        params.isSDS = "eq:true"
        try {
            def lists = queryService.getFilterListResult(params)
            log.debug("Lists: " + lists)
            render (view:'specieslists', model:[lists:lists, total:lists.totalCount])
        }
        catch(Exception e){
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

}
