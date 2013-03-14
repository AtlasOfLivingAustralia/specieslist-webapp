package au.org.ala.specieslist

import org.springframework.web.context.request.RequestContextHolder
import grails.plugin.springcache.annotations.Cacheable
import groovyx.net.http.HTTPBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AuthService {

    private Map<String,String> userNamesById
    private Map<String,String> userNamesByNumericIds

    public static final  String ROLE_ADMIN="ROLE_ADMIN"

    def grailsApplication

    def email(){
        //println(RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes)
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.email)?:null
    }

    def firstname(){
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname)?:null
    }

    def surname(){
        (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.lastname)?:null
    }

    def isValidUserName(String username){
        //TODO check that the username is for a current CAS user
        true
    }

    def isAdmin() {
        return ConfigurationHolder.config.security.cas.bypass ||
                RequestContextHolder.currentRequestAttributes()?.isUserInRole(ROLE_ADMIN)
    }

    protected boolean userInRole(role) {
        return ConfigurationHolder.config.security.cas.bypass ||
                RequestContextHolder.currentRequestAttributes()?.isUserInRole(role) ||
                isAdmin()
    }

    @Cacheable("authCache")
    def Map<String,String> getMapOFAllUserNamesById(){
        loadMapOfAllUserNamesById()
        return userNamesById
    }
    protected void loadMapOfAllUserNamesById() {
        try {
            final String jsonUri = grailsApplication.config.auth.userDetailsUrl + grailsApplication.config.auth.userNamesForIdPath;
            log.info("authCache requesting: " + jsonUri);
            def http = new HTTPBuilder(jsonUri)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            def response=http.post([:])
            userNamesById = (Map<String,String>)response
            //userNamesById = restTemplate.postForObject(jsonUri, null, Map.class);
        } catch (Exception ex) {
            log.error("RestTemplate error: " + ex.getMessage(), ex);
        }
        //logger.debug("userNamesById = " + StringUtils.join(userNamesById.keySet(), "|"));
    }
    public String getDisplayNameFor(String value){
        String displayName = value;
        if(value != null){
            if(grailsApplication.mainContext.authService.getMapOFAllUserNamesById().containsKey(value))
                displayName = userNamesById.get(value);
            else if(grailsApplication.mainContext.authService.getMapOfAllUserNamesByNumericId().containsKey(value)){
                displayName=userNamesByNumericIds.get(value);
            }
            else if(displayName.contains("@"))
                displayName = displayName.substring(0, displayName.indexOf("@"));
        }
        return displayName;
    }

    @Cacheable("authCache")
    public Map<String, String> getMapOfAllUserNamesByNumericId() {
        loadMapOfAllUserNamesByNumericId()
        return userNamesByNumericIds
    }

    public void loadMapOfAllUserNamesByNumericId() {
        try {
            final String jsonUri = grailsApplication.config.auth.userDetailsUrl + grailsApplication.config.auth.userNamesForNumericIdPath;
            log.info("authCache requesting: " + jsonUri);
            def http = new HTTPBuilder(jsonUri)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            def response=http.post([:])
            userNamesByNumericIds = (Map<String,String>)response
            //userNamesByNumericIds = restTemplate.postForObject(jsonUri, null, Map.class);
        } catch (Exception ex) {
            log.error("Error getting numeric ids: " + ex.getMessage(), ex);
        }
        //logger.debug("userNamesByIds = " + StringUtils.join(userNamesByNumericIds.keySet(), "|"));
    }

}
