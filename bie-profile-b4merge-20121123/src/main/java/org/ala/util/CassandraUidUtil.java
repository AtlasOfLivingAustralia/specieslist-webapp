//DELETE ME
//NC I think that this class can be deleted.  It is NOT doing anything to the records in cassandra
// I think that the CassandraUtil replaces it


///***************************************************************************
// * Copyright (C) 2010 Atlas of Living Australia
// * All Rights Reserved.
// *
// * The contents of this file are subject to the Mozilla Public
// * License Version 1.1 (the "License"); you may not use this file
// * except in compliance with the License. You may obtain a copy of
// * the License at http://www.mozilla.org/MPL/
// *
// * Software distributed under the License is distributed on an "AS
// * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
// * implied. See the License for the specific language governing
// * rights and limitations under the License.
// ***************************************************************************/
//package org.ala.util;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.ala.dao.InfoSourceDAO;
//import org.ala.dao.StoreHelper;
//import org.ala.dao.TaxonConceptDao;
//import org.ala.hbase.InfosourceUidLoader;
//import org.apache.log4j.Logger;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.inject.Inject;
//
//import org.apache.cassandra.thrift.Cassandra.Client;
//import org.apache.cassandra.thrift.Column;
//import org.apache.cassandra.thrift.ColumnOrSuperColumn;
//import org.apache.cassandra.thrift.ColumnParent;
//import org.apache.cassandra.thrift.ConsistencyLevel;
//import org.apache.cassandra.thrift.KeyRange;
//import org.apache.cassandra.thrift.KeySlice;
//import org.apache.cassandra.thrift.SlicePredicate;
//import org.apache.cassandra.thrift.SliceRange;
//import org.apache.cassandra.thrift.SuperColumn;
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//import org.wyki.cassandra.pelops.Mutator;
//import org.wyki.cassandra.pelops.Pelops;
//import org.wyki.cassandra.pelops.Policy;
//import org.wyki.cassandra.pelops.Selector;
//
///**
// * Cassandra Uid Util
// * 
// * @author Tommy Wang
// * 
// */
////@Component("cassandraUidUtil")
//public class CassandraUidUtil {
//    protected Logger logger = Logger.getLogger(this.getClass());
//
//    @Inject
//    protected InfoSourceDAO infoSourceDAO;
//    @Inject
//    protected StoreHelper storeHelper;
//
//    public static final int ROWS = 1000;
//    public static final String CHARSET_ENCODING = "UTF-8";
//    public static final String POOL_NAME = "ALA";
//    public static final String PREFIX = "--";
//
//    private String keyspace = "bie";
//    private String columnFamily = "tc";	
//
//    /**
//     * 
//     * Usage: [-host=ala-biedb1.vm.csiro.au][-port=9160][--ColumnName...] [infoSourceId...]
//     * 
//     * eg: --hasImage --hasRegion 1013
//     * remove infoSourceId data from particular column [hasImage & hasRegion].
//     * 
//     * eg: --hasImage --hasRegion
//     * if infoSourceId is empty then remove whole column that equal to input columnName
//     * 
//     * eg: -host=ala-biedb1.vm.csiro.au -port=9160 1013
//     * if columnName is empty then remove infoSource data from all columns.
//     * 
//     * @param args
//     */
//    public static void main(String[] args) throws Exception {
//        ApplicationContext context = SpringUtils.getContext();
//        CassandraUidUtil cassandraUidUtil = context.getBean(CassandraUidUtil.class);
//        
//        //String host = "diasbtest1-cbr.vm.csiro.au";
//        //int port = 9160;
//
//       
//
//       
////        CassandraUidUtil cassandraUidUtil = new CassandraUidUtil(host, port);
////        String[] cast = new String[]{};
//        cassandraUidUtil.doFullScanAndloadUid();
//        cassandraUidUtil.closeConnectionPool();
//        System.exit(0);
//    }
//
//    public CassandraUidUtil(){
//        this("bie", "tc");
//    }
//
//    
//
//    public CassandraUidUtil(String keySpace, String columnFamily){
//        this.keyspace = keySpace;
//        this.columnFamily = columnFamily;
//        
//    }
//
//    public void closeConnectionPool(){
//        storeHelper.shutdown();
//    }
//
//    /**
//     * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
//     * then load uid. 
//     * @param infoSourceIds 
//     * @throws Exception
//     */
//    public void doFullScanAndloadUid() throws Exception {
//        long start = System.currentTimeMillis();
//        long ctr = 1;
//        long totalDelCtr = 0;
//        KeySlice startKey = new KeySlice();
//        KeySlice lastKey = null;
//
//        System.out.println("Uid loading process is started.....");
//
//        ColumnParent columnParent = new ColumnParent(columnFamily);
//
//        KeyRange keyRange = new KeyRange(ROWS);
//        keyRange.setStart_key("");
//        keyRange.setEnd_key("");
//
//        SliceRange sliceRange = new SliceRange();
//        sliceRange.setStart(new byte[0]);
//        sliceRange.setFinish(new byte[0]);
//
//        SlicePredicate slicePredicate = new SlicePredicate();
//        slicePredicate.setSlice_range(sliceRange);
//
//        Client client = Pelops.getDbConnPool(POOL_NAME).getConnection().getAPI();
//
//        Map <String, String> infosourceIdUidMap = getUidInfosourceIdMap();
//
//        // Iterate over all the rows in a ColumnFamily......
//        // start with the empty string, and after each call use the last key read as the start key 
//        // in the next iteration.
//        // when lastKey == startKey is finish.
//        List<KeySlice> keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
//        /*
//		delList = getDumpDeleteItemsList();
//         */
//        doValueUpdate(keySlices, infosourceIdUidMap);
//
//        while (keySlices.size() > 0){
//            lastKey = keySlices.get(keySlices.size()-1);
//            //end of row ?
//            if(lastKey.equals(startKey)){
//                break;
//            }
//            startKey = lastKey;
//            keyRange.setStart_key(lastKey.getKey());			
//            keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
//            doValueUpdate(keySlices, infosourceIdUidMap);
//            //            System.out.println("Total Column Update Count:" + totalDelCtr);
//            System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
//            System.gc();
//        }				
//        System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
//    }
//
//    private Map<String, String> getUidInfosourceIdMap() {
//        Map<String, String> uidInfosourceIDMap = new HashMap<String, String>();
//
//        List<Integer> infosourceIdList = infoSourceDAO.getIdsforAll();
//
//        for (Integer infosourceId : infosourceIdList) {
//            String uid = infoSourceDAO.getUidByInfosourceId(infosourceId.toString());
//            uidInfosourceIDMap.put(infosourceId.toString(), uid);
//        }
//
//        return uidInfosourceIDMap;
//    }
//
//    /**
//     * do update with cassandra repository.
//     * @param delList
//     * @param infoSourceIds
//     * @return
//     * @throws Exception
//     */
//        private int doValueUpdate(List<DeleteItemInfo> delList, String[] infoSourceIds, String[] columnNames) throws Exception{
//            int ctr = 0;
//            Selector selector = Pelops.createSelector(POOL_NAME, keyspace);
//    
//            for(DeleteItemInfo item : delList){		
//                //get cassandra value
//                Column col = selector.getSubColumnFromRow(item.getKey(), columnFamily, item.getSColName(), item.getColName(), ConsistencyLevel.ONE);
//                Mutator mutator = Pelops.createMutator(POOL_NAME, keyspace);
//    
//                // infoSourceIds is empty... remove whole column
//                if(infoSourceIds.length < 1){
//                    if(hasColumnName(item.getColName(), columnNames)){
//                        // remove whole column
//                        mutator.deleteSubColumn(item.getKey(), columnFamily, item.getSColName(), item.getColName());
//                        mutator.execute(ConsistencyLevel.ONE);
//                    }
//                }
//                // else remove each infosourceId column content record 
//                else if(hasColumnName(item.getColName(), columnNames)){
//                    String casJson = getJsonValue(col);
//                    if(casJson != null && casJson.length() > 0){
//                        // do update ....
//                        String json = doInfoSourceDelete(casJson, infoSourceIds);
//                        if(json != null && json.length() > 0){		        	
//                            try{
//                                mutator.writeSubColumn(item.getKey(), columnFamily, item.getSColName(), mutator.newColumn(item.getColName(), json));
//                                mutator.execute(ConsistencyLevel.ONE);
//                            } catch (Exception e){
//                                logger.error(e.getMessage(),e);
//                            }			        	
//                        }
//                        // empty content then remove whole column
//                        else{			        	
//                            mutator.deleteSubColumn(item.getKey(), columnFamily, item.getSColName(), item.getColName());
//                            mutator.execute(ConsistencyLevel.ONE);
//                        }
//                    }				
//                }
//                ctr++;
//            }
//            return ctr;
//        }
//
//    private String getJsonValue(Column column){
//        String value = "";
//        if (column != null) {
//            try {
//                value = new String(column.getValue(), CHARSET_ENCODING);
//            } catch (UnsupportedEncodingException e) {
//                logger.debug(e.toString());				
//            }
//        }
//        return value;		
//    }
//
//    /**
//     * if cassandra column have value of 'infoSourceId', then add the column info into list.
//     * @param keySlices
//     * @param infoSourceIds
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    private void doValueUpdate(List<KeySlice> keySlices, Map<String, String> infosourceIdUidMap) throws UnsupportedEncodingException{
//        List<DeleteItemInfo> l = new ArrayList<DeleteItemInfo>();
//
//        String value = null;
//        String colName = null;
//        String sColName = null;
//
//
//        //        Matcher m = p.matcher(value);
//
//        for (KeySlice keySlice : keySlices) {
//            for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
//                // set break point for debug only 
//                
//                String guid = keySlice.getKey();
//                
////                if(keySlice.getKey().equalsIgnoreCase("urn:lsid:biodiversity.org.au:apni.taxon:359404")){
////                    logger.debug("urn:lsid:biodiversity.org.au:apni.taxon:359404");
////                }
////
////                if (columns.isSetSuper_column()) {
////                    SuperColumn scol = columns.getSuper_column();
////                    sColName = new String(scol.getName(), CHARSET_ENCODING);
////                    for (Column col : scol.getColumns()) {
////                        value = new String(col.getValue(), CHARSET_ENCODING);
////                        
////                        System.out.println(getInfoSourceId(value) + "!!!!!" + value);
////                        // check column infoSourceId
////                        //                        if(hasInfoSourceId(value, infoSourceIds) && hasColumnName(colName, columnNames)){
////                        //                            l.add(new DeleteItemInfo(keySlice.getKey(), sColName, colName));
////                        //                            logger.debug("** col.getName(): " +  colName + ", Key = " + keySlice.getKey() + ", col.getValue(): " + value);
////                        //                        }						
////                    }
////                } else {
////                    Column col = columns.getColumn();
////                    value = new String(col.getValue(), CHARSET_ENCODING);
////                    colName = new String(col.getName(), CHARSET_ENCODING);	
////                    
////                    System.out.println(getInfoSourceId(value) + "!!!!!" + value);
////                    // check column infoSourceId
////                    //                    if(hasInfoSourceId(value, infoSourceIds) && hasColumnName(colName, columnNames)){
////                    //                        l.add(new DeleteItemInfo(keySlice.getKey(), colName));
////                    //                        logger.debug("col.getName(): " +  colName + ", Key = " + keySlice.getKey() + ", col.getValue(): " + value );
////                    //                    }																				
////                }
//            }
//        }
//    }
//
//    private String getInfoSourceId(String value){
//        Pattern p = Pattern.compile("(?:\"infoSourceId\":\\s*\")" + "([0-9]{1,})" + "(?:\")");
//        Matcher m = p.matcher(value);
//        if (m.find()){
//            return m.group(1);
//        }
//        
//        return null;
//    }
//
//    private boolean hasInfoSourceId(JsonNode rootNode, String[] infoSourceIds){
//        boolean b = false;
//
//        String s = rootNode.path("infoSourceId").getTextValue();
//        for(String infoSourceId : infoSourceIds){			
//            if(infoSourceId.equals(s)){
//                return true;
//            }
//        }
//        return b;
//    }
//
//    private boolean hasColumnName(String columnName, String[] columnNames){
//        boolean b = false;
//
//        // no columnNames condition then return true 
//        if(columnNames == null || columnNames.length < 1){
//            return true;
//        }
//
//        for(String colName : columnNames){
//            if(columnName.trim().equalsIgnoreCase(colName)){
//                return true;
//            }
//        }
//        return b;
//    }
//
//    /**
//     * convert json string to Jackson tree model, rebuild tree node without infoSourceId node.
//     * @param json jsonString
//     * @param infoSourceIds
//     * @return jsonString
//     */
//    private String doInfoSourceDelete(String json, String[] infoSourceIds){
//        String jStr = "";
//        List<JsonNode> objectList = new ArrayList<JsonNode>();
//        ObjectMapper mapper = new ObjectMapper();		
//        JsonNode rootNode;
//        try {			
//            rootNode = mapper.readValue(json, JsonNode.class);
//            if(!rootNode.isArray()){
//                if(!(hasInfoSourceId(rootNode, infoSourceIds))){
//                    jStr = json;
//                }
//            }
//            else{
//                JsonNode next = null;
//                Iterator<JsonNode> it = rootNode.iterator();
//                while(it.hasNext()){
//                    next = it.next();
//                    if(!(hasInfoSourceId(next, infoSourceIds))){				
//                        objectList.add(next);
//                    }				
//                }
//                if(objectList.size() > 0){
//                    jStr = mapper.writeValueAsString(objectList);
//                }
//            }			 			
//        } catch (Exception e) {
//            logger.info("doDelete(): " + e.toString());
//        } 		
//        return jStr;		
//    }
//
//    public static int getRows() {
//        return ROWS;
//    }
//
//    public String getHost() {
//        return host;
//    }
//
//    public int getPort() {
//        return port;
//    }
//
//    public String getKeyspace() {
//        return keyspace;
//    }
//
//    public String getColumnFamily() {
//        return columnFamily;
//    }
//
//    //	============<inner class>================== 
//    class DeleteItemInfo {
//        private String key;
//        private String colName;
//        private String sColName;
//
//        public DeleteItemInfo(String key, String sColName, String colName){
//            this.key = key;
//            this.colName = colName;
//            this.sColName = sColName;
//        }
//
//        public DeleteItemInfo(String key, String colName){
//            this(key, "", colName);
//        }
//
//        public String getColName() {
//            return colName;
//        }
//
//        public String getKey() {
//            return key;
//        }	
//
//        public String getSColName() {
//            return sColName;
//        }			
//    }
//}
