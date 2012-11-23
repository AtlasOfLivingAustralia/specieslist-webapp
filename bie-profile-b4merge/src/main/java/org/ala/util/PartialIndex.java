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
package org.ala.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.ala.dao.FulltextSearchDao;
import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import javax.inject.Inject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Takes a file of LSIDs and reindexes the resource.
 */
@Component("partialIndex")
public class PartialIndex {
	/** Logger initialisation */
	private final static Logger logger = Logger.getLogger(PartialIndex.class);
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	@Inject
	protected FulltextSearchDao searchDao;
	@Inject
	protected SolrUtils solrUtils;	
	
	private SolrServer solrServer =null;

	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		PartialIndex loader = context.getBean(PartialIndex.class);
		
		try {
			if (args.length != 1) {
				System.out.println("Please provide a file with a list of lsid....");
				System.exit(0);
			}
			loader.process(args[0]);
		}
		catch(Exception e){			
			e.printStackTrace();
			System.out.println(e);
			System.exit(0);	
		}
		//System.exit(0);	
	}

	public void process(String args) throws Exception {
		int ctr = 0;
		File file = new File(args);
		FileInputStream fstream = new FileInputStream(file);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
//			loader.cleanIndex();
		ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(100);
		IndexingThread[] threads = new IndexingThread[20];
		for(int i = 0; i<20;i++){
		    IndexingThread t = new IndexingThread(i, queue);
		    threads[i]=t;
		    t.start();
		}
		String strLine;
		if(solrServer == null){
            solrServer = solrUtils.getSolrServer();
        }
		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
			// Print the content on the console
			strLine = strLine.trim();
			if(strLine.length() > 0){
				//doIndex(strLine);
			    while(!queue.offer(strLine))
			        Thread.currentThread().sleep(50);
				ctr++;
			}
		}
		for(IndexingThread t : threads){
		    t.setCanStop();	
		    t.join();
		}
		//Close the input stream
		in.close();
		
		commitIndex();
		solrUtils.shutdownSolr();
		logger.info("**** total count: " + ctr);
	}
	
	public void cleanIndex() throws Exception{
		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}
		logger.info("Clearing existing taxon entries in the search index...");
		solrServer.deleteByQuery("idxtype:" + IndexedTypes.TAXON); // delete																		// everything!
		solrServer.commit();		
	}

	public void commitIndex() throws Exception{
		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}
		solrServer.commit();		
	}

	public void doIndex(String guid) throws Exception{
		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}
		logger.debug("***** doIndex guid: " + guid);
		List<SolrInputDocument> docs = taxonConceptDao.indexTaxonConcept(guid);
		if(docs.size()>0)
		    solrServer.add(docs);		
	}
	
	private class IndexingThread extends Thread{
	    private ArrayBlockingQueue<String> queue;
	    private int id, total ;
	    boolean stop = false;
	    boolean canStop = false;
	    IndexingThread(int id,ArrayBlockingQueue<String> queue){
	        logger.info("Creating Index Thread " + id);
	        this.queue = queue;
	        this.id = id;
	    }
	    
	    void setCanStop(){
	        canStop=true;
	    }
	    
	    public void run(){
	        while(!stop){
	            try{
	                if(canStop && queue.size()==0){
	                    stop = true;
	                    logger.info("Index Thread: " + id + " is stopping" );
	                }
	                else{
    	            String guid = queue.poll(500,TimeUnit.MILLISECONDS);
    	            total++;
    	            List<SolrInputDocument> docs = taxonConceptDao.indexTaxonConcept(guid);
    	            if(docs.size()>0)
    	                solrServer.add(docs);
    	            if(total%100== 0){
                        logger.info("Indexer " + id + " has indexed " + total + " last key: " + guid);
                    }
	                }
	            
	            }	           
	            catch(Exception e){
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
}
