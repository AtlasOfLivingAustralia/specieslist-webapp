if (!bie.baseURL) {
    bie.baseURL = "http://bie.ala.org.au"
}
if (!collectory.baseURL) {
    collectory.baseURL="http://collections.ala.org.au"
    //collectory.baseURL = "http://audax.ala.org.au:8080/Collectory"
}
/******* Change this stuff for your project *******/
appName = 'specieslist-webapp'
if(!serverName){
    serverName='http://lists.ala.org.au'
    //serverName='http://audax.ala.org.au:8080'
}
//contextPath='/specieslist-webapp'
security.cas.uriFilterPattern = '/speciesList, /speciesList/.*'
collectory.enableSync = false

/******* End of change this stuff for your project *******/
//if (!security.cas.contextPath) {
//    security.cas.contextPath = "/specieslist-webapp"
//}
if (!security.cas.authenticateOnlyIfLoggedInPattern) {
    security.cas.authenticateOnlyIfLoggedInPattern = "/speciesListItem/list,/speciesListItem/list/.*,/ws/speciesList"
}
/******* ALA standard config ************/
headerAndFooter.baseURL = "http://www2.ala.org.au/commonui"
security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.uriExclusionFilterPattern = '/images.*,/css.*,/js.*,/speciesList/occurrences/.*,/speciesList/fieldGuide/.*'
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
security.cas.bypass = false
ala.baseURL = "http://www.ala.org.au"
bie.baseURL = "http://bie.ala.org.au"
biocacheService.baseURL = "http://biocache.ala.org.au/ws"
bieService.baseURL = "http://bie.ala.org.au/ws"
biocache.baseURL = "http://biocache.ala.org.au"
fieldGuide.baseURL = "http://fieldguide.ala.org.au"
//bie.baseURL = "http://natasha.ala.org.au:8090/bie-webapp2"//"http://bie.ala.org.au"
bie.searchPath = "/search"
grails.project.groupId = au.org.ala // change this to alter the default package name and Maven publishing destination
/******* End of ALA standard config ************/
/*** Config specific for species list ***/
bie.download = "/data/bie-staging/species-list"
bie.nameIndexLocation = "/data/lucene/namematchingv_13"
downloadLimit="200" //the number of species to limit downloads to
/*** End config specific for species list ***/
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']


// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
//grails.converters.json.default.deep=true
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = true
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
//necessary to allow method caching for the Services.
grails.spring.disable.aspectj.autoweaving = false
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = true

//authService properties
auth.userDetailsUrl='http://auth.ala.org.au/userdetails/userDetails/'
auth.userNamesForIdPath='getUserList'
auth.userNamesForNumericIdPath='getUserListWithIds'

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
        grails.serverURL = 'http://natasha.ala.org.au:8080/' + appName
        //grails.serverURL = 'http://moyesyside.ala.org.au:8080/' + appName
        //collectory.baseURL = 'http://natasha.ala.org.au:8080/Collectory'
        collectory.baseURL ='http://audax.ala.org.au:8080/Collectory'
        serverName='http://natasha.ala.org.au:8080'
        contextPath = "/specieslist-webapp"
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
        grails.serverURL = 'http://lists.ala.org.au'
        //collectory.baseURL ='http://audax.ala.org.au:8080/Collectory'
        collectory.baseURL='http://collections.ala.org.au'
        //bie.baseURL = 'http://audax.ala.org.au:8080/bie-webapp2'
        bie.baseURL = 'http://bie.ala.org.au'
        serverName='lists.ala.org.au'
        contextPath = ""
    }
}

// log4j configuration
// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {

        //console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.DEBUG
//        rollingFile name: "dev2", layout: pattern(conversionPattern: "[POSTIE] %c{2} %m%n"), maxFileSize: 1024, file: "/tmp/postie.log", threshold: org.apache.log4j.Level.DEBUG

        environments {
            production {
                //console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.ERROR
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/var/log/tomcat6/specieslist.log", threshold: org.apache.log4j.Level.INFO, layout: pattern(conversionPattern: "%d %-5p [%c{1}] %m%n")
                'null' name: "stacktrace"
            }
            development {
                console name: "stdout", layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n"), threshold: org.apache.log4j.Level.TRACE
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/specieslist.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
                'null' name: "stacktrace"
            }
            test {
                rollingFile name: "tomcatLog", maxFileSize: 102400000, file: "/tmp/specieslist-test.log", threshold: org.apache.log4j.Level.DEBUG, layout: pattern(conversionPattern: "%d %-5p [%c{1}]  %m%n")
                'null' name: "stacktrace"
            }
        }
    }

    root {
        // change the root logger to my tomcatLog file
        error 'tomcatLog'
        warn 'tomcatLog'
        info 'tomcatLog'
        debug 'tomcatLog', 'stdout'
        trace 'tomcatLog', 'stdout'
        additivity = true
    }

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
            'net.sf.ehcache.hibernate',
            'org.codehaus.groovy.grails.plugins.orm.auditable',
            'org.mortbay.log', 'org.springframework.webflow',
            'grails.app',
            'org.apache',
            'org',
            'com',
            'au',
            'grails.app',
            'net',
            'grails.util.GrailsUtil'
    debug  'grails.app.domain.ala.postie',
            'grails.app.controller.ala.postie',
            'grails.app.service.ala.postie',
            'grails.app.tagLib.ala.postie',
            'grails.app',
            'grails.plugin.springcache'

}

//springcache configuration
springcache {
    defaults {
        // set default cache properties that will apply to all caches that do not override them
        eternal = false
        diskPersistent = false
    }
    caches {
        loggerCache {
            // set any properties unique to this cache
            eternal=true
            memoryStoreEvictionPolicy = "LRU"
        }
        authCache {
            memoryStoreEvictionPolicy = "LRU"
            timeToLive="600"
        }
    }
}