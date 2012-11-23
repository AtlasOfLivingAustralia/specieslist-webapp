package org.ala.util;

import org.ala.dao.Scanner;
import org.ala.dao.SolrUtils;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Image;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Increase the ranking of images from a certain dataset
 */
@Component("boostImageRankUtil")
public class BoostImageRankUtil {
    protected Logger logger = Logger.getLogger(this.getClass());

    @Inject
    protected StoreHelper storeHelper;
	@Inject
	protected TaxonConceptDao taxonConceptDao;    

    protected org.ala.util.ImageUtils imageUtils;
    
    public static final int ROWS = 1000;
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String POOL_NAME = "ALA__BoostImageRankUtil";
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
        BoostImageRankUtil cbd = context.getBean(BoostImageRankUtil.class);
        //List<String> infoSrcIdList = new ArrayList<String>();
        if(args!=null && args.length==1){
            System.out.println("Starting boost for images matching UID: " + args[0]);

            cbd.doFullScanAndBoost("tc", args[0], "");

        }

        System.exit(0);

    }

    /**
     * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
     * then delete this column. 
     * @param infoSourceId
     * @throws Exception
     */
    public void doFullScanAndBoost(String columnFamily,String infoSourceId, String startKey) throws Exception {

        //Now get the scanner based on the columns
        Scanner scanner = storeHelper.getScanner(columnFamily, columnFamily, startKey, columnName);
        byte[] guidAsBytes = null;
        int i= 0;
        long start = System.currentTimeMillis();
        long startTime = start;
        long finishTime = start;
        while ((guidAsBytes = scanner.getNextGuid()) != null) {
            i++;
            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
            //om.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
            String guid = new String(guidAsBytes, "UTF-8");
            //if(infoSourceIds.length>0){
 	            String value = (String) scanner.getValue(columnName, String.class);
               // System.out.println(value);
                if(value != null && value.contains(infoSourceId)){
                    System.out.println(guid);
                    //parse the JSON
                    List<Image> images = om.readValue(value, TypeFactory.collectionType(ArrayList.class, Image.class));
                    for(Image image:images){
                        if(image.getInfoSourceUid()!=null && image.getInfoSourceUid().equals(infoSourceId)){
                            image.setPreferred(true);
                            image.setRanking(99999);
                        }
                    }
                    Collections.sort(images);
                    String imageAsString = om.writeValueAsString(images);
                    storeHelper.updateStringValue(columnFamily,columnFamily,columnName, guid, imageAsString);
                }
        }
    }
}

