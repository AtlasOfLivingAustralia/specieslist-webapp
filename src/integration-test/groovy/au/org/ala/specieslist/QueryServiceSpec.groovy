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
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class QueryServiceSpec extends Specification {

    @Autowired QueryService queryService

    void setup() {
        queryService.authService = Mock(AuthService)
        queryService.localAuthService = Mock(LocalAuthService)

        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }

        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1", userId: 1,
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2", userId: 2,
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3", userId: 3,
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list3.addToEditors("1") // user 1 can edit list 3
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)

    }

    void tearDown() {
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }
    }

    void "test getFilterListResult ordering"() {
        when:
        //default order
        def list = queryService.getFilterListResult([:])
        
        then:
        assert list[0].dataResourceUid == '1'
        assert list[1].dataResourceUid == '2'
        assert list[2].dataResourceUid == '3'
    }

    void "test getFilterListResult user ordering"() {
        when:
        //user ordering
        def list = queryService.getFilterListResult([user: 'a', sort:'username'])

        then:
        assert list[0].dataResourceUid == '3'
        assert list[1].dataResourceUid == '1'
        assert list[2].dataResourceUid == '2'
    }

    void "test getFilterListResult defined ordering"() {
        when:
        //defined ordering
        def list = queryService.getFilterListResult([sort: 'description', order: 'desc'])

        then:
        assert list[0].dataResourceUid == '3'
        assert list[1].dataResourceUid == '2'
        assert list[2].dataResourceUid == '1'
    }

    void "test getFilterListResult matcher filtering"() {
        when:
        //matcher filtering
        def list = queryService.getFilterListResult([firstName: "eq:second user"])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '3'
    }

    void "test getFilterListResult q filtering"() {
        when:
        //q filtering
        def list = queryService.getFilterListResult([q: 'a one'])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '1'
    }

    void "test getFilterListResult matcher and q filtering"() {
        when:
        //matcher and q filtering to test 'and' with nested 'or'
        def list = queryService.getFilterListResult([surname:"eq:xyz", q: 'list c'])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '3'
    }

    void "test getFilterListResult searching by userId exact match should also match associated editors"() {
        when:
        def list = queryService.getFilterListResult([userId:"eq:1"])

        then:
        assert list.size() == 2
        assert list[0].dataResourceUid == "1"
        assert list[1].dataResourceUid == "3"
    }

    void "test getFilterListResult searching by userId should not match associated editors if not searching with eq"() {
        when:
        def list = queryService.getFilterListResult([userId:"ne:3"])

        then:
        assert list.size() == 2
        assert list[0].dataResourceUid == "1"
        assert list[1].dataResourceUid == "2"
    }
}
