package au.org.ala.bie.webapp2

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

    def get(String url){
        get(url,false)
    }
    def get(String url, boolean throwError) {
        log.debug "GET on " + url
        def conn = new URL(url).openConnection()
        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            return conn.content.text
        } catch (SocketTimeoutException e) {
            if(throwError)
                throw e
            else{
                def error = [error: "Timed out calling web service. URL= ${url}."]
                println error.error
                return new groovy.json.JsonBuilder( error ).toString()
            }
        } catch (Exception e) {
            if(throwError)
                throw e;
            else{
                def error = [error: "Failed calling web service. ${e.getClass()} ${e.getMessage()} URL= ${url}."]
                println error.error
                return new groovy.json.JsonBuilder( error ).toString()
            }
        }
    }

    def getJson(String url) {
        log.debug "getJson URL = " + url
        def conn = new URL(url).openConnection()
        //JSONObject.NULL.metaClass.asBoolean = {-> false}

        try {
            conn.setConnectTimeout(10000)
            conn.setReadTimeout(50000)
            def json = conn.content.text
            return JSON.parse(json)
        } catch (ConverterException e) {
            def error = "{'error': 'Failed to parse json. ${e.getClass()} ${e.getMessage()} URL= ${url}.', 'exception': '${e}' }"
            log.error error
            return JSON.parse(error)
        } catch (SocketTimeoutException e) {
            def error = "{'error': 'Timed out getting json. URL= ${url}.', 'exception': '${e}'}"
            log.error error
            return JSON.parse(error)
        } catch (Exception e) {
            def error = "{'error': 'Failed to get json from web service. ${e.getClass()} ${e.getMessage()} URL= ${url}.', 'exception': '${e}'}"
            log.error error
            return JSON.parse(error)
        }
    }

    def doJsonPost(String url, String path, String port, String postBody) {
        //println "post = " + postBody
        //log.debug "postBody = " + postBody
        def http = new HTTPBuilder(url)
        http.request( groovyx.net.http.Method.POST, groovyx.net.http.ContentType.JSON ) {
            uri.path = path
            if (port) {
                uri.port = port as int
            }
            body = postBody
            requestContentType = ContentType.URLENC

            response.success = { resp, json ->
                //log.debug "bulk lookup = " + json
                return json
            }

            response.failure = { resp ->
                def error = [error: "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"]
                log.error "Oops: " + error.error
                return error
            }
        }
    }

    def doJsonPost(String url, String path) {
        //println "post = " + postBody
        //log.debug "postBody = " + postBody
        def http = new HTTPBuilder(url)
        http.request( groovyx.net.http.Method.POST, groovyx.net.http.ContentType.JSON ) {
            uri.path = path
            if (port) {
                uri.port = port as int
            }
            body = postBody
            requestContentType = ContentType.URLENC

            response.success = { resp, json ->
                //log.debug "bulk lookup = " + json
                return json
            }

            response.failure = { resp ->
                def error = [error: "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"]
                log.error "Oops: " + error.error
                throw new Exception(error.error);
            }
        }
    }

    def doPost(String url, String path, String port, String postBody){
        doPost(url,path,port,postBody,"application/json")
    }

    def doPost(String url, String path, String port, String postBody, String contentType) {
        //def conn = new URL("http://bie.ala.org.au/ws/species/guids/bulklookup.json").openConnection()
        def conn = new URL(url).openConnection()
        try {
            conn.setDoOutput(true)
            conn.setRequestProperty("Content-Type", contentType);
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
            def error = [error: "Timed out calling web service POST. URL= ${url}."]
            println error.error
            return error as JSON
        } catch (Exception e) {
            def error = [error: "Failed calling web service POST. ${e.getClass()} ${e.getMessage()} ${e} URL= ${url}."]
            println error.error
            return error as JSON
        }
    }
}