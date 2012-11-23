package org.ala.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dao.Scanner;
import org.ala.dao.SolrUtils;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 
 * remove empty/blank image from cassandra & repo file and reindex.
 * 
 * Based on the CassandraBatchDelete.java 
 *
 */
@Component("removeEmptyImageUtil")
public class RemoveEmptyImageUtil {
    protected Logger logger = Logger.getLogger(this.getClass());

    @Inject
    protected StoreHelper storeHelper;
	@Inject
	protected TaxonConceptDao taxonConceptDao;    
    @Inject
	protected SolrUtils solrUtils;	
	
	private SolrServer solrServer =null;
	
    protected org.ala.util.ImageUtils imageUtils;
    
    public static final int ROWS = 1000;
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String POOL_NAME = "ALA__RemoveEmptyImageUtil";
    public static final String PREFIX = "--";
    public static final String HOST_PREFIX = "-host=";
    public static final String PORT_PREFIX = "-port=";
    public static final String START_PREFIX = "-start=";
    public static final String RK_PREFIX = "-rk";
    
    private String columnName = "hasImage";
        
    /**
     * Remove blank images
     * 
     * Usage: [infoSourceId...]
     * eg: 1013 1003
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        RemoveEmptyImageUtil cbd = context.getBean(RemoveEmptyImageUtil.class);
        List<String> infoSrcIdList = new ArrayList<String>();

        String startKey ="";
        
        if (args.length < 1) {
            System.out.println("Please provide a list of infoSourceIds ....");
            System.exit(0);
        }
        
        //setup args option list
        for(int i = 0; i < args.length; i++){
        	infoSrcIdList.add(args[i].trim());            
        }
        
        cbd.doFullScanAndDelete("tc", args, startKey);
        System.exit(0);
    }
    
    
    /**
     * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
     * then delete this column. 
     * @param infoSourceIds 
     * @throws Exception
     */
    public void doFullScanAndDelete(String columnFamily,String[] infoSourceIds, String startKey) throws Exception {

        //Now get the scanner based on the columns
        Scanner scanner =storeHelper.getScanner(columnFamily, columnFamily, startKey, columnName);
        byte[] guidAsBytes = null;
        int i= 0;
        long start = System.currentTimeMillis();
        long startTime = start;
        long finishTime = start;
        while ((guidAsBytes = scanner.getNextGuid()) != null) {
            i++;
            String guid = new String(guidAsBytes, "UTF-8");
            if(infoSourceIds.length>0){
 	            String value = (String)scanner.getValue(columnName, String.class);
	            if(value != null && hasInfoSourceId(value, infoSourceIds)){
	                //update the value of the string
	                value = updateJSONString(value, infoSourceIds);
	                if(value.length()>0){
	                    //update the existing value for the column
	                    storeHelper.updateStringValue(columnFamily,columnFamily,columnName, guid, value);
	                }
	                else{
	                    //no values remaining for the column so remove it
	                    storeHelper.deleteColumns(columnFamily, guid, columnName);
	                }
		            //update solr index
		            doIndex(guid);
	            }
            }
            
            if(i%1000 == 0){
                finishTime = System.currentTimeMillis();
                logger.info(i +" >>> Last key " + guid 
                        + ", records per sec: " + 1000f/(((float)(finishTime - startTime)) / 1000f)
                        + ", time taken for 1000 records: " + ((float)(finishTime - startTime)) / 1000f
                        + ", total time: "+ ((float)(finishTime - start)) / 60000f +" minutes"
                        );
                
                startTime = System.currentTimeMillis();
                
            }
        }
        commitIndex();
    }
    
    private String updateJSONString(String value, String[] infoSourceIds){
        List<JsonNode> objectList = new ArrayList<JsonNode>();
        ObjectMapper mapper = new ObjectMapper();       
        JsonNode rootNode;
        try {           
            rootNode = mapper.readValue(value, JsonNode.class);
            if(!rootNode.isArray()){
                if(!(hasInfoSourceId(rootNode, infoSourceIds))){
                    return value;
                }
            }
            else{
            	if(imageUtils == null){
            		imageUtils = new ImageUtils();
            	}

                JsonNode next = null;
                Iterator<JsonNode> it = rootNode.iterator();
                while(it.hasNext()){
                    next = it.next();
                    if(!(hasInfoSourceId(next, infoSourceIds))){                
                        objectList.add(next);
                    }
                    else {
                    	String loc = getImageRepoLocation(next);
                    	boolean notFound = false;
                    	boolean empty = false;
                    	try{   
                    		File file = new File(loc);
                    		empty = imageUtils.imageIsBlank(file);
                    	}
                    	catch(Exception ex){
                    		notFound = true;
                    		logger.error("*** open file: " + loc + "\n" + ex);
                    	}

                    	// empty image ?
                    	if(empty){
                    		// delete repo directory
                    		String dirStr = loc.substring(0, loc.lastIndexOf('/'));
                    		File dir = null;
                        	try{   
                        		dir = new File(dirStr);                              	
                            	if(dir != null){
    	                    		org.apache.commons.io.FileUtils.deleteDirectory(dir);
    	                    		logger.info("*** delete directory: " + dir);
    	                        }
                        	}
                        	catch(Exception ex1){
                        		logger.error("*** open dir: " + dir + "\n" + ex1);
                        	}
                    	}
                    	else if(notFound){
                    		// remove entry from cassandra but no directory remove action
                    		logger.info("*** file not found: " + notFound);
                    	}
                    	else{
                    		objectList.add(next);
                    	}   
                    }
                }
                if(objectList.size() > 0){
                    return mapper.writeValueAsString(objectList);
                }
                else
                    return "";
            }                       
        } catch (Exception e) {
            logger.info("doDelete(): " + e.toString());
        }
        return value;
    }

    private String getImageRepoLocation(JsonNode rootNode){
        return rootNode.path("repoLocation").getTextValue();
    }
       
    private boolean hasInfoSourceId(JsonNode rootNode, String[] infoSourceIds){
        String s = rootNode.path("infoSourceId").getTextValue();
        for(String infoSourceId : infoSourceIds){           
            if(infoSourceId.equals(s)){
                return true;
            }
        }
        return false;
    }
    
    private boolean hasInfoSourceId(String value, String[] infoSourceIds){
        // no infoSourceIds condition then return true 
        if(infoSourceIds == null || infoSourceIds.length < 1){
            return true;
        }
        
        for(String infoSourceId : infoSourceIds){
            Pattern p = Pattern.compile("\"infoSourceId\":\\s*\"" + infoSourceId + "\"");
            Matcher m = p.matcher(value);
            if (m.find()){
                return true;
            }
        }
        return false;
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
}
