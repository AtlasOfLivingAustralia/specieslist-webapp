package org.ala.hbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Rank;

/**
 * Used to load in a RDF N3 data extract into the profiler. 
 * 
 * @author Dave Martin
 */
public class N3DataLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
    	FileReader fr = new FileReader(new File("/data/dbpedia.txt"));
    	N3DataLoader loader = new N3DataLoader();
    	loader.load(fr);
	}

	/**
	 * Reads a sorted tab delimited source line by line using subject to determine
	 * a concept change.
	 * 
	 * @param reader
	 * @throws Exception
	 */
	public void load(Reader reader) throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDao();
		Pattern p = Pattern.compile("\t");    	
    	BufferedReader br = new BufferedReader(reader);
    	String record = null;
    	long start = System.currentTimeMillis();
    	int i=0;
    	int successfulSync = 0;
    	int failedSync = 0;
    	
    	try {
    		String genus = null;
    		String currentSubject = null;
			Rank currentLowestRank = null;
			String scientificName = null;
    		List<String[]> triples = new ArrayList<String[]>();
    		int documentId = 0; //fake doc id
	    	while((record = br.readLine())!=null){
	    		
	    		//split into subject, predicate, object ignoring any extra columns
	    		String[] triple = p.split(record);
	    		if(triple.length>=3)
	    			continue;
	    		
	    		String subject = triple[0];
	    		if(currentSubject==null){
	    			currentSubject = subject;
	    			triples.add(triple);
	    		} else if(subject.equals(currentSubject)){
	    			triples.add(triple);
	    		} else {
	    			//sync to hbase
	    			//boolean success = syncTriples(table, Integer.toString(documentId), triples, scientificName, currentLowestRank);
	    			boolean success = syncTriples(tcDao, Integer.toString(documentId), triples, genus, scientificName, currentLowestRank);
	    			if(success) 
	    				successfulSync++; 
	    			else 
	    				failedSync++;
	    			
	    			triples = new ArrayList<String[]>();
	    			triples.add(triple);
					scientificName = null;
					genus = null;
					currentLowestRank = null;
					currentSubject = subject;
					documentId++;
	    		}
	    		
	    		String predicate = triple[1];
	    		String object = triple[2];
	    		
				//if this is a taxonomic field, it may tell us if we what rank we have 
				if(predicate.startsWith("has")){
					predicate = predicate.substring(3);
				}
				Rank rank = Rank.getForName(predicate.toLowerCase());
				//is current line indicating species page ?
				if(rank!=null){
					if(currentLowestRank==null){
						currentLowestRank = rank;
						scientificName = object;
					} else if(rank.getId()>currentLowestRank.getId()){
						currentLowestRank = rank;
						scientificName = object;
					}
				}
				
				//scientificName wont be matched by Rank.getForName
				if("scientificName".equalsIgnoreCase(predicate)){
					scientificName = object;
				}
				
				if("genus".equalsIgnoreCase(predicate)){
					genus = object;
				}				
	    	}
	    	
	    	if(!triples.isEmpty()){
				boolean success = syncTriples(tcDao, Integer.toString(documentId), triples, genus, scientificName, currentLowestRank);
				if(success) 
					successfulSync++; 
				else 
					failedSync++;	    	
	    	}
	    	
	    	long finish = System.currentTimeMillis();
	    	System.out.println("loaded dbpedia data in: "+(((finish-start)/1000)/60)+" minutes.");
	    	System.out.println("sync'd: "+successfulSync+", failed:" +failedSync);
	    	
    	} catch (Exception e){
    		System.err.println(i+" error on line");
    		e.printStackTrace();
    	}
	}

	/**
	 * Sync these triples to HBase.
	 * 
	 * @param table
	 * @param triples
	 * @param scientificName
	 * @param currentLowestRank
	 */
	private static boolean syncTriples(TaxonConceptDao tcDao, String documentId, List<String[]> triples,
			String genus, String scientificName, org.ala.model.Rank currentLowestRank) throws Exception {
		
		if(triples.isEmpty() || scientificName==null)
			return false;

		scientificName = scientificName.replaceAll("\"", "");
		
		//find a concept to add the triples to
		String id = tcDao.findConceptIDForName(genus, scientificName);
		
		if(id==null){
//			System.err.println("Unable to match name: "+scientificName);
			return false;
		}
		
		System.out.println("Adding info for: "+scientificName+", guid: "+id+", subject:"+triples.get(0)[0]);
		
		Map<String,Object> kvs = new HashMap<String,Object>();
		
		//add properties to existing taxon concept entry
		String guid = triples.get(0)[0];
		
		kvs.put("URI", guid);
		kvs.put("source", "Wikipedia");
		kvs.put("licence", "Creative Commons");
		
		for(String[] triple: triples){
			//FIXME NOT HANDLING REPEATED PREDICATES
			kvs.put(triple[1], triple[2]);
		}
		
		tcDao.addLiteralValues(guid, "dbpedia", documentId, kvs);
		return true;
	}
}
