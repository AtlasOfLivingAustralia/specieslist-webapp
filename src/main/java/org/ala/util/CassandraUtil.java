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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.hbase.InfosourceUidLoader;
import org.ala.model.AttributableObject;
import org.ala.model.CommonName;
import org.ala.model.Image;
import org.ala.model.InfoSource;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.SuperColumn;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.wyki.cassandra.pelops.Mutator;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;
import org.wyki.cassandra.pelops.Selector;

/**
 * Cassandra Uid Util
 * 
 * @author Tommy Wang
 * 
 */
@Component("cassandraUtil")
public class CassandraUtil {
    protected Logger logger = Logger.getLogger(this.getClass());

    protected InfoSourceDAO infoSourceDAO;

    protected TaxonConceptDao taxonConceptDao;
    protected StoreHelper storeHelper;

    public static final int ROWS = 1000;
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String POOL_NAME = "ALA";
    public static final String PREFIX = "--";
    public static final String HOST_PREFIX = "-host=";
    public static final String PORT_PREFIX = "-port=";
    private static final String TC_TABLE = "taxonConcept";
    private static final String TC_COL_FAMILY = "tc";
    private static final String APNI_INFOSOURCE_ID = "2";
    private static final String APC_INFOSOURCE_ID = "5";

    private String host = "localhost";
    private int port = 9160;
    private String keyspace = "bie";
    private String columnFamily = "tc";	

    private final String AFD_HOME = "http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/home";



    /**
     * 
     * Usage: [-host=ala-biedb1.vm.csiro.au][-port=9160][--ColumnName...] [infoSourceId...]
     * 
     * eg: --hasImage --hasRegion 1013
     * remove infoSourceId data from particular column [hasImage & hasRegion].
     * 
     * eg: --hasImage --hasRegion
     * if infoSourceId is empty then remove whole column that equal to input columnName
     * 
     * eg: -host=ala-biedb1.vm.csiro.au -port=9160 1013
     * if columnName is empty then remove infoSource data from all columns.
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        List<String> infosourceNameList = new ArrayList<String>();

//        CassandraUtil cassandraUtil = (CassandraUtil) context.getBean("cassandraUtil");

        String host = "localhost";
        int port = 9160;

        if (args.length > 0) {

            //setup args option list
            for(int i = 0; i < args.length; i++){
                String tmp = args[i].trim();
                if(tmp.startsWith(HOST_PREFIX)){
                    host = tmp.substring(HOST_PREFIX.length());
                }
                else if(tmp.startsWith(PORT_PREFIX)){
                    port = Integer.parseInt(tmp.substring(PORT_PREFIX.length()));
                } else {
                    infosourceNameList.add(tmp);
                }
            }
        }

        System.out.println("Connecting to: " + host + " port: " + port);
        CassandraUtil cassandraUtil = new CassandraUtil(host, port);
        //        String[] cast = new String[]{};
        for (String infosourceName : infosourceNameList) {
            if ("afd".equalsIgnoreCase(infosourceName)) {
                cassandraUtil.doFullScanAndUpdateInfosourceURLForAFD();
            } else if ("apni".equalsIgnoreCase(infosourceName)) {
                cassandraUtil.doFullScanAndUpdateInfosourceURLForAPNI();
            }
        }


        cassandraUtil.closeConnectionPool();
        System.exit(0);
    }

    public CassandraUtil(){
        this("bie", "tc", "localhost", 9160);
    }

    public CassandraUtil(String host, int port){
        this("bie", "tc", host, port);
    }

    public CassandraUtil(String keySpace, String columnFamily, String host, int port){
        this.keyspace = keySpace;
        this.columnFamily = columnFamily;
        this.host = host;
        this.port = port;
        Pelops.addPool(POOL_NAME, new String[]{this.host}, this.port, false, this.keyspace, new Policy());
    }

    public void closeConnectionPool(){
        Pelops.shutdown();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void doFullScanAndUpdateInfosourceURLForAPNI() throws Exception {
        Scanner scanner = storeHelper.getScanner(TC_TABLE, TC_COL_FAMILY,
                ColumnType.TAXONCONCEPT_COL.getColumnName());

        byte[] guidAsBytes = null;
        int i = 0;
        while (true) {
            
            try {
                guidAsBytes = scanner.getNextGuid();
            } catch(Exception e) {
                continue;
            }
            
            if (guidAsBytes == null) {
                break;
            }
            
            String guid = new String(guidAsBytes);

            if ("urn:lsid:biodiversity.org.au:apni.taxon:296709".equals(guid)) {
                System.out.println("DEBUG!");
            }

            TaxonConcept tc = taxonConceptDao.getByGuid(guid);

            if (tc != null && (APNI_INFOSOURCE_ID.equals(tc.getInfoSourceId()) || APC_INFOSOURCE_ID.equals(tc.getInfoSourceId()))) {
                String infoSrcUrl = generateAPNIURLForGuid(guid);

                tc.setInfoSourceURL(infoSrcUrl);
                if (!taxonConceptDao.update(tc)) {
                    System.out.println("UPDATE FAILURE");
                }
            }

            // get common names
            List<CommonName> commonNames = taxonConceptDao.getCommonNamesFor(guid);

            updateInfsourceURLForInfosource((List) commonNames, APNI_INFOSOURCE_ID, guid);
            storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
                    ColumnType.VERNACULAR_COL.getColumnName(),
                    guid, (List) commonNames, false);

            // get images
            List<Image> images = taxonConceptDao.getImages(guid);
            updateInfsourceURLForInfosource((List) images, APNI_INFOSOURCE_ID, guid);
            storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
                    ColumnType.IMAGE_COL.getColumnName(), guid,
                    (List) images, false);
            i++;

            if (i % 1000 == 0) {
                logger.debug(i + " records processed. Last ID: " + guid);
            }
        }
    }

    private String generateAPNIURLForGuid(String guid) throws Exception {
        String url = null;
        TaxonConcept tc = taxonConceptDao.getByGuid(guid);
        String scientificName = null;

        if (tc != null) {
            scientificName = tc.getNameString();
        }

        if (scientificName != null && scientificName.contains(" ")) {
            url = "http://www.anbg.gov.au/cgi-bin/apni?genus=" + scientificName.split(" ")[0] + "&species=" + scientificName.split(" ")[1];
        }

        return url;
    }

    private void updateInfsourceURLForInfosource(List<AttributableObject> objects,
            String id, String guid) throws Exception {

        List<AttributableObject> toUpdate = new ArrayList<AttributableObject>();
        for (AttributableObject object : objects) {
            if (object.getInfoSourceId() != null
                    && id.equals(object.getInfoSourceId())) {
                toUpdate.add(object);
            }
        }
        objects.removeAll(toUpdate);

        String newUrl = generateAPNIURLForGuid(guid);

        for (AttributableObject ao: toUpdate) {
            ao.setInfoSourceURL(newUrl);
        }

        objects.addAll(toUpdate);
    }

    /**
     * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
     * then load uid. 
     * @param infoSourceIds 
     * @throws Exception
     */
    public void doFullScanAndUpdateInfosourceURLForAFD() throws Exception {
        long start = System.currentTimeMillis();
        long ctr = 1;
        long totalDelCtr = 0;
        KeySlice startKey = new KeySlice();
        KeySlice lastKey = null;

        System.out.println("InfosourceURL updating process is starting.....");

        ColumnParent columnParent = new ColumnParent(columnFamily);

        KeyRange keyRange = new KeyRange(ROWS);
        keyRange.setStart_key("");
        keyRange.setEnd_key("");

        SliceRange sliceRange = new SliceRange();
        sliceRange.setStart(new byte[0]);
        sliceRange.setFinish(new byte[0]);

        SlicePredicate slicePredicate = new SlicePredicate();
        slicePredicate.setSlice_range(sliceRange);

        Client client = Pelops.getDbConnPool(POOL_NAME).getConnection().getAPI();

        // Iterate over all the rows in a ColumnFamily......
        // start with the empty string, and after each call use the last key read as the start key 
        // in the next iteration.
        // when lastKey == startKey is finish.
        List<KeySlice> keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
        /*
		delList = getDumpDeleteItemsList();
         */
        doValueUpdate(keySlices);

        while (keySlices.size() > 0){
            lastKey = keySlices.get(keySlices.size()-1);
            //end of row ?
            if(lastKey.equals(startKey)){
                break;
            }
            startKey = lastKey;
            keyRange.setStart_key(lastKey.getKey());			
            keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
            doValueUpdate(keySlices);
            //            System.out.println("Total Column Update Count:" + totalDelCtr);
            System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
            System.gc();
        }				
        System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
    }

    /**
     * if cassandra column have value of 'infoSourceId', then add the column info into list.
     * @param keySlices
     * @param infoSourceIds
     * @return
     * @throws Exception 
     */
    private void doValueUpdate(List<KeySlice> keySlices) throws Exception{
        List<DeleteItemInfo> l = new ArrayList<DeleteItemInfo>();

        InfoSource afd = infoSourceDAO.getByUri(AFD_HOME);


        //        Matcher m = p.matcher(value);

        for (KeySlice keySlice : keySlices) {
            // set break point for debug only 

            String guid = keySlice.getKey();

            TaxonConcept tc = taxonConceptDao.getByGuid(guid);


            if (tc != null && Integer.toString(afd.getId()).equals(tc.getInfoSourceId())) {
                TaxonName tn = taxonConceptDao.getTaxonNameFor(guid);

                String scientificName = null;
                if (tn != null) {
                    scientificName = URLEncoder.encode(tn.getNameComplete(), "UTF-8");
                } else {
                    scientificName = URLEncoder.encode(tc.getNameString(), "UTF-8");
                }

                scientificName = scientificName.replaceAll("\\+", "%20");
                //                System.out.println(tc.getInfoSourceURL());
                String infoSrcUrl = tc.getInfoSourceURL();
                infoSrcUrl = infoSrcUrl.substring(0, infoSrcUrl.lastIndexOf("/")+1) + scientificName;
                //                System.out.println(guid + "::" + tc.getInfoSourceURL() + "::" + infoSrcUrl);
                tc.setInfoSourceURL(infoSrcUrl);
                if (!taxonConceptDao.update(tc)) {
                    System.out.println("UPDATE FAILURE");
                }
            }

        }
    }

    public static int getRows() {
        return ROWS;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    //	============<inner class>================== 
    class DeleteItemInfo {
        private String key;
        private String colName;
        private String sColName;

        public DeleteItemInfo(String key, String sColName, String colName){
            this.key = key;
            this.colName = colName;
            this.sColName = sColName;
        }

        public DeleteItemInfo(String key, String colName){
            this(key, "", colName);
        }

        public String getColName() {
            return colName;
        }

        public String getKey() {
            return key;
        }	

        public String getSColName() {
            return sColName;
        }			
    }

    public InfoSourceDAO getInfoSourceDAO() {
        return infoSourceDAO;
    }

    public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
        this.infoSourceDAO = infoSourceDAO;
    }

    public TaxonConceptDao getTaxonConceptDao() {
        return taxonConceptDao;
    }

    public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
        this.taxonConceptDao = taxonConceptDao;
    }


    public StoreHelper getStoreHelper() {
        return storeHelper;
    }

    public void setStoreHelper(StoreHelper storeHelper) {
        this.storeHelper = storeHelper;
    }
}
