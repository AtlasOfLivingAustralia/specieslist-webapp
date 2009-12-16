package csiro.diasb.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;

import csiro.diasb.dao.FedoraDAO;
import csiro.diasb.datamodels.DocumentDTO;
import csiro.diasb.datamodels.OrderedDocumentDTO;
import csiro.diasb.datamodels.OrderedPropertyDTO;

/**
 * A Simple controller for viewing the properties derived for a 
 * scientific name. This is intended for debugging purposes only.
 * 
 * @author Dave Martin
 */
@Results({
    @Result(name = "success", type = "redirectAction", params = {"actionName", "properties"})
})
public class DatasetsController extends ActionSupport {
	
	protected static final Logger logger = Logger.getLogger(DatasetsController.class);
    /** This should be set in show() requests */
    private String id;
    
	/**
	 * Required for REST plugin.
	 * @return
	 */
	public HttpHeaders index() {
		return new DefaultHttpHeaders("index").disableCaching();
	}

	/**
	 * Resolves URLs of the form http://.../properties/<scientific-name>
	 * 
	 * @return
	 */
	public HttpHeaders show() {
		
		return new DefaultHttpHeaders("show").disableCaching();
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
	
    
}
