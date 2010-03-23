/*
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
 */

package org.ala.repository;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.junit.Assert.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * JUnit test for {@see org.ala.repository.Validator Validator} class
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:springTest.xml"})
public class ValidatorTest {
    @Inject
    Validator v;

    /**
     * test with valid dc file (contents) should not throw exception
     *
     * @throws Exception
     */
    @Test
    public void testValidateDcFile0() throws Exception  {
        List<String[]> dc = getCompleteDcDoc();
        boolean thrown = false;
        try {
            v.validateDcFile(dc);
        } catch (Exception ex) {
            Assert.fail("unexpected exception");
        }
        assertFalse(thrown);
    }

    /**
     * test for empty dc file
     *
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testValidateDcFile1() throws Exception  {
        List<String[]> dc = getCompleteDcDoc();
        dc.clear();
        String[] s1 = {""};
        dc.add(s1);
        v.validateDcFile(dc);
    }

    /**
     * test for incorrect number of (tab) fields
     */
    //@Test(expected=IllegalArgumentException.class)
    @Test
    public void testValidateDcFile2()  {
        List<String[]> dc = getCompleteDcDoc();
        dc.remove(0);
        String[] s = {"identifier"};
        dc.add(0, s);
        boolean thrown = false;
        try {
            v.validateDcFile(dc);
        } catch (IllegalArgumentException ex) {
            thrown = true;
            assertEquals("Entry not expected size of 2, got 1 - identifier", ex.getMessage());
        } catch (Exception ex) {
            Assert.fail("unexpected exception");
        }
        assertTrue(thrown);
    }

    /**
     * test for invalid mime type
     *
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testValidateDcFile3() throws Exception {
        List<String[]> dc = getCompleteDcDoc();
        dc.remove(1);
        String[] s = {"format","test/xml"};
        dc.add(1,s);
        v.validateDcFile(dc);
    }

    /**
     * test for invalid modified date
     *
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testValidateDcFile4() throws Exception  {
        List<String[]> dc = getCompleteDcDoc();
        dc.remove(2);
        String[] s = {"modified", "09-22-2010"};
        dc.add(2,s);
        v.validateDcFile(dc);
    }

    /**
     * test for invalid URI
     *
     * @throws Exception
     */
    @Test(expected=MalformedURLException.class)
    public void testValidateDcFile5() throws Exception  {
        List<String[]> dc = getCompleteDcDoc();
        dc.remove(3);
        String[] s = {"URI", "www.google.com.au/q=ala"};
        dc.add(3,s);
        v.validateDcFile(dc);
    }

    /**
     * test with valid rdf file (contents) should not throw exception
     *
     * @throws Exception
     */
    @Test
    public void testValidateRdfFile0() throws Exception  {
        List<String[]> rdf = getCompleteRdfDoc();
        boolean thrown = false;
        try {
            v.validateRdfFile(rdf);
        } catch (Exception ex) {
            Assert.fail("unexpected exception");
        }
        assertFalse(thrown);
    }

    /**
     * test for empty rdf file
     *
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testValidateRdfFile1() throws Exception  {
        List<String[]> rdf = getCompleteDcDoc();
        rdf.clear();
        String[] s1 = {""};
        rdf.add(s1);
        v.validateDcFile(rdf);
    }

    /**
     * test for incorrect number of (tab) fields
     */
    //@Test(expected=IllegalArgumentException.class)
    @Test
    public void testValidateRdfFile2()  {
        List<String[]> rdf = getCompleteDcDoc();
        rdf.remove(0);
        String[] s = {"node14qr1m1nqx1", "http://ala.org.au/ontology/ALA#hasKingdom"};
        rdf.add(0, s);
        boolean thrown = false;
        try {
            v.validateRdfFile(rdf);
        } catch (IllegalArgumentException ex) {
            thrown = true;
            assertEquals("RDF Entry not expected size of 3, got 2 - node14qr1m1nqx1|http://ala.org.au/ontology/ALA#hasKingdom", ex.getMessage());
        } catch (Exception ex) {
            Assert.fail("unexpected exception");
        }
        assertTrue(thrown);
    }

    /**
     * test for invalid mime type
     *
     * @throws Exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testValidateRdfFile3() throws Exception {
        List<String[]> rdf = getCompleteDcDoc();
        rdf.remove(1);
        String[] s = {"node14qr1m1nqx1", "http://ala.org.au/ontology/ALA#hasScientificName", ""};
        rdf.add(1,s);
        v.validateRdfFile(rdf);
    }

    /**
     * Create a DC document (as if a file was parsed by {@see au.com.bytecode.opencsv.CsvReader CsVRead})
     *
     * @return 
     */
    private List<String[]> getCompleteDcDoc() {
        List<String[]> dc = new ArrayList<String[]>();
        String[] s0 = {"identifier", "http://www.ala.org.au/species/foo"};
        dc.add(s0);
        String[] s1 = {"format", "text/xml"};
        dc.add(s1);
        String[] s2 = {"modified", "2010-02-09"};
        dc.add(s2);
        String[] s3 = {"URI", "http://www.ala.org.au/species/foo?q=bar&n=bin"};
        dc.add(s3);
        
        return dc;
    }

    /**
     * Create a RDF document (parsed into List of String[]'s)
     *
     * @return
     */
    private List<String[]> getCompleteRdfDoc() {
        List<String[]> rdf = new ArrayList<String[]>();
        String[] s0 = {"node14qr1m1nqx1", 
                       "http://ala.org.au/ontology/ALA#hasKingdom",
                       "Animalia"};
        rdf.add(s0);
        String[] s1 = {"node14qr1m1nqx1", 
                       "http://ala.org.au/ontology/ALA#hasScientificName",
                       "Maccullochella peelii mariensis"};
        rdf.add(s1);
        String[] s2 = {"node14qr1m1nqx1", 
                       "http://ala.org.au/ontology/ALA#hasFamily",
                       "Percichthyidae"};
        rdf.add(s2);
        String[] s3 = {"node14qr1m1nqx1", 
                       "http://ala.org.au/ontology/ALA#hasDescriptiveText",
                       "Some text containing tab character\tand new \n lines."};
        rdf.add(s3);

        return rdf;
    }
}