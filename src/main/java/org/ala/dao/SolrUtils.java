package org.ala.dao;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.core.CoreContainer;
import org.springframework.stereotype.Component;
/**
 * Centralised SOLR initialisation and common functions.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("solrUtils")
public class SolrUtils {
	public static final Version BIE_LUCENE_VERSION = Version.LUCENE_34;
	
	private String solrHome = "/data/solr/bie";
	
    /** SOLR server instance */
    private EmbeddedSolrServer server = null;
	
	/**
     * Initialise the SOLR server instance
     */
    public SolrServer getSolrServer() throws Exception {
        if (this.server == null & solrHome != null) {
	        System.setProperty("solr.solr.home", solrHome);
	        CoreContainer coreContainer = null;
	        try {
		        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		        coreContainer = initializer.initialize();
	        } catch (Exception e) {
	        	//FIXME this is a hack - there must be a better way of initialising SOLR here
	        	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(BIE_LUCENE_VERSION, new StandardAnalyzer(BIE_LUCENE_VERSION));
	        	Directory dir = FSDirectory.open(new File(solrHome+"/index")); 
	        	IndexWriter idxWriter = new IndexWriter(dir, indexWriterConfig);
//	        	IndexWriter idxWriter = new IndexWriter(solrHome+"/index", new StandardAnalyzer());
		        idxWriter.commit();
		        idxWriter.close();
		        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		        coreContainer = initializer.initialize();
	        }
	        this.server = new EmbeddedSolrServer(coreContainer, "");
        }
        return server;
    }
    
    /**
	 * @param solrHome the solrHome to set
	 */
	public void setSolrHome(String solrHome) {
		this.solrHome = solrHome;
	}
	
	public static String cleanName(String name){        
        String patternA = "[^a-zA-Z]";
    	/* replace multiple whitespaces between words with single blank */
    	String patternB = "\\b\\s{2,}\\b";
    	
    	String cleanQuery = "";
    	if(name != null){
    		cleanQuery = ClientUtils.escapeQueryChars(name);//.toLowerCase();
    		cleanQuery = cleanQuery.toLowerCase();
	    	cleanQuery = cleanQuery.replaceAll(patternA, " ");
	    	cleanQuery = cleanQuery.replaceAll(patternB, " ");
	    	cleanQuery = cleanQuery.trim();
    	}
    	return cleanQuery;
    }
	
	public static String concatName(String name){        
        String patternA = "[^a-zA-Z]";
    	/* replace multiple whitespaces between words with single blank */
    	String patternB = "\\b\\s{2,}\\b";
    	
    	String cleanQuery = "";
    	if(name != null){
    		cleanQuery = ClientUtils.escapeQueryChars(name);//.toLowerCase();
    		cleanQuery = cleanQuery.toLowerCase();
	    	cleanQuery = cleanQuery.replaceAll(patternA, "");
	    	cleanQuery = cleanQuery.replaceAll(patternB, "");
	    	cleanQuery = cleanQuery.trim();
    	}
    	return cleanQuery;
    }
	
}
