/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csiro.diasb.datamodels;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author oak021
 */
public class SolrResults {

    /**
   * Maximum number of results returned from a query
   */
    long resultsPerPage = 10;
    /**
     * Current page of results (not currently used)
    */
    long start=0;
    /**
     * Total # of results for the match (indept of resultsPerPage)
     */
    long nResults;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getNResults() {
        return nResults;
    }

    public void setNResults(long nResults) {
        this.nResults = nResults;
    }

    public long getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(long resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }
    public long getCurrentPage()
    {
        return this.getStart()/getResultsPerPage();
    }
    private Collection<SearchResult> searchResults;
    private Collection<FacetResult> facetResults;

    QueryResponse qr;

    public QueryResponse getQr() {
        return qr;
    }

    public void setQr(QueryResponse qr) {
        this.qr = qr;
    }


    public SolrResults(Collection<SearchResult> searchResults, Collection<FacetResult> facetResults) {
        this.searchResults = searchResults;
        this.facetResults = facetResults;
    }

    public SolrResults() {
    }

    public Collection<FacetResult> getFacetResults() {
        if (facetResults==null)
            facetResults = new ArrayList<FacetResult>();
        return facetResults;
    }

    public void setFacetResults(Collection<FacetResult> facetResults) {
        this.facetResults = facetResults;
    }

    public Collection<SearchResult> getSearchResults() {
        if (searchResults==null)
            searchResults = new ArrayList<SearchResult>();
        return searchResults;
    }

    public void setSearchResults(Collection<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

   
}
