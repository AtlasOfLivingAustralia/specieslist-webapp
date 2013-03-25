//import org.codehaus.groovy.grails.commons.ConfigurationHolder

//eventWebXmlEnd = {   String filename ->
//
//  String content = webXmlFile.text
//
//  content = content.replace("@serverName@", ConfigurationHolder.config.serverName)
//  println "Injecting configuration: serverName = ${ConfigurationHolder.config.serverName}"
//
//  content = content.replace("@contextPath@", ConfigurationHolder.config.contextPath)
//  println "Injecting configuration: contextPath = ${ConfigurationHolder.config.contextPath}"
//
//  content = content.replace("@security.cas.casServerName@", ConfigurationHolder.config.security.cas.casServerName)
//  println "Injecting CAS server name = ${ConfigurationHolder.config.security.cas.casServerName}"
//
//  content = content.replace("@security.cas.uriFilterPattern@", ConfigurationHolder.config.security.cas.uriFilterPattern)
//  println "Injecting CAS uri inclusion filter pattern = ${ConfigurationHolder.config.security.cas.uriFilterPattern}"
//
//  content = content.replace("@security.cas.uriExclusionFilterPattern@", ConfigurationHolder.config.security.cas.uriExclusionFilterPattern)
//  println "Injecting CAS uri exclusion filter pattern = ${ConfigurationHolder.config.security.cas.uriExclusionFilterPattern}"
//
//  content = content.replace("@security.cas.loginUrl@", ConfigurationHolder.config.security.cas.loginUrl)
//  println "Injecting CAS login URL = ${ConfigurationHolder.config.security.cas.loginUrl}"
//
//  content = content.replace("@security.cas.authenticateOnlyIfLoggedInPattern@", ConfigurationHolder.config.security.cas.authenticateOnlyIfLoggedInPattern)
//  println "Injecting CAS Authenticate if logged in = ${ConfigurationHolder.config.security.cas.authenticateOnlyIfLoggedInPattern}"
//
//  content = content.replace("@security.cas.casServerUrlPrefix@", ConfigurationHolder.config.security.cas.casServerUrlPrefix)
//  println "Injecting CAS URL prefix = ${ConfigurationHolder.config.security.cas.casServerUrlPrefix}"
//
//  webXmlFile.withWriter { file -> file << content }
//}
