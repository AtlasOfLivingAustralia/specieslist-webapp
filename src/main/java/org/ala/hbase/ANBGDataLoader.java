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

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.lucene.CreateLoadingIndex;
import org.ala.model.CommonName;
import org.ala.model.InfoSource;
import org.ala.model.Publication;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.LoadUtils;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This class loads data from exported ANBG dump files into the HBase table
 * "taxonConcept" after they have been preprocessed by a Scala script.
 * 
 * It makes use of lucene indexes for lookups of concepts, to add synonyms
 * and parent/child relationships. These indexes are generated using 
 * <code>CreateLoadingIndex</code>
 * 
 * This is currently filtering vernacular concepts and congruent 
 * concepts (favouring the "fromTaxon" in the relationship).
 * 
 * @see CreateLoadingIndex
 * 
 * @author David Martin
 */
@Component("anbgDataLoader")
public class ANBGDataLoader {
	
	protected static Logger logger  = Logger.getLogger(ANBGDataLoader.class);
	@Inject
	protected InfoSourceDAO infoSourceDAO;
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	private static final String TAXON_CONCEPTS = "/data/bie-staging/anbg/taxonConcepts.txt";
	private static final String TAXON_NAMES = "/data/bie-staging/anbg/taxonNames.txt";
	private static final String RELATIONSHIPS = "/data/bie-staging/anbg/relationships.txt";
	private static final String PUBLICATIONS = "/data/bie-staging/anbg/publications.txt";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		logger.info("Starting ANBG load....");
		long start = System.currentTimeMillis();
        ApplicationContext context = SpringUtils.getContext();
        ANBGDataLoader loader = (ANBGDataLoader) context.getBean(ANBGDataLoader.class);
    	loader.load();
    	long finish = System.currentTimeMillis();
    	logger.info("Data loaded in: "+((finish-start)/60000)+" minutes.");
//    	System.exit(1);
	}
	
	/**
	 * Load the profile data for taxon concepts. This takes around
	 * 90 mins on my laptop to run over all the ANBG data.
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
    	loadTaxonConcepts();
    	loadTaxonNames(); // includes rank information
    	loadVernacularConcepts();
    	loadRelationships();
    	loadPublications();
	}

	/**
	 * Load the publications for each concept.
	 * 
	 * @throws Exception
	 */
	private void loadPublications() throws Exception {
		logger.info("Starting to load taxon names");
		
		TabReader tr = new TabReader(PUBLICATIONS);
		LoadUtils loadUtils = new LoadUtils();
    	String[] record = null;
    	int i = 0;
    	int j = 0;
    	while((record = tr.readNext())!=null){
    		i++;
    		if(record.length<5){
    			logger.warn("truncated at line "+i+" record: "+record);
    			continue;
    		}
    		
    		List<String> tcs = loadUtils.getGuidsForPublicationGuid(record[0], 100);
    		
    		Publication p = new Publication();
    		p.setGuid(record[0]);
    		p.setTitle(record[1]);
    		p.setAuthor(record[2]);
    		p.setDatePublished(record[3]);
    		p.setPublicationType(record[4]);
    		
    		//add this taxon name to each taxon concept
    		for(String tc: tcs){
    			logger.debug("Adding publication to "+tc+" record: "+p.getGuid());
   				boolean success = taxonConceptDao.addPublication(tc, p);
   				if(success)
   					j++;
    		}
    	}
    	logger.info(i+" lines read. "+j+" publications added to concept records.");
	}

	/**
	 * Add the relationships to the taxon concepts.
	 * 
	 * @throws Exception
	 */
	private void loadRelationships() throws Exception {
		
		logger.info("Starting to load synonyms, parents, children");
		
		LoadUtils loadUtils = new LoadUtils();
		
    	long start = System.currentTimeMillis();
    	//add the relationships
    	TabReader tr = new TabReader(RELATIONSHIPS);
    	String[] keyValue = null;
		int i = 0;
		int j = 0;
    	while((keyValue=tr.readNext())!=null){
    		if(keyValue.length==3){
    			i++;
    			//add the relationship to the "toTaxon"
				if (++i % 10000 == 0)
        			logger.info(i+" relationships processed");
    			
        		//add the synonym information to the accepted concept
    			if(keyValue[2].endsWith("HasSynonym")){
    				TaxonConcept synonym = loadUtils.getByGuid(keyValue[1],1);
    				if(synonym!=null){
    					taxonConceptDao.addSynonym(keyValue[0], synonym);
    					j++;
    				}
    			}

        		//add the synonym information to the accepted concept
    			if(keyValue[2].endsWith("IsCongruentTo")){
    				
    				//currently AFD/APNI seems to organised so that
    				// the accepted concepts are marked as congruent to others...hence 
    				TaxonConcept congruentTc = loadUtils.getByGuid(keyValue[1], 1);
    				if(congruentTc!=null){
        				//get the congruent object from the loading indicies
        				TaxonConcept acceptedConcept = taxonConceptDao.getByGuid(keyValue[0]);
        				
        				if (acceptedConcept==null) {
                            logger.error("acceptedConcept is null for guid: "+keyValue[0]);
                        } else if (!congruentTc.getNameString().equals(acceptedConcept.getNameString())) {
        					taxonConceptDao.addIsCongruentTo(keyValue[0], congruentTc);
        					j++;
        				} else {
        					logger.debug("Avoiding adding congruent taxon with same name:"+acceptedConcept.getNameString()+", "+keyValue[1]);
        				}
    				}
    			}

        		//add the child information to the accepted concept
    			if(keyValue[2].endsWith("IsChildTaxonOf")){
    				
    				//from-to-rel
    				TaxonConcept tc = taxonConceptDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					taxonConceptDao.addParentTaxon(keyValue[0], tc);
    				} else {
    					logger.warn("Unable to add child - No concept for :"+keyValue[1]);
    				}
    				
//    				tcDao.addOverlapsWith();
    			}
    			
        		//add the parent information to the accepted concept
    			if(keyValue[2].endsWith("IsParentTaxonOf")){
//    				tcDao.addOverlapsWith();
    				TaxonConcept tc = taxonConceptDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					taxonConceptDao.addChildTaxon(keyValue[0], tc);
    				} else {
    					logger.warn("Unable to add parent - No concept for :"+keyValue[1]);
    				}
    			}
    			
//    			http://rs.tdwg.org/ontology/voc/TaxonConcept#Includes
//    			http://rs.tdwg.org/ontology/voc/TaxonConcept#Overlaps
//    			http://rs.tdwg.org/ontology/voc/TaxonConcept#IsHybridParentOf
//    			http://rs.tdwg.org/ontology/voc/TaxonConcept#IsHybridChildOf
/*    			
		    	doc.add(new Field("fromTaxon", keyValue[0], Store.YES, Index.ANALYZED));
		    	doc.add(new Field("toTaxon", keyValue[1], Store.YES, Index.ANALYZED));
		    	doc.add(new Field("relationship", keyValue[2], Store.YES, Index.ANALYZED));
*/		    	
    		}
		}
    	tr.close();
		long finish = System.currentTimeMillis();
    	logger.info(i+" loaded relationships, added "+j+" synonyms. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}
	
	/**
	 * Load the taxon names
	 * 
	 * @throws Exception
	 */
	private void loadTaxonNames() throws Exception {
		
		logger.info("Starting to load taxon names");
		
		TabReader tr = new TabReader(TAXON_NAMES);
		LoadUtils loadUtils = new LoadUtils();
    	String[] record = null;
    	int i = 0;
    	int j = 0;
    	while((record = tr.readNext())!=null){
    		i++;
    		if(record.length<8){
    			logger.info("truncated at line "+i+"record: "+record);
    			continue;
    		}
    		
    		List<TaxonConcept> tcs = loadUtils.getByNameGuid(record[0], 100);
    		
    		TaxonName tn = new TaxonName();
    		tn.guid = record[0];
    		tn.nameComplete = record[2];
    		tn.authorship = record[3];
    		tn.rankString = record[4];
    		tn.publishedInCitation = record[5];
    		tn.nomenclaturalCode = record[6];
    		tn.typificationString = record[7];

    		//load the publication information for the name
    		if(record[5]!=null){
    			Publication pub = loadUtils.getPublicationByGuid(record[5]);
    			if(pub!=null){
    				tn.publishedIn = pub.getTitle();
    			}
    		}
    		
    		//add this taxon name to each taxon concept
    		for(TaxonConcept tc: tcs){
    			j++;
//        		boolean isVernacular = loadUtils.isVernacularConcept(tc.guid);
    			if(addTaxonToProfile(loadUtils, tc.getGuid())){
    				//some of these name additions will fail, where the concept
    				//is not an accepted concept and is congruent to another
    				//we have not added the concept to the profile - hence the lookup will fail
    				taxonConceptDao.addTaxonName(tc.getGuid(), tn);
    				
    				//FIXME load the publication information
    				
    			}
    		}
    	}
    	logger.info(i+" lines read. "+j+" names added to concept records.");
	}

	/**
	 * Load the taxon concepts into the profiler
	 * 
	 * @throws Exception
	 */
	private void loadTaxonConcepts() throws Exception {
		
		logger.info("Starting load of taxon concepts...");
		
		LoadUtils loadUtils = new LoadUtils();
		TabReader tr = new TabReader(TAXON_CONCEPTS);
		
		InfoSource afd = infoSourceDAO.getByUri("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/home");
		InfoSource apc = infoSourceDAO.getByUri("http://www.anbg.gov.au/chah/apc/");
		InfoSource apni = infoSourceDAO.getByUri("http://www.anbg.gov.au/apni/");
		InfoSource col = infoSourceDAO.getByUri("http://www.catalogueoflife.org/");
		
    	String[] record = null;
    	long start = System.currentTimeMillis();
    	int i=0;
    	int j=0;
    	try {
	    	while((record = tr.readNext())!=null){
	    		i++;
	    		if(record.length==9){

	    			// if its congruent to another concept dont add it
	    			// if its a synonym for another concept dont add it
	    			
	    			
	    			//if its congruent to another concept and it isnt an accepted
	    			//concept, then dont add it
//	    			boolean toAdd = isCongruent && !isAccepted ? false : true;
	    			
	    			//dont add vernacular or congruent concepts
//	    			if(!isVernacular && !isCongruent){
	    			if(addTaxonToProfile(loadUtils, record[0])){
		    			TaxonConcept tc = new TaxonConcept();
		    			tc.setGuid(record[0]);
		    			tc.setNameGuid(record[1]);
		    			tc.setNameString(record[2]);
		    			tc.setAuthor(record[3]);
		    			tc.setAuthorYear(record[4]);
		    			tc.setPublishedInCitation(record[5]);
		    			tc.setPublishedIn(record[6]);

		    			String accepted = loadUtils.isAcceptedConcept(record[0]);
		    			if("APC".equals(accepted)){
		    				tc.setInfoSourceId(Integer.toString(apc.getId()));
		    				tc.setInfoSourceName(apc.getName());
		    				String internalId = record[0].substring(record[0].lastIndexOf(":")+1);
		    				tc.setInfoSourceURL("http://www.anbg.gov.au/cgi-bin/apni?taxon_id="+internalId);
		    			} else if(record[0].contains(":apni.")){
		    				tc.setInfoSourceId(Integer.toString(apni.getId()));
		    				tc.setInfoSourceName(apni.getName());
		    				String internalId = record[0].substring(record[0].lastIndexOf(":")+1);
		    				tc.setInfoSourceURL("http://www.anbg.gov.au/cgi-bin/apni?taxon_id="+internalId);
		    			} else if(record[0].contains("catalogue")){
		    				tc.setInfoSourceId(Integer.toString(col.getId()));
		    				tc.setInfoSourceName(col.getName());
		    				tc.setInfoSourceURL(col.getWebsiteUrl());
		    			} else if(record[0].contains(":afd.")){
		    				tc.setInfoSourceId(Integer.toString(afd.getId()));
		    				tc.setInfoSourceName(afd.getName());
		    				tc.setInfoSourceURL(afd.getWebsiteUrl());
		    				String internalId = record[0].substring(record[0].lastIndexOf(":")+1);
		    				tc.setInfoSourceURL("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/"+internalId);
		    			}
		    			taxonConceptDao.update(tc);
		    			j++;
	    			} 
	    		} else {
	    			logger.error(i+" - missing fields: "+record.length+" fields:"+record);
	    		}
	    	}
	    	
	    	//add the remainder
	    	
	    	long finish = System.currentTimeMillis();
	    	logger.info(i+" lines read, "+j+" loaded taxon concepts in: "+(((finish-start)/1000)/60)+" minutes.");
    	} catch (Exception e){
    		logger.error(i+" error on line");
    		e.printStackTrace();
    	}
	}
	
	/**
	 * A check to see if the supplied taxon should be added to the profiler
	 * 
	 * @param loadUtils
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public boolean addTaxonToProfile(LoadUtils loadUtils, String guid) throws Exception {
		boolean isVernacular = loadUtils.isVernacularConcept(guid);
		boolean isCongruentTo = loadUtils.isCongruentConcept(guid);
		boolean isSynonymFor = loadUtils.isSynonymFor(guid);
		return !isVernacular && !isCongruentTo && !isSynonymFor;
	}
	
	/**
	 * Load the vernacular concepts
	 * 
	 * @throws Exception
	 */
	private void loadVernacularConcepts() throws Exception {
		
		logger.info("Starting load of common names...");
		
		LoadUtils loadUtils = new LoadUtils();
		TabReader tr = new TabReader(TAXON_CONCEPTS);
    	String[] record = null;
    	long start = System.currentTimeMillis();
    	int i=0;
    	try {
	    	while((record = tr.readNext())!=null){
	    		i++;
	    		if(record.length==9){
	    			
	    			boolean isVernacular = loadUtils.isVernacularConcept(record[0]);
	    			if(isVernacular){
	    				CommonName cn = new CommonName();
		    			cn.guid = record[0];
//		    			tc.nameGuid = record[1];
		    			cn.nameString = record[2];
//		    			tc.author = record[3];
//		    			tc.authorYear = record[4];
//		    			tc.publishedInCitation = record[5];
//		    			tc.publishedIn = record[6];
//		    			tc.acceptedConceptGuid = record[8];
		    			
		    			List<String> guids = loadUtils.getIsVernacularConceptFor(record[0]);
		    			for(String guid: guids){
		    				taxonConceptDao.addCommonName(guid, cn);
		    			}
	    			}
	    		} else {
	    			logger.error(i+" - missing fields: "+record.length+" record:"+record);
	    		}
	    	}
	    	
	    	long finish = System.currentTimeMillis();
	    	logger.info("loaded taxon concepts in: "+(((finish-start)/1000)/60)+" minutes.");
    	} catch (Exception e){
    		logger.error(i+" error on line", e);
    		e.printStackTrace();
    	}
	}

	/**
	 * @param infoSourceDAO the infoSourceDAO to set
	 */
	public void setInfoSourceDAO(InfoSourceDAO infoSourceDAO) {
		this.infoSourceDAO = infoSourceDAO;
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}	
}
