	package org.ala.hbase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
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
		ExternalIndexLoader l = (ExternalIndexLoader) context.getBean("externalIndexLoader");
		
		//load collection data + institution data
//		l.loadCollections();
		
		//load portal data
		
		
		//load regions
		
		//load localities

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
		System.out.println("Finishing syncing collection and institution information.");
	}
	
	private void loadDatasets() throws Exception {
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
		System.out.println("Finishing syncing dataset information.");
	}
	
	private void loadDataProviders() throws Exception {
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
		System.out.println("Finishing syncing data provider information.");
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
