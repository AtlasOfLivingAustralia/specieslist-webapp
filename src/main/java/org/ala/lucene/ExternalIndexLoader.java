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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Load tool for loading external data into the indexes to provide a combined
 * single index for the BIE front end.
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
				"classpath*:spring-profiler.xml",
				"classpath*:spring-external-ds.xml",
				"classpath*:spring.xml"
		};
		
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		ExternalIndexLoader l = (ExternalIndexLoader) context.getBean(ExternalIndexLoader.class);
		
		//load collections
		l.loadCollections();
		
		//load institutions
		l.loadInstitutions();

		//load data providers
		l.loadDataProviders();
		
		//load datasets
		l.loadDatasets();

        // load WordPress pages
        CreateWordPressIndex cwpi = (CreateWordPressIndex) context.getBean(CreateWordPressIndex.class);
        logger.info("Start of crawling and indexing WP pages.");
        cwpi.loadSitemap();
        cwpi.indexPages();
		
		System.exit(0);
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
			solrServer.commit();
		}
		
		solrServer.optimize();
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
			solrServer.commit();
		}
		
		solrServer.optimize();
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
				"inner join data_provider dp ON dp.id=dr.data_provider_id ");
		
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
			solrServer.commit();
		}
		
		solrServer.optimize();
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
			solrServer.commit();
		}
		
		solrServer.optimize();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing data provider information.");
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
