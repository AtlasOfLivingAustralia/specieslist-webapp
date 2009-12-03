/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  NameController.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.controllers;

import csiro.diasb.datamodels.AlaSourcedPropertiesData;
import java.io.*;
import org.apache.log4j.*;

import javax.xml.parsers.*;

import org.apache.struts2.convention.annotation.*;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.rest.*;
import org.xml.sax.*;

import com.opensymphony.xwork2.*;

import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.fedora.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controls the display of TaxonNameContentModel objects.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.1
 */
@Results({
    @Result(name="success", type="redirectAction", params = {"actionName" , "name"})
})
public class NameController extends ActionSupport {

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

  List identifiers;

  private static final Logger classLogger =
    Logger.getLogger(NameController.class.getName());

  private final boolean DEBUG_MSG = true;

 String propXMLDataString;
 String propHTMLDataString;
 String RELS_EXTDataString;
 String DCDataString;

  /**
   * Entry point to the controller from /AlaHarvester/name/<pid>
   * It displays a table of a Taxon Name object's properties

   * @return Forwards results to the name-show web page
   *
   * @since v0.1
   */
	public HttpHeaders show() {

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
      classLogger.warn(
        "Fetching RDF properties datastream of " +
        "`" + this.getId() + "`" + "\n");
    }

    //String propertiesXml = null;
    //first look for DC properties
    try {
        pid = this.getId();
      this.DCDataString = fcGetDs.getDsStringContent(this.getId(),"DC");
      
    } catch (Exception getDsErr) {

      classLogger.warn(
        "Errors during fetching of datastream.", getDsErr);

      this.addActionError("Errors during fetching of datastream.<br />" +
        getDsErr.toString());

      return new DefaultHttpHeaders("error").disableCaching();
    }
        try {
            List titles = (List) fcGetDs.findDCValues("dc:title", DCDataString);
            if (titles.size()>0) title = (String) titles.get(0);
            identifiers = (List) fcGetDs.findDCValues("dc:identifier", DCDataString);

        } catch (ParserConfigurationException ex) {
            classLogger.warn("ParserConfigurationException thrown while searching DC datastream", ex);
        } catch (SAXException ex) {
            classLogger.warn("SAXException thrown while searching DC datastream", ex);
        } catch (IOException ex) {
            classLogger.warn("IOException thrown while searching DC datastream", ex);
        }
//next look for RELS-EXT properties
    try {
      this.RELS_EXTDataString = fcGetDs.getDsStringContent(this.getId(),"RELS-EXT");

    } catch (Exception getDsErr) {

      classLogger.warn(
        "Errors during fetching of RELS-EXT datastream.", getDsErr);

      this.addActionError("Errors during fetching of RELS-EXT datastream.<br />");

      return new DefaultHttpHeaders("error").disableCaching();
    }
    try {
        List guids = (List) fcGetDs.findRELS_EXTValues("hasGuid", RELS_EXTDataString);
        if (guids.size()>0) guid = (String) guids.get(0);
    } catch (ParserConfigurationException ex) {
        classLogger.warn("ParserConfigurationException thrown while searching RELS_EXT for hasGuid", ex);
    } catch (SAXException ex) {
        classLogger.warn("SAXException thrown while searching RELS_EXT for hasGuid", ex);
    } catch (IOException ex) {
        classLogger.warn("IOException thrown while searching RELS_EXT for hasGuid", ex);
    }
   //now look for attributed properties
    try {
      propXMLDataString = fcGetDs.getDsStringContent(this.getId(),"PropXML");
      //propHTMLDataString = fcGetDs.getDsStringContent(this.getId(),"PropHTML");
   
    } catch (Exception getDsErr) {

      classLogger.warn(
        "Errors during fetching of PropXML datastream.", getDsErr);

      this.addActionError("Errors during fetching of datastream.<br />" +
        getDsErr.toString());

      return new DefaultHttpHeaders("error").disableCaching();
    }
    try {
        objProperties =  fcGetDs.findSourcedProperties(RELS_EXTDataString,propXMLDataString);

        } catch (Exception getDsErr) {
           classLogger.warn(
        "Errors thrown while locating sourced properties.", getDsErr);

      this.addActionError("Errors thrown while locating sourced properties: "+getDsErr.getMessage());

      return new DefaultHttpHeaders("error").disableCaching();
    }
    return new DefaultHttpHeaders("show").disableCaching();

	} // End of `NameController.show` method.
    /**
   * Entry point to the controller from /AlaHarvester/name/
   * It displays a list of all Taxon Name objects in the repository

   * @return Forwards results to the name-index web page
   *
   * @since v0.1
   */
    public HttpHeaders index() {

        // Initialises the required mappings.
        this.init();
        ArrayList<SearchResult> sr;
        try {
            sr = (ArrayList<SearchResult>) PseudoRepository.doInitIndex("hasModel", "ala:TaxonNameContentModel", null);
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query"+ex.getMessage());

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        searchResults=sr;

        return new DefaultHttpHeaders("index").disableCaching();
   }
    public List getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List identifiers) {
        this.identifiers = identifiers;
    }

    public void setObjProperties(ArrayList<AlaSourcedPropertiesData> objProperties) {
        this.objProperties = objProperties;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
	public String getId() {
		return id;
	} // End of `TaxaController.getId` method.

	public void setId(String id) {
		if (id != null) {
			// this.model = MessageService.find(id);
		}
		this.id = id;
    
	} // End of `TaxaController.setId` method.


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
      public List getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public Collection<AlaSourcedPropertiesData> getObjProperties() {
    
    // TODO: How to deal with `null` properties?  Throw an exception here?
    
    return this.objProperties;

  } // End of `NameController.getProperties` method.

  private void init() {

  } // End of `initCMIdToUrlMappings` method.

} // End of `NameController` class.
