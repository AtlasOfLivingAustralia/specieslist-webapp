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
package org.ala.web;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.ala.dao.DocumentDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.Document;
import org.ala.repository.Predicates;
import org.ala.util.RepositoryFileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

//import org.ala.bie.web.dao.RepositoryDAO;
//import org.ala.bie.web.dto.ImageDTO;
//import org.ala.bie.web.dto.OrderedDocumentDTO;
//import org.ala.bie.web.dto.OrderedPropertyDTO;
//import org.ala.bie.web.dto.SearchResultDTO;
//import org.ala.bie.web.dto.SolrResultsDTO;
//import org.ala.bie.web.dto.TaxonConceptDTO;
//import org.ala.bie.web.dto.TaxonNameDTO;


/**
 * Main controller for the BIE site
 *
 * If this class gets too big or complex then split into mulitple Controllers.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Controller("taxonConceptController")
public class TaxonConceptController {
    /** DAO bean for data access to repository */
    //private final RepositoryDAO fedoraDAO;
    @Inject
    private final TaxonConceptDao tcDao = null;
    /** DAO bean for access to repostory document table */
    @Inject
    private final DocumentDAO documentDAO = null;
    /** Name of view for site home page */
    private String HOME_PAGE = "homePage";
    /** Name of view for search page */
    private final String SPECIES_SEARCH = "speciesSearchForm";
    /** Name of view for list of taxa */
    private final String SPECIES_LIST = "speciesList";
    /** Name of view for a single taxon */
    private final String SPECIES_SHOW = "speciesShow";
     /** Name of view for a show "error" page */
    private final String SPECIES_ERROR = "speciesError";
    /** Logger initialisation */
    private final static Logger logger = Logger.getLogger(TaxonConceptController.class);

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
		//return HOME_PAGE;
        return "redirect:/index.jsp";
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
            Model model) throws Exception {
        if (query == null) {
            return SPECIES_SEARCH;
        }
        
        String queryJsEscaped = StringEscapeUtils.escapeJavaScript(query);
        model.addAttribute("query", query);
        model.addAttribute("queryJsEscaped", queryJsEscaped);
        String filterQueryChecked = (filterQuery == null) ? "" : filterQuery;
        model.addAttribute("facetQuery", filterQueryChecked);

        SearchResultsDTO searchResults = tcDao.findByScientificName(query, startIndex, pageSize, sortField, sortDirection);
        model.addAttribute("searchResults", searchResults);
        logger.debug("query = "+query);

        if (searchResults.getTaxonConcepts().size() == 1) {
            List taxonConcepts = (List) searchResults.getTaxonConcepts();
            SearchTaxonConceptDTO res = (SearchTaxonConceptDTO) taxonConcepts.get(0);
            String guid = res.getGuid();
            //return "redirect:/species/" + guid;
        }

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
    public String showSpecies(@PathVariable("guid") String guid, Model model) throws Exception {
        String debug = null;

        if (debug!=null) {
            TaxonConceptDao tcDao = new TaxonConceptDao();
            Map<String,String> properties = tcDao.getPropertiesFor(guid);
        } else {
            // hbase lookup for TC guid
            logger.info("Retrieving concept with guid: "+guid);
            model.addAttribute("extendedTaxonConcept", tcDao.getExtendedTaxonConceptByGuid(guid));
        }

        return SPECIES_SHOW;
    }

    /**
     * JSON output for TC guid
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/{guid}.json", method = RequestMethod.GET)
    public ExtendedTaxonConceptDTO showSpeciesJson(@PathVariable("guid") String guid) throws Exception {
        logger.info("Retrieving concept with guid: "+guid);
        //model.addAttribute("extendedTaxonConcept", tcDao.getExtendedTaxonConceptByGuid(guid));
        return tcDao.getExtendedTaxonConceptByGuid(guid);
    }

    /**
     * JSON web service to return details for a repository document
     *
     * @param documentId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/species/document/{documentId}.json", method = RequestMethod.GET)
    public Document getDocumentDetails(@PathVariable("documentId") int documentId) throws Exception {
        Document doc = documentDAO.getById(documentId);

        if (doc != null) {
            // augment data with title from reading dc file
            String fileName = doc.getFilePath()+"/dc";
            RepositoryFileUtils repoUtils = new RepositoryFileUtils();
            List<String[]> lines = repoUtils.readRepositoryFile(fileName);
            //System.err.println("docId:"+documentId+"|filename:"+fileName);
            for (String[] line : lines) {
                // get the dc.title value
                if (line[0].endsWith(Predicates.DC_TITLE.getLocalPart())) {
                    doc.setTitle(line[1]);
                } else if (line[0].endsWith(Predicates.DC_IDENTIFIER.getLocalPart())) {
                    doc.setIdentifier(line[1]);
                }
            }
        }

        return doc;
    }

     /*
     * Getter methods
     */

    public String getHOME_PAGE() {
        return HOME_PAGE;
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
