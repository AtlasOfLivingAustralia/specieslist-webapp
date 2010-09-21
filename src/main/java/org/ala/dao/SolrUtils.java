package org.ala.dao;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.stereotype.Component;
/**
 * Centralised SOLR initialisation and common functions.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("solrUtils")
public class SolrUtils {

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
		        IndexWriter idxWriter = new IndexWriter(solrHome+"/index", new StandardAnalyzer());
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

	/**
	 * @return the server
	 */
	public SolrServer getServer() {
		return server;
	}
}
