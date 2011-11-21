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

import java.util.Iterator;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;


/**
 * Remove the properties associated with the concepts in the system.
 * 
 * @author Dave Martin
 */
public class ClearTriples {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
//		ApplicationContext context = SpringUtils.getContext();
//		TaxonConceptDao taxonConceptDao = (TaxonConceptDao) context.getBean(TaxonConceptDao.class);
//		taxonConceptDao.clearRawProperties();
//		System.exit(1);
		
		if(args.length==0){
			System.out.println("Please supply the columns to clear.");
			System.exit(1);
		}
		System.out.print("Clearing columns:");
		for(String arg: args){
			System.out.print(" ");
			System.out.print(arg);
		}
		System.out.println();
		
		HBaseConfiguration config = new HBaseConfiguration();
    	HTable tcTable = new HTable(config, "taxonConcept");
    	Scanner scanner = tcTable.getScanner(new String[]{"tc:", "tn:"});
    	Iterator<RowResult> iter = scanner.iterator();
    	int i=0;
    	while(iter.hasNext()){
    		RowResult rowResult = iter.next();
    		byte[] row = rowResult.getRow();
    		for(String arg: args){
    			tcTable.deleteAll(row, Bytes.toBytes(arg));
    		}
    	}
    	System.out.println("Columns to cleared.");
	}
}