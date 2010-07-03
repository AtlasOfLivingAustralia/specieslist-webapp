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
package org.ala.hbase;

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
	protected DataSource bioCacheDataSource;
	@Inject
	protected SolrUtils solrUtils;
	
	protected String baseUrlForCollections = "http://collections.ala.org.au/public/show/";
	protected String baseUrlForDataProviders = "http://data.ala.org.au/dataproviders/";
	protected String baseUrlForDatasets = "http://data.ala.org.au/datasets/";
	
	public static void main(String[] args) throws Exception {
		
		String[] locations = {
				"classpath*:spring-profiler.xml",
				"classpath*:spring-external-ds.xml",
				"classpath*:spring.xml"
		};
		
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		ExternalIndexLoader l = (ExternalIndexLoader) context.getBean(ExternalIndexLoader.class);
		
		//load collection data + institution data
		l.loadCollections();

		//load data providers
		l.loadDataProviders();
		
		//load datasets
		l.loadDatasets();
		
		System.exit(0);
	}

	/**
	 * Loads collections and institutions into the BIE search index.
	 * 
	 * @throws Exception
	 */
	public void loadCollections() throws Exception {
		
		logger.info("Starting syncing collection and institution information....");
		Connection conn = collectoryDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select id, guid, name, acronym, institution_type, group_type from provider_group");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.COLLECTION); // delete collections!
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.INSTITUTION); // delete institutions!
		
		while (rs.next()) {
			String id = rs.getString("id");
			String name = rs.getString("name");
			String acronym = rs.getString("acronym");
			String institutionType = rs.getString("institution_type"); // university/museum/government
			String groupType = rs.getString("group_type"); // Collection/Institution (Enum)
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", baseUrlForCollections+id);
			doc.addField("url", baseUrlForCollections+id);
			doc.addField("id", baseUrlForCollections+id);
			doc.addField("acronym", acronym);
			doc.addField("name", name);
			if("Collection".equalsIgnoreCase(groupType)){
				doc.addField("idxtype", IndexedTypes.COLLECTION);
				//add the institution information
			} else if ("Institution".equalsIgnoreCase(groupType)){
				doc.addField("idxtype", IndexedTypes.INSTITUTION);
				//add the number of collections information
			}
			doc.addField("institutionType", institutionType);
			solrServer.add(doc);
			solrServer.commit();
		}
		
		solrServer.optimize();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing collection and institution information.");
	}
	
	public void loadDatasets() throws Exception {
		
		logger.info("Starting syncing dataset information....");
		Connection conn = bioCacheDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select dr.id, dr.name as name, dp.name as data_provider_name, dr.description, dr.basis_of_record " +
				"from data_resource dr " +
				"inner join data_provider dp ON dp.id=dr.data_provider_id " +
				"where dr.occurrence_count > 0");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.DATASET);
		
		while (rs.next()) {
			String id = rs.getString("id");
			String name = rs.getString("name");
			String dataProviderName = rs.getString("data_provider_name");
			String description = rs.getString("description"); 
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", baseUrlForDatasets+id);
			doc.addField("url", baseUrlForDatasets+id);
			doc.addField("id", baseUrlForDatasets+id);
			doc.addField("name", name);
			doc.addField("dataProviderName", dataProviderName);
			doc.addField("description", description);
			doc.addField("idxtype", IndexedTypes.DATASET);
			solrServer.add(doc);
			solrServer.commit();
		}
		
		solrServer.optimize();
		rs.close();
		stmt.close();
		conn.close();
		logger.info("Finished syncing dataset information.");
	}
	
	public void loadDataProviders() throws Exception {
		
		logger.info("Started syncing data provider information....");
		Connection conn = bioCacheDataSource.getConnection();
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select dp.id, dp.name, dp.description, dp.occurrence_count, dp.data_resource_count " +
				"from data_provider dp " +
				"where dp.occurrence_count>0");
		
		SolrServer solrServer = solrUtils.getSolrServer();
		solrServer.deleteByQuery("idxtype:"+IndexedTypes.DATASET);
		
		while (rs.next()) {
			String id = rs.getString("id");
			String name = rs.getString("name");
			String description = rs.getString("description"); 
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", baseUrlForDataProviders+id);
			doc.addField("url", baseUrlForDataProviders+id);
			doc.addField("id", baseUrlForDataProviders+id);
			doc.addField("name", name);
			doc.addField("description", description);
			doc.addField("idxtype", IndexedTypes.DATAPROVIDER);
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
	 * @param baseUrlForCollections the baseUrlForCollections to set
	 */
	public void setBaseUrlForCollections(String baseUrlForCollections) {
		this.baseUrlForCollections = baseUrlForCollections;
	}

	/**
	 * @param baseUrlForDataProviders the baseUrlForDataProviders to set
	 */
	public void setBaseUrlForDataProviders(String baseUrlForDataProviders) {
		this.baseUrlForDataProviders = baseUrlForDataProviders;
	}

	/**
	 * @param baseUrlForDatasets the baseUrlForDatasets to set
	 */
	public void setBaseUrlForDatasets(String baseUrlForDatasets) {
		this.baseUrlForDatasets = baseUrlForDatasets;
	}
}
