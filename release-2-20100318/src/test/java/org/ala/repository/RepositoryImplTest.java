/* *************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.repository;

import java.io.IOException;
import javax.inject.Inject;
import org.ala.model.Document;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * JUnit 4 test for RepositoryImpl
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:springTest.xml"})
public class RepositoryImplTest {
    @Inject
    @Qualifier("test")
    RepositoryImpl repository;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void directoryRootTest() {
        assertEquals("Checking repository cacheDirectoryRoot","/tmp/bie", repository.cacheDirectoryRoot);
    }

    //@Test
    public void storeDocumentTest() throws IOException  {
        String url = "http://www.ala.org.au/species/rdf";
        String content = "Just a line of content.";
        Document doc1 = repository.storeDocument(1, url, content.getBytes(), "text/html", null);
        assertEquals("filepath. ",doc1.getFilePath(), "/tmp/bie/1/0/1");
        assertEquals("id. ",doc1.getId(), 1);
        Document doc2 = repository.storeDocument(101010, "http://www.foo.com/rdf", "more content".getBytes(), "text/html", null);
        System.out.println("filepath check: "+doc2.getFilePath());
        assertEquals("filepath. ",doc2.getFilePath(), "/tmp/bie/101010/0/2");
        assertEquals("id. ",doc2.getId(), 2);
    }

    @Test
    public void storeRDFTest() {

    }

    @Test
    public void storeDublinCoreTest() {

    }
}
