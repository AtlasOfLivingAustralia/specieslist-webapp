package au.org.ala.specieslist

class SpeciesListTagLib {
    static namespace = 'sl'
    def authService

    def getFullNameForUserId = { attrs, body ->
        out << authService.getUserForUserId(attrs.userId)?.displayName
    }

    /**
     * Generate the URL to the current page minus the fq param specified
     *
     * @attr fqs REQUIRED
     * @attr fq REQUIRED
     */
    def removeFqHref = { attrs, body ->
        def fqList = attrs.fqs
        def fq = attrs.fq
        def remainingFq = fqList - fq
        out << "?fq=" + remainingFq.join("&fq=")
    }
}
