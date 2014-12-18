package au.org.ala.specieslist

import org.junit.After
import org.junit.Before
import org.junit.Test

class QueryServiceTests extends GroovyTestCase {

    def queryService

    @Before
    void setUp() {
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }

        super.setUp()
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
    void "test getFilterListResult ordering"() {

        //default order
        def list = queryService.getFilterListResult([:])
        assertTrue list[0].dataResourceUid == '1'
        assertTrue list[1].dataResourceUid == '2'
        assertTrue list[2].dataResourceUid == '3'
    }

    @Test
    void "test getFilterListResult user ordering"() {

        //user ordering
        def list = queryService.getFilterListResult([user: 'a'])
        assertTrue list[0].dataResourceUid == '3'
        assertTrue list[1].dataResourceUid == '1'
        assertTrue list[2].dataResourceUid == '2'
    }

    @Test
    void "test getFilterListResult defined ordering"() {

        //defined ordering
        def list = queryService.getFilterListResult([sort: 'description', order: 'desc'])
        assertTrue list[0].dataResourceUid == '3'
        assertTrue list[1].dataResourceUid == '2'
        assertTrue list[2].dataResourceUid == '1'
    }

    @Test
    void "test getFilterListResult matcher filtering"() {

        //matcher filtering
        def list = queryService.getFilterListResult([firstName: "eq:second user"])
        assertTrue list.size() == 1
        assertTrue list[0].dataResourceUid == '3'
    }

    @Test
    void "test getFilterListResult q filtering"() {

        //q filtering
        def list = queryService.getFilterListResult([q: 'a one'])
        assertTrue list.size() == 1
        assertTrue list[0].dataResourceUid == '1'
    }

    @Test
    void "test getFilterListResult matcher and q filtering"() {
        //matcher and q filtering to test 'and' with nested 'or'
        def list = queryService.getFilterListResult([surname:"eq:xyz", q: 'list c'])
        assertTrue list.size() == 1
        assertTrue list[0].dataResourceUid == '3'
    }
}
