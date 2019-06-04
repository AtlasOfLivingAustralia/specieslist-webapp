package au.org.ala.specieslist

import groovyx.net.http.HTTPBuilder

class LoggerService {

    //@Cacheable("loggerCache")
    def getReasons() {
        log.info("Refreshing the download reasons")
         HTTPBuilder http = new HTTPBuilder("${grailsApplication.config.logger.baseURL}")
         http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        try{
            def result = http.get(path:'/logger/reasons')

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
