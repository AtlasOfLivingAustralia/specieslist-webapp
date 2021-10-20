package au.org.ala.specieslist

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class AdminInterceptorSpec extends Specification implements InterceptorUnitTest<AdminInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test admin interceptor matching"() {
        when: "A request matches the interceptor"
        withRequest(controller: "admin")

        then: "The interceptor does match"
        interceptor.doesMatch()
    }

    void "Test unreadValidatedTasks interceptor matching"() {
        when: "A request matches the interceptor"
        withRequest(controller: "public", action: 'unreadValidatedTasks')

        then: "The interceptor does match"
        interceptor.doesMatch() == false
    }
}
