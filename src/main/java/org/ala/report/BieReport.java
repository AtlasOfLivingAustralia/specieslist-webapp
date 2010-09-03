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
	private List<String> invertebrateList;
	private List<String> vertebrateList;
	private List<String> plantList;
	
	public static final int IMAGE_CTR_INDEX = 0;
	public static final int VERTEBRATE_CTR_INDEX = 1;
	public static final int INVERTEBRATE_CTR_INDEX = 2;
	public static final int PLANT_CTR_INDEX = 3;
	public static final int OTHER_CTR_INDEX = 4;
	public static final int NUMBER_OF_COUNTER = 5;
		
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
		
		invertebrateList = Arrays.asList("acanthocephala" ,"acoelomorpha", "annelida", "arthropoda",
				"brachiopoda", "bryozoa", "chaetognatha", "cnidaria", "ctenophora", "cycliophora", 
				"echinodermata", "entoprocta", "gastrotricha", "gnathostomulida", "hemichordata", 
				"kinorhyncha", "loricifera", "micrognathozoa", "mollusca", "nematoda", "nemertea",
				"onychophora", "phoronida", "platyhelminthes", "porifera", "priapulida", "rotifera", 
				"sipuncula", "tardigrada", "xenoturbellida");
		vertebrateList = Arrays.asList("chordata");
		plantList = Arrays.asList("plantae");
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
			totalCtr[IMAGE_CTR_INDEX] += counters[IMAGE_CTR_INDEX];
			totalCtr[VERTEBRATE_CTR_INDEX] += counters[VERTEBRATE_CTR_INDEX];
			totalCtr[INVERTEBRATE_CTR_INDEX] += counters[INVERTEBRATE_CTR_INDEX];
			totalCtr[PLANT_CTR_INDEX] += counters[PLANT_CTR_INDEX];
			totalCtr[OTHER_CTR_INDEX] += counters[OTHER_CTR_INDEX];
			
			System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
			System.gc();
		}
		
		System.out.println("\n==========< Summary >==========");
		System.out.println("Image Counter: " + totalCtr[IMAGE_CTR_INDEX]);
		System.out.println("Vertebrate Counter: " + totalCtr[VERTEBRATE_CTR_INDEX]);
		System.out.println("Invertebrate Counter: " + totalCtr[INVERTEBRATE_CTR_INDEX]);
		System.out.println("Plant Counter: " + totalCtr[PLANT_CTR_INDEX]);
		System.out.println("Other Counter: " + totalCtr[OTHER_CTR_INDEX]);
		System.out.println("Row Count:" + ROWS * ctr);
		System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
		writeToFile(fileName, totalCtr, ROWS * ctr);
	}
	
	private void writeToFile(String fileName, int[] totalCtr, long rowCtr) throws IOException{
		FileWriter fw = new FileWriter(fileName);
		
		fw.write("Image Counter: " + totalCtr[IMAGE_CTR_INDEX] + "\r\n");
		fw.write("Vertebrate Counter: " + totalCtr[VERTEBRATE_CTR_INDEX] + "\r\n");
		fw.write("Invertebrate Counter: " + totalCtr[INVERTEBRATE_CTR_INDEX] + "\r\n");
		fw.write("Plant Counter: " + totalCtr[PLANT_CTR_INDEX] + "\r\n");
		fw.write("Other Counter: " + totalCtr[OTHER_CTR_INDEX] + "\r\n");
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
					int ctr = getAusImageCount(scol);
					ctrs[IMAGE_CTR_INDEX] += ctr;
					
					int[] taxaCtr = getAusTaxaCount(scol);
					ctrs[VERTEBRATE_CTR_INDEX] += taxaCtr[VERTEBRATE_CTR_INDEX];
					ctrs[INVERTEBRATE_CTR_INDEX] += taxaCtr[INVERTEBRATE_CTR_INDEX];
					ctrs[PLANT_CTR_INDEX] += taxaCtr[PLANT_CTR_INDEX];
					ctrs[OTHER_CTR_INDEX] += taxaCtr[OTHER_CTR_INDEX];
					
					if(ctr > 0 || ctrs[VERTEBRATE_CTR_INDEX] > 0 || ctrs[INVERTEBRATE_CTR_INDEX] > 0 ||
							ctrs[PLANT_CTR_INDEX] > 0 || ctrs[OTHER_CTR_INDEX] > 0){
						logger.debug("*** IsAustralian guid: " + keySlice.getKey());
					}
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
		int listSize = 0;
		String value = null;
		String colName = null;
		boolean isAustralian = false;
		boolean hasClassification = false;
		
		//scan all columns
		for (Column col : scol.getColumns()) {
			try {
				value = new String(col.getValue(), CHARSET_ENCODING);
				colName = new String(col.getName(), CHARSET_ENCODING);
				if("IsAustralian".equalsIgnoreCase(colName) && "true".equalsIgnoreCase(value)){
					isAustralian = true;
				}
				else if("hasClassification".equalsIgnoreCase(colName)){
					List<Classification> classifications = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, Classification.class));
					listSize = classifications.size();
					ctr[VERTEBRATE_CTR_INDEX] = getVertebrateCount(classifications);
					ctr[INVERTEBRATE_CTR_INDEX] = getInvertebrateCount(classifications);
					ctr[PLANT_CTR_INDEX] = getPlantCount(classifications);
					ctr[OTHER_CTR_INDEX] = (listSize - (ctr[VERTEBRATE_CTR_INDEX] + ctr[INVERTEBRATE_CTR_INDEX] + ctr[PLANT_CTR_INDEX]));
					hasClassification = true;
				}
				logger.debug("col.getName(): " +  colName + " col.getValue(): " + value);
			} catch (Exception e) {
				logger.error(e);
			} 	
		}	
		
		if(!(isAustralian && hasClassification)){
			//reset counter
			ctr = new int[NUMBER_OF_COUNTER];
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
	 * check classification of vertebrate.
	 * 
	 * @param classifications
	 * @return
	 */
	private int getVertebrateCount(List<Classification> classifications){
		int ctr = 0;
		
		for (Classification classification : classifications) {
			if(vertebrateList.contains(classification.getPhylum().toLowerCase())){
				ctr++;
			}
		}
		return ctr;
	}
	
	/**
	 * check classification of invertebrate.
	 * 
	 * @param classifications
	 * @return
	 */
	private int getInvertebrateCount(List<Classification> classifications){
		int ctr = 0;
		
		for (Classification classification : classifications) {
			if(invertebrateList.contains(classification.getPhylum().toLowerCase())){
				ctr++;
			}
		}
		return ctr;
	}

	/**
	 * check classification of plant.
	 * 
	 * @param classifications
	 * @return
	 */
	private int getPlantCount(List<Classification> classifications){
		int ctr = 0;
		
		for (Classification classification : classifications) {
			if(plantList.contains(classification.getKingdom().toLowerCase()) ||
					plantList.contains(classification.getPhylum().toLowerCase())){
				ctr++;
			}
		}
		return ctr;
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
