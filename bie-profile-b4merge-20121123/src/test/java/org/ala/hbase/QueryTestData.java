package org.ala.hbase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;

public class QueryTestData {

	static final Pattern pipeDelimited = Pattern.compile(">>>");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
    	HBaseConfiguration config = new HBaseConfiguration();
    	HTable table = new HTable(config, "taxonConcept");
    	byte[][] columns = {Bytes.toBytes("tc:")};
    	Scanner scanner = table.getScanner(columns);
    	Iterator<RowResult> rowIter = scanner.iterator();
    	int i=0;
    	while(rowIter.hasNext()){
    		i++;
    		RowResult rowResult = rowIter.next();
    		String identifier = new String(rowResult.getRow());
			System.out.println("ROW KEY: "+identifier);
    		Set<Map.Entry<byte[], Cell>> entrySet = rowResult.entrySet();
    		for(Map.Entry<byte[], Cell> entry: entrySet){
    			String[] values = deserialiseArray(entry.getValue().getValue());
	    		for(String value: values){
	    			System.out.print(new String(entry.getKey()));	
	    			System.out.println(": "+value);
	    		}    			
    		}
    		System.out.println("-------------------------------------------------");	
    		if(i>10){ 
    			break;
    		}
    	}
	}
	
	static String[] deserialiseArray(byte[] bytes){
		String array = new String(bytes);
		return pipeDelimited.split(array);
	}	
}
