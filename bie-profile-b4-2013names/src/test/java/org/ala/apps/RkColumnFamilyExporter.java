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
package org.ala.apps;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.util.SpringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import org.ala.util.ColumnType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * export cassandra rk column family in csv format.
 * 
 * @author MOK011
 * 
 * History:
 * init version: Jan 2012.
 */
@Component("rkColumnFamilyExporter")
public class RkColumnFamilyExporter {
	protected static Logger logger  = Logger.getLogger(RkColumnFamilyExporter.class);	
	public static String RK_COLUMN_FAMILY = "rk";	
			
	@Inject
	protected StoreHelper storeHelper;

	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Usage: outputFileName [option: cassandraAddress cassandraPort]
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		RkColumnFamilyExporter loader = context.getBean(RkColumnFamilyExporter.class);
		
		try {
			loader.exportRanks();
		}
		catch(Exception e){			
			System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");			
			e.printStackTrace();
			logger.error(e);
			System.exit(0);	
		}
		System.exit(0);	
	}

	private void exportRanks() throws Exception {
		FileOutputStream csvOut = FileUtils.openOutputStream(new File("/data/bie/rk_"+System.currentTimeMillis() + ".csv"));		
		
		long start = System.currentTimeMillis();
    	int i = 0;
    	int j = 0;
    	logger.debug("export Ranks...");
		Scanner scanner = storeHelper.getScanner(RK_COLUMN_FAMILY, RK_COLUMN_FAMILY, "", ColumnType.columnsToIndex);
		byte[] guidAsBytes = null;		
		while ((guidAsBytes = scanner.getNextGuid())!=null) {			
			String guid = new String(guidAsBytes);
			String sciName = "";
			try{
				ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid, false);
				if(etc != null && etc.getTaxonConcept() != null){
					sciName = etc.getTaxonConcept().getNameString();
				}
			}
			catch(Exception e){
				logger.error("etc.getTaxonConcept(): " + e);
			}
			
			i++;			
			try{	
	    		//get taxon concept details
				List<String> list = storeHelper.getSuperColumnsByGuid(guid, RK_COLUMN_FAMILY);
				for(String superColumnName : list){
					try{
						Map<String, List<Comparable>> columnList = storeHelper.getColumnList(RK_COLUMN_FAMILY, guid, Map.class);
						Set<String> keys = columnList.keySet();
						Iterator<String> itr = keys.iterator();
						while(itr.hasNext()){
							j++;
							String key = itr.next();	
							List rankingList = columnList.get(key);
							csvOut.write((guid + "; " + sciName + "; "  + superColumnName + "; " + key + "; " + mapper.writeValueAsString(rankingList) + "\n").getBytes());
							logger.info("Indexed records: "+j+", current guid: "+guid);							
						}
					}
					catch(Exception ex){
						logger.error("***** guid: " + guid + " ," + ex);
					}
				}
			}
			catch(Exception ex){
				logger.error("***** guid: " + guid + " ," + ex);
			}
    	}
		
		csvOut.flush();
		csvOut.close();

    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds with  species: " + i + ", column items: " + j);
    	logger.debug("reload Ranks finished...");
	}
	
	/**
	 * @param storeHelper the storeHelper to set
	 */
	public void setStoreHelper(StoreHelper storeHelper) {
		this.storeHelper = storeHelper;
	}
}
