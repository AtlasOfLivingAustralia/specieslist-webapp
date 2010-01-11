package org.ala.lucene;

import java.io.File;
import java.util.regex.Pattern;

import org.ala.util.TabReader;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;

/**
 * Create a simple lucene index for Taxon Concept GUID lookups
 * 
 * TODO Currently creating from files - switch to using HBase to derive the index.
 * 
 * @author Dave Martin
 */
public class CreateNameIndex {

	public static final String TC_INDEX_LOCATION = "/data/lucene/taxonConcept";
	public static final String REL_INDEX_LOCATION = "/data/lucene/relationship";
	
	static Pattern p = Pattern.compile("\t");
	
	public static void main(String[] args) throws Exception {
		loadRelationships();		
		loadTaxonConcepts();
	}

	private static void loadRelationships() throws Exception {

		File file = new File(REL_INDEX_LOCATION);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	int i=0;
		KeywordAnalyzer analyzer = new KeywordAnalyzer();
    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		try {
	    	long start = System.currentTimeMillis();
	    	//add the relationships
	    	TabReader tr = new TabReader("/data/relationships.txt");
	    	String[] keyValue = null;
	    	
	    	while((keyValue=tr.readNext())!=null){
	    		if(keyValue.length==3){
	    			i++;
			    	Document doc = new Document();
			    	doc.add(new Field("fromTaxon", keyValue[0], Store.YES, Index.ANALYZED));
			    	doc.add(new Field("toTaxon", keyValue[1], Store.YES, Index.ANALYZED));
			    	doc.add(new Field("relationship", keyValue[2], Store.YES, Index.ANALYZED));
			    	iw.addDocument(doc);
	    		}
			}
	    	tr.close();
			long finish = System.currentTimeMillis();
	    	System.out.println(i+" loaded relationships, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
		} finally {
			iw.close();
		}
	}	    		

	private static void loadTaxonConcepts() throws Exception {
		long start = System.currentTimeMillis();
		
		//create a name index
    	File file = new File(TC_INDEX_LOCATION);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	//Analyzer analyzer = new KeywordAnalyzer(); - works for exact matches
    	KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
    	IndexSearcher is = new IndexSearcher(REL_INDEX_LOCATION);
    	
    	int i = 0;
    	
    	NameParser nameParser = new NameParser();
    	
    	//names files to index
    	TabReader tr = new TabReader("/data/taxonConcepts.txt");
    	String[] keyValue = null;
    	while((keyValue=tr.readNext())!=null){
    		
    		if(keyValue.length>3){
    			
    			//remove the subgenus
    			String normalized = "";
    			
    			if(keyValue[2]!=null){
    				normalized = keyValue[2].replaceFirst("\\([A-Za-z]{1,}\\) ", "");
    			}
    			ParsedName parsedName = nameParser.parseIgnoreAuthors(normalized);
		    	Document doc = new Document();
		    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
		    	doc.add(new Field("tc:hasName", keyValue[1], Store.YES, Index.ANALYZED));
		    	if(parsedName!=null){
		    		if(parsedName.isBinomial()){
		    			//add multiple versions
		    			doc.add(new Field("scientificName", parsedName.buildAbbreviatedCanonicalName().toLowerCase(), Store.YES, Index.ANALYZED));
		    			doc.add(new Field("scientificName", parsedName.buildAbbreviatedFullName().toLowerCase(), Store.YES, Index.ANALYZED));
		    		}
		    		//add lowercased version
		    		doc.add(new Field("scientificName", parsedName.buildCanonicalName().toLowerCase(), Store.YES, Index.ANALYZED));
		    	} else {
		    		//add lowercased version if name parser failed			    		
			    	doc.add(new Field("scientificName", normalized.toLowerCase(), Store.YES, Index.ANALYZED));
		    	}
		    	
		    	if(keyValue[2]!=null){
		    		doc.add(new Field("scientificNameRaw", keyValue[2], Store.YES, Index.NO));
		    	}
		    	
		    	//add relationships between concepts
		    	addRels(is,keyValue[0], doc);
		    	
		    	//add to index
		    	iw.addDocument(doc, analyzer);
			}
	    	i++;
		}
    	
    	//close taxonConcept stream
    	tr.close();
    	iw.close();
    	
    	long finish = System.currentTimeMillis();
    	System.out.println(i+" indexed taxon concepts in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}

	public static String stripQuotes(String field){
		return field.substring(1, field.length()-1);
	}
	
	private static void addRels(IndexSearcher is, String guid, Document doc) throws Exception {
		Query q = new TermQuery(new Term("fromTaxon", guid));
		TopDocs topDocs = is.search(q, 100);
		
		boolean hasParent = false;
		
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document relDoc = is.doc(scoreDoc.doc);
			Field toTaxon = relDoc.getField("toTaxon");
			Field relType = relDoc.getField("relationship");
			if(relType.stringValue().endsWith("IsChildTaxonOf")){
				hasParent = true;
			}
			doc.add(new Field(relType.stringValue(),toTaxon.stringValue(),Store.YES,Index.ANALYZED));
		}
		
		if(!hasParent){
			//add a representative null value
			doc.add(new Field("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf","NULL",Store.YES,Index.ANALYZED));
		}
	}	
	
//	private static Document findDoc(IndexSearcher is, String guid) throws Exception {
//		Query q = new TermQuery(new Term("guid", guid));
//		TopDocs topDocs = is.search(q, 1);
//		if(topDocs.scoreDocs.length>0)
//			return is.doc(topDocs.scoreDocs[0].doc);
//		return null;
//	}
}
