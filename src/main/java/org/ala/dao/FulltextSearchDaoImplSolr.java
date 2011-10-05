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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.ala.dto.SearchCollectionDTO;
import org.ala.dto.SearchDTO;
import org.ala.dto.SearchDataProviderDTO;
import org.ala.dto.SearchDatasetDTO;
import org.ala.dto.SearchInstitutionDTO;
import org.ala.dto.SearchRegionDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.util.ColumnType;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVWriter;
import java.util.regex.Pattern;
import org.ala.dto.AutoCompleteDTO;
import org.ala.dto.SearchWordpressDTO;

/**
 * SOLR implementation of {@see org.ala.dao.FulltextSearchDao}. Used for searching against Lucene
 * indexes created by {@see org.ala.dao.TaxonConceptDaoImpl}.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("fulltextSearchDaoImplSolr")
public class FulltextSearchDaoImplSolr implements FulltextSearchDao {
	
    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(FulltextSearchDaoImplSolr.class);
    /** field name for dataset */
    private static final String DATASET = "dataset";

    @Inject
    protected Vocabulary vocabulary;

    @Inject
    protected SolrUtils solrUtils;
    
    protected int maxResultsForChildConcepts = 5000;
    
    protected int maxDownloadForConcepts = 1000000;

    /**
     * @see org.ala.dao.FulltextSearchDao#getClassificationByLeftNS(int)
     */
	@Override
	public SearchResultsDTO getClassificationByLeftNS(int leftNSValue)
			throws Exception {
        try {
            // set the query
            StringBuffer queryString = new StringBuffer();
            queryString.append("idxtype:"+IndexedTypes.TAXON);
            String[] fq = new String[]{"left:[* TO "+leftNSValue+"]", "right:["+leftNSValue+" TO *]"};
            logger.debug("search query: "+queryString.toString());
            return doSolrSearch(queryString.toString(), fq, 100, 0, "rankId", "asc");
        } catch (SolrServerException ex) {
        	SearchResultsDTO searchResults = new SearchResultsDTO();
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
    }
	
    /**
	 * @see org.ala.dao.FulltextSearchDao#getChildConcepts(int)
	 */
	@Override
	public List<SearchTaxonConceptDTO> getChildConceptsByNS(int leftNS, int rightNS, Integer rankId, int maxResults) throws Exception {
        try {
            // set the query
            StringBuffer queryString = new StringBuffer();
            queryString.append("idxtype:"+IndexedTypes.TAXON);
            if(rankId!=null){
	            queryString.append(" AND ");
	            queryString.append("rankId:"+rankId);
            }
            String[] fq = new String[]{"left:["+leftNS+" TO "+rightNS+"]"};
            logger.debug("search query: "+queryString.toString());
            SearchResultsDTO<SearchTaxonConceptDTO> tcs =  doSolrSearch(queryString.toString(), fq, maxResults, 0, "name", "asc");
            List<SearchTaxonConceptDTO> stds = tcs.getResults();
            Collections.sort(stds);
            return stds;
        } catch (SolrServerException ex) {
        	List<SearchTaxonConceptDTO>  searchResults = new ArrayList<SearchTaxonConceptDTO>();
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return searchResults;
        }
	}
	
	@Override
	public List<SearchTaxonConceptDTO> getChildConceptsByNS(int leftNS, int rightNS, Integer rankId) throws Exception {
		return getChildConceptsByNS(leftNS, rightNS, rankId, maxResultsForChildConcepts);
	}
	
    /**
	 * @see org.ala.dao.FulltextSearchDao#getChildConcepts(int)
	 */
	@Override
	public List<SearchTaxonConceptDTO> getChildConceptsParentId(String parentId) throws Exception {
        try {
            // set the query
            StringBuffer queryString = new StringBuffer();
            queryString.append("idxtype:"+IndexedTypes.TAXON);
            String[] fq = new String[]{"parentId:"+parentId};
            logger.debug("search query: "+queryString.toString());
            SearchResultsDTO<SearchTaxonConceptDTO> tcs =  doSolrSearch(queryString.toString(), fq, maxResultsForChildConcepts, 0, "name", "asc");
            List<SearchTaxonConceptDTO> stds = tcs.getResults();
            Collections.sort(stds);
            return stds;
        } catch (SolrServerException ex) {
        	List<SearchTaxonConceptDTO>  searchResults = new ArrayList<SearchTaxonConceptDTO>();
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return searchResults;
        }
	}

	/**
	 * @see org.ala.dao.FulltextSearchDao#findByScientificName(java.lang.String, int)
	 */
    @Override
    public List<SearchTaxonConceptDTO> findByScientificName(String input, int limit) throws Exception {
        SearchResultsDTO<SearchTaxonConceptDTO> sr = findByScientificName(input, null, 0, limit, "score", "asc");
        return sr.getResults();
    }

    /**
	 * @see org.ala.dao.FulltextSearchDao#findByScientificName(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
    @Override
    public SearchResultsDTO<SearchTaxonConceptDTO> findByScientificName(String query, String[] filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
        
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
                queryString.append("idxtype:"+IndexedTypes.TAXON);
                queryString.append(" AND (");
                queryString.append("scientificNameText:"+cleanQuery);
                queryString.append(" OR commonName:"+cleanQuery);
                queryString.append(" OR guid:"+cleanQuery);
                
        		String canonicalSciName = retrieveCanonicalForm(query);
                if(canonicalSciName!=null){
    	            queryString.append(" OR ");
    	            queryString.append(" text:"+canonicalSciName);
                }
                
//                queryString.append(" OR simpleText:"+cleanQuery);  //commented out for now as this gives confusing results to users
                queryString.append(")");
            }
            logger.info("search query: "+queryString.toString());
            return doSolrSearch(queryString.toString(), filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
        	SearchResultsDTO searchResults = new SearchResultsDTO();
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
    }
    
    /**
     * @see org.ala.dao.FulltextSearchDao#findAllRegionsByType(java.lang.String)
     */
    @Override
	public SearchResultsDTO<SearchRegionDTO> findAllRegionsByType(RegionTypes regionType) throws Exception {
        try {
        	StringBuffer queryString = new StringBuffer();
            queryString.append("idxtype:"+IndexedTypes.REGION);
            String[] fq = new String[]{"regionTypeId:["+regionType.getLowerId() +" TO "+regionType.getHigherId()+"]"};
//            String[] fq = new String[]{};
            logger.debug("search query: "+queryString.toString());
            return doSolrSearch(queryString.toString(), fq, 1000, 0, "name", "asc");
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
    		SearchResultsDTO searchResults = new SearchResultsDTO();
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
	}

	/**
	 * @see org.ala.dao.FulltextSearchDao#findByName(java.lang.String, int)
	 */
	@Override
	public SearchResultsDTO<SearchDTO> findByName(IndexedTypes indexType, String query, String[] filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
        
        try {
        	StringBuffer queryString = new StringBuffer();
            String cleanQuery = ClientUtils.escapeQueryChars(query).toLowerCase();
            queryString.append("idxtype:"+indexType);
            query = StringUtils.trimToNull(query);
            if(query!=null){
            	cleanQuery = ClientUtils.escapeQueryChars(query).toLowerCase();
	            queryString.append(" AND ");
	            queryString.append(" (");
	            queryString.append(" commonName:"+cleanQuery);
	            queryString.append(" OR ");
	            queryString.append(" text:"+cleanQuery);
	            queryString.append(" OR ");
	            queryString.append(" scientificNameText:"+cleanQuery);
	            
	    		String canonicalSciName = retrieveCanonicalForm(query);
	            if(canonicalSciName!=null){
		            queryString.append(" OR ");
		            queryString.append(" text:"+canonicalSciName);
	            }
	            
	            queryString.append(")");
            }
            logger.debug("search query: "+queryString.toString());
            return doSolrSearch(queryString.toString(), filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
    		SearchResultsDTO searchResults = new SearchResultsDTO();
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
	}
	
	/**
     * @see org.ala.dao.FulltextSearchDao#findByName(java.lang.String, int)
     */
    @Override
    public SearchResultsDTO<SearchDTO> findByUid(IndexedTypes indexType, String query, String[] filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
        
        try {
            StringBuffer queryString = new StringBuffer();
            String cleanQuery = ClientUtils.escapeQueryChars(query).toLowerCase();
            queryString.append("idxtype:"+indexType);
            query = StringUtils.trimToNull(query);
            if(query!=null){
                cleanQuery = ClientUtils.escapeQueryChars(query).toLowerCase();
                queryString.append(" AND ");
                queryString.append(" (");
                queryString.append(" uid:"+cleanQuery);
                queryString.append(")");
            }
            logger.debug("search query: "+queryString.toString());
            return doSolrSearch(queryString.toString(), filterQuery, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            SearchResultsDTO searchResults = new SearchResultsDTO();
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
    }
	
	private String replacePrefix(String field, String query){
		query = query.trim().replaceAll("[()]", ""); //remove '(' and ')'
		query = query.replaceAll("\\s+", " "); //replace multiple spaces to single space
		return " (" +field +":" + query.replaceAll(" ", " AND "+field +":")+")";
	}

	private String buildQuery(String query, boolean textTermOr){
    	StringBuffer queryString = new StringBuffer();
    	if(query!=null && query.length()>0){
            String cleanQuery = query.toLowerCase(); //ClientUtils.escapeQueryChars(query).toLowerCase();
            queryString.append(replacePrefix("commonName", cleanQuery)); //" commonName:"+replacePrefix("commonName",cleanQuery));
            queryString.append(" OR ");
            //text term in OR operator
            if(textTermOr){
            	queryString.append(" text:"+cleanQuery+"");
            }
            else{
            	queryString.append(" text:\""+cleanQuery+"\"");
            }
            queryString.append(" OR ");
            queryString.append(replacePrefix("scientificNameText", cleanQuery));//queryString.append(" scientificNameText:\""+cleanQuery+"\"");
            
            queryString.append(" OR ");	            
            queryString.append(replacePrefix("name_complete", cleanQuery));  //queryString.append(" name_complete:\""+cleanQuery+"\"");
            queryString.append(" OR ");
            queryString.append(" concat_name:\"" + SolrUtils.concatName(cleanQuery) + "\"");
            queryString.append(" OR ");
            queryString.append(replacePrefix("stopped_common_name", cleanQuery)); //" stopped_common_name:\"" + cleanQuery + "\"");
            
            //make the exact matches score higher
            //The boost is 100000000000 because this is how much of a boost is required to make the "Acacia" exact matches appear first.
            //Acacia farnesiana has many terms that match with "Acacia" thus the high level boost required.
            queryString.append(" OR ");
            queryString.append(" exact_text:\"" + cleanQuery + "\"^100000000000");
            
            
    		String canonicalSciName = retrieveCanonicalForm(query);
            if(canonicalSciName!=null){
	            queryString.append(" OR ");
	            queryString.append(" text:\""+canonicalSciName + "\"");
            }
            
            logger.debug("search query: "+queryString.toString());
    	} else {
    		queryString.append("*:*");
    	}
    	return queryString.toString();
	}
		
	public SearchResultsDTO<SearchDTO> doFullTextSearch(String query, String[] filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
		SearchResultsDTO<SearchDTO> dto = null;
		
		//parse scientific name into a canonical form, strip authorships etc
		//construct a searchable form of the sci name
        try {
        	String queryString = buildQuery(query, false);
            dto = doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection);
            if(dto == null || dto.getResults() == null || dto.getResults().size() < 1){
            	queryString = buildQuery(query, true);
            	dto = doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection);
            }
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
    		SearchResultsDTO searchResults = new SearchResultsDTO();
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
        return dto;
	}

	public SearchResultsDTO<SearchDTO> doExactTextSearch(String query, String[] filterQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection) throws Exception {
		SearchResultsDTO<SearchDTO> dto = null;
		
        try {
        	if(query!=null && query.length()>0){
	        	String queryString = " exact_text:\"" + query.toLowerCase().trim() + "\"";
	            dto = doSolrSearch(queryString, filterQuery, pageSize, startIndex, sortField, sortDirection); 
        	}
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
    		SearchResultsDTO searchResults = new SearchResultsDTO();
            searchResults.setStatus("ERROR"); // TODO also set a message field on this bean with the error message(?)
            return searchResults;
        }
        return dto;
	}

	/**
	 * Retrieve a canonical form of the name to search with.
	 * @param query
	 * @return
	 */
	private String retrieveCanonicalForm(String query) {
		NameParser np = new NameParser();
		ParsedName pn = np.parse(query);
		if(pn!=null){
			return pn.canonicalName();
		}
		return null;
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
    private SearchResultsDTO doSolrSearch(String queryString, String filterQuery[], Integer pageSize,
          Integer startIndex, String sortField, String sortDirection) throws Exception {
        SolrQuery solrQuery = initSolrQuery(); // general search settings
        solrQuery.setFields("*","score");
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
    private SearchResultsDTO doSolrQuery(SolrQuery solrQuery, String[] filterQuery, Integer pageSize,
            Integer startIndex, String sortField, String sortDirection) throws Exception {
    	
    	if(logger.isDebugEnabled()){
    		logger.debug(solrQuery.getQuery());
    	}
        
    	SearchResultsDTO searchResults = new SearchResultsDTO();
        
        // set the facet query if set
        if (filterQuery != null) {
            for (String fq : filterQuery) {
                // pull apart fq. E.g. Rank:species and then sanitize the string parts
                // so that special characters are escaped appropriately
                if (fq == null || fq.isEmpty()) continue;
                String[] parts = fq.split(":", 2); // separate query field from query text
                logger.debug("fq split into: "+parts.length+" parts: "+parts[0]+" & "+parts[1]);
                String prefix = null;
                String suffix = null;
                // don't escape range queries
                if (parts[1].contains(" TO ")) {
                    prefix = parts[0];
                    suffix = parts[1];
                } else {
                    prefix = ClientUtils.escapeQueryChars(parts[0]);
                    suffix = ClientUtils.escapeQueryChars(parts[1]);
                }

                solrQuery.addFilterQuery(prefix + ":" + suffix); // solrQuery.addFacetQuery(facetQuery)
                logger.debug("adding filter query: " + prefix + ":" + suffix);
            }
        }

        solrQuery.setRows(pageSize);
        solrQuery.setStart(startIndex);
        solrQuery.setSortField(sortField, ORDER.valueOf(sortDirection));
        
        // do the Solr search
        QueryResponse qr = solrUtils.getSolrServer().query(solrQuery); // can throw exception
        
        //process results
        SolrDocumentList sdl = qr.getResults();
        List<FacetField> facets = qr.getFacetFields();
//        Map<String, Map<String, List<String>>> highlights = qr.getHighlighting();
        List<SearchDTO> results = new ArrayList<SearchDTO>();
        List<FacetResultDTO> facetResults = new ArrayList<FacetResultDTO>();
        searchResults.setTotalRecords(sdl.getNumFound());
        searchResults.setStartIndex(sdl.getStart());
        searchResults.setPageSize(pageSize);
        searchResults.setStatus("OK");
        searchResults.setSort(sortField);
        searchResults.setDir(sortDirection);
        searchResults.setQuery(solrQuery.getQuery());
        // populate SOLR search results
        if (!sdl.isEmpty()) {
            for (SolrDocument doc : sdl) {
            	if(IndexedTypes.TAXON.toString().equalsIgnoreCase((String) doc.getFieldValue("idxtype"))){
                    results.add(createTaxonConceptFromIndex(qr, doc));
            	} else if(IndexedTypes.COLLECTION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
                    results.add(createCollectionFromIndex(qr, doc));
            	} else if(IndexedTypes.INSTITUTION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
                    results.add(createInstitutionFromIndex(qr, doc));
            	} else if(IndexedTypes.DATAPROVIDER.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
            		results.add(createDataProviderFromIndex(qr, doc));
            	} else if(IndexedTypes.DATASET.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
                    results.add(createDatasetFromIndex(qr, doc));
            	} else if(IndexedTypes.REGION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
            		results.add(createRegionFromIndex(qr, doc));
            	} else if(IndexedTypes.WORDPRESS.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
            		results.add(createWordpressFromIndex(qr, doc));
            	}
            }
        } else {
        	logger.debug("No results for query.");
        }
        searchResults.setResults(results);
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
     * @see org.ala.dao.FulltextSearchDao#findAllSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
     */
    public SearchResultsDTO findAllSpeciesByRegionAndHigherTaxon(String regionType, String regionName, 
    		String rank, String higherTaxon, 
    		String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection, boolean withImages) throws Exception {
    	
        List<String> higherTaxa = new ArrayList<String>();
        higherTaxa.add(higherTaxon);
        StringBuffer queryBuffer = constructQueryForRegion(regionType, regionName, rank, higherTaxa, withImages);
        String[] filterQueries =  new String[]{ filterQuery };
        try {
            return doSolrSearch(queryBuffer.toString(), filterQueries, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }
    
    /**
	 * @see org.ala.dao.FulltextSearchDao#findAllDifferencesInSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
	@Override
	public SearchResultsDTO findAllDifferencesInSpeciesByRegionAndHigherTaxon(
			String regionType, String regionName, String altRegionType,
			String altRegionName, String rank, List<String> higherTaxa,
			String filterQuery, Integer startIndex, Integer pageSize,
			String sortField, String sortDirection) throws Exception {
		
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
		sb.append(" AND ");
		sb.append("!"+altRegionType + ":\"" + altRegionName+"\"");
        String[] filterQueries = { filterQuery };

        try {
            return doSolrSearch(sb.toString(), filterQueries, pageSize, startIndex, sortField, sortDirection);
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
    	return findAllSpeciesByRegionAndHigherTaxon(regionType, regionName, rank, higherTaxa, filterQuery, startIndex,pageSize,sortField,sortDirection, false);
    }
    
    public SearchResultsDTO findAllSpeciesByRegionAndHigherTaxon(String regionType, String regionName, 
    		String rank, List<String> higherTaxa, 
    		String filterQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection, boolean withImages) throws Exception {
    	
        try {
            StringBuffer queryBuffer = constructQueryForRegion(regionType, regionName, rank, higherTaxa, withImages);
            String[] filterQueries = new String[]{ filterQuery };
            return doSolrSearch(queryBuffer.toString(), filterQueries, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }

	@Override
	public SearchResultsDTO findAllSpeciesByRegionAndHigherTaxon(
			String regionType, String regionName, String rank,
			String higherTaxon, String filterQuery, Integer startIndex,
			Integer pageSize, String sortField, String sortDirection)
			throws Exception {
		return findAllSpeciesByRegionAndHigherTaxon(
				regionType, regionName, rank,
				higherTaxon, filterQuery, startIndex,
				pageSize, sortField, sortDirection, false);
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
            StringBuffer queryStringBuffer = constructQueryForRegion(regionType, regionName, rank, higherTaxa, false);
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQueryType("standard");
            solrQuery.setRows(maxDownloadForConcepts);
            solrQuery.setQuery(queryStringBuffer.toString());

            int startIndex = 0;
            int pageSize = 1000;
            
            SearchResultsDTO results = doSolrQuery(solrQuery, null, pageSize, startIndex, "score", "asc");
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(output), '\t', '"');
            
            csvWriter.writeNext(new String[]{
            		"GUID",
            		"Scientific name",
            		"Taxon rank",
            		"Common name",
            		"Conservation status in Australia",
            		"Conservation status in ACT",
            		"Conservation status in NSW",
            		"Conservation status in NT",
            		"Conservation status in QLD",
            		"Conservation status in SA",
            		"Conservation status in TAS",
            		"Conservation status in VIC",
            		"Conservation status in WA"
            });
            
            while(results.getResults().size()>0 && resultsCount<=maxDownloadForConcepts){
            	logger.debug("Start index: "+startIndex);
	            List<SearchTaxonConceptDTO> concepts = results.getResults();
	            for(SearchTaxonConceptDTO concept : concepts){
	            	resultsCount++;
	            	String[] record = new String[]{
	            		concept.getGuid(),
	            		concept.getName(),
	            		concept.getRank(),
	            		concept.getCommonNameSingle(),
	            		concept.getConservationStatusAUS(),
	            		concept.getConservationStatusACT(),
	            		concept.getConservationStatusNSW(),
	            		concept.getConservationStatusNT(),
	            		concept.getConservationStatusQLD(),
	            		concept.getConservationStatusSA(),
	            		concept.getConservationStatusTAS(),
	            		concept.getConservationStatusVIC(),
	            		concept.getConservationStatusWA(),
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
		StringBuffer sb = constructQueryForRegion(regionType, regionName, rank, taxa, false);
		return doCountQuery(sb.toString());
	}

	/**
	 * @see org.ala.dao.FulltextSearchDao#countSpeciesByRegionAndHigherTaxon(java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
	public int countSpeciesByRegionAndHigherTaxon(String regionType,
			String regionName, String rank, List<String> higherTaxa)
			throws Exception {
		StringBuffer sb = constructQueryForRegion(regionType, regionName, rank, higherTaxa, false);
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
	private StringBuffer constructQueryForRegion(String regionType, String regionName, String rank, List<String> higherTaxa, boolean hasImages) {
		StringBuffer sb = new StringBuffer();
		sb.append("( rank:species OR rank:subspecies ) ");
		sb.append(" AND ");
		sb.append("(");
		for(int i=0; i< higherTaxa.size(); i++){
			if(i>0){
				sb.append(" OR ");
			}
			sb.append(rank + ":\"" + higherTaxa.get(i)+"\"");
		}
		sb.append(")");
		sb.append(" AND ");
		sb.append(regionType + ":\"" + regionName+"\"");
		if(hasImages){
			sb.append(" AND hasImage:true");
		}
		return sb;
	}
	
	/**
	 * Retrieves a simple count using the supplied query 
	 * 
	 * @param query
	 * @return
	 * @throws SolrServerException
	 */
	private int doCountQuery(String query) throws Exception {
		
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
        
        QueryResponse qr = solrUtils.getSolrServer().query(solrQuery); // can throw exception
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
        String[] filterQueries = {filterQuery};
        
        try {
            return doSolrSearch(queryString, filterQueries, pageSize, startIndex, sortField, sortDirection);
        } catch (SolrServerException ex) {
            logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
            return  new SearchResultsDTO();
        }
    }

    /**
     * @see org.ala.dao.FulltextSearchDao#getAllDatasetCounts()
     */
    @Override
    public Map<String, Long> getAllDatasetCounts() throws Exception {
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
	 * Populate a Collection from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchCollectionDTO createCollectionFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchCollectionDTO collection = new SearchCollectionDTO();
                collection.setScore((Float)doc.getFirstValue("score"));
		collection.setIdxType(IndexedTypes.COLLECTION.toString());
		collection.setGuid((String) doc.getFirstValue("guid"));
		collection.setInstitutionName((String) doc.getFirstValue("institutionName"));
		collection.setName((String) doc.getFirstValue("name"));
		collection.setScore((Float) doc.getFirstValue("score"));
        return collection;
	}
    
    /**
	 * Populate a Collection from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchRegionDTO createRegionFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchRegionDTO region = new SearchRegionDTO();
		region.setScore((Float)doc.getFirstValue("score"));
                region.setIdxType(IndexedTypes.REGION.toString());
		region.setGuid((String) doc.getFirstValue("guid"));
		region.setName((String) doc.getFirstValue("name"));
		region.setRegionTypeName((String) doc.getFirstValue("regionType"));
		region.setScore((Float) doc.getFirstValue("score"));
        return region;
	}
	
    /**
	 * Populate a Collection from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchInstitutionDTO createInstitutionFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchInstitutionDTO institution = new SearchInstitutionDTO();
		institution.setScore((Float)doc.getFirstValue("score"));
                institution.setIdxType(IndexedTypes.INSTITUTION.toString());
		institution.setGuid((String) doc.getFirstValue("guid"));
		institution.setName((String) doc.getFirstValue("name"));
		institution.setScore((Float) doc.getFirstValue("score"));
        return institution;
	}

    /**
	 * Populate a Wordpress from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchWordpressDTO createWordpressFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchWordpressDTO wordpress = new SearchWordpressDTO();
		wordpress.setScore((Float) doc.getFirstValue("score"));
        wordpress.setIdxType(IndexedTypes.WORDPRESS.toString());
        wordpress.setGuid((String) doc.getFirstValue("guid"));
		wordpress.setName((String) doc.getFirstValue("name"));
		wordpress.setScore((Float) doc.getFirstValue("score"));

        // add SOLR generated highlights
        String id = (String) doc.getFirstValue("id");
        if (qr.getHighlighting()!=null && qr.getHighlighting().get(id) != null) {
            logger.debug("Got highlighting (" + id + "): "+qr.getHighlighting().get(id).size());
            Map<String, List<String>> highlightVal = qr.getHighlighting().get(id);
            for (Map.Entry<String, List<String>> entry : highlightVal.entrySet()) {
                List<String> newHls = new ArrayList<String>();

                for (String hl : entry.getValue()) {
                    // Strip leading punctuation twice (which SOLR tends to include)
                    String punctuation = ".,;:)]}>!?%-_";
                    String hl2 = StringUtils.stripStart(hl, punctuation);
                    hl2 = StringUtils.stripStart(hl2, punctuation);
                    newHls.add(hl2.trim() + " ...");
                }

                wordpress.setHighlight(StringUtils.join(newHls, "<br/>"));
            }
        }

        return wordpress;
	}
	
    /**
	 * Populate a Dataset DTO from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchDatasetDTO createDatasetFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchDatasetDTO dataset = new SearchDatasetDTO();
		dataset.setScore((Float)doc.getFirstValue("score"));
                dataset.setIdxType(IndexedTypes.DATASET.toString());
		dataset.setGuid((String) doc.getFirstValue("guid"));
		dataset.setName((String) doc.getFirstValue("name"));
		dataset.setDescription((String) doc.getFirstValue("description"));
		dataset.setDataProviderName((String) doc.getFirstValue("dataProviderName"));
		dataset.setScore((Float) doc.getFirstValue("score"));
        return dataset;
	}
	
    /**
	 * Populate a Collection from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchDataProviderDTO createDataProviderFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchDataProviderDTO provider = new SearchDataProviderDTO();
		provider.setScore((Float)doc.getFirstValue("score"));
                provider.setIdxType(IndexedTypes.DATAPROVIDER.toString());
		provider.setGuid((String) doc.getFirstValue("guid"));
		provider.setName((String) doc.getFirstValue("name"));
		provider.setDescription((String) doc.getFirstValue("description"));
		provider.setScore((Float) doc.getFirstValue("score"));
        return provider;
	}
	
    /**
	 * Populate a TaxonConcept from the data in the lucene index.
	 *
	 * @param doc
	 * @return
	 */
	private SearchTaxonConceptDTO createTaxonConceptFromIndex(QueryResponse qr, SolrDocument doc) {
		SearchTaxonConceptDTO taxonConcept = new SearchTaxonConceptDTO();
		taxonConcept.setScore((Float)doc.getFirstValue("score"));
		taxonConcept.setIdxType(IndexedTypes.TAXON.toString());
		taxonConcept.setGuid((String) doc.getFirstValue("guid"));
		taxonConcept.setParentGuid((String) doc.getFirstValue("parentGuid"));
		taxonConcept.setName((String) doc.getFirstValue("scientificNameRaw"));
		taxonConcept.setAcceptedConceptName((String) doc.getFirstValue("acceptedConceptName"));
		String hasChildrenAsString = (String) doc.getFirstValue("hasChildren");
		taxonConcept.setCommonName((String) doc.getFirstValue("commonNameDisplay"));
		taxonConcept.setCommonNameSingle((String) doc.getFirstValue("commonNameSingle"));
		taxonConcept.setImage((String) doc.getFirstValue("image"));
		taxonConcept.setThumbnail((String) doc.getFirstValue("thumbnail"));
		taxonConcept.setHasChildren(Boolean.parseBoolean(hasChildrenAsString));
        taxonConcept.setScore((Float) doc.getFirstValue("score"));
        taxonConcept.setRank((String) doc.getFirstValue("rank"));
        taxonConcept.setLeft((Integer) doc.getFirstValue("left"));
        taxonConcept.setRight((Integer) doc.getFirstValue("right"));
        taxonConcept.setKingdom((String) doc.getFirstValue("kingdom"));
        taxonConcept.setAuthor((String) doc.getFirstValue("author"));
        taxonConcept.setNameComplete((String) doc.getFirstValue("nameComplete"));
        taxonConcept.setConservationStatusAUS((String) doc.getFirstValue("conservationStatusAUS"));
        taxonConcept.setConservationStatusACT((String) doc.getFirstValue("conservationStatusACT"));
        taxonConcept.setConservationStatusNSW((String) doc.getFirstValue("conservationStatusNSW"));
        taxonConcept.setConservationStatusNT((String) doc.getFirstValue("conservationStatusNT"));
        taxonConcept.setConservationStatusQLD((String) doc.getFirstValue("conservationStatusQLD"));
        taxonConcept.setConservationStatusSA((String) doc.getFirstValue("conservationStatusSA"));
        taxonConcept.setConservationStatusTAS((String) doc.getFirstValue("conservationStatusTAS"));
        taxonConcept.setConservationStatusVIC((String) doc.getFirstValue("conservationStatusVIC"));
        taxonConcept.setConservationStatusWA((String) doc.getFirstValue("conservationStatusWA"));
        taxonConcept.setIsAustralian((String) doc.getFirstValue("australian_s"));
//        taxonConcept.setLinkIdentifier((String) doc.getFirstValue("linkIdentifier"));
		try {
			taxonConcept.setLinkIdentifier(java.net.URLEncoder.encode((String) doc.getFirstValue("linkIdentifier"), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			taxonConcept.setLinkIdentifier((String) doc.getFirstValue("linkIdentifier"));
		}
        
        try {
        	Integer rankId = (Integer) doc.getFirstValue("rankId");
        	if(rankId!=null){
        		taxonConcept.setRankId(rankId);
        	}
        } catch (Exception ex) {
            logger.error("Error parsing rankId: "+ex.getMessage(), ex);
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
        solrQuery.addFacetField("idxtype");
        solrQuery.addFacetField("rank");
        solrQuery.addFacetField("kingdom");
        solrQuery.addFacetField("australian_s");
        //solrQuery.addFacetField("rankId");
        //solrQuery.addFacetField("pestStatus");
//        solrQuery.addFacetField("conservationStatus");
        solrQuery.addFacetField("conservationStatusAUS");
        solrQuery.addFacetField("conservationStatusACT");
        solrQuery.addFacetField("conservationStatusNSW");
        solrQuery.addFacetField("conservationStatusNT");
        solrQuery.addFacetField("conservationStatusQLD");
        solrQuery.addFacetField("conservationStatusSA");
        solrQuery.addFacetField("conservationStatusTAS");
        solrQuery.addFacetField("conservationStatusVIC");
        solrQuery.addFacetField("conservationStatusWA");
        solrQuery.addFacetField("speciesGroup");

        solrQuery.setFacetMinCount(1);
        solrQuery.setRows(10);
        solrQuery.setStart(0);

        //add highlights
        solrQuery.setHighlight(true);
        solrQuery.setHighlightFragsize(80);
        solrQuery.setHighlightSnippets(2);
        solrQuery.setHighlightSimplePre("<strong>");
        solrQuery.setHighlightSimplePost("</strong>");
        solrQuery.add("hl.usePhraseHighlighter", "true");
        solrQuery.addHighlightField("commonName");
        solrQuery.addHighlightField("scientificName");
        solrQuery.addHighlightField("pestStatus");
        solrQuery.addHighlightField("conservationStatus");
        solrQuery.addHighlightField("simpleText");
        solrQuery.addHighlightField("content");

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

	/**
	 * @param solrUtils the solrUtils to set
	 */
	public void setSolrUtils(SolrUtils solrUtils) {
		this.solrUtils = solrUtils;
	}

	public void setMaxResultsForChildConcepts(int maxResultsForChildConcepts) {
		this.maxResultsForChildConcepts = maxResultsForChildConcepts;
	}
     /**
      * Applies a prefix and suffix to higlight the search terms in the
      * supplied list.
      *
      * NC: This is a workaround as I can not get SOLR highlighting to work for partial term matches.
      *
      * @param names
      * @param m
      * @return
      */
     private List<String> getHighlightedNames(List<String> names, java.util.regex.Matcher m, String prefix, String suffix){
    	LinkedHashSet<String> hlnames =null;
    	List<String> lnames =null;
        if(names != null){
            hlnames = new LinkedHashSet<String>();
            for(String name : names){
            	String name1 = SolrUtils.concatName(name.trim());
                m.reset(name1);
                if(m.find()){
                    //insert <b> and </b>at the start and end index
                    name = name.substring(0, m.start()) + prefix + name.substring(m.start(), m.end()) + suffix + name.substring(m.end(), name.length());
                    hlnames.add(name);
                }
            }
            if(!hlnames.isEmpty()){
            	lnames = new ArrayList<String>(hlnames);
            	Collections.sort(lnames);
            }            	
        }        
        return lnames;
    }
    /**
     * Creates an auto complete DTO from the supplied result.
     * @param qr
     * @param doc
     * @param value
     * @return
     */
    private AutoCompleteDTO createAutoCompleteFromIndex(QueryResponse qr, SolrDocument doc, String value1){    	
        AutoCompleteDTO autoDto = new AutoCompleteDTO();
        autoDto.setGuid((String) doc.getFirstValue("guid"));
        autoDto.setName((String) doc.getFirstValue("scientificNameRaw"));
        autoDto.setCommonName((String) doc.getFirstValue("commonNameDisplay"));
        autoDto.setOccurrenceCount((Integer)doc.getFirstValue("occurrenceCount"));
        
        autoDto.setGeoreferencedCount((Integer)doc.getFirstValue("georeferencedCount"));
        autoDto.setRankId((Integer)doc.getFirstValue("rankId"));
        autoDto.setRankString((String)doc.getFirstValue("rank"));
        autoDto.setLeft((Integer)doc.getFirstValue("left"));
        autoDto.setRight((Integer)doc.getFirstValue("right"));
        
        String value = null;
        if(value1 != null){
        	value = SolrUtils.concatName(value1.trim());	
        }
        Pattern p = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(value);
        List<String> matchedNames = new ArrayList<String>(); // temp list to stored matched names

        if(autoDto.getCommonName() != null){
            List<String> commonNames = org.springframework.util.CollectionUtils.arrayToList(((String)doc.get("commonNameDisplay")).split(","));
            autoDto.setCommonNameMatches(getHighlightedNames(commonNames, m, "<b>", "</b>"));
            if (autoDto.getCommonNameMatches() != null) {
                matchedNames.addAll(getHighlightedNames(commonNames, m, "", ""));
            }
        }
//        List<String> scientificNames = org.springframework.util.CollectionUtils.arrayToList(((String)doc.get("scientificNameRaw")).split(","));
//        List<String> scientificNames = org.springframework.util.CollectionUtils.arrayToList(((String)doc.get("nameComplete")).split(","));        
        String[] name1 = new String[0];
        if((String)doc.get("scientificNameRaw") != null){
        	name1 = ((String)doc.get("scientificNameRaw")).split(",");
        }
        String[] name2 = new String[0];
        if((String)doc.get("nameComplete") != null){
        	name2 = ((String)doc.get("nameComplete")).split(",");
        }
        ArrayList<String> scientificNames = new ArrayList<String>();
        for(String name : name1){
        	scientificNames.add(name);
        }
        for(String name : name2){
        	scientificNames.add(name);
        }
        
        autoDto.setScientificNameMatches(getHighlightedNames(scientificNames, m, "<b>", "</b>"));
        if (autoDto.getScientificNameMatches() != null) {
            matchedNames.addAll(getHighlightedNames(scientificNames, m, "", ""));
        }
        
        autoDto.setMatchedNames(matchedNames);
        //get the highlighted values
        //NC TODO get partial term matching highlighting to work - I am not sure on the SOLR configurations that are required
//        if(qr.getHighlighting() != null && qr.getHighlighting().get(autoDto.getGuid()) != null){
//            Map<String, List<String>> fieldHighlights = qr.getHighlighting().get(autoDto.getGuid());
//            //get the scientific names
//            if(fieldHighlights.containsKey("scientificNameHL"))
//                autoDto.setScientificNameMatches(fieldHighlights.get("scientificNameHL"));
//            else if(fieldHighlights.containsKey("commonName"))
//                autoDto.setCommonNameMatches(fieldHighlights.get("commonName"));
//        }
        return autoDto;
    }

    /**
     * @see org.ala.dao.FulltextSearchDao#getAutoCompleteList(String, String[], int)
     */
    public List<AutoCompleteDTO> getAutoCompleteList(String value, IndexedTypes indexType, boolean gsOnly, int maxTerms) {
        StringBuffer queryString = new StringBuffer();

//        String cleanQuery = ClientUtils.escapeQueryChars(value);//.toLowerCase();
//        String cleanQuery = SolrUtils.cleanName(value);
        String cleanQuery = value.toLowerCase();
        cleanQuery = cleanQuery.trim().replaceAll("[()]", ""); //remove '(' and ')'
        cleanQuery = cleanQuery.replaceAll("\\s+", " "); //replace multiple spaces to single space
        
        if (StringUtils.trimToNull(cleanQuery) != null && cleanQuery.length() >= 3) {
            if(indexType != null){
                queryString.append("idxtype:" + indexType);
                
                queryString.append(" AND ");
            }
            if(gsOnly){
                    queryString.append("georeferencedCount:[1 TO *]");
                    queryString.append(" AND ");
                }
            queryString.append("(");
            List<AutoCompleteDTO> items = new ArrayList<AutoCompleteDTO>();
            SolrQuery solrQuery = new SolrQuery();
            queryString.append("auto_text:\"" + cleanQuery + "\"");
            queryString.append(" OR ");
            queryString.append("auto_text_edge:\"" + cleanQuery + "\""); //This field ensures that matches at the start of the term have a higher ranking
            queryString.append(" OR ");
            queryString.append("concat_name:\"" + SolrUtils.concatName(cleanQuery) + "\"");
            queryString.append(" OR ");	            
            queryString.append(replacePrefix("name_complete", cleanQuery)); //queryString.append(" name_complete:\""+cleanQuery+"\"");
            queryString.append(" OR ");
            queryString.append(replacePrefix("stopped_common_name", cleanQuery));
            queryString.append(")");
            solrQuery.setQuery(queryString.toString());
            solrQuery.setQueryType("standard");
            solrQuery.setFields("*", "score");
            solrQuery.setRows(maxTerms);

            QueryResponse qr;
			try {
				qr = solrUtils.getSolrServer().query(solrQuery);
				
	            SolrDocumentList sdl = qr.getResults();
	            if (sdl != null && !sdl.isEmpty()) {
	                for (SolrDocument doc : sdl) {
	                    if (IndexedTypes.TAXON.toString().equalsIgnoreCase((String) doc.getFieldValue("idxtype"))) {
	                        items.add(createAutoCompleteFromIndex(qr, doc, value));
	
	                    }
//                else if(IndexedTypes.COLLECTION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
//                    results.add(createCollectionFromIndex(qr, doc));
//            	} else if(IndexedTypes.INSTITUTION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
//                    results.add(createInstitutionFromIndex(qr, doc));
//            	} else if(IndexedTypes.DATAPROVIDER.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
//            		results.add(createDataProviderFromIndex(qr, doc));
//            	} else if(IndexedTypes.DATASET.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
//                    results.add(createDatasetFromIndex(qr, doc));
//            	} else if(IndexedTypes.REGION.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
//            		results.add(createRegionFromIndex(qr, doc));
//            	} else if(IndexedTypes.LOCALITY.toString().equalsIgnoreCase((String)doc.getFieldValue("idxtype"))){
////                    results.add(createTaxonConceptFromIndex(qr, doc));
//            	}
	                }
	            }
            } catch (Exception e) {
				logger.error(e);
			}
            return items;
        }
        return new ArrayList<AutoCompleteDTO>();
    }
    
    /*
     * if guid is null then return all guid been updated by userId.
     * 
     * @return collection of FacetResultDTO
     */
	public Collection getRankingFacetByUserIdAndGuid(String userId, String guid) throws Exception {
		String key = null;
		String sortField = null;
		try {
			String[] fq = new String[]{};
			SolrQuery solrQuery = new SolrQuery();
	        solrQuery.setQueryType("standard");	        
	        solrQuery.setRows(0);
	        solrQuery.setFacet(true);
	        solrQuery.setFacetMinCount(1);
	        solrQuery.setFacetLimit(-1);  // unlimited = -1
		    
			if(guid == null || guid.length() < 1 || "*".equals(guid)){
				key = "*";
		        solrQuery.addFacetField("guid");
		        sortField = "guid";
			}
			else{
				key = ClientUtils.escapeQueryChars(guid);
				sortField = "superColumnName";
			}
	        solrQuery.addFacetField("superColumnName");
			solrQuery.setQuery("idxtype:" + IndexedTypes.RANKING + " AND userId:" + userId + " AND guid:" + key);
			
			SearchResultsDTO qr = doSolrQuery(solrQuery, fq, 100, 0, sortField, "asc");
		    if(qr == null || qr.getFacetResults() == null){
		    	return new ArrayList();
		    }
		    return qr.getFacetResults();
		} catch (SolrServerException ex) {
		    logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
		    return new ArrayList();
		}
	}    	
	
	/*
     * @return collection of FacetResultDTO
     */
	public Collection getUserIdFacetByGuid(String guid) throws Exception {
		String key = null;

		try {
			String[] fq = new String[]{};
			SolrQuery solrQuery = new SolrQuery();
	        solrQuery.setQueryType("standard");	        
	        solrQuery.setRows(0);
	        solrQuery.setFacet(true);
	        solrQuery.setFacetMinCount(1);
	        solrQuery.setFacetLimit(-1);  // unlimited = -1
		    
			if(guid == null || guid.length() < 1 || "*".equals(guid)){
				logger.info("Invalid guid: " + guid);
				return new ArrayList();
			}
			else{
				key = ClientUtils.escapeQueryChars(guid);
			}
	        solrQuery.addFacetField("userId");
			solrQuery.setQuery("idxtype:" + IndexedTypes.RANKING + " AND guid:" + key);
			
		    SearchResultsDTO qr = doSolrQuery(solrQuery, fq, 100, 0, "userId", "asc");
		    if(qr == null || qr.getFacetResults() == null){
		    	return new ArrayList();
		    }
		    return qr.getFacetResults();
		} catch (SolrServerException ex) {
		    logger.error("Problem communicating with SOLR server. " + ex.getMessage(), ex);
		    return new ArrayList();
		}
	}  	
}
