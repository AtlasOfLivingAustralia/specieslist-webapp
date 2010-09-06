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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.ala.model.Classification;
import org.ala.model.Image;
import org.ala.model.TaxonConcept;
import org.apache.log4j.Logger;
import java.util.ArrayList;
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
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;

/**
 * BieReport.
 * 
 * @author MOK011
 * 
 * init version: 3 Sept 2010.
 * 
 */
public class BieReport {
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final int ROWS = 1000;
	public static final String CHARSET_ENCODING = "UTF-8";
	public static final String POOL_NAME = "ALA";
	
	private String host = "localhost";
	private int port = 9160;
	private String keyspace = "bie";
	private String columnFamily = "tc";	
	private ObjectMapper mapper;
	
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
		OTHER_NAME_CTR_INDEX}	
	enum Taxa {VERTEBRATE, INVERTEBRATE, PLANT, OTHER, INVALID}
	public static final int NUMBER_OF_COUNTER = CtrIndex.values().length;
	private int invalidImageCtr = 0;

		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BieReport bieReport = null;
		
		if (args.length < 1) {
			System.out.println("Output File Name Missing ....");
			System.exit(0);
		}
		else if (args.length == 1){
			bieReport = new BieReport();
		}		
		else if (args.length == 2){
			bieReport = new BieReport(args[1], 9160);
		}
		else if (args.length == 3){
			bieReport = new BieReport(args[1], Integer.parseInt(args[2]));
		}
		
		if(bieReport != null){
			bieReport.doFullScanAndCount(args[0]);
			bieReport.closeConnectionPool();
		}
		else{
			System.out.println("Invalid input arguments ...." + args);
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
		this.host = host;
		this.port = port;
		Pelops.addPool(POOL_NAME, new String[]{this.host}, this.port, false, this.keyspace, new Policy());
		mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public void closeConnectionPool(){
		Pelops.shutdown();
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
		KeySlice startKey = new KeySlice();
		KeySlice lastKey = null;		
		
		System.out.println("BieReport process is started.....");
		
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
		totalCtr = getBieReportCount(keySlices);

		while (keySlices.size() > 0){
			lastKey = keySlices.get(keySlices.size()-1);
			//end of row ?
			if(lastKey.equals(startKey)){
				break;
			}
			startKey = lastKey;
			keyRange.setStart_key(lastKey.getKey());			
			keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
			int[] counters = getBieReportCount(keySlices);
			for(int i = 0; i < counters.length; i++){
				totalCtr[i] += counters[i]; 
			}			
			System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
			System.gc();
		}
		
		System.out.println("\n==========< Summary >==========");
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
		System.out.println("No/Invalid Classification Image Count:" + invalidImageCtr);
		System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
		writeToFile(fileName, totalCtr, ROWS * ctr);
	}
	
	private void writeToFile(String fileName, int[] totalCtr, long rowCtr) throws IOException{
		FileWriter fw = new FileWriter(fileName);
		
		fw.write("All Image Counter: " + totalCtr[CtrIndex.IMAGE_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Vertebrate Image Counter: " + totalCtr[CtrIndex.VERTEBRATE_IMAGE_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Invertebrate Image Counter: " + totalCtr[CtrIndex.INVERTEBRATE_IMAGE_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Plant Image Counter: " + totalCtr[CtrIndex.PLANT_IMAGE_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Other Image Counter: " + totalCtr[CtrIndex.OTHER_IMAGE_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Vertebrate Name Counter: " + totalCtr[CtrIndex.VERTEBRATE_NAME_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Invertebrate Name Counter: " + totalCtr[CtrIndex.INVERTEBRATE_NAME_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Plant Name Counter: " + totalCtr[CtrIndex.PLANT_NAME_CTR_INDEX.ordinal()] + "\r\n");
		fw.write("Other Name Counter: " + totalCtr[CtrIndex.OTHER_NAME_CTR_INDEX.ordinal()] + "\r\n");		
		fw.write("\nRow Counter: " + rowCtr + "\r\n");
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
	private int[] getBieReportCount(List<KeySlice> keySlices){
		int[] ctrs = new int[NUMBER_OF_COUNTER] ;		
		
		for (KeySlice keySlice : keySlices) {
			for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
				if (columns.isSetSuper_column()) {
					SuperColumn scol = columns.getSuper_column();
					int[] taxaCtr = getAusTaxaCount(scol);
					for(int i = 0; i < taxaCtr.length; i++){
						ctrs[i] += taxaCtr[i];
					}			

					int ctr = getAusImageCount(scol);
					ctrs[CtrIndex.IMAGE_CTR_INDEX.ordinal()] += ctr;
				}
			}
		}
		return ctrs;
	}
	
	/**
	 * do counting of aus image.
	 * 
	 * @param scol
	 * @return
	 */
	private int getAusImageCount(SuperColumn scol){
		int ctr = 0;
		String value = null;
		String colName = null;
		boolean isAustralian = false;
		boolean hasImages = false;
		
		//scan all columns
		for (Column col : scol.getColumns()) {
			try {
				value = new String(col.getValue(), CHARSET_ENCODING);
				colName = new String(col.getName(), CHARSET_ENCODING);
				if("IsAustralian".equalsIgnoreCase(colName) && "true".equalsIgnoreCase(value)){
					isAustralian = true;
				}
				if("hasImage".equalsIgnoreCase(colName)){
					List<Image> images = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Image.class));
					ctr = images.size();
					hasImages = true;
				}
				logger.debug("col.getName(): " +  colName + " col.getValue(): " + value);
			} catch (Exception e) {
				logger.error(e);
			} 	
		}	
		
		if(!(isAustralian && hasImages)){
			//reset counter
			ctr = 0;
		}
		else{
			try {
				logger.debug("SuperColumn Name(): " +  new String(scol.getName(), CHARSET_ENCODING));
			} catch (UnsupportedEncodingException e) {
				//do nothing...
			}
		}		
		return ctr;
	}
		
	/**
	 * do counting of vertebrate, invertebrate, plant and other in Australia.
	 * 
	 * @param scol
	 * @return
	 */
	private int[] getAusTaxaCount(SuperColumn scol){
		int[] ctr = new int[NUMBER_OF_COUNTER];
		int imageCtr = 0;
		int synonymCtr = 0;
		String value = null;
		String colName = null;
		boolean isAustralian = false;
		boolean hasImages = false;
		boolean hasSynonym = false;
		Taxa taxa = Taxa.INVALID;
		
		//scan all columns
		for (Column col : scol.getColumns()) {
			try {
				value = new String(col.getValue(), CHARSET_ENCODING);
				colName = new String(col.getName(), CHARSET_ENCODING);
				if("IsAustralian".equalsIgnoreCase(colName) && "true".equalsIgnoreCase(value)){
					isAustralian = true;
				}
				if("hasClassification".equalsIgnoreCase(colName)){
					List<Classification> classifications = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Classification.class));
					taxa = getClassification(classifications);
				}
				if("hasImage".equalsIgnoreCase(colName)){
					List<Image> images = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Image.class));
					imageCtr = images.size();
					hasImages = true;
				}
				if("hasSynonym".equalsIgnoreCase(colName)){
					List<TaxonConcept> synonym = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, TaxonConcept.class));
					synonymCtr = synonym.size();
					hasSynonym = true;
				}
				
				logger.debug("col.getName(): " +  colName + " col.getValue(): " + value);
			} catch (Exception e) {
				logger.error(e);
			} 	
		}	
		
		//populate counter
		if(isAustralian && (!Taxa.INVALID.equals(taxa)) && (hasImages || hasSynonym)){
			switch(taxa){
				case VERTEBRATE:
					if(hasImages){
						ctr[CtrIndex.VERTEBRATE_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
					}
					if(hasSynonym){
						ctr[CtrIndex.VERTEBRATE_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}
					break;
					
				case INVERTEBRATE:
					if(hasImages){
						ctr[CtrIndex.INVERTEBRATE_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
					}
					if(hasSynonym){
						ctr[CtrIndex.INVERTEBRATE_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
				
				case PLANT:
					if(hasImages){
						ctr[CtrIndex.PLANT_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
					}
					if(hasSynonym){
						ctr[CtrIndex.PLANT_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
					
				case OTHER:
					if(hasImages){
						ctr[CtrIndex.OTHER_IMAGE_CTR_INDEX.ordinal()] = imageCtr;
					}
					if(hasSynonym){
						ctr[CtrIndex.OTHER_NAME_CTR_INDEX.ordinal()] = synonymCtr;
					}					
					break;
					
				default:
					//reset counter
					ctr = new int[NUMBER_OF_COUNTER];
					break;
				
			}
		}
		else {
			//reset counter
			ctr = new int[NUMBER_OF_COUNTER];
			if(isAustralian && Taxa.INVALID.equals(taxa) && hasImages){
				logger.debug("**** No/Invalid classification Image:" + imageCtr);
				invalidImageCtr += imageCtr;				
			}
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
		if(PLANT_LIST.contains(classifications.get(0).getKingdom().toLowerCase())){
			taxa = Taxa.PLANT;
		}
		else if(INVERTEBRATE_LIST.contains(classifications.get(0).getPhylum().toLowerCase())){
			taxa = Taxa.INVERTEBRATE;
		}
		else if(VERTEBRATE_LIST.contains(classifications.get(0).getPhylum().toLowerCase())){
			taxa = Taxa.VERTEBRATE;
		}
		return taxa;
	}
	
	//========= Getter & Setter ========
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
}
