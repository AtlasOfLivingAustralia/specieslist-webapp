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

import org.apache.http.HttpStatus


class WebServiceInterceptor {

    WebServiceInterceptor() {
        match(controller: 'webService', action: /(getListDetails|saveList)/)
    }

    boolean before() {
        //ensure that the supplied druid is valid
        log.debug("Prevalidating...")
        if (params.druid) {
            def list = SpeciesList.findByDataResourceUid(params.druid)
            if (list) {
                params.splist = list
            } else {
                response.sendError(HttpStatus.SC_NOT_FOUND, "Unable to locate species list ${params.druid}")
                return false
            }
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
