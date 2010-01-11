package org.ala.hbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 * Load in the DBPedia data extract.
 * 
 * @author Dave Martin
 */
public class LoadDBPediaData {

	static final Pattern pipeDelimited = Pattern.compile(">>>");
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
    	HBaseConfiguration config = new HBaseConfiguration();
		Pattern p = Pattern.compile("\t");
//    	
    	HTable table = new HTable(config, "taxonConcept");
    	FileReader fr = new FileReader(new File("/data/dbpedia.txt"));
    	BufferedReader br = new BufferedReader(fr);
    	String record = null;
    	long start = System.currentTimeMillis();
    	int i=0;
    	int successfulSync = 0;
    	int failedSync = 0;
    	
    	try {
    		String currentSubject = null;
			Rank currentLowestRank = null;
			String scientificName = null;
    		List<String[]> triples = new ArrayList<String[]>();
    		int documentId = 0; //fake doc id
	    	while((record = br.readLine())!=null){
	    		
	    		//split into subject, predicate, object
	    		String[] triple = p.split(record);
	    		if(triple.length!=3)
	    			continue;
	    		
	    		String subject = triple[0];
	    		if(currentSubject==null){
	    			currentSubject = subject;
	    			triples.add(triple);
	    		} else if(subject.equals(currentSubject)){
	    			triples.add(triple);
	    		} else {
	    			//sync to hbase
	    			boolean success = syncTriples(table, Integer.toString(documentId), triples, scientificName, currentLowestRank);
	    			if(success) 
	    				successfulSync++; 
	    			else 
	    				failedSync++;
	    			
	    			triples = new ArrayList<String[]>();
	    			triples.add(triple);
					scientificName = null;
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
	    		
//	    		String[] taxonConcept = p.split(record);
//	    		i++;
//	    		if(i%10000==0){
//	    			System.out.println("count: "+i);
//	    		}
//	    		if(taxonConcept.length==6){
//		        	BatchUpdate batchUpdate = new BatchUpdate(taxonConcept[0]);
//		        	addFieldValue(batchUpdate, "tc:hasName", taxonConcept, 1);
//		        	addFieldValue(batchUpdate, "tc:hasAuthor", taxonConcept, 2);
//		        	addFieldValue(batchUpdate, "tc:hasAuthorYear", taxonConcept, 3);
//		        	addFieldValue(batchUpdate, "tc:publishedInCitation", taxonConcept, 4);
//		        	addFieldValue(batchUpdate, "tc:publishedIn", taxonConcept, 5);
//		        	table.commit(batchUpdate);
//	    		} else {
//	    			System.err.println(i+" - missing fields ");
//	    		}
	    	}
//	    	table.flushCommits();
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
	private static boolean syncTriples(HTable table, String documentId, List<String[]> triples,
			String scientificName, org.ala.hbase.Rank currentLowestRank) throws Exception {
		
		if(triples.isEmpty())
			return false;

		scientificName = scientificName.replaceAll("\"", "");
		
		//find a concept to add the triples to
		String id = findConceptIDForName(scientificName);
		
		if(id==null){
//			System.err.println("Unable to match name: "+scientificName);
			return false;
		}
		
		System.out.println("Adding info for: "+scientificName+", guid: "+id+", subject:"+triples.get(0)[0]);
		
		//add properties to existing taxon concept entry
		BatchUpdate batchUpdate = new BatchUpdate(id);
		
		String guid = triples.get(0)[0];
		batchUpdate.put("raw:html:"+documentId+":URI", Bytes.toBytes(guid));
		batchUpdate.put("raw:html:"+documentId+":source", Bytes.toBytes("Wikipedia"));
		batchUpdate.put("raw:html:"+documentId+":licence", Bytes.toBytes("Creative Commons"));
		
		for(String[] triple: triples){
			
			//raw:<doc-type>:<document-id>:<subject>
			//raw:<doc-type>:<document-id>:<subject>:<subject>  for nested triples ??
			
			// what about link childConcept + scientific name
			//tc:hasChildConcept:urn:lsid:afd.taxon:1213 -> Aus bus
			
			// what about link to georegion
			
			//tc:hasOccurredInRegion:urn:lsid:afd.taxon:1213 -> Aus bus
			
			//raw:<document-id>:hasCommonName -> Davus bus
			//raw:<document-id>:hasCommonName -> Davus cus

			// for core values, the deserialisation could be based on the
			// field being mapped to
			
			
			//need to check every "raw" value for serialised lists
			
			//could do...
			//raw:<doc-type>:<infosource-id>:<document-id>:<subject>
			//rendering could use a hashmap of infosources for rendering RDF/XML report
			
			//FIXME NOT HANDLING REPEATED PREDICATES
			batchUpdate.put("raw:html:"+documentId+":"+triple[1], Bytes.toBytes(triple[2]));
		}
		table.commit(batchUpdate);
		return true;
	}

	/**
	 * FIXME Move to a DAO
	 * 
	 * @param scientificName
	 * @return
	 * @throws Exception
	 */
	public static String findConceptIDForName(String scientificName) throws Exception {
		try {
			IndexSearcher is = new IndexSearcher("/data/lucene/taxonConcept");
			QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());			
			Query q = qp.parse("\""+scientificName+"\"");
			
			TopDocs topDocs = is.search(q, 20);
			
			for(ScoreDoc scoreDoc: topDocs.scoreDocs){
				Document doc = is.doc(scoreDoc.doc);
				Field hasSynonym = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym");
				if(hasSynonym!=null){
					System.out.println("Returning synonym");
					return hasSynonym.stringValue();
				}
				Field hasVernacular = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular");
				if(hasVernacular!=null){
					System.out.println("Returning vernacular");
					return hasVernacular.stringValue();
				}
				Field isCongruentTo = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo");
				if(isCongruentTo!=null){
					System.out.println("Returning congruent");
					return isCongruentTo.stringValue();
				}
//			System.out.println("Doc Id: "+scoreDoc.doc);
//			System.out.println("Guid: "+doc.getField("guid").stringValue());
//			System.out.println("Name: "+doc.getField("scientificName").stringValue());
//			System.out.println("Raw name: "+doc.getField("scientificNameRaw").stringValue());
//			System.out.println("#################################");
				return doc.getField("guid").stringValue();
			}
		} catch (Exception e) {
			System.err.println("Problem searching with:"+scientificName+" : "+e.getMessage());
		}		
		return null;
	}

	public static byte[] serialiseArray(String[] values){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<values.length; i++){
			if(i>0){
				sb.append(">>>");
			}			
			sb.append(values[i]);
		}
		return sb.toString().getBytes();
	}
}
