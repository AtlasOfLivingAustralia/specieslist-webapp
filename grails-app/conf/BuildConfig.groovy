grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        grailsHome()
        mavenLocal()
        mavenRepo "http://maven.ala.org.au/repository/"
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
        compile group:'au.org.ala',
                name:'ala-name-matching',
                version:'1.1-SNAPSHOT',
                transitive:true

        compile group: 'org.codehaus.groovy.modules.http-builder',
                name: 'http-builder',
                version: '0.5.2'



        /*
        <dependency>
  <groupId>org.codehaus.groovy.modules.http-builder</groupId>
  <artifactId>http-builder</artifactId>
  <version>0.5.2</version>
</dependency>
         */

        // runtime 'mysql:mysql-connector-java:5.1.16'
        build 'au.org.ala:ala-cas-client:1.0-SNAPSHOT'
        build 'org.jasig.cas:cas-client-core:3.1.10'
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.1"
        runtime ":resources:1.1.6"
        runtime (":grails-ui:1.2.3"){
            //for unresolvable dependency yui:[2.6.0,)
            excludes "yui"
        }

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"
    }
}
