// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

/******************************************************************************\
 *  EXTERNAL SERVERS
 \******************************************************************************/
if (!bie.baseURL) {
    bie.baseURL = "http://bie.ala.org.au"
}
if (!bie.searchPath) {
    bie.searchPath = "/search"
}
if (!biocache.baseURL) {
    biocache.baseURL = "http://biocache.ala.org.au"
}
if (!spatial.baseURL) {
    spatial.baseURL = "http://spatial.ala.org.au"
}
if (!ala.baseURL) {
    ala.baseURL = "http://www.ala.org.au"
}
if (!collectory.baseURL) {
    collectory.baseURL = "http://collections.ala.org.au"
}
if (!bhl.baseURL) {
    bhl.baseURL = "http://bhlidx.ala.org.au"
}
if( !speciesList.baseURL){
    speciesList.baseURL ="http://lists.ala.org.au"
}
userDetails.url ="http://auth.ala.org.au/userdetails/userDetails/"
userDetails.path ="getUserList"
alerts.baseUrl = "http://alerts.ala.org.au/ws/"
brds.guidUrl = "http://cs.ala.org.au/bdrs-ala/bdrs/user/atlas.htm?surveyId=1&guid="
collectory.threatenedSpeciesCodesUrl = collectory.baseURL + "/public/showDataResource"
ranking.readonly = false

/******************************************************************************\
 *  SECURITY
 \******************************************************************************/
if (!security.cas.urlPattern) {
    security.cas.urlPattern = "/admin, /admin/.*"
}
if (!security.cas.urlExclusionPattern) {
    security.cas.urlExclusionPattern = "/images.*,/css.*,/js.*,.*json,.*xml"
}
if (!security.cas.authenticateOnlyIfLoggedInPattern) {
    security.cas.authenticateOnlyIfLoggedInPattern = "/species/.*"
}
if (!security.cas.casServerName) {
    security.cas.casServerName = "https://auth.ala.org.au"
}
if (!security.cas.loginUrl) {
    security.cas.loginUrl = "${security.cas.casServerName}/cas/login"
}
if (!security.cas.logoutUrl) {
    security.cas.logoutUrl = "${security.cas.casServerName}/cas/logout"
}
if (!security.cas.contextPath) {
    //security.cas.contextPath = "/workforce" //"""${appName}"
}
if (!security.cas.bypass) {
    security.cas.bypass = false
}


nonTruncatedSources = ["http://www.environment.gov.au/biodiversity/abrs/online-resources/flora/main/index.html"]

auth.admin_role = "ROLE_ADMIN"

springcache {
    defaults {
        // set default cache properties that will apply to all caches that do not override them
        eternal = false
        diskPersistent = false
        timeToLive = 600
        timeToIdle = 600
    }
    caches {
        userListCache {
            // set any properties unique to this cache
            memoryStoreEvictionPolicy = "LRU"
        }
    }
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      //xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      //json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

grails.project.war.file = "bie-webapp2.war"
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = true

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
        grails.host = "http://localhost"
        grails.serverURL = "${grails.host}:8080/${appName}"
        security.cas.appServerName = "${grails.host}:8080"
        security.cas.contextPath = "/${appName}"
        // cached-resources plugin - keeps original filenames but adds cache-busting params
        grails.resources.debug = true
    }
    test {
        grails.logging.jul.usebridge = false
        grails.host = "bie-test.ala.org.au"
        grails.serverURL = "http://bie-test.ala.org.au"
        security.cas.appServerName = grails.serverURL
        security.cas.contextPath = ""
        //log4j.appender.'errors.File'="/var/log/tomcat/biewebapp2-stacktrace.log"
    }
    production {
        grails.logging.jul.usebridge = false
        grails.host = "bie.ala.org.au"
        grails.serverURL = "http://bie.ala.org.au"
        security.cas.appServerName = grails.serverURL
        security.cas.contextPath = ""
        log4j.appender.'errors.File'="/var/log/tomcat6/biewebapp2-stacktrace.log"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%-5p [%c{2}] %m%n')
    }

//    root {
//        debug: stdout
//    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
    warn   'org.mortbay.log',
           'grails.app'
    //info   'grails.app'

    environments {

        test {
            info   'grails.app'
        }
        development {
            // Override previous setting for 'grails.app'
            info   'grails.app'
            debug  'grails.app'
        }
    }
}
