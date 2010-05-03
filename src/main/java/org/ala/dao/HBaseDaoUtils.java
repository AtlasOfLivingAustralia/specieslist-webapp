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
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;

/**
 * Utilities for HBase DAO implementations.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class HBaseDaoUtils {
	
	protected static Logger logger = Logger.getLogger(HBaseDaoUtils.class);

	/**
	 * Store a complex object, handling duplicates in the list and sorting.
	 * 
	 * Duplicate handling is dependent on the complex objects overriding
	 * Object.equals in a sensible manner for a specific type of object.
	 * 
	 * @param guid
	 * @param columnFamily
	 * @param columnName
	 * @param object
	 * @param typeReference
	 * @return true if the object was stored, false otherwise
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static boolean storeComplexObject(HTable htable, String guid, String columnFamily, String columnName, Comparable object, TypeReference typeReference) throws Exception {
		Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(columnFamily));
		Result result = htable.get(getter);
		if (result.getRow() == null) {
			logger.error("Unable to find the row for guid: "+guid+". Unable to add object to "+columnName);
			return false;
		}
		
		byte [] columnValue = result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
		List objectList = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		
		if (columnValue != null) {
			objectList = mapper.readValue(columnValue, 0, columnValue.length, typeReference);
		} else {
			objectList = new ArrayList();
		}
		
		if (objectList.contains(object)) {
			int idx = objectList.indexOf(object);
			//replace with this version
			objectList.remove(idx);
			objectList.add(object);
		} else {
			objectList.add(object);
		}
		
		//sort the objects
		Collections.sort(objectList);
		// Serialise to JSON and save in HBase
		String objectsAsJson = mapper.writeValueAsString(objectList); 
		Put putter = new Put(Bytes.toBytes(guid)).add(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(objectsAsJson));
		htable.put(putter);
		return true;
	}
	
	/**
	 * Writes a cell to hbase. Note that any previous data for the cell is over written.
	 * 
	 * @param htable
	 * @param guid
	 * @param columnFamily
	 * @param columnName
	 * @param listOfObjects
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static boolean putComplexObject(HTable htable, String guid, String columnFamily, String columnName, List listOfObjects) throws Exception {
		Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(columnFamily));
		Result result = htable.get(getter);
		if (result.getRow() == null) {
			logger.error("Unable to find the row for guid: "+guid+". Unable to put column object to "+columnName);
			return false;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		
		// Serialise to JSON and save in HBase
		String objectsAsJson = mapper.writeValueAsString(listOfObjects); 
		Put putter = new Put(Bytes.toBytes(guid)).add(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(objectsAsJson));
		htable.put(putter);
		return true;
	}
	
	/**
	 * Retrieve the value for this column handling the possibility
	 * the column doesnt exist.
	 * 
	 * @param rowResult
	 * @param column
	 * @return the string value for this column
	 */
	public static String getField(Result result, String family, String column) {
		byte [] value = result.getValue(Bytes.toBytes(family), Bytes.toBytes(column));
		return Bytes.toString(value);
	}
}
