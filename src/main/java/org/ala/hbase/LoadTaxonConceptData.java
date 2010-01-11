package org.ala.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.CommonName;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.TabReader;

public class LoadTaxonConceptData {

	static final Pattern pipeDelimited = Pattern.compile(">>>");
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//tc:hasName "urn:lsid"
		//tc:nameString	"Aus bus"
		//tc:hasVernacularConcept "urn:lsid"
		//tc:hasVernacularConcept:hasName	"Dusada"
		System.out.println("Starting ANBG load....");
		long start = System.currentTimeMillis();
    	loadTaxonConcepts();
    	loadTaxonNames(); // includes rank information
    	loadVernacularConcepts();
    	loadRelationships();
    	long finish = System.currentTimeMillis();
    	System.out.println("Data loaded in: "+((finish-start)/60000)+" minutes.");
	}

	private static void loadRelationships() throws Exception {
		
		System.out.println("Starting to load synonyms, parents, children");
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
        			System.out.println(i+" relationships processed");
    			
    			if(keyValue[2].endsWith("HasSynonym")){
    				TaxonConcept synonym = tcDao.getByGuid(keyValue[1]);
    				if(synonym!=null){
    					tcDao.addSynonym(keyValue[0], synonym.guid, synonym.nameString);
    					j++;
    				}
    			}

    			if(keyValue[2].endsWith("IsChildTaxonOf")){
    				
    				//from-to-rel
    				TaxonConcept tc = tcDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					tcDao.addChildTaxon(keyValue[0], tc.guid, tc.nameString);
    				} else {
    					System.err.println("Unable to add child - No concept for :"+keyValue[1]);
    				}
    				
//    				tcDao.addOverlapsWith();
    			}
    			
    			if(keyValue[2].endsWith("IsParentTaxonOf")){
//    				tcDao.addOverlapsWith();
    				TaxonConcept tc = tcDao.getByGuid(keyValue[1]);
    				if(tc!=null){
    					tcDao.addParentTaxon(keyValue[0], tc.guid, tc.nameString);
    				} else {
    					System.err.println("Unable to add parent - No concept for :"+keyValue[1]);
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
    	System.out.println(i+" loaded relationships, added "+j+" synonyms. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
		
	}
	
	private static void loadTaxonNames() throws Exception {
		TabReader tr = new TabReader("/data/taxonNames.txt");
    	String[] record = null;
    	TaxonConceptDao tcDao = new TaxonConceptDao();
    	int i = 0;
    	int j = 0;
    	while((record = tr.readNext())!=null){
//    		if(++i % 1000==0) 
//    			System.out.println(i+" names processed");
    		
    		if(record.length!=8){
    			System.out.println("truncated record: "+record);
    			continue;
    		}    		
    		
    		List<TaxonConcept> tcs = tcDao.getByNameGuid(record[0], 100);
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
    	System.out.println("Finished. "+j+" names added to concept records.");
	}

	private static void loadTaxonConcepts() throws Exception {
		
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
	    			
	    			boolean isVernacular = tcDao.isVernacularConcept(record[0]);
	    			if(!isVernacular){
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
	    			System.err.println(i+" - missing fields: "+record.length+" record:"+record);
	    		}
	    	}
	    	
	    	//add the remainder
			tcDao.create(tcBatch);
			tcBatch.clear();
	    	
	    	long finish = System.currentTimeMillis();
	    	System.out.println(i+" lines read, "+j+" loaded taxon concepts in: "+(((finish-start)/1000)/60)+" minutes.");
    	} catch (Exception e){
    		System.err.println(i+" error on line");
    		e.printStackTrace();
    	}
	}
	
	private static void loadVernacularConcepts() throws Exception {
		
		TabReader tr = new TabReader("/data/taxonConcepts.txt");
		TaxonConceptDao tcDao = new TaxonConceptDao();
    	String[] record = null;
    	long start = System.currentTimeMillis();
    	int i=0;
    	try {
	    	while((record = tr.readNext())!=null){
	    		i++;
	    		if(record.length==9){
	    			
	    			boolean isVernacular = tcDao.isVernacularConcept(record[0]);
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
		    			
		    			List<String> guids = tcDao.getIsVernacularConceptFor(record[0]);
		    			for(String guid: guids){
//		    				System.out.println("adding common name to: "+guid+", name:"+cn.nameString);
		    				tcDao.addCommonName(guid, cn);
		    			}
	    			}
	    		} else {
	    			System.err.println(i+" - missing fields: "+record.length+" record:"+record);
	    		}
	    	}
	    	
	    	long finish = System.currentTimeMillis();
	    	System.out.println("loaded taxon concepts in: "+(((finish-start)/1000)/60)+" minutes.");
    	} catch (Exception e){
    		System.err.println(i+" error on line");
    		e.printStackTrace();
    	}
	}	
}
