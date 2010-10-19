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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.InfoSource;
import org.ala.model.SpecimenHolding;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.checklist.lucene.model.NameSearchResult;

/**
 * This class loads data from Botanical Gardens data file (data.csv) into the Cassandra
 * 
 * @author MOK011
 *
 */

@Component("specimenHoldingLoader")
public class SpecimenHoldingLoader {	
	protected static Logger logger  = Logger.getLogger(SpecimenHoldingLoader.class);	
	private static final String INPUT_FILE_NAME = "/data/bie-staging/specimenHolding/";
	@Inject
	protected InfoSourceDAO infoSourceDao;	
	@Inject
	protected TaxonConceptDao taxonConceptDao;	
	
    public static enum BOTANTICAL_GARDENS_IDX {URL, INSTITUTION, SITE_NAME, FAMILY, GENUS, HYBRID_Q, SPECIES,
		SCIENCETIFIC_NAME, INFRASPECIFIC_Q, INFRASPECIFIC_NAME, CULTIVAR, COMMON_NAME, NOTES, COUNT, UNKNOWN}    
	
	/**
	 * Usage: inputFileName
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Input File Name Missing ....");
			System.exit(0);
		}		
		System.out.println("Starting SpecimenHoldingLoader process.....");
		ApplicationContext context = SpringUtils.getContext();
		SpecimenHoldingLoader l = context.getBean(SpecimenHoldingLoader.class);
		try {
			System.out.println("Starting load process.....");
			l.load(args[0]);
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

		try{
			reader = new CSVReader(new FileReader(INPUT_FILE_NAME + fileName));
			//ignore first header line
			nextLine = reader.readNext();
			//first data line
			nextLine = reader.readNext();
			while (nextLine != null) {
				List<SpecimenHolding> list = new ArrayList<SpecimenHolding> ();
				SpecimenHolding sh = toSpecimenHolding(nextLine);
				if(sh == null){
					logger.debug("*** SCIENCETIFIC_NAME: " + nextLine[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()] + ", guid: ");	
				}
				else{
					logger.debug("*** SCIENCETIFIC_NAME: " + nextLine[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()] + ", guid: " + sh.getIdentifier());
				}
				nextLine = reader.readNext();
				// have guid & infosource ....
				if(sh != null){
					list.add(sh);
					// more than one row have same guid, ignore it
					while (nextLine != null) {
						SpecimenHolding nextKey = toSpecimenHolding(nextLine);																		
//						if(nextKey != null && sh.getIdentifier().equals(nextKey.getIdentifier())){
						if(nextKey != null && !sh.equals(nextKey)){
							logger.debug("*** SCIENCETIFIC_NAME: " + nextLine[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()] + ", guid: " + nextKey.getIdentifier());
							list.add(nextKey);
							nextLine = reader.readNext();
						}
						else{
							break;
						}
					}					
				}
				if(!list.isEmpty() && sh.getIdentifier() != null && sh.getIdentifier().length() > 0){
					try {
						ctr += list.size();
						taxonConceptDao.addSpecimenHoldings(sh.getIdentifier(), list);
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}
			}
		}
		catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
		finally{
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

	/**
	 * populate data into SpecimenHolding.
	 * 
	 * @param idLifeData csv data line
	 * @return
	 */
	private SpecimenHolding toSpecimenHolding(String[] data){
		SpecimenHolding o = null;
		String tmp = null;
		String guid = "";
		
		// get guid & infosource
		if("".equals(data[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()].trim())){
			String scientificName = "";
			if(data[BOTANTICAL_GARDENS_IDX.CULTIVAR.ordinal()].trim().startsWith("'")){
				scientificName = data[BOTANTICAL_GARDENS_IDX.GENUS.ordinal()].trim() + " " + 
					data[BOTANTICAL_GARDENS_IDX.CULTIVAR.ordinal()].trim();
			}
			else{
				scientificName = data[BOTANTICAL_GARDENS_IDX.GENUS.ordinal()].trim() + " '" + 
				data[BOTANTICAL_GARDENS_IDX.CULTIVAR.ordinal()].trim() + "'";
			}
			try {				
				NameSearchResult rs = taxonConceptDao.findCBDataByName(scientificName, null, null);
				logger.debug("*** findCBDataByName(SCIENCETIFIC_NAME): " + scientificName + ", NameSearchResult: " + rs);
				if(rs != null && "Genus".equalsIgnoreCase(rs.getRank().name())){
					guid = rs.getLsid();
				}
				else{
					return o;
				}
			} catch (Exception e) {
				logger.error(e);
				return o;
			}
		}
		else{
		
			guid = taxonConceptDao.findLsidByName(data[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()].trim());
		}
		
		InfoSource infosource = infoSourceDao.getByUri(data[BOTANTICAL_GARDENS_IDX.URL.ordinal()].trim());
		logger.debug("guid: " + guid + ", infosourceId: " + infosource);
		if(guid != null && guid.length() > 0 && infosource != null){			
			o = new SpecimenHolding();			
			o.setInfoSourceId("" + infosource.getId());
			o.setInfoSourceName(infosource.getName());
			o.setInfoSourceURL(data[BOTANTICAL_GARDENS_IDX.URL.ordinal()].trim());
			o.setIdentifier(guid);
			
			//some data in csv file have less column, copy it into fix size array.
			String[] copy = new String[BOTANTICAL_GARDENS_IDX.values().length];
			for(int j = data.length; j < copy.length; j++){
				copy[j] = "";
			}
			//copy data
			for(int i = 0; i < data.length; i++){
				copy[i] = data[i];
			}
			
			o.setUrl(copy[BOTANTICAL_GARDENS_IDX.URL.ordinal()].trim());
			o.setInstitutionName(copy[BOTANTICAL_GARDENS_IDX.INSTITUTION.ordinal()].trim());
			o.setSiteName(copy[BOTANTICAL_GARDENS_IDX.SITE_NAME.ordinal()].trim());
			o.setFamily(copy[BOTANTICAL_GARDENS_IDX.FAMILY.ordinal()].trim());
			o.setGenus(copy[BOTANTICAL_GARDENS_IDX.GENUS.ordinal()].trim());
			o.setHybirdQualifier(copy[BOTANTICAL_GARDENS_IDX.HYBRID_Q.ordinal()].trim());
			o.setSpecies(copy[BOTANTICAL_GARDENS_IDX.SPECIES.ordinal()].trim());
			o.setScientificName(copy[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()].trim());
			o.setInfraspecificQualifier(copy[BOTANTICAL_GARDENS_IDX.INFRASPECIFIC_Q.ordinal()].trim());
			o.setInfraspecificName(copy[BOTANTICAL_GARDENS_IDX.INFRASPECIFIC_NAME.ordinal()].trim());
			o.setCultivar(copy[BOTANTICAL_GARDENS_IDX.CULTIVAR.ordinal()].trim());
			o.setCommonName(copy[BOTANTICAL_GARDENS_IDX.COMMON_NAME.ordinal()].trim());
			o.setNotes(copy[BOTANTICAL_GARDENS_IDX.NOTES.ordinal()].trim());
			
			tmp = copy[BOTANTICAL_GARDENS_IDX.COUNT.ordinal()].trim();
			if(tmp != null && tmp.length() > 0){
				o.setCount(Integer.parseInt(tmp));
			}
		}
		else{
			if(infosource == null){
				logger.warn("Unable to find infosource : " + data[BOTANTICAL_GARDENS_IDX.URL.ordinal()].trim());
			}
			else{
				logger.warn("Unable to find LSID for '" + data[BOTANTICAL_GARDENS_IDX.SCIENCETIFIC_NAME.ordinal()].trim() + "'");
			}
		}
		return o;
	}
}

