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

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.CommonName;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Simple class to load in the common names as supplied by the ANBG.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("commonNamesLoader")
public class CommonNamesLoader {

	protected Logger logger = Logger.getLogger(CommonNamesLoader.class);
	
	private static final String AFD_COMMON_NAMES = "/data/bie-staging/anbg/AFD-common-names.csv";
	private static final String APNI_COMMON_NAMES = "/data/bie-staging/anbg/APNI-common-names.csv";
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		CommonNamesLoader l = context.getBean(CommonNamesLoader.class);
		l.load();
	}
	
	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		// For testing
		loadCommonNames(AFD_COMMON_NAMES);
		loadCommonNames(APNI_COMMON_NAMES);
	}

	/**
	 * @param regionDatFile
	 * @throws Exception
	 */
	private void loadCommonNames(String dataFile) throws Exception {
		logger.info("Starting to load region occurrences from " + dataFile);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
        //NC A TabReader can not be used because quoted fields can contain a comma
    	//TabReader tr = new TabReader(dataFile, true, ',');
        CSVReader tr = new CSVReader(new FileReader(dataFile), ',', '"',1);
    	String[] values = null;
        Pattern p = Pattern.compile(",");
    	int namesAdded = 0;
		while ((values = tr.readNext()) != null) {
    		if (values.length > 5) {
    			String guid = values[1];
    			String commonNameString = values[2];
                        //the common name string can be a comma separated list of names
    			String taxonConceptGuid = values[5];
    			CommonName commonName = new CommonName();
    			commonName.setGuid(guid);
    			String[] commonNameStrings = p.split(commonNameString);
                        for(String cn: commonNameStrings){
                            commonName.setNameString(cn);
                            boolean success = taxonConceptDao.addCommonName(taxonConceptGuid, commonName);
                            if(success) namesAdded++;
                        }
    			
    		}
		}
		
    	tr.close();
		long finish = System.currentTimeMillis();
		logger.info(namesAdded+" common names added to taxa. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
