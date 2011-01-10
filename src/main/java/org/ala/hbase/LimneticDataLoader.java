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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.InfoSource;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.org.ala.data.model.LinnaeanRankClassification;
/**
 * This class loads data reports extracted from Limnetic files into the BIE.
 *
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
@Component("limneticDataLoader")
public class LimneticDataLoader {

	protected static Logger logger  = Logger.getLogger(LimneticDataLoader.class);

	private static final String DATA_DIR = "/data/bie-staging/limnetic";

	protected static final String HABITAT_CODE= "Limnetic";
	protected static final int INFOSOURCE_ID = 1063;

	@Inject
	protected InfoSourceDAO infoSourceDao;

	@Inject
	protected TaxonConceptDao taxonConceptDao;

	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		LimneticDataLoader l = context.getBean(LimneticDataLoader.class);
		l.load();
		System.exit(1);
	}

	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		loadCsvData(DATA_DIR);
	}

	private void loadCsvData(String dir) throws Exception {
		System.out.println("Starting to load data from " + dir);


		InfoSource infosource = infoSourceDao.getById(INFOSOURCE_ID);

		long start = System.currentTimeMillis();

		// add the taxon concept regions

		File inputDir = new File(dir);

		if (inputDir.isDirectory()) {

			String[] inputFiles = inputDir.list();

			for (String csvFile : inputFiles) {
				
				if (csvFile.endsWith(".csv")) {
					csvFile = dir + File.separator + csvFile;
					
					TabReader tr = new TabReader(csvFile);
					String[] values = null;
					int i = 0;
					String guid = null;
					String previousScientificName = null;
					while ((values = tr.readNext()) != null) {
						if (values.length == 8) {
							String identifier = values[0];
							String kingdom = values[1];
							String phylum = values[2];
							String klass = values[3];
							String order = values[4];
							String family = values[5];
							String genus = values[6];
							String currentScientificName = values[7];
							
							System.out.println("Processing '" + currentScientificName + "'");
							
							LinnaeanRankClassification linnaeanRankClassification = new LinnaeanRankClassification(kingdom, phylum, klass, order, family, genus, currentScientificName);

							if (!currentScientificName.equalsIgnoreCase(previousScientificName)) {
								guid = taxonConceptDao.findLsidByName(currentScientificName, linnaeanRankClassification, null);
								if (guid == null) {
									System.out.println("Unable to find LSID for '" + currentScientificName + "'");
								} else {
									System.out.println("Found LSID for '" + currentScientificName + "' - " + guid);
								}
								previousScientificName = currentScientificName;
							}
							if (guid != null) {

								List<Habitat> habitatList = new ArrayList<Habitat>();
								Habitat h = new Habitat(HABITAT_CODE);
								h.setInfoSourceId(Integer.toString(infosource.getId()));
								h.setInfoSourceName(infosource.getName());
								habitatList.add(h);

								System.out.println("Adding guid=" + guid + " SciName=" + currentScientificName + " Habitat=" + HABITAT_CODE);
								taxonConceptDao.addHabitat(guid, habitatList);
								i++;
							}
						} else {
							logger.error("Incorrect number of fields in tab file - " + csvFile);
						}
					}
					tr.close();
					long finish = System.currentTimeMillis();
					System.out.println(i+" Limnetic records loaded. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
				}
			}
		}
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}

	/**
	 * @param infoSourceDao the infoSourceDao to set
	 */
	public void setInfoSourceDao(InfoSourceDAO infoSourceDao) {
		this.infoSourceDao = infoSourceDao;
	}
}
