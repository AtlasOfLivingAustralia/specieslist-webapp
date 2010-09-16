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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
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
	private static final String SUBSPECIES_REGION_OCCURRENCE = "/data/bie-staging/biocache/subspecies_region.txt";
        private static final String OCCURRENCE_COUNT = "/data/bie-staging/biocache/taxa_occurrence_count.txt";
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		BioCacheLoader l = context.getBean(BioCacheLoader.class);
		l.load();
		System.exit(0);
	}
	
	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		loadRegions(FAMILY_REGION_OCCURRENCE, "family");
		loadRegions(GENUS_REGION_OCCURRENCE, "genus");
		loadRegions(SPECIES_REGION_OCCURRENCE, "species");
		loadRegions(SUBSPECIES_REGION_OCCURRENCE, "subspecies");
                loadOccurrenceCounts();
	}

        private void loadOccurrenceCounts() throws Exception {
            long start = System.currentTimeMillis();
            logger.info("Starting to load the occurrence counts");
            int t=0;
            CSVReader tr = new CSVReader(new FileReader(OCCURRENCE_COUNT), '\t', '"');
            String[] values = null;
            while ((values = tr.readNext()) != null) {
                t++;
                if(values.length == 2){
                    String guid = values[0];
                    Integer count = Integer.parseInt(values[1]);
                    taxonConceptDao.setOccurrenceRecordsCount(guid, count);
                }
                if(t%1000 ==0)
                    logger.debug("Finished processing " + t + " taxa " + (System.currentTimeMillis()-start) + " ms");
            }
            long finish = System.currentTimeMillis();
            logger.info("Occurrence counts for "+ t+ " taxa finished in "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
        }

	/**
	 * @param regionDatFile
	 * @throws Exception
	 */
	private void loadRegions(String regionDatFile, String taxonRank) throws Exception {
		logger.info("Starting to load region occurrences from " + regionDatFile);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
    	CSVReader tr = new CSVReader(new FileReader(regionDatFile), '\t', '"');
    	String[] values = null;
		int noOfTaxa = 0;
		int noOfRegions = 0;
		String currentGuid = null;
		List<OccurrencesInGeoregion> regions = new ArrayList<OccurrencesInGeoregion>();
		while ((values = tr.readNext()) != null) {
    		if (values.length == 6) {
    			String guid = values[0];
    			String regionType = values[1];
    			String regionId = values[2];
    			String regionName = values[3];
    			Integer regionTypeId = Integer.parseInt(values[4]);
    			Integer occurrences = Integer.parseInt(values[5]);
    			if (!guid.equals(currentGuid) && !regions.isEmpty()) {
					// Flush list of regions
					taxonConceptDao.addRegions(currentGuid, regions);
					logger.debug("Added region list for guid = " + currentGuid +", number of regions = "+regions.size());
    				noOfTaxa++;
    				regions.clear();
    			}
    			
				OccurrencesInGeoregion region = new OccurrencesInGeoregion(guid, regionId, regionName, regionTypeId, regionType, occurrences);
				logger.debug("Adding guid=" + guid + " Region=" + regionName + " Type=" + regionType + " regionTypeId=" + regionTypeId + " Occs=" + occurrences);
				regions.add(region);
				noOfRegions++;
    			
    			currentGuid = guid;
    		} else {
    			logger.error("Incorrect number of fields in tab file - " + regionDatFile+", found: "+values.length);
    		}
    		
		}
		if (!regions.isEmpty()) {
			// Flush list of regions
			taxonConceptDao.addRegions(currentGuid, regions);
			logger.debug("Added region list for guid = " + currentGuid +", number of regions = "+regions.size());
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
