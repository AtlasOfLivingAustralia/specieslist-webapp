/* *************************************************************************
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

package org.ala.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.springframework.stereotype.Component;

/**
 * SOLR implementation of {@see org.ala.dao.FulltextSearchDao}. Used for searching against Lucene
 * indexes created by {@see org.ala.dao.TaxonConceptDaoImpl}.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("fulltextSearchDaoImplSolr")
public class FulltextSearchDaoImplSolr implements FulltextSearchDao {//implements TaxonConceptDao {
    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(FulltextSearchDaoImplSolr.class);
    /** SOLR home directory */
    private static final String SOLR_HOME = "/data/solr";
    /** SOLR server instance */
    private EmbeddedSolrServer server = null;
    @Inject
    protected Vocabulary vocabulary;
    
    /**
     * Constructor to set the server field
     */
    public FulltextSearchDaoImplSolr() {
        //initSolrServer();
    }

    /**
     * Initialise the SOLR server instance
     */
    protected void initSolrServer() {
        if (this.server == null & SOLR_HOME != null) {
            try {
                System.setProperty("solr.solr.home", SOLR_HOME);
                CoreContainer.Initializer initializer = new CoreContainer.Initializer();
                CoreContainer coreContainer = initializer.initialize();
                server = new EmbeddedSolrServer(coreContainer, "");
            } catch (Exception ex) {
                logger.error("Error initialising embedded SOLR server: "+ex.getMessage(), ex);
            }
        }
    }

    /**
	 * @see org.ala.dao.FulltextSearchDao#findByScientificName(java.lang.String, int)
	 */
    @Override
    public List<SearchTaxonConceptDTO> findByScientificName(String input, int limit) throws Exception {
        SearchResultsDTO sr = findByScientificName(input, null, 0, limit, null, null);
        return sr.getTaxonConcepts();
    }

    /**
	 * @see org.ala.dao.FulltextSearchDao#findByScientificName(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
    @Override
    public SearchResultsDTO findByScientificName(String query, String filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
        SearchResultsDTO searchResults = new SearchResultsDTO();
        //String filterQuery = null; // FIXME add another method paramater for this

        try {
            
            // set the query
            StringBuffer queryString = new StringBuffer();
            if (query.contains(":") && !query.startsWith("urn")) {
                String[] bits = StringUtils.split(query, ":");
                queryString.append(ClientUtils.escapeQueryChars(bits[0]));
                queryString.append(":");
                queryString.append(ClientUtils.escapeQueryChars(bits[1]).toLowerCase());
            } else {
                String cleanQuery = ClientUtils.escapeQueryChars(query).toLowerCase();
                //queryString.append(ClientUtils.escapeQueryChars(query));
                queryString.append("scientificNameText:"+cleanQuery);
                queryString.append(" OR commonName:"+cleanQuery);
                queryString.append(" OR guid:"+cleanQuery);
                queryString.append(" OR simpleText:"+cleanQuery);
            }
            searchResults = doSolrSearch(queryString.toString(), filterQuery, pageSize, startIndex, sortField, sortDirection);
            logger.info("search query: "+queryString.toString());
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
        }

        return searchResults;
    }

    private SearchResultsDTO doSolrSearch(String queryString, String filterQuery,
            Integer pageSize, Integer startIndex, String sortField, String sortDirection) throws SolrServerException {
        SolrQuery solrQuery = initSolrQuery();
        SearchResultsDTO searchResults = new SearchResultsDTO();
        solrQuery.setQuery(queryString);
        // set the facet query if set
        if (filterQuery != null && !filterQuery.isEmpty()) {
            // pull apart fq. E.g. Rank:species and then sanitize the string parts
            // so that special characters are escaped apporpriately
            String[] parts = filterQuery.split(":");
            String prefix = ClientUtils.escapeQueryChars(parts[0]);
            String suffix = ClientUtils.escapeQueryChars(parts[1]);
            solrQuery.addFilterQuery(prefix + ":" + suffix); // solrQuery.addFacetQuery(facetQuery)
            System.out.println("adding filter query: " + StringUtils.join(solrQuery.getFacetQuery(), ", "));
        }
        solrQuery.setRows(pageSize);
        solrQuery.setStart(startIndex);
        solrQuery.setSortField(sortField, ORDER.valueOf(sortDirection));
        // do the Solr search
        if (server == null) {
            this.initSolrServer();
        }
        QueryResponse qr = server.query(solrQuery); // can throw exception
        SolrDocumentList sdl = qr.getResults();
        List<FacetField> facets = qr.getFacetFields();
        Map<String, Map<String, List<String>>> highlights = qr.getHighlighting();
        List<SearchTaxonConceptDTO> results = new ArrayList<SearchTaxonConceptDTO>();
        List<FacetResultDTO> facetResults = new ArrayList<FacetResultDTO>();
        searchResults.setTotalRecords(sdl.getNumFound());
        searchResults.setStartIndex(sdl.getStart());
        searchResults.setStatus("OK");
        searchResults.setSort(sortField);
        searchResults.setDir(sortDirection);
        searchResults.setQuery(solrQuery.getQuery());
        // populate SOLR search results
        if (!sdl.isEmpty()) {
            for (SolrDocument doc : sdl) {
                results.add(createTaxonConceptFromIndex(qr, doc));
            }
        }
        searchResults.setTaxonConcepts(results);
        // populate SOLR facet results
        if (facets != null) {
            for (FacetField facet : facets) {
                List<FacetField.Count> facetEntries = facet.getValues();
                if ((facetEntries != null) && (facetEntries.size() > 0)) {
                    ArrayList<FieldResultDTO> r = new ArrayList<FieldResultDTO>();
                    for (FacetField.Count fcount : facetEntries) {
                        String msg = fcount.getName() + ": " + fcount.getCount();
                        //logger.trace(fcount.getName() + ": " + fcount.getCount());
                        r.add(new FieldResultDTO(fcount.getName(), fcount.getCount()));
                    }
                    FacetResultDTO fr = new FacetResultDTO(facet.getName(), r);
                    facetResults.add(fr);
                }
            }
        }
        searchResults.setFacetResults(facetResults);
        // The query result is stored in its original format so that all the information
        // returned is available later on if needed
        searchResults.setQr(qr);

        return searchResults;
    }

    /**
     * @see org.ala.dao.FulltextSearchDao#findAllByStatus(org.ala.util.StatusType, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String) 
     */
    @Override
    public SearchResultsDTO findAllByStatus(StatusType statusType, String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {
        SearchResultsDTO searchResults = null;
        //String filterQuery = null; // FIXME add another method paramater for this
        List<String> statusTerms = vocabulary.getTermsForStatusType(statusType);
        List<String> searchTerms = new ArrayList<String>();
        
        for (String st : statusTerms) {
            //searchQuery.add(new TermQuery(new Term(statusType.toString(), st)), BooleanClause.Occur.SHOULD);
            searchTerms.add(statusType.toString() + ":" + st);
        }

        String queryString = StringUtils.join(searchTerms, " OR ");
        logger.info("search query = "+queryString);

        try {
            searchResults = doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
        }

        return searchResults;

    }

    /**
	 * Populate a TaxonConcept from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchTaxonConceptDTO createTaxonConceptFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchTaxonConceptDTO taxonConcept = new SearchTaxonConceptDTO();
		taxonConcept.setGuid((String) doc.getFirstValue("guid"));
		taxonConcept.setParentGuid((String) doc.getFirstValue("parentGuid"));
		taxonConcept.setNameString((String) doc.getFirstValue("scientificNameRaw"));
		taxonConcept.setAcceptedConceptName((String) doc.getFirstValue("acceptedConceptName"));
		String hasChildrenAsString = (String) doc.getFirstValue("hasChildren");
		taxonConcept.setCommonName((String) doc.getFirstValue("commonNameDisplay"));

		taxonConcept.setHasChildren(Boolean.parseBoolean(hasChildrenAsString));
        taxonConcept.setScore((Float) doc.getFirstValue("score"));
        taxonConcept.setRank((String) doc.getFirstValue("rank"));
        try {
            taxonConcept.setRankId(Integer.parseInt((String) doc.getFirstValue("rankId")));
        } catch (NumberFormatException ex) {
            logger.error("Error parsing rankId: "+ex.getMessage());
        }
        taxonConcept.setPestStatus((String) doc.getFirstValue(StatusType.PEST.toString()));
        taxonConcept.setConservationStatus((String) doc.getFirstValue(StatusType.CONSERVATION.toString()));

        // highlights
        if (qr.getHighlighting().get(taxonConcept.getGuid()) != null) {
            //List<String> highlightSnippets = qr.getHighlighting().get(taxonConcept.getGuid()).get("commonName");
            //if (highlightSnippets!=null && !highlightSnippets.isEmpty()) taxonConcept.setHighlight(highlightSnippets.get(1));

            Map<String, List<String>> highlightVal = qr.getHighlighting().get(taxonConcept.getGuid());
            for (Map.Entry<String, List<String>> entry : highlightVal.entrySet()) {
                //System.out.println(taxonConcept.getGuid()+": "+entry.getKey() + " => " + StringUtils.join(entry.getValue(), "|"));
                taxonConcept.setHighlight(StringUtils.join(entry.getValue(), " "));
            }
        }


        return taxonConcept;
	}

    /**
     * Helper method to create SolrQuery object and add facet settings
     *
     * @return solrQuery the SolrQuery
     */
    protected SolrQuery initSolrQuery() {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");
        solrQuery.setFacet(true);
        solrQuery.addFacetField("rank");
        //solrQuery.addFacetField("rankId");
        solrQuery.addFacetField("pestStatus");
        solrQuery.addFacetField("conservationStatus");
        solrQuery.setFacetMinCount(1);
        solrQuery.setRows(10);
        solrQuery.setStart(0);

        //add highlights
        solrQuery.setHighlight(true);
        solrQuery.setHighlightFragsize(40);
        solrQuery.setHighlightSnippets(1);
        solrQuery.setHighlightSimplePre("<b>");
        solrQuery.setHighlightSimplePost("</b>");
        solrQuery.addHighlightField("commonName");
        solrQuery.addHighlightField("scientificName");
        solrQuery.addHighlightField("pestStatus");
        solrQuery.addHighlightField("conservationStatus");
        solrQuery.addHighlightField("simpleText");

        return solrQuery;
    }
}
