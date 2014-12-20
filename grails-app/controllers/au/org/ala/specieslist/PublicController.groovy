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
        if (params.isSDS) {
            // work around for SDS sub-list
            redirect(action:'sdsLists')
            return
        }
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ?: "listName"

        log.info "params = " + params

        try{
            def lists, count

            if (params.q) {
                lists = queryService.getFilterListResult(params)
                count = lists.totalCount
            } else {
                // the public listing should not include any private lists
                lists = SpeciesList.findAllByIsPrivateIsNullOrIsPrivate(false, params)
                count = SpeciesList.countByIsPrivateIsNullOrIsPrivate(false)
            }

            log.info "lists = ${lists.size()} || count = ${count}"
            render (view:'specieslists', model:[lists:lists, total:count])
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
