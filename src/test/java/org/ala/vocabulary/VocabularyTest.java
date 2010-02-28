package org.ala.vocabulary;

import junit.framework.TestCase;

import org.ala.model.ConservationStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class VocabularyTest extends TestCase {

	public void testConservationStatusLookup(){
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring.xml");
		Vocabulary vocabulary = (Vocabulary) context.getBean("vocabulary");
		ConservationStatus cs = vocabulary.getConservationStatusFor(1009, "Conservation status in NSW: Endangered");
		assertEquals("Endangered",cs.getStatus());
		assertEquals("New South Wales",cs.getRegion());
	}

	public void testPestStatusLookup(){
		
		
	}
}
