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
import org.gbif.ecat.parser.NameParser;

/**
 * Create a simple lucene index for Taxon Concept GUID lookups
 * to assist in the loading of HBase rows. 
 * 
 * These created indexes are for ANBG data loading purposes only.
 * 
 * @author Dave Martin
 */
public class CreateLoadingIndex {

	public static final String BASE_DIR = "/data/lucene/loading/";
	public static final String TC_INDEX_DIR = BASE_DIR+"taxonConcept";
	public static final String REL_INDEX_DIR = BASE_DIR+"relationship";
	static Pattern p = Pattern.compile("\t");
	
	/**
	 * Run this to create the index.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		loadRelationships();		
		loadTaxonConcepts();
	}

	/**
	 * Create the relationships index.
	 * 
	 * @throws Exception
	 */
	private static void loadRelationships() throws Exception {

		File file = new File(REL_INDEX_DIR);
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

	/**
	 * Create the taxon concept index.
	 * 
	 * @throws Exception
	 */
	private static void loadTaxonConcepts() throws Exception {
		long start = System.currentTimeMillis();
		
		//create a name index
    	File file = new File(TC_INDEX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	//Analyzer analyzer = new KeywordAnalyzer(); - works for exact matches
    	KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
    	IndexSearcher is = new IndexSearcher(REL_INDEX_DIR);
    	
    	int i = 0;
    	
    	NameParser nameParser = new NameParser();
    	
    	//names files to index
    	TabReader tr = new TabReader("/data/taxonConcepts.txt");
    	String[] keyValue = null;
    	while((keyValue=tr.readNext())!=null){
    		
    		if(keyValue.length>3){

    			Document doc = new Document();
		    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
		    	doc.add(new Field("tc:hasName", keyValue[1], Store.YES, Index.ANALYZED));
		    	LuceneUtils.addScientificNameToIndex(doc, keyValue[2]);
		    	
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

	/**
	 * Add a relationship to document representing the concept.
	 * 
	 * @param is
	 * @param guid
	 * @param doc
	 * @throws Exception
	 */
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
	
	public static String stripQuotes(String field){
		return field.substring(1, field.length()-1);
	}
}
