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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Classification;
import org.ala.model.Rank;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Reads a Darwin Core extracted which contains a classification for a taxon,
 * and add this classification to the taxon profile.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("checklistBankLoader")
public class ChecklistBankLoader {
	
    protected static Logger logger = Logger.getLogger(ChecklistBankLoader.class);
    
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	private static final String IDENTIFIERS_FILE="/data/bie-staging/checklistbank/cb_identifiers.txt";
    
	private static final String CB_EXPORT_DIR="/data/bie-staging/checklistbank/";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		ChecklistBankLoader l = context.getBean(ChecklistBankLoader.class);
		//FIXME do two passes on this file - not ideal
		logger.info("Loading concepts....");
		l.loadConcepts();
		logger.info("Loading synonyms....");
		l.loadSynonyms();
		logger.info("Loading identifiers....");
//		l.loadIdentifiers();
		
		logger.info("Finished loading checklistbank data.");
		System.exit(0);
	}

	/**
	 * Load the contents of the file.
	 * 
	 * @throws IOException
	 * @throws UnsupportedArchiveException
	 * @throws Exception
	 */
	public void loadConcepts() throws IOException, UnsupportedArchiveException, Exception {
		
		Archive archive = ArchiveFactory.openArchive(new File(CB_EXPORT_DIR),true);
		Iterator<DarwinCoreRecord> iter = archive.iteratorDwc();
		int numberRead = 0;
		int numberAdded = 0;
		while (iter.hasNext()) {
			numberRead++;
			DarwinCoreRecord dwc = iter.next();
			String guid = dwc.getTaxonID();
			String identifier = dwc.getIdentifier();
			if(guid == null){
				guid = identifier;
			}
			
			if (guid != null && StringUtils.isEmpty(dwc.getAcceptedNameUsageID())) {
				
				//add the base concept
				TaxonConcept tc = new TaxonConcept();
				tc.setId(Integer.parseInt(identifier));
				tc.setGuid(guid);
				tc.setParentId(dwc.getParentNameUsageID());
				tc.setNameString(dwc.getScientificName());
				tc.setAuthor(dwc.getScientificNameAuthorship());
				tc.setRankString(dwc.getTaxonRank());
				
				if (taxonConceptDao.create(tc)) {
					logger.info("Adding concept: "+tc);
					numberAdded++;
				}
				
				//add the classification
				Classification c = new Classification();
				c.setGuid(dwc.getTaxonID());
				c.setScientificName(dwc.getScientificName());
				c.setRank(dwc.getTaxonRank());
                c.setSpecies(dwc.getSpecificEpithet());
                c.setGenus(dwc.getGenus());
                c.setFamily(dwc.getFamily());
                c.setOrder(dwc.getOrder());
                c.setPhylum(dwc.getPhylum());
                c.setKingdom(dwc.getKingdom());
                // Attempt to set the rank Id via Rank enum
                try {
                    c.setRankId(Rank.getForName(dwc.getTaxonRank()).getId());
                } catch (Exception e) {
                    logger.warn("Could not set rankId for: "+dwc.getTaxonRank()+" in "+guid);
                }
				taxonConceptDao.addClassification(guid, c);
			}
		}
		logger.info(numberAdded + " concepts added from " + numberRead + " rows of Checklist Bank data.");
	}
		
	public void loadSynonyms() throws IOException, UnsupportedArchiveException, Exception {
		Archive archive = ArchiveFactory.openArchive(new File(CB_EXPORT_DIR),true);
		Iterator<DarwinCoreRecord> iter = archive.iteratorDwc();
		int numberRead = 0;
		int numberAdded = 0;
		while (iter.hasNext()) {
			numberRead++;
			DarwinCoreRecord dwc = iter.next();
			String guid = dwc.getTaxonID();
			String identifier = dwc.getIdentifier();
			if(guid == null){
				guid = identifier;
			}
			
			if (guid != null && StringUtils.isNotEmpty(dwc.getAcceptedNameUsageID())) {
				
				//add the base concept
				TaxonConcept tc = new TaxonConcept();
				tc.setId(Integer.parseInt(identifier));
				tc.setGuid(guid);
				tc.setParentId(dwc.getParentNameUsageID());
				tc.setNameString(dwc.getScientificName());
				tc.setAuthor(dwc.getScientificNameAuthorship());
				tc.setRankString(dwc.getTaxonRank());
				
				String acceptedGuid = dwc.getAcceptedNameUsageID();
				logger.info("Adding synonym: "+tc);
				if (taxonConceptDao.addSynonym(acceptedGuid, tc)) {
					numberAdded++;
				}
			}
		}
		logger.info(numberAdded + " synonyms added from " + numberRead + " rows of Checklist Bank data.");
	}

	private void loadIdentifiers()
			throws FileNotFoundException, IOException, Exception {
		//read the identifiers file
		CSVReader reader = new CSVReader(new FileReader(IDENTIFIERS_FILE),'\t', '\n');
		String[] line = null;
		int numberRead = 0;
		int numberAdded = 0;
		while((line = reader.readNext())!=null){
			numberRead++;
			if(line[1]!=null && line[2]!=null){
				//add this guid somewhere
				if(taxonConceptDao.addIdentifier(line[1], line[2])){
					numberAdded++;
				}
			}
		}
		logger.info(numberAdded + " identifiers added from " + numberRead + " rows of Checklist Bank data.");		
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
