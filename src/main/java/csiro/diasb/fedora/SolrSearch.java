/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  SolrSearch.java 697 2009-08-01 00:23:45Z oak021 $
 */

package csiro.diasb.fedora;

import csiro.diasb.datamodels.FacetResult;
import csiro.diasb.datamodels.FieldResult;
import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.datamodels.SolrResults;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.*;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.SolrInputDocument;

/**
 * Searches the SOLR indices for the repository
 * @author oak021
 */
public class SolrSearch {
   CommonsHttpSolrServer server = null;

   /**
    * Default constructor
    */
   public SolrSearch() {

    }

    /**
     * Returns a pointer to the current faceted query stored in the FedoraAPI object
     * @return
     */
    public FacetQuery getCurrentFacetQuery() {
        return  RepositoryFactory.getRepositoryFactory().getRepository().getFedoraAPI().currentFacetQuery;
    }

   /**
	 * The logger for this class.
   *
   * Using fully qualified name to indicate that this is a Apache Log 4 J
   * Logger, as opposed to JRE's built-in Logger.
   *
	 */
	private static final org.apache.log4j.Logger logger =
        Logger.getLogger(SolrSearch.class.getName());

    /**
     * Removes all SOLR indices and commits the changes
     * @throws org.apache.solr.client.solrj.SolrServerException
     * @throws java.io.IOException
     */
    public void deleteAll() throws SolrServerException, IOException
    {        
        try {
            getServer().deleteByQuery("*:*");
        } catch (SolrServerException ex) {
            throw new SolrServerException(
            "Failed to connect to server during call to delete all SOLR indices",ex);
        } catch (IOException ex) {
            throw new IOException("Failed to delete SOLR indices",ex);
        }
        try {
            getServer().commit();
        }   catch (SolrServerException ex) {
            throw new SolrServerException(
            "Failed to connect to server during commit call",ex);
        } catch (IOException ex) {
            throw new IOException("Failed to delete SOLR indices",ex);
        }
    }

    
    /**
     * Adds three predetermined documents to the SOLR indices
     * @throws org.apache.solr.client.solrj.SolrServerException
     * @throws java.io.IOException
     */
    private void addData() throws SolrServerException, IOException
    {        
      SolrInputDocument doc1 = new SolrInputDocument();
            doc1.addField( "PID", "ala:51ae43e78427b4448923747596372d23", 1.0f );
            doc1.addField( "dc.title", "Marcenia cunctatrix Sjöstedt, 1918", 1.0f );
            doc1.addField( "fgs.contentModel", "ala:TaxonNameContentModel" , 1.0f);

        SolrInputDocument doc2 = new SolrInputDocument();
            doc2.addField( "PID", "ala:97501f3dd55ff93f52cc7f74e0d47f3b", 1.0f );
            doc2.addField( "dc.title", "Blue Unicornfish", 1.0f );
            doc2.addField( "fgs.contentModel", "ala:TaxonNameContentModel", 1.0f );

       SolrInputDocument doc3 = new SolrInputDocument();
            doc3.addField( "PID", "ala:e3f3df42e731f5266800586fb06b2b4c", 1.0f );
            doc3.addField( "dc.title", "Ecnomus pilbarensis Cartwright, 1990", 1.0f );
            doc3.addField( "fgs.contentModel", "ala:TaxonNameContentModel", 1.0f );

        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
            docs.add( doc1 );
            docs.add( doc2 );
            docs.add( doc3 );
        try {
            server.add(docs);
        } catch (SolrServerException ex) {
            throw new SolrServerException(
            "Failed to connect to server during add call",ex);
        } catch (IOException ex) {
            throw new IOException("Failed to add new SOLR indices",ex);
        }
        try {
            server.commit();
        } catch (SolrServerException ex) {
            throw new SolrServerException(
            "Failed to connect to server during commit call",ex);
        } catch (IOException ex) {
            throw new IOException("Failed to add new SOLR indices",ex);
        }
    }

    /**
     * Searches the SOLR indices with the query, and sorting by title
     * in ascending alphabetical order
     * @param q Basic search parameter
     * @param start Result no to start with - this allows pagination
     * @param rows No of results to be returned
     * @return Results from the SOLR search as an array of SearchResult objects
     * @throws csiro.diasb.fedora.SolrQueryException
     */
    public Collection<SearchResult> query(String q, int start, int rows) throws SolrQueryException
    {

        ArrayList<SearchResult> res = new ArrayList<SearchResult>();
       
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setQueryType("standard");
        query.setRows(rows);
        query.setStart(start);
       
        //query.addSortField("dc2.title", SolrQuery.ORDER.asc);
  
        try
        {
            QueryResponse qr = getServer().query(query);

            SolrDocumentList sdl = qr.getResults();

            logger.trace("Found: " + sdl.getNumFound());
            logger.trace("Start: " + sdl.getStart());
            logger.trace("Max Score: " + sdl.getMaxScore());
            logger.trace("--------------------------------");

            ArrayList<HashMap<String, Object>> hitsOnPage = new ArrayList<HashMap<String, Object>>();

            for(SolrDocument d : sdl)
            {
                HashMap<String, Object> values = new HashMap<String, Object>();

                for(Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext(); )
                {
                    Map.Entry<String, Object> e2 = i.next();
                    values.put(e2.getKey(), e2.getValue());
                }

                hitsOnPage.add(values);
                
                String PID = (String) values.get("PID");
                String title = (String) values.get("dc.title");
                String contentModel = (String) values.get("rdf.hasModel");
                SearchResult sr = new SearchResult(PID, title, contentModel);
                res.add(sr);
             }   
        }
        catch (SolrServerException ex) {
            throw new SolrQueryException(
            "Failed to complete query <"+q+"> "+ ex.getMessage());
        }
        return res;
    }

    /**
     * Search the SOLR indices using the supplied facet query
     * @param query Full query to use in searching the SOLR indices
     * @return  Search and facet results in SolrResults format
     * @throws org.apache.solr.client.solrj.SolrServerException
     * @throws csiro.diasb.fedora.SolrQueryException
     */
    public SolrResults getQueryResults(SolrQuery query) throws SolrServerException, SolrQueryException
    {
        QueryResponse qr = getServer().query(query);
        logger.debug("query: "+query.toString());
        return parseSolrResults(qr);
    }
 /**
  * Stores this faceted query in the fedoraAPI so that it is available for facet constraint manipulation
  * and pagination
  * @param query
 */
 public void setCurrentFacetQuery(FacetQuery query)
 {
     RepositoryFactory.getRepositoryFactory().getRepository().getFedoraAPI().currentFacetQuery = query;
 }
 /**
  *
  * @param q Query parameter used to construct a full SOLR query eg PID:ala*
  * @return A FacetQuery including the default facet, sort and highlight fields
  * @throws csiro.diasb.fedora.SolrQueryException
  * @throws org.apache.solr.client.solrj.SolrServerException
  */
 public FacetQuery initFacetedQuery(String q) throws SolrQueryException, SolrServerException
    {
        FacetQuery facetQuery = new FacetQuery();
        facetQuery.setQuery(q);
     
        return facetQuery;
    }
 /**
  * Transforms the response from a SOLR search to SolrResults format
  * @param qr QueryResponse returned from a call to SOLR search
  * @return The response in SolrResults format
  * @throws csiro.diasb.fedora.SolrQueryException
  */
private static SolrResults parseSolrResults(QueryResponse qr) throws SolrQueryException
{
    if (qr == null)
            throw new SolrQueryException("Query response was null");
    SolrDocumentList sdl = qr.getResults();

    SolrResults solrResults = new SolrResults();
    ArrayList<SearchResult> res = new ArrayList<SearchResult>();
    ArrayList<FacetResult> facRes = new ArrayList<FacetResult>();

    logger.trace("Found: " + sdl.getNumFound());
    logger.trace("Start: " + sdl.getStart());
    logger.trace("Max Score: " + sdl.getMaxScore());
    logger.trace("--------------------------------");

    solrResults.setStart(sdl.getStart());
    solrResults.setNResults(sdl.getNumFound());
    ArrayList<HashMap<String, Object>> hitsOnPage = new ArrayList<HashMap<String, Object>>();

    for(SolrDocument d : sdl)
    {
        HashMap<String, Object> values = new HashMap<String, Object>();

        for(Iterator<Map.Entry<String, Object>> i = d.iterator(); i.hasNext(); )
        {
            Map.Entry<String, Object> e2 = i.next();
            values.put(e2.getKey(), e2.getValue());
        }

        hitsOnPage.add(values);

        String PID = (String) values.get("PID");
        String title = (String) values.get("dc.title");
        String contentModel = (String) values.get("rdf.hasModel");
        String rank = (String) values.get("Rank");
        String rankId = (String) values.get("rdf.hasRankId");
        //SearchResult sr = new SearchResult(PID, title, contentModel);
        SearchResult sr = new SearchResult(PID, title, rank, rankId);
        sr.setContentModel(contentModel);
        //look for highlighting
        Map<String, List<String>> hlItem = qr.getHighlighting().get(PID);
        if (hlItem != null && !hlItem.isEmpty())
            sr.setHighLights(hlItem.entrySet());
        res.add(sr);
    }


    List<FacetField> facets = qr.getFacetFields();

    if (facets != null)
    {
        for(FacetField facet : facets)
        {
            List<FacetField.Count> facetEntries = facet.getValues();

            if ((facetEntries != null) && (facetEntries.size()>0))
            {
                ArrayList<FieldResult> r = new ArrayList<FieldResult>();
                for(FacetField.Count fcount : facetEntries)
                {
                    String msg = fcount.getName() + ": " + fcount.getCount();
                    logger.trace(fcount.getName() + ": " + fcount.getCount());
                    r.add(new FieldResult(fcount.getName(),fcount.getCount()));
                }
                FacetResult fr = new FacetResult(facet.getName(),r);
                facRes.add(fr);
            }
        }
    }

    solrResults.setSearchResults(res);
    solrResults.setFacetResults(facRes);

    //The query result is stored in its original format so that all the information returned
    //is available later on if needed
    solrResults.setQr(qr);

    return solrResults;
}

/**
 * returns a pointer to the SOLR server singleton stored in fedoraAPI
 * @return
 */
    public CommonsHttpSolrServer getServer() {
     if (server==null)
     {
        server =  RepositoryFactory.getRepositoryFactory().getRepository().getFedoraAPI().initSolrServer();
     }
        return server;
    }

    public void setServer(CommonsHttpSolrServer server) {
        this.server = server;
    }

    /**
     * Performs a SOLR search based on the supplied query. No highlight or facet information is available.
     * @param propertyName Name of the field to search
     * @param queryValue Value of the field to match
     * @param start Result # to start with (for pagination of results)
     * @param rows # of results to return
     * @return The search results in SearchResult format
     * @throws csiro.diasb.fedora.SolrQueryException
     */
    public  Collection<SearchResult> query(String propertyName, String queryValue,int start,int rows) throws SolrQueryException
    {
      String q = new String(propertyName + ":" + queryValue);
      return query(q,start,rows);
    }
    /**
     * Creates a new FacetQuery based on a basic search for the property value supplied
     * @param propertyName Name of the field to search
     * @param queryValue Value of the field to match
     * @return The created FacetQuery object
     * @throws csiro.diasb.fedora.SolrQueryException
     * @throws org.apache.solr.client.solrj.SolrServerException
     */
    public FacetQuery initFacetedQuery(String propertyName, String queryValue) throws SolrQueryException, SolrServerException
    {
      String q = new String(escapeComponent(propertyName) + ":" + escapeComponent(queryValue));
      return initFacetedQuery(q);
    }   
/**
   * Encodes the passed String as UTF-8 using an algorithm that
   * passes through * without touching it, and adds a backslash to :
   * there must be a better way of doing this, as I need to escape more chars
   *  (without escaping *)
   *
   * @param s The String to be encoded
 * @return the encoded String
 * @throws SolrQueryException
   */
  public static String encodeComponent(String s) throws SolrQueryException
  {
    String result = null;

    try
    {
      String enc = URLEncoder.encode(s, "UTF-8")
                         .replaceAll("\\+", "%20")
                         .replaceAll("\\%21", "!")
                         .replaceAll("\\%27", "'")
                         .replaceAll("\\%28", "(")
                         .replaceAll("\\%29", ")")
                         .replaceAll("\\%7E", "~");
//list of chars that need to be escaped:  + - & || ! ( ) { } [ ] ^ " ~ * ? : \
        result = enc.replace("%3A","\\:");
    }

    // This exception should never occur.
    catch (UnsupportedEncodingException e)
    {
      result = s;
      throw new SolrQueryException("Error encoding query component");
    }

    return result;
  }

  /**
   * Adds a backslash before any of the following characters: + - & || ! ( ) { } [ ] ^ " ~  : \
   * @param s Input string
   * @return The escaped string
   */
 private String  escapeComponent(String s)
    {
        //list of chars that need to be escaped:  + - & || ! ( ) { } [ ] ^ " ~ * ? : \
        String e = s.replace("+","\\+").replace("-","\\-").replace("&","\\&").replace("||","\\||");
        String f = e.replace("!","\\!").replace("(","\\(").replace(")","\\)").replace("{","\\{").replace("}","\\}");
        e =  f.replace("[","\\[").replace("]","\\]").replace("^","\\^").replace("\"","\\\"");
      //  f =  e.replace("~","\\~").replace("*","\\*").replace("?","\\?").replace(":","\\:").replace("\\","\\\\");
        //I've removed * and ? from here because this escaping is done after I've added wildcards
        f =  e.replace("~","\\~").replace(":","\\:").replace("\\","\\\\");
       return f;
    }  
}