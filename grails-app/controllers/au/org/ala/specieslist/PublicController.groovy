package au.org.ala.specieslist

class PublicController {

    def authService

    def index() {
        //redirect to the correct type of list based on whether or not the use is logged in
        def username = authService.email()
        if(username && SpeciesList.countByUsername(username)>0)
            redirect(controller: 'speciesList',action: 'upload')

        redirect(action: 'speciesLists')
    }

    def speciesLists(){
        if (params.message)
            flash.message = params.message
        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        params.sort = params.sort ?: "listName"
        params.fetch = [items: 'lazy']
        render (view:'specieslists', model:[lists:SpeciesList.list(params), total:SpeciesList.count])
    }
}
