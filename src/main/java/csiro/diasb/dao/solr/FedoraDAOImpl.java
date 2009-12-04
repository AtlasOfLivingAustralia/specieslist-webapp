/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package csiro.diasb.dao.solr;

import csiro.diasb.dao.FedoraDAO;
import csiro.diasb.datamodels.HtmlPageDTO;
import csiro.diasb.datamodels.ImageDTO;
import csiro.diasb.datamodels.SearchResult;
import csiro.diasb.datamodels.TaxonNameDTO;
import csiro.diasb.fedora.RepositoryFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * DAO implementation for accessing FC object data via SOLR
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class FedoraDAOImpl implements FedoraDAO {
    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(FedoraDAOImpl.class.getName());
    /** SOLR server instance */
    private CommonsHttpSolrServer server = null;
    /** URL of the SOLR servlet */
    protected static String solrUrl = "http://diasbdev1-cbr.vm.csiro.au:8080/solr";  // http://localhost:8080/solr

    /**
     * Constructor - set the server field
     */
    public void FedoraDAOImpl() {
        initSolrServer();
    }

    /**
     * For a given list of TN guids, query SOLR and return a list of TaxonNameDTOs
     *
     * @param taxonNameIds
     * @return list of TaxonNameDTOs
     */
    @Override
    public List<TaxonNameDTO> getTaxonNamesForUrns(List<String> taxonNameIds) {
        List<TaxonNameDTO> tns = new ArrayList<TaxonNameDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(taxonNameIds, "dc.identifier",  "ContentModel:ala.TaxonNameContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");
            
            if (contentModel.equalsIgnoreCase("ala.TaxonNameContentModel")) {
                // Only want to request these values if the returned document is the expected type
                String pid = (String) doc.getFieldValue("PID");
                String title = (String) doc.getFieldValue("dc.title");
                String name = (String) doc.getFieldValue("rdf.hasNameComplete");
                String rank = (String) doc.getFieldValue("Rank");
                String guid = (String) doc.getFieldValue("rdf.hasGuid");
                List sourceList = (List) doc.getFieldValues("rdf.sameAs");
                String source = (String) sourceList.get(0); // TODO: Fix this code to check all values and pick out the one we want
                // populate bean with variables
                TaxonNameDTO tn = new TaxonNameDTO(pid, title, name, rank, guid, source);
                // Add the bean to the list
                tns.add(tn);
            }
        }
        
        return tns;
    }

    /**
     * Return a list of ImageDTOs for a given list of scientific names by querying
     * SOLR index.
     *
     * @param scientificNames the list of scientific names used to serach against
     * @return imageDTOs the list of ImageDTO objects populated with reults from SOLR search
     */
    @Override
    public List<ImageDTO> getImagesForScientificNames(ArrayList<String> scientificNames) {
        List <ImageDTO> imageDTOs = new ArrayList<ImageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.ImageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.ImageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                ImageDTO image = new ImageDTO();
                image.setPid((String) doc.getFieldValue("PID"));
                image.setGuid((String) doc.getFieldValue("rdf.hasGuid"));
                image.setTitle((String) doc.getFieldValue("dc.title"));
                image.setDescription((String) doc.getFieldValue("dc.description"));
                image.setCountry((String) doc.getFieldValue("Country"));
                image.setRegion((String) doc.getFieldValue("Region"));
                image.setLatitude((String) doc.getFieldValue("rdf.latitude"));
                image.setLongitude((String) doc.getFieldValue("rdf.longitude"));
                image.setPhotoPage((String) doc.getFieldValue("rdf.hasPhotoPage"));
                image.setPhotoSourceUrl((String) doc.getFieldValue("rdf.hasPhotoSourceUrl"));
                image.setScientificName((String) doc.getFieldValue("rdf.hasScientificName"));
                // Add the bean to the list
                imageDTOs.add(image);
            }
        }

        return imageDTOs;
    }

    /**
     * Return a list of ImageDTOs for a given list of scientific names by querying
     * SOLR index.
     *
     * @param scientificNames the list of scientific names used to serach against
     * @return imageDTOs the list of ImageDTO objects populated with reults from SOLR search
     */
    @Override
    public List<HtmlPageDTO> getHtmlPagesForScientificNames(ArrayList<String> scientificNames) {
        List <HtmlPageDTO> htmlPageDTOs = new ArrayList<HtmlPageDTO>();
        SolrDocumentList sdl = null;
        try {
            sdl = doListQuery(scientificNames, "rdf.hasScientificName", "ContentModel:ala.HtmlPageContentModel");
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server." + ex.getMessage(), ex);
            return null;
        }

        for (SolrDocument doc : sdl) {
            String contentModel = (String) doc.getFieldValue("ContentModel");

            if (contentModel.equalsIgnoreCase("ala.HtmlPageContentModel")) {
                // Only want to request these values if the returned document is the expected type
                HtmlPageDTO htmlPage = new HtmlPageDTO();
                htmlPage.setPid((String) doc.getFieldValue("PID"));
                htmlPage.setGuid((String) doc.getFieldValue("rdf.hasGuid"));
                htmlPage.setTitle((String) doc.getFieldValue("dc.title"));
                htmlPage.setScientificName((String) doc.getFieldValue("rdf.hasScientificName"));
                htmlPage.setUrl((String) doc.getFieldValue("rdf.hasURL"));
                htmlPage.setSource((String) doc.getFieldValue("fgs.label"));
                // optional fields stored in HashMap
                Map fieldMap = doc.getFieldValueMap();
                HashMap<String, String> rdfProperties = new HashMap<String, String>();

                for (Object keyObj : fieldMap.keySet()) {
                    // Cast key to String
                    try {
                        String key = (String) keyObj;
                        
                        if (key.startsWith("rdf")) {
                            String value = (String) fieldMap.get(keyObj);
                            rdfProperties.put(key, value);
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing SOLR values: " + e.getMessage());
                    }
                }

                htmlPage.setRdfProperties(rdfProperties);
                
                // Add the bean to the list
                htmlPageDTOs.add(htmlPage);
            }
        }

        return htmlPageDTOs;
    }

    /**
     * Look-up a PID for a given LSID
     *
     * @param lsid
     * @return pid
     */
    @Override
    public String getPidForLsid(String lsid) {
        String pid = null;
        try {
            String safeQuery = ClientUtils.escapeQueryChars(lsid);
            SolrQuery searchQuery = new SolrQuery(); // handle better?
            searchQuery.setQuery("dc.identifier:" + safeQuery);
            // do the Solr search
            if (server == null) this.initSolrServer();
            QueryResponse qr = null;
            qr = server.query(searchQuery); // can throw exception
            SolrDocumentList sdl = qr.getResults();

            if (!sdl.isEmpty()) {
                SolrDocument doc = sdl.get(0); // assume 1 result
                pid = (String) doc.getFieldValue("PID");
            }
        } catch (SolrServerException ex) {
            logger.warn("Problem communicating with SOLR server. " + ex.getMessage(), ex);
        }
        return pid;
    }

    /**
     * Helper method to format SOLR query (Boolean expression) and perform the SOLR search
     *
     * @param fieldValues
     * @param fieldName
     * @param contentModelFilter 
     * @return SolrDocumentList
     * @throws SolrServerException
     */
    protected SolrDocumentList doListQuery(List<String> fieldValues, String fieldName, String contentModelFilter)
            throws SolrServerException {

        StringBuffer query = new StringBuffer();
        Iterator it = fieldValues.iterator();

        while (it.hasNext()) {
            String safeQueryStr = ClientUtils.escapeQueryChars(it.next().toString());
            query.append(fieldName);
            query.append(":");
            query.append(safeQueryStr);
            if (it.hasNext()) {
                query.append(" OR ");
            }
        }

        if (!contentModelFilter.isEmpty()) {
            // add optional contentModel filter, which requires parentheses around existing query
            // so that we get the expected presedence for Boolean expression
            query.insert(0,"(");
            query.append(") AND " + contentModelFilter);
        }

        SolrQuery searchQuery = new SolrQuery();  // handle better?
        searchQuery.setQuery(query.toString());
        logger.info("SOLR query: " + query.toString());
        // do the Solr search
        if (server == null) this.initSolrServer();
        QueryResponse qr = null;
        qr = server.query( searchQuery ); // can throw exception

        return qr.getResults();
    }

    /**
     * returns a pointer to the SOLR server singleton stored in fedoraAPI
     * @return
     */
    public CommonsHttpSolrServer initSolrServer() {
        if (this.server == null & solrUrl != null) {
            try {
                this.server = new CommonsHttpSolrServer(solrUrl);
            } catch (MalformedURLException e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        return this.server;
    }

    /* 
     * Getters & Setters
     */
    
    public CommonsHttpSolrServer getServer() {
        return server;
    }

    public void setServer(CommonsHttpSolrServer server) {
        this.server = server;
    }

    public static String getSolrUrl() {
        return solrUrl;
    }

    public static void setSolrUrl(String solrUrl) {
        FedoraDAOImpl.solrUrl = solrUrl;
    }

}
