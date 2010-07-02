	package org.ala.hbase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
/**
 * Load tool for loading external data into the indexes to provide a combined
 * single index for the BIE front end.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ExternalIndexLoader {

	protected ApplicationContext context;
	protected DataSource collectoryDataSource;
	protected JdbcTemplate cTemplate;
	protected SolrUtils solrUtils;
	protected String baseUrlForCollections = "http://collections.ala.org.au/public/show/";
	
	public static void main(String[] args) throws Exception {
		
		ExternalIndexLoader l = new ExternalIndexLoader();
		l.init();
		
		//load collection data
		l.loadCollections();
		
		//load institution data
		
		//load regions 
		
		//load localities

		//load data providers
		
		//load datasets
		
		System.exit(0);
	}
	
	/**
	 * Initialise DB connections.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		
		String[] locations = {
				"classpath*:spring-profiler.xml",
				"classpath*:spring-external-ds.xml",
				"classpath*:spring.xml"
		};
		
		//initialise the datasource
		context = new ClassPathXmlApplicationContext(locations);
		collectoryDataSource = (DataSource) context.getBean("collectoryDataSource");
		cTemplate = new JdbcTemplate(collectoryDataSource);
		solrUtils = (SolrUtils) context.getBean("solrUtils");
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
}
