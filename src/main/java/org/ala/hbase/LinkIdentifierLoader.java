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

import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.HomonymException;

/**
 * LinkIdentifierLoader.
 * 
 * @author MOK011
 * 
 * History:
 * init version: 14 Sept 2011.
 */
@Component("linkIdentifierLoader")
public class LinkIdentifierLoader {
	protected static Logger logger  = Logger.getLogger(LinkIdentifierLoader.class);	
			
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	@Inject
	protected StoreHelper storeHelper;
	
	@Inject
	protected CBIndexSearch indexSearch;
	
	/**
	 * Usage: outputFileName [option: cassandraAddress cassandraPort]
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		LinkIdentifierLoader loader = context.getBean(LinkIdentifierLoader.class);
		
		try {
			loader.doFullScan();
		}
		catch(Exception e){			
			System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");			
			e.printStackTrace();
			logger.error(e);
			System.exit(0);	
		}
		System.exit(0);	
	}
	
	/**
	 * scan cassandra repository
	 * 
	 * @throws Exception
	 */
	public void doFullScan() throws Exception {
		long start = System.currentTimeMillis();
		int ctr = 0;
		int pctr = 0;
		storeHelper.init();
		Scanner scanner = storeHelper.getScanner("bie", "tc", "taxonConcept");
		byte[] guidAsBytes = null;

		while ((guidAsBytes = scanner.getNextGuid()) != null) {
			String guid = new String(guidAsBytes);
			ExtendedTaxonConceptDTO taxonConcept = taxonConceptDao.getExtendedTaxonConceptByGuid(guid);
			if(taxonConcept != null && taxonConcept.getTaxonConcept() != null){
				String name = taxonConcept.getTaxonConcept().getNameString();
				
				try {
					String lsid = indexSearch.searchForLSID(name);
					if(lsid == null){
						taxonConceptDao.setLinkIdentifier(guid, guid);
					} else {
						taxonConceptDao.setLinkIdentifier(guid, name);
					}
				} catch(HomonymException e){
					//expected exception
					taxonConceptDao.setLinkIdentifier(guid, guid);
				}
				
				ctr++;
				if(pctr++ > 1000){
					System.out.println("****** guid = " + guid + ", sciName = " + name + ", current count = " + ctr);
					pctr = 0;
				}		
			}
		}
		logger.info("total time taken (sec) = " + ((System.currentTimeMillis() - start)/1000)); 
	}
	
	/**
	 * @param storeHelper the storeHelper to set
	 */
	public void setStoreHelper(StoreHelper storeHelper) {
		this.storeHelper = storeHelper;
	}
}
