/*
 * Copyright (C) 2012 Atlas of Living Australia
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

package au.org.ala.bie.webapp2

import grails.test.mixin.*
import grails.converters.JSON
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(UtilityService)
class UtilityServiceTests {
    def etc1 = null

    @Before
    void setup() {
        def jsonText = getClass().getResourceAsStream('redKangaroo.json').text
        assert jsonText.size() > 0
        println "etc1 = " + jsonText?.substring(0, 200)
        etc1 = JSON.parse(jsonText)
    }

    void testUtilityServiceInjection() {
        assertNotNull("utilityService is null", service)
    }

    void testJson() {
        assertNotNull("etc1 is null", etc1)
        assertTrue("commonNames is empty",  etc1.commonNames?.size() > 0)
        assertEquals("nameString error", etc1.commonNames?.get(0)?.nameString, "Red Kangaroo")
        println "commonNames = " + etc1.commonNames?.toString()?.substring(0,500)
        assertNotNull("etc1.commonNames is null", etc1.commonNames)
    }

    void testUnDuplicateNames() {
        def commonNamesDeduped = service.unDuplicateNames(etc1.commonNames)
        assertTrue("commonNames list is empty", commonNamesDeduped.size() > 0)
    }

    void testCommonNamesMap() {
        def nameMap = service.getNamesAsSortedMap(etc1.commonNames)
        assertTrue("commonNamesMap is empty", nameMap.size() > 0)
    }
}
