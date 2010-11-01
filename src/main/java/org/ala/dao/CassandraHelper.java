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
import java.util.Map;

import org.ala.model.TaxonConcept;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * A StoreHelper implementation for the Cassandra platform.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Deprecated
public class CassandraHelper implements StoreHelper {

	protected static Logger logger = Logger.getLogger(CassandraHelper.class);
	
	protected static String keySpace = "bie";
	
	protected static Cassandra.Client clientConnection;
	
	protected String host = "localhost";
	
	protected int port = 9160;
	
	protected String charsetEncoding = "UTF-8";
        
        //The last time that the connection to cassandra was attempted
        protected long lastChecked =0;
        //The minimum time between attempts to connect to cassandra
        protected long checkFrequency = 10000; //check every 10 seconds

	/**
	 * @see org.ala.dao.StoreHelper#init()
	 */
	@Override
	public void init() throws Exception {
		getConnection();
	}

	/**
	 * Initialise the connection to Cassandra.
	 * 
	 * @return
	 * @throws TTransportException
	 */
	public Cassandra.Client getConnection() throws TTransportException {
            
		try {
			if(clientConnection==null){
                            //only initialise the client connection if it is the first time or reached checkFrequency
                            if(lastChecked ==0 || System.currentTimeMillis() > lastChecked + checkFrequency){
				lastChecked = System.currentTimeMillis();
                                TTransport tr = new TSocket(host, port);
                                TProtocol proto = new TBinaryProtocol(tr);
                                this.clientConnection = new Cassandra.Client(proto);
                                tr.open();
                            }
                            else{
                                
                                throw new TTransportException();
                            }
			}
			return this.clientConnection;
		} catch (TTransportException e) {
			if(e.getMessage()!= null)
                            logger.error("Unable to initialise connection to Cassandra server. Using host: "+host+", and port:"+port);
			throw e;
		}
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#getList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class)
	 */
	public List<Comparable> getList(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
		
		ColumnPath columnPath = new ColumnPath(columnFamily);
		columnPath.setSuper_column(columnFamily.getBytes());
		columnPath.setColumn(columnName.getBytes());
        ColumnOrSuperColumn col = null;
        
        try {
        	col = getConnection().get(keySpace, guid, columnPath, ConsistencyLevel.ONE);
        } 
        catch (TTransportException e){
            //NC: This is a quick fix for communication issues between the webapp server and the cassandra server.
            //TODO We possibly want to implement connection pooling/management for Cassandra connections
            if(e.getMessage() != null)
                logger.info("Unable to contact Cassandra. Attempt to reinitialise the connection next time it is used.");
            this.clientConnection = null; //reinitialise the connection next time
        }
        catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		//read the existing value
		List<Comparable> objectList = null;
		if(col!=null){
			String value = new String(col.getColumn().value, charsetEncoding);
//			logger.info(value);
			objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, theClass));
		} else {
			objectList = new ArrayList<Comparable>();
		}
		return objectList;
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class)
	 */
	public Comparable get(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
		
		ColumnPath columnPath = new ColumnPath(columnFamily);
		columnPath.setSuper_column(columnFamily.getBytes());
		columnPath.setColumn(columnName.getBytes());
        ColumnOrSuperColumn col = null;
        try {
        	col = getConnection().get(keySpace, guid, columnPath, ConsistencyLevel.ONE);
        } 
        catch (TTransportException e){
            //NC: This is a quick fix for communication issues between the webapp server and the cassandra server.
            //TODO We possibly want to implement connection pooling/management for Cassandra connections
            if(e.getMessage() != null)
                logger.info("Unable to contact Cassandra. Attempt to reinitialise the connection next time it is used.");
            this.clientConnection = null;//reinitialise the connection next time
        }
        catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		//read the existing value
		if(col!=null){
			String value = new String(col.getColumn().value,charsetEncoding);
//			logger.info(value);
			return (Comparable) mapper.readValue(value, theClass);
		} else {
			return null;
		}
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#putSingle(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
	 */
	public boolean putSingle(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
		
		guid =  StringUtils.trimToNull(guid);
		if(guid==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+object);
			return false;
		}
		
//        ColumnPath path = new ColumnPath(table, columnFamily.getBytes(), columnName.getBytes("UTF-8"));
		ColumnPath columnPath = new ColumnPath(columnFamily);
		columnPath.setSuper_column(columnFamily.getBytes());
		columnPath.setColumn(columnName.getBytes());
//        ColumnOrSuperColumn col = null;
//        try {
//        	col = getConnection().get(keySpace, guid, path, ConsistencyLevel.ONE);
//        } catch (Exception e){
//        	//expected behaviour. current thrift API doesnt seem
//        	//to support a retrieve null getter
//        }

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		//convert to JSON
		String json = mapper.writeValueAsString(object); 
		long timestamp = System.currentTimeMillis();
		
		//insert into table
		getConnection().insert(keySpace, //keyspace
	        	guid,
	        	columnPath,   // columnFamily = tc
                json.getBytes(charsetEncoding),
                timestamp,
                ConsistencyLevel.ONE);
		
		return true;
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#putList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean)
	 */
	@Override
	public boolean putList(String table, String columnFamily, String columnName, String guid, List<Comparable> objects, boolean append) throws Exception {
        
		guid =  StringUtils.trimToNull(guid);
		if(guid==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+objects);
			return false;
		}
		
		ColumnPath columnPath = new ColumnPath(columnFamily);
		columnPath.setSuper_column(columnFamily.getBytes());
		columnPath.setColumn(columnName.getBytes());

        ColumnOrSuperColumn col = null;
        try {
        	col = getConnection().get(keySpace, guid, columnPath, ConsistencyLevel.ONE);
        } catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

		String json = null;
		
		if(append){
		
			//read the existing value
			List<Comparable> objectList = null;
			if(col!=null){
				String value = new String(col.getColumn().value, charsetEncoding);
				
				if(!objects.isEmpty()){
					Object first = objects.get(0);
					objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, first.getClass()));
				} else {
					objectList = new ArrayList<Comparable>();
				}
			} else {
				objectList = new ArrayList<Comparable>();
			}
			//FIXME not currently checking for duplicates
			objectList.addAll(objects);
			json = mapper.writeValueAsString(objectList);
			
		} else {
			
			Collections.sort(objects);
			//convert to JSON
			json = mapper.writeValueAsString(objects);
		}

		long timestamp = System.currentTimeMillis();
		
		//insert into table
		getConnection().insert(keySpace, //keyspace
	        	guid,
	        	columnPath,
                json.getBytes(charsetEncoding),
                timestamp,
                ConsistencyLevel.ONE);
		
		return true;
	}

	/**
	 * @see org.ala.dao.StoreHelper#put(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
	 */
	public boolean put(String table, String columnFamily, String superColumn, String columnName, String guid, Comparable object) throws Exception {
		guid =  StringUtils.trimToNull(guid);
		if(guid==null || object==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+object);
			return false;
		}
		
		ColumnPath columnPath = new ColumnPath(columnFamily);
		columnPath.setSuper_column(superColumn.getBytes());
		columnPath.setColumn(columnName.getBytes());

        ColumnOrSuperColumn col = null;
        try {
        	col = getConnection().get(keySpace, guid, columnPath, ConsistencyLevel.ONE);
        } catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		//read the existing value
		List<Comparable> objectList = null;
		if(col!=null){
			String value = new String(col.getColumn().value, charsetEncoding);
			objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, object.getClass()));
		} else {
			objectList = new ArrayList<Comparable>();
		}

		//add to the collection and sort the objects
		if(objectList.contains(object)){
			int idx = objectList.indexOf(object);
			//replace with this version
			objectList.remove(idx);
			objectList.add(object);
		} else {
			objectList.add(object);
		}
		Collections.sort(objectList);

		//convert to JSON
		String json = mapper.writeValueAsString(objectList); 
		long timestamp = System.currentTimeMillis();
		
		//insert into table
		try{
			getConnection().insert(keySpace, //keyspace
		        	guid,
		        	columnPath,
	                json.getBytes(charsetEncoding),
	                timestamp,
	                ConsistencyLevel.ONE);
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			return false;
		}
		return true;		
	}
	
	
	/**
	 * @see org.ala.dao.StoreHelper#put(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
	 */
	public boolean put(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
		return put(table, columnFamily, columnFamily, columnName, guid, object);
	}
	
	/**
	 * @see org.ala.dao.StoreHelper#getScanner(java.lang.String, java.lang.String)
	 */
	@Override
	public Scanner getScanner(String table, String columnFamily, String column) throws Exception {
		return new CassandraScanner(getConnection(), keySpace, columnFamily, column);
	}
	
	public static void main(String[] args) throws Exception {
		
		CassandraHelper cu = new CassandraHelper();
		
		ColumnParent cp = new ColumnParent("tc");
		
		SlicePredicate slicePredicate = new SlicePredicate();
		List<byte[]> colNames = new ArrayList<byte[]>();
		colNames.add("taxonConcept".getBytes());
		slicePredicate.setColumn_names(colNames);
		
		List<KeySlice> keys = cu.getConnection().get_range_slice(keySpace, cp, slicePredicate, "", "", 100, ConsistencyLevel.ONE);
		int count = 0;
		while(!keys.isEmpty() || keys.size()==1){
			
			count += keys.size();
			
			KeySlice firstKey = keys.get(0);
			KeySlice lastKey = keys.get(keys.size()-1);
			
			System.out.println("First key: " + firstKey);
			System.out.println("Last key: " + lastKey);
			
			keys = cu.getConnection().get_range_slice(keySpace, cp, slicePredicate, lastKey.getKey(), "", 100, ConsistencyLevel.ONE);
			
			keys.remove(lastKey);
		}
		
		System.out.println("Number of rows: " + count);
		
		System.exit(0);
		
		TaxonConcept t = null;
		
		for(int i=0; i< 100000; i++){
	        t =  new TaxonConcept();
//	        int id = (int) System.currentTimeMillis();
	        t.setId(i);
	        t.setGuid("urn:lsid:"+i);
	        t.setNameString("Aus bus");
	        t.setAuthor("Smith");
	        t.setAuthorYear("2008");
	        t.setInfoSourceName("AFD");
	        t.setInfoSourceURL("http://afd.org.au");
	        cu.putSingle("taxonConcept", "tc", "taxonConcept", t.getGuid(), t);
	        
	        if(i % 1000==0){
	        	System.out.println("id: "+i);
	        }
		}
        
        /*
        CommonName c1 = new CommonName();
        c1.setNameString("Dave");

        CommonName c2 = new CommonName();
        c2.setNameString("Frank");

        cu.putSingle("taxonConcept", "tc", "taxonConcept", "123", t);
        cu.put("taxonConcept", "tc", "commonName", "123", c1);
        cu.put("taxonConcept", "tc", "commonName", "123", c2);
        cu.putSingle("taxonConcept", "tc", "taxonConcept", "124", t);
        
        TaxonConcept tc = (TaxonConcept) cu.get("taxonConcept", "tc", "taxonConcept", "123", TaxonConcept.class);
        System.out.println("Retrieved: "+tc.getNameString());
        
        List<CommonName> cns = (List) cu.getList("taxonConcept", "tc", "commonName", "123", CommonName.class);
        System.out.println("Retrieved: "+cns);
        
        //cassandra scanning
         * 
         */
        
	}
	
	/**
	 * @param keySpace the keySpace to set
	 */
	public static void setKeySpace(String keySpace) {
		CassandraHelper.keySpace = keySpace;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	public Map<String, Object> getSubColumnsByGuid(String columnFamily,String superColName,String guid) throws Exception{
		throw new NoSuchMethodException("No such method implementation in this class : " + this.getClass().getName());
	}
}
