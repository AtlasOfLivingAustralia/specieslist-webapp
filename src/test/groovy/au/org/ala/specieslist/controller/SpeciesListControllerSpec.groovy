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

package au.org.ala.specieslist.controller

import au.org.ala.specieslist.HelperService
import au.org.ala.specieslist.SpeciesListController
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class SpeciesListControllerSpec extends Specification implements ControllerUnitTest<SpeciesListController>, DataTest {

    static final String CSV_CONTENT = "col1,col2\nval1,val2\n"
    static final String TAB_CONTENT = "col1\tcol2\nval3\tval4\n"

    def mockFile = Mock(MultipartFile)
    def mockMultipartRequest = Mock(MultipartHttpServletRequest)

    def setup() {
        mockMultipartRequest.getFile(_) >> mockFile
        controller.helperService = new HelperService()
//        controller.helperService.transactionManager = transactionManager
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
