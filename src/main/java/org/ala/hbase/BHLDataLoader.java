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

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Reference;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loads the references in BHL into the taxon concept 
 * profiles in the BIE 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component
public class BHLDataLoader {

	protected static Logger logger = Logger.getLogger(BHLDataLoader.class);
	
	public static String BHL_EXPORT="/data/bie-staging/bhl/bhlexport.txt";
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext c = SpringUtils.getContext();
		BHLDataLoader l = c.getBean(BHLDataLoader.class);
		l.load();
	}

	/**
	 * 
	 * @throws Exception
	 */
	private void load() throws Exception {
		
		logger.info("Starting to load literature references from: " + BHL_EXPORT);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
    	CSVReader tr = new CSVReader(new FileReader(BHL_EXPORT), '\t', '"');
    	
    	String[] values = null;
		int i = 0;
		Reference r = null;
		
		while ((values = tr.readNext()) != null) {
    		if (values.length == 7) {
    			
    			//if the itemID changes or the scientific name changes, then sync to profiles
    			if(r==null || !r.getIdentifier().equals(values[3]) || !r.getScientificName().equals(values[5])){
    				
    				if(r!=null){
    					//sync profile
    	    			String guid = taxonConceptDao.findLsidByName(r.getScientificName(), null);
    	    			if(guid!=null){
    	    				logger.debug("Add reference to " + guid
    	    						+" for document with id: "+r.getIdentifier()
    	    						+", scientificName: "+r.getScientificName());
    	    				taxonConceptDao.addReference(guid, r);
    	    				i++;
    	    			} else {
    	    				logger.debug("Unable to find concept for name: " + r.getScientificName());
    	    			}
    				}
    				//set the last identifier
        			r = new Reference();
        			r.setTitle(values[1]);
        			r.setIdentifier(values[3]);
        			r.getPageIdentifiers().add(values[4]);
        			r.setScientificName(values[5]);
    			} else {
    				r.getPageIdentifiers().add(values[4]);
    			}
    		} else {
    			logger.error("Incorrect number of fields in tab file - " + BHL_EXPORT);
    		}
		}
    	tr.close();
		long finish = System.currentTimeMillis();
		logger.info(i+" literature references loaded. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
