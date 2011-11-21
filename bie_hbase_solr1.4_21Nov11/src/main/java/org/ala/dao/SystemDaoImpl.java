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

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;
/**
 * HBase implementation if the SystemDao.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SystemDaoImpl implements SystemDao {

	protected static Logger logger = Logger.getLogger(SystemDaoImpl.class);
	
	/**
	 * @see org.ala.dao.SystemDao#init()
	 */
	public void init() throws Exception {
    	logger.info("Initialising HBase");
    	
    	HBaseConfiguration config = new HBaseConfiguration();
    	HBaseAdmin hBaseAdmin = new HBaseAdmin(config);
    	logger.info("HBase ZooKeeper Quorum - " + hBaseAdmin.getConnection().getZooKeeperWrapper().getQuorumServers());
    	
    	if(hBaseAdmin.tableExists("taxonConcept")){
    		hBaseAdmin.disableTable("taxonConcept");
    		hBaseAdmin.deleteTable("taxonConcept");
    	}
//    	if(hBaseAdmin.tableExists("taxonName")){
//    		hBaseAdmin.disableTable("taxonName");
//    		hBaseAdmin.deleteTable("taxonName");
//    	}
//    	if(hBaseAdmin.tableExists("publication")){
//    		hBaseAdmin.disableTable("publication");
//    		hBaseAdmin.deleteTable("publication");
//    	}
//    	if(hBaseAdmin.tableExists("document")){
//    		hBaseAdmin.disableTable("document");
//    		hBaseAdmin.deleteTable("document");
//    	}
//    	if(hBaseAdmin.tableExists("image")){
//    		hBaseAdmin.disableTable("image");
//    		hBaseAdmin.deleteTable("image");
//    	}
    	
//    	//create taxon concept table
    	HTableDescriptor htd = new HTableDescriptor("taxonConcept");
    	
    	 // this is the taxon concept data from AFD/APNI/CoL
    	HColumnDescriptor tcHcd = new HColumnDescriptor("tc:");
    	tcHcd.setMaxVersions(1);
    	htd.addFamily(tcHcd);
    	
    	// taxon name properties
    	HColumnDescriptor tnHcd = new HColumnDescriptor("tn:");
    	tnHcd.setMaxVersions(1);
    	htd.addFamily(tnHcd);
    	
    	// publication properties
    	HColumnDescriptor pubHcd = new HColumnDescriptor("pub:");
    	pubHcd.setMaxVersions(1);
    	htd.addFamily(pubHcd);
    	
    	// this is the properties from harvested sources (HTML etc)
    	HColumnDescriptor rawHcd = new HColumnDescriptor("raw:");
    	rawHcd.setMaxVersions(1);
    	htd.addFamily(rawHcd);
    	
//    	htd.addFamily(new HColumnDescriptor("taxonomic:"));
//    	htd.addFamily(new HColumnDescriptor("geospatial:"));
//    	htd.addFamily(new HColumnDescriptor("morphological:"));
    	
    	hBaseAdmin.createTable(htd);
//    	
//    	//create taxon name table
//    	htd = new HTableDescriptor("taxonName");
//    	htd.addFamily(new HColumnDescriptor("tn:"));
//    	hBaseAdmin.createTable(htd);

    	//create taxon name table
//    	htd = new HTableDescriptor("document");
//    	htd.addFamily(new HColumnDescriptor("loc:"));
//    	htd.addFamily(new HColumnDescriptor("raw:".getBytes(),Integer.MAX_VALUE, HColumnDescriptor.CompressionType.NONE,false,false,Integer.MAX_VALUE,Integer.MAX_VALUE,false));
//    	htd.addFamily(new HColumnDescriptor("triples:".getBytes(), Integer.MAX_VALUE, CompressionType.NONE,false,false,Integer.MAX_VALUE,Integer.MAX_VALUE,false));
//    	hBaseAdmin.createTable(htd);    	
    	
//    	HTable table = new HTable(config, "taxonConcept");
//    	BatchUpdate batchUpdate = new BatchUpdate("myRow");
//    	batchUpdate.put("names:nameGUID", Bytes.toBytes("urn:lsid:afd.name:123"));
//    	table.commit(batchUpdate);
    	
    	logger.info("Schema setup complete.");
	}
}
