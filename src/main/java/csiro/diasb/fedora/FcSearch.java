/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  FcSearch.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.fedora;

import csiro.diasb.datamodels.SearchResult;
import java.io.*;
import java.util.*;

import org.apache.commons.httpclient.*;
import org.apache.log4j.*;

/**
 * Search utilities for the Fedora Repositoy using the API findObjects interface
 * as well as the Resource Index
 * @author oak021
 */
public class FcSearch {   
     /**
	 * The logger for this class.
   *
   * Using fully qualified name to indicate that this is a Apache Log 4 J
   * Logger, as opposed to JRE's built-in Logger.
   *
	 */
	private static final org.apache.log4j.Logger logger =
    Logger.getLogger(FcSearch.class.getName());
     /**
   * Holds parameters that might change from installation to installation.
   * Set by Spring framework.
   */
 // private FedoraAPIConfig fedoraAPIConfig;
     /**
   * Constructs an ITQL or SPARQL query from the supplied parameters
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query  format
   */
 /*  private static String constructQuery(String propertyName, String propertyValue, String queryLang) throws FedoraException
  {
     String query;
      if (queryLang.equalsIgnoreCase("itql"))
        query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+"'";
      else if (queryLang.equalsIgnoreCase("sparql"))
        query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+"' }";
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
  }*/
private static String constructQuery(String propertyName, String propertyValue, String queryLang) throws FedoraException
  {
      String query;
      if (queryLang.equalsIgnoreCase("itql"))
        query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+"'";
      else if (queryLang.equalsIgnoreCase("sparql"))
      {
      //  query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+"' }";
        String rel = propertyName.substring(propertyName.lastIndexOf('#')+1);
          StringBuffer bf = new StringBuffer("SELECT ?pid ?title ?rel ?val ?contentModel ?guid from <#ri> where {");

          bf.append("?object ?rel ?val");
          bf.append("  . ?object <dc:identifier> ?pid");
          bf.append("  . ?object <dc:identifier> ?guid ");
          bf.append("  . ?object <dc:title> ?title");
          bf.append("  . ?object <fedora-model:hasModel> ?contentModel");

          bf.append(" FILTER( regex(?val,\""+propertyValue+"\")&& regex(?pid,\"^ala\") && (?pid != ?guid)) }");
          query = bf.toString();
      }
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
  }

  /**
   * Constructs an ITQL or SPARQL query from the supplied parameters
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param contentModel restrict the objects returned to those matching this content model
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query as a String
   */
  private static String constructQuery(String propertyName,  String propertyValue, String contentModel, String queryLang) throws FedoraException
  {
      String query;
      if (queryLang.equalsIgnoreCase("itql"))
          query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+
              "' and $object <fedora-model:hasModel> <"+contentModel+">";
      else if (queryLang.equalsIgnoreCase("sparql"))
          query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+
              "' . ?object <fedora-model:hasModel> <"+contentModel+"> . }";
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;

  } // End of `FedoraAPI.constructQuery` method.
      /**
     * Searches the Fedora Resource Index for all objects with matching property value and contentModel
     * @param propertyName type of property to search for
     * @param propertyValue value of the property
     * @param contentModel restrict the objects returned to those matching this model
       * @return the Fedora PIDS of any matching objects
       * @throws IOException
       * @throws FedoraException
     */
   public Collection<String> findObjectsByProperty(String propertyName, String propertyValue, String contentModel) throws  IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = "";
    
    query = constructQuery(propertyName, propertyValue, contentModel,queryLang);
    return findObjectsByQuery(query,queryLang);
  }
  /**
   * Searches the Fedora Resource Index for all objects with matching property values
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @return the Fedora PIDS of any matching objects
   * @throws IOException
   * @throws FedoraException
   */
  public Collection<String> findObjectsByProperty(String propertyName, String propertyValue) throws IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = constructQuery(propertyName, propertyValue,queryLang);
    return findObjectsByQuery(query,queryLang);
  }
  /**
   * Searches the Fedora Resource Index for all objects that matches the supplied query
   * @param query in ITQL or sparql query language
   * @param queryLang must be "itql" or "sparql"
   * @return the Fedora PIDS of any matching objects
   * @throws IOException if there's a problem in the http method
   * @throws FedoraException If an invalid query language selected, or a problem in encoding it
   */
  Collection<String> findObjectsByQuery(String query, String queryLang) throws IOException, FedoraException
  {
      RepositoryFactory rf = RepositoryFactory.getRepositoryFactory();
      
    return rf.getRepository().getFedoraAPI().findObjectsByQuery(query, queryLang);
  }
  /**
   * Searches the Fedora Resource Index for all objects that matches the supplied query
   * @param query in ITQL or sparql query language
   * @param queryLang must be "itql" or "sparql"
   * @return the Fedora PIDS of any matching objects
   * @throws IOException if there's a problem in the http method
   * @throws FedoraException If an invalid query language selected, or a problem in encoding it
   */
  Collection<SearchResult> findRIObjectsByQuery(String query, String queryLang) throws IOException, FedoraException
  {
      RepositoryFactory rf = RepositoryFactory.getRepositoryFactory();

      return rf.getRepository().getFedoraAPI().findRIObjectsByQuery(query, queryLang);
  }

  /**
   *
   * @param propertyName
   * @param propertyValue
   * @param maxResults
   * @return
   * @throws java.io.IOException
   * @throws csiro.diasb.fedora.FedoraException
   */
  public Collection<SearchResult> findRIObjectsByProperty(String propertyName, String propertyValue, int maxResults) throws IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = constructRIQuery(propertyName, propertyValue, maxResults, queryLang);
    return findRIObjectsByQuery(query,queryLang);
  }
    /**
     *
     * @param propertyName
     * @param propertyValue
     * @param contentModel
     * @param maxResults
     * @return
     * @throws java.io.IOException
     * @throws csiro.diasb.fedora.FedoraException
     */
    public Collection<SearchResult> findRIObjectsByProperty(String propertyName, String propertyValue,
          String contentModel, int maxResults) throws IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = constructRIQuery(propertyName, propertyValue, contentModel, maxResults, queryLang);
    return findRIObjectsByQuery(query,queryLang);
  }
    /**
   * Constructs an ITQL or SPARQL query from the supplied parameters
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query  format
   */
  private static String constructRIQuery(String propertyName, String propertyValue, int maxResults, String queryLang) throws FedoraException
  {
      //multiply maxResults by 2 because the current query returns twice as many results as we want - one for
      //each content model. I throw out the non-ala: ones later
      int mResults = maxResults*2;
      String query;
      if (queryLang.equalsIgnoreCase("itql"))
      {
          String msg = "Extended object returns only implemented in sparql";
          throw new FedoraException(msg);
        //query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+"'";
      }
      else if (queryLang.equalsIgnoreCase("sparql"))
      {
        //query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+"' }";
        //  query = "select ?pid ?title ?contentModel ?guid from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+
        //          "' . ?object <fedora-model:hasModel> ?contentModel . ?object <dc:identifier> ?guid . "+
        //          "?object <dc:identifier> ?pid . ?object <dc:title> ?title FILTER((?pid != ?guid) && (?pid != ?guid))}";
          query = "select ?object ?contentModel ?title from <#ri> where { "+
                  "   ?object <"+propertyName+"> ?val "+
                  " . ?object <fedora-model:hasModel> ?contentModel "+
                  " . ?object <dc:title> ?title "+
                  " . FILTER(regex(str(?val), \""+propertyValue+"\",'i'))} +" +
                  " ORDER BY ?title LIMIT "+mResults;
      }
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
  }
  /**
   * Constructs an ITQL or SPARQL query from the supplied parameters, restricting the results to
   * objects satisfying a particular content model
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param contentModel Content model to restrict the results to
   * @param maxResults Maximum # of results to return
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query  format
   * @throws csiro.diasb.fedora.FedoraException
   */
  private static String constructRIQuery(String propertyName, String propertyValue, String contentModel, int maxResults, String queryLang) throws FedoraException
  {
      String query;
      if (queryLang.equalsIgnoreCase("itql"))
      {
          String msg = "Extended object returns only implemented in sparql";
          throw new FedoraException(msg);
        //query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+"'";
      }
      else if (queryLang.equalsIgnoreCase("sparql"))
      {
           query = "select ?object ?contentModel ?title from <#ri> where { ?object <"+propertyName+"> ?val"+
                  " . ?object <fedora-model:hasModel> '"+contentModel+"' "+
                  " . ?object <fedora-model:hasModel> ?contentModel "+
                  " . ?object <dc:title> ?title . FILTER(regex(?val, \""+propertyValue+"\",'i'))}" +
                  " limit "+maxResults;
      }
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
  }
}
