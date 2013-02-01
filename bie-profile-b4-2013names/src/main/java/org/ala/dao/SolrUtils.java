package org.ala.dao;


import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
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
    private CoreContainer coreContainer = null;
    private AddDocThread[] threads = null;
    ArrayBlockingQueue<List<SolrInputDocument>> queue = null;
    
    private int numThreads = 4; 
	
	/**
     * Initialise the SOLR server instance
     */
    public SolrServer getSolrServer() throws Exception {
        if (this.server == null & solrHome != null) {
	        System.setProperty("solr.solr.home", solrHome);
	        logger.info("SOLR HOME : " + solrHome);
	        coreContainer = null;
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
    public void shutdownSolr(){
        coreContainer.shutdown();
    }
    /**
     * Reopens the SOLR core container to allow a refresh of the index from external processes.
     * @throws Exception
     */
    public void reopenSolr() throws Exception{
        coreContainer.reload("");
    }
    
    
    public Set<IndexFieldDTO> getIndexFieldDetails(String... fields) throws Exception{
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/admin/luke");
        
        params.set("tr", "luke.xsl");
        if(fields != null){
            params.set("fl" ,fields);
            params.set("numTerms", "1");
        }
        else
            params.set("numTerms", "0");        
        QueryResponse response = server.query(params);
        Set<IndexFieldDTO>  results = parseLukeResponse(response.toString(), fields != null);
        
        return results;
    }
    
    /**
     * parses the response string from the service that returns details about the indexed fields
     * @param str
     * @return
     */
    private  Set<IndexFieldDTO> parseLukeResponse(String str, boolean includeCounts) {
        //System.out.println(str);
        Set<IndexFieldDTO> fieldList = includeCounts?new java.util.LinkedHashSet<IndexFieldDTO>():new java.util.TreeSet<IndexFieldDTO>();
        
        Pattern typePattern = Pattern.compile(
        "(?:type=)([a-z]{1,})");

        Pattern schemaPattern = Pattern.compile(
        "(?:schema=)([a-zA-Z\\-]{1,})");
        
        Pattern distinctPattern = Pattern.compile(
        "(?:distinct=)([0-9]{1,})");

        String[] fieldsStr = str.split("fields=\\{");

        for (String fieldStr : fieldsStr) {
            if (fieldStr != null && !"".equals(fieldStr)) {
                String[] fields = includeCounts?fieldStr.split("\\}\\},"):fieldStr.split("\\},");

                for (String field : fields) {
                    if (field != null && !"".equals(field)) {
                        IndexFieldDTO f = new IndexFieldDTO();
                        
                        String fieldName = field.split("=")[0];
                        String type = null;
                        String schema = null;
                        Matcher typeMatcher = typePattern.matcher(field);
                        if (typeMatcher.find(0)) {
                            type = typeMatcher.group(1);
                        }
                        
                        Matcher schemaMatcher = schemaPattern.matcher(field);
                        if (schemaMatcher.find(0)) {
                            schema = schemaMatcher.group(1);
                        }
                        if(schema != null){
                            logger.debug("fieldName:" + fieldName);
                            logger.debug("type:" + type);
                            logger.debug("schema:" + schema);
                            //don't allow the sensitive coordinates to be exposed via ws
                            if(fieldName != null && !fieldName.startsWith("sensitive")){
                                
                                f.setName(fieldName);
                                f.setDataType(type);
                                //interpret the schema information
                                f.setIndexed(schema.contains("I"));
                                f.setStored(schema.contains("S"));
                                
                                fieldList.add(f);
                            }
                        }
                        Matcher distinctMatcher = distinctPattern.matcher(field);
                        if(distinctMatcher.find(0)){
                            Integer distinct = Integer.parseInt(distinctMatcher.group(1));
                            f.setNumberDistinctValues(distinct);
                        }
                    }
                }
            }
        }
        
        return fieldList;
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
	 * Changes the direction of the sort based in "score" because a user would assume that a
	 * default ordering should have the highest score first.
	 * 
	 * @param sort
	 * @param dir
	 * @return
	 */
	public static String getSortDirection(String sort, String dir){
	    String direction = dir;
        if("score".equals(sort)){
                if("asc".equals(dir))
                    direction = "desc";
                else
                    direction = "asc";
            }
        return direction;
	}
	
	/**
	 * Stops the threads for indexing. Allows the program to exit gracefully.
	 * @throws Exception
	 */
	public void stopIndexing() throws Exception {
	    //wait until the queue is empty then stop all the threads
	    while(!queue.isEmpty()){
	        Thread.currentThread().sleep(100);
	    }
	    for(AddDocThread thread :threads){
	        thread.stopRunning();
	    }
	    //issue the last commit
        logger.info("stopIndexing - committing index");
	    server.commit();
        logger.info("stopIndexing - committed");
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
	
	/**
	 * DTO for the fields that belong to the index.
	 * 
	 * A field is available for faceting if indexed=true 
	 * 
	 * @author "Natasha Carter <Natasha.Carter@csiro.au>"
	 */
	public class IndexFieldDTO implements Comparable<IndexFieldDTO> {
	    /** The name of the field in the index */
	    private String name;
	    /** The SOLR data type for the field */
	    private String dataType;
	    /** True when the field is available in the index for searching purposes */
	    private boolean indexed;
	    /** True when the field is available for extraction in search results */
	    private boolean stored;
	    /** Stores the number of distinct values that are in the field */
	    private Integer numberDistinctValues;
	    
	    @Override
	    public boolean equals(Object obj){
	        if(obj instanceof IndexFieldDTO && name != null){
	            return name.equals(((IndexFieldDTO)obj).getName());
	        }
	        return false;
	    }
	    
	    /**
	     * @return the name
	     */
	    public String getName() {
	        return name;
	    }
	    /**
	     * @param name the name to set
	     */
	    public void setName(String name) {
	        this.name = name;
	    }
	    /**
	     * @return the dataType
	     */
	    public String getDataType() {
	        return dataType;
	    }
	    /**
	     * @param dataType the dataType to set
	     */
	    public void setDataType(String dataType) {
	        this.dataType = dataType;
	    }
	    /**
	     * @return the indexed
	     */
	    public boolean isIndexed() {
	        return indexed;
	    }
	    /**
	     * @param indexed the indexed to set
	     */
	    public void setIndexed(boolean indexed) {
	        this.indexed = indexed;
	    }
	    /**
	     * @return the stored
	     */
	    public boolean isStored() {
	        return stored;
	    }
	    /**
	     * @param stored the stored to set
	     */
	    public void setStored(boolean stored) {
	        this.stored = stored;
	    }
	    /**
	     * @return the numberDistinctValues
	     */
	    public Integer getNumberDistinctValues() {
	        return numberDistinctValues;
	    }
	    /**
	     * @param numberDistinctValues the numberDistinctValues to set
	     */
	    public void setNumberDistinctValues(Integer numberDistinctValues) {
	        this.numberDistinctValues = numberDistinctValues;
	    }

	    @Override
	    public int compareTo(IndexFieldDTO other) {        
	        return this.getName().compareTo(other.getName());
	    }
	    
	    
	}

	
}
