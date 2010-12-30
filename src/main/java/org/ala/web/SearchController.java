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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dto.AutoCompleteDTO;
import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Search controller intended to provide the front door for the BIE.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Controller("searchController")
public class SearchController {

	private final static Logger logger = Logger.getLogger(SearchController.class);
	
	/** DAO bean for SOLR search queries */
	@Inject
	private FulltextSearchDao searchDao;
	
	@Inject
	protected RepoUrlUtils repoUrlUtils;
	
	/** Name of view for list of taxa */
	private final String SEARCH_LIST = "search/list"; // "species/list" || "search/species"
	private final String SEARCH = "search"; //default view when empty query submitted
    private final String AUTO_JSON = "search/autoJson";
    /** WordPress SOLR URI */
    private final String WP_SOLR_URL = "http://alaprodweb1-cbr.vm.csiro.au/solr/select/?wt=json&q=";
	
	/**
	 * Performs a search across all objects, and selects to show the view for the closest match.
	 * 
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/search*", method = RequestMethod.GET)
	public String search(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		
		if (StringUtils.isEmpty(query)) {
			return SEARCH;
		}
		
		//search across the board, select tab with highest score - with a facet on other types
		//if no results for species - pick another tab
		//initial across the board search
		//with facets on TAXON, REGION, DATASET, DATAPROVIDER, COLLECTION, INSTITUTION
		
        // if params are set but empty (e.g. foo=&bar=) then provide sensible defaults
        if (filterQuery != null && filterQuery.length == 0) {
            filterQuery = null;
        } else if (filterQuery == null) {
            // catch search with no fq param and default to "Recorded in Australia"
            return "redirect:/search?q=" + query + "&fq=australian_s:recorded";
        }
        if (startIndex == null) {
            startIndex = 0;
        }
        if (pageSize == null) {
            pageSize = 20;
        }
        if (sortField.isEmpty()) {
            sortField = "score";
        }
        if (sortDirection.isEmpty()) {
            sortDirection = "asc";
        }

        //reverse the sort direction for the "score" field a normal sort should be descending while a reverse sort should be ascending
        sortDirection = getSortDirection(sortField, sortDirection);
                
		String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
		model.addAttribute("query", query);
		model.addAttribute("queryJsEscaped", queryJsEscaped);
		model.addAttribute("title", StringEscapeUtils.escapeJavaScript(title));
		
		logger.debug("Initial query = "+query);
		SearchResultsDTO<SearchDTO> searchResults = searchDao.doFullTextSearch(query, filterQuery, startIndex, pageSize, sortField, sortDirection);
        repoUrlUtils.fixRepoUrls(searchResults);
        model.addAttribute("facetMap", addFacetMap(filterQuery));
        
		//get facets - and counts to model for each idx type
		Collection<FacetResultDTO> facetResults = searchResults.getFacetResults();
		Iterator<FacetResultDTO> facetIter = facetResults.iterator();
		while(facetIter.hasNext()){
			FacetResultDTO facetResultDTO = facetIter.next();
			if("idxtype".equals(facetResultDTO.getFieldName())){
				List<FieldResultDTO> fieldResults = facetResultDTO.getFieldResult();
				for(FieldResultDTO fieldResult: fieldResults){
					model.addAttribute(fieldResult.getLabel(), fieldResult.getCount());
				}
			}
		}
        
		String view = SEARCH_LIST;

        // Site search - get number of hits
        /*
        try {
            String jsonString = getUrlContentAsString(new URI(WP_SOLR_URL + query, false).getEscapedURI());
            JSONObject jsonObj = (JSONObject) JSONValue.parse(jsonString);
            JSONObject responseObj = (JSONObject) jsonObj.get("response");
            Long numFound = (Long) responseObj.get("numFound");
            model.addAttribute("wordpress", numFound);
        } catch (Exception ex) {
            logger.error("Failed to load counts from Wordpress SOLR index: "+ex.getMessage(), ex);
        }
        */
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("totalRecords", searchResults.getTotalRecords());
        model.addAttribute("lastPage", calculateLastPage(searchResults.getTotalRecords(), pageSize));

        logger.debug("Selected view: "+view);
        
		return view;
	}
	
    /**
     * Provides the auto complete service. 
     *
     * @param query The value to auto complete
     * @param geoRefOnly When true only include results that have some geospatial occurrence records
     * @param idxType The index type to limit see bie-hbase/src/main/java/org/ala/dao/IndexedTypes
     * @param maxTerms The maximum number of results to return
     * @param model
     * @throws Exception
     */
    @RequestMapping(value="/search/auto.json*", method = RequestMethod.GET)
    public String searchForAutocompleteValues(
            @RequestParam(value="q", required=true) String query,
            @RequestParam(value="geoOnly", required=false) boolean geoRefOnly,
            @RequestParam(value="idxType", required=false) String idxType,
            @RequestParam(value="limit", required=false, defaultValue ="10") int maxTerms,
            Model model) throws Exception {

        logger.debug("Autocomplete on " + query + " geoOnly: " + geoRefOnly  );
        IndexedTypes it = idxType != null ? IndexedTypes.valueOf(idxType.toUpperCase()):null;
        List<AutoCompleteDTO> autoCompleteList = searchDao.getAutoCompleteList(query,it, geoRefOnly , maxTerms);
        model.addAttribute("autoCompleteList", autoCompleteList);
        return AUTO_JSON;
    }

    /**
     * Changes the direction of the sore if the sortField is score
     * @param sortField
     * @param sortDirection
     * @return
     */
    private String getSortDirection(String sortField, String sortDirection){
        String direction = sortDirection;
        if(sortField.equals("score")){
                if(sortDirection.equals("asc"))
                    direction = "desc";
                else
                    direction = "asc";
            }
        return direction;

    }
	
    /**
     * Create a HashMap for the filter queries
     *
     * @param filterQuery
     * @return
     */
    private HashMap<String, String> addFacetMap(String[] filterQuery) {
               HashMap<String, String> facetMap = new HashMap<String, String>();

        if (filterQuery != null && filterQuery.length > 0) {
            logger.debug("filterQuery = "+StringUtils.join(filterQuery, "|"));
            for (String fq : filterQuery) {
                if (fq != null && !fq.isEmpty()) {
                    String[] fqBits = StringUtils.split(fq, ":", 2);
                    facetMap.put(fqBits[0], fqBits[1]);
                }
            }
        }
        return facetMap;
    }

    /**
	 * Retrieve content as String.
	 *
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String getUrlContentAsString(String url) throws Exception {
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod(url);
		gm.setFollowRedirects(true);
		httpClient.executeMethod(gm);
		// String requestCharset = gm.getRequestCharSet();
		String content = gm.getResponseBodyAsString();
		// content = new String(content.getBytes(requestCharset), "UTF-8");
		return content;
	}
    
     /**
     * Calculate the last page number for pagination
     * 
     * @param totalRecords
     * @param pageSize
     * @return
     */
    private Integer calculateLastPage(Long totalRecords, Integer pageSize) {
        Integer lastPage = 0;
        Integer lastRecordNum = totalRecords.intValue();
        
        if (pageSize > 0) {
            lastPage = (lastRecordNum / pageSize) + ((lastRecordNum % pageSize > 0) ? 1 : 0);
        }
        
        return lastPage;
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
