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
bie.nameIndexLocation = "/data/lucene/namematching"

//boolean that indicates if biocache is configured to index species lists
biocache.indexAuthoritative = false
occurrenceDownload.enabled = true
termsOfUseUrl = "http://www.ala.org.au/about-the-atlas/terms-of-use/#TOUusingcontent"

logger.baseURL = "https://logger.ala.org.au/service"

registryApiKey = "xxxxxxxxxxxxxxxxxx"

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
security.cas.appServerName='http://dev.ala.org.au:8082'
security.cas.casServerName = 'https://auth.ala.org.au'
security.cas.loginUrl = 'https://auth.ala.org.au/cas/login'
security.cas.logoutUrl = 'https://auth.ala.org.au/cas/logout'
security.cas.casServerUrlPrefix = 'https://auth.ala.org.au/cas'
// CAS properties moved from external config while migrating to ala-auth:2.x from 1.x
security.cas.uriFilterPattern='/speciesList,/speciesList/.*,/admin,/admin/.*,/editor,/editor/.*,/alaAdmin.*'
security.cas.authenticateOnlyIfLoggedInPattern='/speciesListItem/list,/speciesListItem/list/.*,/speciesListItem/listAuth,/speciesListItem/listAuth/.*'
security.cas.uriExclusionFilterPattern='/images.*,/css.*,/js.*,/less.*,/speciesList/occurrences/.*,/speciesList/fieldGuide/.*,/ws/speciesList'

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
auth.userNamesForIdPath='getUserList'
auth.userNamesForNumericIdPath='getUserListWithIds'

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

grails.cache.config = {

    defaults {
        eternal false
        overflowToDisk false
        maxElementsInMemory 20000
        timeToLiveSeconds 3600
    }
    cache {
        name 'userListCache'
    }
    cache {
        name 'userMapCache'
    }
    cache {
        name 'userDetailsCache'
    }

}

app.dataDir='/tmp/'

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */


// moved from DataSource.groovy during grails 3 migration
dataSource {
    pooled = true
    logSql = false
    driverClassName = "com.mysql.jdbc.Driver"
    username = ""
    password = ""
    dialect = org.hibernate.dialect.MySQL5Dialect
    properties {
        initialSize = 3
        maxActive = 6
        minEvictableIdleTimeMillis=1800000
        timeBetweenEvictionRunsMillis=600000
        numTestsPerEvictionRun=3
        testOnBorrow=true
        testWhileIdle=true
        testOnReturn=true
        removeAbandoned= true
        removeAbandonedTimeout= 180
        logAbandoned= false
        validationQuery="SELECT 1"
    }
}

// environment specific settings
environments {
    development {
        println "setting up development datasource"
        dbCreate = "create"
    }
    test {
        println "setting up test datasource"
        dataSource {
            dialect = "org.hibernate.dialect.H2Dialect"
            dbCreate = "create-drop"
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE;"
        }
    }
    production {
        println "setting up production datasource"
        dbCreate = "update"
        // must be set via external config
    }
}
