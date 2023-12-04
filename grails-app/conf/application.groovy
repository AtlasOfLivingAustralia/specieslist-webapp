import grails.util.Environment

/******************************************************************************\
 *  CONFIG MANAGEMENT
 \******************************************************************************/
def appName = 'specieslist-webapp'
if(Environment.current != Environment.TEST){
    def ENV_NAME = "${appName.toUpperCase()}_CONFIG"
    default_config = "/data/${appName}/config/${appName}-config.properties"
    if(!grails.config.locations || !(grails.config.locations instanceof List)) {
        grails.config.locations = []
    }

    if(System.getenv(ENV_NAME) && new File(System.getenv(ENV_NAME)).exists()) {
        println "[${appName}] Including configuration file specified in environment: " + System.getenv(ENV_NAME);
        grails.config.locations.add "file:" + System.getenv(ENV_NAME)
    } else if(System.getProperty(ENV_NAME) && new File(System.getProperty(ENV_NAME)).exists()) {
        println "[${appName}] Including configuration file specified on command line: " + System.getProperty(ENV_NAME);
        grails.config.locations.add "file:" + System.getProperty(ENV_NAME)
    } else if(new File(default_config).exists()) {
        println "[${appName}] Including default configuration file: " + default_config;
        grails.config.locations.add "file:" + default_config
    } else {
        println "[${appName}] No external configuration file defined."
    }
}

println "[${appName}] (*) grails.config.locations = ${grails.config.locations}"

println("Current environment ${Environment.current}")

//boolean that indicates if biocache is configured to index species lists
biocache.indexAuthoritative = false
occurrenceDownload.enabled = true
termsOfUseUrl = "http://www.ala.org.au/about-the-atlas/terms-of-use/#TOUusingcontent"

logger.baseURL = "https://logger.ala.org.au/service"
bieService.baseURL = "https://bie-ws.ala.org.au/ws"

//registryApiKey = "xxxxxxxxxxxxxxxxxx"

bieApiKey = "xxxxxx"

/*** Config specific for species list ***/
updateUserDetailsOnStartup = false
iconicSpecies.uid = "dr781"

skin.orgNameLong = "Atlas of Living Australia"
skin.layout = "main"

 //the number of species to limit downloads to
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

/******************************************************************************\
 *  CAS SETTINGS
 *
 *  NOTE: Some of these will be ignored if default_config exists
 \******************************************************************************/
security.cas.appServerName='http://dev.ala.org.au:8080'
security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
// CAS properties moved from external config while migrating to ala-auth:2.x from 1.x
// Delete CAS patterns from external properties file to use AUTH 3+
security.cas.uriFilterPattern=['/speciesList','/speciesList/upload','/speciesList/*','/admin','/admin/*','/speciesListItem/listAuth/*','/editor','/editor/*','/alaAdmin/*']
security.cas.authenticateOnlyIfLoggedInFilterPattern=['/public','/public/*','/speciesListItem/list','/speciesListItem/list/*']
security.cas.uriExclusionFilterPattern=['/images.*','/css.*','/js.*','/speciesList/occurrences/.*','/speciesList/fieldGuide/.*','/ws/speciesList']

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
grails.json.legacy.builder = false
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

//localAuthService properties
auth.userDetailsUrl='http://auth.ala.org.au/userdetails/userDetails/'
//auth.userNamesForIdPath='getUserList'
//auth.userNamesForNumericIdPath='getUserListWithIds'

// set per-environment serverURL stem for creating absolute links
environments {
    development {}
    production {}
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

grails {
    cache {
        ehcache {
            ehcacheXmlLocation = 'classpath:lists-ehcache.xml'
            lockTimeout = 1000
        }
    }
}

app.dataDir='/tmp/'