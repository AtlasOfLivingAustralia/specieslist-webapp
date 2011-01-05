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

import java.util.List;
import java.util.Map;

import org.ala.util.ColumnType;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
/**
 * A skeleton implementation of a StoreHelper for HBase
 *
 * TODO Implementation to be added/ported from the current TaxonConceptDaoImpl
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class HBaseHelper implements StoreHelper {

	protected HBaseConfiguration config;
	
	protected HTable htable;
	
	protected String tableName = "taxonConcept";
	
	@Override
	public void init() throws Exception {
		HBaseConfiguration config = new HBaseConfiguration();
		//FIXME move table name out to spring configuration
		htable = new HTable(config, tableName);
		htable.setWriteBufferSize(1024*1024*12);
	}
	
	@Override
	public Comparable get(String table, String columnFamily, String columnName,
			String guid, Class theClass) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Comparable> getList(String table, String columnFamily,
			String columnName, String guid, Class theClass) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean put(String table, String columnFamily, String superColumn, String columnName,
			String guid, Comparable object) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean put(String table, String columnFamily, String columnName,
			String guid, Comparable object) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean putSingle(String table, String columnFamily,
			String columnName, String guid, Comparable object) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see org.ala.dao.StoreHelper#putList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean)
	 */
	@Override
	public boolean putList(String table, String columnFamily,
			String columnName, String guid, List<Comparable> object,
			boolean append) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#getScanner(java.lang.String, java.lang.String)
	 */
	@Override
	public Scanner getScanner(String table, String columnFamily, String column) throws Exception {
		return new HBaseScanner(htable, columnFamily);
	}
	
	public Map<String, Object> getSubColumnsByGuid(String columnFamily,String superColName,String guid) throws Exception{
		throw new NoSuchMethodException("No such method implementation in this class : " + this.getClass().getName());
	}

	@Override
	public Map<String, Map<String, Object>> getPageOfSubColumns(
			String tcColFamily, String superColumn, String startGuid,
			int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, Object>> getPageOfSubColumns(
			String tcColFamily, String superColumn, ColumnType[] subColumns,
			String startGuid, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}	
}
