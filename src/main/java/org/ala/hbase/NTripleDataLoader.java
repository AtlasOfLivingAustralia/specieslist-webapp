package org.ala.hbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Triple;

/**
 * Used to load in a RDF N-Triple data extract into the profiler. 
 * 
 * FIXME Should make use of Sesame's openrdf n-triple reading API.
 * 
 * @author Dave Martin
 */
public class NTripleDataLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String defaultFilePath = "/data/dbpedia.txt";
		String filePath = null;
		if(args.length==0){
			filePath = defaultFilePath;
		}
    	FileReader fr = new FileReader(new File(filePath));
    	NTripleDataLoader loader = new NTripleDataLoader();
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
    	int lineNumber = 0;
    	int successfulSync = 0;
    	int failedSync = 0;
    	
    	final String documentId = "0"; //fake doc id
    	final String infoSourceId = "0"; //fake info source id
    	
    	try {
    		String currentSubject = null;
    		List<Triple> triples = new ArrayList<Triple>();
    		
    		
    		
	    	while((record = br.readLine())!=null){
	    		
	    		//split into subject, predicate, object ignoring any extra columns
	    		String[] tripleAsArray = p.split(record);
	    		if(tripleAsArray.length>=3)
	    			continue;
	    		
	    		//create triple
	    		Triple triple = new Triple(tripleAsArray[0],tripleAsArray[1],tripleAsArray[2]);
	    		
	    		if(currentSubject==null){
	    			currentSubject = triple.subject;
	    			triples.add(triple);
	    		} else if(triple.subject.equals(currentSubject)){
	    			//if subject unchanged, add to list
	    			triples.add(triple);
	    		} else {
	    			//subject has changed - sync to hbase
	    			boolean success = tcDao.syncTriples(infoSourceId, documentId, triples, null);
	    			if(success) 
	    				successfulSync++; 
	    			else 
	    				failedSync++;
	    			
	    			triples = new ArrayList<Triple>();
	    			triples.add(triple);
					currentSubject = triple.subject;
	    		}
	    	}
	    	
	    	//sync the remaining batch
	    	if(!triples.isEmpty()){
				boolean success = tcDao.syncTriples(infoSourceId, documentId, triples, null);
				if(success) 
					successfulSync++; 
				else 
					failedSync++;
	    	}
	    	
	    	long finish = System.currentTimeMillis();
	    	System.out.println("Loaded dbpedia data in: "+(((finish-start)/1000)/60)+" minutes.");
	    	System.out.println("Sync'd: "+successfulSync+", Failed to sync:" +failedSync);
	    	
    	} catch (Exception e){
    		System.err.println(lineNumber+" error on line");
    		e.printStackTrace();
    	}
	}
}
