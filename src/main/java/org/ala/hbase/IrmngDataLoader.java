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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
/**
 * This class loads data reports extracted from IRMNG into the BIE.
 *
 * @author Peter Flemming (Peter.Flemming@csiro.au)
 */
@Component("irmngDataLoader")
public class IrmngDataLoader {
	
	protected static Logger logger  = Logger.getLogger(IrmngDataLoader.class);
	
	private static final String IRMNG_FAMILY_DATA = "/data/bie-staging/irmng/family_list.txt";
	private static final String IRMNG_GENUS_DATA = "/data/bie-staging/irmng/genus_list.txt";
	private static final String IRMNG_SPECIES_DATA = "/data/bie-staging/irmng/species_list.txt";
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		IrmngDataLoader l = context.getBean(IrmngDataLoader.class);
		l.load();
	}
	
	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		// For testing
//		((TaxonConceptDaoImpl) taxonConceptDao).setLuceneIndexLocation(LoadUtils.BASE_DIR + "taxonConcept");
		
		loadIrmngData(IRMNG_FAMILY_DATA);
		loadIrmngData(IRMNG_GENUS_DATA);
		loadIrmngData(IRMNG_SPECIES_DATA);
	}

	private void loadIrmngData(String irmngDataFile) throws Exception {
		logger.info("Starting to load IRMNG data from " + irmngDataFile);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
    	TabReader tr = new TabReader(irmngDataFile);
    	String[] values = null;
		int i = 0;
		String guid = null;
		String previousScientificName = null;
		while ((values = tr.readNext()) != null) {
    		if (values.length == 5) {
    			String currentScientificName = values[1];
    			String extantCode = values[3];
    			String habitatCode = values[4];
    			
    			if (!currentScientificName.equalsIgnoreCase(previousScientificName)) {
    				guid = taxonConceptDao.findConceptIDForName(null, null, currentScientificName.toLowerCase());
        			if (guid == null) {
        				logger.warn("Unable to find taxon concept for '" + currentScientificName + "'");
        			} else {
        				logger.debug("Loading IRMNG data for '" + currentScientificName + "'");
        			}
    				previousScientificName = currentScientificName;
    			}
    			if (guid != null) {
    				
    				List<ExtantStatus> extantStatusList = new ArrayList<ExtantStatus>();
    				ExtantStatus e = new ExtantStatus(extantCode);
    				e.setInfoSourceId("4");
    				e.setInfoSourceName("IRMNG");
    				e.setInfoSourceURL("http://www.cmar.csiro.au/datacentre/irmng/");
    				extantStatusList.add(e);
    				
    				List<Habitat> habitatList = new ArrayList<Habitat>();
    				Habitat h = new Habitat(habitatCode);
    				h.setInfoSourceId("4");
    				h.setInfoSourceName("IRMNG");
    				h.setInfoSourceURL("http://www.cmar.csiro.au/datacentre/irmng/");
    				habitatList.add(h);
    				
    				logger.trace("Adding guid=" + guid + " SciName=" + currentScientificName + " Extant=" + extantCode + " Habitat=" + habitatCode);
    				taxonConceptDao.addExtantStatus(guid, extantStatusList);
    				taxonConceptDao.addHabitat(guid, habitatList);
    				i++;
    			}
    		} else {
    			logger.error("Incorrect number of fields in tab file - " + irmngDataFile);
    		}
		}
    	tr.close();
		long finish = System.currentTimeMillis();
		logger.info(i+" IRMNG records loaded. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
