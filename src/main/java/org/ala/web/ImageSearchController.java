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
import java.util.Map;

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.model.LinnaeanRankClassification;

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

	public SearchResultsDTO<SearchDTO> search(String jsonString, int startIndex, String sortField, String sortDirection, int screenWidth) throws Exception {
        // set the query
		List<String> filterQueries = new ArrayList<String>();
		filterQueries.add("idxtype:TAXON");
		filterQueries.add("hasImage:true");
        filterQueries.add("australian_s:recorded");
        filterQueries.add("rankId:[" + 7000 +" TO * ]");

        StringBuffer sb = new StringBuffer();
    	ObjectMapper om = new ObjectMapper();
        Map map = om.readValue(jsonString, Map.class);
        Object guids = null;
        if(map != null){
        	try{
        		guids = ((Map)((List)map.get("facetResults")).get(0)).get("fieldResult");
        	}
        	catch(Exception e){
        		//do nothing
        		logger.error("do nothing !!!! " + e);
        	}
        	if(guids != null && guids instanceof List && ((List)guids).size() > 0){
        		List<Map<String, String>> list = (List<Map<String, String>>)guids;
        		sb.append(ClientUtils.escapeQueryChars(list.get(0).get("label")));
        		for(int i = 1; i < list.size(); i++){
        			sb.append(" OR " + ClientUtils.escapeQueryChars(list.get(i).get("label")));
        		}
        	}
        }
        if(sb.toString().length() > 0){
        	filterQueries.add("guid:(" + sb.toString() + ")");
        }
        else{
        	return null;
        }
        
        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;

		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
		return repoUrlUtils.fixRepoUrls(results);
	}
	
	public SearchResultsDTO<SearchDTO> search(int leftNSValue, int rightNSValue, int startIndex, String sortField, String sortDirection, int screenWidth) throws Exception {
        // set the query
		List<String> filterQueries = new ArrayList<String>();
		filterQueries.add("idxtype:TAXON");
		filterQueries.add("hasImage:true");
        filterQueries.add("australian_s:recorded");
        // get all species below this ranking (left & right)
        filterQueries.add("left:["+leftNSValue+" TO "+rightNSValue+"]");
        filterQueries.add("rankId:[" + 7000 +" TO * ]");
        
        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;

		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
		return repoUrlUtils.fixRepoUrls(results);
	}

	private Integer[] getLeftRightValue(String kingdom, String taxonRank, String scientificName){
		Integer[] leftRight = new Integer[]{null, null};
		// get value from name matching
		try{
            LinnaeanRankClassification cl = new LinnaeanRankClassification();
            cl.setKingdom(kingdom);
            cl.setScientificName(scientificName);
            NameSearchResult l = taxonConceptDao.findCBDataByName(scientificName, cl, taxonRank);
            if(l != null){
            	leftRight[0] = new Integer(l.getLeft());
            	leftRight[1] = new Integer(l.getRight());
            }		
		}
		catch(Exception ex){
			// do nothing
			logger.error(ex);
		}
		
		// failed (eg: Macropus) -- try get left right value from cassandra
		if(leftRight[0] == null && leftRight[1] == null){
			String lsid = taxonConceptDao.findLsidByName(scientificName, taxonRank);
			if(lsid == null){
	 			lsid = taxonConceptDao.findLsidByName(scientificName);
			}
			// can't find left right values from cassandra then use solr search of species images
	    	if(lsid != null && lsid.length() > 0){
    		// get left right value from cassandra
	        	ExtendedTaxonConceptDTO etc;
				try {
					etc = taxonConceptDao.getExtendedTaxonConceptByGuid(lsid);
		        	if(etc != null && etc.getTaxonConcept() != null){
		        		leftRight[0] = etc.getTaxonConcept().getLeft();
		        		leftRight[1] = etc.getTaxonConcept().getRight();
		        	}
				} 
				catch (Exception e) {
					logger.error(e);
				}
	    	}
		}		
		return leftRight;
	}

	@RequestMapping("/image-search/showBiocacheSpecies")
	public String searchBiocache(
			@RequestParam(value="radius", required=false, defaultValue="5f") Float radius,
            @RequestParam(value="latitude") Float latitude,
            @RequestParam(value="longitude") Float longitude,
            @RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="sw", required=false, defaultValue="1024") Integer screenWidth,
			Model model) throws Exception {
        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;
        String pageBiocacheUrl = "http://biocache.ala.org.au/ws/occurrences/search";
		String pageParameters = "?q=*:* AND (species_guid:[* TO *] OR subspecies_guid:[* TO *])&lat=" + latitude + "&lon=" + longitude + "&radius=" + radius + "&facets=taxon_concept_lsid&pageSize=0&flimit=" + pageSize + "&foffset=" + startIndex;

    	String jsonString = PageUtils.getUrlContentAsJsonString(new URI(pageBiocacheUrl + pageParameters, false).getEscapedURI());
 
		SearchResultsDTO<SearchDTO> results = search(jsonString, 0, sortField, sortDirection, screenWidth);
		
		model.addAttribute("results", results);
		model.addAttribute("noOfColumns", noOfColumns);		
		model.addAttribute("maxWidthImages", maxWidthImages);
        model.addAttribute("pageSize", pageSize);

    	return "images/bioSearch";
	}

	@RequestMapping("/image-search/showSpecies")
	public String search(
			@RequestParam(value="kingdom", required=false) String kingdom,
			@RequestParam(value="taxonRank") String taxonRank,
			@RequestParam(value="scientificName") String scientificName,
			@RequestParam(value="fq", required=false) String[] fq,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="sw", required=false, defaultValue="1024") Integer screenWidth,
			Model model) throws Exception {
		
        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;

		Integer[] leftRight = getLeftRightValue(kingdom, taxonRank, scientificName);
		if(leftRight[0] == null && leftRight[1] == null){
			// can't find left right values from name matching or cassandra then use solr search of species images
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

    		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
    		model.addAttribute("results", repoUrlUtils.fixRepoUrls(results));

	    }
    	else{
     		SearchResultsDTO<SearchDTO> results = search(leftRight[0], leftRight[1], startIndex, sortField, sortDirection, screenWidth);
     		model.addAttribute("results", results);
    	}
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
	public SearchResultsDTO<SearchDTO> searchJson(Integer leftNSValue, Integer rightNSValue, Integer startIndex, 
			String sortField, String sortDirection, Integer pageSize) throws Exception {
		List<String> filterQueries = new ArrayList<String>();
		filterQueries.add("idxtype:TAXON");
		filterQueries.add("hasImage:true");
        filterQueries.add("rank:species");
        filterQueries.add("australian_s:recorded");
        // get all species below this ranking (left & right)
        filterQueries.add("left:["+leftNSValue+" TO "+rightNSValue+"]");
        filterQueries.add("rankId:[" + 7000 +" TO * ]");

		SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
		results = repoUrlUtils.fixRepoUrls(results);
		
		return results;
	}
	
    /**
     * JSON web service to return a list of img src
     * 
     */
	@RequestMapping("/image-search/showSpecies.json")
	public @ResponseBody SearchResultsDTO<SearchDTO> searchJson(
			@RequestParam(value="kingdom", required=false) String kingdom,
			@RequestParam(value="taxonRank") String taxonRank,
			@RequestParam(value="scientificName") String scientificName,
			@RequestParam(value="fq", required=false) String[] fq,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="pageSize", required=false, defaultValue="1024") Integer pageSize) throws Exception {
		Integer[] leftRight = getLeftRightValue(kingdom, taxonRank, scientificName);
		if(leftRight[0] == null && leftRight[1] == null){
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
	
			SearchResultsDTO<SearchDTO> results = searchDao.doFullTextSearch(null, (String[]) filterQueries.toArray(new String[0]), startIndex, pageSize, sortField, sortDirection);
			results = repoUrlUtils.fixRepoUrls(results);
			
			return results;
		}
		else{
			return searchJson(leftRight[0], leftRight[1], startIndex, sortField, sortDirection, pageSize);
		}
	}	
	
	@RequestMapping("/image-search/showBiocacheSpecies.json")
	public @ResponseBody SearchResultsDTO<SearchDTO> searchBiocacheJson(
			@RequestParam(value="radius", required=false, defaultValue="5f") Float radius,
            @RequestParam(value="latitude") Float latitude,
            @RequestParam(value="longitude") Float longitude,
            @RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="sw", required=false, defaultValue="1024") Integer screenWidth,
			Model model) throws Exception {
        Integer noOfColumns = screenWidth / (maxWidthImages + 2);
        Integer pageSize = noOfColumns * 12;
        String pageBiocacheUrl = "http://biocache.ala.org.au/ws/occurrences/search";
		String pageParameters = "?q=*:* AND (species_guid:[* TO *] OR subspecies_guid:[* TO *])&lat=" + latitude + "&lon=" + longitude + "&radius=" + radius + "&facets=taxon_concept_lsid&pageSize=0&flimit=" + pageSize + "&foffset=" + startIndex;

    	String jsonString = PageUtils.getUrlContentAsJsonString(new URI(pageBiocacheUrl + pageParameters, false).getEscapedURI());
 
		SearchResultsDTO<SearchDTO> results = search(jsonString, 0, sortField, sortDirection, screenWidth);

    	return results;
	}	
}
