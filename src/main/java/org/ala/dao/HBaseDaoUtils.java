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

import org.apache.hadoop.hbase.client.HTable;
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
		RowResult row = htable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(columnFamily)});
		if(row==null){
			logger.error("Unable to find the row for guid: "+guid);
			return false;
		}
		
		Cell cell = row.get(Bytes.toBytes(columnName));
		List objectList = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		
		if(cell!=null){
			byte[] value = cell.getValue();
			objectList = mapper.readValue(value, 0, value.length, typeReference);
		} else {
			objectList = new ArrayList();
		}
		
		if(objectList.contains(object)){
			int idx = objectList.indexOf(object);
			//replace with this version
			objectList.remove(idx);
			objectList.add(object);
		} else {
			objectList.add(object);
		}
		
		//sort the objects
		Collections.sort(objectList);
		//save in HBase
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		//serialise to json
		String commonNamesAsJson = mapper.writeValueAsString(objectList); 
		batchUpdate.put(columnName, Bytes.toBytes(commonNamesAsJson));
		htable.commit(batchUpdate);
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
	public static String getField(RowResult rowResult, String column) {
		Cell cell = rowResult.get(column);
		if(cell!=null && cell.getValue()!=null){
			return new String(rowResult.get(column).getValue());	
		}
		return null;
	}
}
