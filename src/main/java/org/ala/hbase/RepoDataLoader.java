package org.ala.hbase;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import org.ala.dao.NTriplesUtils;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Triple;
import org.apache.commons.io.FileUtils;

/**
 * Data Loader that scans through a BIE repository, find triples
 * and adds them to concepts held in the profiler. 
 * 
 * @author Dave Martin
 */
public class RepoDataLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		RepoDataLoader loader = new RepoDataLoader();
		long start = System.currentTimeMillis();
		int filesRead = loader.load("/data/bie");	
    	long finish = System.currentTimeMillis();
    	System.out.println(filesRead+" files scanned/loaded in: "+((finish-start)/60000)+" minutes "+((finish-start)/1000)+" seconds.");
	}

	/**
	 * Scan through the repository, retrieve triples and
	 * add to taxon concepts
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	private int load(String filePath) throws Exception {
		System.out.println("Scanning directory: "+filePath);
		
		//initialise DAO 
		TaxonConceptDao tcDao = new TaxonConceptDao();
		
		int filesRead = 0;
		
		//start scan
		File file = new File(filePath);
		Iterator<File> fileIterator = FileUtils.iterateFiles(file, null, true);
		while(fileIterator.hasNext()){
			File currentFile = fileIterator.next();
			if(currentFile.getName().equals("rdf")){
				filesRead++;
				List<Triple> triples = NTriplesUtils.readNTriples(new FileReader(currentFile), true);
				//sync these triples
//				/../../infosource-id/document-id div 1000/document-id/rdf
				String infosourceId = currentFile.getParentFile().getParentFile().getParentFile().getName();
				String documentId = currentFile.getParentFile().getName();
				tcDao.syncTriples(infosourceId, documentId, triples);
			}
		}
		return filesRead;
	}
}
