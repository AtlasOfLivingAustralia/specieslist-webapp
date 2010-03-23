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
import org.ala.model.Region;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
/**
 * This class loads data reports extracted from the BioCache into the BIE.
 *
 * @author Peter Flemming (Peter.Flemming@csiro.au)
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("bioCacheLoader")
public class BioCacheLoader {
	
	protected static Logger logger  = Logger.getLogger(BioCacheLoader.class);
	
	private static final String FAMILY_REGION_OCCURRENCE = "/data/bie-staging/biocache/family_region.txt";
	private static final String GENUS_REGION_OCCURRENCE = "/data/bie-staging/biocache/genus_region.txt";
	private static final String SPECIES_REGION_OCCURRENCE = "/data/bie-staging/biocache/species_region.txt";
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		BioCacheLoader l = context.getBean(BioCacheLoader.class);
		l.load();
	}
	
	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		// For testing
//		taxonConceptDao.setLuceneIndexLocation(LoadUtils.BASE_DIR + "taxonConcept");
		
		loadRegions(FAMILY_REGION_OCCURRENCE);
		loadRegions(GENUS_REGION_OCCURRENCE);
		loadRegions(SPECIES_REGION_OCCURRENCE);
	}

	/**
	 * @param regionDatFile
	 * @throws Exception
	 */
	private void loadRegions(String regionDatFile) throws Exception {
		logger.info("Starting to load region occurrences from " + regionDatFile);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
    	TabReader tr = new TabReader(regionDatFile);
    	String[] values = null;
		int noOfTaxa = 0;
		int noOfRegions = 0;
		String guid = null;
		String previousScientificName = null;
		List<Region> regions = new ArrayList<Region>();
		while ((values = tr.readNext()) != null) {
    		if (values.length == 6) {
    			String taxonId = values[0]; //FIXME this should later be replaced with the LSIDs
    			String currentScientificName = values[1];
    			String regionType = values[2];
    			String regionId = values[3];
    			String regionName = values[4];
    			String occurrences = values[5];
    			
    			if (!currentScientificName.equalsIgnoreCase(previousScientificName)) {
    				if (!regions.isEmpty()) {
    					// Flush list of regions
    					taxonConceptDao.addRegions(guid, regions);
        				logger.trace("Added region list for guid=" + guid + " SciName=" + currentScientificName);
        				noOfTaxa++;
        				regions.clear();
    				}
    				guid = taxonConceptDao.findConceptIDForName(null, null, currentScientificName.toLowerCase());
        			if (guid == null) {
        				logger.warn("Unable to find taxon concept for '" + currentScientificName + "'");
        			} else {
        				logger.debug("Loading region occurrences for '" + currentScientificName + "'");
        			}
    				previousScientificName = currentScientificName;
    			}
    			if (guid != null) {
    				Region region = new Region(taxonId, regionId, regionName, regionType, Integer.parseInt(occurrences));
    				logger.trace("Adding guid=" + guid + " SciName=" + currentScientificName + " Region=" + regionName + " Type=" + regionType + " Occs=" + occurrences);
    				regions.add(region);
    				noOfRegions++;
    			}
    		} else {
    			logger.error("Incorrect number of fields in tab file - " + regionDatFile);
    		}
		}
		if (!regions.isEmpty()) {
			// Flush list of regions
			taxonConceptDao.addRegions(guid, regions);
			logger.trace("Added region list for guid=" + guid + " SciName=" + previousScientificName);
			noOfTaxa++;
		}
		
    	tr.close();
		long finish = System.currentTimeMillis();
		logger.info(noOfRegions+" region occurrences loaded for " + noOfTaxa + " taxa. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
