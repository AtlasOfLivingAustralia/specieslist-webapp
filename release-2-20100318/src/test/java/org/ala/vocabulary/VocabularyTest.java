/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
 ***************************************************************************/
package org.ala.vocabulary;

import junit.framework.TestCase;

import org.ala.model.ConservationStatus;
import org.ala.model.PestStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JUnit tests for Vocabulary API
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class VocabularyTest extends TestCase {

	public void testConservationStatusLookup(){
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		Vocabulary vocabulary = (Vocabulary) context.getBean("vocabulary");
		ConservationStatus cs = vocabulary.getConservationStatusFor(1009, "Conservation status in NSW: Endangered");
		assertEquals("Endangered",cs.getStatus());
		assertEquals("New South Wales",cs.getRegion());
	}

	public void testPestStatusLookup(){
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		Vocabulary vocabulary = (Vocabulary) context.getBean("vocabulary");
		PestStatus ps = vocabulary.getPestStatusFor(1023, "Exotic (absent from Australia)");
		assertEquals("invasive", ps.getStatus());
	}
}
