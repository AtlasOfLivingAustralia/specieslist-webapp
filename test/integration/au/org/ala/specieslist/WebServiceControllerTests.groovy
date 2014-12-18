package au.org.ala.specieslist

import org.junit.After
import org.junit.Before
import org.junit.Test

class WebServiceControllerTests  extends GroovyTestCase {

    @Before
    void setUp() {
        super.setUp()

        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }

        def list1 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "1",
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        def list2 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "2",
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        def list3 = new SpeciesList(dateCreated: new Date(), lastUpdated: new Date(), dataResourceUid: "3",
                username: "a", description: "3", firstName: "second user", listName: "list c three", surname: "xyz")
        list1.save(flush: true, failOnError: true)
        list2.save(flush: true, failOnError: true)
        list3.save(flush: true, failOnError: true)
    }

    @After
    void tearDown() {
        super.tearDown()
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }
    }

    @Test
    void "test SpeciesLists"() {
        WebServiceController wsc = new WebServiceController()

        wsc.getListDetails()

        assertTrue wsc.response.json.lists.size() == 3
    }

    @Test
    void "test SpeciesLists user order, q filtering"() {
        WebServiceController wsc = new WebServiceController()
        wsc.params.user = 'a'
        wsc.params.q = 'xyz'

        wsc.getListDetails()

        assertTrue wsc.response.json.lists.size() == 2
        assertTrue wsc.response.json.lists[0].dataResourceUid == '3'
        assertTrue wsc.response.json.lists[1].dataResourceUid == '1'
    }

    @Test
    void "test SpeciesLists default order, q filter"() {
        WebServiceController wsc = new WebServiceController()
        wsc.params.q = 'first'

        wsc.getListDetails()

        assertTrue wsc.response.json.lists.size() == 2
        assertTrue wsc.response.json.lists[0].dataResourceUid == '1'
        assertTrue wsc.response.json.lists[1].dataResourceUid == '2'
    }
}
