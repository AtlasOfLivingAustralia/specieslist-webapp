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
package org.ala.report;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.ala.dao.StoreHelper;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.ColumnType;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import java.util.ArrayList;

import javax.inject.Inject;


import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * GoogleSitemapGenerator.
 * 
 * @author MOK011
 * 
 * History:
 * init version: 10 Sept 2011.
 * 
 * 
 * 
 */
@Component("googleSitemapGenerator")
public class GoogleSitemapGenerator {
    @Inject
    protected StoreHelper storeHelper;
        protected Logger logger = Logger.getLogger(this.getClass());

        public static final int ROWS = 1000;
        public static final String CHARSET_ENCODING = "UTF-8";
//      public static final String POOL_NAME = "ALA";
        
//      private String host = "localhost";
//      private int port = 9160;
        private String keyspace = "bie";
        private String columnFamily = "tc";     
        private ObjectMapper mapper;

        private int urlCtr = 0;
        private int fileNameCtr = 1;
        
        public static final int MAX_NUMBER_URL = 20000;
        private FileWriter fw = null;
        private String fileName = null;
        public static final String APNI_TAXON = ":apni.taxon:";
        public static final String ADF_TAXON = ":afd.taxon:";
        enum NamePos {SCIENTIFIC_NAME, COMMON_NAME, KINGDOM, IS_AUSTRALIAN, NAME_COMPLETE}
        
        //tracking what sci name been add into sitemap that prevent duplicate url.
        private Set<Integer> track = new HashSet<Integer>();

        /**
         * Usage: outputFileName [option: cassandraAddress cassandraPort]
         * 
         * @param args
         */
        public static void main(String[] args) throws Exception {
            ApplicationContext context = SpringUtils.getContext();
            GoogleSitemapGenerator googleSitemapGenerator = context.getBean(GoogleSitemapGenerator.class);
                
                                
                //check input arguments
                if (args.length == 0) {                 
                        googleSitemapGenerator.setFileName("Sitemap");
                }
                else if (args.length == 1){                     
                        googleSitemapGenerator.setFileName(args[0]);
                }               
//              else if (args.length == 2){
//                      googleSitemapGenerator = new GoogleSitemapGenerator(args[1], 9160);
//                      googleSitemapGenerator.setFileName(args[0]);
//              }
//              else if (args.length == 3){
//                      googleSitemapGenerator = new GoogleSitemapGenerator(args[1], Integer.parseInt(args[2]));
//                      googleSitemapGenerator.setFileName(args[0]);
//              }
                
                // do sitemap
                try{
                        if(googleSitemapGenerator != null){
                                googleSitemapGenerator.doFullScan();
                                googleSitemapGenerator.closeConnectionPool();
                        }
                        else{
                                System.out.println("Invalid input arguments ...." + args);
                                System.exit(0);                 
                        }
                }
                catch(Exception e){                     
                        System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");
                        e.printStackTrace();
                        googleSitemapGenerator.closeConnectionPool();
                        System.exit(0); 
                }
                System.exit(0);
        }

        public GoogleSitemapGenerator(){
                this("bie", "tc");
        }
        
        public GoogleSitemapGenerator(String keySpace, String columnFamily){
                this.keyspace = keySpace;
                this.columnFamily = columnFamily;
                
                
                mapper = new ObjectMapper();
                mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        
        /**
         * close cassandra connection pool.
         */
        public void closeConnectionPool(){
                storeHelper.shutdown();
        }
        
        /**
         * scan whole columnFamily tree; 
         * plant and other in Australia.
         * 
         * @param infoSourceIds 
         * @throws Exception
         */
        public void doFullScan() throws Exception {
                long start = System.currentTimeMillis();
                //KeySlice startKey = new KeySlice();
                //KeySlice lastKey = null;              

                Date dateNow = new Date ();     
                SimpleDateFormat dateformat = new SimpleDateFormat("yyMMddHHmm");
                this.setFileName(fileName + dateformat.format( dateNow ));
                
                System.out.println("GoogleSitemapGenerator process is started.....");
                
                
                ColumnType[] columns = new ColumnType[]{
                ColumnType.TAXONCONCEPT_COL,
                ColumnType.CLASSIFICATION_COL,
                ColumnType.VERNACULAR_COL,
                ColumnType.IS_AUSTRALIAN,
                ColumnType.TAXONNAME_COL,
               
        };
        String lastKey = "";
        String startKey="";
        Map<String, Map<String,Object>> rowMaps = storeHelper.getPageOfSubColumns(columnFamily,columns, "", ROWS);
    
                generateURL(rowMaps);
                
                while(rowMaps.size() >0){
                    lastKey = rowMaps.keySet().toArray()[rowMaps.size()-1].toString();
                    if(lastKey.equals(startKey)){
                break;
            }
                    startKey = lastKey;
            rowMaps = storeHelper.getPageOfSubColumns(columnFamily,columns, startKey, ROWS);
            generateURL(rowMaps);
                }
        writeFileFooter();
        urlCtr = 0;
                
//              ColumnParent columnParent = new ColumnParent(columnFamily);
//
//              KeyRange keyRange = new KeyRange(ROWS);
//              keyRange.setStart_key("");
//              keyRange.setEnd_key("");
//
//              SliceRange sliceRange = new SliceRange();
//              sliceRange.setStart(new byte[0]);
//              sliceRange.setFinish(new byte[0]);
//
//              SlicePredicate slicePredicate = new SlicePredicate();
//              slicePredicate.setSlice_range(sliceRange);

                //Client client = Pelops.getDbConnPool(POOL_NAME).getConnection().getAPI();
                
                // Iterate over all the rows in a ColumnFamily......
                // start with the empty string, and after each call use the last key read as the start key 
                // in the next iteration.
                // when lastKey == startKey is finish.
//              List<KeySlice> keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);             
//              generateURL(keySlices);
//              while (keySlices.size() > 0){
//                      lastKey = keySlices.get(keySlices.size()-1);
//                      //end of scan ?
//                      if(lastKey.equals(startKey)){
//                              writeFileFooter();
//                              urlCtr = 0;
//                              break;
//                      }
//                      startKey = lastKey;
//                      keyRange.setStart_key(lastKey.getKey());                        
//                      keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
//                      generateURL(keySlices);
//                      System.gc();
//              }
                System.out.println("GoogleSitemapGenerator process is ended, total time takem: " + ((System.currentTimeMillis() - start)/1000));
        }
                
        private void writeURL(String name) throws IOException{
                if(name != null && name.trim().length() > 0){
                	    if(track.contains(name.trim().hashCode())){
                	        return;
                	    }
                        if(urlCtr == 0){
                                writeFileHeader();
                                track.clear();
                        }
                        fw.write("<url>\n");
                        fw.write("<loc>http://bie.ala.org.au/species/" + java.net.URLEncoder.encode(name.trim(), "UTF-8") + "</loc>\n");
                        fw.write("<changefreq>daily</changefreq>\n");
                        fw.write("<priority>0.5000</priority>\n");
                        fw.write("</url>\n");
                        
                        track.add(name.trim().hashCode());
                        urlCtr++;
                        if(urlCtr >= MAX_NUMBER_URL){
                                writeFileFooter();
                                urlCtr = 0;
                        }                       
                }               
        }
                
        private void writeFileHeader() throws IOException{
                //init file write
                System.out.println("**** file created: " + fileName + fileNameCtr + ".xml");
                fw = new FileWriter(fileName + "_" + fileNameCtr + ".xml");
                
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                fw.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
                
                fileNameCtr++;
        }
        
        private void writeFileFooter() throws IOException{              
                fw.write("</urlset>\n");
                fw.flush();
                fw.close();     
        }
        
        private void generateURL(Map<String, Map<String,Object>> rowMaps){      
            
            
            for(String guid : rowMaps.keySet()){
                String names[] = getSciAndCmnName(rowMaps.get(guid), guid);
                if(names != null && "true".equalsIgnoreCase(names[NamePos.IS_AUSTRALIAN.ordinal()])){
                	logger.debug("******** GUID: " +guid + ", SCIENTIFIC_NAME: " + names[NamePos.SCIENTIFIC_NAME.ordinal()] + " urlCtr: " + urlCtr);
                //ignore last column[isAustralian]
                for(int i = 0; i < names.length - 2; i++){
                    try {                           
                        if(names[i] != null && !names[i].isEmpty()){                                        
                            if(i == NamePos.KINGDOM.ordinal()){
                                writeURL(StringEscapeUtils.escapeXml(names[NamePos.SCIENTIFIC_NAME.ordinal()] + " (" + names[NamePos.KINGDOM.ordinal()] + ")"));
                            }
                            else{
                                writeURL(StringEscapeUtils.escapeXml(names[i]));
                            }
                        }
                    } catch (IOException e) {
                        logger.error(e);
                        e.printStackTrace();
                        //close file
                        try {
                            writeFileFooter();
                        } catch (IOException e1) {
                            logger.error(e1);
                            e1.printStackTrace();
                        }
                        urlCtr = 0;
                    }
                }
                }
            }
            
            
//              for (KeySlice keySlice : keySlices) {
//                      for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
//                              if (columns.isSetSuper_column()) {
//                                      SuperColumn scol = columns.getSuper_column();                           
//                                      String[] names = getSciAndCmnName(scol, keySlice.getKey());
//                                      if(names != null && "true".equalsIgnoreCase(names[NamePos.IS_AUSTRALIAN.ordinal()])){
//                                              logger.debug("******** GUID: " + keySlice.getKey() + " urlCtr: " + urlCtr);
//                                              //ignore last column[isAustralian]
//                                              for(int i = 0; i < names.length - 1; i++){
//                                                      try {                                                   
//                                                              if(names[i] != null && !names[i].isEmpty()){                                                                            
//                                                                      if(i == NamePos.KINGDOM.ordinal()){
//                                                                              writeURL(StringEscapeUtils.escapeXml(names[NamePos.SCIENTIFIC_NAME.ordinal()] + " (" + names[NamePos.KINGDOM.ordinal()] + ")"));
//                                                                      }
//                                                                      else{
//                                                                              writeURL(StringEscapeUtils.escapeXml(names[i]));
//                                                                      }
//                                                              }
//                                                      } catch (IOException e) {
//                                                              logger.error(e);
//                                                              e.printStackTrace();
//                                                              //close file
//                                                              try {
//                                                                      writeFileFooter();
//                                                              } catch (IOException e1) {
//                                                                      logger.error(e1);
//                                                                      e1.printStackTrace();
//                                                              }
//                                                              urlCtr = 0;
//                                                      }
//                                              }
//                                      }
//                              }
//                      }
//              }
        }

        private String[] getSciAndCmnName(Map<String,Object> columnMap, String guid){
                String value = null;
                String colName = null;          
                String[] names = new String[]{"", "", "", "", ""};
                                
                if(guid == null || (!guid.trim().contains(APNI_TAXON) && !guid.trim().contains(ADF_TAXON))){
                        return null;
                }
                
                if(columnMap.containsKey(ColumnType.CLASSIFICATION_COL.getColumnName())){
                    List<Classification> classifications = (List<Classification>)columnMap.get(ColumnType.CLASSIFICATION_COL.getColumnName());
                    if(classifications != null && classifications.size() > 0 && classifications.get(0).getKingdom() != null){
                        names[NamePos.KINGDOM.ordinal()] = classifications.get(0).getKingdom().trim();
                    }
                }
                if(columnMap.containsKey(ColumnType.VERNACULAR_COL.getColumnName())){
                    List<CommonName> commonNames = (List<CommonName>)columnMap.get(ColumnType.VERNACULAR_COL.getColumnName());
            if(commonNames != null ){
              for(int i = 0; i < commonNames.size(); i++){
                  if(commonNames.get(i).isPreferred()){
                      names[NamePos.COMMON_NAME.ordinal()] = commonNames.get(i).getNameString().trim();
                      break;
                  }
              }
          }
                }
                if(columnMap.containsKey(ColumnType.TAXONCONCEPT_COL.getColumnName())){
                    TaxonConcept taxonConcept = (TaxonConcept)columnMap.get(ColumnType.TAXONCONCEPT_COL.getColumnName());
                    names[NamePos.SCIENTIFIC_NAME.ordinal()] = taxonConcept.getNameString().trim();
                }
                if(columnMap.containsKey(ColumnType.IS_AUSTRALIAN.getColumnName())){
                    names[NamePos.IS_AUSTRALIAN.ordinal()] = columnMap.get(ColumnType.IS_AUSTRALIAN.getColumnName()).toString().trim();
                }
                
                if(columnMap.containsKey(ColumnType.TAXONNAME_COL.getColumnName())){
                    List<TaxonName> taxonNames = (List<TaxonName>)columnMap.get(ColumnType.TAXONNAME_COL.getColumnName());
                    if(taxonNames != null && taxonNames.size() > 0 && taxonNames.get(0).getNameComplete() != null){
                        names[NamePos.NAME_COMPLETE.ordinal()] = taxonNames.get(0).getNameComplete().trim();
                    }
                }
                
                // replace scientic name with name complete.
                if(names[NamePos.NAME_COMPLETE.ordinal()] != null && !"".equals(names[NamePos.NAME_COMPLETE.ordinal()])){
                    names[NamePos.SCIENTIFIC_NAME.ordinal()] = names[NamePos.NAME_COMPLETE.ordinal()];
                }
                
                //scan all columns
//              for (Column col : scol.getColumns()) {
//                      try {
//                              value = new String(col.getValue(), CHARSET_ENCODING);
//                              colName = new String(col.getName(), CHARSET_ENCODING);
//                              if("hasClassification".equalsIgnoreCase(colName)){
//                                      List<Classification> classifications = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Classification.class));
//                                      if(classifications != null && classifications.size() > 0){
//                                              names[NamePos.KINGDOM.ordinal()] = classifications.get(0).getKingdom();
//                                      }
//                              }
//                              else if("hasVernacularConcept".equalsIgnoreCase(colName)){
//                                      List<CommonName> commonNames = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, CommonName.class));
//                                      if(commonNames != null ){
//                                              for(int i = 0; i < commonNames.size(); i++){
//                                                      if(commonNames.get(i).isPreferred()){
//                                                              names[NamePos.COMMON_NAME.ordinal()] = commonNames.get(i).getNameString();
//                                                              break;
//                                                      }
//                                              }
//                                      }
//                              }
//                              else if("taxonConcept".equalsIgnoreCase(colName)){
//                                      TaxonConcept taxonConcept = mapper.readValue(value, TaxonConcept.class);
//                                      names[NamePos.SCIENTIFIC_NAME.ordinal()] = taxonConcept.getNameString();
//                              }
//                              else if("IsAustralian".equalsIgnoreCase(colName)){
//                                      names[NamePos.IS_AUSTRALIAN.ordinal()] = value;
//                              }
//                      } catch (Exception e) {
//                              logger.error(e);
//                      }       
//              }       

                return names;
        }
                
        //========= Getter =======
        public static int getRows() {
                return ROWS;
        }

        public String getKeyspace() {
                return keyspace;
        }

        public String getColumnFamily() {
                return columnFamily;
        }       
        
        // =========== setter ===========
        public void setFileName(String fileName) {
                this.fileName = fileName;
        }


        public void setKeyspace(String keyspace) {
                this.keyspace = keyspace;
        }


        public void setColumnFamily(String columnFamily) {
                this.columnFamily = columnFamily;
        }       
}
