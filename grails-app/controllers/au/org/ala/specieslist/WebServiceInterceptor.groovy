package au.org.ala.specieslist

import org.apache.http.HttpStatus


class WebServiceInterceptor {

    WebServiceInterceptor() {
        match(controller: 'webService', action: /(getListDetails|saveList)/)
    }

    boolean before() {
        //ensure that the supplied druid is valid
        log.debug("Prevalidating...")
        if (params.druid) {
            def list = SpeciesList.findByDataResourceUid(params.druid)
            if (list) {
                params.splist = list
            } else {
                response.sendError(HttpStatus.SC_NOT_FOUND, "Unable to locate species list ${params.druid}")
                return false
            }
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
