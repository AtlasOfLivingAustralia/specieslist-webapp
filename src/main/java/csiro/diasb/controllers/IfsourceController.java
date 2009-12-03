package csiro.diasb.controllers;

import csiro.diasb.datamodels.InfoSource;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.*;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.convention.annotation.Result;
import com.opensymphony.xwork2.ActionSupport;
import java.util.List;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Struts 2 REST plugin for InfoSources (ifsource) digital objects.
 *
 * The digital objects are assumed to be residing in Fedora Commons.
 *
 * @author oak021
 * @version 0.1
 */
@Results({
    @Result(name="success", type="redirectAction", params = {"actionName" , "ifsource"})
})
public class IfsourceController extends ActionSupport {

  //PID assigned to it withing the Fedora Repository
  private String pid;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
  // PID of digital object request from URL.
  private String id;
  //InfoSource
  /**
  * The GUID of the InfoSource.
  */
  private String guid;
  /**
  * fedora PID for InfoSource Content Model.
  */
  private String hasModelPID;
  /**
  * A source is Authoritative if it is the most trusted source of the target model
   * New ditial objects will only be created from Authoritative sources
  */
  private boolean authoritative;
  /**
  * The description of the InfoSource.
  */
  private String description;
  /**
  * The protocol of the InfoSource. eg.OAI-PMH,JMS,FlatFile+XPath
  */
  private String protocol;
  /**
  * The URI of the InfoSource.
  */
  private String endpoint;
  /**
  * Any other Protocol-specific parameters.
  */
  private String protocolParam;

    public String getDestMetadataClass() {
        return destMetadataClass;
    }

    public void setDestMetadataClass(String destMetadataClass) {
        this.destMetadataClass = destMetadataClass;
    }

    public String getDestMetadataClassPID() {
        return destMetadataClassPID;
    }

    public void setDestMetadataClassPID(String destMetadataClassPID) {
        this.destMetadataClassPID = destMetadataClassPID;
    }

    public String getDocumentMapper() {
        return documentMapper;
    }

    public void setDocumentMapper(String documentMapper) {
        this.documentMapper = documentMapper;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getHasModelPID() {
        return hasModelPID;
    }

    public void setHasModelPID(String hasModelPID) {
        this.hasModelPID = hasModelPID;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public void setAuthoritative(boolean authoritative) {
        this.authoritative = authoritative;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocolParam() {
        return protocolParam;
    }

    public void setProtocolParam(String protocolParam) {
        this.protocolParam = protocolParam;
    }

    public String getSourceXMLSchema() {
        return sourceXMLSchema;
    }

    public void setSourceXMLSchema(String sourceXMLSchema) {
        this.sourceXMLSchema = sourceXMLSchema;
    }

    public List getProtocolList() {
        return protocolList;
    }
    private List protocolList;
    private List mapperList;
    private List contentModelList;


    public List getMapperList() {
        return mapperList;
    }

    public List getContentModelList() {
        return contentModelList;
    }

    public void setContentModelList(List contentModelList) {
        this.contentModelList = contentModelList;
    }

    public List getParamList() {
        return paramList;
    }

    public void setParamList(List paramList) {
        this.paramList = paramList;
    }
    private List paramList;
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
  /**
  * URL for XML Schema or RDF Schema document for validating documents retrieved from InfoSource.
  */
  private String sourceXMLSchema;
  /**
  * MetadataClass for digital objects to hold documents retrieved from InfoSource.
  */
  private String destMetadataClass;
  /**
  * PID for MetadataClass content model.
  */
  private String destMetadataClassPID;
  /**
  * Java class name for DocumentMapper class to extract GUID and arbitrary properties from XML documents retrieved from InfoSource.
  */
  private String documentMapper;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
  private final String FC_OBJ_CM_ID =
    "info:fedora/fedora-system:FedoraObject-3.0";

  private InfoSource infoSource;

    public InfoSource getInfoSource() {
        return infoSource;
    }

    public void setInfoSource(InfoSource infoSource) {
        this.infoSource = infoSource;
    }
  private static final Logger classLogger =
    Logger.getLogger(IfsourceController.class.getName());

  private final boolean DEBUG_MSG = true;

  /**
   * Method to process URL in the form of:
   * HTTP GET /taxa/<pid>
   * 
   * @since v0.1
   */
	public HttpHeaders show() {
        try {
            // Initialises the required mappings.
            this.init();
        } catch (Exception ex) {
            this.addActionError("Error during form initialisation:" + ex.getMessage());
        }

    if (this.getId().equals("create")) return new DefaultHttpHeaders("create").disableCaching();

		return new DefaultHttpHeaders("show").disableCaching();

	} // End of `TaxaController.show` method.
public HttpHeaders index() {
		
		return new DefaultHttpHeaders("create").disableCaching();
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
 

  private void init() throws IOException, ConfigurationException {
      infoSource = new InfoSource();
      this.setGuid("ala:infosource:demo:sprint1:tc:apodinae");
      setDescription("Apodinae Taxonomy Concept Demo info source for UC 2 Sprint 1");
      setAuthoritative(true);
      setProtocol("RFD");
      setProtocolParam("<params><param id=\"1\" key=\"key\" value=\"value\"></param></params>");
      setEndpoint("http://se1-cbr.vm.csiro.au:8080/data/rdf-for-ALA/Apodinae.xml");
      //setDestMetadataClassPID("ala:TaxonConceptContentModel");
      setSourceXMLSchema("http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonConcept");

      PropertiesConfiguration config = new PropertiesConfiguration("/DiasbIngester.properties");
      protocolList = config.getList("protocolHandler");
      mapperList = config.getList("documentMapper");

      //These should come directly from the repository, as should the PID
      //perhaps this should be a list of dc.title fields from the available content models
      contentModelList = (List) PseudoRepository.getContentModels();
      //infoSource.setContentModel("newPID");
      infoSource.setDestMetadataClass("metadataclass");
      paramList  = new ArrayList<String>();
      paramList.add("No parameters set");
  } // End of `init` method.

public HttpHeaders ingest() {

/*    if (!key.isEmpty())
    {
        String newparam = new String("Key: "+key +" Value: "+value);
        if (paramList==null) paramList =  new ArrayList<String>();
        paramList.add(newparam);
        key = value = "";
        return new DefaultHttpHeaders("create").disableCaching();
    }
    else*/
    {
        String cModel = this.getDestMetadataClass();
        this.setDestMetadataClassPID(PseudoRepository.getContentModelPID(cModel));
        InfoSource is = new InfoSource(this.getGuid(),this.isAuthoritative(),this.getDescription(),
            this.getProtocol(),this.getEndpoint(),this.getProtocolParam(),
            this.getSourceXMLSchema(),
            this.getDestMetadataClass(),this.getDestMetadataClassPID(),
            this.getDocumentMapper());
        //now how to ingest this into Fedora
        pid = PseudoRepository.createOrUpdateInfoSource(is);
   
    return new DefaultHttpHeaders("ingest").disableCaching();
    }

}
public HttpHeaders addParam() {

    
    return new DefaultHttpHeaders("create").disableCaching();

}
} // End of `IfsourceController` class.
