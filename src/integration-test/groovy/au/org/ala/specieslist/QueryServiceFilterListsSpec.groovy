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


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class QueryServiceFilterListsSpec extends Specification {

    @Autowired QueryService service

    void tearDown() {
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }
    }

    def "filterLists should return all lists in the db containing any of the names when the drId list is empty"() {
        setup:
        SpeciesListItem.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList list1 = new SpeciesList(dataResourceUid: "dr1", username: "bla", listName: "list1").save(failOnError: true, flush: true)
        SpeciesList list2 = new SpeciesList(dataResourceUid: "dr2", username: "bla", listName: "list2").save(failOnError: true, flush: true)
        SpeciesList list3 = new SpeciesList(dataResourceUid: "dr3", username: "bla", listName: "list3").save(failOnError: true, flush: true)

        new SpeciesListItem(rawScientificName: "name1", guid: "1", dataResourceUid: "dr1", mylist: list1, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "name2", guid: "2", dataResourceUid: "dr2", mylist: list2, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "name1", guid: "2", dataResourceUid: "dr3", mylist: list3, itemOrder: 1).save(flush: true, failOnError: true)


        when:
        List<String> results = service.filterLists(["name1"], [])

        then:
        assert results.size() == 2
        assert results.contains("dr1")
        assert results.contains("dr3")
    }

    def "filterLists should return only lists from the specified drId set containing any of the names"() {
        setup:
        SpeciesListItem.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList list1 = new SpeciesList(dataResourceUid: "dr1", username: "bla", listName: "list1").save(failOnError: true, flush: true)
        SpeciesList list2 = new SpeciesList(dataResourceUid: "dr2", username: "bla", listName: "list2").save(failOnError: true, flush: true)
        SpeciesList list3 = new SpeciesList(dataResourceUid: "dr3", username: "bla", listName: "list3").save(failOnError: true, flush: true)

        new SpeciesListItem(rawScientificName: "name1", guid: "1", dataResourceUid: "dr1", mylist: list1, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "name2", guid: "2", dataResourceUid: "dr2", mylist: list2, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "name1", guid: "2", dataResourceUid: "dr3", mylist: list3, itemOrder: 1).save(flush: true, failOnError: true)


        when:
        List<String> results = service.filterLists(["name1"], ["dr1"])

        then:
        assert results.size() == 1
        assert results.contains("dr1")
    }

    def "filterLists should match on either the matchedName or rawScientificName"() {
        setup:
        SpeciesListItem.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList list1 = new SpeciesList(dataResourceUid: "dr1", username: "bla", listName: "list1").save(failOnError: true, flush: true)
        SpeciesList list2 = new SpeciesList(dataResourceUid: "dr2", username: "bla", listName: "list2").save(failOnError: true, flush: true)
        SpeciesList list3 = new SpeciesList(dataResourceUid: "dr3", username: "bla", listName: "list3").save(failOnError: true, flush: true)

        new SpeciesListItem(rawScientificName: "name1", guid: "1", dataResourceUid: "dr1", mylist: list1, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "name2", guid: "2", dataResourceUid: "dr2", mylist: list2, itemOrder: 1).save(flush: true, failOnError: true)
        new SpeciesListItem(rawScientificName: "blabla", matchedName: "name1", guid: "2", dataResourceUid: "dr3", mylist: list3, itemOrder: 1).save(flush: true, failOnError: true)


        when:
        List<String> results = service.filterLists(["name1"])

        then:
        assert results.size() == 2
        assert results.contains("dr1")
        assert results.contains("dr3")

        cleanup:
        SpeciesListItem.findAll().each { it.delete(flush: true, failOnError: true) }
        SpeciesList.findAll().each { it.delete(flush: true, failOnError: true) }
    }

}
