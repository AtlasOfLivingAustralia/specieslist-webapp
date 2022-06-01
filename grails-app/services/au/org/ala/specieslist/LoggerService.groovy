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

import groovyx.net.http.HTTPBuilder
import grails.plugin.cache.Cacheable

class LoggerService {
    def grailsApplication

    @Cacheable("loggerCache")
    def getReasons() {
        log.info("Refreshing the download reasons")
         HTTPBuilder http = new HTTPBuilder("${grailsApplication.config.logger.baseURL}/logger/reasons")
         http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        try{
            def result = http.get([:])

            def map = [:]
            result.toArray().each{
                map.put(it.getAt("id"),it.getAt("name"))
            }
            log.info "download reasons map = ${map}"
            return map;
        } catch(ex) {
            //TODO return a default list
            log.error "Error loading download reasons: ${ex}", ex
            return null;
        }
    }
}
