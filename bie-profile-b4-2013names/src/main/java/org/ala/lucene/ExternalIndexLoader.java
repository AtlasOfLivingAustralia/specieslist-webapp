/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.lucene;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
import org.ala.documentmapper.Mapping;
import org.ala.repository.Predicates;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Load tool for loading external data into the indexes to provide a combined
 * single index for the BIE front end.
 *
 * TODO Replace the database links with loading via webservices
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("externalIndexLoader")
public class ExternalIndexLoader {

	protected static Logger logger = Logger.getLogger(ExternalIndexLoader.class);
	
	@Inject
	protected DataSource collectoryDataSource;
	@Inject
	protected SolrUtils solrUtils;
	
	protected String baseUrlForCollectory = "http://collections.ala.org.au/public/show/";

	/**
	 * Run the loading of indexes for institutions, collections, data providers and data resources
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		String[] locations = {
				"classpath*:spring.xml"
		};
		
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		ExternalIndexLoader l = (ExternalIndexLoader) context.getBean(ExternalIndexLoader.class);
		
        l.loadRegions();

        //load collections
        l.loadCollections();

        //load institutions
        l.loadInstitutions();

        //load data providers
        l.loadDataProviders();

        //load datasets
        l.loadDatasets();

        //load layers
        l.loadLayers();

        // load WordPress pages
        CreateWordPressIndex cwpi = (CreateWordPressIndex) context.getBean(CreateWordPressIndex.class);
        logger.info("Start of crawling and indexing WP pages.");
        cwpi.loadSitemap();
        cwpi.indexPages();

		System.exit(0);
	}

    public void loadLayers() throws Exception {

        SolrServer solrServer = solrUtils.getSolrServer();
        solrServer.deleteByQuery("idxtype:"+IndexedTypes.LAYERS); // delete layers!

        HttpClient httpClient = new HttpClient();
        GetMethod gm = new GetMethod("http://spatial.ala.org.au/layers-service/layers.json");
        logger.debug("Response code for get method: " +httpClient.executeMethod(gm));
        String layerJson = gm.getResponseBodyAsString();
        ObjectMapper om = new ObjectMapper();
        List<Map<String,Object>> layers = om.readValue(layerJson, new TypeReference<List<Map<String,Object>>>() {});
        for(Map<String,Object> layer: layers){
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("name", layer.get("displayname"));
            doc.addField("guid", "http://spatial.ala.org.au/layers/more/"+layer.get("name"));
            doc.addField("description", layer.get("notes"));
            doc.addField("text", layer.get("source"));
            doc.addField("text", layer.get("type"));
            doc.addField("text", layer.get("notes"));
            doc.addField("text", layer.get("keywords"));
            if(layer.get("classification1") !=null) doc.addField("text", layer.get("classification1"));
            if(layer.get("classification2") !=null) doc.addField("text", layer.get("classification2"));
            doc.addField("content", layer.get("notes"));
            doc.addField("dataProviderName", layer.get("source"));
            doc.addField("url", "http://spatial.ala.org.au/layers/more/"+layer.get("name"));
            doc.addField("id", layer.get("id"));
            doc.addField("idxtype", IndexedTypes.LAYERS);
            doc.addField("australian_s", "recorded"); // so they appear in default QF search
            solrServer.add(doc);
        }
        logger.info("Finished syncing layer information with the collectory.");
        solrServer.commit();
        logger.info("Finished syncing layers.");
    }

	/**
	 * Loads collections and institutions into the BIE search index.
	 * 
	 * @throws Exception
	 */
	public void loadCollections() throws Exception {
		
		logger.info("Starting syncing collection information....");
		Connection conn = collectoryDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select uid, guid, name, acronym, collection_type, pub_description, sub_collections, keywords from collection");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.COLLECTION); // delete collections!
		
		while (rs.next()) {
			String uid = rs.getString("uid");
			String externalGuid = rs.getString("guid");
			String name = rs.getString("name");
			String acronym = rs.getString("acronym");
			String description = rs.getString("pub_description");
			String subCollections = rs.getString("sub_collections");
			String keywords = rs.getString("keywords");
			String collectionType = rs.getString("collection_type");
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("acronym", acronym, 1.2f);
			doc.addField("name", name, 1.2f);
			doc.addField("guid", baseUrlForCollectory+uid);
			
			doc.addField("otherGuid", uid); // the internal UID e.g. co1
			if(externalGuid!=null){
				doc.addField("otherGuid", externalGuid); // the external GUID e.g. url:lsid:bci:123
			}
			
			//add as text
			doc.addField("text", description);
			doc.addField("text", subCollections);
			doc.addField("text", keywords);
			doc.addField("text", collectionType);
			
			doc.addField("url", baseUrlForCollectory+uid);
			doc.addField("id", baseUrlForCollectory+uid);
			doc.addField("idxtype", IndexedTypes.COLLECTION);
//			doc.addField("aus_s", "yes");
			doc.addField("australian_s", "recorded"); // so they appear in default QF search

			solrServer.add(doc);
		}

        solrServer.commit();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing collection information with the collectory.");
	}
	
	/**
	 * Loads collections and institutions into the BIE search index.
	 * 
	 * @throws Exception
	 */
	public void loadInstitutions() throws Exception {
		
		logger.info("Starting syncing institution information....");
		Connection conn = collectoryDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select uid, guid, name, acronym, institution_type, pub_description from institution");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.INSTITUTION); // delete institutions!
		
		while (rs.next()) {
			String uid = rs.getString("uid");
			String externalGuid = rs.getString("guid");
			String name = rs.getString("name");
			String acronym = rs.getString("acronym");
			String institutionType = rs.getString("institution_type"); // university/museum/government
			String pubDescription = rs.getString("pub_description"); 
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("acronym", acronym, 1.2f);
			doc.addField("name", name, 1.2f);
			doc.addField("guid", baseUrlForCollectory+uid);
			
			if(externalGuid!=null){
				doc.addField("otherGuid", externalGuid);
			}
			
			doc.addField("text", pubDescription);
			doc.addField("url", baseUrlForCollectory+uid);
			doc.addField("id", baseUrlForCollectory+uid);
			doc.addField("idxtype", IndexedTypes.INSTITUTION);
			doc.addField("institutionType", institutionType);
//			doc.addField("aus_s", "yes");
			doc.addField("australian_s", "recorded"); // so they appear in default QF search
			solrServer.add(doc);
		}

        solrServer.commit();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing institution information.");
	}
	
	/**
	 * Load the datasets
	 * 
	 * @throws Exception
	 */
	public void loadDatasets() throws Exception {
		
		logger.info("Starting syncing data resource information....");
		Connection conn = collectoryDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select dr.uid, dr.guid, dr.name, dr.acronym as acronym, dp.name as data_provider_name, dr.pub_description as description " +
				"from data_resource dr " +
				"left join data_provider dp ON dp.id=dr.data_provider_id ");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.DATASET);
		
		while (rs.next()) {
			String uid = rs.getString("uid");
			String externalGuid = rs.getString("guid");			
			String name = rs.getString("name");
			String acronym = rs.getString("acronym");
			String dataProviderName = rs.getString("data_provider_name");
			String description = rs.getString("description"); 
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", baseUrlForCollectory+uid);
			doc.addField("url", baseUrlForCollectory+uid);
			doc.addField("id", baseUrlForCollectory+uid);
			doc.addField("name", name);
			if(externalGuid!=null){
				doc.addField("otherGuid", externalGuid);
			}
			if(acronym!=null){
				doc.addField("acronym", acronym);
			}
			doc.addField("dataProviderName", dataProviderName);
			doc.addField("description", description);
			doc.addField("idxtype", IndexedTypes.DATASET);
//			doc.addField("aus_s", "yes");
			doc.addField("australian_s", "recorded"); // so they appear in default QF search
			solrServer.add(doc);
		}

        solrServer.commit();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing data resource information.");
	}
	
	/**
	 * Load the data providers
	 * 
	 * @throws Exception
	 */
	public void loadDataProviders() throws Exception {
		
		logger.info("Started syncing data provider information....");
		Connection conn = collectoryDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select dp.uid, dp.name, dp.pub_description as description from data_provider dp");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.DATAPROVIDER);
		
		while (rs.next()) {
			String uid = rs.getString("uid");
			String name = rs.getString("name");
			String description = rs.getString("description"); 
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", baseUrlForCollectory+uid);
			doc.addField("url", baseUrlForCollectory+uid);
			doc.addField("id", baseUrlForCollectory+uid);
			doc.addField("name", name);
			doc.addField("description", description);
			doc.addField("idxtype", IndexedTypes.DATAPROVIDER);
//			doc.addField("aus_s", "yes");
			doc.addField("australian_s", "recorded"); // so they appear in default QF search
			solrServer.add(doc);
		}

        solrServer.commit();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing data provider information.");
	}

	// =======================
	private String getTagValue(String sTag, Element eElement) {
		NodeList nl = eElement.getElementsByTagName(sTag);
		if(nl == null || nl.getLength() < 1){
			return null;
		}
		
		NodeList nlList = nl.item(0).getChildNodes();	 
	    Node nValue = (Node) nlList.item(0);	 
	    return nValue.getNodeValue();
	}
	
	public void loadRegions() throws Exception {
		int ctr = 0;
		logger.info("Started syncing regions information....");
		
		// init....
		HttpClient httpClient = new HttpClient();
		GetMethod gm = new GetMethod("http://regions.ala.org.au/data/sitemap.xml");
		logger.debug("Response code for get method: " +httpClient.executeMethod(gm));        
		InputStream responseStream = gm.getResponseBodyAsStream();
		
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);

		DocumentBuilder parser = dbFactory.newDocumentBuilder();
		Document document = parser.parse(responseStream);
		NodeList nList = document.getElementsByTagName("url");
		
		// start process....
		if(nList.getLength() > 0){
			SolrServer solrServer = solrUtils.getSolrServer();
			solrServer.deleteByQuery("idxtype:"+IndexedTypes.REGION);
			for (int temp = 0; temp < nList.getLength(); temp++) {	 
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;
					String loc = getTagValue("loc", eElement);
					if(loc != null){
						String url = java.net.URLDecoder.decode(loc, "UTF-8");
						String[] terms = url.split("/");
						if(terms.length > 1){
							String name = terms[terms.length -1];
							String region = terms[terms.length - 2];
							if(name != null && region != null){
								SolrInputDocument doc = new SolrInputDocument();
								doc.addField("idxtype", IndexedTypes.REGION);
								doc.addField("guid", url);
								doc.addField("id", url);
								doc.addField("url", url);
								doc.addField("regionType", region);
								doc.addField("name", name);
                                doc.addField("text", name);
                                doc.addField("australian_s", "recorded"); // so they appear in default QF search
								solrServer.add(doc);
								ctr++;
							}						
						}
					}			      
				}
			}
			solrServer.commit();
		}
		logger.info("Finished syncing regions information. Total count: " + ctr);
	}

    /**
     * @param collectoryDataSource the collectoryDataSource to set
     */
    public void setCollectoryDataSource(DataSource collectoryDataSource) {
        this.collectoryDataSource = collectoryDataSource;
    }

    /**
     * @param solrUtils the solrUtils to set
     */
    public void setSolrUtils(SolrUtils solrUtils) {
        this.solrUtils = solrUtils;
    }

    /**
     * @return the collectoryDataSource
     */
    public DataSource getCollectoryDataSource() {
        return collectoryDataSource;
    }

    /**
     * @param baseUrlForCollectory the baseUrlForCollectory to set
     */
    public void setBaseUrlForCollectory(String baseUrlForCollectory) {
        this.baseUrlForCollectory = baseUrlForCollectory;
    }
}
