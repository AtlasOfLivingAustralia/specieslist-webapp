/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  HarvestController.java 697 2009-08-01 00:23:45Z oak021 $
 */
package csiro.diasb.fedora;

import csiro.diasb.datamodels.AlaSourcedPropertiesData;
import org.apache.log4j.*;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Obtains the content of a datastream of a Fedora Commons' digital object.
 *
 * @author oak021
 *
 * @version 0.3
 */
public class FcGetDsContent {
/**
	 * The logger for this class.
   *
   * Using fully qualified name to indicate that this is a Apache Log 4 J
   * Logger, as opposed to JRE's built-in Logger.
   *
	 */
  private static final Logger classLogger =
    Logger.getLogger(FcGetDsContent.class.getName());

  /**
   * Set to true for extra DEBUG tracing at INFO level
   */
  private final boolean DEBUG_MSG = true;

  /**
   * Reference to the Fedora Repository web service client.
   */
  private FedoraRepository fedoraRepository;

  /**
   * Default constructor.
   *
   * @throws IOException
   * @since v0.3
   */
  public FcGetDsContent() throws IOException {

    this.fedoraRepository = RepositoryFactory.getRepositoryFactory().getRepository();

  } // End of default constructor.
  
  /**
   * Obtains and returns the content of a datastream of a digital object
   * as a {@link java.lang.String}
   *
   * @param pid The <code>PID</code> of the Fedora Commons' digital object.
   *
   * @param ds The name of the datastream
   *
   * @return {@link java.lang.String} representation of the datastream.
   *
   * @throws Exception
   * @since v0.1
   */
  public String getDsStringContent(String pid, String ds) throws Exception {

    if (pid == null) {
      throw new NullPointerException("Supplied reference to PID is null.");
    }
    if (pid.length() == 0) {
      throw new IllegalArgumentException("Supplied PID is blank.");
    }

    if (ds == null) {
      throw new NullPointerException("Supplied reference to datastream name is null.");
    }
    if (ds.length() == 0) {
      throw new IllegalArgumentException("Supplied datastream name is blank.");
    }

    final String getDsUrl = "/get/" + pid + "/" + ds;

    if (DEBUG_MSG == true) {
      FcGetDsContent.classLogger.log(Level.INFO,
        "URL to issue GET method to is: " + getDsUrl);
    }

    return fedoraRepository.get(getDsUrl);
    
  } // End of `FcgetDsContent.getDsStringContent` method.

  /**
   * Obtains and returns the content of a datastream of a digital object
   * as a {@link java.lang.String}
   * 
   * @param pid The <code>PID</code> of the Fedora Commons' digital object.
   *
   * @return {@link java.lang.String} representation of the datastream.
   *
   * @throws Exception
   * @since v0.2
   */
  public String getDsStringContent(String pid) throws Exception {
    return this.getDsStringContent(pid,
      this.findSourcedPropertiesDatastream(pid));
    
  } // End of `FcGetDsContent.getDsStringContent` method.

  /**
   * Returns the data stream of a Fedora Commons digital object that contains
   * RDF for source properties.
   *
   * Assumes to be data stream with the format:
   * <code>
   * SP-<pid>
   * </code>
   *
   * @param pid The <code>PID</code> of the Fedora Commons digital object.
   * 
   * @return Name of the data stream that contains RDF of sourced properties.
   * <code>null</code> if there are none.
   *
   * @throws {@link java.lang.Exception} On error.
   *
   * @since v0.2
   */
  private String findSourcedPropertiesDatastream(String pid) throws Exception {

//    final String getUri = "/fedora/objects/";

    // Constructs the URL.
    final String getDsUrl = "/objects/" + pid + "/datastreams.xml";

    if (DEBUG_MSG == true) {
      FcGetDsContent.classLogger.log(Level.INFO,
        "Issuing HTTP GET to obtain datastreams of digital object with PID " +
        pid + "\n");
      FcGetDsContent.classLogger.log(Level.INFO,
        "URL to issue GET method to is: " + getDsUrl + "\n");
    }

    String responseString = fedoraRepository.get(getDsUrl);

    // HTTP GET command was successful, now start parsing XML for datastream
    // with the prefix `SP-`

    // Instantiating a DOM parser to parse the XML result.
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(responseString));
    Document doc = docBuilder.parse(inStream);

    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String("datastream");
    NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    // Each Fedora Commons digital object should have minimum of 2 data streams,
    // one for Dublin Core (DC) and the other is RELS-EXT.
    if (dsXmlElements.getLength() < 2) {
      throw new Exception("Number of datastreams found in digital object " +
        "with PID " + pid + " is less than minimum 2.");
    }

    // Iterate over the `datastream` XML elements, parsing the `dsid`
    // attribute.  Find and return the datastream with name that starts with
    // `SP-`
    final String targetDsPrefix = "SP-";
    final String dsIdAttr = "dsid";
    int index = 0;
    while (index < dsXmlElements.getLength()) {
      Element currentXml = (Element) dsXmlElements.item(index);
      String currentDsId = currentXml.getAttribute(dsIdAttr);
      if (currentDsId.startsWith(targetDsPrefix)) {
        return currentDsId;
      }
      index++;
    }

    return null;
  } // End of `FcGetDsContent.findSourcedPropertiesDatastream` method.


  /**
   * Lists the datastreams of an object.
   * @param pid The pid of the object.
   * @return DatastreamDef[] A datastream definition object, containing the following values
String ID The datastream id - "DC" for the DC datastream
String label The datastream label
String MIMEType The mimetype of the datastream, if any

   * @throws java.io.IOException
   * @throws csiro.diasb.fedora.FedoraException
   */
  public Collection<String> listDatastreams(String pid) throws IOException, FedoraException
{
  ArrayList<String> dsID = new ArrayList<String>();

    final String queryUrl = "/objects/" + pid+"/datastreams.xml";
    String responseString="";
    try {
        responseString = queryFedora(queryUrl);
    } catch (IOException ex) {
        throw new IOException("Error querying Fedora from listDatastreams");
    }
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
        throw new FedoraException("Error building new document in listDatastreams",ex);
    }

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(responseString));
    Document doc = null;
    try {
        doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new FedoraException("Error paring response from listDatastreams",ex);
    }
    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String("datastream");
    NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    // Each Fedora Commons digital object should have minimum of 2 data streams,
    // one for Dublin Core (DC) and the other is RELS-EXT.
    if (dsXmlElements.getLength() < 2) {
                throw new FedoraException("Number of datastreams found in digital object " + "with PID " + pid + " is less than minimum 2.");
    }

    // Iterate over the `datastream` XML elements, parsing the `dsid`
    // attribute.
    final String dsIdAttr = "dsid";
    int index = 0;
    while (index < dsXmlElements.getLength()) {
          Element currentXml = (Element) dsXmlElements.item(index);
          String currentDsId = currentXml.getAttribute(dsIdAttr);
          dsID.add(currentDsId);

          index++;
     }

    return dsID;
}
  /**
   * Gets the content of a datastream.
   * @param pid The PID of the object.
   * @param dsID The datastream ID
   * @return MIMETypedStream** String MIMEType The mimetype of the stream
byte[] stream The contents of the Stream
Property[] header The header will be empty, or if applicable, contain the http header as name/value pairs.
String name
String value

   * @throws java.io.IOException
   */
  public String getDatastreamDissemination(String pid, String dsID) throws IOException
{
    final String queryUrl = "/objects/" + pid + "/datastreams/"+dsID+"/content";
    return queryFedora(queryUrl);
}
/**
 * Gets a list of timestamps that correspond to modification dates of components. This currently includes changes to Datastreams and disseminators.
 * @param pid The pid of the object.
 * @return A string containing the list of timestamps indicating when changes were made to the object.
 * @throws java.io.IOException
 */
public String getObjectHistory(String pid) throws IOException
{
    final String queryUrl = "/objects/" + pid + ".xml";
    return queryFedora(queryUrl);
}
/**
 * Gets the specified datastream.
 * @param pid The pid of the object.
 * @param dsID
 * @return Datastream:
DatastreamControlGroup controlGroup - String restricted to the values of "X", "M", "R", or "E" (InlineXML,Managed Content,Redirect, or External Referenced).
String ID - The datastream ID (64 characters max).
String versionID - The ID of the most recent datastream version
String[] altIDs - Alternative IDs for the datastream, if any.
String label - The Label of the datastream.
boolean versionable - Whether the datastream is versionable.
String MIMEType - The mime-type for the datastream, if set.
String formatURI - The format uri for the datastream, if set.
String createDate - The date the first version of the datastream was created.
long size - The size of the datastream in Fedora. Not the size of any referenced contents, but only the fedora stored xml. TODO: What for Managed?
String state - The state of the datastream. Will be "A" (active), "I" (inactive) or "D" (deleted).
String location - If the datastream is an external reference or redirect, the url to the contents. TODO: Managed?
String checksumType - The algorithm used to compute the checksum. One of "DEFAULT", "DISABLED", "MD5", "SHA-1", "SHA-256", "SHA-385", "SHA-512", "HAVAL", "TIGER", "WHIRLPOOL".
String checksum - The value of the checksum represented as a hexadecimal string.

 * @throws java.io.IOException
 */
public String getDatastream(String pid, String dsID) throws IOException
{
      final String queryUrl = "/get/" + pid + "/" + dsID;
    return queryFedora(queryUrl);
}

/**
 * Queries the Fedora Repository using the REST interface
 * @param query
 * @return The web application result as a string.
 * @throws java.io.IOException
 */
public String queryFedora(String query) throws IOException
{
  return fedoraRepository.get(query);
}

/**
 * Searches the Fedora Repository and lists the specified fields of each object matching the given criteria.
 * @param propertyName type of property to search for
 * @param propertyValue The value of the property
 * @return The PIDs of all matching objects found
 * @throws java.io.IOException
 * @throws csiro.diasb.fedora.FedoraException
 */
public Collection<String> findObjects(String propertyName, String propertyValue) throws IOException, FedoraException
{

     final String queryUrl = "/objects?terms="+propertyValue+"&pid=true&subject=true&label=true&resultFormat=xml";

     ArrayList<String> res = new ArrayList<String>();
     String responseString="";
     try {
        responseString = queryFedora(queryUrl);
    } catch (IOException ex) {
        throw new IOException("Error querying Fedora from findObjects");
    }
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
        throw new FedoraException("Error building new document in findObjects",ex);
    }

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(responseString));
    Document doc = null;
    try {
            doc = docBuilder.parse(inStream);
    }
    catch (SAXException ex) {
        throw new FedoraException("Error parsing response from findObjects",ex);
    }
    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String("pid");
    NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    int index = 0;
    while (index < dsXmlElements.getLength()) {
      Element currentXml = (Element) dsXmlElements.item(index);
      String pid = currentXml.getTextContent();
      res.add(pid);

      index++;
    }
        
    return res;
}
/**
 * Searches the Fedora Repository and lists the specified fields of each object matching the given criteria.
 * @param propertyValue The value of the property
 * @param searchFields List of fields to look in
 * @return The PIDs of all matching objects found
 * @throws java.io.IOException
 * @throws csiro.diasb.fedora.FedoraException
 */
public Collection<String> findObjectsByField( String propertyValue, ArrayList<String> searchFields) throws IOException, FedoraException
{
       StringBuffer queryUrl = new StringBuffer("/objects?terms="+propertyValue);
       queryUrl.append("&pid=true");
       if (searchFields!=null) for (String fieldName : searchFields)
       {
           if (!fieldName.equals("pid")) queryUrl.append("&"+fieldName+"=true");
       }
       queryUrl.append("&resultFormat=xml");

    String responseString = queryFedora(queryUrl.toString());
    ArrayList<String> res = new ArrayList<String>();

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException ex) {
        throw new FedoraException("Error building new document in findObjectsByField",ex);
    }

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(responseString));
    Document doc = null;
    try {
        doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new FedoraException("Error parsing response from findObjectsByField",ex);
    }

    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String("pid");
    NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    int index = 0;
    while (index < dsXmlElements.getLength()) {
      Element currentXml = (Element) dsXmlElements.item(index);
      String pid = currentXml.getTextContent();
      res.add(pid);

      index++;
    }

    return res;
}
/**
 * Extracts all the values matching a particular field name from a supplied Fedora Object DC datastream
 * @param field The name of the field to look for eg dc.title or dc.identifier
 * @param DCDatastream The DC datastream extracted from a Fedora Repository object (as a string)
 * @return A set of all values matching the supplied field name
 * @throws javax.xml.parsers.ParserConfigurationException
 * @throws org.xml.sax.SAXException
 * @throws java.io.IOException
 */
public  Collection<String> findDCValues(String field, String DCDatastream) throws ParserConfigurationException, SAXException, IOException
{
    ArrayList<String> res = new ArrayList<String>();
        // Instantiating a DOM parser to parse the XML result.
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(DCDatastream));
    Document doc = null;
    try
    {
      doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new SAXException("Error parsing DC datastream",ex);
    } catch (IOException ex) {
        throw new IOException("Error parsing DC datastream",ex);
    }

    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String(field);
      NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    int index = 0;
    while (index < dsXmlElements.getLength()) {
      Element currentXml = (Element) dsXmlElements.item(index);
      String pid = currentXml.getTextContent();
      res.add(pid);

      index++;
 }
    return res;
}
/**
 * Extracts all the values matching a particular field name from a supplied Fedora Object RELS-EXT datastream
 * @param field The name of the field to look for (without any prefix) eg hasGuid
 * @param RSDatastream The RELS-EXT datastream extracted from a Fedora Repository object (as a string)
 * @return A set of all values matching the supplied field name
 * @throws javax.xml.parsers.ParserConfigurationException
 * @throws org.xml.sax.SAXException
 * @throws java.io.IOException
 */
public  Collection<String> findRELS_EXTValues(String field, String RSDatastream) throws ParserConfigurationException, SAXException, IOException
{
    ArrayList<String> res = new ArrayList<String>();
        // Instantiating a DOM parser to parse the XML result.
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(RSDatastream));
    Document doc = null;
    try {
        doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new SAXException("Error parsing RELS-EXT datastream",ex);
    } catch (IOException ex) {
        throw new IOException("Error parsing RELS-EXT datastream",ex);
    }

    // Find XML elements with the name `datastream`
    final String dsXmlElementName = new String("rdf:Description");
    NodeList dsXmlElements = doc.getElementsByTagName(dsXmlElementName);

    int index = 0;
    while (index < dsXmlElements.getLength()) {
      Element currentXml = (Element) dsXmlElements.item(index);
      NodeList props = currentXml.getElementsByTagName(field);

      int pIndex=0;
         while (pIndex < props.getLength()) {
             Node n = props.item(pIndex);
             String prop = n.getNodeName();
             if (!prop.equals("#text"))
             {
                 res.add(n.getTextContent());
             }
             pIndex++;
         }
      index++;
 }
    return res;
}
/**
 * Extracts all the values matching a particular field name from a supplied Fedora Object RELS-EXT datastream
 * @param field The name of the field to look for (without any prefix) eg hasGuid
 * @param RSDatastream The RELS-EXT datastream extracted from a Fedora Repository object (as a string)
 * @return A set of all values matching the supplied field name
 * @throws javax.xml.parsers.ParserConfigurationException
 * @throws org.xml.sax.SAXException
 * @throws java.io.IOException
 */
/**
 * Extracts the properties stored in the RELS-EXT datastream and creates an array of AlaSourcedPropertiesData
 * objects by supplementing the RDF triples with information from the PropXML stream (which also stores information
 * such as when the properties were harvested, and from where)
 * @param RELS_EXTDatastream The RELS-EXT datastream extracted from a Fedora Repository object (as a string)
 * @param PropXMLDatastream The PropXML datastream extracted from a Fedora Repository object (as a string)
 * @return An array of AlaSourcedProperties ready for display
 * @throws javax.xml.parsers.ParserConfigurationException
 * @throws org.xml.sax.SAXException
 * @throws java.io.IOException
 */
public  ArrayList<AlaSourcedPropertiesData> findSourcedProperties(String RELS_EXTDatastream, String PropXMLDatastream) throws ParserConfigurationException, SAXException, IOException
{
    // Instantiating a DOM parser to parse the XML result.
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(RELS_EXTDatastream));
    Document doc = null;
    try {
        doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new SAXException("Error parsing RELS-EXT datastream",ex);
    } catch (IOException ex) {
        throw new IOException("Error parsing RELS-EXT datastream",ex);
    }
       
    // Find XML elements with the name `rdf:Description`
    final String dsXmlElementName = new String("rdf:Description");
    NodeList reXmlElements = doc.getElementsByTagName(dsXmlElementName);
    //Now find the attributed xml nodes
    inStream.setCharacterStream(new StringReader(PropXMLDatastream));
    try {
        doc = docBuilder.parse(inStream);
    } catch (SAXException ex) {
        throw new SAXException("Error parsing PropXML datastream",ex);
    } catch (IOException ex) {
        throw new IOException("Error parsing PropXML datastream",ex);
    }
    NodeList apXmlElements = doc.getElementsByTagName("property");
    String contentModel = "";
    if (doc.getElementsByTagName("contentModel").getLength()>0)
        contentModel = doc.getElementsByTagName("contentModel").item(0).getTextContent();
    return findSourcedProperties(reXmlElements, apXmlElements, contentModel);
}
/**
 * Extracts an element matching the given relationship from a NodeList. Used to find the
 * nodes in the PropXML datastream that match relationships in the RELS-EXT datastream
 * @param relationship Name of the node to extract
 * @param pxdsNodeList List of candidate nodes to be searched
 * @return the Element matching the given realtionship (if found), null otherwise
 */
private Element findPropertyNode(String relationship, NodeList pxdsNodeList)
{
    Element e=null;

    for (int i=0;i<pxdsNodeList.getLength();i++)
      {
          e = (Element)pxdsNodeList.item(i);
          NodeList nl = e.getElementsByTagName("relationship");
          if (nl.getLength()>0)
          {
              String arel =nl.item(0).getTextContent();
              String subS = arel.substring(arel.lastIndexOf('#')+1);
              if (subS.matches(relationship)) return e;
          }
    }
    return null;
}

/**
 * Creates an array of AlaSourcedPropertiesData
 * objects by combining a list of RDF triples with extra information from a NodeList sourced from the PropXML stream
 * @param rsdsNodeList NodeList of all XML elements with the name `rdf:Description`, sourced from the RELS-EXT datastream
 * @param pxdsNodeList NodeList of all XML elements with the name `property`, sourced from the PropXML datastream
 * @param contentModel PID of the ala content model that describes the object owning the properties
 * @return
 */
public  ArrayList<AlaSourcedPropertiesData> findSourcedProperties(NodeList rsdsNodeList, NodeList pxdsNodeList, String contentModel)
{
    ArrayList<AlaSourcedPropertiesData> res = new ArrayList<AlaSourcedPropertiesData>();

    int index = 0;
    while (index < rsdsNodeList.getLength()) {
        Element currentXml = (Element) rsdsNodeList.item(index);
        NodeList props = currentXml.getChildNodes();
        int pIndex=0;
         while (pIndex < props.getLength()) {
             Node n = props.item(pIndex);
             String prop = n.getNodeName();
             if (!prop.equals("#text"))
             {
                 Element apNode = findPropertyNode(n.getNodeName(), pxdsNodeList);
                 if (apNode==null)
                 {
                     classLogger.warn("Could not find attributes for property "+n.getNodeName());
                 }
                 else
                 {
                     String propertyName = n.getNodeName();
                     String propertyValue = n.getTextContent();
                     String dataSource = apNode.getElementsByTagName("dataSource").item(0).getTextContent();
                     String sourceDSID = apNode.getElementsByTagName("sourceDSID").item(0).getTextContent();
                     String harvested = apNode.getElementsByTagName("harvested").item(0).getTextContent();
                     AlaSourcedPropertiesData data = new AlaSourcedPropertiesData(
                        propertyName,propertyValue,contentModel,dataSource,sourceDSID,harvested);
                     res.add(data);
                 }
             }
             pIndex++;
         }
      index++;
    }
    return res;
}
  
} // End of `FcgetDsContent` class.
