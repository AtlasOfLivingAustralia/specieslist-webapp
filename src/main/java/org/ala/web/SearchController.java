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

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dto.SearchResultsDTO;
import org.apache.commons.lang.StringEscapeUtils;
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


		if (query.isEmpty()) {
			return COLLECTIONS_LIST;
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
		//String filterQueryChecked = (filterQuery == null) ? "" : filterQuery;
		//model.addAttribute("facetQuery", filterQueryChecked);
		logger.debug("query = "+query);
		
		SearchResultsDTO searchResults = searchResults = searchDao.findByName(IndexedTypes.COLLECTION, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
		model.addAttribute("searchResults", searchResults);

        Long totalRecords = searchResults.getTotalRecords();
        model.addAttribute("totalRecords", totalRecords);
        Integer lastPage = (totalRecords.intValue() / pageSize) + 1;
        model.addAttribute("lastPage", lastPage);

		return COLLECTIONS_LIST;
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


		if (query.isEmpty()) {
			return INSTITUTIONS_LIST;
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
		//String filterQueryChecked = (filterQuery == null) ? "" : filterQuery;
		//model.addAttribute("facetQuery", filterQueryChecked);
		logger.debug("query = "+query);
		
		SearchResultsDTO searchResults = searchResults = searchDao.findByName(IndexedTypes.INSTITUTION, query, filterQuery, startIndex, pageSize, sortField, sortDirection);
		model.addAttribute("searchResults", searchResults);

        Long totalRecords = searchResults.getTotalRecords();
        model.addAttribute("totalRecords", totalRecords);
        Integer lastPage = (totalRecords.intValue() / pageSize) + 1;
        model.addAttribute("lastPage", lastPage);

		return INSTITUTIONS_LIST;
	}
	
	/**
	 * @param searchDao the searchDao to set
	 */
	public void setSearchDao(FulltextSearchDao searchDao) {
		this.searchDao = searchDao;
	}
}
