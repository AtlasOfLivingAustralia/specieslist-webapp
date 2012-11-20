package au.org.ala.bie.webapp2

import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.plugin.springcache.annotations.Cacheable

class AuthService {

    static transactional = false
    def webService
    def userListMap = [:]

    def username() {
        return (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.email)?:null
    }

    def displayName() {
        if(RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname){
            ((RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname) +
                    " " + (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.lastname))
        } else {
            null
        }
    }

    protected boolean userInRole(role) {
        return ConfigurationHolder.config.security.cas.bypass ||
                RequestContextHolder.currentRequestAttributes()?.isUserInRole(role) // || isAdmin()
    }

    @Cacheable("userListCache")
    def getUserNamesForIdsMap(Boolean ignoredArg) {
        //def userListMap = [:]
        try {
            def userListJson = webService.doJsonPost(ConfigurationHolder.config.userDetails.url, ConfigurationHolder.config.userDetails.path, "", "")
            log.debug "lookup"
            if (userListJson instanceof net.sf.json.JSONObject) {
                userListJson.keySet().each { id ->
                    userListMap.put(id, userListJson[id]);
                }
            } else {
                log.info "error -  " + userListJson.getClass() + ":"+ userListJson
            }
        } catch (Exception e) {
            log.error "Cache refresh error" + e.message
        }
        return userListMap
    }
}