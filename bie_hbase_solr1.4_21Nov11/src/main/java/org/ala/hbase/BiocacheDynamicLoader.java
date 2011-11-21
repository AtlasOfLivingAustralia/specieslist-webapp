package org.ala.hbase;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.ala.dao.SolrUtils;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class loads data from the new biocache dynamically through WS calls to the biocache
 * 
 * 
 * 
 * @author Natasha Carter (Natasha.Carter@csiro.au)
 */
@Component("biocacheDynamicLoader")
public class BiocacheDynamicLoader {
    @Inject
        protected TaxonConceptDao taxonConceptDao;
    @Inject
    protected SolrUtils solrUtils;
    private long start = System.currentTimeMillis();
    protected static Logger logger  = Logger.getLogger(BiocacheDynamicLoader.class);
    private String suffix = "http://biocache.ala.org.au/ws/occurrences/facets/download?q=lat_long:%5B*+TO+*%5D&count=true&facets=";     
    private String[] geoLoads = new String[]{"species_guid", "genus_guid","family","order","class","phylum","kingdom"};
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        BiocacheDynamicLoader l = context.getBean(BiocacheDynamicLoader.class);
        l.load(5);
        System.exit(0);
    }
    public void load(int workers){
        //so we need to load all the information from WS's
        HttpClient httpClient = new HttpClient();
        ArrayBlockingQueue<String[]> lsidQueue = new ArrayBlockingQueue<String[]>(500);
        List<SolrInputDocument>docs = Collections.synchronizedList(new ArrayList<SolrInputDocument>(100));
        IndexingThread primaryThread = new IndexingThread(lsidQueue, docs, 1);
        new Thread(primaryThread).start();
        IndexingThread[] otherThreads = new IndexingThread[workers-1];
        int i =0;
        while(workers >1){
            IndexingThread it = new IndexingThread(lsidQueue, docs, workers--);
            otherThreads[i++] =it;
            new Thread(it).start();
           
        }
        for(String load : geoLoads){
            String loadUrl = suffix + load;
            logger.info("Starting to reload " + loadUrl);
                   
            try{                
                GetMethod gm = new GetMethod(loadUrl); 
                logger.info("Response code for get method: " +httpClient.executeMethod(gm));
                
                CSVReader reader = new CSVReader(new InputStreamReader(gm.getResponseBodyAsStream(), gm.getResponseCharSet()));
                String[] values = reader.readNext();
                logger.info("values: " + values.length);
                boolean lookup = !(values[0].contains("lsid") || values[0].contains("guid"));
                primaryThread.setLookup(lookup);
                for(IndexingThread it : otherThreads)
                    it.setLookup(lookup);
                values =reader.readNext();
                while(values!= null){
                    if(values.length == 2){
                        String lsid = lookup?taxonConceptDao.findLsidByName(values[0]):values[0];
                        if(lsid != null && lsid.length()>0){                            
                            lsidQueue.put(values);
                        }
                    }
                    values = reader.readNext();
                   
                }
                while(!lsidQueue.isEmpty()){
                    //logger.debug(">>>>>>The lsid queue has " + lsidQueue.size());
                    try{
                        Thread.currentThread().sleep(50);
                    }
                    catch(Exception e){
                    
                    }
                }
                //after each level has been processed commit the index
                primaryThread.commit();              
            }
            catch(Exception e){
                logger.error("Unable to reload " + geoLoads[0], e);
            }
        }
        
        
    }

    /**
     * 
     * TODO move this out so it is more generic for other load purposes
     *
     */
    private class IndexingThread implements Runnable{
        private BlockingQueue<String[]> lsidQueue;
        private SolrServer solrServer = null;
        private List<SolrInputDocument> docs = null;
        private boolean isPrimary =false;
        private boolean lookup = false;
        private int id;
        private int count =0;
        private long lastStart = System.currentTimeMillis(); 
        IndexingThread(BlockingQueue<String[]> queue, List<SolrInputDocument> docs, int num){
            lsidQueue=queue;
            isPrimary = num==1;
            id = num;
            this.docs = docs;
            if(isPrimary){
                try{
                    solrServer = solrUtils.getSolrServer();
                }
                catch(Exception e){
                    
                }
            }
        }
        public void commit(){
            try{ 
                
                index();
                solrServer.commit();
                logger.info("Finished loading");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public void setLookup(boolean lu){
            this.lookup = lu;
        }
        private void index(){
            try{
                //logger.debug("sending " + docs.size() + " to the index");
                synchronized(docs){
                    if(docs.size() >0){
                        count+=docs.size();
                        logger.info("Adding items " + docs.size() + " to index");
                        solrServer.add(docs);
                    }
                    
                    long end = System.currentTimeMillis();
                logger.info(count
                        + " >> "
                        + ", records per sec: " + ((float)docs.size()) / (((float)(end - lastStart)) / 1000f)
                        + ", time taken for "+docs.size()+" records: " + ((float)(end - lastStart)) / 1000f
                        + ", total time: "+ ((float)(end - start)) / 60000f +" minutes");
                logger.info("");
                docs.clear();
                lastStart = System.currentTimeMillis();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            while(true){
                if(lsidQueue.size()>0){
                    String value[] = lsidQueue.poll();
                    try{                      
                      String lsid = lookup?taxonConceptDao.findLsidByName(value[0]):value[0];
                      if(lsid != null){
                          logger.debug(id+">>Indexing " + lsid);
                          Integer count= Integer.parseInt(value[1]);
                          logger.debug("Updating: " + value[0] +" : " + count);
                          taxonConceptDao.setGeoreferencedRecordsCount(lsid, count);
                          docs.addAll(taxonConceptDao.indexTaxonConcept(lsid));
                      }
                      else{
                          logger.warn("Unable to locate " + value);
                      }
                    }
                    catch(Exception e){
                        logger.warn("Unable to generate index documents for " + value);
                    }
                    if(docs.size()>=100 && isPrimary)
                        index();
                }
                else{
                    try{
                        Thread.currentThread().sleep(50);
                    }
                    catch(Exception e){}
                }
            }
        }
    }
    
}
