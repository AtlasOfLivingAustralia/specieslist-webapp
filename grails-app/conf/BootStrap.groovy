import au.org.ala.specieslist.SpeciesList
import org.apache.commons.lang.WordUtils

class BootStrap {

    def authService, userDetailsService, grailsApplication

    def init = { servletContext ->
        Object.metaClass.trimLength = {Integer stringLength ->

            String trimString = delegate?.toString()
            String concatenateString = "..."
            List separators = [".", " "]

            if (stringLength && (trimString?.length() > stringLength)) {
                trimString = trimString.substring(0, stringLength - concatenateString.length())
                String separator = separators.findAll{trimString.contains(it)}?.max{trimString.lastIndexOf(it)}
                if(separator){
                    trimString = trimString.substring(0, trimString.lastIndexOf(separator))
                }
                trimString += concatenateString
            }
            return trimString
        }

        Object.metaClass.wrapHtmlLength = {Integer stringLength ->
            String inputString = delegate?.toString()
            WordUtils.wrap(inputString, stringLength, "<br/>\n", true)
        }

        if (grailsApplication.config.updateUserDetailsOnStartup) {
            userDetailsService.updateSpeciesListUserDetails()
            userDetailsService.updateEditorsList()
        }

    }
    def destroy = {
    }
}


