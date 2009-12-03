/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  DatastreamController.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.controllers;

import java.util.ArrayList;
import org.apache.log4j.*;
import java.io.IOException;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.convention.annotation.Result;
import csiro.diasb.fedora.FcGetDsContent;
import com.opensymphony.xwork2.ActionSupport;
import csiro.diasb.datamodels.SolrResults;
import csiro.diasb.fedora.FacetQuery;
import csiro.diasb.fedora.SolrSearch;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;

/**
 * Controls the display of generic digital objects and their datastreams.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.1
 */
@Results({
    @Result(name="success", type="redirectAction", params = {"actionName" , "datastream"})
})
public class DatastreamController extends ActionSupport {

  // PID of digital object request from URL.
  private String id;

  /**
   * Holds the fedora-Repository id for the current object or datastream (if any)
   */
  String pid;
  /**
   * Holds the fedora-Repository datastream id for the current datastream (if any)
   */
  String selectedDSID;
  /**
   * Contains the list of datastreams owned by the current object (if any)
   */
   ArrayList<String> dsID;
   /**
   * List of fields that can be searched using the SOLR search module
   */
    List solrFieldList;
    /**
     * List of faceted search results - included search results which may have highlights
     * and facet field results
     */
    SolrResults solrResults;
    /**
     * List of constraints that have been placed on the current set of results by using a queryFilter
     */
    List facetConstraints;
    /**
     * Current field contstraint when id is fq=foo
     */
    private String fieldConstraint;
    /**
     * Message displayed when the database has been searched - but is empty
     */
    String responseMessage="";

  private static final Logger classLogger =
    Logger.getLogger(DatastreamController.class.getName());

  private final boolean DEBUG_MSG = true;
    
   
  /**
   * Entry point to the controller from /AlaHarvester/datastream/<pid>
   * and /AlaHarvester/datastream/<pid>_<dsid>
   * If no dsID is specified, it displays a list of all datastreams for <Pid>
   * If dsID is specified, the default display is shown

   * @return Forwards results to the datastream-show or datastream-showstream web pages
   * 
   * @since v0.1
   */

	public HttpHeaders show() {

        //first check if this is a faceted-search request
        if (id.startsWith("fq=")) return addConstraint(id);
        if (id.startsWith("rfq=")) return removeConstraint(id);
        if (id.equalsIgnoreCase("removeConstraint")) return removeAllConstraints();
        if (id.equalsIgnoreCase("getNextPage")) return this.getNextPage();
        if (id.equalsIgnoreCase("getPrevPage")) return this.getPrevPage();
        else
        {
        // Initialises the required mappings.
        this.init();

        // Obtains the content of the RDF properties of the Fedora Digital object.
        FcGetDsContent fcGetDs = null;
        try {
          fcGetDs = new FcGetDsContent();

        } catch (IOException initFcGetDsErr) {

          classLogger.warn(
            "Errors during instantiation of object to get data stream.",
            initFcGetDsErr);

          this.addActionError("Errors during instantiation of object to " +
            "get data stream<br />" +
            initFcGetDsErr.toString());

          return new DefaultHttpHeaders("error").disableCaching();
        }

        if (this.DEBUG_MSG) {
          DatastreamController.classLogger.log(Level.INFO,
            "Fetching RDF properties datastream of " +
            "`" + this.getId() + "`" + "\n");
        }

        try {
            //look for a specified datastream Id in the format <dig object id>_dsID
            //if it's not there, show a general dig object page
            String[] s = this.getId().split("_");

            dsID = (ArrayList<String>) fcGetDs.listDatastreams(s[0]);

            //verify that the datastream ID given is valid
            boolean validDSID = false;

            this.setPid(s[0]);
            if (s.length>1)
                for (String st : dsID) if (st.equals(s[1])) validDSID = true;
            if (validDSID) selectedDSID = s[1];
            else return new DefaultHttpHeaders("show").disableCaching();
        } catch (Exception getDsErr) {

          classLogger.warn(
            "Errors during fetching of datastream.", getDsErr);

          this.addActionError("Errors during fetching of datastream.<br />" +
            getDsErr.toString());

          return new DefaultHttpHeaders("error").disableCaching();
        }

        return new DefaultHttpHeaders("showstream").disableCaching();
        }

	} // End of `DatastreamController.show` method.

    /**
     * Entry point to the controller from /AlaHarvester/datastream
     * Searches the repository for all ala objects and returns them for display
     * @return returns results to the datastream-index web page which displays the results
     */
  
    public HttpHeaders index() {

        this.init();

        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.initFacetedQuery("PID", "ala*");
            ss.setCurrentFacetQuery(query);
            solrResults = ss.getQueryResults(query);
            if (solrResults.getSearchResults().isEmpty())
                this.setResponseMessage("There are no objects which match your criterion");
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query"+ex.getMessage(), ex);

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
   }
    /**
     * Adds the selected facet filter (constraint) to the currentFacetQuery
     * and performs a new search
     * @return The updated index page
     */
    private HttpHeaders addConstraint(String cStr) {
        try {
            int index = cStr.indexOf(':');
            SolrSearch ss = new SolrSearch();
            String fieldName = cStr.substring(3, index);
            fieldConstraint = cStr.substring(index+1).replace('_', '.');
            FacetQuery query = ss.getCurrentFacetQuery();
            query.addFacetConstraint(fieldName, fieldConstraint);
            query.setStart(0);
            solrResults = ss.getQueryResults(query);
            String s[] = query.getFilterQueries();
            for (String str : s)
                this.getFacetConstraints().add(str.replace('.', '_'));
        } catch (Exception ex) {
            classLogger.warn("Errors returned from add constraint to query"+ex.getMessage());

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }
    /**
     * Removes the selected facet filter (constraint) from the currentFacetQuery
     * and performs a new search
     * @return The updated index page
     */
    private HttpHeaders removeConstraint(String cStr) {
        try {
            SolrSearch ss = new SolrSearch();
            String fieldName = cStr.substring(4).replace('_','.');
            FacetQuery query = ss.getCurrentFacetQuery();
            query.removeFacetConstraint(fieldName);
            query.setStart(0);
            FacetQuery q = ss.getCurrentFacetQuery();
            solrResults = ss.getQueryResults(query);
            String s[] = query.getFilterQueries();
            if (s!=null) for (String str : s)
                this.getFacetConstraints().add(str.replace('.', '_'));
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query"+ex.getMessage());

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }
    /**
     * Removes all facet filters (constraints) from the currentFacetQuery
     * and performs a new search
     * @return The updated index page
     */
    private HttpHeaders removeAllConstraints() {
        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.getCurrentFacetQuery();
            query.removeAllFacetConstraints();
            query.setStart(0);
            solrResults = ss.getQueryResults(query);
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query"+ex.getMessage());

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }
    /**
     * Sets the currentFacetQuery to return the next page of results and performs a new search
     * @return The updated index page
     */
    private HttpHeaders getNextPage()
    {
        try {
            SolrSearch ss = new SolrSearch();
            FacetQuery query = ss.getCurrentFacetQuery();
            query.setToNextPage();
            solrResults = ss.getQueryResults(query);
            String s[] = query.getFilterQueries();
            if (s != null)for (String str : s)
                this.getFacetConstraints().add(str.replace('.', '_'));
        } catch (Exception ex) {
            addActionError(ex.getMessage());
            return new DefaultHttpHeaders(ERROR).disableCaching();
        }
        return new DefaultHttpHeaders("index").disableCaching();
    }
    /**
     * Sets the currentFacetQuery to return the previous page of results and performs a new search
     * @return The updated index page
     */
    private HttpHeaders getPrevPage()
    {
        SolrSearch ss = new SolrSearch();

        try {
            FacetQuery query = ss.getCurrentFacetQuery();
            query.setToPrevPage();
            solrResults = ss.getQueryResults(query);
            String s[] = query.getFilterQueries();
            if (s != null)for (String str : s)
                this.getFacetConstraints().add(str.replace('.', '_'));
        } catch (Exception ex) {
            addActionError(ex.getMessage());
            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }
/**
 * Returns the page number of the results currently displayed
 * @return
 */
    public long getCurrentPage() {
        return solrResults.getStart()/solrResults.getResultsPerPage();
    }
/**
 * Returns the maximum # of pages needed to display all the results for the current search
 * @return
 */
    public long getMaxPages() {
        return (this.getSolrResults().getNResults()-1)/this.getSolrResults().getResultsPerPage()+1;
    }
     
   public String getId() {
		return id;
	} // End of `DatastreamController.getId` method.

	public void setId(String id) {
		if (id != null) {
		}
		this.id = id;

	} // End of `DatastreamController.setId` method.

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getSelectedDSID() {
        return selectedDSID;
    }

    public void setSelectedDSID(String selectedDSID) {
        this.selectedDSID = selectedDSID;
    }

    public ArrayList<String> getDsID() {
        return dsID;
    }

    public void setDsID(ArrayList<String> dsID) {
        this.dsID = dsID;
    }

     public List getFacetConstraints() {
       if (facetConstraints==null)
            facetConstraints = new ArrayList<FacetField>();
        return facetConstraints;
    }

    public void setFacetConstraints(List facetConstraints) {
        this.facetConstraints = facetConstraints;
    }
       public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
      public SolrResults getSolrResults() {
        if (solrResults==null)
            solrResults = new SolrResults();
        return solrResults;
    }

    public void setSolrResults(SolrResults solrResults) {
        this.solrResults = solrResults;
    }
   private void init() {

   }

    public String getFieldConstraint() {
        return fieldConstraint;
    }

    public void setFieldConstraint(String fieldConstraint) {
        this.fieldConstraint = fieldConstraint;
    }

} // End of `DatastreamController` class.
