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
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired

@Integration
@Rollback
@Ignore
class QueryServicePrivateListSpec extends Specification {

    @Autowired QueryService service

    def "only public lists should be returned when there is no user present"() {
        setup:
        service.setAuthService([getUserId: { null }] as AuthService)
        service.setLocalAuthService([isAdmin: { false }] as LocalAuthService)

        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList publicList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "publicList1",
                isPrivate: false).save(failOnError: true, flush: true)
        SpeciesList publicList2 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "publicList2",
                isPrivate: null).save(failOnError: true, flush: true)
        SpeciesList privateList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "privateList1",
                isPrivate: true).save(failOnError: true, flush: true)

        when:
        List results = service.getFilterListResult([:])

        then:
        assert results.size() == 2
        assert results.contains(publicList1) && results.contains(publicList2)
        assert !results.contains(privateList1)
    }

    def "admin users should see all lists, including private lists"() {
        setup:
        service.setAuthService([getUserId: { "fred" }] as AuthService)
        service.setLocalAuthService([isAdmin: { true }] as LocalAuthService)

        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList publicList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "publicList1",
                isPrivate: false).save(failOnError: true, flush: true)
        SpeciesList publicList2 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "publicList2",
                isPrivate: null).save(failOnError: true, flush: true)
        SpeciesList privateList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", listName: "privateList1",
                isPrivate: true).save(failOnError: true, flush: true)

        when:
        List results = service.getFilterListResult([:])

        then:
        assert results.size() == 3
        assert results.contains(publicList1) && results.contains(publicList2) && results.contains(privateList1)
    }

    def "users should only see public lists, lists they own, and lists they can edit"() {
        setup:
        service.setAuthService([getUserId: { "1234" }] as AuthService)
        service.setLocalAuthService([isAdmin: { false }] as LocalAuthService)

        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList publicList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", userId: "1234",
                listName: "publicList1", isPrivate: false).save(failOnError: true, flush: true)
        SpeciesList publicList2 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", userId: "9876",
                listName: "publicList2", isPrivate: null).save(failOnError: true, flush: true)
        SpeciesList privateList1 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", userId: "1234",
                listName: "privateList1", isPrivate: true).save(failOnError: true, flush: true)
        SpeciesList privateList2 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", userId: "9876",
                listName: "privateList2", isPrivate: true)
        privateList2.addToEditors("1234")
        privateList2.save(failOnError: true, flush: true)
        SpeciesList privateList3 = new SpeciesList(dataResourceUid: "Dr1", username: "bla", userId: "9875",
                listName: "privateList3", isPrivate: true).save(failOnError: true, flush: true)

        when:
        List results = service.getFilterListResult([:])

        then:
        // user 1234 can see the 2 public lists, privateList1 (because they own it) and privateList2 (because they're an
        // editor), but not privateList3 (because they don't own it and cannot edit it).
        assert results.size() == 4
        assert results.contains(publicList1) && results.contains(publicList2) && results.contains(privateList1) &&
                results.contains(privateList2), "Should see public lists, lists you own, and lists you can edit"
        assert !results.contains(privateList3), "Should not be able to see a private list you don't own and can't edit"
    }
}
