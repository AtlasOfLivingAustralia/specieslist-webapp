package au.org.ala.specieslist

import au.org.ala.web.AuthService
import org.apache.http.HttpStatus

class SecurityFilters {


    LocalAuthService localAuthService
    AuthService authService

    def filters = {
        speciesListItem(controller:'speciesListItem', action:'list|listAuth|downloadList') {
            before = {
                checkSecurity(delegate, params.id, authService, localAuthService)
            }
        }

        speciesList(controller:'speciesList', action:'deleteList') {
            before = {
                checkSecurity(delegate, params.id, authService, localAuthService)
            }
        }

    }

    // The auth and localAuth services need to be passed in in order to use the same instance that the filters
    // closure has - this is an issue when unit testing because the closure gets the mock services, but this method
    // gets the 'real' injected services unless we pass them in
    private boolean checkSecurity(filter, String listId, AuthService authService, LocalAuthService localAuthService) {
        SecurityUtil securityUtil = new SecurityUtil(localAuthService: localAuthService, authService: authService)

        if (!securityUtil.checkListAccess(listId)) {
            filter.response.sendError(HttpStatus.SC_UNAUTHORIZED, "Not authorised")
            false
        } else {
            true
        }
    }
}
