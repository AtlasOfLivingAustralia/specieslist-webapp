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
import org.ala.dao.RegionTypes;
import org.ala.dto.ExtendedGeoRegionDTO;
import org.ala.dto.SearchRegionDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
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
	@Inject
	protected RepoUrlUtils repoUrlUtils;
	/** Name of view for an empty search page */
	private final String GEOREGION_SHOW = "regions/show";
	private final String GEOREGION_TAXA_SHOW = "regions/taxaShow";
	private final String HOME_PAGE = "regions/browse";
	
	/**
	 * Default view when a region isnt specified.
	 * 
	 * @return
	 */
	@RequestMapping("/regions/")
	public String homePageHandler(Model model) throws Exception {
		SearchResultsDTO<SearchRegionDTO> states = searchDao.findAllRegionsByType(RegionTypes.STATE);
		SearchResultsDTO<SearchRegionDTO> lga = searchDao.findAllRegionsByType(RegionTypes.LGA);
		SearchResultsDTO<SearchRegionDTO> ibra = searchDao.findAllRegionsByType(RegionTypes.IBRA);
		SearchResultsDTO<SearchRegionDTO> imcra = searchDao.findAllRegionsByType(RegionTypes.IMCRA);
		model.addAttribute("states", states.getResults());
		model.addAttribute("lga", lga.getResults());
		model.addAttribute("ibra", ibra.getResults());
		model.addAttribute("imcra", imcra.getResults());
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
		logger.debug("Retrieving region with guid: " + guid);
		ExtendedGeoRegionDTO geoRegion = geoRegionDao.getExtendedGeoRegionByGuid(guid);
		model.addAttribute("geoRegion", geoRegion.getGeoRegion());
		model.addAttribute("extendedGeoRegion", geoRegion);
		
		Integer regionTypeId = Integer.parseInt(geoRegion.getGeoRegion().getRegionType());
		RegionTypes rt = RegionTypes.getRegionType(regionTypeId);
		model.addAttribute("regionType", rt);
		
		//retrieve the other regions to compare to
		SearchResultsDTO<SearchRegionDTO> otherRegions = searchDao.findAllRegionsByType(rt);
		model.addAttribute("otherRegions", otherRegions.getResults());
		
		//birds counts
		SearchResultsDTO birds = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Aves", null, 0, 24, "scientificNameRaw", "asc", true);
		int birdsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Aves");
		model.addAttribute("birds", repoUrlUtils.fixRepoUrls(birds));
		model.addAttribute("birdCount", birdsCount);
		
		//mammal counts
		SearchResultsDTO mammals = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Mammalia", null, 0, 24, "scientificNameRaw", "asc", true);
		int mammalsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Mammalia");
		model.addAttribute("mammals", repoUrlUtils.fixRepoUrls(mammals));
		model.addAttribute("mammalCount", mammalsCount);

		//reptile counts
		SearchResultsDTO reptiles = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Reptilia", null, 0, 24, "scientificNameRaw", "asc", true);
		int reptilesCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Reptilia");
		model.addAttribute("reptiles", repoUrlUtils.fixRepoUrls(reptiles));
		model.addAttribute("reptileCount", reptilesCount);
		
		//frog counts
		SearchResultsDTO frogs = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Amphibia", null, 0, 24, "scientificNameRaw", "asc", true);
		int frogsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Amphibia");
		model.addAttribute("frogs", repoUrlUtils.fixRepoUrls(frogs));
		model.addAttribute("frogCount", frogsCount);

		//anthropods counts
		SearchResultsDTO arthropods = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "phylum", "Arthropoda", null, 0, 24, "scientificNameRaw", "asc", true);
		int arthropodsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", "Arthropoda");
		model.addAttribute("arthropods", repoUrlUtils.fixRepoUrls(arthropods));
		model.addAttribute("arthropodCount", arthropodsCount);
		
		//molluscs counts
		SearchResultsDTO molluscs = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "phylum", "Mollusca", null, 0, 24, "scientificNameRaw", "asc", true);
		int molluscsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "phylum", "Mollusca");
		model.addAttribute("molluscs", repoUrlUtils.fixRepoUrls(molluscs));
		model.addAttribute("molluscCount", molluscsCount);
		
		//angiosperms counts
		SearchResultsDTO angiosperms = searchDao.findAllSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "phylum", "Magnoliophyta", null, 0, 24, "scientificNameRaw", "asc", true);
		int angiospermsCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "phylum", "Magnoliophyta");
		model.addAttribute("angiosperms", repoUrlUtils.fixRepoUrls(angiosperms));
		model.addAttribute("angiospermCount", angiospermsCount);
		
		//fish counts
		List<String> fishTaxa = new ArrayList<String>();
		fishTaxa.add("Myxini");
		fishTaxa.add("Chondrichthyes");
		fishTaxa.add("Sarcopterygii");
		fishTaxa.add("Actinopterygii");
		SearchResultsDTO fish = searchDao.findAllSpeciesByRegionAndHigherTaxon("state", regionName, "class", fishTaxa, null, 0, 24, "scientificNameRaw", "asc", true);
		int fishCount = searchDao.countSpeciesByRegionAndHigherTaxon(rt.toString(), regionName, "class", fishTaxa);
		model.addAttribute("fish", repoUrlUtils.fixRepoUrls(fish));
		model.addAttribute("fishCount", fishCount);

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
			@RequestParam(value="title", defaultValue="speciesList",required=false) String downloadTitle,
            HttpServletResponse response)
            throws Exception {
        
		if(higherTaxa==null)
			return null;
		
		String[] taxa = higherTaxa.trim().split(",");
		List<String> taxaList = Arrays.asList(taxa);
		
        response.setHeader("Cache-Control", "must-revalidate");
        response.setHeader("Pragma", "must-revalidate");
        response.setHeader("Content-Disposition", "attachment;filename="+downloadTitle);
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
		
		model.addAttribute("searchResults", repoUrlUtils.fixRepoUrls(searchResults));
		
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

		SearchResultsDTO<SearchTaxonConceptDTO> searchResults = searchDao.findAllDifferencesInSpeciesByRegionAndHigherTaxon(
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

	/**
	 * @param repoUrlUtils the repoUrlUtils to set
	 */
	public void setRepoUrlUtils(RepoUrlUtils repoUrlUtils) {
		this.repoUrlUtils = repoUrlUtils;
	}
}
