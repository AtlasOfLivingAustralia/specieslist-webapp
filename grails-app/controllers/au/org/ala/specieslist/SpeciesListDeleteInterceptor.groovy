package au.org.ala.specieslist

import au.org.ala.web.AuthService
import org.apache.http.HttpStatus


class SpeciesListDeleteInterceptor {
    LocalAuthService localAuthService
    AuthService authService

    SpeciesListDeleteInterceptor(){
        match(controller:'speciesList', action:/(deleteList|delete)/)
    }

    boolean before () {
        checkDeletePermission(params.id, authService, localAuthService)
    }

    private boolean checkDeletePermission(String listId, AuthService authService, LocalAuthService localAuthService) {
        SecurityUtil securityUtil = new SecurityUtil(localAuthService: localAuthService, authService: authService)
        if (!securityUtil.checkListDeletePermission(listId)) {
            response.sendError(HttpStatus.SC_UNAUTHORIZED, "Not authorised")
            false
        } else {
            true
        }
    }
    void afterView() {
        // no-op
    }
}

