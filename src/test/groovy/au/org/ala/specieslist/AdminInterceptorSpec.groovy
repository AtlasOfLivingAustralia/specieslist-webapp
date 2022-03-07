/*
 * Copyright (C) 2022 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

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
