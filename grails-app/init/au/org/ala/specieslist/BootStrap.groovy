/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.specieslist

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

        if (grailsApplication.config.updateUserDetailsOnStartup.toBoolean()) {
            userDetailsService.updateSpeciesListUserDetails()
            userDetailsService.updateEditorsList()
        }
    }

    def destroy = {
    }
}
