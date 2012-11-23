package org.ala.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.hbase.ALANamesLoader;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 
 * A batch delete from cassandra making use of the CassandrPelopsHelper class.
 * 
 * Based on the original batch delete located int src/test/java/org/ala/apps
 * 
 * @author Natasha Carter
 *
 */
@Component("cassandraBatchDelete")
public class CassandraBatchDelete {
    protected Logger logger = Logger.getLogger(this.getClass());

    @Inject
    protected StoreHelper storeHelper;
    
    public static final int ROWS = 1000;
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String POOL_NAME = "ALA__CassandraBatchDelete";
    public static final String PREFIX = "--";
    public static final String HOST_PREFIX = "-host=";
    public static final String PORT_PREFIX = "-port=";
    public static final String START_PREFIX = "-start=";
    public static final String RK_PREFIX = "-rk";
    
    private String host = "localhost";
    private int port = 9160;
    private String keyspace = "bie";
    private String columnFamily = "tc"; 
        
    /**
     * 
     * Usage: [-start=guid][--ColumnName...] [infoSourceId...]
     * 
     * eg: --hasImage --hasRegion 1013
     * remove infoSourceId data from particular column [hasImage & hasRegion].
     * 
     * eg: --hasImage --hasRegion
     * if infoSourceId is empty then remove whole column that equal to input columnName
     * 
     * eg: 1013
     * if columnName is empty then remove infoSource data from all columns.
     * 
     * eg -start=urn:lsid:biodiversity.org.au:apni.taxon:53229 --hasConservationStatus
     * removes the conservationStatus column from all records starting with urn:lsid:biodiversity.org.au:apni.taxon:53229
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        CassandraBatchDelete cbd = context.getBean(CassandraBatchDelete.class);
        List<String> columnNameList = new ArrayList<String>();
        List<String> infoSrcIdList = new ArrayList<String>();
        String host = "localhost";
        int port = 9160;
        boolean _rk = false;
        String startKey ="";
        
        if (args.length < 1) {
            System.out.println("Please provide a list of infoSourceIds or columnNames....");
            System.exit(0);
        }
        
        //setup args option list
        for(int i = 0; i < args.length; i++){
            String tmp = args[i].trim();
            if(tmp.startsWith(PREFIX)){
                columnNameList.add(tmp.substring(PREFIX.length()));
            }
            else if(tmp.startsWith(HOST_PREFIX)){
                host = tmp.substring(HOST_PREFIX.length());
            }
            else if(tmp.startsWith(PORT_PREFIX)){
                port = Integer.parseInt(tmp.substring(PORT_PREFIX.length()));
            }
            else if(tmp.startsWith(RK_PREFIX)){
                _rk = true;
            }
            else if(tmp.startsWith(START_PREFIX)){
                startKey = tmp.substring(START_PREFIX.length());
            }
            else{
                infoSrcIdList.add(tmp);
            }
        }
        
        cbd.doFullScanAndDelete("tc", infoSrcIdList.toArray(new String[]{}), columnNameList.toArray(new String[]{}),startKey);
        System.exit(0);
    }
    
    
    /**
     * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
     * then delete this column. 
     * @param infoSourceIds 
     * @throws Exception
     */
    public void doFullScanAndDelete(String columnFamily,String[] infoSourceIds, String[] columnNames,String startKey) throws Exception {
        //When the user deosn't specify the columns to delete from use ALL columns
        boolean columnsSupplied = true;
        if(columnNames.length<1){
            columnNames = ColumnType.columnsToIndex;
            columnsSupplied=false;
        }
        //Now get the scanner based on the columns
        Scanner scanner =storeHelper.getScanner(columnFamily, columnFamily, startKey, columnNames);
        byte[] guidAsBytes = null;
        int i= 0;
        long start = System.currentTimeMillis();
        long startTime = start;
        long finishTime = start;
        while ((guidAsBytes = scanner.getNextGuid()) != null) {
            i++;
            String guid = new String(guidAsBytes, "UTF-8");
            //if the infosources are empty just remove the entire value for the supplied columns
            if(infoSourceIds.length<1 && columnNames.length>0){
                //removing the whole list of columns
                storeHelper.deleteColumns(columnFamily, guid, columnNames);
            }
            else if(infoSourceIds.length>0){
                //delete the info source from all the columns that were requested. 
                for(String column: columnNames){
                    String value = (String)scanner.getValue(column, String.class);
                    if(value != null && hasInfoSourceId(value, infoSourceIds)){
                        //update the value of the string
                        value = updateJSONString(value, infoSourceIds);
                        if(value.length()>0){
                            //update the existing value for the column
                            storeHelper.updateStringValue(columnFamily,columnFamily,column, guid, value);
                        }
                        else{
                            //no values remaining for the column so remove it
                            storeHelper.deleteColumns(columnFamily, guid, column);
                        }
                    }
                }
            }
            //else there is no delete that can be performed without a column or infosource
            
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
                JsonNode next = null;
                Iterator<JsonNode> it = rootNode.iterator();
                while(it.hasNext()){
                    next = it.next();
                    if(!(hasInfoSourceId(next, infoSourceIds))){                
                        objectList.add(next);
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
    
    private boolean hasInfoSourceId(JsonNode rootNode, String[] infoSourceIds){
        boolean b = false;
        
        String s = rootNode.path("infoSourceId").getTextValue();
        for(String infoSourceId : infoSourceIds){           
            if(infoSourceId.equals(s)){
                return true;
            }
        }
        return b;
    }
    
    private boolean hasInfoSourceId(String value, String[] infoSourceIds){
        boolean b = false;
        
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
        return b;
    }
    
    
}
