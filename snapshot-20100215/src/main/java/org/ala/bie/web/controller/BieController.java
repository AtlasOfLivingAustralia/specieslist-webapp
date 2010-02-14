/* *************************************************************************
 *  Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.bie.web.controller;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.ala.bie.web.dao.RepositoryDAO;
import org.ala.bie.web.dto.ImageDTO;
import org.ala.bie.web.dto.OrderedDocumentDTO;
import org.ala.bie.web.dto.OrderedPropertyDTO;
import org.ala.bie.web.dto.SearchResultDTO;
import org.ala.bie.web.dto.SolrResultsDTO;
import org.ala.bie.web.dto.TaxonConceptDTO;
import org.ala.bie.web.dto.TaxonNameDTO;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Main controller for the BIE site
 *
 * If this class gets too big or complex then split into mulitple Controllers.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Controller("bieController")
public class BieController {
    /** DAO bean for data access to repository */
    private final RepositoryDAO fedoraDAO;
    /** Name of view for site home page */
    private String HOME_PAGE = "foo";  // testing injection of view name from properties file using property-override
    /** Name of view for search page */
    private final String SPECIES_SEARCH = "speciesSearchForm";
    /** Name of view for list of taxa */
    private final String SPECIES_LIST = "speciesList";
    /** Name of view for a single taxon */
    private final String SPECIES_SHOW = "speciesShow";
     /** Name of view for a show "error" page */
    private final String SPECIES_ERROR = "speciesError";
    /** Logger initialisation */
    private final static Logger logger = Logger.getLogger(BieController.class);

    /**
     * Contructor to inject DAO instance
     *
     * @param fedoraDAO the FedoraDAO to inject
     */
    @Inject
    public BieController(RepositoryDAO fedoraDAO) {
        this.fedoraDAO = fedoraDAO;
    }

    /**
	 * Custom handler for the welcome view.
	 * <p>
	 * Note that this handler relies on the RequestToViewNameTranslator to
	 * determine the logical view name based on the request URL: "/welcome.do"
	 * -&gt; "welcome".
     *
     * @return viewname to render
     */
	@RequestMapping("/")
	public String homePageHandler() {
		return HOME_PAGE;
	}

    /**
     * Default method for Controller
     *
     * @return mav
     */
    @RequestMapping(value = "/species", method = RequestMethod.GET)
    public ModelAndView listSpecies() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(SPECIES_LIST);
        mav.addObject("message", "Results list for search goes here. (TODO)");
        return mav;
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
     * @param model
     * @return view name
     */
    @RequestMapping(value = "/species/search*", method = RequestMethod.GET)
    public String searchSpecies(
            @RequestParam(value="q", required=false) String query,
            @RequestParam(value="fq", required=false) String filterQuery,
            @RequestParam(value="startIndex", required=false, defaultValue="0") Integer startIndex,
            @RequestParam(value="results", required=false, defaultValue ="10") Integer pageSize,
            @RequestParam(value="sort", required=false, defaultValue="score") String sortField,
            @RequestParam(value="dir", required=false, defaultValue ="asc") String sortDirection,
            Model model) {
        if (query == null) {
            return SPECIES_SEARCH;
        }
        
        String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
        model.addAttribute("query", query);
        model.addAttribute("queryJsEscaped", queryJsEscaped);
        String filterQueryChecked = (filterQuery == null) ? "" : filterQuery;
        model.addAttribute("facetQuery", filterQueryChecked);
        SolrResultsDTO solrResults = fedoraDAO.getFullTextSearchResults(query, filterQueryChecked, startIndex, pageSize, sortField, sortDirection);

        if (solrResults.getSearchResults().size() == 1) {
            // If just one result then rediect to the species page view
            List results = (List) solrResults.getSearchResults();
            SearchResultDTO res = (SearchResultDTO) results.get(0);
            String guid = res.getGuid();
            return "redirect:/species/" + guid;
        }
        
        model.addAttribute("solrResults", solrResults);
        return SPECIES_LIST;
    }

    /**
     * Map to a /{guid} URI.
     * E.g. /species/urn:lsid:biodiversity.org.au:afd.taxon:a402d4c8-db51-4ad9-a72a-0e912ae7bc9a
     * 
     * @param guid
     * @param model
     * @return view name
     */ 
    @RequestMapping(value = "/species/{guid}", method = RequestMethod.GET)
    public String showSpecies(@PathVariable("guid") String guid, Model model) {
        // SOLR server URL lookup via DAO (via bie.properties)
        String solrServerUrl = fedoraDAO.getServerUrl();
        model.addAttribute("solrServerUrl", solrServerUrl);

        // get the taxon concept
        TaxonConceptDTO taxonConcept = fedoraDAO.getTaxonConceptForIdentifier(guid);
        model.addAttribute("taxonConcept", taxonConcept);
        //logger.info("taxonConcept "+ id + " info: " + taxonConcept);
        if (taxonConcept == null) {
            model.addAttribute("errorMessage", "The requested Taxon was not found.");
            return SPECIES_ERROR;
        }
        
        // Get the taxon names
        List<String> taxonNameGuids = new ArrayList<String>();
        taxonNameGuids.add(taxonConcept.getTaxonNameGuid());
        List<TaxonNameDTO> taxonNames = fedoraDAO.getTaxonNamesForUrns(taxonNameGuids);
        model.addAttribute("taxonNames", taxonNames);

        // Get list of images
        List<String> scientificNames = new ArrayList<String>();
        List<ImageDTO> images = null;
        for (TaxonNameDTO tn : taxonNames) {
            scientificNames.add(tn.getNameComplete());
        }
        images = fedoraDAO.getImagesForScientificNames(scientificNames);
        model.addAttribute("images", images);

        // Get the ordered documents & properties 
        List<OrderedDocumentDTO> orderedDocuments  = fedoraDAO.getOrderedDocumentsForName(scientificNames);
        model.addAttribute("orderedDocuments", orderedDocuments);
        List<OrderedPropertyDTO> orderedProperties = fedoraDAO.getOrderedPropertiesForName(scientificNames);
        model.addAttribute("orderedProperties", orderedProperties);

        return SPECIES_SHOW;
    }

       /*
     * Getter methods
     */

    public String getHOME_PAGE() {
        return HOME_PAGE;
    }

    public RepositoryDAO getFedoraDAO() {
        return fedoraDAO;
    }

    public String getSPECIES_LIST() {
        return SPECIES_LIST;
    }

    public String getSPECIES_SEARCH() {
        return SPECIES_SEARCH;
    }

    public String getSPECIES_SHOW() {
        return SPECIES_SHOW;
    }

    public String getSPECIES_ERROR() {
        return SPECIES_ERROR;
    }

    public void setHOME_PAGE(String HOME_PAGE) {
        this.HOME_PAGE = HOME_PAGE;
    }
}
