package csiro.diasb.util;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import csiro.diasb.datamodels.Tuple;
import csiro.diasb.fedora.FcGuidConverter;


/**
 * Utility class to extract RDF properties from different type of sources
 * to a single type that contains all the RDF properties.
 *
 * @author hwa002
 */
public class RdfPropertiesParser {

  private static final Logger classLogger = 
    Logger.getLogger("csiro.diasb.util.RdfPropertiesParser");
  private boolean showDebugMessage = true;

  /**
   * Parses a RDF/XML represented as a String to extract the RDF properties.
   *
   * The RDF/XML is assumed to follow this format:
   * <span>
   * <ds>
   *   <property rdf:resource="uri">
   *   <property rdf:resource="uri">
   *   ...
   * </ds>
   * </span>
   *
   * @param rdfXml RDF/XML represented as a {@link java.lang.String}
   *
   * @return {@link csiro.diasb.sandbox.Tuple> that contains the RDF properties
   * found in the <code>rdfXml</code> String.  Empty if there are no
   * properties.
   *
   * @throws Exception On error.
   */
  public Collection<Tuple> parseRdfProperties(String rdfXml) throws Exception {

    if (this.showDebugMessage == true) {
      RdfPropertiesParser.classLogger.log(Level.INFO,
        "String to parse is: " + rdfXml + "\n");
    }

    // Instantiates a new List to hold the properties.
    ArrayList<Tuple> foundProperties = new ArrayList<Tuple>();

    // Instantiating a DOM parser to parse the XML file.
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
    inStream.setCharacterStream(new StringReader(rdfXml));
    Document doc = docBuilder.parse(inStream);

    // Get the first node, which is the root element that we'll ignore.
    Element rootElement = doc.getDocumentElement();

    if (this.showDebugMessage == true) {
      RdfPropertiesParser.classLogger.log(Level.INFO,
        "Root element is: " + rootElement.getNodeName() + "\n");
    }

    // Now we get all the properties nodes.
    NodeList xmlElements = rootElement.getChildNodes();

    if (xmlElements.getLength() == 0) {
      throw new Exception("RDF/XML document is empty.");
    }
    
    // Ignore the root element.
    for (int currentIndex = 0;
         currentIndex < xmlElements.getLength();
         currentIndex++) {

      // Obtains the current node, only if it is an XML element.
      Element currentXmlElement = null;
      Node currentNode = xmlElements.item(currentIndex);
      if( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
        currentXmlElement = (Element) currentNode;

        // Obtains the XML element name and the single `rdf:resource` attribute.
        String propertyName = currentXmlElement.getTagName();

        if (this.showDebugMessage) {
          classLogger.log(Level.INFO, "Property is: " + propertyName);
        }

        // Obtains the resource the relationship is pointing at.
        // Assumes the attribute `rdf:resource` contains the target URI.
        String propertyTarget = currentXmlElement.getAttribute("rdf:resource");

        if (this.showDebugMessage) {

          if (propertyTarget.length() == 0) {
            classLogger.warn(
              "Target of property is empty.");
          } else {
            classLogger.log(Level.INFO, "target of property is: " +
              propertyTarget);
          }
        }

        // Adds the current Tupel to the list, on the condition
        // that the property target is empty.
        if (propertyTarget.length() != 0) {

          // Converts the GUID to a Fedora Commons PID name convention.
          String pid = FcGuidConverter.toRepositoryGUID(propertyTarget);

          // Constructs a new Tupel to hold the current relationship.
          Tuple<String, String> currentProperty =
            new Tuple<String, String>(propertyName, pid);

          foundProperties.add(currentProperty);
          
        } // End of creating a new new Tuple and populating the list.

      } // End of if current node is XML element.
      
    } // End of traversing the XML tree.

    if (this.showDebugMessage == true) {
      // Traverse the property collection list to show it's content.
      
      final int propertyPairsCount = foundProperties.size();

      RdfPropertiesParser.classLogger.log(Level.INFO,
        propertyPairsCount + " property pairs found.\n");

      for (int currentCount = 0;
           currentCount < propertyPairsCount;
           currentCount++) {

        RdfPropertiesParser.classLogger.log(Level.INFO,
          currentCount + " " +
          foundProperties.get(currentCount).toString());
      }

      RdfPropertiesParser.classLogger.log(Level.INFO, "\n");

    } // End of debug message showing traversal of property collection list.


    return foundProperties;

  } // End of `TaxaController.parseRdfProperties` method.

} // End of `RdfPropertiesParser` class.

