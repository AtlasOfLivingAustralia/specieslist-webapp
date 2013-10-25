package au.org.ala.specieslist

import grails.plugin.cache.Cacheable
import groovyx.net.http.HTTPBuilder

class LoggerService {

    @Cacheable("loggerCache")
    def getReasons() {
        log.info("Refreshing the download reasons")
         def http = new HTTPBuilder("http://logger.ala.org.au")
         http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
        try{
            def result = http.get(path:'/service/logger/reasons')

            def map = [:]
            result.toArray().each{
                map.put(it.getInt("id"),it.getString("name"))
            }
            log.debug(map)
            return map;
        } catch(ex) {
            //TODO return a default list
            return null;
        }
    }
}
