package au.org.ala.specieslist

class SpeciesListTagLib {
    static namespace = 'sl'
    static returnObjectForTags = ['buildFqList', 'excludedFqList']
    def authService

    def getFullNameForUserId = { attrs, body ->
        def displayName = authService.getUserForUserId(attrs.userId)?.displayName
        out << "${displayName?:attrs.userId}"
    }

    /**
     * Generates a list of filter query strings including that identified by the fq parameter, if supplied.
     *
     * @attr fqs REQUIRED the current list of filter query strings
     * @attr fq the additional filter query string to be added
     */
    def buildFqList = { attrs, body ->
        ArrayList ret = []
        def fq = attrs.fq
        if (attrs.fqs) {
            attrs.fqs.each {
                if (!ret.contains(it) && it != "") {
                    ret << it
                }
            }
        }
        if (fq && !ret.contains(fq)) {
            ret << fq
        }
        ret
    }

    /**
     * Generates a list of filter query strings without that identified by the fq parameter
     *
     * @attr fqs REQUIRED
     * @attr fq REQUIRED
     */
    def excludedFqList = { attrs, body ->
        def fq = attrs.fq
        def remainingFq = attrs.fqs - fq
        remainingFq
    }

    /**
     * Generates an HTML id from a string
     *
     * @attr key REQUIRED the value to use as id;
     * spaces will be replaced with hyphens, brackets will be removed
     * @attr prefix a prefix to use in the returned value;
     * a hyphen will be used to separate the prefix from the key, if provided
     */
    def facetAsId = { attrs, body ->
        def prefix = attrs.prefix ? attrs.prefix + "-" : ""
        out << prefix + attrs.key.replaceAll(" ", "-")
                .replaceAll("\\(", "").replaceAll("\\)", "")
                .toLowerCase()
    }
}
