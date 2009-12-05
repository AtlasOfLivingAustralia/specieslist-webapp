/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  AlaSourcedPropertiesData.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.datamodels;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Encapsulates the sourced properties data.  This is the unit of
 * data transfer between Struts 2 Action and its Results.
 *
 * Creates the URL for Hyperlinks of PIDs
 *
 * @author oak021
 *
 * @since v0.3
 */
public class AlaSourcedPropertiesData {

  private static final Logger classLogger =
    Logger.getLogger(AlaSourcedPropertiesData.class);

  private final boolean DEBUG_MSG = true;
/**
 * The PID of the ala content model that describes the Digital Object
 */
  private String contentModelId;
  /**
   * The RDF relationship property.
   */
  private String relationship;
  /**
   * The value of the RDF relationship to which the <code>propertyName</code> refers.
   */
  private String value;
  /**
   * Label describing the collection from which the property is sourced
   */
  private String dataSource;
  /**
   * Fedora DatastreamID  of the datastream containing a copy of the original source data
   */
  private String sourceDSID;
  /**
   * The date and time when this property was harvested from its source
   */
  private String harvested;
  
  // Mapping between the PID of content model and web application's context.
  // E.g.,
  // ala:TaxonConceptContentModel mapes to /taxa
  // ala:TaxonNameContentModel maps to /name
  static private final HashMap<String, String> cmIdToURLMapping =
    new HashMap<String, String>(1);

  // First part of URL to return
  private String urlMapping = new String();

  /**
   * Default constructor.  Populate this with data for data transfer.
   * 
   * @param propertyName The RDF relationship property.
   * @param propertyValue The value of the RDF relationship to which the <code>propertyName</code> refers.
   * @param contentModel The PID of the ala content model that describes the Digital Object
   * @param ds Label describing the collection from which the property is sourced
   * @param dsid Fedora DatastreamID  of the datastream containing a copy of the original source data
   * @param hTime  The date and time when this property was harvested from its source
   * with <code>propertyValue</code> belongs to.
   *
   * @since v0.3
   */
  public AlaSourcedPropertiesData(String propertyName, String propertyValue, String contentModel, String ds, String dsid, String hTime) {

    contentModelId = contentModel;
    relationship = propertyName;
    value = propertyValue;
    dataSource = ds;
    sourceDSID = dsid;
    harvested = hTime; 
    
    // Looks up and instantiates the URL mapping.
    // Populates the `urlMapping` instance variable.
    urlMapping = AlaSourcedPropertiesData.lookUpUrlMapping(contentModelId);

    if (this.DEBUG_MSG == true) {
      AlaSourcedPropertiesData.classLogger.log(Level.INFO,
        "URL mapping for Content Model PID " +
        this.getUrlMapping() + "\n");
    }

  } // End of constructor.

    
    public String getValue() {
      try {
        new URL(value);
        return "<a href=\"#\" onClick=\"AlaWindow=window.open('"+value+"','AlaWindow','toolbar=no," +
        		"location=yes,directories=no,status=yes,menubar=no,scrollbars=yes,resizable=yes,width=600,height=300'); " +
        		"return false;\">"+value+"</a>";
      } catch (MalformedURLException e) {
        return value;
      }
    }

    public void setValue(String value) {
        this.value = value;
    }
  
  /**
   * Looks up the URL to map to according to Content Model's ID.
   *
   * The full URL should looks like
   * <code>http://host:port/context/alaType/id</code>
   *
   * This method finds the correct path for <code>alaType</code>
   * based on a Digital Object's Content Model's PID.
   *
   * @since v0.3
   */
  static private String lookUpUrlMapping(String key) {

    if (cmIdToURLMapping.isEmpty())
    {// TODO: A better place would be from a property file
        AlaSourcedPropertiesData.cmIdToURLMapping.put("ala:TaxonConceptContentModel", "/taxa");
        AlaSourcedPropertiesData.cmIdToURLMapping.put("ala:TaxonNameContentModel", "/name");
        AlaSourcedPropertiesData.cmIdToURLMapping.put("ala:PublicationContentModel", "/pub");
        AlaSourcedPropertiesData.cmIdToURLMapping.put("ala:ImageContentModel", "/image");
        AlaSourcedPropertiesData.cmIdToURLMapping.put("ala:HtmlPageContentModel", "/html");
    }
    String mapping = AlaSourcedPropertiesData.cmIdToURLMapping.get(key);
    if (mapping==null) mapping="/datastream";
    return mapping;
  } // End of `lookUpUrlMapping` method.

  public String getUrlMapping() {
    return this.urlMapping;
  }
  public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getHarvested() {
        return harvested;
    }

    public void setHarvested(String harvested) {
        this.harvested = harvested;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getSourceDSID() {
        return sourceDSID;
    }

    public void setSourceDSID(String sourceDSID) {
        this.sourceDSID = sourceDSID;
    }

} // End of `AlaSourcedPropertiesData` class.
