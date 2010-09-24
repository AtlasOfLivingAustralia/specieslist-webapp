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
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.harvester.IdentifyLifeHarvester;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.IdentificationKey;
import org.ala.model.InfoSource;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class IdentifyLifeLoader {	
	protected static Logger logger  = Logger.getLogger(IdentifyLifeLoader.class);	
	protected String idLifeURI = "http://www.identifylife.org/";
	protected String idLifeWSURI = "http://www.identifylife.org:8001/";
	
	private static final String INPUT_FILE_NAME = "/data/bie-staging/identifylife/data.csv";
	@Inject
	protected InfoSourceDAO infoSourceDao;
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	public static void main(String[] args) throws Exception {
		String[] locations = {"classpath*:spring.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(locations);		
//		ApplicationContext context = SpringUtils.getContext();
		IdentifyLifeLoader l = context.getBean(IdentifyLifeLoader.class);
		l.load();
		System.exit(1);
	}
		
	/**
	 * @throws Exception
	 */
	private void load() throws Exception {
		String[] nextLine;
			
		CSVReader reader = new CSVReader(new FileReader(INPUT_FILE_NAME));
		while ((nextLine = reader.readNext()) != null) {
			System.out.println("*** " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()] + ", taxon: " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()]);			
			List<IdentificationKey> idKeyList = new ArrayList<IdentificationKey> ();	
			IdentificationKey idKey = toIdentificationKey(nextLine);
			if(idKey != null){
				System.out.println("*** guid: " + idKey.getIdentifier());	
				idKeyList.add(idKey);
//				taxonConceptDao.addIdentificationKeys(idKey.getIdentifier(), idKeyList);
			}			
		}
		reader.close();
	}

	private IdentificationKey toIdentificationKey(String[] idLifeData){
		IdentificationKey idKey = null;
		
		String guid = getGuid(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()]);		
		if(guid != null && guid.length() > 0){
			idKey = new IdentificationKey();
			InfoSource infosource = infoSourceDao.getByUri(idLifeURI);
			idKey.setInfoSourceId("" + infosource.getId());
			idKey.setInfoSourceName(infosource.getName());
			idKey.setInfoSourceURL(idLifeWSURI + "Keys/" + idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()]);
			idKey.setIdentifier(guid);
					
			idKey.setId(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()]);
			idKey.setTitle(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TITLE.ordinal()]);
			idKey.setUrl(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.URL.ordinal()]);
			idKey.setDescription(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.DESCRIPTION.ordinal()]);
			idKey.setPublisher(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.PUBLISHER.ordinal()]);
			idKey.setPublishedyear(Integer.parseInt(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.PUBLISHYEAR.ordinal()]));
			idKey.setTaxonomicscope(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()]);
			idKey.setGeographicscope(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.GEOGRAPHICSCOPE.ordinal()]);
			idKey.setKeytype(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.KEYTYPE.ordinal()]);
			idKey.setAccessibility(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ACCESSIBILITY.ordinal()]);
			idKey.setVocabulary(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.VOCABULARY.ordinal()]);
			idKey.setTechnicalskills(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TECHNICALSKILLS.ordinal()]);
			idKey.setImagery(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.IMAGERY.ordinal()]);
		}
		else{
			logger.warn("Unable to find LSID for '" + idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()] + "'");
		}
		return idKey;
	}
	
	private String getGuid(String taxonomicscope){
		return taxonConceptDao.findLsidByName(taxonomicscope, "genus");
	}
	
	private void loadIrmngData(String irmngDataFile, String rank, String baseUrl) throws Exception {
		logger.info("Starting to load IRMNG data from " + irmngDataFile);
		
		
		InfoSource infosource = infoSourceDao.getByUri(idLifeURI);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
    	TabReader tr = new TabReader(irmngDataFile);
    	String[] values = null;
		int i = 0;
		String guid = null;
		String previousScientificName = null;
		while ((values = tr.readNext()) != null) {
    		if (values.length == 5) {
    			String identifier = values[0];
    			String currentScientificName = values[1];
    			String extantCode = values[3];
    			String habitatCode = values[4];
    			
    			if (!currentScientificName.equalsIgnoreCase(previousScientificName)) {
					guid = taxonConceptDao.findLsidByName(currentScientificName, rank);
        			if (guid == null) {
        				logger.warn("Unable to find LSID for '" + currentScientificName + "'");
        			} else {
        				logger.debug("Found LSID for '" + currentScientificName + "' - " + guid);
        			}
    				previousScientificName = currentScientificName;
    			}
    			if (guid != null) {
    				
    				List<ExtantStatus> extantStatusList = new ArrayList<ExtantStatus>();
    				ExtantStatus e = new ExtantStatus(extantCode);
    				e.setInfoSourceId(Integer.toString(infosource.getId()));
    				e.setInfoSourceName(infosource.getName());
    				e.setInfoSourceURL(baseUrl+identifier);
    				extantStatusList.add(e);
    				
    				List<Habitat> habitatList = new ArrayList<Habitat>();
    				Habitat h = new Habitat(habitatCode);
    				h.setInfoSourceId(Integer.toString(infosource.getId()));
    				h.setInfoSourceName(infosource.getName());
    				h.setInfoSourceURL(baseUrl+identifier);
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

	/**
	 * @param infoSourceDao the infoSourceDao to set
	 */
	public void setInfoSourceDao(InfoSourceDAO infoSourceDao) {
		this.infoSourceDao = infoSourceDao;
	}
}

