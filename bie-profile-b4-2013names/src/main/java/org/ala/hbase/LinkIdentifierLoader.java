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


import java.io.FileReader;

import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.HomonymException;
import au.org.ala.checklist.lucene.SearchResultException;

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
	 * This class does not need to run unless there was an issue with ALANamesLoader.
	 * 
	 * The link identifier is loaded as part of ALANamesLoader
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		LinkIdentifierLoader loader = context.getBean(LinkIdentifierLoader.class);
		
		try {
			loader.loadAllLinkIdentifiers();
		}
		catch(Exception e){			
			System.out.println("***** Fatal Error !!!.... shutdown cassandra connection.");			
			e.printStackTrace();
			logger.error(e);
			System.exit(0);	
		}
		System.exit(0);	
	}
	/*
	 * Load all the link identifiers from the ala names dump
	 */
	public void loadAllLinkIdentifiers() throws Exception{
	    CSVReader tr = new CSVReader(new FileReader(ALANamesLoader.ALA_NAMES_FILE), '\t', '"', '\\');
	    String[] cols = tr.readNext(); //first line contains headers - ignore
        int lineNumber = 1;
        int ctr = 0;
        long start = System.currentTimeMillis();
        while((cols=tr.readNext())!=null){
            String guid = cols[2];
            String acceptedGuid = cols[4];
            String scientificName = cols[6];
            if(StringUtils.isEmpty(acceptedGuid)){
                updateLinkIdentifier(guid,scientificName);
                ctr++;
                if(ctr%1000==0){
                    System.out.println("****** guid = " + guid + ", sciName = " + scientificName + ", current count = " + ctr);                    
                }
            }
        }
        logger.info("total time taken (sec) = " + ((System.currentTimeMillis() - start)/1000)); 
        
	}
	
	public void updateLinkIdentifier(String guid, String scientificName)throws Exception{
	    try {
            //List results  = indexSearch.searchForRecords(scientificName, null,false);
	        String lsid = indexSearch.searchForLSID(scientificName);
            if(lsid == null || !lsid.equals(guid)){
                taxonConceptDao.setLinkIdentifier(guid, guid);
            } else {
                taxonConceptDao.setLinkIdentifier(guid, scientificName);
            }
        } catch(SearchResultException e){
            //expected exception
            taxonConceptDao.setLinkIdentifier(guid, guid);
        }
	    
	}
	
	/**
	 * scan cassandra repository
	 * 
	 * This is obsolete it is inefficient to page through cassandra when the same information can be obtained through the names
	 * dump
	 * 
	 * 
	 * @throws Exception
	 */
	public void doFullScan() throws Exception {
		long start = System.currentTimeMillis();
		int ctr = 0;
		int pctr = 0;
//		storeHelper.init();
		Scanner scanner = storeHelper.getScanner("tc", "tc", "","taxonConcept");
		byte[] guidAsBytes = null;

		while ((guidAsBytes = scanner.getNextGuid()) != null) {
			String guid = new String(guidAsBytes);
			TaxonConcept taxonConcept = (TaxonConcept)scanner.getValue("taxonConcept", TaxonConcept.class);
			//ExtendedTaxonConceptDTO taxonConcept = taxonConceptDao.getExtendedTaxonConceptByGuid(guid, false);
			if(taxonConcept != null ){
				String name = taxonConcept.getNameString();
				//Looking up the name again to determine whether or not it is a homonym
				// We only want to add the scientific name as a link identifier if it is not a homonym
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
				if(ctr%1000==0){
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
