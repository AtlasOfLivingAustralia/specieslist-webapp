package au.org.ala.specieslist.controller

import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.SpeciesListController
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(SpeciesListController)
@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
@Unroll
class SpeciesListControllerTest extends Specification {

    static final String CSV_CONTENT = "col1,col2\nval1,val2\n"
    static final String TAB_CONTENT = "col1\tcol2\nval3\tval4\n"


    def mockFile
    def mockMultipartRequest

    def setup() {
        mockFile = Mock(CommonsMultipartFile)

        mockMultipartRequest = Mock(MultipartHttpServletRequest)
        mockMultipartRequest.getFile(_) >> mockFile
        controller.helperService = new HelperService()
        controller.helperService.transactionManager = transactionManager
    }

    def "parseData should detect multi-part requests and read data from a file upload"() {
        setup:
        mockFile.getContentType() >> contentType
        // need to return a closure here so that a new stream is created each time the method is invoked
        mockFile.getInputStream() >> { new ByteArrayInputStream(fileContent.getBytes()) }
        controller.metaClass.request = mockMultipartRequest

        when:
        controller.parseData()

        then:
        println model
        assert model.dataRows == expected

        where:
        contentType     |   fileContent     |   expected
        "text/csv"      |   CSV_CONTENT     |   [["val1", "val2"]]
        "text/plain"    |   TAB_CONTENT     |   [["val3", "val4"]]
    }

    def "parseData should only accept valid content types for file uploads"() {
        setup:
        // need to return a closure here so that a new stream is created each time the method is invoked
        mockFile.getInputStream() >> { new ByteArrayInputStream(CSV_CONTENT.getBytes()) }
        mockFile.getContentType() >> contentType
        controller.metaClass.request = mockMultipartRequest

        when:
        controller.parseData()

        then:
        assert model.error == valid ? null : SpeciesListController.INVALID_FILE_TYPE_MESSAGE

        where:
        contentType        |  valid
        "text/plain"       |  true
        "text/csv"         |  true
        "application/zip"  |  false
    }

    def "parseData should read data from a form field if the request is not multi-part"() {
        setup:
        request.content = rawContent

        when:
        controller.parseData()

        then:
        println model
        assert model.dataRows == expected

        where:
        rawContent      |   expected
        CSV_CONTENT     |   [["val1", "val2"]]
        TAB_CONTENT     |   [["val3", "val4"]]
    }

}
