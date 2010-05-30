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
package org.ala.dao;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVWriter;

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
    /** field name for dataset */
    private static final String DATASET = "dataset";
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
        SearchResultsDTO sr = findByScientificName(input, null, 0, limit, "score", "asc");
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

    /**
     * Re-usable method for performing SOLR searches - takes query string input
     *
     * @param queryString
     * @param filterQuery
     * @param pageSize
     * @param startIndex
     * @param sortField
     * @param sortDirection
     * @return
     * @throws SolrServerException
     */
    private SearchResultsDTO doSolrSearch(String queryString, String filterQuery, Integer pageSize,
          Integer startIndex, String sortField, String sortDirection) throws SolrServerException {

        SolrQuery solrQuery = initSolrQuery(); // general search settings
        solrQuery.setQuery(queryString);

        return doSolrQuery(solrQuery, filterQuery, pageSize, startIndex, sortField, sortDirection);
    }

    /**
     * Re-usable method for performing SOLR searches - takes SolrQuery input
     *
     * @param solrQuery
     * @param filterQuery
     * @param pageSize
     * @param startIndex
     * @param sortField
     * @param sortDirection
     * @return
     * @throws SolrServerException
     */
    private SearchResultsDTO doSolrQuery(SolrQuery solrQuery, String filterQuery, Integer pageSize,
            Integer startIndex, String sortField, String sortDirection) throws SolrServerException {
        
        SearchResultsDTO searchResults = new SearchResultsDTO();
        
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
//        Map<String, Map<String, List<String>>> highlights = qr.getHighlighting();
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
                        //String msg = fcount.getName() + ": " + fcount.getCount();
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
     * Find all the taxa for a specific region.
     * 
     * @param regionName
     * @param regionType
     * @param higherTaxon
     * @return
     * @throws Exception
     */
    public SearchResultsDTO findAllSpeciesByRegionAndHigherTaxon(String regionType, String regionName, 
    		String rank, String higherTaxon, 
    		String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {
    	
        List<String> searchTerms = new ArrayList<String>();
        searchTerms.add(rank + ":\"" + higherTaxon+"\"");
        searchTerms.add(regionType + ":\"" + regionName+"\"");
        String queryString = StringUtils.join(searchTerms, " AND ");
        logger.info("search query = "+queryString);

        try {
            return doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }
    
    /**
     * @see org.ala.dao.FulltextSearchDao#findAllSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
     */
    public SearchResultsDTO findAllSpeciesByRegionAndHigherTaxon(String regionType, String regionName, 
    		String rank, List<String> higherTaxa, 
    		String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {
    	
        try {
            StringBuffer queryBuffer = constructQueryForRegion(regionType, regionName, rank, higherTaxa);
            return doSolrSearch(queryBuffer.toString(), filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }

    /**
     * @see org.ala.dao.FulltextSearchDao#writeSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream)
     */
	public int writeSpeciesByRegionAndHigherTaxon(String regionType,
			String regionName, String rank, String higherTaxon, OutputStream output) throws Exception{
		List<String> higherTaxa = new ArrayList<String>();
		higherTaxa.add(higherTaxon);
		return writeSpeciesByRegionAndHigherTaxon(regionType, regionName, rank, higherTaxa, output);
	}
	
    /**
     * @see org.ala.dao.FulltextSearchDao#writeSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream)
     */
	public int writeSpeciesByRegionAndHigherTaxon(String regionType,
			String regionName, String rank, List<String> higherTaxa, OutputStream output)
			throws Exception {
        int resultsCount = 0;
        try {
            StringBuffer queryStringBuffer = constructQueryForRegion(regionType, regionName, rank, higherTaxa);
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQueryType("standard");
            solrQuery.setRows(1000000);
            solrQuery.setQuery(queryStringBuffer.toString());

            int startIndex = 0;
            int pageSize = 1000;
            
            SearchResultsDTO results = doSolrQuery(solrQuery, null, pageSize, startIndex, "score", "asc");
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(output), '\t', '"');
            
            csvWriter.writeNext(new String[]{
            		"GUID",
            		"Scientific name",
            		"Common name",
            		"Conservation status",
            		"Taxon rank",
            });
            
            while(results.getTaxonConcepts().size()>0 && resultsCount<=1000000){
            	logger.debug("Start index: "+startIndex);
	            List<SearchTaxonConceptDTO> concepts = results.getTaxonConcepts();
	            for(SearchTaxonConceptDTO concept : concepts){
	            	resultsCount++;
	            	String[] record = new String[]{
	            		concept.getGuid(),
	            		concept.getNameString(),
	            		concept.getCommonName(),
	            		concept.getConservationStatus(),
	            		concept.getRank(),
	            	};
	            	csvWriter.writeNext(record);
	            	csvWriter.flush();
	            }
	            startIndex +=pageSize;
	            results = doSolrQuery(solrQuery, null, pageSize, startIndex, "score", "asc");
            }
            
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
        }
        return resultsCount;
	}

	/**
     * @see org.ala.dao.FulltextSearchDao#countSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
	public int countSpeciesByRegionAndHigherTaxon(String regionType,
			String regionName, String rank, String higherTaxon)
			throws Exception {
		
		List<String> taxa = new ArrayList<String>(); 
		taxa.add(higherTaxon);
		StringBuffer sb = constructQueryForRegion(regionType, regionName, rank, taxa);
		return doCountQuery(sb.toString());
	}

	/**
	 * @see org.ala.dao.FulltextSearchDao#countSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	public int countSpeciesByRegionAndHigherTaxon(String regionType,
			String regionName, String rank, List<String> higherTaxa)
			throws Exception {
		StringBuffer sb = constructQueryForRegion(regionType, regionName, rank, higherTaxa);
		return doCountQuery(sb.toString());
	}

	/**
	 * Construct a query for species in a given region.
	 * 
	 * @param regionType
	 * @param regionName
	 * @param rank
	 * @param higherTaxa
	 * @return
	 */
	private StringBuffer constructQueryForRegion(String regionType, String regionName,
			String rank, List<String> higherTaxa) {
		StringBuffer sb = new StringBuffer();
		sb.append("( rank:species OR rank:subspecies ) AND ");
		sb.append("(");
		for(int i=0; i< higherTaxa.size(); i++){
			if(i>0){
				sb.append(" OR ");
			}
			sb.append(rank + ":\"" + higherTaxa.get(i)+"\"");
		}
		sb.append(") AND ");
		sb.append(regionType + ":\"" + regionName+"\"");
		return sb;
	}
	
	/**
	 * Retrieves a simple count using the supplied query 
	 * 
	 * @param query
	 * @return
	 * @throws SolrServerException
	 */
	private int doCountQuery(String query) throws SolrServerException {
		
		logger.info("Count query:  "+query);
		
		//do a query to retrieve a count
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");
        solrQuery.setFacet(false);
        solrQuery.setFacetMinCount(0);
        solrQuery.setFacetLimit(10000);
        solrQuery.setRows(0);
        solrQuery.setStart(0);
        solrQuery.setQuery(query);
        
        if (server == null) {
            this.initSolrServer();
        }
        
        QueryResponse qr = server.query(solrQuery); // can throw exception
        SolrDocumentList sdl = qr.getResults();
        return (int) sdl.getNumFound();
	}
    
    /**
     * @see org.ala.dao.FulltextSearchDao#findAllByStatus(org.ala.util.StatusType, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String) 
     */
    @Override
    public SearchResultsDTO findAllByStatus(StatusType statusType, String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {
    	
        //String filterQuery = null; // FIXME add another method parameter for this
        List<String> statusTerms = vocabulary.getTermsForStatusType(statusType);
        List<String> searchTerms = new ArrayList<String>();
        
        for (String st : statusTerms) {
            //searchQuery.add(new TermQuery(new Term(statusType.toString(), st)), BooleanClause.Occur.SHOULD);
            searchTerms.add(statusType.toString() + ":" + st);
        }

        String queryString = StringUtils.join(searchTerms, " OR ");
        logger.info("search query = "+queryString);

        try {
            return doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }

    /**
     * @see org.ala.dao.FulltextSearchDao#getAllDatasetCounts()
     */
    @Override
    public Map<String, Long> getAllDatasetCounts() {
        Map<String, Long> counts = new HashMap<String, Long>();
        SearchResultsDTO searchResults = null;
        String queryString = "*:*";
        
        try {
            SolrQuery solrQuery = initCountsQuery(DATASET);
            solrQuery.setQuery(queryString);
            //searchResults = doSolrSearch(query, null, 0, 1, null, null);
            searchResults = doSolrQuery(solrQuery, null, 0, 1, "score", "asc");
            List<FacetResultDTO> facetResults = (List) searchResults.getFacetResults();

            for (FacetResultDTO fr : facetResults) {
                if (fr.getFieldName().equalsIgnoreCase(DATASET)) {
                    for (FieldResultDTO res : fr.getFieldResult()) {
                        counts.put(res.getLabel(), res.getCount());
                    }
                }
            }
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
        }
        
        return counts;
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
        if (qr.getHighlighting()!=null && qr.getHighlighting().get(taxonConcept.getGuid()) != null) {
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

    /**
     * Helper method to create SolrQuery facets for dataset counts
     *
     * @return solrQuery the SolrQuery
     */
    protected SolrQuery initCountsQuery(String facetField) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");
        solrQuery.setFacet(true);
        solrQuery.addFacetField(facetField);
        solrQuery.setFacetMinCount(0);
        solrQuery.setFacetLimit(10000);
        solrQuery.setRows(1);
        solrQuery.setStart(0);
        return solrQuery;
    }
}
