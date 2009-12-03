/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package csiro.diasb.controllers;

import com.opensymphony.xwork2.ActionSupport;
import csiro.diasb.datamodels.SolrResults;
import csiro.diasb.fedora.FacetQuery;
import csiro.diasb.fedora.SolrSearch;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.dispatcher.HttpHeaderResult;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

/**
 * Controller that gets called by client via AJAX. Returns JSON data structure.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Results({
    @Result(name="success", type="redirectAction", params = {"actionName" , "ajax"})
})
public class AjaxController extends ActionSupport {
    /** Field to store error messages */
    private String responseMessage;
    /** SOLR results Object */
    private SolrResults solrResults;
    /* Fields mapped to request parameters */
    private String id;
    private String propertyName = "PID";
    private String query = "ala*";
    private String facetQuery = "";
    private String status = "OK";
    /* Request parameters for naviagtion of SOLR results */
    private Integer startIndex = 0;
    private Integer results = 10;  // page size
    private String sort = "";
    private String dir = "asc";
    /** log4j logger */
    private static final Logger logger = Logger.getLogger(AjaxController.class.getName());

    /**
     * Main entry point as defined by rest plugin.
     * 
     * @return String 
     */
    public HttpHeaders index() {
        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery fq = ss.initFacetedQuery(propertyName, query);
            //fq.setStart(Integer.parseInt(startIndex));
            //fq.setRows(Integer.parseInt(results));
            fq.setStart(startIndex);
            fq.setRows(results);
            // set the sort order
            if (!sort.equals("")) fq.setSortField(sort, ORDER.valueOf(dir));
            // add a facet query if param is set
            if (!facetQuery.isEmpty()) {
                int index = facetQuery.indexOf(':');
                String fieldName = facetQuery.substring(0, index);
                String fieldConstraint = facetQuery.substring(index+1).replace('_', '.');
                logger.debug("facetQuery = "+facetQuery);
                logger.debug("fq args: "+fieldName+" | "+fieldConstraint);
                fq.addFacetConstraint(fieldName, fieldConstraint);
            }
            ss.setCurrentFacetQuery(fq);
            solrResults = ss.getQueryResults(fq);
            if (solrResults.getSearchResults().isEmpty())
                this.setResponseMessage("There are no objects which match your criterion");
        } catch (NumberFormatException exn) {
            String msg = "Errors parsing request param/s from String to int. "+exn.getMessage();
            logger.warn(msg, exn);
            this.addActionError(msg);
            this.status = "ERROR";
            //return new DefaultHttpHeaders(ERROR).disableCaching();
        } catch (Exception ex) {
            String msg = "Errors returned from initialise index query "+ex.getMessage();
            logger.warn(msg, ex);
            this.addActionError(msg);
            this.status = "ERROR";
            //return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }
    
    /**
     * Initialise the solrFieldMap map
     */
    private void init() {
        
    }

    /*
     * Getters & Setters
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFacetQuery() {
        return facetQuery;
    }

    public void setFacetQuery(String filterQuery) {
        this.facetQuery = filterQuery;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getResults() {
        return results;
    }

    public void setResults(Integer pageSize) {
        this.results = pageSize;
    }

    public SolrResults getSolrResults() {
        return solrResults;
    }

    public void setSolrResults(SolrResults solrResults) {
        this.solrResults = solrResults;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String sortDirection) {
        this.dir = sortDirection;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Integer start) {
        this.startIndex = start;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
