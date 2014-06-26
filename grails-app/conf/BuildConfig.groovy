grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.work.dir = "work"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// Remove the conflicting groovy jar before bundling
grails.war.resources = { stagingDir ->
    delete(file:"${stagingDir}/WEB-INF/lib/groovy-1.7.11.jar")
    delete(file:"${stagingDir}/WEB-INF/lib/groovy-all-2.0.5.jar")
}
grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        grailsHome()
        mavenLocal()
        mavenRepo "http://maven.ala.org.au/repository/"
        mavenRepo "http://maven.tmatesoft.com/content/repositories/releases/"
        mavenRepo "http://repository.gbif.org/content/repositories/gbif/"
        mavenRepo "http://repository.codehaus.org"
        mavenCentral()
        grailsPlugins()
        grailsCentral()

        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile("au.org.ala:ala-name-matching:1.2-SNAPSHOT") {
            //transitive: true
            excludes "simmetrics"
        }

        //compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.2'

//        compile group:'org.gbif',
//                name:'ecat-common',
//                version:'1.5.1-SNAPSHOT'
//
        compile 'org.gbif:gbif-common:0.7'

        // runtime 'mysql:mysql-connector-java:5.1.16'
        //build 'au.org.ala:ala-cas-client:1.0-SNAPSHOT'
        //build 'org.jasig.cas:cas-client-core:3.1.10'
        runtime 'mysql:mysql-connector-java:5.1.18'
    }

    plugins {
        runtime ":hibernate:3.6.10.15"
        runtime ":jquery:1.7.1"
        runtime ":resources:1.2"

        compile(":ala-web-theme:[0.1,]") {
            excludes "jquery","resources","cache","servlet-api"
        }
//        runtime ":yui:2.8.2"
//        runtime (":grails-ui:1.2.3"){
//            //for unresolvable dependency yui:[2.6.0,)
//            excludes "yui"
//        }
        //compile ":springcache:1.3.1"
        compile ':cache:1.0.1'
        compile ":jsonp:0.2"
        compile ":rest:0.8"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:7.0.53"
    }
}
