package org.ala.dao;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

public class SystemDaoImpl implements SystemDao {

	protected static Logger logger = Logger.getLogger(SystemDaoImpl.class);
	
	/* (non-Javadoc)
	 * @see org.ala.dao.ISystemDao#init()
	 */
	public void init() throws Exception {
    	logger.info("Initialising HBase");
    	
    	HBaseConfiguration config = new HBaseConfiguration();
    	HBaseAdmin hBaseAdmin = new HBaseAdmin(config);
    	
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
    	htd.addFamily(new HColumnDescriptor("tc:")); // this is the taxon concept data from AFD/APNI/CoL
    	htd.addFamily(new HColumnDescriptor("tn:")); // taxon name properties
    	htd.addFamily(new HColumnDescriptor("pub:")); // publication properties    	
    	htd.addFamily(new HColumnDescriptor("raw:")); //this is the properties from harvested sources (HTML etc)
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
