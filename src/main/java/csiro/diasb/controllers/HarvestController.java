/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  HarvestController.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.controllers;

import java.util.ArrayList;
import org.apache.log4j.*;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.convention.annotation.Result;
import com.opensymphony.xwork2.ActionSupport;
import csiro.diasb.datamodels.SearchResult;
import java.util.List;

/**
 * Controls the harvesting of data from InfoSources
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.1
 */
@Results({
    @Result(name="success", type="redirectAction", params = {"actionName" , "harvest"})
})
public class HarvestController extends ActionSupport {

  // PID of digital object request from URL.
  private String id;

    public String getInfoSourceGUID() {
        return infoSourceGUID;
    }

    public void setInfoSourceGUID(String infoSourceGUID) {
        this.infoSourceGUID = infoSourceGUID;
    }

    public List getInfoSourceList() {
        return infoSourceList;
    }

    public void setInfoSourceList(List infoSourceList) {
        this.infoSourceList = infoSourceList;
    }

  String infoSourceGUID;
  List infoSourceList;
/**
 * List of all current publications in the Fedora repository (made if no
 * pid is specified on entry)
 */
 private List searchResults;

  private static final Logger classLogger =
    Logger.getLogger(HarvestController.class.getName());

  private final boolean DEBUG_MSG = true;

  
	public HttpHeaders show() {

    infoSourceList = (ArrayList<String>) PseudoRepository.getInfoSourceLabels();
 
	return new DefaultHttpHeaders("setup").disableCaching();

	} // End of `TaxaController.show` method.

public HttpHeaders go(){
    PseudoRepository.harvest(infoSourceGUID);
    return new DefaultHttpHeaders("monitor").disableCaching();
}
    public HttpHeaders index() {
         // Initialises the required mappings.
        this.init();
        ArrayList<SearchResult> sr;
        try {
            sr = (ArrayList<SearchResult>) PseudoRepository.doInitIndex("hasModel", "ala:InfoSourceContentModel", null);
        } catch (Exception ex) {
            classLogger.warn("Errors returned from initialise index query"+ex.getMessage());

            this.addActionError("Errors returned from initialise index query "+ex.getMessage());

            return new DefaultHttpHeaders("error").disableCaching();
        }
        searchResults=sr;

        return new DefaultHttpHeaders("index").disableCaching();
    }
	public String getId() {
		return id;
	} 

	public void setId(String id) {
		if (id != null) {
			
		}
		this.id = id;
    
	} 
 
  private void init() {

  } 

} // End of `HarvestController` class.
