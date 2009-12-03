/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id: InfoSource.java 827 2009-06-29 04:38:53Z oak021 $
 */
package csiro.diasb.datamodels;

/**
 * This class defines an InfoSource for metadata. See: TBD
 *
 * @author fri096
 */
public class InfoSource {


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
  private boolean isAuthoritative;
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


  
  /**
   * Empty Constructor
   */
  public InfoSource() {
    // Empty constructor.
  }

  /**
   * Default constructor.
   * 
   * The generation of a <code>GUID</code> is done inside the constructor.
   *
   *
   * @param pidCM The identifier of the Content Model digital object which
   * is class belongs to.
   * @param label A description of this InfoSource
   * @param isAuthoritative True if this is the most trusted source for the target content model
   * @param proto Type of protocol to harvest
   * @param conEndPoint The connection's endpoint to point harvester at
   * @param protoParam Additional parameters of the harvest protocol
   * @param dataSrcSchema Format of the harvested data
   * @param metadataClassName Meta-data class of Content Repository system to
   * ingest data to
   * @param metadataClassPID The fedora Repository PID of the content model it conforms to
   * @param docMapperClassName Class name of parser of harvested data.
   */
  public InfoSource(String pidCM,boolean isAuthoritative, String label, 
                     String proto, String conEndPoint, String protoParam,
                     String dataSrcSchema,
                     String metadataClassName,String metadataClassPID,
                     String docMapperClassName) {

 //   this.guid = FCGUIDConverterImpl.toRepositoryGUID(UUID.randomUUID().toString());

    this.hasModelPID = pidCM;
    if (this.hasModelPID == null)
      throw new NullPointerException("Supplied reference to " +
        "ID of content model digital object is null.");
    this.isAuthoritative = isAuthoritative;

    this.description = label;
    if (this.description == null)
      throw new NullPointerException("Supplied reference to " +
        "label for Infosource digital object is null.");
    this.protocol = proto;
    if (this.protocol == null)
      throw new NullPointerException("Supplied reference to " +
        "protocol for Infosource digital object is null.");
    
    this.endpoint = conEndPoint;
    if (this.endpoint == null)
      throw new NullPointerException("Supplied reference to " +
        "endpoint for Infosource digital object is null.");
    
    this.protocolParam = protoParam;
    if (this.protocolParam == null)
      throw new NullPointerException("Supplied reference to " +
        "protocol parameters for Infosource digital object is null.");
    
    this.sourceXMLSchema = dataSrcSchema;
    if (this.sourceXMLSchema == null)
      throw new NullPointerException("Supplied reference to " +
        "data schema parameters for Infosource digital object is null.");

    this.destMetadataClass = metadataClassName;
    if (this.destMetadataClass == null)
      throw new NullPointerException("Supplied reference to " +
        "Metadata class parameters for Infosource digital object is null.");

    this.destMetadataClassPID = metadataClassPID;
    if (this.destMetadataClassPID == null)
      throw new NullPointerException("Supplied reference to " +
        "Metadata class parameters for Infosource digital object is null.");
    
    this.documentMapper = docMapperClassName;
    if (this.documentMapper == null)
      throw new NullPointerException("Supplied reference to " +
        "DocumentMapper class parameters for Infosource digital object is null.");

  } // End of `InfoSource.InfoSource` 

  /**
   * Gets the value of the GUID property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getGUID() {
    return guid;
  }

  /**
   * Sets the value of the GUID property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setGUID(String value) {
    this.guid = value;
  }
  /**
   * Gets the value of the Description property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the value of the Description property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setDescription(String value) {
    this.description = value;
  }
  /**
   * Gets the value of the protocol property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  /**
   * Gets the value of the IsAuthoritative property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
    public boolean getIsAuthoritative() {
        return isAuthoritative;
    }
/**
   * Sets the value of the IsAuthoritative property.
   *@param isAuthoritative 1 if true 0 otherwise
   *
   */
    public void setIsAuthoritative(boolean isAuthoritative) {
        this.isAuthoritative = isAuthoritative;
    }
    
    /**
     * Gets the protocol of the InfoSource. eg.OAI-PMH,JMS,FlatFile+XPath
     * @return the name of the protocol as a String
     */
    public String getProtocol() {
    return protocol;
  }

  /**
   * Sets the value of the protocol property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setProtocol(String value) {
    this.protocol = value;
  }

  /**
   * Gets the value of the Endpoint property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * Sets the value of the URI property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setEndpoint(String value) {
    this.endpoint = value;
  }

  /**
   * Gets the value of the others property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getProtocolParam() {
    return protocolParam;
  }

  /**
   * Sets the value of the others property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setProtocolParam(String value) {
    this.protocolParam = value;
  }
  /**
   * Gets the value of the others property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getSourceXMLSchema() {
    return sourceXMLSchema;
  }

  /**
   * Sets the value of the others property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setSourceXMLSchema(String value) {
    this.sourceXMLSchema = value;
  }
  /**
   * Gets the value of the DestMetadataClass property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getDestMetadataClass() {
    return destMetadataClass;
  }

  /**
   * Sets the value of the DestMetadataClass property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setDestMetadataClass(String value) {
    this.destMetadataClass = value;
  }
  /**
   * Gets the value of the DestMetadataClassPID property.
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getDestMetadataClassPID() {
    return destMetadataClassPID;
  }

  /**
   * Sets the value of the DestMetadataClassPID property.
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setDestMetadataClassPID(String value) {
    this.destMetadataClassPID = value;
  }
  /**
   * Gets the value of the DocumentMapper property.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getDocumentMapper() {
    return documentMapper;
  }

  /**
   * Sets the value of the DocumentMapper property.
   * 
   * @param value
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setDocumentMapper(String value) {
    this.documentMapper = value;
  }  
  /**
   * Gets the value of the hasContentModel.
   * 
   * @return
   *     possible object is
   *     {@link String }
   *     
   */
  public String getContentModel() {
    return hasModelPID;
  }

  /**
   * Sets the value of the hasContentModel property.
   * 
   * @param pidCM
   *     allowed object is
   *     {@link String }
   *     
   */
  public void setContentModel(String pidCM) {
    this.hasModelPID = pidCM;
  }  

  @Override
  public String toString() {
    return this.guid + "+" + this.hasModelPID + "+" + this.isAuthoritative + "+"+ this.protocol + "+" + this.endpoint + "+" + this.protocolParam+ "+" +
            this.sourceXMLSchema+ "+" + this.destMetadataClass+ "+" + this.destMetadataClassPID+ "+" + this.documentMapper;
  }
}