package au.org.ala.specieslist


import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class QueryServiceFilterListsTest extends Specification {

    QueryService service = new QueryService()

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
