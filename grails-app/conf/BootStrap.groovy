import org.codehaus.groovy.grails.web.json.JSONObject

class BootStrap {

    def init = { servletContext ->
//        JSONObject.NULL.metaClass.asBoolean = {-> false}
    }
    def destroy = {
    }
}
