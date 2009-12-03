/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  TaxaController.java 697 2009-08-01 00:23:45Z oak021 $
 */
package csiro.diasb.controllers;

import csiro.diasb.datamodels.AlaSourcedPropertiesData;
import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.struts2.convention.annotation.*;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.rest.*;

import com.opensymphony.xwork2.*;

import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.fedora.*;
import org.xml.sax.SAXException;

/**
 * Controls the display of TaxonConceptContentModel objects.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.2
 */
@Results({
    @Result(name = "success", type = "redirectAction", params = {"actionName", "taxa"})
})
public class TaxaController extends ActionSupport {

    private static org.apache.log4j.Logger classLogger =
            org.apache.log4j.Logger.getLogger(TaxaController.class);
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
    /**
     * List of all current publications in the Fedora repository (made if no
     * pid is specified on entry)
     */
    private List searchResults;
    //    String propHTMLDataString;
    //RELS-EXT properties
    /**
     * List of properties stored in the RELS-EXT and PropXML datastreams
     * of the current publication object
     */
    private ArrayList<AlaSourcedPropertiesData> objProperties =
            new ArrayList<AlaSourcedPropertiesData>(1);

    private String tcTitle = "";
    
//  private static final Logger classLogger =
    //   Logger.getLogger(TaxaController.class.getName());
    private final boolean DEBUG_MSG = true;

    /**
     * Entry point to the controller from /AlaHarvester/taxa/<pid>
     * It displays a table of Taxon Concept object's properties

     * @return Forwards results to the taxa-show web page
     *
     * @since v0.1
     */
    public HttpHeaders show() {

        // Initialises the required mappings.
        this.init();
        this.pid = this.getId();
        // Obtains the content of the RDF properties of the Fedora Digital object.
        FcGetDsContent fcGetDs = null;
        try {
            fcGetDs = new FcGetDsContent();

        } catch (IOException initFcGetDsErr) {

            classLogger.warn("Errors during instantiation of object to get data stream.", initFcGetDsErr);

            this.addActionError("Errors during instantiation of object to " +
                    "get data stream<br />" +
                    initFcGetDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }

        if (this.DEBUG_MSG) {
            classLogger.info(
                    "Fetching RDF properties datastream of " +
                    "`" + pid + "`" + "\n");
        }

        Collection<String> availableDatastreams = new ArrayList<String>();
        String tcObject = "";
        try {
            availableDatastreams = fcGetDs.listDatastreams(pid);
            classLogger.info("Available datastreams are: "+availableDatastreams);
            for (String ds : availableDatastreams) {
                if (ds.startsWith("ala")) {
                    tcObject = ds;
                    break;
                }
            }
            classLogger.info("ala* datastreams is: "+tcObject);
        } catch (Exception getDsErr) {
            classLogger.warn("Errors during fetching list of datastream.", getDsErr);
        }

        String tcDataString = "";
        List<String> tcTitles = new ArrayList<String>();
        try {
            tcDataString =  fcGetDs.getDsStringContent(pid, tcObject);
            classLogger.info("ala* datastreams contains: "+tcDataString);
            tcTitles = (List) fcGetDs.findDCValues("dcterms:title", tcDataString);
            classLogger.info("dcterms:title contains: "+tcTitles);
            tcTitle = tcTitles.get(0);
        } catch (Exception getDsErr) {
            classLogger.warn("Errors during fetching datastream content as string.", getDsErr);
        }

        //first look for DC properties
        String DCDataString = "";
        try {
            //pid = this.getId();
            DCDataString = fcGetDs.getDsStringContent(pid, "DC");
        } catch (Exception getDsErr) {

            classLogger.warn(
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

        } catch (ParserConfigurationException ex) {
            classLogger.warn(ex);
        } catch (SAXException ex) {
            classLogger.warn(ex);
        } catch (IOException ex) {
            classLogger.warn(ex);
        }
//next look for RELS-EXT properties
        String RELS_EXTDataString = "";
        try {
            RELS_EXTDataString = fcGetDs.getDsStringContent(pid, "RELS-EXT");

        } catch (Exception getDsErr) {

            classLogger.warn(
                    "Errors during fetching of datastream.", getDsErr);

            this.addActionError("Errors during fetching of datastream.<br />" +
                    getDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }

        List<String> hasTaxonNames = new ArrayList<String>();
        try {
            List guids = (List) fcGetDs.findRELS_EXTValues("hasGuid", RELS_EXTDataString);
            hasTaxonNames = (List) fcGetDs.findDCValues("hasTaxonName", RELS_EXTDataString);
            classLogger.info("RELS-EXT hasTaxonName = "+hasTaxonNames.get(0));
            if (guids.size() > 0) {
                guid = (String) guids.get(0);
            }
        } catch (ParserConfigurationException ex) {
            classLogger.warn(ex);
        } catch (SAXException ex) {
            classLogger.warn(ex);
        } catch (IOException ex) {
            classLogger.warn(ex);
        }

        List<String> taxonNamePids = new ArrayList<String>();
        // Lookup associated taxon name objects and retrieve required fields
        for (String taxonNameUrn : hasTaxonNames) {
            ArrayList<SearchResult> sr;
            try {
                sr = (ArrayList<SearchResult>) PseudoRepository.findObjects("identifier", taxonNameUrn, null);
                if (!sr.isEmpty()) {
                    //assume only a single result as we are searching on an identifier URN
                    classLogger.info("first taxonName found: "+sr.get(0).getTitle() + " | PID: "+sr.get(0).getPid());
                    taxonNamePids.add(sr.get(0).getPid());
                }
            } catch (IOException ex) {
                classLogger.warn("Errors returned from Resource Index query" + ex.getMessage());

                this.addActionError("Errors returned from Resource Index query " + ex.getMessage());

                return new DefaultHttpHeaders(ERROR).disableCaching();
            } catch (FedoraException ex) {
                classLogger.warn("Errors returned from Resource Index query" + ex.getMessage());

                this.addActionError("Errors returned from Resource Index query " + ex.getMessage());

                return new DefaultHttpHeaders(ERROR).disableCaching();
            }
        }

        // Add the associated TN objects to the page model
        

        //now look for attributed properties
        String propXMLDataString = "";
        try {
            propXMLDataString = fcGetDs.getDsStringContent(pid, "PropXML");
            //    propHTMLDataString = fcGetDs.getDsStringContent(pid,"PropHTML");
        } catch (Exception getDsErr) {

            classLogger.warn(
                    "Errors during fetching of datastream.", getDsErr);

            this.addActionError("Errors during fetching of datastream.<br />" +
                    getDsErr.toString());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        try {
            objProperties = fcGetDs.findSourcedProperties(RELS_EXTDataString, propXMLDataString);
            //classLogger.info("objProperties.0 = "+objProperties.get(0));
        } catch (ParserConfigurationException ex) {
            classLogger.warn(ex);
        } catch (SAXException ex) {
            classLogger.warn(ex);
        } catch (IOException ex) {
            classLogger.warn(ex);
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

        // Initialises the required mappings.
        this.init();
        ArrayList<SearchResult> sr;
        try {
            sr = (ArrayList<SearchResult>) PseudoRepository.doInitIndex("hasModel", "ala:TaxonConceptContentModel", null);
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query" + ex.getMessage());

            this.addActionError("Errors returned from initialise index query " + ex.getMessage());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        searchResults = sr;

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

    public Collection<AlaSourcedPropertiesData> getObjProperties() {

        // TODO: How to deal with `null` properties?  Throw an exception here?

        return this.objProperties;

    } // End of `TaxaController.getProperties` method.

    private void init() {
    } // End of `initCMIdToUrlMappings` method.
} // End of `TaxaController` class.

