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

import org.ala.util.CassandraClientPool;
import org.apache.log4j.Logger;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.JsonNode;

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

	private CassandraClientPool pool ;	
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
		
//		ApplicationContext context = SpringUtils.getContext();
//		CassandraBatchDelete casBatch = (CassandraBatchDelete) context.getBean(CassandraBatchDelete.class);
//		casBatch.doFullScanAndDelete(args);
		
//		CassandraHelper cassandraHelper = new CassandraHelper();
//		CassandraBatchDelete cassandraBatchDelete = cassandraHelper.getBatchDelete("bie", "tc", "localhost", 9160);
//		cassandraBatchDelete.doFullScanAndDelete(args);	
		
		CassandraBatchDelete cassandraBatchDelete = new CassandraBatchDelete();
		cassandraBatchDelete.doFullScanAndDelete(args);			
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
		pool = new CassandraClientPool(host, port);
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

		Client client = pool.getClient();
		Client delClient = pool.getClient();
		
		// Iterate over all the rows in a ColumnFamily......
		// start with the empty string, and after each call use the last key read as the start key 
		// in the next iteration.
		// when lastKey == startKey is finish.
		List<KeySlice> keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
		List<DeleteItemInfo> delList = getDeleteItemsList(keySlices, infoSourceIds);	
		int delCtr = doValueUpdate(delList, infoSourceIds, delClient);
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
				delCtr = doValueUpdate(delList, infoSourceIds, delClient);
				totalDelCtr += delCtr;
			}
			System.out.println("Total Delete Count:" + totalDelCtr);
			System.out.println("Row Count:" + (ROWS * ctr++) + " >>>> lastKey: " + lastKey.getKey());
			System.gc();
		}
		pool.returnClient(client);
		pool.returnClient(delClient);
		System.out.println("Total time taken: "	+ (System.currentTimeMillis() - start));
	}
		
	/**
	 * do update with cassandra repository.
	 * @param delList
	 * @param infoSourceIds
	 * @param client
	 * @return
	 * @throws Exception
	 */
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
			if(value.indexOf("\"infoSourceId\":\"" + infoSourceId + "\"") != -1){
				return true;
			}
		}
		return b;
	}

	private boolean hasInfoSourceId(JsonNode rootNode, String[] infoSourceIds){
		boolean b = false;
		
		for(String infoSourceId : infoSourceIds){
			String s = rootNode.path("infoSourceId").getTextValue();
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
