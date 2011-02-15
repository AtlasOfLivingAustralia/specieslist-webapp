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
package org.ala.hbase;

import java.util.List;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import javax.inject.Inject;

import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;

import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.SuperColumn;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.wyki.cassandra.pelops.Mutator;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;
import org.wyki.cassandra.pelops.Selector;

/**
 * LinkIdentifierLoader.
 * 
 * @author MOK011
 * 
 * History:
 * init version: 14 Sept 2011.
 * 
 * 
 * 
 */
@Component("linkIdentifierLoader")
public class LinkIdentifierLoader {
	protected static Logger logger  = Logger.getLogger(LinkIdentifierLoader.class);	

	public static final int ROWS = 1000;
	public static final String CHARSET_ENCODING = "UTF-8";
	public static final String POOL_NAME = "ALA";
	public static final String LINK_IDENTIFIER_COLUMN_NAME = "linkIdentifier";
	
	private String host = "localhost";
	private int port = 9160;
	private String keyspace = "bie";
	private String columnFamily = "tc";	
	private ObjectMapper mapper;
		
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	/**
	 * Usage: outputFileName [option: cassandraAddress cassandraPort]
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		LinkIdentifierLoader loader = context.getBean(LinkIdentifierLoader.class);		
		
				
		if (args.length == 1){
			loader.setHost(args[0]);
		}		
		else if (args.length == 2){
			loader.setHost(args[0]);
			loader.setPort(Integer.valueOf(args[1]));
		}
		
		// do sitemap
		try{
			loader.init();
			loader.doFullScan();
			loader.closeConnectionPool();

		}
		catch(Exception e){			
			System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");
			e.printStackTrace();
			loader.closeConnectionPool();
			System.exit(0);	
		}
	}
	
	public void init(){	
		Pelops.addPool(POOL_NAME, new String[]{this.host}, this.port, false, this.keyspace, new Policy());
		mapper = new ObjectMapper();
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	/**
	 * close cassandra connection pool.
	 */
	public void closeConnectionPool(){
		Pelops.shutdown();
	}
	
	/**
	 * scan whole columnFamily tree; 
	 * plant and other in Australia.
	 * 
	 * @param infoSourceIds 
	 * @throws Exception
	 */
	public void doFullScan() throws Exception {
		long start = System.currentTimeMillis();
		KeySlice startKey = new KeySlice();
		KeySlice lastKey = null;		
		
		System.out.println("LinkIdentifierLoader process is started.....");
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
		generateLinkIdentifier(keySlices);
		while (keySlices.size() > 0){
			lastKey = keySlices.get(keySlices.size()-1);
			//end of scan ?
			if(lastKey.equals(startKey)){
				break;
			}
			startKey = lastKey;
			keyRange.setStart_key(lastKey.getKey());			
			keySlices = client.get_range_slices(keyspace, columnParent, slicePredicate, keyRange, ConsistencyLevel.ONE);
			generateLinkIdentifier(keySlices);
			System.gc();
		}
		System.out.println("LinkIdentifierLoader process is ended, total time takem: " + ((System.currentTimeMillis() - start)/1000));
	}
			
	private void generateLinkIdentifier(List<KeySlice> keySlices) {
		Mutator mutator = Pelops.createMutator(POOL_NAME, keyspace);
		Selector selector = Pelops.createSelector(POOL_NAME, keyspace);

		for (KeySlice keySlice : keySlices) {
			String guid = keySlice.getKey();
			System.out.println("**** guid: " +  guid);
			if(guid == null){
				continue;
			}
			
			for (ColumnOrSuperColumn columns : keySlice.getColumns()) {
				if (columns.isSetSuper_column()) {
					try {
						SuperColumn scol = columns.getSuper_column();
						String sColName = new String(scol.getName(), CHARSET_ENCODING);						
						try {
							// remove old linkIdentifier column
							Column col = selector.getSubColumnFromRow(guid, columnFamily, sColName, LINK_IDENTIFIER_COLUMN_NAME, ConsistencyLevel.ONE);								
							String colName = new String(col.getName(), CHARSET_ENCODING);
				        	mutator.deleteSubColumn(guid, columnFamily, sColName, colName);
				        	mutator.execute(ConsistencyLevel.ONE);
						} 
						catch (Exception e) {
							//record not found! do nothing....continue process...
						}							

						//add linkIdentifier column
						String name = getSciName(scol, keySlice.getKey());						
						if(name != null && !name.isEmpty()){							
							String lsid = taxonConceptDao.findLsidByName(name);
							if(lsid != null){
								mutator.writeSubColumn(guid, columnFamily, sColName, mutator.newColumn(LINK_IDENTIFIER_COLUMN_NAME, name));
				    			mutator.execute(ConsistencyLevel.ONE);
							}
							else{
								mutator.writeSubColumn(guid, columnFamily, sColName, mutator.newColumn(LINK_IDENTIFIER_COLUMN_NAME, guid));
				    			mutator.execute(ConsistencyLevel.ONE);
							}
						}
						else{
							mutator.writeSubColumn(guid, columnFamily, sColName, mutator.newColumn(LINK_IDENTIFIER_COLUMN_NAME, guid));
			    			mutator.execute(ConsistencyLevel.ONE);
						}						
					} 
					catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}						
				}
			}
		}
	}

	private String getSciName(SuperColumn scol, String guid){
		String value = null;
		String colName = null;		
		String name = null;
		
		if(guid == null){
			return null;
		}				
		
		//scan all columns
		for (Column col : scol.getColumns()) {
			try {
				value = new String(col.getValue(), CHARSET_ENCODING);
				colName = new String(col.getName(), CHARSET_ENCODING);
				if("taxonConcept".equalsIgnoreCase(colName)){
					TaxonConcept taxonConcept = mapper.readValue(value, TaxonConcept.class);
					name = taxonConcept.getNameString();
					break;
				}
			} catch (Exception e) {
				logger.error(e);
			} 	
		}	
		return name;
	}
		
	//========= Getter =======
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
	
	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}	
}
