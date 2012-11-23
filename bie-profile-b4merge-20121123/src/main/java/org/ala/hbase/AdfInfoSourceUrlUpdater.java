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

import java.util.Hashtable;
import java.util.Map;

import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import javax.inject.Inject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * InfoSourceUrlUpdater. Based on InfosourceUidLoader.java
 * Update infosourceUrl from 'http://biodiversity.org.au/afd.taxon/...' to 'http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/...'
 * 
 * @author mok011
 * @since Mar 2012
 */
@Component("adfInfoSourceUrlUpdater")
public class AdfInfoSourceUrlUpdater {
    protected static Logger logger  = Logger.getLogger(AdfInfoSourceUrlUpdater.class);	

    @Inject
    protected TaxonConceptDao taxonConceptDao;

    @Inject
    protected StoreHelper storeHelper;

    /**
     * Usage: outputFileName [option: cassandraAddress cassandraPort]
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	Map<String, String> hashtable = new Hashtable<String, String>();
    	for(int i = 0; i < args.length; i++){
    		String infoId = args[i].substring(0, args[i].indexOf(':'));
    		String url = args[i].substring(args[i].indexOf(':') + 1);
    		hashtable.put(infoId, url);
    	}
    	
        ApplicationContext context = SpringUtils.getContext();
        AdfInfoSourceUrlUpdater loader = context.getBean(AdfInfoSourceUrlUpdater.class);

        try {
            loader.doFullScan(hashtable);
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
    public void doFullScan(Map<String, String> hashtable) throws Exception {
        long start = System.currentTimeMillis();

        //		storeHelper.init();
        Scanner scanner = storeHelper.getScanner("bie", "tc", "taxonConcept");
        byte[] guidAsBytes = null;

//        Map<String, String> uidInfosourceIDMap = getUidInfosourceIdMap();

        while ((guidAsBytes = scanner.getNextGuid()) != null) {
            String guid = new String(guidAsBytes);
            ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid, false);
            if(etc != null){
                if (etc.getTaxonConcept() != null) {
                    TaxonConcept tc = etc.getTaxonConcept();
                    String infosourceId = tc.getInfoSourceId();
                    System.out.println("guid: " + guid + ", infosource id: " + infosourceId);
                    
                    if(hashtable != null && hashtable.size() > 0){
	                    if(infosourceId != null && hashtable.containsKey(infosourceId)){
	                        tc.setInfoSourceURL(hashtable.get(infosourceId));                        
	                        taxonConceptDao.update(tc);
	                        System.out.println("**** record updated - guid: " + guid + ", infosource id: " + infosourceId);                    
	                    }
                    }
                    else{
	                    // for ADF only ????
	                    if(infosourceId != null && "1".equals(infosourceId.trim())){
	                        tc.setInfoSourceURL("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/" + tc.getNameString());                        
	                        taxonConceptDao.update(tc);
	                        System.out.println("**** record updated - guid: " + guid + ", infosource id: " + infosourceId);
	                    } 
                    }
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

