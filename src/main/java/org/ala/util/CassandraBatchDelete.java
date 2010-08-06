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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.SuperColumn;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.wyki.cassandra.pelops.Mutator;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;
import org.wyki.cassandra.pelops.Selector;

/**
 * Cassandra Batch Delete.
 * 
 * @author MOK011
 * 
 */
//@Component
public class CassandraBatchDelete {
	protected Logger logger = Logger.getLogger(this.getClass());

	public static final int ROWS = 1000;
	public static final String CHARSET_ENCODING = "UTF-8";
	public static final String POOL_NAME = "ALA";
	
	private String host = "localhost";
	private int port = 9160;
	private String keyspace = "bie";
	private String columnFamily = "tc";
	
	
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		if (args.length < 1) {
			System.out.println("Please provide a list of infoSourceId....");
			System.exit(0);
		}
				
		CassandraBatchDelete cassandraBatchDelete = new CassandraBatchDelete();
		cassandraBatchDelete.doFullScanAndDelete(args);
		cassandraBatchDelete.closeConnectionPool();
	}

	public CassandraBatchDelete(){
		this("bie", "tc", "localhost", 9160);
	}
	
	public CassandraBatchDelete(String host, int port){
		this("bie", "tc", host, port);
	}
	
	public CassandraBatchDelete(String keySpace, String columnFamily, String host, int port){
		this.keyspace = keySpace;
		this.columnFamily = columnFamily;
		this.host = host;
		this.port = port;
		Pelops.addPool(POOL_NAME, new String[]{this.host}, this.port, false, this.keyspace, new Policy());
	}
	
	public void closeConnectionPool(){
		Pelops.shutdown();
	}
	
	/**
	 * scan whole columnFamily tree, any column contains infoSourceId is equal to user input
	 * then delete this column. 
	 * @param infoSourceIds 
	 * @throws Exception
	 */
	public void doFullScanAndDelete(String[] infoSourceIds) throws Exception {
		long start = System.currentTimeMillis();
		long ctr = 1;
		long totalDelCtr = 0;
		KeySlice startKey = new KeySlice();
		KeySlice lastKey = null;
		
		System.out.println("Delete process is started.....");
		
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
		List<DeleteItemInfo> delList = getDeleteItemsList(keySlices, infoSourceIds);
		//dump test case
		/*
		delList = getDumpDeleteItemsList();
		*/
		int delCtr = doValueUpdate(delList, infoSourceIds);
		totalDelCtr += delCtr;
		logger.debug("Delete Count:" + delCtr);

		while (keySlices.size() > 0){
			lastKey = keySlices.get(keySlices.size()-1);
			//end of row ?
			if(lastKey.equals(startKey)){
				break;
			}
			startKey = lastKey;
			keyRange.setStart_key(lastKey.getKey());			
			keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
			delList = getDeleteItemsList(keySlices, infoSourceIds);
			if(delList.size() > 0){
				delCtr = doValueUpdate(delList, infoSourceIds);
				totalDelCtr += delCtr;
			}
			System.out.println("Total Column Update Count:" + totalDelCtr);
			System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
			System.gc();
		}				
		System.out.println("Total time taken (sec): "	+ ((System.currentTimeMillis() - start)/1000));
	}
	
	/*
	private List<DeleteItemInfo> getDumpDeleteItemsList(){
		List<DeleteItemInfo> l = new ArrayList<DeleteItemInfo>();
		l.add(new DeleteItemInfo("urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537", "tc", "hasImage"));
		return l;
	}
	*/
	
	/**
	 * do update with cassandra repository.
	 * @param delList
	 * @param infoSourceIds
	 * @return
	 * @throws Exception
	 */
	private int doValueUpdate(List<DeleteItemInfo> delList, String[] infoSourceIds) throws Exception{
		int ctr = 0;
		Selector selector = Pelops.createSelector(POOL_NAME, keyspace);
		
		for(DeleteItemInfo item : delList){		
			//get cassandra value
			Column col = selector.getSubColumnFromRow(item.getKey(), columnFamily, item.getSColName(), item.getColName(), ConsistencyLevel.ONE);
	        String casJson = getJsonValue(col);
	        if(casJson != null && casJson.length() > 0){
		        // do update ....
		        String json = doDelete(casJson, infoSourceIds);
		        System.out.println("guid: " + item.getKey() + " Col Name: " + item.getColName());
		        System.out.println(">>>> Before: " + casJson);
		        System.out.println("\n\n>>>> After: " + json);
		        
		        Mutator mutator = Pelops.createMutator(POOL_NAME, keyspace);
		        if(json != null && json.length() > 0){		        	
		        	try{
		    			mutator.writeSubColumn(item.getKey(), columnFamily, item.getSColName(), mutator.newColumn(item.getColName(), json));
		    			mutator.execute(ConsistencyLevel.ONE);
		    		} catch (Exception e){
		    			logger.error(e.getMessage(),e);
		    		}			        	
		        }
		        else{
		        	mutator.deleteSubColumn(item.getKey(), columnFamily, item.getSColName(), item.getColName());
		        	mutator.execute(ConsistencyLevel.ONE);
		        }
	        }
			ctr++;			
		}
		return ctr;
	}

	/**
	 * NOT USED
	 * do update with cassandra repository.
	 * @param delList
	 * @param infoSourceIds
	 * @param client
	 * @return
	 * @throws Exception
	 */
	/*
	private int doValueUpdate(List<DeleteItemInfo> delList, String[] infoSourceIds, Client client) throws Exception{
		int ctr = 0;
		ColumnPath colPath = new ColumnPath(columnFamily);
		for(DeleteItemInfo item : delList){		
			colPath.setSuper_column(item.getSColName().getBytes());
	        colPath.setColumn(item.getColName().getBytes());
	        //get cassandra value
	        ColumnOrSuperColumn columns = client.get(keyspace, item.getKey(), colPath, ConsistencyLevel.ONE);
	        String casJson = getJsonValue(columns);
	        if(casJson != null && casJson.length() > 0){
		        // do update ....
		        String json = doDelete(casJson, infoSourceIds);
		        if(json != null && json.length() > 0){
			        client.insert(keyspace, item.getKey(), colPath, json.getBytes(CHARSET_ENCODING),
			        		System.currentTimeMillis(), ConsistencyLevel.ONE);
		        }
		        else{
		        	client.remove(keyspace, item.getKey(), colPath, System.currentTimeMillis(), ConsistencyLevel.ONE);
		        }
	        }
			ctr++;			
		}
		return ctr;
	}	
	 */
	
	private String getJsonValue(Column column){
		String value = "";
		if (column != null) {
			try {
				value = new String(column.getValue(), CHARSET_ENCODING);
			} catch (UnsupportedEncodingException e) {
				logger.debug(e.toString());				
			}
		}
		return value;		
	}

	/*
	private String getJsonValue(ColumnOrSuperColumn columns){
		String value = "";
		if (columns != null && columns.isSetColumn()) {
			Column col = columns.getColumn();
			try {
				value = new String(col.getValue(), CHARSET_ENCODING);
			} catch (UnsupportedEncodingException e) {
				logger.debug(e.toString());				
			}
		}
		if (columns != null && columns.isSetSuper_column()) {
			logger.debug(" +++++++++++ getJsonValue(): columns type is super column");	
			System.out.println(" +++++++++++ getJsonValue(): columns type is super column");
		}
		return value;		
	}
	*/
	
	/**
	 * if cassandra column have value of 'infoSourceId', then add the column info into list.
	 * @param keySlices
	 * @param infoSourceIds
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private List<DeleteItemInfo> getDeleteItemsList(List<KeySlice> keySlices, String[] infoSourceIds) throws UnsupportedEncodingException{
		List<DeleteItemInfo> l = new ArrayList<DeleteItemInfo>();
		
		String value = null;
		String colName = null;
		String sColName = null;
		for (KeySlice keySlice : keySlices) {
			for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
				if (columns.isSetSuper_column()) {
					SuperColumn scol = columns.getSuper_column();
					sColName = new String(scol.getName(), CHARSET_ENCODING);
					for (Column col : scol.getColumns()) {
						value = new String(col.getValue(), CHARSET_ENCODING);
						colName = new String(col.getName(), CHARSET_ENCODING);
						logger.debug("SuperColumn: col.getName(): " +  colName + " col.getValue(): " + value + " lastKey = " + keySlice.getKey());
						// check column infoSourceId
						if(hasInfoSourceId(value, infoSourceIds)){
							l.add(new DeleteItemInfo(keySlice.getKey(), sColName, colName));
						}						
					}
				} else {
					Column col = columns.getColumn();
					value = new String(col.getValue(), CHARSET_ENCODING);
					colName = new String(col.getName(), CHARSET_ENCODING);
					logger.debug("Column: col.getName(): " +  colName + " col.getValue(): " + value + " lastKey = " + keySlice.getKey());
					// check column infoSourceId
					if(hasInfoSourceId(value, infoSourceIds)){
						l.add(new DeleteItemInfo(keySlice.getKey(), colName));
					}																				
				}
			}
		}
		return l;
	}
	
	private boolean hasInfoSourceId(String value, String[] infoSourceIds){
		boolean b = false;
		
		for(String infoSourceId : infoSourceIds){
			Pattern p = Pattern.compile("\"infoSourceId\":\\s*\"" + infoSourceId + "\"");
			Matcher m = p.matcher(value);
			if (m.find()){
				return true;
			}
		}
		return b;
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
	
	/**
	 * convert json string to Jackson tree model, rebuild tree node without infoSourceId node.
	 * @param json jsonString
	 * @param infoSourceIds
	 * @return jsonString
	 */
	private String doDelete(String json, String[] infoSourceIds){
		String jStr = "";
		List<JsonNode> objectList = new ArrayList<JsonNode>();
		ObjectMapper mapper = new ObjectMapper();		
		JsonNode rootNode;
		try {			
			rootNode = mapper.readValue(json, JsonNode.class);
			if(!rootNode.isArray()){
				if(!this.hasInfoSourceId(rootNode, infoSourceIds)){
					jStr = json;
				}
			}
			else{
				JsonNode next = null;
				Iterator<JsonNode> it = rootNode.iterator();
				while(it.hasNext()){
					next = it.next();
					logger.debug(next.toString());
					if(!this.hasInfoSourceId(next, infoSourceIds)){				
						objectList.add(next);
					}				
				}
				if(objectList.size() > 0){
					jStr = mapper.writeValueAsString(objectList);
				}
			}			 			
		} catch (Exception e) {
			logger.info("doDelete(): " + e.toString());
		} 		
		return jStr;		
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
}
