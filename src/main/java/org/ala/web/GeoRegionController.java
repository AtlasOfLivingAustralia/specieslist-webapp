/**************************************************************************
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
package org.ala.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.GeoRegionDao;
import org.ala.dto.SearchResultsDTO;
import org.ala.model.GeoRegion;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for serving region pages and views around regions.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Controller("geoRegionController")
public class GeoRegionController {

	/** Logger initialisation */
	private final static Logger logger = Logger.getLogger(GeoRegionController.class);
	
	/** DAO bean for access to taxon concepts */
	@Inject
	private GeoRegionDao geoRegionDao;
	/** DAO bean for SOLR search queries */
	@Inject
	private FulltextSearchDao searchDao;
	/** Name of view for an empty search page */
	private final String GEOREGION_SHOW = "regions/show";
	private final String GEOREGION_TAXA_SHOW = "regions/taxaShow";
	private final String HOME_PAGE = "regions/list";
	
	/**
	 * Default view when a region isnt specified.
	 * 
	 * @return
	 */
	@RequestMapping("/regions/")
	public String homePageHandler() {
		return HOME_PAGE;
	}
	
	/**
	 * View a specific region with breakdowns for selected higher taxa.
	 * 
	 * @param regionType
	 * @param regionName
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/regions/{regionType}/{regionName}", method = RequestMethod.GET)
	public String show(
			@PathVariable("regionType") String regionType,
			@PathVariable("regionName") String regionName, 
			Model model) throws Exception {
		
		String guid = regionType + "/" +regionName;
		logger.debug("Retrieving concept with guid: " + guid);
		GeoRegion geoRegion = geoRegionDao.getByGuid(guid);
		model.addAttribute("geoRegion", geoRegion);
		
		//birds counts
		SearchResultsDTO birds = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "class", "Aves", null, 0, 25, "scientificNameRaw", "asc");
		model.addAttribute("birds", birds);
		
		//mammal counts
		SearchResultsDTO mammals = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "class", "Mammalia", null, 0, 25, "scientificNameRaw", "asc");
		model.addAttribute("mammals", mammals);

		//reptile counts
		SearchResultsDTO reptiles = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "class", "Reptilia", null, 0, 25, "scientificNameRaw", "asc");
		model.addAttribute("reptiles", reptiles);

		//frog counts
		SearchResultsDTO frogs = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "class", "Amphibia", null, 0, 25, "scientificNameRaw", "asc");
		model.addAttribute("frogs", frogs);

		//fish counts
		List<String> fishTaxa = new ArrayList<String>();
		fishTaxa.add("Myxini");
		fishTaxa.add("Petromyzontida");
		fishTaxa.add("Chondrichthyes");
		fishTaxa.add("Sarcopterygii");
		fishTaxa.add("Actinopterygii");
		SearchResultsDTO fish = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "bioOrder", fishTaxa, null, 0, 25, "scientificNameRaw", "asc");
		model.addAttribute("fish", fish);

		return GEOREGION_SHOW;
	}
	
	/**
	 * Download a list of species within a higher taxon group, that have occurred within a region.
	 * 
	 * @param regionType
	 * @param regionName
	 * @param higherTaxon
	 * @param rank
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/regions/{regionType}/{regionName}/download*", method = RequestMethod.GET)
	public String downloadSpeciesList(
			@PathVariable("regionType") String regionType,
			@PathVariable("regionName") String regionName, 
			@RequestParam("higherTaxon") String higherTaxa,
			@RequestParam("rank") String rank,
            HttpServletResponse response)
            throws Exception {
        
		if(higherTaxa==null)
			return null;
		
		String[] taxa = higherTaxa.trim().split(",");
		List<String> taxaList = Arrays.asList(taxa);
		
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Pragma", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment;filename=speciesList");
        response.setContentType("application/vnd.ms-excel");
        ServletOutputStream out = response.getOutputStream();
        try {
        	searchDao.writeSpeciesByRegionAndHigherTaxon("state", regionName, rank, taxaList, out);
        } catch (Exception e){
        	e.printStackTrace();
        }
        return null;
	}
	
	/**
	 * Example regions/taxa?regionType=state&regionName=Tasmania&higherTaxon=Mammalia&rank=class
	 * 
	 * @param regionType
	 * @param regionName
	 * @param taxon
	 * @param rank
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/regions/taxa.json", method = RequestMethod.GET)
	public String showTaxa(
			@RequestParam("regionType") String regionType,
			@RequestParam("regionName") String regionName, 
			@RequestParam("higherTaxon") String higherTaxa,
			@RequestParam("rank") String rank,
			Model model) throws Exception {

		if(higherTaxa==null)
			return null;
		
		String[] taxa = higherTaxa.trim().split(",");
		List<String> taxaList = Arrays.asList(taxa);
		
		SearchResultsDTO searchResults = searchDao.findAllSpeciesByRegionAndHigherTaxon(
				"state", regionName, rank, taxaList, 
				null, 0, 100, "scientificNameRaw", "asc");
		
		model.addAttribute("searchResults", searchResults);
		
		return GEOREGION_TAXA_SHOW;
	}
	
	/**
	 * Retrieve a list of species
	 * 
	 * @param regionType
	 * @param regionName
	 * @param altRegionType
	 * @param altRegionName
	 * @param higherTaxon
	 * @param rank
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/regions/taxaDiff.json", method = RequestMethod.GET)
	public String showTaxaDiff(
			@RequestParam("regionType") String regionType,
			@RequestParam("regionName") String regionName, 
			@RequestParam("altRegionType") String altRegionType,
			@RequestParam("altRegionName") String altRegionName, 
			@RequestParam("higherTaxon") String higherTaxon,
			@RequestParam("rank") String rank,
			@RequestParam(defaultValue="false", value="inCommon") boolean inCommon,
			Model model) throws Exception {
		
		if(higherTaxon==null)
			return null;
		
		String[] taxa = higherTaxon.trim().split(",");
		List<String> taxaList = Arrays.asList(taxa);

		SearchResultsDTO searchResults = searchDao.findAllDifferencesInSpeciesByRegionAndHigherTaxon(
				regionType, regionName, 
				regionType, altRegionName,
				rank, taxaList,
				null, 0, 100, "scientificNameRaw", "asc");
		
		model.addAttribute("searchResults", searchResults);
		
		return GEOREGION_TAXA_SHOW;
	}
	
	/**
	 * @param geoRegionDao the geoRegionDao to set
	 */
	public void setGeoRegionDao(GeoRegionDao geoRegionDao) {
		this.geoRegionDao = geoRegionDao;
	}

	/**
	 * @param searchDao the searchDao to set
	 */
	public void setSearchDao(FulltextSearchDao searchDao) {
		this.searchDao = searchDao;
	}
}
