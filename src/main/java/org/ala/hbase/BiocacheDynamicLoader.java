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
    protected static Logger logger  = Logger.getLogger(BiocacheDynamicLoader.class);
    private String[] geoLoads = new String[]{"http://biocache.ala.org.au/ws/occurrences/facets/download?q=data_resource_uid:dr344&facets=taxon_concept_lsid&count=true",
            "http://biocache.ala.org.au/ws/occurrences/facets/download?q=data_resource_uid:dr344&facets=family&count=true"};//,"http://biocache.ala.org.au/ws/occurrences/facets/download?q=latitude:[* TO *]&facets=taxon_concept_lsid&count=true", "http://biocache.ala.org.au/ws/occurrences/facets/download?q=latitude:[* TO *]&facets=species_guid&count=true"};
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        BiocacheDynamicLoader l = context.getBean(BiocacheDynamicLoader.class);
        if(args.length>0)
            l.geoLoads = args;
        l.load(5);
        System.exit(0);
    }
    public void load(int workers){
        //so we need to load all the information from WS's
        HttpClient httpClient = new HttpClient();
        ArrayBlockingQueue<String> lsidQueue = new ArrayBlockingQueue<String>(500);
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
        for(String loadUrl : geoLoads){
            logger.info("Starting to reload " + loadUrl);
            GetMethod gm = new GetMethod(loadUrl);        
            try{
                httpClient.executeMethod(gm);
                CSVReader reader = new CSVReader(new InputStreamReader(gm.getResponseBodyAsStream(), gm.getResponseCharSet()));
                String[] values = reader.readNext();
                boolean lookup = !(values[0].contains("lsid") || values[0].contains("guid"));
                primaryThread.setLookup(lookup);
                for(IndexingThread it : otherThreads)
                    it.setLookup(lookup);
                values =reader.readNext();
                while(values!= null){
                    if(values.length == 2){
                        String lsid = lookup?taxonConceptDao.findLsidByName(values[0]):values[0];
                        if(lsid != null && lsid.length()>0){
                            //update the value of the count
                            Integer count= Integer.parseInt(values[1]);
                            //logger.debug("Updating: " + values[0] +" : " + count);
                            taxonConceptDao.setGeoreferencedRecordsCount(values[0], count);
                            //now add it to the lsidQueue
                            lsidQueue.put(values[0]);
                        }
                    }
                    values = reader.readNext();
                   
                }
                
                
            }
            catch(Exception e){
                logger.error("Unable to reload " + geoLoads[0], e);
            }
        }
        while(!lsidQueue.isEmpty()){
                //logger.debug(">>>>>>The lsid queue has " + lsidQueue.size());
            try{
                Thread.currentThread().sleep(50);
            }
            catch(Exception e){
                
            }
        }
        primaryThread.shutdown();
    }

    /**
     * 
     * TODO move this out so it is more generic for other load purposes
     *
     */
    private class IndexingThread implements Runnable{
        private BlockingQueue<String> lsidQueue;
        private SolrServer solrServer = null;
        private List<SolrInputDocument> docs = null;
        private boolean isPrimary =false;
        private boolean lookup = false;
        private int id;
        IndexingThread(BlockingQueue<String> queue, List<SolrInputDocument> docs, int num){
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
        public void shutdown(){
            try{                
                index();
                solrServer.commit();
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
                    solrServer.add(docs);                    
                    docs.clear();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            while(true){
                if(lsidQueue.size()>0){
                    String value = lsidQueue.poll();
                    try{                      
                      String lsid = lookup?taxonConceptDao.findLsidByName(value):value;
                      if(lsid != null){
                          //logger.debug(id+">>Indexing " + lsid);
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
