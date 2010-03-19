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
import java.io.IOException;
import java.util.Iterator;

import org.ala.dao.TaxonConceptDao;
import org.ala.dao.TaxonConceptDaoImpl;
import org.ala.model.Classification;
import org.ala.model.Rank;
import org.apache.log4j.Logger;
import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.ArchiveFile;
import org.gbif.dwc.text.UnsupportedArchiveException;


/**
 * Reads a Darwin Core extracted which contains a classification for a taxon,
 * and add this classification to the taxon profile.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class DwcClassificationLoader {
    protected static Logger logger = Logger.getLogger(DwcClassificationLoader.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DwcClassificationLoader l = new DwcClassificationLoader();
		l.load();
	}

	/**
	 * Load the contents of the file.
	 * 
	 * @throws IOException
	 * @throws UnsupportedArchiveException
	 * @throws Exception
	 */
	public void load() throws IOException, UnsupportedArchiveException,
			Exception {
		Archive archive = ArchiveFactory.openArchive(new File("/data/bie-staging/checklistbank/"),true);
		ArchiveFile coreFile = archive.getCore();
        if (coreFile.hasTerm(DwcTerm.scientificName)){
            System.out.println("Has scientific name");
        }
        
//        ClosableIterator<StarRecord> iter = archive.iterator();
        
		Iterator<DarwinCoreRecord> iter = archive.iteratorDwc();
		TaxonConceptDao taxonConceptDao = new TaxonConceptDaoImpl();
//		
		while(iter.hasNext()){
			
//			StarRecord sr = iter.next();
//			System.out.println(sr.value("kingdomID"));
			
			
			DarwinCoreRecord dwc = iter.next();
			String guid = dwc.getTaxonID();
			if(guid!=null){
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
	}
}
