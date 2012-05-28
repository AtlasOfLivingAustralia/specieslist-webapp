package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class SpeciesController {
    def bieService
    def utilityService
    def authService

    def index = {

    }

    def show = {
        def guid = params.guid
        def tc = bieService.getTaxonConcept(guid)
        log.debug("guid = " + guid + " + tc.name = " + tc?.taxonConcept?.nameString)

        if (tc.error) {
            log.error "Error requesting taxon concept object: " + tc.error
            render(view: '../error', model: [message: tc.error])
        } else {
            render(view: 'show', model: [
                    tc: tc,
                    statusRegionMap: utilityService.getStatusRegionCodes(),
                    infoSources: bieService.getInfoSourcesForGuid(guid),
                    infoSourceMap: utilityService.getInfoSourcesForTc(tc), // fallback for bieService.getInfoSourcesForGuid(guid)
                    extraImages: bieService.getExtraImages(tc),
                    textProperties: utilityService.filterSimpleProperties(tc),
                    isRoleAdmin: authService.userInRole(ConfigurationHolder.config.auth.admin_role),
                    userName: authService.username()
                ]
            )
        }
    }

    /**
     * Do logouts through this app so we can invalidate the session.
     *
     * @param casUrl the url for logging out of cas
     * @param appUrl the url to redirect back to after the logout
     */
    def logout = {
        session.invalidate()
        redirect(url:"${params.casUrl}?url=${params.appUrl}")
    }
}