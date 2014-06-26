package au.org.ala.specieslist

import grails.converters.*
import grails.web.JSONBuilder

class PublicController {

    def authService
    def queryService

    def index() {
        //redirect to the correct type of list based on whether or not the use is logged in
        try{
            def username = authService.email
            log.debug("username: " + username)
            if(username && SpeciesList.countByUsername(username)>0)
                redirect(controller: 'speciesList',action: 'upload')

            redirect(action: 'speciesLists')
        }
        catch(Exception e){
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def speciesLists(){
//        if (params.message)
//            flash.message = params.message
//        params.max = Math.min(params.max ? params.int('max') : 25, 100)
//        params.sort = params.sort ?: "listName"
//        params.fetch = [items: 'lazy']
        log.debug "params = " + params
        //println("Returning the species list for render")
        try{
            def lists = queryService.getFilterListResult(params)
//            def lists=SpeciesList.list(params)
//            def total = SpeciesList.count
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
