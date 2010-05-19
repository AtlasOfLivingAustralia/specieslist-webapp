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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ala.model.AttributableObject;
import org.apache.commons.collections.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
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
	 * @param columnName family:cell
	 * @param object
	 * @param typeReference
	 * @return true if the object was stored, false otherwise
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static boolean storeComplexObject(HTable htable, String guid, String columnName, Comparable object, TypeReference typeReference) throws Exception {
		if (guid == null || guid.equals("")) {
			logger.error("Attempting to store column=" +  columnName + " with undefined key. Object=" + object);
			return false;
		}
		Get getter = new Get(Bytes.toBytes(guid)).addColumn(Bytes.toBytes(columnName));
		Result result = htable.get(getter);
		if (result.getRow() == null) {
			logger.error("Unable to find the row for guid: "+guid+". Unable to add object to "+columnName);
			return false;
		}
		
		byte [] columnValue = result.getValue(Bytes.toBytes(columnName));
		List objectList = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
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
		Put putter = new Put(Bytes.toBytes(guid));
		putter.add(Bytes.toBytes(columnName), putter.getTimeStamp(), Bytes.toBytes(objectsAsJson));
		htable.put(putter);
		return true;
	}
	
	/**
	 * Writes a cell to hbase. Note that any previous data for the cell is over written.
	 * 
	 * @param htable
	 * @param guid
	 * @param columnName family:cell
	 * @param listOfObjects
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static boolean putComplexObject(HTable htable, String guid, String columnName, List listOfObjects) throws Exception {
		if (guid == null || guid.equals("")) {
			logger.error("Attempting to put column=" +  columnName + " with undefined key.");
			return false;
		}
		Get getter = new Get(Bytes.toBytes(guid)).addColumn(Bytes.toBytes(columnName));
		Result result = htable.get(getter);
		if (result.getRow() == null) {
			logger.error("Unable to find the row for guid: "+guid+". Unable to put column object to "+columnName);
			return false;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		// Serialise to JSON and save in HBase
		String objectsAsJson = mapper.writeValueAsString(listOfObjects); 
		Put putter = new Put(Bytes.toBytes(guid));
		putter.add(Bytes.toBytes(columnName), putter.getTimeStamp(), Bytes.toBytes(objectsAsJson));
		htable.put(putter);
		return true;
	}
	
	/**
	 * Update a row if there is new or changed data.
	 * 
	 * @param htable
	 * @param guid
	 * @param kvList
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static boolean update(HTable htable, String guid, List<KeyValue> kvList) throws IOException {
		if (guid == null || guid.equals("")) {
			logger.error("Attempting to update Taxon Concept with undefined key.");
			return false;
		}
		Get getter = new Get(Bytes.toBytes(guid));
//		for (KeyValue kv : kvList) {
//			getter.addColumn(Bytes.toBytes((String) kv.getKey()));
//		}
		Result result = htable.get(getter);
		if (result.getRow() == null) {
			logger.error("Unable to find the row for guid: "+guid+". Unable to update row.");
			return false;
		}
		
		boolean dirty = false;
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		Put putter = new Put(Bytes.toBytes(guid));
		
		for (KeyValue kv : kvList) {
			String column = (String) kv.getKey();
			ArrayList<AttributableObject> valueList = (ArrayList<AttributableObject>) kv.getValue();
			List newValues = new ArrayList();
			String columnValue = Bytes.toString(result.getValue(Bytes.toBytes(column)));
			List objList = new ArrayList();
			if (columnValue != null) {
				objList = mapper.readValue(columnValue, TypeFactory.collectionType(ArrayList.class, valueList.get(0).getClass()));
				for (AttributableObject value : valueList) {
					if (!objList.contains(value)) {
						newValues.add(value);
					}
				}
			} else {
				newValues.addAll(valueList);
			}
			
			if (!newValues.isEmpty()) {
				dirty = true;
				objList.addAll(newValues);
				Collections.sort(objList);
				String objectsAsJson = mapper.writeValueAsString(objList); 
				putter.add(Bytes.toBytes(column), putter.getTimeStamp(), Bytes.toBytes(objectsAsJson));
			}
		}
		if (dirty ) {
			htable.put(putter);
		} else {
			logger.debug("Nothing to update");
		}
		return true;
	}
	
	/**
	 * Retrieve the value for this column handling the possibility
	 * the column doesnt exist.
	 * 
	 * @param rowResult
	 * @param column as family:cell
	 * @return the string value for this column
	 */
	public static String getField(Result result, String column) {
		byte [] value = result.getValue(Bytes.toBytes(column));
		return Bytes.toString(value);
	}
}
