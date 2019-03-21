package au.org.ala.specieslist

import au.org.ala.web.AuthService
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.Integration
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
@TestMixin(ControllerUnitTestMixin)
class QueryServiceSpec extends Specification{

    AuthService authService = Mock(AuthService)
    LocalAuthService localAuthService = Mock(LocalAuthService)
    QueryService queryService = Mock(QueryService)

//    @Before
    void setup() {
        println "In setup"
        SpeciesList.findAll().each {
            it.delete(flush: true, failOnError: true)
        }

        Calendar calendar = Calendar.getInstance(TimeZone.default) // Ensure sort sequence
        def list1 = new SpeciesList(dateCreated: calendar.time, lastUpdated: calendar.time, dataResourceUid: "1", userId: 1,
                username: "b", description: "1", firstName: "first user", listName: "list a one", surname: "xyz")
        calendar.add(Calendar.SECOND, 1)
        def list2 = new SpeciesList(dateCreated: calendar.time, lastUpdated: calendar.time, dataResourceUid: "2", userId: 2,
                username: "c", description: "2", firstName: "first user", listName: "list b two", surname: "surname")
        calendar.add(Calendar.SECOND, 1)
        def list3 = new SpeciesList(dateCreated: calendar.time, lastUpdated: calendar.time, dataResourceUid: "3", userId: 3,
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

//    @Test
    void "test getFilterListResult ordering"() {
        when:
        //default order
        def list = queryService.getFilterListResult([:])
        
        then:
        assert list[0].dataResourceUid == '1'
        assert list[1].dataResourceUid == '2'
        assert list[2].dataResourceUid == '3'
    }

//    @Test
    void "test getFilterListResult user ordering"() {
        when:
        //user ordering
        def list = queryService.getFilterListResult([user: 'a'])

        then:
        assert list[0].dataResourceUid == '3'
        assert list[1].dataResourceUid == '2'
        assert list[2].dataResourceUid == '1'
    }

//    @Test
    void "test getFilterListResult defined ordering"() {
        when:
        //defined ordering
        def list = queryService.getFilterListResult([sort: 'description', order: 'desc'])

        then:
        assert list[0].dataResourceUid == '3'
        assert list[1].dataResourceUid == '2'
        assert list[2].dataResourceUid == '1'
    }

//    @Test
    void "test getFilterListResult matcher filtering"() {
        when:
        //matcher filtering
        def list = queryService.getFilterListResult([firstName: "eq:second user"])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '3'
    }

//    @Test
    void "test getFilterListResult q filtering"() {
        when:
        //q filtering
        def list = queryService.getFilterListResult([q: 'a one'])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '1'
    }

//    @Test
    void "test getFilterListResult matcher and q filtering"() {
        when:
        //matcher and q filtering to test 'and' with nested 'or'
        def list = queryService.getFilterListResult([surname:"eq:xyz", q: 'list c'])

        then:
        assert list.size() == 1
        assert list[0].dataResourceUid == '3'
    }

//    @Test
    void "test getFilterListResult searching by userId exact match should also match associated editors"() {
        when:
        def list = queryService.getFilterListResult([userId:"eq:1"])

        then:
        assert list.size() == 2
        assert list[0].dataResourceUid == "1"
        assert list[1].dataResourceUid == "3"
    }

//    @Test
    void "test getFilterListResult searching by userId should not match associated editors if not searching with eq"() {
        when:
        def list = queryService.getFilterListResult([userId:"ne:3"])

        then:
        assert list.size() == 2
        assert list[0].dataResourceUid == "1"
        assert list[1].dataResourceUid == "2"
    }
}
