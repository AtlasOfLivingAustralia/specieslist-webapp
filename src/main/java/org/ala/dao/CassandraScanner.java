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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.apache.cassandra.utils.ByteBufferUtil


import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.scale7.cassandra.pelops.Bytes;
import org.scale7.cassandra.pelops.Pelops;
import org.scale7.cassandra.pelops.Selector;

/**
 * An implementation of a scanner for Cassandra. 
 * 
 * TODO extend this to include support for retrieving multiple
 * columns in a scan.
 * 
 * - 2011-11-22: Changed to use Pelops instead of using thrift directly
 * - 2011=11-12: Added the ability to retrieve the record values that were requested
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class CassandraScanner implements Scanner {

	private String keySpace;
	private int pageSize = 100;
	private List<KeySlice> keySlices;
	private int countInSlice = 0;
	private Cassandra.Client clientConnection;
	private SlicePredicate slicePredicate;
	private ColumnParent columnParent;
	private String pool;
	private String columnFamily;
	private Selector selector;
	private Map<Bytes,List<Column>> rowMap;
	private List<Bytes> rowList;
	private HashMap<String,String> currentValues = new HashMap<String,String>();
    //initialise the object mapper
    ObjectMapper mapper = new ObjectMapper();
//  mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
   
	/**
	 * Initialise a client connection
	 * 
	 * @param clientConnection
	 * @param keySpace
	 * @param columnFamily
	 * @param column
	 * @throws Exception
	 */
	public CassandraScanner(Cassandra.Client clientConnection, String keySpace, String columnFamily, String column) throws Exception {
		
//		this.clientConnection = clientConnection;
//		this.keySpace = keySpace;
//		this.columnParent = new ColumnParent(columnFamily);
//		this.slicePredicate = new SlicePredicate();
//		
//		List<ByteBuffer> colNames = new ArrayList<ByteBuffer>();
//		if(column != null && column.length() > 0){
//			colNames.add(ByteBufferUtil.bytes(column));
//			slicePredicate.setColumn_names(colNames);
//		}
//		else{
//			SliceRange sliceRange = new SliceRange();
//			sliceRange.setStart(new byte[0]);
//			sliceRange.setFinish(new byte[0]);
//
//			slicePredicate.setSlice_range(sliceRange);
//		}
//		
//		//get the first page of data preloaded
//		this.keySlices = clientConnection.get_range_slice(keySpace, columnParent, slicePredicate, "", "", pageSize, ConsistencyLevel.ONE);
	}
	
	public CassandraScanner(String pool, String keySpace, String columnFamily, String... column) throws Exception {
	    this.pool = pool;
	    this.keySpace = keySpace;
	    this.selector = Pelops.createSelector(pool);
	    this.slicePredicate = Selector.newColumnsPredicate(column);
	    this.columnFamily = columnFamily;
	    KeyRange keyRange = Selector.newKeyRange("", "", pageSize+1);
	    rowMap =selector.getColumnsFromRows(columnFamily, keyRange, slicePredicate, ConsistencyLevel.ONE);
	    rowList = new ArrayList<Bytes>(rowMap.keySet());//rowMap.keySet();
	    mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
//	public Map<String,String> getCurrentValues() throws Exception {
//	    if(countInSlice >0){
//	        List<Column> columns = rowMap.get(rowList.get(countInSlice-1));
//	        HashMap<String,String> map = new HashMap<String,String>(); 
//	        for(Column column: columns){
//	            String name = new String(column.getName(),"UTF-8");
//	            String value = new String(column.getValue(),"UTF-8");
//	            map.put(name, value);
//	        }
//	        return map;
//	    }
//	    return null;
//	}
	
	private void initCurrentValues(List<Column> columns) throws Exception{
	    currentValues.clear();
	    for(Column column: columns){
          String name = new String(column.getName(),"UTF-8");
          String value = new String(column.getValue(),"UTF-8");
          currentValues.put(name, value);
      }
	}
	
	public Comparable getValue(String column, Class theClass)throws Exception {
	    if(currentValues.containsKey(column)){
	        return (Comparable) mapper.readValue(currentValues.get(column), theClass);
	    }
	    return null;
	}
	
	public List<Comparable> getListValue(String column, Class theClass)throws Exception {
	    if(currentValues.containsKey(column)){
	        return  mapper.readValue(currentValues.get(column), TypeFactory.collectionType(ArrayList.class, theClass));
	    }
	    return new ArrayList<Comparable>();
	}
	
	/**
	 * @see org.ala.dao.Scanner#getNextGuid()
	 */
	@Override
	public byte[] getNextGuid() throws Exception {
	    
	    if(rowList.size()>countInSlice){
	        //return one off the pile
	        byte[]guid = rowList.get(countInSlice).toByteArray();
	        initCurrentValues(rowMap.get(rowList.get(countInSlice)));
	        countInSlice++;
	        return guid;
	    }
	    else if(!rowList.isEmpty()){
	        Bytes lastBytes = rowList.get(rowList.size()-1);
	        String lastKey = lastBytes.toUTF8();
	        KeyRange keyRange = Selector.newKeyRange(lastKey, "", pageSize+1);
	        rowMap =selector.getColumnsFromRows(columnFamily, keyRange, slicePredicate, ConsistencyLevel.ONE);
	        rowList = new ArrayList<Bytes>(rowMap.keySet());
	        rowList.remove(lastBytes);
	        
	        //reset the counter
	        countInSlice = 0;
	        if(rowMap.isEmpty()){
	            //indicate that we are at the end
	            return null;
	        }
	        else{
	            byte[]guid = rowList.get(countInSlice).toByteArray();
	            initCurrentValues(rowMap.get(rowList.get(countInSlice)));
	            countInSlice++;
	            return guid;
	        }
	                
	    }
	    else{
	        return null;
	    }
	    

//		if(keySlices.size() > countInSlice){
//			//return one off the pile
//			byte[] guid = keySlices.get(countInSlice).getKey().getBytes();
//			countInSlice++;
//			return guid;
//		} else if(!keySlices.isEmpty()){
//			
//			KeySlice lastKey = keySlices.get(keySlices.size()-1);
//			keySlices = clientConnection.get_range_slice(keySpace, columnParent, slicePredicate, lastKey.getKey(), "", pageSize, ConsistencyLevel.ONE);
//			keySlices.remove(lastKey);
//			
//			//reset counter
//			countInSlice = 0;
//			if(keySlices.isEmpty()){
//				//indicate we are at the end
//				return null;
//			} else {
//				byte[] guid = keySlices.get(countInSlice).getKey().getBytes();
//				countInSlice++;
//				return guid;
//			}
//		} else {
//			return null;
//		}
	}

//	/**
//	 * A bit of test code for scanning.
//	 *
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(String[] args) throws Exception {
//		CassandraHelper c = new CassandraHelper();
//		c.init();
//		CassandraScanner s = new CassandraScanner(c.getConnection(),"bie", "tc", "taxonConcept");
//		byte[] guid = null;
//		int i = 0;
////		TreeSet<String> set = new TreeSet<String>();
//
//		while((guid=s.getNextGuid())!=null){
//			i++;
////			set.add(new String(guid));
//			if(i%100==0){
//				System.out.println("record: "+i+" guid:"+new String(guid));
//			}
//		}
//
////		System.out.println("first loaded: "+ set.first());
////		System.out.println("last loaded: "+ set.last());
//		System.out.println("row count: "+i);
////		System.out.println("set count: "+set.size());
//	}
//

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public CassandraScanner(Cassandra.Client clientConnection, String keySpace, String columnFamily) throws Exception {
		
//		this.clientConnection = clientConnection;
//		this.keySpace = keySpace;
//		this.columnParent = new ColumnParent(columnFamily);
//		this.slicePredicate = new SlicePredicate();
//
//		KeyRange keyRange = new KeyRange(pageSize);
//		keyRange.setStart_key("");
//		keyRange.setEnd_key("");
//		
//		SliceRange sliceRange = new SliceRange();
//		sliceRange.setStart(new byte[0]);
//		sliceRange.setFinish(new byte[0]);
//		
//		slicePredicate.setSlice_range(sliceRange);
//				
//		//get the first page of data preloaded
//		this.keySlices = clientConnection.get_range_slices(keySpace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
	}
	
}
