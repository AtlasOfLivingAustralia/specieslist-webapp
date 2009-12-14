/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  TaxaController.java 697 2009-08-01 00:23:45Z oak021 $
 */
package csiro.diasb.controllers;

import csiro.diasb.datamodels.SolrResults;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import csiro.diasb.dao.FedoraDAO;
import csiro.diasb.datamodels.ImageDTO;
import csiro.diasb.datamodels.TaxonNameDTO;
import csiro.diasb.fedora.FcGetDsContent;
import csiro.diasb.datamodels.AlaSourcedPropertiesData;
import csiro.diasb.datamodels.HtmlPageDTO;
import csiro.diasb.datamodels.OrderedDocumentDTO;
import csiro.diasb.datamodels.TaxonConceptDTO;
import csiro.diasb.fedora.FacetQuery;
import csiro.diasb.fedora.SolrSearch;
import org.apache.solr.client.solrj.response.FacetField;

/**
 * Controls the display of TaxonConceptContentModel objects.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.2
 */
@Results({
    @Result(name = "success", type = "redirectAction", params = {"actionName", "taxon"})
})
public class SpeciesController extends ActionSupport {

    private static Logger logger = Logger.getLogger(SpeciesController.class);
    // ID of digital object request from URL.
    private String id;
    /**
     * Title of the object, as stored in dc:title
     */
    private String title = "Unknown";
    /**
     * External object GUID
     */
    private String guid = "Unknown";
    /**
     * Fedora repository pid
     */
    private String pid = "Unknown";
    // params for search
    private String propertyValue = "";
    //private String propertyName = "";
    /**
     * List of all current publications in the Fedora repository (made if no
     * pid is specified on entry)
     */
    private List searchResults;
     /**
     * List of constraints that have been placed on the current set of results by using a queryFilter
     */
    List facetConstraints;
    /**
     * Current field contstraint when id is fq=foo
     */
    private String fieldConstraint;
    //    String propHTMLDataString;
    //RELS-EXT properties
    /**
     * List of properties stored in the RELS-EXT and PropXML datastreams
     * of the current publication object
     */
    private ArrayList<AlaSourcedPropertiesData> objProperties =
            new ArrayList<AlaSourcedPropertiesData>(1);

    private String tcTitle = "";

    private TaxonConceptDTO taxonConcept = null;
    private List<TaxonNameDTO> taxonNames = null;
    private List<ImageDTO> images = null;
    private List<HtmlPageDTO> htmlPages = null;
    private List<OrderedDocumentDTO> orderedDocuments = null;
    
    //  private static final Logger classLogger =
    //  Logger.getLogger(TaxaController.class.getName());
    private final boolean DEBUG_MSG = true;

    private FedoraDAO fedoraDAO;
    private SolrResults solrResults;
    private String responseMessage = "";
    private String propertyName = "rdf.hasModel";
    private String solrQuery = "ala_TaxonConceptContentModel";
    private String solrServerUrl = "";
    
    /**
     * Entry point to the controller from /AlaHarvester/taxa/<pid>
     * It displays a table of Taxon Concept object's properties

     * @return Forwards results to the taxa-show web page
     *
     * @since v0.1
     */
    public HttpHeaders show() {
        // Initialises the required mappings.
        // this.init();
        if (id.startsWith("fq=")) 
        	return addConstraint(id);
        if (id.startsWith("search")) 
        	return new DefaultHttpHeaders("show").disableCaching();
        if (id.equalsIgnoreCase("show")) {
            // e.g. /bie/taxon/show?guid=urn:lsid:biodiversity.org.au:afd.taxon:3da1a9b5-92f6-4096-84a6-3c976b06cbd4
            taxonConcept = fedoraDAO.getTaxonConceptForIdentifier(guid);
            pid = taxonConcept.getPid();
            logger.info("ID = " + this.getId() + "; PID = " + pid);
        } else {
            pid = id;
            taxonConcept = fedoraDAO.getTaxonConceptForIdentifier(pid);
        }
        logger.info("TC title = " + taxonConcept.getTitle());
        solrServerUrl = fedoraDAO.getServerUrl();
        // Obtains the content of the RDF properties of the Fedora Digital object.
        FcGetDsContent fcGetDs = null;
        try {
            fcGetDs = new FcGetDsContent();

        } catch (IOException initFcGetDsErr) {

            logger.warn("Errors during instantiation of object to get data stream.", initFcGetDsErr);

            this.addActionError("Errors during instantiation of object to " +
                    "get data stream<br />" +
                    initFcGetDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }

        if (this.DEBUG_MSG) {
            logger.info(
                    "Fetching RDF properties datastream of " +
                    "`" + pid + "`" + "\n");
        }
        // Get list of available data streams
        Collection<String> availableDatastreams = new ArrayList<String>();
        String tcObject = "";
        try {
            availableDatastreams = fcGetDs.listDatastreams(pid);
            logger.info("Available datastreams are: "+availableDatastreams);
            for (String ds : availableDatastreams) {
                if (ds.startsWith("ala")) {
                    tcObject = ds;
                    break;
                }
            }
            logger.info("ala* datastreams is: "+tcObject);
        } catch (Exception getDsErr) {
            logger.warn("Errors during fetching list of datastream.", getDsErr);
        }
        // get the TC data stream as a string
        String tcDataString = "";
        List<String> tcTitles = new ArrayList<String>();
        try {
            tcDataString =  fcGetDs.getDsStringContent(pid, tcObject);
            logger.info("ala* datastreams contains: "+tcDataString);
            tcTitles = (List) fcGetDs.findDCValues("dcterms:title", tcDataString);
            logger.info("dcterms:title contains: "+tcTitles);
            tcTitle = tcTitles.get(0);
        } catch (Exception getDsErr) {
            logger.warn("Errors during fetching datastream content as string.", getDsErr);
        }

        //first look for DC properties
        String DCDataString = "";
        try {
            //pid = this.getId();
            DCDataString = fcGetDs.getDsStringContent(pid, "DC");
        } catch (Exception getDsErr) {

            logger.warn(
                    "Errors during fetching of datastream.", getDsErr);

            this.addActionError("Errors during fetching of datastream.<br />" +
                    getDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        try {
            List titles = (List) fcGetDs.findDCValues("dc:title", DCDataString);
            if (titles.size() > 0) {
                title = (String) titles.get(0);
            }
            //  identifiers = (List) fcGetDs.findDCValues("dc:identifier", DCDataString);

        } catch (Exception ex) {
            logger.warn(ex);
        }
        
        //next look for RELS-EXT properties
        String RELS_EXTDataString = "";
        try {
            RELS_EXTDataString = fcGetDs.getDsStringContent(pid, "RELS-EXT");

        } catch (Exception getDsErr) {

            logger.warn(
                    "Errors during fetching of datastream.", getDsErr);

            this.addActionError("Errors during fetching of datastream.<br />" +
                    getDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        // Get a list of associated taxon names
        List<String> hasTaxonNames = new ArrayList<String>();
        try {
            List guids = (List) fcGetDs.findRELS_EXTValues("hasGuid", RELS_EXTDataString);
            hasTaxonNames = (List) fcGetDs.findDCValues("hasTaxonName", RELS_EXTDataString);
            logger.info("RELS-EXT hasTaxonName = "+hasTaxonNames.get(0));
            if (guids.size() > 0) {
                guid = (String) guids.get(0);
            }
        } catch (ParserConfigurationException ex) {
            logger.warn(ex);
        } catch (SAXException ex) {
            logger.warn(ex);
        } catch (IOException ex) {
            logger.warn(ex);
        }

        taxonNames = fedoraDAO.getTaxonNamesForUrns(hasTaxonNames);
        if (taxonNames.size() > 0) 
        	logger.info("fedoraDAO TN list: "+taxonNames.get(0).toString());
        
        
        // Get the first taxon name and search for other FC objects that reference it
        String scientificName = null;
        List<String> scientificNames = new ArrayList<String>();
        
        if (taxonNames.size() > 0) {
            scientificName = taxonNames.get(0).getNameComplete();

            for (TaxonNameDTO tn : taxonNames) {
                scientificNames.add(tn.getNameComplete());
            }
            images = fedoraDAO.getImagesForScientificNames(scientificNames);
            htmlPages = fedoraDAO.getHtmlPagesForScientificNames(scientificNames);
            logger.info("htmlpage for " + scientificName + " found " + htmlPages.size() + " pages.");
            // TODO references as well
            if (images.size() > 0) logger.info("image 1: "+images.get(0));
        }

        this.orderedDocuments  = fedoraDAO.getOrderedDocumentsForName(scientificNames);
        
        //now look for attributed properties
        String propXMLDataString = "";
        try {
            propXMLDataString = fcGetDs.getDsStringContent(pid, "PropXML");
            //    propHTMLDataString = fcGetDs.getDsStringContent(pid,"PropHTML");
        } catch (Exception getDsErr) {

            logger.warn(
                    "Errors during fetching of datastream.", getDsErr);

            this.addActionError("Errors during fetching of datastream.<br />" +
                    getDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        try {
            objProperties = fcGetDs.findSourcedProperties(RELS_EXTDataString, propXMLDataString);
            //classLogger.info("objProperties.0 = "+objProperties.get(0));
        } catch (ParserConfigurationException ex) {
            logger.warn(ex);
        } catch (SAXException ex) {
            logger.warn(ex);
        } catch (IOException ex) {
            logger.warn(ex);
        }
        
        return new DefaultHttpHeaders("show").disableCaching();
    } // End of `TaxaController.show` method.

    /**
     * Entry point to the controller from /AlaHarvester/taxa/
     * It displays a list of all Taxon Concepts in the repository

     * @return Forwards results to the taxa-index web page
     *
     * @since v0.1
     */
    public HttpHeaders index() {

        try {
            SolrSearch ss = new SolrSearch();
            String queryStr;
            String fieldName;
            if (propertyValue.isEmpty()) {
                // view all
                queryStr = solrQuery;
                fieldName = propertyName;
            } else {
                // search
                queryStr = propertyValue;
                fieldName = "all_text";
            }

            FacetQuery query = ss.initFacetedQuery(fieldName, queryStr);
            // Hack to restrict search results to just TC objects
            StringBuffer queryString = new StringBuffer(query.getQuery());
            queryString.append(" AND rdf.hasModel:ala.TaxonConceptContentModel");
            query.setQuery(queryString.toString());
            ss.setCurrentFacetQuery(query);
            solrResults = ss.getQueryResults(query);
            if (solrResults.getSearchResults().isEmpty())
                responseMessage = "There are no objects which match your criterion";
        } catch (Exception ex) {
            logger.warn("Errors returned from initialise index query"+ex.getMessage(), ex);
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
            logger.warn("Errors returned from add constraint to query"+ex.getMessage());
            this.addActionError("Errors returned from initialise index query "+ex.getMessage());
            return new DefaultHttpHeaders(ERROR).disableCaching();
        }

        return new DefaultHttpHeaders("index").disableCaching();
    }

    public String getId() {
        return id;
    } // End of `TaxaController.getId` method.

    public void setId(String id) {
        this.id = id;

    } // End of `TaxaController.setId` method.

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTcTitle() {
        return tcTitle;
    }

    public void setTcTitle(String tcTitle) {
        this.tcTitle = tcTitle;
    }

    public List<TaxonNameDTO> getTaxonNames() {
        return taxonNames;
    }

    public void setTaxonNames(ArrayList<TaxonNameDTO> taxonNames) {
        this.taxonNames = taxonNames;
    }

    public List<ImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ImageDTO> images) {
        this.images = images;
    }

    public Collection<AlaSourcedPropertiesData> getObjProperties() {
        // TODO: How to deal with `null` properties?  Throw an exception here?
        return this.objProperties;
    }

    public FedoraDAO getFedoraDAO() {
        return fedoraDAO;
    }

    @Inject
    public void setFedoraDAO(FedoraDAO fedoraDAO) {
        this.fedoraDAO = fedoraDAO;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getSolrQuery() {
        return solrQuery;
    }

    public void setSolrQuery(String solrQuery) {
        this.solrQuery = solrQuery;
    }

    public SolrResults getSolrResults() {
        return solrResults;
    }

    public void setSolrResults(SolrResults solrResults) {
        this.solrResults = solrResults;
    }

    public List<HtmlPageDTO> getHtmlPages() {
        return htmlPages;
    }

    public void setHtmlPages(List<HtmlPageDTO> htmlPages) {
        this.htmlPages = htmlPages;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
    
    public List getFacetConstraints() {
       if (facetConstraints==null)
            facetConstraints = new ArrayList<FacetField>();
        return facetConstraints;
    }

    public void setFacetConstraints(List facetConstraints) {
        this.facetConstraints = facetConstraints;
    }

    public TaxonConceptDTO getTaxonConcept() {
        return taxonConcept;
    }

    public void setTaxonConcept(TaxonConceptDTO taxonConcept) {
        this.taxonConcept = taxonConcept;
    }

	/**
	 * @return the orderedDocuments
	 */
	public List<OrderedDocumentDTO> getOrderedDocuments() {
		return orderedDocuments;
	}

	/**
	 * @param orderedDocuments the orderedDocuments to set
	 */
	public void setOrderedDocuments(List<OrderedDocumentDTO> orderedDocuments) {
		this.orderedDocuments = orderedDocuments;
	}

    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    public void setSolrServerUrl(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
    }

}
