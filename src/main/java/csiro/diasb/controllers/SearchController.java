/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  SearchController.java 697 2009-08-01 00:23:45Z oak021 $
 */
package csiro.diasb.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.opensymphony.xwork2.ActionSupport;

import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.datamodels.SolrResults;
import csiro.diasb.fedora.FacetQuery;
import csiro.diasb.fedora.FedoraException;
import csiro.diasb.fedora.PseudoRepository;
import csiro.diasb.fedora.SolrSearch;

@Results({
    @Result(name = "success", type = "redirectAction", params = {"actionName", "searchSOLR"}),
    @Result(name = "input", type = "redirectAction", params = {"actionName", "searchSOLR"})
})
/**
 * Controls searching the Fedora Repository and displaying the results.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.1
 */
public class SearchController extends ActionSupport {

    // PID of digital object request from URL.
    private String id;
    /**
     * Name of the property to be compared
     * */
    String propertyName;
    /**
     * Property value to search for
     */
    String propertyValue;
    /**
     * Name of content model if you want to restrict the search to a particular
     * type of object (this is not currently used in any search)
     */
    String contentModel;
    QueryResponse qr;
    String responseMessage;

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public QueryResponse getQr() {
        return qr;
    }

    public void setQr(QueryResponse qr) {
        this.qr = qr;
    }
    //The following lists are used to populate list or combo boxes with appropriate
    //property name options
    /**
     * List of standard DC components without the dc: prefix
     */
    List DCStreamFieldList;
    /**
     * List of properties to be searched for, without namespace prefixes. If properties
     * have been mapped to a namespaced string in pseudorepository propToNSPropMap
     * then they will be replaced by the mapped property before being passed to the search module
     * */
    List propertyList;
    /**
     * List of known content models
     */
    List contentModelList;
    /**
     * List of fields that can be searched using the SOLR search module
     */
    List solrFieldList;

    /**
     * Returns the maximum # of pages needed to display all the results for the current search
     * @return
     */
    public long getMaxPages() {
        return (this.getSolrResults().getNResults() - 1) / this.getSolrResults().getResultsPerPage() + 1;
    }

    SolrResults solrResults;
    /**
     * List of query filters (also called facet constraints) that are currently applied to the basic search query
     */
    List facetConstraints;

    public SolrResults getSolrResults() {
        if (solrResults == null) {
            solrResults = new SolrResults();
        }
        return solrResults;
    }

    public void setSolrResults(SolrResults solrResults) {
        this.solrResults = solrResults;
    }
    /**
     * List of results returned from a search without facet information
     */
    List searchResults;
    /**
     * The logger for this class.
     *
     * Using fully qualified name to indicate that this is a Apache Log 4 J
     * Logger, as opposed to JRE's built-in Logger.
     *
     */
    private static final org.apache.log4j.Logger classLogger =
            Logger.getLogger(SearchController.class.getName());
    private final boolean DEBUG_MSG = true;

    /**
     * Displays a form for choosing parameters to be sent to a search engine
     * @return Forwards to the search-setup or search-solr web pages
     */
    public HttpHeaders show() {
        init();
        //first check if this is a faceted-search request
        if (id.startsWith("fq=")) {
            return addConstraint(id);
        }
        if (id.startsWith("rfq=")) {
            return removeConstraint(id);
        }
        if (id.equalsIgnoreCase("RI")) {
            return new DefaultHttpHeaders("setupRI").disableCaching();
        }
        if (id.equalsIgnoreCase("solr")) {
            new DefaultHttpHeaders("SOLR").disableCaching();
        }
        if (id.equalsIgnoreCase("searchRI")) {
            return searchRI();
        }
        if (id.equalsIgnoreCase("searchSOLR")) {
            return searchSOLR();
        }
        if (id.equalsIgnoreCase("removeConstraint")) {
            return removeAllConstraints();
        }
        if (id.equalsIgnoreCase("getNextPage")) {
            return this.getNextPage();
        }
        if (id.equalsIgnoreCase("getPrevPage")) {
            return this.getPrevPage();
        }
        return new DefaultHttpHeaders("SOLR").disableCaching();

    } // End of `SearchController.show` method.

    /**
     * Displays a form for choosing parameters to be sent to a search engine
     * @return Forwards to the default search engine type (the RI search engine)
     */
    public HttpHeaders index() {
        init();
        return new DefaultHttpHeaders("setupRI").disableCaching();
    }

    /**
     * Searches the Fedora repository using the Fedora API (findObjects). Not currently implemented.
     * @return Forwards the results to the search-results web page for display
     * @throws java.io.IOException
     */
    public HttpHeaders searchFields() throws IOException {

        //      ArrayList<SearchResult> sr = (ArrayList<SearchResult>)PseudoRepository.findObjectsByField(propertyValue, (ArrayList<String>) selectedDCFields);
        //       searchResults.setResultList( sr);
        return new DefaultHttpHeaders("results").disableCaching();
    }

    /**
     * Searches the Fedora repository using the Fedora RI search engine
     * @return Forwards the results to the search-results web page for display
     */
    public HttpHeaders searchRI() {
        ArrayList<SearchResult> sr;
        try {
            sr = (ArrayList<SearchResult>) PseudoRepository.findObjects(propertyName, propertyValue, contentModel);
        } catch (IOException ex) {
            classLogger.warn("Errors returned from Resource Index query" + ex.getMessage());

            this.addActionError("Errors returned from Resource Index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        } catch (FedoraException ex) {
            classLogger.warn("Errors returned from Resource Index query" + ex.getMessage());

            this.addActionError("Errors returned from Resource Index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }
        searchResults = sr;
        return new DefaultHttpHeaders("results").disableCaching();
    }

    /**
     * Searches the Fedora repository using the SOLR search engine
     * @return Returns the results to the search-SOLR web page for display
     */
    public HttpHeaders searchSOLR() {

        // Initialises the required mappings.
        this.init();

        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.initFacetedQuery(propertyValue);
            ss.setCurrentFacetQuery(query);
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
            if (solrResults.getSearchResults().isEmpty()) {
                this.setResponseMessage("There are no objects which match your criterion");
            }
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query" + ex.getMessage());

            this.addActionError("Errors returned from initialise index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    /**
     * Adds the selected facet filter (constraint) to the currentFacetQuery
     * and performs a new search
     * @return The updated search-SOLR page
     */
    private HttpHeaders addConstraint(String cStr) {
        try {
            int index = cStr.indexOf(':');
            SolrSearch ss = new SolrSearch();
            String fieldName = cStr.substring(3, index);
            String fieldConstraint = cStr.substring(index + 1).replace('_', '.');
            FacetQuery query = ss.getCurrentFacetQuery();
            query.addFacetConstraint(fieldName, fieldConstraint);
            query.setStart(0);
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
            String s[] = query.getFilterQueries();
            for (String str : s) {
                this.getFacetConstraints().add(str.replace('.', '_'));
            }

            List<FacetField> ffl = qr.getLimitingFacets();
            for (FacetField ff : ffl) {
                String h = ff.getName();
            }

        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query" + ex.getMessage());

            this.addActionError("Errors returned from initialise index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    /**
     * Removes the selected facet filter (constraint) from the currentFacetQuery
     * and performs a new search
     * @return The updated search-SOLR page
     */
    private HttpHeaders removeConstraint(String cStr) {
        try {
            SolrSearch ss = new SolrSearch();
            String fieldName = cStr.substring(4).replace('_', '.');
            FacetQuery query = ss.getCurrentFacetQuery();
            query.removeFacetConstraint(fieldName);
            query.setStart(0);
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
            String s[] = ss.getCurrentFacetQuery().getFilterQueries();
            if (s != null) {
                for (String str : s) {
                    this.getFacetConstraints().add(str.replace('.', '_'));
                }
            }
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query" + ex.getMessage());

            this.addActionError("Errors returned from initialise index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    /**
     * Removes all facet filters (constraints) from the currentFacetQuery
     * and performs a new search
     * @return The updated search-SOLR page
     */
    private HttpHeaders removeAllConstraints() {
        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.getCurrentFacetQuery();
            query.removeAllFacetConstraints();
            query.setStart(0);
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query" + ex.getMessage());

            this.addActionError("Errors returned from initialise index query " + ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    /**
     * Sets the currentFacetQuery to return the next page of results and performs a new search
     * @return The updated result page
     */
    private HttpHeaders getNextPage() {
        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.getCurrentFacetQuery();
            query.setToNextPage();
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
            String s[] = query.getFilterQueries();
            if (s != null) {
                for (String str : s) {
                    this.getFacetConstraints().add(str.replace('.', '_'));
                }
            }
        } catch (Exception ex) {
            addActionError(ex.getMessage());
            return new DefaultHttpHeaders(ERROR).disableCaching();
        }
        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    /**
     * Sets the currentFacetQuery to return the previous page of results and performs a new search
     * @return The updated result page
     */
    private HttpHeaders getPrevPage() {
        SolrSearch ss = new SolrSearch();

        try {
            FacetQuery query = ss.getCurrentFacetQuery();
            query.setToNextPage();
            solrResults = ss.getQueryResults(query);
            qr = solrResults.getQr();
            String s[] = query.getFilterQueries();
            if (s != null) {
                for (String str : s) {
                    this.getFacetConstraints().add(str.replace('.', '_'));
                }
            }
        } catch (Exception ex) {
            addActionError(ex.getMessage());
            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("SOLR").disableCaching();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id != null) {
        }
        this.id = id;
    }

    public List getSolrFieldList() {
        if (solrFieldList == null) {
            solrFieldList = (List) PseudoRepository.getSOLRFields();
        }
        return solrFieldList;
    }

    public void setSolrFieldList(List solrFieldList) {
        this.solrFieldList = solrFieldList;
    }

    public List getDCStreamFieldList() {
        if (DCStreamFieldList == null) {
            DCStreamFieldList = (List) PseudoRepository.getDCStreamFields();
        }
        return DCStreamFieldList;
    }

    public void setDCStreamFieldList(List DCStreamFieldList) {
        this.DCStreamFieldList = DCStreamFieldList;
    }

    public List getSelectedDCFields() {
        return selectedDCFields;
    }

    public void setSelectedDCFields(List selectedDCFields) {
        this.selectedDCFields = selectedDCFields;
    }
    List selectedDCFields;

    public String getContentModel() {
        return contentModel;
    }

    public List getSearchResults() {
        if (searchResults == null) {
            searchResults = new ArrayList<String>();
        }
        return searchResults;
    }

    public void setSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }

    public List getContentModelList() {
        if (contentModelList == null) {
            contentModelList = (List) PseudoRepository.getContentModels();
        }
        return contentModelList;
    }

    public void setContentModelList(List contentModelList) {
        this.contentModelList = contentModelList;
    }

    public List getPropertyList() {
        if (propertyList == null) {
            propertyList = (List) PseudoRepository.getProperties();
        }
        return propertyList;
    }

    public void setPropertyList(List propertyList) {
        this.propertyList = propertyList;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public long getCurrentPage() {
        return solrResults.getStart() / solrResults.getResultsPerPage();
    }

    public List getFacetConstraints() {
        if (facetConstraints == null) {
            facetConstraints = new ArrayList<String>();
        }
        return facetConstraints;
    }

    public void setFacetConstraints(List facetConstraints) {
        this.facetConstraints = facetConstraints;
    }

    /**
     * Initialises lists that can be selected in various search options
     * Most are not currently used at all
     */
    private void init() {
        contentModelList = (List) PseudoRepository.getContentModels();
        propertyList = (List) PseudoRepository.getProperties();
        this.DCStreamFieldList = (List) PseudoRepository.getDCStreamFields();
        selectedDCFields = (List) new ArrayList<String>();
        this.solrFieldList = (List) PseudoRepository.getSOLRFields();

    }

    @Override
    public void validate() {
        if (this.getPropertyName().isEmpty()) {
            addFieldError("propertyName", "SC Select a property name to query");
        }
        if (this.getPropertyValue().isEmpty()) {
            addFieldError("propertyValue", "SC Query is required");
        }
        //These aren't called, as the id is "search"
        if (id.equalsIgnoreCase("searchSOLR")) {
            if (!getSolrFieldList().contains(propertyName)) {
                addFieldError("propertyName", "The field " + propertyName + " is not known to the SOLR implementation");
            }

        } else if (id.equalsIgnoreCase("searchRI")) {
            if (!getPropertyList().contains(propertyName)) {
                addFieldError("propertyName", "The field " + propertyName + " is not known to the RI implementation");
            }
        }
    }
} // End of `SearchController` class.

