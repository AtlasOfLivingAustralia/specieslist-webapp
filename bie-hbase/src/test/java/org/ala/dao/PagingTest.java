package org.ala.dao;

import java.util.List;

import junit.framework.TestCase;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SpeciesProfileDTO;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;

public class PagingTest extends TestCase {

	protected static Logger logger = Logger.getLogger(PagingTest.class);
	
	public void testGetPage2() throws Exception {
		
		TaxonConceptDao tcDao = SpringUtils.getContext().getBean(TaxonConceptDao.class);
		//page through DB
		String lastGuid = null;
		List<SpeciesProfileDTO> tcDTOs = tcDao.getProfilePage(lastGuid, 10000);
		int counter = 0;
		long start = System.currentTimeMillis();
		while(tcDTOs.size()>0){
			
			for(SpeciesProfileDTO e: tcDTOs){
				counter++;
				if(e!=null){
					//logger.debug("GUID: "+e.getGuid()+", ScientificName: "+e.getScientificName());
					lastGuid = e.getGuid();
				} else {
					logger.debug("NULL Taxon concept");
				}
				if(counter % 10000 == 0) { logger.info("################# Counter: "+ counter+", GUID: "+lastGuid); }
			}
			tcDTOs = tcDao.getProfilePage(lastGuid, 10000);
			logger.info("tcDTOS.size:" +tcDTOs.size());
		}
		long finish = System.currentTimeMillis();
		System.out.println("Concepts read: "+counter+", time taken: "+((finish-start)/1000) + " seconds.");
	}
	
	public void testGetPage() throws Exception {
		
		TaxonConceptDao tcDao = SpringUtils.getContext().getBean(TaxonConceptDao.class);
		//page through DB
		String lastGuid = null;
		List<ExtendedTaxonConceptDTO> tcDTOs = tcDao.getPage(lastGuid, 10);
		int counter = 0;
		long start = System.currentTimeMillis();
		while(tcDTOs.size()>0){
			
			for(ExtendedTaxonConceptDTO e: tcDTOs){
				counter++;
				if(e.getTaxonConcept()!=null){
					logger.debug("GUID: "+e.getTaxonConcept().getGuid()+", ScientificName: "+e.getTaxonConcept().getNameString());
					lastGuid = e.getTaxonConcept().getGuid();
				} else {
					logger.debug("NULL Taxon concept");
				}
				if(counter % 1000 == 0) { logger.info("################# Counter: "+ counter+", GUID: "+lastGuid); }
			}
			tcDTOs = tcDao.getPage(lastGuid, 10);
		}
		long finish = System.currentTimeMillis();
		System.out.println("Concepts read: "+counter+", time taken: "+((finish-start)/1000) + " seconds.");
	}
}
