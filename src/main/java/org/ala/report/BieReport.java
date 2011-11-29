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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ala.dao.StoreHelper;
import org.ala.model.Classification;
import org.ala.model.Image;
import org.ala.model.TaxonConcept;
import org.ala.util.ColumnType;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * BieReport.
 * 
 * @author MOK011
 * 
 * History:
 * init version: 3 Sept 2010.
 * 10-Sept-10 (MOK011): added new counter for australian spices & asutralian species with image.
 * 
 * 
 */
@Component("bieReport")
public class BieReport {
    @Inject
    protected StoreHelper storeHelper;
    
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final int ROWS = 1000;
	public static final String CHARSET_ENCODING = "UTF-8";
	
	
//	private String host = "localhost";
//	private int port = 9160;
	private String keyspace = "bie";
	private String columnFamily = "tc";	
	private ObjectMapper mapper;
	
	public static final String CARRIAGE_RETURN = "\r\n";
	public static final String AUSTRALIAN_GUID_PREFIX = "urn:lsid:biodiversity";
	public static final List<String> VERTEBRATE_LIST = Arrays.asList("chordata");;
	public static final List<String> PLANT_LIST = Arrays.asList("plantae");
	public static final List<String> INVERTEBRATE_LIST = Arrays.asList("acanthocephala" ,"acoelomorpha", "annelida", "arthropoda",
			"brachiopoda", "bryozoa", "chaetognatha", "cnidaria", "ctenophora", "cycliophora", 
			"echinodermata", "entoprocta", "gastrotricha", "gnathostomulida", "hemichordata", 
			"kinorhyncha", "loricifera", "micrognathozoa", "mollusca", "nematoda", "nemertea",
			"onychophora", "phoronida", "platyhelminthes", "porifera", "priapulida", "rotifera", 
			"sipuncula", "tardigrada", "xenoturbellida");
	
	enum CtrIndex {IMAGE_CTR_INDEX, VERTEBRATE_IMAGE_CTR_INDEX, INVERTEBRATE_IMAGE_CTR_INDEX, PLANT_IMAGE_CTR_INDEX,
		OTHER_IMAGE_CTR_INDEX, VERTEBRATE_NAME_CTR_INDEX, INVERTEBRATE_NAME_CTR_INDEX, PLANT_NAME_CTR_INDEX,
		OTHER_NAME_CTR_INDEX, VERTEBRATE_WITH_IMAGE_CTR_INDEX, INVERTEBRATE_WITH_IMAGE_CTR_INDEX, PLANT_WITH_IMAGE_CTR_INDEX,
		OTHER_WITH_IMAGE_CTR_INDEX, VERTEBRATE_CTR_INDEX, INVERTEBRATE_CTR_INDEX, PLANT_CTR_INDEX,
		OTHER_CTR_INDEX}	
	enum Taxa {VERTEBRATE, INVERTEBRATE, PLANT, OTHER, INVALID}
	public static final int NUMBER_OF_COUNTER = CtrIndex.values().length;
		
	/**
	 * Usage: outputFileName [option: cassandraAddress cassandraPort]
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		//BieReport bieReport = null;
		
		//check input arguments
		if (args.length < 1) {
			System.out.println("Output File Name Missing ....");
			System.exit(0);
		}
		ApplicationContext context = SpringUtils.getContext();
        BieReport bieReport = context.getBean(BieReport.class);
        
//		else if (args.length == 1){
//			bieReport = new BieReport();
//		}		
//		else if (args.length == 2){
//			bieReport = new BieReport(args[1], 9160);
//		}
//		else if (args.length == 3){
//			bieReport = new BieReport(args[1], Integer.parseInt(args[2]));
//		}
		
		// do report
		try{
			if(bieReport != null){
				bieReport.doFullScanAndCount(args[0]);
				bieReport.closeConnectionPool();
			}
			else{
				System.out.println("Invalid input arguments ...." + args);
				System.exit(0);			
			}
		}
		catch(Exception e){			
			System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");
			e.printStackTrace();
			bieReport.closeConnectionPool();
			System.exit(0);	
		}
	}

	public BieReport(){
		this("bie", "tc", "localhost", 9160);
	}
	
	public BieReport(String host, int port){
		this("bie", "tc", host, port);
	}
	
	public BieReport(String keySpace, String columnFamily, String host, int port){
		this.keyspace = keySpace;
		this.columnFamily = columnFamily;
		//this.host = host;
		//this.port = port;
		//Pelops.addPool(POOL_NAME, new String[]{this.host}, this.port, false, this.keyspace, new Policy());
		mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * close cassandra connection pool.
	 */
	public void closeConnectionPool(){
	    storeHelper.shutdown();
	    //Pelops.shutdown();
	}
	
	/**
	 * scan whole columnFamily tree and counting image; vertebrate; invertebrate; 
	 * plant and other in Australia.
	 * 
	 * @param infoSourceIds 
	 * @throws Exception
	 */
	public void doFullScanAndCount(String fileName) throws Exception {
		long start = System.currentTimeMillis();
		long ctr = 1;
		int[] totalCtr = new int[NUMBER_OF_COUNTER];
		
		ColumnType[] columns = new ColumnType[]{
                ColumnType.TAXONCONCEPT_COL,
                ColumnType.CLASSIFICATION_COL,
                ColumnType.IMAGE_COL,
                ColumnType.SYNONYM_COL,
               
        };
		
		Map<String, Map<String,Object>> rowMaps = storeHelper.getPageOfSubColumns(columnFamily, columnFamily,columns, "", ROWS);
		
		
		
//		KeySlice startKey = new KeySlice();
//		KeySlice lastKey = null;		
		String lastKey="";
		String startKey="";
		System.out.println("BieReport process is started.....");
		
//		ColumnParent columnParent = new ColumnParent(columnFamily);
//
//		KeyRange keyRange = new KeyRange(ROWS);
//		keyRange.setStart_key("");
//		keyRange.setEnd_key("");
//
//		SliceRange sliceRange = new SliceRange();
//		sliceRange.setStart(new byte[0]);
//		sliceRange.setFinish(new byte[0]);
//
//		SlicePredicate slicePredicate = new SlicePredicate();
//		slicePredicate.setSlice_range(sliceRange);
//
//		Client client = Pelops.getDbConnPool(POOL_NAME).getConnection().getAPI();
//		
//		// Iterate over all the rows in a ColumnFamily......
//		// start with the empty string, and after each call use the last key read as the start key 
//		// in the next iteration.
//		// when lastKey == startKey is finish.
//		List<KeySlice> keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
		totalCtr = getBieReportCount(rowMaps);

		while (rowMaps.size() > 0){
			lastKey = rowMaps.keySet().toArray()[rowMaps.size()-1].toString();
			//end of scan ?
			if(lastKey.equals(startKey)){
				break;
			}
			startKey = lastKey;
			rowMaps = storeHelper.getPageOfSubColumns(columnFamily, columnFamily,columns, startKey, ROWS);
			
			//keyRange.setStart_key(lastKey.getKey());			
			//keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
			int[] counters = getBieReportCount(rowMaps);
			for(int i = 0; i < counters.length; i++){
				totalCtr[i] += counters[i]; 
			}			
			System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey);
			System.gc();
		}
		
		System.out.println("\n==========< Summary >==========");
		System.out.println("Australian vertebrates: " + totalCtr[CtrIndex.VERTEBRATE_CTR_INDEX.ordinal()]);
		System.out.println("Australian invertebrates: " + totalCtr[CtrIndex.INVERTEBRATE_CTR_INDEX.ordinal()]);
		System.out.println("Australian plants: " + totalCtr[CtrIndex.PLANT_CTR_INDEX.ordinal()]);
		System.out.println("Australian other: " + totalCtr[CtrIndex.OTHER_CTR_INDEX.ordinal()]);
		System.out.println("Australian vertebrates with at least one image: " + totalCtr[CtrIndex.VERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Australian invertebrates with at least one image: " + totalCtr[CtrIndex.INVERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Australian plants with at least one image: " + totalCtr[CtrIndex.PLANT_WITH_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Australian other with at least one image: " + totalCtr[CtrIndex.OTHER_WITH_IMAGE_CTR_INDEX.ordinal()]);
		
		System.out.println("All Image Counter: " + totalCtr[CtrIndex.IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Vertebrate Image Counter: " + totalCtr[CtrIndex.VERTEBRATE_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Invertebrate Image Counter: " + totalCtr[CtrIndex.INVERTEBRATE_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Plant Image Counter: " + totalCtr[CtrIndex.PLANT_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Other Image Counter: " + totalCtr[CtrIndex.OTHER_IMAGE_CTR_INDEX.ordinal()]);
		System.out.println("Vertebrate Name Counter: " + totalCtr[CtrIndex.VERTEBRATE_NAME_CTR_INDEX.ordinal()]);
		System.out.println("Invertebrate Name Counter: " + totalCtr[CtrIndex.INVERTEBRATE_NAME_CTR_INDEX.ordinal()]);
		System.out.println("Plant Name Counter: " + totalCtr[CtrIndex.PLANT_NAME_CTR_INDEX.ordinal()]);
		System.out.println("Other Name Counter: " + totalCtr[CtrIndex.OTHER_NAME_CTR_INDEX.ordinal()]);		
		
		System.out.println("Row Count:" + ROWS * ctr);		
		System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
		writeToFile(fileName, totalCtr, ROWS * ctr);
	}
	
	/**
	 * write report into file.
	 * 
	 * @param fileName
	 * @param totalCtr
	 * @param rowCtr
	 * @throws IOException
	 */
	private void writeToFile(String fileName, int[] totalCtr, long rowCtr) throws IOException{
		FileWriter fw = new FileWriter(fileName);
		fw.write(CARRIAGE_RETURN + "===========<Australian Species Count>===========" + CARRIAGE_RETURN);
		fw.write("Australian Species : where guid = '" + AUSTRALIAN_GUID_PREFIX + "*'; rankString = 'species'" + CARRIAGE_RETURN);
		fw.write("Australian vertebrates: " + totalCtr[CtrIndex.VERTEBRATE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian invertebrates: " + totalCtr[CtrIndex.INVERTEBRATE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian plants: " + totalCtr[CtrIndex.PLANT_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian other: " + totalCtr[CtrIndex.OTHER_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian vertebrates with at least one image: " + totalCtr[CtrIndex.VERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian invertebrates with at least one image: " + totalCtr[CtrIndex.INVERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian plants with at least one image: " + totalCtr[CtrIndex.PLANT_WITH_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian other with at least one image: " + totalCtr[CtrIndex.OTHER_WITH_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN + CARRIAGE_RETURN);
		fw.write(CARRIAGE_RETURN + "===========<Australian Image & Synonym Name Count>===========" + CARRIAGE_RETURN);
		fw.write("All Australian Species Image Counter (no rankString check): " + totalCtr[CtrIndex.IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Australian Vertebrate Image Counter (with rankString = 'species'): " + totalCtr[CtrIndex.VERTEBRATE_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Invertebrate Image Counter (with rankString = 'species'): " + totalCtr[CtrIndex.INVERTEBRATE_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Plant Image Counter (with rankString = 'species'): " + totalCtr[CtrIndex.PLANT_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Other Image Counter (with rankString = 'species'): " + totalCtr[CtrIndex.OTHER_IMAGE_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Vertebrate Synonym Name Counter (with rankString = 'species'): " + totalCtr[CtrIndex.VERTEBRATE_NAME_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Invertebrate Synonym Name Counter (with rankString = 'species'): " + totalCtr[CtrIndex.INVERTEBRATE_NAME_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Plant Synonym Name Counter (with rankString = 'species'): " + totalCtr[CtrIndex.PLANT_NAME_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);
		fw.write("Australian Other Synonym Name Counter (with rankString = 'species'): " + totalCtr[CtrIndex.OTHER_NAME_CTR_INDEX.ordinal()] + CARRIAGE_RETURN);		
		fw.write("\nRow Counter: " + rowCtr + CARRIAGE_RETURN);
		fw.flush();
		fw.close();		
	}
	
	/**
	 * do counting image; vertebrate; invertebrate; plant and other in Australia.
	 * 
	 * @param keySlices
	 * @param infoSourceIds
	 * @return
	 */
	private int[] getBieReportCount(Map<String, Map<String,Object>> rowMaps){
		int[] ctrs = new int[NUMBER_OF_COUNTER] ;		
		
		for(String guid : rowMaps.keySet()){
		    //get the columns and object values
		    int[] taxaCtr = getAusTaxaCount(rowMaps.get(guid), guid);
		    for(int i = 0; i < taxaCtr.length; i++){
              ctrs[i] += taxaCtr[i];
          }
		}
		
//		for (KeySlice keySlice : keySlices) {
//			for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
//				if (columns.isSetSuper_column()) {
//					SuperColumn scol = columns.getSuper_column();
//					int[] taxaCtr = getAusTaxaCount(scol, keySlice.getKey());
//					for(int i = 0; i < taxaCtr.length; i++){
//						ctrs[i] += taxaCtr[i];
//					}			
//				}
//			}
//		}
		return ctrs;
	}
		
	/**
	 * do counting of vertebrate, invertebrate, plant and other in Australia.
	 * 
	 * @param scol
	 * @return
	 */
	private int[] getAusTaxaCount(Map<String,Object> columnMap, String guid){
		int[] ctr = new int[NUMBER_OF_COUNTER];
		int imageCtr = 0;
		int synonymCtr = 0;
		String value = null;
		String colName = null;
		boolean hasImages = false;
		boolean hasSynonym = false;
		boolean isSpecies = false;
		Taxa taxa = Taxa.INVALID;
		
		if(guid == null || !guid.trim().startsWith(AUSTRALIAN_GUID_PREFIX)){
			return ctr;
		}
		
		
		//check for classification
		if(columnMap.containsKey(ColumnType.CLASSIFICATION_COL.getColumnName())){
		    List<Classification> classifications = (List<Classification>)columnMap.get(ColumnType.CLASSIFICATION_COL.getColumnName());
		    taxa = getClassification(classifications);
		}
		if(columnMap.containsKey(ColumnType.IMAGE_COL.getColumnName())){
		    List<Image> images = (List<Image>)columnMap.get(ColumnType.IMAGE_COL.getColumnName());
		    imageCtr = images.size();
            if(imageCtr > 0){
                hasImages = true;
            }
		}
		if(columnMap.containsKey(ColumnType.SYNONYM_COL.getColumnName())){
		    List<TaxonConcept> synonym = (List<TaxonConcept>)columnMap.get(ColumnType.SYNONYM_COL.getColumnName());
		    synonymCtr = synonym.size();
            if(synonymCtr > 0){
                hasSynonym = true;
            }
		}
		if(columnMap.containsKey(ColumnType.TAXONCONCEPT_COL.getColumnName())){
		    TaxonConcept taxonConcept = (TaxonConcept)columnMap.get(ColumnType.TAXONCONCEPT_COL.getColumnName());
		    if("species".equalsIgnoreCase(taxonConcept.getRankString().trim())){
                isSpecies = true;
            }
		}
		
		//scan all columns
//		for (Column col : scol.getColumns()) {
//			try {
//				value = new String(col.getValue(), CHARSET_ENCODING);
//				colName = new String(col.getName(), CHARSET_ENCODING);
//				if("hasClassification".equalsIgnoreCase(colName)){
//					List<Classification> classifications = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Classification.class));
//					taxa = getClassification(classifications);
//				}
//				if("hasImage".equalsIgnoreCase(colName)){
//					List<Image> images = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Image.class));
//					imageCtr = images.size();
//					if(imageCtr > 0){
//						hasImages = true;
//					}
//				}
//				if("hasSynonym".equalsIgnoreCase(colName)){
//					List<TaxonConcept> synonym = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, TaxonConcept.class));
//					synonymCtr = synonym.size();
//					if(synonymCtr > 0){
//						hasSynonym = true;
//					}
//				}
//				if("taxonConcept".equalsIgnoreCase(colName)){
//					TaxonConcept taxonConcept = mapper.readValue(value, TaxonConcept.class);
//					if("species".equalsIgnoreCase(taxonConcept.getRankString().trim())){
//						isSpecies = true;
//					}
//				}
//			} catch (Exception e) {
//				logger.error(e);
//			} 	
//		}	

		//populate counter
		if(!Taxa.INVALID.equals(taxa) && isSpecies){
			switch(taxa){
				case VERTEBRATE:
					ctr[CtrIndex.VERTEBRATE_CTR_INDEX.ordinal()]++;
					if(hasImages){
						ctr[CtrIndex.VERTEBRATE_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
						ctr[CtrIndex.VERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()]++;
					}
					if(hasSynonym){
						ctr[CtrIndex.VERTEBRATE_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}
					break;
					
				case INVERTEBRATE:
					ctr[CtrIndex.INVERTEBRATE_CTR_INDEX.ordinal()]++;
					if(hasImages){
						ctr[CtrIndex.INVERTEBRATE_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
						ctr[CtrIndex.INVERTEBRATE_WITH_IMAGE_CTR_INDEX.ordinal()]++;
					}
					if(hasSynonym){
						ctr[CtrIndex.INVERTEBRATE_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
				
				case PLANT:
					ctr[CtrIndex.PLANT_CTR_INDEX.ordinal()]++;
					if(hasImages){
						ctr[CtrIndex.PLANT_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
						ctr[CtrIndex.PLANT_WITH_IMAGE_CTR_INDEX.ordinal()]++;
					}
					if(hasSynonym){
						ctr[CtrIndex.PLANT_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
					
				case OTHER:
					ctr[CtrIndex.OTHER_CTR_INDEX.ordinal()]++;
					if(hasImages){
						ctr[CtrIndex.OTHER_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
						ctr[CtrIndex.OTHER_WITH_IMAGE_CTR_INDEX.ordinal()]++;
					}
					if(hasSynonym){
						ctr[CtrIndex.OTHER_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
					
				default:
					//reset counter
					ctr = new int[NUMBER_OF_COUNTER];
					logger.info("****** INVALID AUSTRALIAN CLASSIFICATION: " + guid);
					break;
				
			}
		}
		//populate total image count.
		if(hasImages){
			ctr[CtrIndex.IMAGE_CTR_INDEX.ordinal()] = imageCtr;
		}		
		return ctr;
	}
	
	/**
	 * get taxa type from classification.
	 * 
	 * @param classifications
	 * @return
	 */
	public static Taxa getClassification(List<Classification> classifications){
		Taxa taxa = Taxa.OTHER;
		
		// No classification
		if(classifications.size() != 1){
			return Taxa.INVALID;
		}
		if(PLANT_LIST.contains(classifications.get(0).getKingdom() == null? classifications.get(0).getKingdom() : classifications.get(0).getKingdom().toLowerCase())){
			taxa = Taxa.PLANT;
		}
		else if(INVERTEBRATE_LIST.contains(classifications.get(0).getPhylum() == null? classifications.get(0).getPhylum():classifications.get(0).getPhylum().toLowerCase())){
			taxa = Taxa.INVERTEBRATE;
		}
		else if(VERTEBRATE_LIST.contains(classifications.get(0).getPhylum() == null?classifications.get(0).getPhylum():classifications.get(0).getPhylum().toLowerCase())){
			taxa = Taxa.VERTEBRATE;
		}
		return taxa;
	}
	
	//========= Getter =======
	public static int getRows() {
		return ROWS;
	}

//	public String getHost() {
//		return host;
//	}
//
//	public int getPort() {
//		return port;
//	}

	public String getKeyspace() {
		return keyspace;
	}

	public String getColumnFamily() {
		return columnFamily;
	}	
}
