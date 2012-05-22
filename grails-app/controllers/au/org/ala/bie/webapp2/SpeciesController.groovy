package au.org.ala.bie.webapp2

class SpeciesController {
    def bieService
    def lookupService

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
                    statusRegionMap: lookupService.getStatusRegionCodes(),
                    extraImages: bieService.getExtraImages(tc)
                ]
            )
        }
    }

}