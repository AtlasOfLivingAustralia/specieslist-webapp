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

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;

/**
 * An implementation of a scanner for Cassandra. 
 * 
 * TODO extend this to include support for retrieving multiple
 * columns in a scan.
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
		
		this.clientConnection = clientConnection;
		this.keySpace = keySpace;
		this.columnParent = new ColumnParent(columnFamily);
		this.slicePredicate = new SlicePredicate();
		
		List<byte[]> colNames = new ArrayList<byte[]>();
		colNames.add(column.getBytes());
		slicePredicate.setColumn_names(colNames);
		
		//get the first page of data preloaded
		this.keySlices = clientConnection.get_range_slice(keySpace, columnParent, slicePredicate, "", "", pageSize, ConsistencyLevel.ONE);
	}
	
	/**
	 * @see org.ala.dao.Scanner#getNextGuid()
	 */
	@Override
	public byte[] getNextGuid() throws Exception {

		if(keySlices.size() > countInSlice){
			//return one off the pile
			byte[] guid = keySlices.get(countInSlice).getKey().getBytes();
			countInSlice++;
			return guid;
		} else if(!keySlices.isEmpty()){
			
			KeySlice lastKey = keySlices.get(keySlices.size()-1);
			keySlices = clientConnection.get_range_slice(keySpace, columnParent, slicePredicate, lastKey.getKey(), "", pageSize, ConsistencyLevel.ONE);
			keySlices.remove(lastKey);
			
			//reset counter
			countInSlice = 0;
			if(keySlices.isEmpty()){
				//indicate we are at the end
				return null;
			} else {
				byte[] guid = keySlices.get(countInSlice).getKey().getBytes();
				countInSlice++;
				return guid;
			}
		} else {
			return null;
		}
	}

	/**
	 * A bit of test code for scanning.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CassandraHelper c = new CassandraHelper();
		c.init();
		CassandraScanner s = new CassandraScanner(c.getConnection(),"bie", "tc", "taxonConcept");
		byte[] guid = null;
		int i = 0;
//		TreeSet<String> set = new TreeSet<String>();
		
		while((guid=s.getNextGuid())!=null){
			i++;
//			set.add(new String(guid));
			if(i%100==0){
				System.out.println("record: "+i+" guid:"+new String(guid));
			}
		}
		
//		System.out.println("first loaded: "+ set.first());
//		System.out.println("last loaded: "+ set.last());
		System.out.println("row count: "+i);
//		System.out.println("set count: "+set.size());
	}
	

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
}
