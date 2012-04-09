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
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.model.TaxonConcept;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for serving region pages and views around regions.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Controller("imageSearchController")
public class ImageSearchController {
	
	private final static Logger logger = Logger.getLogger(ImageSearchController.class);
	
	protected Integer maxWidthImages = 170;
	
	@Inject
	FulltextSearchDao searchDao;
	
	@Inject
	RepoUrlUtils repoUrlUtils;
	
	@Inject
	TaxonConceptDao taxonConceptDao;
	
	@RequestMapping("/image-search/showSpecies")
	public String search(
			@RequestParam(value="taxonRank") String taxonRank,
			@RequestParam(value="scientificName") String scientificName,
			@RequestParam(value="fq", required=false) String[] fq,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="sw", required=false, defaultValue="1024") Integer screenWidth,
			Model model) throws Exception {
		
		List<String> filterQueries = new ArrayList<String>();
		filterQueries.add("idxtype:TAXON");
		filterQueries.add("hasImage:true");
        filterQueries.add("rank:species");
        filterQueries.add("australian_s:recorded");

		if(fq!=null && fq.length>0){
			for(String f: fq) { filterQueries.add(f); }
		}

        if("order".equals(taxonRank)){
            taxonRank  = "bioOrder";
        }

		filterQueries.add(taxonRank+":"+scientificName);

        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;

		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
		model.addAttribute("results", repoUrlUtils.fixRepoUrls(results));

		model.addAttribute("noOfColumns", noOfColumns);		
		model.addAttribute("maxWidthImages", maxWidthImages);
        model.addAttribute("pageSize", pageSize);
		return "images/search";
	}

 	@RequestMapping("/image-search/infoBox")
	public String getImageInfoBox(@RequestParam("q") String guid, Model model) throws Exception {
		ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
		model.addAttribute("extendedTaxonConcept",repoUrlUtils.fixRepoUrls(etc));
//		model.addAttribute("spatialPortalMap", PageUtils.getSpatialPortalMap(etc.getTaxonConcept().getGuid()));
		model.addAttribute("commonNames", PageUtils.dedup(etc.getCommonNames()));
		return "images/infoBox";
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

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
	
    /**
     * JSON web service to return a list of img src
     * 
     */
	@RequestMapping("/image-search/showSpecies.json")
	public @ResponseBody SearchResultsDTO<SearchDTO> searchJson(
			@RequestParam(value="taxonRank") String taxonRank,
			@RequestParam(value="scientificName") String scientificName,
			@RequestParam(value="fq", required=false) String[] fq,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="pageSize", required=false, defaultValue="1024") Integer pageSize) throws Exception {
		List<String> filterQueries = new ArrayList<String>();
		filterQueries.add("idxtype:TAXON");
		filterQueries.add("hasImage:true");
        filterQueries.add("rank:species");
        filterQueries.add("australian_s:recorded");

		if(fq!=null && fq.length>0){
			for(String f: fq) { filterQueries.add(f); }
		}

        if("order".equals(taxonRank)){
            taxonRank  = "bioOrder";
        }

		filterQueries.add(taxonRank+":"+scientificName);

//        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
//        Integer pageSize = noOfColumns * 12;

		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
		results = repoUrlUtils.fixRepoUrls(results);
		
		return results;
	}		
 }
