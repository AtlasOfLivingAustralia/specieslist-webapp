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
package org.ala.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.util.Bytes;
/**
 * Performance test for HBase.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class WideTableTest {
	
	final static int rows = 100000;
	final static int columns = 1000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//how many *things* say that this species has a particular synonym ?
		init();
		loadWideTable();
		loadThinTable();
		//query();
		System.exit(1);
	}	
	
	public static void init() throws Exception {
		
		// create
    	System.out.println("Initialising HBase Scale Test");
    	
    	HBaseConfiguration config = new HBaseConfiguration();
    	HBaseAdmin hBaseAdmin = new HBaseAdmin(config);
    	
    	if(hBaseAdmin.tableExists("widetable")){
    		hBaseAdmin.disableTable("widetable");
    		hBaseAdmin.deleteTable("widetable");
    	}
    	
    	if(hBaseAdmin.tableExists("thintable")){
    		hBaseAdmin.disableTable("thintable");
    		hBaseAdmin.deleteTable("thintable");
    	}
    	
    	//create wide table
    	HTableDescriptor htd = new HTableDescriptor("widetable");
    	htd.addFamily(new HColumnDescriptor("col:")); 
    	hBaseAdmin.createTable(htd);
    	
    	//create thin table
    	htd = new HTableDescriptor("thintable");
    	htd.addFamily(new HColumnDescriptor("col:"));
    	hBaseAdmin.createTable(htd);

    	System.out.println("Schema setup complete.");		
	}
	
	private static void loadThinTable() throws Exception {
		System.out.println("Starting thintable HBase load.");
		long start = System.currentTimeMillis();
		String colNamePrefix = "col:property_";
		HBaseConfiguration config = new HBaseConfiguration();
    	HTable tcTable = new HTable(config, "thintable");
    	
    	long last = System.currentTimeMillis();
    	
		//range of 0 to 1,000,000
		//for each row add 1000 cells or varying column names
		for(int i=0; i<rows; i++){
			if(i%10000==0 && i>0){
				long current = System.currentTimeMillis();
				System.out.println("rows loaded: "+i+" last 10000 loaded in: "+((current-last)/1000)+" seconds");
				last = current;
			}
			
			BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes("row_"+i));
			for(int j=0; j<columns; j++){
				batchUpdate.put(colNamePrefix+j, Bytes.toBytes("padding out the cell"+j));
			}
			tcTable.commit(batchUpdate);
		}
		
		long finish = System.currentTimeMillis();
		System.out.println("loaded rows:"+rows+", columns:"+columns+", cells: "+(rows*columns)+", in "+((finish-start)/60000)+" minutes.");
	}	


	private static void loadWideTable() throws Exception {
		System.out.println("Starting widetable HBase load.");
		long start = System.currentTimeMillis();
		String colNamePrefix = "col:property_";
		HBaseConfiguration config = new HBaseConfiguration();
    	HTable tcTable = new HTable(config, "widetable");
    	
    	long last = System.currentTimeMillis();
    	
		//range of 0 to 1,000,000
		Random randomGenerator = new Random();
		//for each row add 1000 cells or varying column names
		for(int i=0; i<rows; i++){
			if(i%10000==0 && i>0){
				long current = System.currentTimeMillis();
				System.out.println("rows loaded: "+i+" last 10000 loaded in: "+((current-last)/1000)+" seconds");
				last = current;
			}
			
			BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes("row_"+i));
			List<Integer> usedIndexes = new ArrayList<Integer>();
			
			//add columns with random headers
			for(int j=0; j<columns; j++){
				int randomInt = randomGenerator.nextInt(1000000);
				while(usedIndexes.contains(randomInt)){
					randomInt = randomGenerator.nextInt(1000000); 
				}
				usedIndexes.add(randomInt);
				
				batchUpdate.put(colNamePrefix+randomInt, Bytes.toBytes("padding out the cell "+j));
			}
			tcTable.commit(batchUpdate);
		}
		
		long finish = System.currentTimeMillis();
		System.out.println("loaded rows:"+rows+", columns:"+columns+", cells: "+(rows*columns)+", in "+((finish-start)/60000)+" minutes.");
	}
}
