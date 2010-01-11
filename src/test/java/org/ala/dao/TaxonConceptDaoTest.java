package org.ala.dao;

import junit.framework.TestCase;

public class TaxonConceptDaoTest extends TestCase {

	public void testVernacularName() throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDao();
		
		boolean isVernacular = tcDao.isVernacularConcept("urn:lsid:biodiversity.org.au:afd.taxon:3b915d97-2376-4c40-bd04-7ae0acbaa34b");
		
		assertTrue(isVernacular);
		
	}
	
}
