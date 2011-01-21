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
import java.io.IOException;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.InfoSource;
import org.ala.model.SensitiveStatus;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A temporary loader that adds sensitivity status values to taxa.
 * 
 * This should eventually be replaced with a class that relies on SDS
 * webservices.
 * 
 * @author Dave Martin
 */
@Component("sensitiveStatusLoader")
public class SensitiveStatusLoader {
	
	protected static Logger logger  = Logger.getLogger(SensitiveStatusLoader.class);
	
	private static final String DEFAULT_INPUT_FILE_NAME = "/data/bie-staging/sds/SensitiveSpecies.csv";
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	@Inject
	protected InfoSourceDAO infoSourceDao;
		
	/**
	 * Usage: inputFileName
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting Sensitive data process.....");
		ApplicationContext context = SpringUtils.getContext();
		SensitiveStatusLoader l = context.getBean(SensitiveStatusLoader.class);
		try {
			System.out.println("Starting load process.....");
			String fileName = DEFAULT_INPUT_FILE_NAME;
			
			if (args.length == 1) {
				fileName = args[0];
			}
			
			l.load(fileName);
			System.out.println("load process finished.....");
		} catch (Exception e) {			
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
	
	/**
	 * data load process
	 */
	private void load(String fileName){
		int ctr = 0;
		String[] nextLine = null;
		CSVReader reader = null;
		int lineNo = 1;
		try {
			reader = new CSVReader(new FileReader(fileName), ',');
			//first data line
			nextLine = reader.readNext();
			
			while (nextLine != null && nextLine.length==7) {
				//a single line contains multiple status
				String[] zones = nextLine[3].split(",");
				String[] infosources = nextLine[6].split(",");

				//retrieve taxon concept
				String guid = taxonConceptDao.findLsidByName(nextLine[0], null);
				
				//retrieve the infosource to be used
				for(int i=0; i<zones.length; i++){
					SensitiveStatus ss = new SensitiveStatus();
					ss.setSensitivityCategory(nextLine[4]);
					ss.setSensitivityZone(zones[i]);
					InfoSource infosource = infoSourceDao.getByUri(infosources[i].trim());
    				ss.setInfoSourceId(Integer.toString(infosource.getId()));
    				ss.setInfoSourceName(infosource.getName());
    				ss.setInfoSourceURL(infosource.getWebsiteUrl());
					
					taxonConceptDao.addSensitiveStatus(guid, ss);
				}
				lineNo++;
				nextLine = reader.readNext();
			}
		} catch (Exception e) {
			logger.error("Line number: "+lineNo, e);
			e.printStackTrace();
		} finally{
			System.out.println("*** Total Records Updated: " + ctr);
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
}
