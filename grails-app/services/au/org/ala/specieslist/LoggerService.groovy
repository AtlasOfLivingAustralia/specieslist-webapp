package au.org.ala.specieslist

import grails.plugin.cache.Cacheable
import groovyx.net.http.HTTPBuilder

class LoggerService {

    //@Cacheable("loggerCache")
    def getReasons() {
        log.info("Refreshing the download reasons")
         def http = new HTTPBuilder("http://logger.ala.org.au")
         http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        try{
            def result = http.get(path:'/service/logger/reasons')

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
