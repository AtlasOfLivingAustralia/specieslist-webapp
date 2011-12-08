package org.ala.dao;


import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
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
	static Logger logger = Logger.getLogger(SolrUtils.class);
	private String solrHome = "/data/solr/bie";
	
    /** SOLR server instance */
    private EmbeddedSolrServer server = null;
    
    private AddDocThread[] threads = null;
    ArrayBlockingQueue<List<SolrInputDocument>> queue = null;
    
    private int numThreads =4; 
	
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
	/**
	 * Stops the threads for indexing. Allows the program to exit gracefully.
	 * @throws Exception
	 */
	public void stopIndexing() throws Exception {
	    //wait until the queue is empty then stop all the threads
	    while(queue.size()>0){
	        Thread.currentThread().sleep(100);
	    }
	    for(AddDocThread thread :threads){
	        thread.stopRunning();
	    }
	    //issue the last commit
	    server.commit();
	    threads = null;
	    queue = null;
	}
	/**
	 * Adds a list of documents to the SOLR server in a threaded manner.
	 * @param docs
	 * @throws Exception
	 */
	public void addDocs(List<SolrInputDocument> docs) throws Exception{
	    if(threads == null){
	        queue = new ArrayBlockingQueue<List<SolrInputDocument>>(10);	        
	        //ArrayBlockingQueue<List<SolrInputDocument>> queue = new ArrayBlockingQueue<List<SolrInputDocument>>(5);
	        threads = new AddDocThread[numThreads];
	        for(int i =0;i<numThreads;i++){
    	        AddDocThread thread = new AddDocThread(i,queue);	        
    	        thread.start();
    	        threads[i] = thread;
	        }
	    }
	    queue.put(docs);

	}
	/**
	 * A thread that adds solr documents to the index. 
	 *
	 */
	private class AddDocThread extends Thread{
	    private static final int MAX_BATCH = 10;
	    ArrayBlockingQueue<List<SolrInputDocument>> queue = new ArrayBlockingQueue<List<SolrInputDocument>>(MAX_BATCH);
	    int id =-1;
	    boolean shouldRun = true;
	    AddDocThread(int id, ArrayBlockingQueue<List<SolrInputDocument>> queue){
	        this.queue = queue;
	        this.id = id;
	    }
	    void stopRunning(){
	        shouldRun = false;
	    }
	    
	    public void run(){
	        while(shouldRun){
	            if(queue.size()>0){
	                
	                List<SolrInputDocument> docs = queue.poll();
	                //add and commit the docs
	                if (docs != null && !docs.isEmpty()) {
	                    try{
	                    logger.info("Thread " + id + " is adding " + docs.size() + " documents to the index.");
                        server.add(docs);
                        //only the first thread should commit
                        if(id == 0)
                            server.commit();
                        docs =null;
	                    }
	                    catch(Exception e){
	                        
	                    }
                        
                    }
	            }
	            else{
	                try{
	                Thread.sleep(250);
	                }
	                catch(Exception e){}
	            }
	        }
	    }
	}
	
}
