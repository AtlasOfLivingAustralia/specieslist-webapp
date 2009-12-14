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
public class PropertiesController extends ActionSupport {
	
	protected static final Logger logger = Logger.getLogger(PropertiesController.class);
    /** This should be set in show() requests */
    private String id;
    private boolean propertiesOnly = false;
    private boolean sort = false;
	private FedoraDAO fedoraDAO;
	private List<DocumentDTO> documents;
	private List<OrderedDocumentDTO> orderedDocuments;
	private List<OrderedPropertyDTO> orderedProperties;

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
		
		List<String> sciNames = new ArrayList<String>();
		sciNames.add(getId());
		
		logger.info("Searching with name:"+getId());
		if(propertiesOnly){
			this.orderedProperties = fedoraDAO.getOrderedPropertiesForName(sciNames);
		} else if(!sort){
			this.documents = fedoraDAO.getDocumentsForName(sciNames);
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

	/**
	 * @return the propertiesOnly
	 */
	public boolean isPropertiesOnly() {
		return propertiesOnly;
	}

	/**
	 * @param propertiesOnly the propertiesOnly to set
	 */
	public void setPropertiesOnly(boolean propertiesOnly) {
		this.propertiesOnly = propertiesOnly;
	}

	/**
	 * @return the documents
	 */
	public List<DocumentDTO> getDocuments() {
		return documents;
	}

	/**
	 * @param documents the documents to set
	 */
	public void setDocuments(List<DocumentDTO> documents) {
		this.documents = documents;
	}

	/**
	 * @return the orderedProperties
	 */
	public List<OrderedPropertyDTO> getOrderedProperties() {
		return orderedProperties;
	}

	/**
	 * @param orderedProperties the orderedProperties to set
	 */
	public void setOrderedProperties(List<OrderedPropertyDTO> orderedProperties) {
		this.orderedProperties = orderedProperties;
	}
}
