/***************************************************************************
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
package org.ala.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.ala.model.Term;
import org.ala.util.StatusType;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JUnit 4 test for VocabularyDAOImpl
 * 
 * @author Tommy Wang
 * @author Dave Martin
 */
public class VocabularyDAOTest extends TestCase{
	
    private VocabularyDAO vocabularyDAO;
    private static ApplicationContext context;
    
	final String expectedPreferredPestTerm = "invasive";
	final int pestInfosourceId = 1023;
	final String pestPredicate = "hasPestStatus";
	final String pestRawValue = "Exotic (absent from Australia)";
	
	final String expectedPreferredConservationTerm = "Near Threatened";
	final int conservationInfosourceId = 1017;
	final String conservationPredicate = "hasConservationStatus";
	final String conservationRawValue = "Near Threatened";
	
	final String expectedRduPreferredConservationTerm = "Critically Endangered";
	final int rduConservationInfosourceId = 1025;
	final String rduConservationPredicate = "hasConservationStatus";
	final String rduConservationRawValue = "rare or likely to become extinct";
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
		DataSource dataSource = (DataSource) context.getBean("dataSource");
		vocabularyDAO = new VocabularyDAOImpl(dataSource);
	}
	
	@Test
	public void testLookups() {
		
		// pest status test		
		List<Term> preferredTermObj = vocabularyDAO.getPreferredTermsFor(pestInfosourceId, pestPredicate, pestRawValue);
		Assert.assertEquals(1, preferredTermObj.size());
		if(preferredTermObj.size()==1){
			Term singleTerm = preferredTermObj.get(0);
			System.out.println(singleTerm.getPredicate() + "\t" + singleTerm.getTermString() + "\t" + singleTerm.getVocabularyId());
			Assert.assertEquals(expectedPreferredPestTerm, singleTerm.getTermString());
		}
		
		// conservation status test
		preferredTermObj = vocabularyDAO.getPreferredTermsFor(conservationInfosourceId, conservationPredicate, conservationRawValue);
		Assert.assertEquals(1, preferredTermObj.size());
		if(preferredTermObj.size()==1){
			Term singleTerm = preferredTermObj.get(0);
			System.out.println(singleTerm.getPredicate() + "\t" + singleTerm.getTermString() + "\t" + singleTerm.getVocabularyId());
			Assert.assertEquals(expectedPreferredConservationTerm, singleTerm.getTermString());
		}
		
		// rdu conservation status test
		preferredTermObj = vocabularyDAO.getPreferredTermsFor(rduConservationInfosourceId, rduConservationPredicate, rduConservationRawValue);
		Assert.assertEquals(1, preferredTermObj.size());
		if(preferredTermObj.size()==1){
			Term singleTerm = preferredTermObj.get(0);
			System.out.println(singleTerm.getPredicate() + "\t" + singleTerm.getTermString() + "\t" + singleTerm.getVocabularyId());
			Assert.assertEquals(expectedRduPreferredConservationTerm, singleTerm.getTermString());
		}

        // test status lookup
        Map<String, Integer> pestTerms = vocabularyDAO.getTermMapForStatusType(StatusType.PEST);
        System.out.println("====\n"+pestTerms.size()+" pest terms:\n- "+StringUtils.join(pestTerms.keySet(), "\n- "));
        Map<String, Integer> conservationTerms = vocabularyDAO.getTermMapForStatusType(StatusType.CONSERVATION);
        System.out.println("====\n"+conservationTerms.size()+" conservation terms:\n- "+StringUtils.join(conservationTerms.keySet(), "\n- "));
	}
}
