/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  PseudoRepository.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.fedora;

import csiro.diasb.datamodels.InfoSource;
import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.fedora.FcGetDsContent;
import csiro.diasb.fedora.FcSearch;
import csiro.diasb.fedora.FedoraConstants;
import csiro.diasb.fedora.FedoraException;
import csiro.diasb.fedora.SolrQueryException;
import csiro.diasb.fedora.SolrSearch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.*;

/**
 * A series of functions that contain code copied from the DiasbIngester
 * and that should be replaced by direct calls to that application. Contains
 * sets of lists of available classes, properties that can be searched for
 * and stub links to InfoSoruce and harvesting functions.
 * @author oak021
 */
public class PseudoRepository {

/**
 * Mapping between shortened forms of property names and their namespace
 * qualified equivalents. This allows the user to be presented with a simplifed
 * interface
 */
static private final HashMap<String, String> propToNSPropMap =
    new HashMap<String, String>(1);
/**
 * Mapping between shortened forms of field names as stored in SOLR and their namespace
 * qualified equivalents. This means that SOLR indices can be stored with simple names
 */
static private final HashMap<String, String> propToSOLRFieldMap =
    new HashMap<String, String>(1);

/**
 * Mapping between shortened forms or colloquial forms to content models  and pid
 * equivalents. This allows the user to be presented with a simplifed
 * interface
 */
static private final HashMap<String, String> cmToCmIDMap =
        new HashMap<String, String>(1);
/**
	 * The logger for this class.
   *
   * Using fully qualified name to indicate that this is a Apache Log 4 J
   * Logger, as opposed to JRE's built-in Logger.
   *
	 */
private static final org.apache.log4j.Logger logger =
    Logger.getLogger(PseudoRepository.class.getName());

/**
 * A list of common names for known content models. These must be
 * mapped to their Fedora pids in the cmToCmIDMap mapper
 * @return
 */
public static Collection<String> getContentModels()
{
    ArrayList<String> res = new ArrayList<String>();
    res.add("Publication Content Model");
    res.add("Taxon Concept Content Model");
    res.add("Taxon Name Content Model");
    res.add("Image Content Model");
    res.add("HTML Page Content Model");
    res.add("InfoSource Content Model");
    return res;
}
/**
 * A list of simplified names of common properties for searching using
 * the RI search engine. These must be
 * mapped to their namespaced equivalents using propToNSPropMap
 * @return
 */
public static Collection<String> getProperties()
{
    ArrayList<String> res = new ArrayList<String>();
    res.add("identifier");
    res.add("title");
    res.add("hasRank");
    res.add("hasUninomial");
    res.add("sameAs");
    res.add("hasNameComplete");
    res.add("hasGenusPart");
    res.add("hasSpecificEpithet");

    res.add("hasScientificName");
    res.add("hasTaxonName");
    res.add("hasPublishedIn");
    res.add("hasIsParentTaxonOf");
    res.add("hasIsChildTaxonOf");
    res.add("hasCollectionId");
    res.add("hasGeographicRegionId");

    res.add("hasTitle");
    res.add("hasPublicationType");

    return res;
}

/**
 * List of search terms that match felds defined in SOLR schema.xml
 */
public static Collection<String> getSOLRFields()
{
    if (propToSOLRFieldMap.isEmpty()) createPropertySOLRMap();
    ArrayList<String> res = new ArrayList<String>();
    res.add("PID");
    res.add("hasModel");
    res.add("title");
    res.add("identifier");
    res.add("hasRank");
    res.add("hasUninomial");
    res.add("hasNameComplete");
    res.add("hasTaxonName");
    res.add("hasScientificName");
    res.add("hasSpecificEpithet");
    res.add("hasGenusPart");
    res.add("hasRank");
    res.add("sameAs");
    res.add("hasPublishedIn");
    res.add("hasIsParentTaxonOf");
    res.add("hasIsChildTaxonOf");
    res.add("hasCollectionId");
    res.add("hasGeographicRegionId");
    res.add("hasTitle");
    res.add("hasPublicationType");
    res.add("asGeographicRegionId");

    return res;
}
/**
 * Initialises the contentModel common-name to pid map
 */
static private void createContentModelPIDMap()
{
    cmToCmIDMap.put("Publication Content Model","info:fedora/ala:PublicationContentModel");
    cmToCmIDMap.put("Taxon Name Content Model","info:fedora/ala:TaxonNameContentModel");
    cmToCmIDMap.put("Taxon Concept Content Model", "info:fedora/ala:TaxonConceptContentModel");
    cmToCmIDMap.put("Image Content Model", "info:fedora/ala:ImageContentModel");
    cmToCmIDMap.put("HTML Page Content Model", "info:fedora/ala:HtmlPageContentModel");
    cmToCmIDMap.put("Info Source Content Model", "info:fedora/ala:HtmlPageContentModel");
    cmToCmIDMap.put("Publication","info:fedora/ala:PublicationContentModel");
    cmToCmIDMap.put("Taxon Name","info:fedora/ala:TaxonNameContentModel");
    cmToCmIDMap.put("Taxon Concept", "info:fedora/ala:TaxonConceptContentModel");
    cmToCmIDMap.put("Image", "info:fedora/ala:ImageContentModel");
    cmToCmIDMap.put("HTML Page", "info:fedora/ala:HtmlPageContentModel");
    cmToCmIDMap.put("Info Source", "info:fedora/ala:InfoSourceContentModel");
}
/**
 * Looks for a fedora pid given a common content model name
 * @param contentModelName
 * @return The pid for this model in the Fedora Repository
 */
static String getContentModelPID(String contentModelName)
{
    if (cmToCmIDMap.isEmpty()) createContentModelPIDMap();
    String cmID = cmToCmIDMap.get(contentModelName);
    return cmID;
}
/**
 * Creates the mapping between common property names and their namespaced equivalents
 */
static private void createPropertyNSMap()
{   
    //Note I need to specify a namespace for an IRSearch, as I can't give a wildcard. If more
    //than one type of object can have this name, we cannot map it - will need to distinguish between them
    //that's why I've commented-out sameAs

    String nameSpace = "dc:";
    String[] pdc = {"label","state","ownerId","cDate","mDate","dcmDate",
        "title","creator","subject","description","publisher","contributor",
        "date","type","format","identifier","source","language","relation","coverage","rights"};
    for (int i=0;i<pdc.length;i++)
        propToNSPropMap.put(pdc[i], new String(nameSpace+pdc[i]));
    
    nameSpace = FedoraConstants.ALA_TAXONNAME_NAMESPACE;
    String[] ptn = {"hasRank","hasUninomial",/*"sameAs",*/"hasNameComplete"};
    for (int i=0;i<ptn.length;i++)
        propToNSPropMap.put(ptn[i], new String(nameSpace+ptn[i]));

    nameSpace = FedoraConstants.ALA_TAXONCONCEPT_NAMESPACE;
    String[] ptc = {"hasScientificName","hasTaxonName","hasPublishedIn","hasIsParentTaxonOf",
    "hasIsChildTaxonOf","hasCollectionId","hasGeographicRegionId"};
    for (int i=0;i<ptc.length;i++)
        propToNSPropMap.put(ptc[i], new String(nameSpace+ptc[i]));

    nameSpace = FedoraConstants.ALA_PUBLICATION_NAMESPACE;
    String[] ptp = {"hasTitle","hasPublicationType",/*"sameAs",*/};
    for (int i=0;i<ptp.length;i++)
        propToNSPropMap.put(ptp[i], new String(nameSpace+ptp[i]));

    //miscellaneous
    propToNSPropMap.put("hasModel","fedora-model:hasModel");
    propToNSPropMap.put("contentModel","fgs.contentModel");

   

}
/**
 * Initialises the propToSOLRFieldMap
 */
static private void createPropertySOLRMap()
{
    //Note I need to specify a namespace for an IRSearch, as I can't give a wildcard. If more
    //than one type of object can have this name, we cannot map it - will need to distinguish between them
    //that's why I've commented-out sameAs

    String nameSpace = "dc.";
    String[] pdc = {"label","state","ownerId","cDate","mDate","dcmDate",
        "title","creator","subject","description","publisher","contributor",
        "date","type","format","identifier","source","language","relation","coverage","rights"};
    for (int i=0;i<pdc.length;i++)
        propToSOLRFieldMap.put(pdc[i], new String(nameSpace+pdc[i]));

    nameSpace = "rdf.";
    String[] pt = {"hasRank","hasUninomial","sameAs","hasNameComplete",
                    "hasScientificName","hasTaxonName","hasPublishedIn","hasIsParentTaxonOf",
                    "hasIsChildTaxonOf","hasCollectionId","hasGeographicRegionId",
                    "hasTitle","hasPublicationType",/*"sameAs",*/
                    "hasModel","hasSpecificEpithet","hasGenusPart"};
    for (int i=0;i<pt.length;i++)
        propToSOLRFieldMap.put(pt[i], new String(nameSpace+pt[i]));

}
/**
 * Searches the propToNSPropMap for a name-spaced ueqivalent for the given property
 * @param propertyName
 * @return the namespaced euqivalent if one is found, or propertyName unchanged if not
 */
public static String getNSPropertyName(String propertyName)
{
    if (propToNSPropMap.isEmpty()) createPropertyNSMap();
    String NSProp = propToNSPropMap.get(propertyName);
    if (NSProp == null) return propertyName;
    else return NSProp;
}
/**
 * Searches the propToSOLRPropMap for a name-spaced ueqivalent for the given property
 * @param propertyName
 * @return the namespaced equivalent if one is found, or propertyName unchanged if not
 */
static String getSOLRFieldName(String propertyName)
{
    if (propToSOLRFieldMap.isEmpty()) createPropertySOLRMap();
    String solrField = propToSOLRFieldMap.get(propertyName);
    if (solrField == null) return propertyName;
    else return solrField;
}
/**
 * Returns a list of all ingested info sources, but by guid rather than internal pid. Used to
 * populate a list of availale InfoSources to harvest
 * @return A list of infosource labels
 */
static Collection<String> getInfoSourceLabels()
{
    //this should come directly from the Fedora Repository
    ArrayList<String> res = new ArrayList<String>();
    res.add("ala:infosource:demo:sprint3:tn:AFDportalmatch");
    return res;
}
/**
 * Returns a list of DC fields for populating a list box. The namespace prefix dc.
 * if first removed.
 * @return
 */
public static Collection<String> getDCStreamFields()
{
    ArrayList<String> res = new ArrayList<String>();
    //res.add("pid");
    res.add("label");
    res.add("state");
    res.add("ownerId");
    res.add("cDate");
    res.add("mDate");
    res.add("dcmDate");
    res.add("title");
    res.add("creator");
    res.add("subject");
    res.add("description");
    res.add("publisher");
    res.add("contributor");
    res.add("date");
    res.add("type");
    res.add("format");
    res.add("identifier");
    res.add("source");
    res.add("language");
    res.add("relation");
    res.add("coverage");
    res.add("rights");
    return res;
}
 /**
   * Creates a new InforSource in Fedora Commons Content Repository system,
   *
   * At the time of writing, Fedora Commons version 3.1 is the target
   * Content Repository system.

   * @param isInput An {@link csiro.diasb.ingester.InfoSource InfoSource}
   * whose data will be used to generate a digital object in Fedora Commons
   * Content Repository system.
   *
   * @return PID (Identifier of a Fedora Commons Digital Object) of the
   * newly created {@link csiro.diasb.ingester.InfoSource InfoSource} type
   * Fedora Commons Digital Object in {@link String} representation.
   * @throws IOException  if the InfoSource could not be be created in Fedora.
   */
static String createOrUpdateInfoSource(InfoSource is) /*throws IOException*/
{
    return new String("ala:"+is.getDescription());
}
/**
 * Place-holder for a function that harvests the data from a given InfoSource. This is not yet implemented.
 * @param guid the first line of the infosource text file, or the label of the InfoSource object
 * @return Message string.
 */
//the guid is 
static String harvest(String guid){
    return "Harvesting not yet implemented";
}
/**
 * This is the default index search called from index() within the controllers
 * @param propertyName
 * @param propertyValue
 * @return
 */
public static Collection<SearchResult> doInitIndex(String propertyName, String propertyValue, String contentModelName) throws IOException, FedoraException, SolrQueryException
{
    SolrSearch solrj = new SolrSearch();

    String SOLRFieldName = PseudoRepository.getSOLRFieldName(propertyName);
    int currentPage=0;
    int resultsPerPage = 50;

    String escVal = propertyValue.replace(":", "\\:");

     return (ArrayList<SearchResult>) solrj.query(SOLRFieldName,escVal,
                 currentPage*resultsPerPage,resultsPerPage);
}
/**
 * Adds the namespaces to property names and values before passing them through to the Fedora Resource-Index search
 * @param propertyName Name of the property to match (without namespace prefix)
 * @param propertyValue Value of the property to match (without namespace prefix)
 * @param contentModelName Type of object model to return (if null then all content model
 * types will be returned) (without namespace prefix)
 * @return
 * @throws java.io.IOException
 * @throws csiro.diasb.fedora.FedoraException
 */
public static Collection<SearchResult> findObjects(String propertyName, String propertyValue, String contentModelName) throws IOException, FedoraException
{
    String contentModelPID = getContentModelPID(contentModelName);
    String NSPropertyName = getNSPropertyName(propertyName);
    int maxResults = 50;
    
    return findRIObjectByProperty(NSPropertyName, propertyValue, contentModelPID, maxResults);
}

/**
 * Searches the Resource Index of the Fedora Repository for objects matching the given critera
 * @param propertyName Name of the property to match (with namespace prefix)
 * @param propertyValue Value of the property to match (with namespace prefix)
 * @param contentModel Type of object model to return (if null then all content model
 * types will be returned) (with namespace prefix)
 * @param maxResults Maximum number of results to return
 * @return All matching results as an array of SearchResults objects
 * @throws java.io.IOException
 * @throws csiro.diasb.fedora.FedoraException
 */
static Collection<SearchResult> findRIObjectByProperty(String propertyName, String propertyValue, String contentModel, int maxResults) throws IOException, FedoraException
{
    ArrayList<SearchResult> res = new ArrayList<SearchResult>();
     
    FcSearch fcS = new FcSearch();
    if ((contentModel==null) || contentModel.isEmpty() || contentModel.equalsIgnoreCase("unknown"))
        res = (ArrayList<SearchResult>) fcS.findRIObjectsByProperty(propertyName, propertyValue, maxResults);
    else res = (ArrayList<SearchResult>) fcS.findRIObjectsByProperty(propertyName, propertyValue, contentModel, maxResults);

    return res;
}

}
