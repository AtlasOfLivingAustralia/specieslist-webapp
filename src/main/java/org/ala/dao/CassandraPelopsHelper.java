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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ala.model.RankUtils;
import org.ala.model.Rankable;
import org.ala.util.ColumnType;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SuperColumn;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.scale7.cassandra.pelops.Bytes;
import org.scale7.cassandra.pelops.Cluster;
import org.scale7.cassandra.pelops.OperandPolicy;
import org.scale7.cassandra.pelops.Pelops;//Selector, Bytes
import org.scale7.cassandra.pelops.Selector;
import org.scale7.cassandra.pelops.Mutator;
//import org.wyki.cassandra.pelops.Mutator;
//import org.wyki.cassandra.pelops.Pelops;
//import org.wyki.cassandra.pelops.Policy;
//import org.wyki.cassandra.pelops.Selector;
import org.scale7.cassandra.pelops.pool.CommonsBackedPool;

/**
 * A StoreHelper implementation for Cassandra that uses Pelops over the
 * top of Thrift.
 * @author Natasha
 * 
 * History:
 * 4 Aug 2010 (MOK011): implement put, putList, putSingle and getScanner functions based on CassandraHelper.java.
 * 8 Oct 2010 (MOK011): added getSubColumnsByGuid function
 */
public class CassandraPelopsHelper implements StoreHelper  {
	protected static Logger logger = Logger.getLogger(CassandraPelopsHelper.class);

	protected static String keySpace = "bie";

	protected String host = "localhost";

	protected String pool = "ALA";

	protected int port = 9160;
	
	protected int minPool =10;
	
	protected int maxPool = 100;
	
	protected int targetConnections =30;

	protected String charsetEncoding = "UTF-8";	

	@Override
	public void init() throws Exception {
	    CommonsBackedPool.Policy policy = new CommonsBackedPool.Policy();
	    policy.setMaxActivePerNode(maxPool);	    
	    OperandPolicy operandPolicy = new OperandPolicy();
		//set up the connection pool

	    logger.info(host);
		Pelops.addPool(pool, new Cluster(host,port), keySpace,policy, operandPolicy);

	    logger.info("Initialising Pelops connection pool to " +host + " with min connections " + minPool + " target connections: " + targetConnections + " max connections " + maxPool);
	    
//	    Policy policy = new Policy();
//	    policy.setMinCachedConnectionsPerNode(minPool);
//	    policy.setMaxConnectionsPerNode(maxPool);
//	    policy.setTargetConnectionsPerNode(targetConnections);
//	    
//		Pelops.addPool(pool, new String[]{host}, port, false, keySpace, policy);

	}
	
	public void shutdown(){
	    Pelops.shutdown();
	}

	/**
	 * Retrieves a map of subcolumns
	 */
    public Map<String, Object> getSubColumnsByGuid(String columnFamily,String guid) throws Exception {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
        logger.debug("Pelops get guid: " + guid);
        Selector selector = Pelops.createSelector(pool);
        List<Column> cols = null;
        try{
            //initialise the object mapper
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            cols = selector.getColumnsFromRow(columnFamily,guid,            		
            		Selector.newColumnsPredicateAll(true, 10000), ConsistencyLevel.ONE);
            // convert json string to Java object and add into Map object.
            for(Column col : cols){
            	String name = new String(col.getName(), charsetEncoding);
            	String value = new String(col.getValue(), charsetEncoding);
            	ColumnType type = ColumnType.getColumnType(name);
            	Object o = null;
            	try{
            		if(type != null){
            			// convertion is based on pre-define ColumnType.
		            	if(!type.isList()){
		            		Class clazz = type.getClazz();
		            		//non-json value 
		            		if(clazz == String.class){
		            			o = value;
		            		}
		            		// json data binding type : (Object, Boolean, Integer...etc)
		            		else{
		            			o = mapper.readValue(value, type.getClazz());
		            		}
		            	}
		            	// json data binding type : (List of Object)
		            	else{
		            		o = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, type.getClazz()));
		            	}
            		}
            		else{
                            logger.info("ColumnType lookup failed. Invalid SubColumn Name: " + name);
            		}
	            }
            	catch(Exception ex){
            		logger.error(ex);
            	}
            	
            	if(o != null){
            		map.put(name, o);
            	}
            	logger.debug("*** name: " + name + ", value: " + value);            	
            }
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
        	}
        }
		return map;
    }
    
    /**
     * @see org.ala.dao.StoreHelper#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class)
     */
    @Override
    public Comparable get(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
        logger.debug("Pelops get table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
        Selector selector = Pelops.createSelector(pool);
        Column col = null;
        try{
            col = selector.getColumnFromRow(columnFamily, guid, columnName, ConsistencyLevel.ONE);
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
        	}
        }
        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//read the existing value
		if(col!=null){
			String value = new String(col.getValue(),charsetEncoding);
//			logger.info(value);
			return (Comparable) mapper.readValue(value, theClass);
		} else {
			return null;
		}
    }

    /**
     * @see org.ala.dao.StoreHelper#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class)
     */
    @Override
    public String getStringValue(String table, String columnFamily, String columnName, String guid) throws Exception {
        logger.debug("Pelops get table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
        Selector selector = Pelops.createSelector(pool);
        Column col = null;
        try{
            col = selector.getColumnFromRow(columnFamily, guid, columnName, ConsistencyLevel.ONE);
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	//logger.error(e.getMessage() + " POTENTIAL NULL VALUE");
        }

		//read the existing value
		if(col!=null){
			String value = new String(col.getValue(),charsetEncoding);
			return value;
		} else {
			return null;
		}
    }
 
    @Override
    public boolean updateStringValue(String table, String columnFamily, String columnName, String guid, String value) throws Exception {
		Mutator mutator = Pelops.createMutator(pool);
		try{
			if(value != null && !value.isEmpty()){
//				String value1 = getStringValue(table, columnFamily, columnName, guid);
//				if(value1 != null){
//					// remove old linkIdentifier column
//			    	mutator.deleteSubColumn(guid, columnFamily, "tc", columnName);
//			    	mutator.execute(ConsistencyLevel.ONE);
//				}
				mutator.writeColumn(columnFamily, guid, mutator.newColumn(columnName, value));
				mutator.execute(ConsistencyLevel.ONE);
				return true;
			}
		}
		catch(Exception e){
			logger.error(e.getMessage(), e);
			return false;
		}
		return false;
    }
    
    /**
     * @see org.ala.dao.StoreHelper#getList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class)
     */
    @Override
    public List<Comparable> getList(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
        logger.debug("Pelops getList table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
        Selector selector = Pelops.createSelector(pool);
        Column col = null;
        try{ 
            col=selector.getColumnFromRow(columnFamily, guid, columnName, ConsistencyLevel.ONE);
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
        	}
        }
         //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//read the existing value
		List<Comparable> objectList = null;
		if(col!=null){
			String value = new String(col.getValue(), charsetEncoding);
//			logger.info(value);
			objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, theClass));
		} else {
			objectList = new ArrayList<Comparable>();
		}
		logger.debug("Pelops getList returning.");
		return objectList;
    }

    /**
     * @see org.ala.dao.StoreHelper#putSingle(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
     */
    @Override
    public boolean putSingle(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
    	logger.debug("Pelops putSingle table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
    	Mutator mutator = Pelops.createMutator(pool);
    	
		guid =  StringUtils.trimToNull(guid);
		if(guid==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+object);
			return false;
		}

        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		//convert to JSON
		String json = mapper.writeValueAsString(object); 
		
		//insert into table
		try{
			mutator.writeColumn(columnFamily, guid, mutator.newColumn(columnName, json));
			mutator.execute(ConsistencyLevel.ONE);
			logger.debug("Pelops putSingle returning");
			return true;
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			return false;
		}
    }

	/**
	 * @see org.ala.dao.StoreHelper#put(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
	 */
	@Override
	public boolean put(String table, String columnFamily, 
			String columnName, String guid, Comparable object) throws Exception {
		logger.debug("Pelops put table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
    	Mutator mutator = Pelops.createMutator(pool);
    	Selector selector = Pelops.createSelector(pool);
    	
		guid =  StringUtils.trimToNull(guid);
		if(guid==null || object==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+object);
			return false;
		}
		
		Column col = null;
		try{
			col = selector.getColumnFromRow(columnFamily, guid, columnName, ConsistencyLevel.ONE);
		}catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
        	}
        }
    	
        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		//read the existing value
		List<Comparable> objectList = null;
		if(col!=null){
			String value = new String(col.getValue(), charsetEncoding);
			objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, object.getClass()));
		} else {
			objectList = new ArrayList<Comparable>();
		}

		//add to the collection and sort the objects
		if(objectList.contains(object)){
			
			int idx = objectList.indexOf(object);
			//replace with this version
			Comparable objectToReplace = objectList.remove(idx);
			//dont lose rankings!!!!!!!!!!!!!!!!!!!!!!!
			if(object instanceof Rankable){
				//retrieve those rankings
				RankUtils.copyAcrossRankings((Rankable) objectToReplace, (Rankable) object);
			}
			objectList.add(object);
		} else {
			objectList.add(object);
		}
		Collections.sort(objectList);

		//convert to JSON
		String json = mapper.writeValueAsString(objectList); 
		
		//insert into table
		try{
			mutator. writeColumn(columnFamily, guid, mutator.newColumn(columnName, json));
			mutator.execute(ConsistencyLevel.ONE);
			logger.debug("Pelops put returning");
			return true;
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			return false;
		}
	}

	/**
	 * @see org.ala.dao.StoreHelper#put(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Comparable)
	 */
//    @Override
//    public boolean put(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
//        logger.debug("Pelops put table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
//		return put(table, columnFamily, columnFamily, columnName, guid, object);
//    }

    /**
     * FIXME Note - this currently doesnt preserve rankings.
     * 
     * @see org.ala.dao.StoreHelper#putList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, boolean)
     */
    @Override
    public boolean putList(String table, String columnFamily, String columnName, String guid, List<Comparable> objects, boolean append) throws Exception {
    	logger.debug("Pelops putList table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
    	Mutator mutator = Pelops.createMutator(pool);
    	Selector selector = Pelops.createSelector(pool);
    	
		guid =  StringUtils.trimToNull(guid);
		if(guid==null){
			logger.warn("Null or empty guid supplied. Unable to add to row ["+guid+"] column ["+columnName+"] object: "+objects);
			return false;
		}		

		Column col = null;
		try {
			col = selector.getColumnFromRow(columnFamily, guid, columnName, ConsistencyLevel.ONE);
		} catch (Exception e){
        	//expected behaviour. current thrift API doesnt seem
        	//to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
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
				String value = new String(col.getValue(), charsetEncoding);
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
		//insert into table
		try{
			mutator. writeColumn( columnFamily,guid, mutator.newColumn(columnName, json));
			mutator.execute(ConsistencyLevel.ONE);
			logger.debug("Pelops putList returning");
			return true;
		} catch (Exception e){
			logger.error(e.getMessage(),e);
			return false;
		}
    }

    /**
     *
     * @param columnFamily
     * @param superColName
     * @param startGuid the first object to return
     * @param pageSize the number of objects to return
     * @return
     */
    public Map<String, Map<String,Object>> getPageOfSubColumns(String columnFamily,  String startGuid, int pageSize) {
    	return getPageOfSubColumns(columnFamily,  null, startGuid, pageSize);
    }

    /**
     *
     * @param columnFamily
     * @param superColName
     * @param guids
     * @return
     */
    public Map<String, Map<String,Object>> getPageOfSubColumns(String columnFamily,  List<String> guids) {
        return getPageOfSubColumns(columnFamily,  null, guids);
    }

    /**
     * Retrieve a page of subcolumns using the supplied GUIDS.
     *
     * @param columnFamily
     * @param superColName
     * @param columns
     * @param guids
     * @return
     */
	public Map<String, Map<String,Object>> getPageOfSubColumns(String columnFamily, ColumnType[] columns, List<String> guids) {

        logger.debug("Pelops getting page with using a list of guids");
        Selector selector = Pelops.createSelector(pool);

        try{
            //initialise the object mapper
    		ObjectMapper mapper = new ObjectMapper();
    		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    		//create the column slice
            SlicePredicate slicePredicate = getSlicePredicateForAll(columns);

    		//retrieve a list of columns for each of the rows
    		Map<String, List<Column>> colListMap = selector.getColumnsFromRowsUtf8Keys(columnFamily, guids, slicePredicate, ConsistencyLevel.ONE);
            return processColumnMap(colListMap);
        } catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }
        return new LinkedHashMap<String, Map<String,Object>>();
	}

    private SlicePredicate getSlicePredicateForAll(ColumnType[] columns) {
        if(columns==null || columns.length==0){
            return Selector.newColumnsPredicateAll(true, 10000);
        } else {
            String[] names = new String[columns.length];
            for(int i =0;i<columns.length;i++){
                names[i] = columns[i].getColumnName();
            }
            SlicePredicate slicePredicate =  Selector.newColumnsPredicate(names);//new SlicePredicate();
//            for(ColumnType ct: columns) {
//                slicePredicate.addToColumn_names(ct.getColumnName().getBytes());
//            }
            return slicePredicate;
        }
    }

    @Override
	public Map<String, Map<String,Object>> getPageOfSubColumns(String columnFamily,  ColumnType[] columns, String startGuid, int pageSize) {
    	
        logger.debug("Pelops getting page with start guid: " + startGuid);
        Selector selector = Pelops.createSelector(pool);
        
        try{
            String startKey = StringUtils.isNotEmpty(startGuid)?startGuid:"";
            String endKey = "";
            
            
            KeyRange kr = Selector.newKeyRange(startKey, endKey, pageSize+1);

//    		KeyRange kr = new KeyRange();
//    		if(StringUtils.isNotEmpty(startGuid)){
//    			kr.start_key = Bytes.fromByteArray(startGuid.getBytes());
//    			kr.end_key = new Bytes("");
//    		} else {
//    			kr.start_key = "";
//    			kr.end_key = "";
//    		}
    		
    		//kr.setCount(pageSize);
    		
    		//create the column slice
            SlicePredicate slicePredicate = getSlicePredicateForAll(columns);

    		//retrieve a list of columns for each of the rows
    		Map<String, List<Column>> colListMap = selector.getColumnsFromRowsUtf8Keys( columnFamily, kr, slicePredicate, ConsistencyLevel.ONE);
    		//remove the matching key to enable paging without duplication
    		colListMap.remove(startGuid);

            return processColumnMap(colListMap);
        } catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isDebugEnabled()){
        		logger.debug(e.getMessage(), e);
        	}
        }
        return new LinkedHashMap<String, Map<String,Object>>();
	}


    private Map<String, Map<String,Object>> processColumnMap(Map<String, List<Column>> colListMap) throws UnsupportedEncodingException {

        logger.debug("Returning rows: " + colListMap.size());
        Map<String, Map<String,Object>> rowMap = new LinkedHashMap<String, Map<String,Object>>();
        //initialise the object mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);


        // convert json string to Java object and add into Map object.
        for(String guid: colListMap.keySet()){

            List<Column> cols = colListMap.get(guid);
            Map<String, Object> map = new HashMap<String,Object>();

            for(Column col : cols){
                String name = new String(col.getName(), charsetEncoding);
                String value = new String(col.getValue(), charsetEncoding);
                ColumnType type = ColumnType.getColumnType(name);
                Object o = null;
                try{
                    if(type != null){
                        // convertion is based on pre-define ColumnType.
                        if(!type.isList()){
                            o = mapper.readValue(value, type.getClazz());
                        } else{
                            o = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, type.getClazz()));
                        }
                    } else{
                        logger.info("ColumnType lookup failed. Invalid SubColumn Name: " + name);
                    }
                }
                catch(Exception ex){
                    logger.error(ex);
                }

                if(o != null){
                    map.put(name, o);
                }
                logger.trace("*** name: " + name + ", value: " + value);
            }
            rowMap.put(guid, map);
        }
        return rowMap;
    }

    /**
     * @see org.ala.dao.StoreHelper#getScanner(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Scanner getScanner(String table, String columnFamily, String... column) throws Exception {
        return new CassandraScanner(pool, table,columnFamily,column);
    	//return new CassandraScanner(Pelops.getDbConnPool(pool).getConnection().getAPI(), keySpace, columnFamily, column);
    }

	public Scanner getScanner(String table, String columnFamily) throws Exception {
    	return new CassandraScanner(Pelops.getDbConnPool(pool).getConnection().getAPI(), keySpace, columnFamily);
    }
	
    public List<String> getSuperColumnsByGuid(String guid, String columnFamily) throws Exception {
		List<String> al = new ArrayList<String>();
		Selector selector = Pelops.createSelector(pool);
		List<Column> l = selector.getColumnsFromRow( columnFamily,guid, Selector.newColumnsPredicateAll(true, 10000), ConsistencyLevel.ONE);
		for(int i = 0; i < l.size(); i++){
			Column s = l.get(i);
			al.add(new String(s.getName(), "UTF-8"));			
		}		
		return al;		    	
    }
    
    public Map<String, List<Comparable>> getColumnList(String columnFamily, String guid, Class theClass) throws Exception {
        Selector selector = Pelops.createSelector(pool);
        List<Column> cl = null;
        Map<String, List<Comparable>> al = new HashMap<String, List<Comparable>>();
        
        try{ 
        	cl = selector.getColumnsFromRow( columnFamily, guid, Selector.newColumnsPredicateAll(true, 10000), ConsistencyLevel.ONE);            
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        	if(logger.isTraceEnabled()){
        		logger.trace(e.getMessage(), e);
        	}
        }
        
		if(cl != null){
			//initialise the object mapper
			ObjectMapper mapper = new ObjectMapper();
			mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			for(Column c : cl){
				String name = new String(c.getName(), "utf-8");
				String value = new String(c.getValue(), "utf-8");					
				List<Comparable> objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, theClass));
				if(objectList != null && objectList.size() > 0){
					al.put(name, objectList);
				}
			}
		}        
		return al;
    }


    public static void main(String[] args) throws Exception {
    	CassandraPelopsHelper helper = new CassandraPelopsHelper();
    	helper.init();

    	Map<String, Object> map = helper.getSubColumnsByGuid("tc", "103067807");
    	Set<String> keys = map.keySet();
    	Iterator<String> it = keys.iterator();
    	while(it.hasNext()){
    		String key = it.next();
    		ColumnType type = ColumnType.getColumnType(key);
    		Object o = map.get(type.getColumnName());
    		if(o instanceof List){
    			List l = (List)o;
    		}
    		else{
    			Comparable c = (Comparable)o;
    		}
    	}

/*
		TaxonConcept t = null;
    	List<Comparable> l = new ArrayList<Comparable>();

		for(int i=0; i< 10; i++){
	        t =  new TaxonConcept();
	        t.setId(i);
	        t.setGuid("urn:lsid:"+i);
	        t.setNameString("Aus bus");
	        t.setAuthor("Smith");
	        t.setAuthorYear("2008");
	        t.setInfoSourceName("AFD");
	        t.setInfoSourceURL("http://afd.org.au");
	        helper.putSingle("taxonConcept", "tc", "taxonConcept", t.getGuid(), t);

	        l.add(t);
	        if(i % 1000==0){
	        	System.out.println("id: "+i);
	        }
		}
		helper.putList("taxonConcept", "tc", "taxonConcept", "128", l, true);

        CommonName c1 = new CommonName();
        c1.setNameString("Dave");

        CommonName c2 = new CommonName();
        c2.setNameString("Frank");

        helper.putSingle("taxonConcept", "tc", "taxonConcept", "123", t);
        helper.put("taxonConcept", "tc", "commonName", "123", c1);
        helper.put("taxonConcept", "tc", "commonName", "123", c2);
        helper.putSingle("taxonConcept", "tc", "taxonConcept", "124", t);

        TaxonConcept tc = (TaxonConcept) helper.get("taxonConcept", "tc", "taxonConcept", "123", TaxonConcept.class);
        System.out.println("Retrieved: "+tc.getNameString());

        List<CommonName> cns = (List) helper.getList("taxonConcept", "tc", "commonName", "123", CommonName.class);
        System.out.println("Retrieved: "+cns);
*/
        //cassandra scanning
    	Scanner scanner = helper.getScanner("taxonConcept", "tc", "taxonConcept");
    	for(int i = 0; i < 10; i++){
    		System.out.println(new String(scanner.getNextGuid()));
    	}
		System.exit(0);
    }

	/**
	 * @param keySpace the keySpace to set
	 */
	public static void setKeySpace(String keySpace) {
		CassandraPelopsHelper.keySpace = keySpace;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param pool the pool to set
	 */
	public void setPool(String pool) {
		this.pool = pool;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	

	/**
     * @param targetConnections the targetConnections to set
     */
    public void setTargetConnections(int targetConnections) {
        this.targetConnections = targetConnections;
    }

    /**
     * @param minPool the minPool to set
     */
    public void setMinPool(int minPool) {
        this.minPool = minPool;
    }

    /**
     * @param maxPool the maxPool to set
     */
    public void setMaxPool(int maxPool) {
        this.maxPool = maxPool;
    }

    /**
	 * @param charsetEncoding the charsetEncoding to set
	 */
	public void setCharsetEncoding(String charsetEncoding) {
		this.charsetEncoding = charsetEncoding;
	}
}
