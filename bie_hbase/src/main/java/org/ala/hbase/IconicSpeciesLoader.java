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

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.checklist.lucene.CBIndexSearch;

/**
 * Loads the iconic species.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("iconicSpeciesLoader")
public class IconicSpeciesLoader {
	protected static Logger logger  = Logger.getLogger(IconicSpeciesLoader.class);
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	@Inject
	protected CBIndexSearch cbIdxSearcher;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		IconicSpeciesLoader l = context.getBean(IconicSpeciesLoader.class);
		l.load();
		System.exit(0);
	}
	
	public void load() throws Exception {
		logger.info("Loading iconic species...");
		InputStream in = getClass().getResourceAsStream("/iconicSpecies.txt");
		CSVReader r = new CSVReader(new InputStreamReader(in), '\t');
		String[] cols = r.readNext();
		while(cols!=null){
			try {
				String sciName = StringUtils.trim(cols[0]);
				String guid = cbIdxSearcher.searchForLSID(sciName);
				logger.debug(sciName+ " " + guid);
				this.taxonConceptDao.setIsIconic(guid);
			} catch (Exception e){
				logger.info(e.getMessage());
			}
			cols = r.readNext();
		}
		r.close();
		logger.info("Loaded iconic species.");
	}

	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}

	public void setCbIdxSearcher(CBIndexSearch cbIdxSearcher) {
		this.cbIdxSearcher = cbIdxSearcher;
	}
}
