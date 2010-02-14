/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.bie.web.dto;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * DTO to represents the results from a SOLR search
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SolrResultsDTO {

    /** Maximum number of results returned from a query */
    private long pageSize = 10;
    /** Current page of results (not currently used) */
    private long startIndex = 0;
    /** Total number of results for the match (indept of resultsPerPage) */
    private long totalRecords;
    /** Field to sort results by */
    private String sort;
    /** Direction to sort results by (asc || desc) */
    private String dir = "asc";
    /** Status code to be set by Controller (e.g. OK) */
    private String status;
    /** List of results from search */
    private Collection<SearchResultDTO> searchResults;
    /** List of facet results from search */
    private Collection<FacetResultDTO> facetResults;
    /** SOLR query response following search */
    private QueryResponse qr;

    /**
     * Constructor with 2 args
     *
     * @param searchResults
     * @param facetResults
     */
    public SolrResultsDTO(Collection<SearchResultDTO> searchResults, Collection<FacetResultDTO> facetResults) {
        this.searchResults = searchResults;
        this.facetResults = facetResults;
    }

    /**
     * Contructor with no args
     */
    public SolrResultsDTO() {}

    /*
     * Getters & Setters
     */
    
    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long start) {
        this.startIndex = start;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
    
    @JsonIgnore
    public long getCurrentPage() {
        return this.startIndex/pageSize;
    }
   
    @JsonIgnore
    public QueryResponse getQr() {
        return qr;
    }

    public void setQr(QueryResponse qr) {
        this.qr = qr;
    }
    
    @JsonIgnore
    public Collection<FacetResultDTO> getFacetResults() {
        if (facetResults==null)
            facetResults = new ArrayList<FacetResultDTO>();
        return facetResults;
    }

    public void setFacetResults(Collection<FacetResultDTO> facetResults) {
        this.facetResults = facetResults;
    }

    public Collection<SearchResultDTO> getSearchResults() {
        if (searchResults==null)
            searchResults = new ArrayList<SearchResultDTO>();
        return searchResults;
    }

    public void setSearchResults(Collection<SearchResultDTO> searchResults) {
        this.searchResults = searchResults;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
