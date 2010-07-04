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
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
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
	
	/** Name of view for list of taxa */
	private final String COLLECTIONS_LIST = "collections/list";
	private final String INSTITUTIONS_LIST = "institutions/list";
	private final String DATASETS_LIST = "datasets/list";
	private final String DATAPROVIDERS_LIST = "dataproviders/list";
	private final String REGIONS_LIST = "regions/list";
	private final String SPECIES_LIST = "species/list";
	private final String SEARCH = "search"; //default view when empty query submitted
	
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

		String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
		model.addAttribute("query", query);
		model.addAttribute("queryJsEscaped", queryJsEscaped);
		model.addAttribute("title", StringEscapeUtils.escapeJavaScript(title));
		
		logger.debug("Initial query = "+query);
		SearchResultsDTO<SearchDTO> searchResults = searchDao.doFullTextSearch(query, filterQuery, startIndex, pageSize, sortField, sortDirection);
		
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
		
		String view = SPECIES_LIST;
		//what was the top hit?
		if(!searchResults.getResults().isEmpty()){
			
			SearchDTO topHit = searchResults.getResults().get(0);
			logger.debug("Top Hit: "+topHit.getName()+", idxtype: "+topHit.getIdxType());

			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.TAXON.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.TAXON, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = SPECIES_LIST;
			}
			
			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.COLLECTION.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.COLLECTION, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = COLLECTIONS_LIST;
			}

			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.INSTITUTION.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.INSTITUTION, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = INSTITUTIONS_LIST;
			}
			
			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.DATASET.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.DATASET, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = DATASETS_LIST;
			}
			
			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.DATAPROVIDER.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.DATAPROVIDER, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = DATAPROVIDERS_LIST;
			}
			
			//top hit is species, re-run species search to just get species results
			if(IndexedTypes.REGION.toString().equals(topHit.getIdxType())){
				searchResults = searchDao.findByName(IndexedTypes.REGION, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
				view = REGIONS_LIST;
			}
		}

        Long totalRecords = searchResults.getTotalRecords();
        Integer lastPage = (totalRecords.intValue() / pageSize) + 1;
        
		model.addAttribute("searchResults", searchResults);
        model.addAttribute("totalRecords", searchResults.getTotalRecords());
        model.addAttribute("lastPage", lastPage);
        
        System.out.println("Selected view: "+view);
        
		return view;
	}
	
	/**
	 * Map to a /search URI - perform a full-text SOLR search
	 * Note: adding .json to URL will result in JSON output and
	 * adding .xml will result in XML output.
	 *
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return view name
	 * @throws Exception
	 */
	@RequestMapping(value = "/species/search*", method = RequestMethod.GET)
	public String solrSearchSpecies(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {

		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, SPECIES_LIST, IndexedTypes.TAXON);
	}

	
	/**
	 * Map to a /search URI - perform a full-text SOLR search
	 * Note: adding .json to URL will result in JSON output and
	 * adding .xml will result in XML output.
	 *
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return view name
	 * @throws Exception
	 */
	@RequestMapping(value = "/collections/search*", method = RequestMethod.GET)
	public String searchCollections(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, COLLECTIONS_LIST, IndexedTypes.COLLECTION);
	}

	/**
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
	@RequestMapping(value = "/regions/search*", method = RequestMethod.GET)
	public String searchRegions(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, REGIONS_LIST, IndexedTypes.REGION);
	}

	/**
	 * Map to a /search URI - perform a full-text SOLR search
	 * Note: adding .json to URL will result in JSON output and
	 * adding .xml will result in XML output.
	 *
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return view name
	 * @throws Exception
	 */
	@RequestMapping(value = "/datasets/search*", method = RequestMethod.GET)
	public String searchDatasets(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, DATASETS_LIST, IndexedTypes.DATASET);
	}
	
	/**
	 * Map to a /search URI - perform a full-text SOLR search
	 * Note: adding .json to URL will result in JSON output and
	 * adding .xml will result in XML output.
	 *
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return view name
	 * @throws Exception
	 */
	@RequestMapping(value = "/dataproviders/search*", method = RequestMethod.GET)
	public String searchDataProviders(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, DATAPROVIDERS_LIST, IndexedTypes.DATAPROVIDER);
	}
	
	/**
	 * Map to a /search URI - perform a full-text SOLR search
	 * Note: adding .json to URL will result in JSON output and
	 * adding .xml will result in XML output.
	 *
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @return view name
	 * @throws Exception
	 */
	@RequestMapping(value = "/institutions/search*", method = RequestMethod.GET)
	public String searchInstitutions(
			@RequestParam(value="q", required=false) String query,
			@RequestParam(value="fq", required=false) String[] filterQuery,
			@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
			@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
			@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
			@RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
			@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
			Model model) throws Exception {
		return doGenericSearch(query, filterQuery, startIndex, pageSize, sortField,
				sortDirection, title, model, INSTITUTIONS_LIST, IndexedTypes.INSTITUTION);
	}
	
	/**
	 * Perform an generic free text search.
	 * 
	 * @param query
	 * @param filterQuery
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @param title
	 * @param model
	 * @param defaultView
	 * @param indexedType
	 * @return
	 * @throws Exception
	 */
	private String doGenericSearch(String query, 
			String[] filterQuery,
			Integer startIndex, 
			Integer pageSize, 
			String sortField,
			String sortDirection, 
			String title, 
			Model model,
			String defaultView,
			IndexedTypes indexedType) throws Exception {
		
		if (StringUtils.isEmpty(query)) {
			return SEARCH;
		}
        // if params are set but empty (e.g. foo=&bar=) then provide sensible defaults
        if (filterQuery != null && filterQuery.length == 0) {
            filterQuery = null;
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

		String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
		model.addAttribute("query", query);
		model.addAttribute("queryJsEscaped", queryJsEscaped);
		model.addAttribute("title", StringEscapeUtils.escapeJavaScript(title));
		logger.debug("query = "+query);
		
		SearchResultsDTO<SearchDTO> searchResults = searchDao.findByName(indexedType, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
		model.addAttribute("searchResults", searchResults);

        Long totalRecords = searchResults.getTotalRecords();
        model.addAttribute("totalRecords", totalRecords);
        Integer lastPage = (totalRecords.intValue() / pageSize) + 1;
        model.addAttribute("lastPage", lastPage);
        return defaultView;
	}
	
	/**
	 * @param searchDao the searchDao to set
	 */
	public void setSearchDao(FulltextSearchDao searchDao) {
		this.searchDao = searchDao;
	}
}
