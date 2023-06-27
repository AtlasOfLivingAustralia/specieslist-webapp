import ch.qos.logback.core.util.FileSize
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

def loggingDir = (System.getProperty('catalina.base') ? System.getProperty('catalina.base') + '/logs' : './logs')
def appName = 'specieslist-webapp'
final TOMCAT_LOG = 'TOMCAT_LOG'
final FULL_STACKTRACE = 'FULL_STACKTRACE'
final STDOUT = 'STDOUT'

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

switch (Environment.current) {
    case Environment.PRODUCTION:
        appender(TOMCAT_LOG, RollingFileAppender) {
            file = "${loggingDir}/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "${loggingDir}/${appName}.%i.log.gz"
                minIndex = 1
                maxIndex = 4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('10MB')
            }
        }
        root(WARN, [TOMCAT_LOG])
        break
    case Environment.TEST:
        appender(TOMCAT_LOG, RollingFileAppender) {
            file = "${loggingDir}/${appName}.log"
            encoder(PatternLayoutEncoder) {
                pattern =
                        '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                                '%5p ' + // Log level
                                '--- [%15.15t] ' + // Thread
                                '%-40.40logger{39} : ' + // Logger
                                '%m%n%wex' // Message
            }
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "${loggingDir}/${appName}.%i.log.gz"
                minIndex = 1
                maxIndex = 4
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = FileSize.valueOf('1MB')
            }
        }
        root(WARN, [TOMCAT_LOG])
        break
    case Environment.DEVELOPMENT:
        appender(STDOUT, ConsoleAppender) {
            encoder(PatternLayoutEncoder) {
                charset = Charset.forName('UTF-8')
                pattern =
                        '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                                '%clr(%5p) ' + // Log level
                                '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                                '%m%n%wex' // Message
            }
        }
        appender(FULL_STACKTRACE, FileAppender) {
            file = "${loggingDir}/stacktrace.log"
            append = true
            encoder(PatternLayoutEncoder) {
                pattern = '%d{yyyy-MM-dd HH:mm:ss.SSS} ' + // Date
                        "%level %logger - %msg%n"
            }
        }
        root(WARN, [FULL_STACKTRACE, STDOUT])
        break
    default:
        appender(TOMCAT_LOG, ConsoleAppender) {
            encoder(PatternLayoutEncoder) {
                pattern = '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
            }
        }
        root(WARN, [TOMCAT_LOG])
        break
}

if (Environment.isDevelopmentMode()) {
    [
            (OFF)  : [],
            (ERROR): [],
            (WARN) : [],
            (INFO) : [
                    'asset.pipeline',
                    'au.org.ala',
                    'grails.app',
                    'grails.plugins.mail',
                    'grails.plugins.quartz',
                    'org.hibernate',
                    'org.quartz',
                    'org.springframework',
            ],
            (DEBUG): [
                    'org.apache.http.headers',
                    'org.apache.http.wire',
                    'org.hibernate.SQL',
                    'grails.app',
                    'au.org.ala.specieslist'
            ],
            (TRACE): []
    ].each { level, names ->
        names.each { name ->
            logger(name, level)
        }
    }
}