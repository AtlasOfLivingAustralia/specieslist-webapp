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

public class ExternalIndexLoader {

	ApplicationContext context;
	DataSource collectoryDataSource;
	JdbcTemplate cTemplate;
	SolrUtils solrUtils;
	
	public static void main(String[] args) throws Exception {
		
		ExternalIndexLoader l = new ExternalIndexLoader();
		l.init();
		
		//load collection data
		l.loadCollections();
		
		//load institution data
		
		
		//load regions 
		
		
		//load localities
		
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
//			String guid = rs.getString("guid");
			String name = rs.getString("name");
			String acronym = rs.getString("acronym");
			String institutionType = rs.getString("institution_type"); // university/museum/government
			String groupType = rs.getString("group_type"); // Collection/Institution (Enum)
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("guid", "http://www.ala.org.au/nhc/"+id);
			doc.addField("url", "http://www.ala.org.au/nhc/"+id);
			doc.addField("id", "http://www.ala.org.au/nhc/"+id);
			doc.addField("acronym", acronym);
			doc.addField("name", name);
			if("Collection".equalsIgnoreCase(groupType)){
				doc.addField("idxtype", IndexedTypes.COLLECTION);
			} else if ("Institution".equalsIgnoreCase(groupType)){
				doc.addField("idxtype", IndexedTypes.INSTITUTION);
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
