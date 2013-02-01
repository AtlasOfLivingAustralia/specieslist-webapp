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
import org.ala.harvester.IdentifyLifeHarvester;
import org.ala.model.IdentificationKey;
import org.ala.model.InfoSource;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class loads data from IdentifyLifeHarvester data file (data.csv) into the Cassandra
 * 
 * @author MOK011
 *
 */

@Component
public class IdentifyLifeLoader {	
	protected static Logger logger  = Logger.getLogger(IdentifyLifeLoader.class);	
	protected String idLifeURI = "http://www.identifylife.org/";
	protected String idLifeRsURI = "http://www.identifylife.org:8001/";
	
	private static final String INPUT_FILE_NAME = "/data/bie-staging/identifylife/data.csv";
	@Inject
	protected InfoSourceDAO infoSourceDao;	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	/**
	 * 
	 * Usage: [idLifeURI][idLifeRsURI]
	 * eg: http://www.identifylife.org/ http://www.identifylife.org:8001/
	 * 
	 */	
	public static void main(String[] args) {
		System.out.println("Starting IdentifyLifeLoader process.....");
		ApplicationContext context = SpringUtils.getContext();
		IdentifyLifeLoader l = context.getBean(IdentifyLifeLoader.class);
		try {
			if (args.length == 1) {
				l.setIdLifeURI(args[0]);
			}
			else if (args.length == 2) {
				l.setIdLifeURI(args[0]);
				l.setIdLifeRsURI(args[1]);
			}
			System.out.println("Starting load process.....");
			l.load();
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
	private void load(){
		String[] nextLine = null;
		CSVReader reader = null;

		try{
			reader = new CSVReader(new FileReader(INPUT_FILE_NAME));
			nextLine = reader.readNext();
			while (nextLine != null) {
				System.out.println("*** idKey: " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()] + 
						", taxon: " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()]);					
				List<IdentificationKey> idKeyList = new ArrayList<IdentificationKey> ();
				IdentificationKey idKey = toIdentificationKey(nextLine);
				nextLine = reader.readNext();
				// have guid & infosource ....
				if(idKey != null){
					logger.warn("*** guid: " + idKey.getIdentifier());	
					idKeyList.add(idKey);
					// more than one idKey for same guid, add into same list (same cassandra column)
					while (nextLine != null) {
						System.out.println("*** idKey: " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()] + 
								", taxon: " + nextLine[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()]);	
						IdentificationKey nextKey = toIdentificationKey(nextLine);
						if(nextKey != null && idKey.getIdentifier().equals(nextKey.getIdentifier())){
							idKeyList.add(nextKey);
							nextLine = reader.readNext();
						}
						else{
							break;
						}
					}
				}
				if(!idKeyList.isEmpty() && idKey.getIdentifier() != null && idKey.getIdentifier().length() > 0){
					try {
						taxonConceptDao.addIdentificationKeys(idKey.getIdentifier(), idKeyList);
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
	 * populate data into identificationKey.
	 * 
	 * @param idLifeData csv data line
	 * @return
	 */
	private IdentificationKey toIdentificationKey(String[] idLifeData){
		IdentificationKey idKey = null;
		
		// get guid & infosource
		String guid = taxonConceptDao.findLsidByName(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()].trim());
		InfoSource infosource = infoSourceDao.getByUri(idLifeURI);
		if(guid != null && guid.length() > 0 && infosource != null){
			idKey = new IdentificationKey();			
			idKey.setInfoSourceId("" + infosource.getId());
			idKey.setInfoSourceName(infosource.getName());
			idKey.setInfoSourceURL(idLifeRsURI + "Keys/" + idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()]);
			idKey.setIdentifier(guid);
					
			idKey.setId(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ID.ordinal()].trim());
			idKey.setTitle(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TITLE.ordinal()].trim());
			idKey.setUrl(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.URL.ordinal()].trim());
			idKey.setDescription(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.DESCRIPTION.ordinal()].trim());
			idKey.setPublisher(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.PUBLISHER.ordinal()].trim());
			idKey.setPublishedyear(Integer.parseInt(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.PUBLISHYEAR.ordinal()].trim()));
			idKey.setTaxonomicscope(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()].trim());
			idKey.setGeographicscope(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.GEOGRAPHICSCOPE.ordinal()].trim());
			idKey.setKeytype(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.KEYTYPE.ordinal()].trim());
			idKey.setAccessibility(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.ACCESSIBILITY.ordinal()].trim());
			idKey.setVocabulary(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.VOCABULARY.ordinal()].trim());
			idKey.setTechnicalskills(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TECHNICALSKILLS.ordinal()].trim());
			idKey.setImagery(idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.IMAGERY.ordinal()].trim());
		}
		else{
			if(infosource == null){
				logger.warn("Unable to find infosource : " + idLifeURI);
			}
			else{
				logger.warn("Unable to find LSID for '" + idLifeData[IdentifyLifeHarvester.IDLIFE_IDX.TAXONOMICSCOPE.ordinal()].trim() + "'");				
			}			
		}
		return idKey;
	}
	
	public String getIdLifeURI() {
		return idLifeURI;
	}

	public void setIdLifeURI(String idLifeURI) {
		this.idLifeURI = idLifeURI;
	}

	public String getIdLifeRsURI() {
		return idLifeRsURI;
	}

	public void setIdLifeRsURI(String idLifeRsURI) {
		this.idLifeRsURI = idLifeRsURI;
	}	
}

