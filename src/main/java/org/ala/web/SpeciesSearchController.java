package org.ala.web;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.ala.dao.FulltextSearchDao;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchResultsDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Simple controller providing quick search mechanisms before
 * routing off to separate tools.
 * 
 * @author Dave Martin
 */
@Controller("speciesSearchController")
public class SpeciesSearchController {

	private final static Logger logger = Logger.getLogger(SpeciesSearchController.class);
	
	/** Name of view for list of pest/conservation status */
	private final String SHARE_LIST = "share/list";

	/** Name of view for list of pest/conservation status */
	private final String STATUS_CHECK = "status/check";
	
	@Inject
	protected FulltextSearchDao searchDao;
	@Inject
	protected RepoUrlUtils repoUrlUtils;
	
	@RequestMapping(value = "/sds", method = RequestMethod.GET)
	public String sensitiveSpeciesSearch(
        @RequestParam(value="qs", defaultValue ="", required=false) String searchString,
        HttpServletRequest request,
        Model model) throws Exception {
		
		// index search
		
		//cassandra lookup for concept
		
		return STATUS_CHECK;
	}
	
	@RequestMapping(value = "/share/sighting", method = RequestMethod.GET)
	public String speciesSearch(
        @RequestParam(value="qs", defaultValue ="", required=false) String searchString,
		@RequestParam(value="fq", required=false) String[] filterQuery,
		@RequestParam(value="start", required=false, defaultValue="0") Integer startIndex,
		@RequestParam(value="pageSize", required=false, defaultValue ="10") Integer pageSize,
		@RequestParam(value="sort", required=false, defaultValue="score") String sortField,
		@RequestParam(value="dir", required=false, defaultValue ="desc") String sortDirection,
		@RequestParam(value="title", required=false, defaultValue ="Search Results") String title,
        HttpServletRequest request,
        Model model) throws Exception {
		
		if (StringUtils.isEmpty(searchString)) {
			return SHARE_LIST;
		}
		
		// index search
		if(StringUtils.isNotEmpty(searchString)){
			searchString = searchString.trim();
			SearchResultsDTO<SearchDTO> searchResults = searchDao.doFullTextSearch(searchString, filterQuery, startIndex, pageSize, sortField, sortDirection);
			repoUrlUtils.fixRepoUrls(searchResults);
			model.addAttribute("results", searchResults);
			
	        Long totalRecords = searchResults.getTotalRecords();
	        Integer lastPage = (totalRecords.intValue() / pageSize) + 1;
	        
			model.addAttribute("searchResults", searchResults);
	        model.addAttribute("totalRecords", searchResults.getTotalRecords());
	        model.addAttribute("lastPage", lastPage);
			
		}
		return SHARE_LIST;
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
