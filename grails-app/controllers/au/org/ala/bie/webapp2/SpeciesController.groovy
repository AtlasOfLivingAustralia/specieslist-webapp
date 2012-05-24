package au.org.ala.bie.webapp2

class SpeciesController {
    def bieService
    def utilityService

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
                    textProperties: utilityService.filterSimpleProperties(tc)
                ]
            )
        }
    }

}