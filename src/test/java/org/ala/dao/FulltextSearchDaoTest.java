package org.ala.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.util.SpringUtils;

public class FulltextSearchDaoTest extends TestCase {

	/**
	 * @param args
	 */
	public void testFindAllInRegion() throws Exception {

		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO srDTO = null;
		List<SearchTaxonConceptDTO> tcs = null;
		
		//VIC
		srDTO = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", "Victoria", "kingdom", "Animalia", 
				null, 0, 10, "score", "asc");
		tcs = srDTO.getTaxonConcepts();
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getNameString() + " " + tc.getCommonName());
		}
		
		//NSW
		srDTO = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", "New South Wales", "kingdom", "Animalia", 
				null, 0, 10, "score", "asc");
		tcs = srDTO.getTaxonConcepts();
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getNameString() + " " + tc.getCommonName());
		}
	}
	
	public void testCountAllInRegion() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");

		//Animals in Victoria
		int count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "Victoria", "kingdom", "Animalia");
		System.out.println("Animals records in Victoria: "+count);
		
		//Plants in Victoria
		count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "Victoria", "kingdom", "Plantae");
		System.out.println("Plants records in Victoria: "+count);

		//Animals in New South Wales
		count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "New South Wales", "kingdom", "Animalia");
		System.out.println("Animals records in New South Wales: "+count);
		
		//Plants in New South Wales
		count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "New South Wales", "kingdom", "Plantae");
		System.out.println("Plants records in New South Wales: "+count);
//
//		//Animals in Queensland
//		count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "Queensland", "kingdom", "Animalia");
//		System.out.println("Animals records in Queensland: "+count);
//		
//		//Plants in Queensland
//		count = searchDao.countSpeciesByRegionAndHigherTaxon("state", "Queensland", "kingdom", "Plantae");
//		System.out.println("Plants records in Queensland: "+count);
	}
	
	public void testDifferencesInRegions() throws Exception {
		
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO srDTO = null;
		List<SearchTaxonConceptDTO> tcs = null;
		
		List<String> taxa = new ArrayList<String>();
		taxa.add("Animalia");
		
		//VIC
		srDTO = searchDao.findAllDifferencesInSpeciesByRegionAndHigherTaxon(
				"state", "Victoria",
				"state", "New South Wales",
				"kingdom", taxa, 
				null, 0, 10, "scientificNameRaw", "asc");
		tcs = srDTO.getTaxonConcepts();
		System.out.println("Species found in Victoria but not in NSW");
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getNameString() + " " + tc.getCommonName());
		}
	}
}
