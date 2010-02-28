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

import org.ala.dao.TaxonConceptDao;
import org.ala.lucene.CreateLoadingIndex;
import org.ala.model.CommonName;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.LoadUtils;
import org.ala.util.TabReader;
import org.apache.log4j.Logger;

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
public class ANBGDataLoader {
	
	protected static Logger logger  = Logger.getLogger(ANBGDataLoader.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		logger.info("Starting ANBG load....");
		long start = System.currentTimeMillis();
    	ANBGDataLoader loader = new ANBGDataLoader();
    	loader.load();
    	long finish = System.currentTimeMillis();
    	logger.info("Data loaded in: "+((finish-start)/60000)+" minutes.");
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
	}

	/**
	 * Add the relationships to the taxon concepts.
	 * 
	 * @throws Exception
	 */
	private static void loadRelationships() throws Exception {
		
		logger.info("Starting to load synonyms, parents, children");
		
		TaxonConceptDao tcDao = new TaxonConceptDao();
		
    	long start = System.currentTimeMillis();
    	//add the relationships
    	TabReader tr = new TabReader("/data/relationships.txt");
    	String[] keyValue = null;
		int i = 0;
		int j = 0;
    	while((keyValue=tr.readNext())!=null){
    		if(keyValue.length==3){
    			i++;
    			//add the relationship to the "toTaxon"
        		if(++i % 1000==0) 
        			logger.info(i+" relationships processed");
    			
    			if(keyValue[2].endsWith("HasSynonym")){
    				TaxonConcept synonym = tcDao.getByGuid(keyValue[1]);
    				if(synonym!=null){
    					tcDao.addSynonym(keyValue[0], synonym);
    					j++;
    				}
    			}

    			if(keyValue[2].endsWith("IsChildTaxonOf")){
    				
    				//from-to-rel
    				TaxonConcept tc = tcDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					tcDao.addChildTaxon(keyValue[0], tc);
    				} else {
    					logger.warn("Unable to add child - No concept for :"+keyValue[1]);
    				}
    				
//    				tcDao.addOverlapsWith();
    			}
    			
    			if(keyValue[2].endsWith("IsParentTaxonOf")){
//    				tcDao.addOverlapsWith();
    				TaxonConcept tc = tcDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					tcDao.addParentTaxon(keyValue[0], tc);
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
	private static void loadTaxonNames() throws Exception {
		TabReader tr = new TabReader("/data/taxonNames.txt");
		LoadUtils loadUtils = new LoadUtils();
    	String[] record = null;
    	TaxonConceptDao tcDao = new TaxonConceptDao();
    	int i = 0;
    	int j = 0;
    	while((record = tr.readNext())!=null){
    		i++;
    		if(record.length!=8){
    			logger.info("truncated record: "+record);
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
    		
    		
    		

    		//add this taxon name to each taxon concept
    		for(TaxonConcept tc: tcs){
    			j++;
    			tcDao.addTaxonName(tc.guid, tn);
    		}
    	}
    	logger.info(i+" lines read. "+j+" names added to concept records.");
	}

	/**
	 * Load the taxon concepts into the profiler
	 * 
	 * @throws Exception
	 */
	private static void loadTaxonConcepts() throws Exception {
		
		LoadUtils loadUtils = new LoadUtils();
		TabReader tr = new TabReader("/data/taxonConcepts.txt");
		TaxonConceptDao tcDao = new TaxonConceptDao();
    	String[] record = null;
    	List<TaxonConcept> tcBatch = new ArrayList<TaxonConcept>();
    	long start = System.currentTimeMillis();
    	int i=0;
    	int j=0;
    	try {
	    	while((record = tr.readNext())!=null){
	    		i++;
	    		if(i%1000==0){
	    			tcDao.create(tcBatch);
	    			tcBatch.clear();
	    		}
	    		if(record.length==9){
	    			
	    			boolean isVernacular = loadUtils.isVernacularConcept(record[0]);
	    			boolean isCongruent = loadUtils.isCongruentConcept(record[0]);
	    			
	    			//dont add vernacular or congruent concepts
	    			if(!isVernacular && !isCongruent){
		    			TaxonConcept tc = new TaxonConcept();
		    			tc.guid = record[0];
		    			tc.nameGuid = record[1];
		    			tc.nameString = record[2];
		    			tc.author = record[3];
		    			tc.authorYear = record[4];
		    			tc.publishedInCitation = record[5];
		    			tc.publishedIn = record[6];
		    			tc.acceptedConceptGuid = record[8];
		    			tcBatch.add(tc);
		    			j++;
	    			}
	    		} else {
	    			logger.error(i+" - missing fields: "+record.length+" record:"+record);
	    		}
	    	}
	    	
	    	//add the remainder
			tcDao.create(tcBatch);
			tcBatch.clear();
	    	
	    	long finish = System.currentTimeMillis();
	    	logger.info(i+" lines read, "+j+" loaded taxon concepts in: "+(((finish-start)/1000)/60)+" minutes.");
    	} catch (Exception e){
    		logger.error(i+" error on line");
    		e.printStackTrace();
    	}
	}
	
	/**
	 * Load the vernacular concepts
	 * 
	 * @throws Exception
	 */
	private static void loadVernacularConcepts() throws Exception {
		
		LoadUtils loadUtils = new LoadUtils();
		TabReader tr = new TabReader("/data/taxonConcepts.txt");
		TaxonConceptDao tcDao = new TaxonConceptDao();
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
		    				tcDao.addCommonName(guid, cn);
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
}
