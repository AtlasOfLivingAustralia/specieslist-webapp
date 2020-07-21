package au.org.ala.specieslist

import au.org.ala.web.AuthService

/**
 * Interceptor for AdminController
 */
class AdminInterceptor {

    LocalAuthService localAuthService
    AuthService authService

    AdminInterceptor() {
        match(controller: 'admin', action: "*")
    }

    boolean before() {
        auth()
    }

    private boolean auth() {
        if (!localAuthService.isAdmin()) {
            flash.message = "You are not authorised to access this page."
            redirect(controller: "public", action: "speciesLists")
            false
        } else {
            true
        }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
