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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.ala.dao.InfoSourceDAO;
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

import au.org.ala.checklist.lucene.CBIndexSearch;


/**
 * InfosourceUidLoader.
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 * 
 */
@Component("infosourceUidLoader")
public class InfosourceUidLoader {
    protected static Logger logger  = Logger.getLogger(InfosourceUidLoader.class);	

    @Inject
    protected TaxonConceptDao taxonConceptDao;

    @Inject
    protected InfoSourceDAO infosourceDao;

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
        InfosourceUidLoader loader = context.getBean(InfosourceUidLoader.class);

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
        //		storeHelper.init();
        Scanner scanner = storeHelper.getScanner("bie", "tc", "taxonConcept");
        byte[] guidAsBytes = null;

        Map<String, String> uidInfosourceIDMap = getUidInfosourceIdMap();

        while ((guidAsBytes = scanner.getNextGuid()) != null) {
            String guid = new String(guidAsBytes);
            ExtendedTaxonConceptDTO etc = taxonConceptDao.getExtendedTaxonConceptByGuid(guid, false);
            if(etc != null){
                if (etc.getTaxonConcept() != null) {
                    TaxonConcept tc = etc.getTaxonConcept();
                    String infosourceId = tc.getInfoSourceId();
                    //			    String uid = infosourceDao.getUidByInfosourceId(infosourceId);		

                    String uid = uidInfosourceIDMap.get(infosourceId);
                    System.out.println("guid: " + guid + ", infosource id: " + infosourceId + ", uid: " + uid);

                    if(uid != null && !"".equals(uid)){
                        tc.setInfoSourceUid(uid);
                        System.out.println("guid: " + guid + ", infosource id: " + infosourceId + ", uid: " + uid);
                        taxonConceptDao.update(tc);
                    } 
                }
                
            }
        }
        logger.info("total time taken (sec) = " + ((System.currentTimeMillis() - start)/1000)); 
    }

    private Map<String, String> getUidInfosourceIdMap() {
        Map<String, String> uidInfosourceIDMap = new HashMap<String, String>();

        List<Integer> infosourceIdList = infosourceDao.getIdsforAll();

        for (Integer infosourceId : infosourceIdList) {
            String uid = infosourceDao.getUidByInfosourceId(infosourceId.toString());
            uidInfosourceIDMap.put(infosourceId.toString(), uid);
        }

        return uidInfosourceIDMap;
    }

    /**
     * @param storeHelper the storeHelper to set
     */
    public void setStoreHelper(StoreHelper storeHelper) {
        this.storeHelper = storeHelper;
    }
}
