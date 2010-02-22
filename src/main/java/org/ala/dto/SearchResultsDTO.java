/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ala.dto;

import java.util.ArrayList;
import java.util.Collection;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * DTO to represents the results from a Lucene search
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SearchResultsDTO {

    /** Maximum number of results returned from a query */
    private long pageSize = 10;
    /** Current page of results (not currently used) */
    private long startIndex = 0;
    /** Total number of results for the match (indept of resultsPerPage) */
    private long totalRecords = 0;
    /** Field to sort results by */
    private String sort;
    /** Direction to sort results by (asc || desc) */
    private String dir = "asc";
    /** Status code to be set by Controller (e.g. OK) */
    private String status;
    /** List of results from search */
    private Collection<SearchTaxonConceptDTO> taxonConcepts;
    /** List of facet results from search */
    //private Collection<FacetResultDTO> facetResults;
    /** SOLR query response following search */
    //private QueryResponse qr;

    /**
     * Constructor with 2 args
     *
     * @param searchResults
     * @param facetResults
     */
    public SearchResultsDTO(Collection<SearchTaxonConceptDTO> searchResults) {
        this.taxonConcepts = searchResults;
        //this.facetResults = facetResults;
    }

    /**
     * Contructor with no args
     */
    public SearchResultsDTO() {}

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

    public Collection<SearchTaxonConceptDTO> getTaxonConcepts() {
        return taxonConcepts;
    }

    public void setTaxonConcepts(Collection<SearchTaxonConceptDTO> taxonConcepts) {
        this.taxonConcepts = taxonConcepts;
    }

}
