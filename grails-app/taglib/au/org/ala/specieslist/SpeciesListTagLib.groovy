package au.org.ala.specieslist

class SpeciesListTagLib {
    static namespace = 'sl'
    def authService

    def getFullNameForUserId = { attrs, body ->
        out << authService.getUserForUserId(attrs.userId)?.displayName
    }
}
