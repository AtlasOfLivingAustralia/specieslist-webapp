grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.work.dir = "target/work"

grails.project.fork = [
        // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

        // configure settings for the test-app JVM, uses the daemon by default
        test: false,//[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
        // configure settings for the run-app JVM
        run: false,//[maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the run-war JVM
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the Console UI JVM
        console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

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
        mavenLocal()
        mavenRepo("http://nexus.ala.org.au/content/groups/public/") {
            updatePolicy 'always'
        }
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        compile ('au.org.ala:ala-name-matching:2.4.0') {
            excludes "log4j","slf4j-log4j12"
        }
        compile 'org.gbif:gbif-common:0.17'
        compile "org.nibor.autolink:autolink:0.5.0"
        runtime 'mysql:mysql-connector-java:5.1.18'
    }


    plugins {
        build ":release:3.0.1"
        runtime ":hibernate:3.6.10.15"

        runtime ":cors:1.1.8"

        runtime ":ala-bootstrap2:2.4.2"
        runtime (":ala-auth:1.3.2") {
            exclude "servlet-api"
        }
        compile ':cache:1.0.1'
        compile ':cache-ehcache:1.0.0'
        compile ":jsonp:0.2"
        compile ":rest:0.8"

        build(":tomcat:7.0.53",
                ":release:3.0.1") {
            export = false
        }
    }
}
