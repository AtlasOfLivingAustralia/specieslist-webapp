/**
 * 
 */
package org.ala.hbase;

import java.util.Iterator;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Clear the triples in the "raw:" column family.
 * 
 * @author Dave Martin
 */
public class ClearRawTriples {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		HBaseConfiguration config = new HBaseConfiguration();
    	HTable tcTable = new HTable(config, "taxonConcept");
    	Scanner scanner = tcTable.getScanner(new String[]{"tc:"});
    	Iterator<RowResult> iter = scanner.iterator();
    	int i=0;
    	while(iter.hasNext()){
    		
    		RowResult rowResult = iter.next();
    		
    		rowResult.
    		
    		byte[] row = iter.next().getRow();
    		tcTable.deleteFamily(row, Bytes.toBytes("raw:"));
    		System.out.println(++i + " " + (new String(row)));
    	}
    	System.out.println("Raw triples cleared");
	}
}