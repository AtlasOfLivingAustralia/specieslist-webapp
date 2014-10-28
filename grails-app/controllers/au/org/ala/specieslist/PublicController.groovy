package au.org.ala.specieslist

import grails.converters.*
import grails.web.JSONBuilder

class PublicController {

    def authService
    def queryService

    def index() {
        //redirect to the correct type of list based on whether or not the use is logged in
        try{
            def userId = authService.userId
            log.debug("userId: " + userId)
            if(userId && SpeciesList.countByUserId(userId)>0)
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
        if (params.isSDS) {
            // work around for SDS sub-list
            redirect(action:'sdsLists')
            return
        }
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ?: "listName"
        //params.fetch = [items: 'lazy']
        log.info "params = " + params
        //println("Returning the species list for render")
        try{
            //def lists = queryService.getFilterListResult(params)
            def q = "%" + params.q + "%"
            def lists, count

            if (params.q) {
                lists = SpeciesList.findAllByListNameIlikeOrDescriptionIlikeOrSurnameIlikeOrFirstNameIlike(q,q,q,q, params)
                count = SpeciesList.countByListNameIlikeOrDescriptionIlikeOrSurnameIlikeOrFirstNameIlike(q,q,q,q)
            } else {
                lists = SpeciesList.list(params)
                count = SpeciesList.count
            }

//            def lists=SpeciesList.list(params)
//            def total = SpeciesList.count
            log.info "lists = ${lists.size()} || count = ${count}"
            render (view:'specieslists', model:[lists:lists, total:count])
        }
        catch(Exception e){
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
