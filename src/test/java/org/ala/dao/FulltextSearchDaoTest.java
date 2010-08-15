package org.ala.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ala.dto.SearchDTO;
import org.ala.dto.SearchRegionDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.util.SpringUtils;

public class FulltextSearchDaoTest extends TestCase {

	public void testGetRegions() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO<SearchRegionDTO> rs = (SearchResultsDTO<SearchRegionDTO>) searchDao.findAllRegionsByType(RegionTypes.STATE);
		for(SearchRegionDTO r: rs.getResults()){
			System.out.println(r.getName());
		}
	}
	
	public void testGetChildConceptsWithNS() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		List<SearchTaxonConceptDTO> tcs = searchDao.getChildConceptsByNS(2126057,2126076, 7000);
		for(SearchTaxonConceptDTO stc: tcs){
			System.out.println(stc.getName()+", "+stc.getRank()+", left: "+stc.getLeft()+", common name: "+stc.getCommonNameSingle());
		}
	}
	
	public void testGetChildConceptsWithParentId() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		List<SearchTaxonConceptDTO> tcs = searchDao.getChildConceptsParentId("464487");
		for(SearchTaxonConceptDTO stc: tcs){
			System.out.println(stc.getName()+", "+stc.getRank()+", left: "+stc.getLeft()+", common name: "+stc.getCommonNameSingle());
		}
	}
	
	public void testGetClassification() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		
		List<SearchTaxonConceptDTO> tcs = searchDao.findByScientificName("Macropus rufus", 10);
		for(SearchTaxonConceptDTO stc: tcs){
			
			System.out.println(stc.getName()+", "+stc.getRank()+", left: "+stc.getLeft());
			if(stc.getLeft()!=null){
				
				SearchResultsDTO<SearchTaxonConceptDTO> searchResults = searchDao.getClassificationByLeftNS(stc.getLeft());
				for(SearchTaxonConceptDTO t: searchResults.getResults()){
					System.out.println(t.getName()+", "+t.getRank()+", left: "+t.getLeft()+", right: "+t.getRight());
				}
			}
		}
	}

	public void testFindWithPartial() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO<SearchDTO> srDTO = searchDao.findByName(IndexedTypes.TAXON, "Tachyglossus ac*", null, 0, 10, "score", "asc");
		for(SearchDTO tc: srDTO.getResults()){
			System.out.println(tc.getName());
		}
		System.out.println("Number of results: "+srDTO.getResults().size());
	}	
	
	public void testFindTaxa() throws Exception {
		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO<SearchTaxonConceptDTO> srDTO = searchDao.findByScientificName("Macropus", null, 0, 10, "score", "asc");
		for(SearchDTO tc: srDTO.getResults()){
			System.out.println(tc.getName());
		}
		System.out.println("Number of results: "+srDTO.getResults().size());
	}
	
	public void testFindCollections() throws Exception {

		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO<SearchDTO> srDTO = searchDao.findByName(IndexedTypes.COLLECTION, "ANIC", null, 0, 10, "score", "asc");
		for(SearchDTO tc: srDTO.getResults()){
			System.out.println(tc.getName());
		}
		System.out.println("Number of results: "+srDTO.getResults().size());
		
		
		//Australian National Insect Collection
		srDTO = searchDao.findByName(IndexedTypes.COLLECTION, "Australian National Insect Collection", null, 0, 10, "score", "asc");
		for(SearchDTO tc: srDTO.getResults()){
			System.out.println(tc.getName());
		}
		System.out.println("Number of results: "+srDTO.getResults().size());
		
		//Australian National Insect Collection
		srDTO = searchDao.findByName(IndexedTypes.COLLECTION, "Australian National", null, 0, 10, "score", "asc");
		for(SearchDTO tc: srDTO.getResults()){
			System.out.println(tc.getName());
		}
		System.out.println("Number of results: "+srDTO.getResults().size());
	}
	
	/**
	 * Test find all in region.
	 * 
	 * @param args
	 */
	public void testFindAllInRegion() throws Exception {

		FulltextSearchDao searchDao = (FulltextSearchDao) SpringUtils.getContext().getBean("fulltextSearchDaoImplSolr");
		SearchResultsDTO srDTO = null;
		List<SearchTaxonConceptDTO> tcs = null;
		
		//VIC
		srDTO = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", "Victoria", "kingdom", "Animalia", 
				null, 0, 10, "score", "asc");
		tcs = srDTO.getResults();
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getName() + " " + tc.getCommonName()+" image URL:" +tc.getImage());
		}
		
		//NSW
		srDTO = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", "New South Wales", "kingdom", "Animalia", 
				null, 0, 10, "score", "asc", true);
		tcs = srDTO.getResults();
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getName() + " " + tc.getCommonName()+" image URL:" +tc.getImage());
		}
	}
	
	/**
	 * Test count in region.
	 * 
	 * @throws Exception
	 */
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
		tcs = srDTO.getResults();
		System.out.println("Species found in Victoria but not in NSW");
		for(SearchTaxonConceptDTO tc: tcs){
			System.out.println(tc.getName() + " " + tc.getCommonName());
		}
	}
}
