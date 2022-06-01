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

import au.org.ala.web.AuthService
import grails.testing.gorm.DataTest
import grails.testing.web.interceptor.InterceptorUnitTest
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SecurityInterceptorSpec extends Specification implements InterceptorUnitTest<SecurityInterceptor>, DataTest {

    def setupSpec(){
        mockDomains(SpeciesList)
    }

    def setup(){
        config.security.cas.bypass = false
        SpeciesListKVP.metaClass.static.executeQuery = { String query ->
            []
        }

        SpeciesListItem.metaClass.static.executeQuery = { String query, Collection params ->
            []
        }
        interceptor.localAuthService = Stub(LocalAuthService)
        interceptor.localAuthService.isAdmin() >> false
        interceptor.authService = Stub(AuthService)
        interceptor.authService.userId >> 666
    }

    def "list should return a HTTP 'not authorised' for private lists that the user cannot access - #action"() {
        setup:
//        def controller = mockController(Class.forName(controllerName))
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 1234)
        speciesList.addToEditors("9876")
        speciesList.save(failOnError: true)

        when:
        params.id = speciesList.dataResourceUid
//        mockInterceptor(SecurityInterceptor)
        interceptor.before()
        withRequest(controller: controllerName, action: action)
//        withInterceptors(action: action) {
//            controller."${action}"()
//        }

        then:
        assert response.status == HttpStatus.SC_UNAUTHORIZED

        where:
        controllerName                                       |   action
        "au.org.ala.specieslist.SpeciesListItemController"   |   "list"
        "au.org.ala.specieslist.SpeciesListItemController"   |   "listAuth"
        "au.org.ala.specieslist.SpeciesListItemController"   |   "downloadList"
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"
    }

    def "list should return a HTTP 'OK' for private lists that the user can access - #action"() {
        setup:
        def controller = mockController(Class.forName(controllerName))
        SpeciesList speciesList = new SpeciesList(dataResourceUid: "dr1", username: "fred", listName: "list1",
                isPrivate: true, userId: 666)
        speciesList.addToEditors("9876")
        speciesList.save(flush: true, failOnError: true)

        if (controller instanceof SpeciesListController){
            ((SpeciesListController)controller).helperService = Stub(HelperService)
            ((SpeciesListController)controller).helperService.deleteDataResourceForList(_) >> false
        }

        when:
        params.id = speciesList.dataResourceUid
//        withRequest(controller: controllerName, action: action)
        withInterceptors(controller: controllerName, action: action) {
            controller."$action"()
        }

        then:
        assert response.status == status

        where:
        controllerName                                       |   action         | status
        "au.org.ala.specieslist.SpeciesListItemController"   |   "list"         | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListItemController"   |   "listAuth"     | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListItemController"   |   "downloadList" | HttpStatus.SC_OK
        "au.org.ala.specieslist.SpeciesListController"       |   "deleteList"   | HttpStatus.SC_MOVED_TEMPORARILY
    }
}
