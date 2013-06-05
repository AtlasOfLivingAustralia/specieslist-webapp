package au.org.ala.specieslist

class AdminController {
    def authService
    def queryService
    def beforeInterceptor = [action:this.&auth]

    def index() { redirect(action: 'speciesLists')}

    private auth() {
        if (!authService.isAdmin()) {
            flash.message = "You are not authorised to access this page."
            redirect(controller: "public", action: "speciesLists")
            return false
        }
        return true
    }
    def speciesLists(){
        //returns all the species list for editable actions
//        if (params.message)
//            flash.message = params.message
//        params.max = Math.min(params.max ? params.int('max') : 25, 100)
//        params.sort = params.sort ?: "listName"
//        params.fetch = [items: 'lazy']
        //println("Returning the species list for render")
        try{
//            def lists=SpeciesList.list(params)
//            def total = SpeciesList.count
            def lists = queryService.getFilterListResult(params)
            render (view:'specieslists', model:[lists:lists, total:lists.totalCount])
        }
        catch(Exception e){
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }
}
