package org.ala.hbase;

import java.util.regex.Pattern;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.util.Bytes;

public class AddTestData {

	static final Pattern pipeDelimited = Pattern.compile(">>>");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
    	HBaseConfiguration config = new HBaseConfiguration();
    	
    	HTable table = new HTable(config, "document");
    	BatchUpdate batchUpdate = new BatchUpdate("http://www.reptilesdownunder.com/ahc/animal.php?saleID=30680");

    	//add dublin core properties for this page
    	batchUpdate.put("dc:source", Bytes.toBytes("Reptiles Down Under"));
    	batchUpdate.put("dc:title", Bytes.toBytes("Animal details - Inland bearded dragon - Pogona vitticeps | ReptilesDownUnder.com"));
    	batchUpdate.put("dc:rights", Bytes.toBytes("All content Stewart Macdonald, unless specified otherwise."));
    	
    	//add extracted properties 
    	batchUpdate.put("raw:hasFamily", Bytes.toBytes("Dragons (Agamidae)"));
    	batchUpdate.put("raw:hasCommonName", serialiseArray(new String[]{"Inland bearded dragon","Inland dragon"}));
    	batchUpdate.put("raw:hasScientificName", Bytes.toBytes("Pogona vitticeps"));
    	
    	//add processed properties 
    	batchUpdate.put("triples:hasFamilyID", Bytes.toBytes("urn:lsid:afd.taxon:123"));
    	batchUpdate.put("triples:hasTaxonID", Bytes.toBytes("urn:lsid:afd.taxon:1221323"));    	
    	
    	//add location
    	batchUpdate.put("loc:filePath", Bytes.toBytes("/tmp/infosource-id/123/12.html"));
    	
    	table.commit(batchUpdate);
	}
	
	public static byte[] serialiseArray(String[] values){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<values.length; i++){
			if(i>0){
				sb.append(">>>");
			}			
			sb.append(values[i]);
		}
		return sb.toString().getBytes();
	}
}
