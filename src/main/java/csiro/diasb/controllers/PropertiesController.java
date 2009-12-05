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

/**
 * A Simple controller for viewing the properties derived for a 
 * scientific name. This is intended for debugging purposes only.
 * 
 * @author Dave Martin
 */
@Results({
    @Result(name = "success", type = "redirectAction", params = {"actionName", "properties"})
})
public class PropertiesController extends ActionSupport {
	
	protected static final Logger logger = Logger.getLogger(PropertiesController.class);
    /** This should be set in show() requests */
    private String id;
    private boolean sort = false;
	private FedoraDAO fedoraDAO;
	private List<DocumentDTO> properties;
	private List<OrderedDocumentDTO> orderedDocuments;
	
	public HttpHeaders index() {
		System.out.println("PropertyViewController - kicking off");
		return new DefaultHttpHeaders("index").disableCaching();
	}

	/**
	 * Resolves URLs of the form http://.../properties/<scientific-name>
	 * 
	 * @return
	 */
	public HttpHeaders show() {
		List<String> sciNames = new ArrayList<String>();
		sciNames.add(getId());
		logger.info("Searching with name:"+getId());
		if(!sort){
			this.properties = fedoraDAO.getDocumentsForName(sciNames);
		} else {
			this.orderedDocuments = fedoraDAO.getOrderedDocumentsForName(sciNames);
		}
		return new DefaultHttpHeaders("show").disableCaching();
	}	
	
    @Inject
	public void setFedoraDAO(FedoraDAO fedoraDAO) {
		this.fedoraDAO = fedoraDAO;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<DocumentDTO> getProperties() {
		return properties;
	}

	public void setProperties(List<DocumentDTO> properties) {
		this.properties = properties;
	}

	public FedoraDAO getFedoraDAO() {
		return fedoraDAO;
	}

	public boolean isSort() {
		return sort;
	}

	public void setSort(boolean sort) {
		this.sort = sort;
	}

	public List<OrderedDocumentDTO> getOrderedDocuments() {
		return orderedDocuments;
	}

	public void setOrderedDocuments(List<OrderedDocumentDTO> orderedDocuments) {
		this.orderedDocuments = orderedDocuments;
	}
}
