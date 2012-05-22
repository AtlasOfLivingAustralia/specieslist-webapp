package au.org.ala.bie.webapp2

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.beans.factory.InitializingBean


class WebService implements InitializingBean {

    public void afterPropertiesSet() {
        JSONObject.NULL.metaClass.asBoolean = {-> false}
    }

    def get(String url) {
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            return conn.content.text
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            println error.error
            return error as JSON
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
            println error.error
            return error as JSON
        }
    }

    def getJson(String url) {
        def conn = new URL(url).openConnection()
        //JSONObject.NULL.metaClass.asBoolean = {-> false}

        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            def json = conn.content.text
            return JSON.parse(json)
        } catch (ConverterException e) {
            def error = ['error': "Failed to parse json. ${e.getClass()} ${e.getMessage()} URL= ${url}.", 'exception': e]
            log.error error
            return error
        } catch (SocketTimeoutException e) {
            def error = ['error': "Timed out getting json. URL= \${url}.", 'exception': e]
            log.error  error
            return error
        } catch (Exception e) {
            def error = ['error': "Failed to get json from web service. ${e.getClass()} ${e.getMessage()} URL= ${url}.", 'exception': e]
            log.error  error
            return error
        }
    }

    def doJsonPost(String url, String path, String port, String postBody) {
        //println "post = " + postBody
        def http = new HTTPBuilder(url)
        http.request( groovyx.net.http.Method.POST, groovyx.net.http.ContentType.JSON ) {
            uri.path = path
            if (port) {
                uri.port = port as int
            }
            body = postBody
            requestContentType = ContentType.URLENC

            response.success = { resp, json ->
                //println "bulk lookup = " + json
                return json
            }

            response.failure = { resp ->
                def error = [error: "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"]
                println error.error
                return error
            }
        }

    }

    def doPost(String url, String path, String port, String postBody) {
        def conn = new URL("http://bie.ala.org.au/ws/species/guids/bulklookup.json").openConnection()
        try {
            conn.setDoOutput(true)
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())
            wr.write(postBody)
            wr.flush()
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            def resp = ""
            while ((line = rd.readLine()) != null) {
                resp += line
            }
            rd.close()
            wr.close()
            return [error:  null, resp: JSON.parse(resp)]
        } catch (SocketTimeoutException e) {
            def error = [error: "Timed out calling web service. URL= \${url}."]
            println error.error
            return error as JSON
        } catch (Exception e) {
            def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
            println error.error
            return error as JSON
        }
    }
}