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
package org.ala.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.ala.lucene.LuceneUtils;
import org.ala.model.TaxonConcept;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
/**
 * This class provides utilities for loading data from source files.
 * This includes creating temporary lucene indexes for loading
 * purposes, and then lookups against these indexes.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class LoadUtils {

	public static final String BIE_STAGING_DIR = "/data/bie-staging/";
	public static final String BASE_DIR = "/data/lucene/loading/";
	public static final String TC_INDEX_DIR = BASE_DIR+"taxonConcept";
	public static final String REL_INDEX_DIR = BASE_DIR+"relationship";
	public static final String ACC_INDEX_DIR = BASE_DIR+"accepted";
	static Pattern p = Pattern.compile("\t");
	
	protected IndexSearcher tcIdxSearcher;
	protected IndexSearcher relIdxSearcher;
	protected IndexSearcher accIdxSearcher;
	
	public LoadUtils() throws Exception {
		//FIXME move to dependency injection
		this.tcIdxSearcher = new IndexSearcher(TC_INDEX_DIR);
		this.relIdxSearcher = new IndexSearcher(REL_INDEX_DIR);
		this.accIdxSearcher = new IndexSearcher(ACC_INDEX_DIR);
	}
	
	/**
	 * Retrieve a list of concepts associated with this name guid.
	 *  
	 * @param nameGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getByNameGuid(String nameGuid, int limit) throws Exception {
		return searchTaxonConceptIndexBy("nameGuid", nameGuid, limit); 
	}	
	
	/**
	 * Retrieve a list of concepts associated with this name guid.
	 *  
	 * @param nameGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public TaxonConcept getByGuid(String guid, int limit) throws Exception {
		List<TaxonConcept> tcs =  searchTaxonConceptIndexBy("guid", guid, limit);
		if(tcs.isEmpty())
			return null;
		return tcs.get(0);
	}		
	
	/**
	 * Search the index with the supplied value targeting a specific column.
	 * 
	 * @param columnName
	 * @param value
	 * @param limit
	 * @return
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	private List<TaxonConcept> searchTaxonConceptIndexBy(String columnName, String value, int limit)
			throws Exception {
		Query query = new TermQuery(new Term(columnName, value));
		TopDocs topDocs = tcIdxSearcher.search(query, limit);
		List<TaxonConcept> tcs = new ArrayList<TaxonConcept>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = tcIdxSearcher.doc(scoreDoc.doc);
			tcs.add(createTaxonConceptFromIndex(doc));
		}	
		return tcs;
	}
	
	/**
	 * Populate a TaxonConcept from the data in the lucene index.
	 * 
	 * @param doc
	 * @return
	 */
	private TaxonConcept createTaxonConceptFromIndex(Document doc) {
		TaxonConcept taxonConcept = new TaxonConcept();
		taxonConcept.guid = doc.get("guid");
		taxonConcept.parentGuid = doc.get("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf");
		taxonConcept.nameString = doc.get(LuceneUtils.SCI_NAME_RAW);
		
		Field[] fields = doc.getFields("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsParentTaxonOf");
		taxonConcept.hasChildren = (fields==null || fields.length==0) ? false : true;
		
		return taxonConcept;
	}
	
	/**
	 * Is this concept a vernacular concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public boolean isVernacularConcept(String guid) throws Exception {
		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", guid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 1);
		return topDocs.scoreDocs.length>0;
	}
	
	/**
	 * Retrieve the taxon concepts for which the supplied taxon concept guid is a vernacular concept.
	 * 
	 * @param vernacularGuid
	 * @return
	 * @throws Exception
	 */
	public List<String> getIsVernacularConceptFor(String vernacularGuid) throws Exception {

		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", vernacularGuid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 20);
		List<String> guids = new ArrayList<String>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = relIdxSearcher.doc(scoreDoc.doc);
			guids.add(doc.get("fromTaxon"));
		}
		return guids;
	}

	/**
	 * Is this concept an accepted concept? If so return the authority that
	 * that says so. e.g. "AFD", "APC"
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public String isAcceptedConcept(String guid) throws Exception {
		TermQuery guidQuery = new TermQuery(new Term("guid", guid));
//		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo"));
//		BooleanQuery query = new BooleanQuery();
//		query.add(toTaxonQuery, Occur.MUST);
//		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = accIdxSearcher.search(guidQuery, 1);
		if(topDocs.scoreDocs.length>0){
			Document doc = accIdxSearcher.doc(topDocs.scoreDocs[0].doc);
			return doc.get("authority");
		} else {
			return null;
		}
//		
//		return topDocs.scoreDocs[0];
	}
	
	/**
	 * Is this concept congruent to another and the "toTaxon" in the relationship
	 * as supplied by ANBG?
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public boolean isCongruentConcept(String guid) throws Exception {
		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", guid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 1);
		return topDocs.scoreDocs.length>0;
	}
	

	public boolean isSynonymFor(String guid) throws Exception {
		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", guid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 1);
		return topDocs.scoreDocs.length>0;
	}
	
	/**
	 * Load the accepted concepts 
	 * 
	 * @throws Exception
	 */
	public void loadAccepted() throws Exception {

		File file = new File(ACC_INDEX_DIR);
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
	    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/acceptedConcepts.txt");
	    	String[] keyValue = null;
	    	
	    	while((keyValue=tr.readNext())!=null){
	    		if(keyValue.length==2){
	    			i++;
			    	Document doc = new Document();
			    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
			    	doc.add(new Field("authority", keyValue[1], Store.YES, Index.ANALYZED));
			    	iw.addDocument(doc);
	    		}
			}
	    	tr.close();
			long finish = System.currentTimeMillis();
	    	System.out.println(i+" loaded accepted guids, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
		} finally {
			iw.close();
		}
	}

	/**
	 * Create the relationships index.
	 * 
	 * @throws Exception
	 */
	public void loadRelationships() throws Exception {

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
	    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/relationships.txt");
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
	public void loadTaxonConcepts() throws Exception {
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
//    	IndexSearcher is = new IndexSearcher(REL_INDEX_DIR);
    	
    	int i = 0;
    	
    	//names files to index
    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/taxonConcepts.txt");
    	String[] keyValue = null;
    	while((keyValue=tr.readNext())!=null){
    		
    		if(keyValue.length>3){

    			Document doc = new Document();
		    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
		    	doc.add(new Field("nameGuid", keyValue[1], Store.YES, Index.ANALYZED));
		    	LuceneUtils.addScientificNameToIndex(doc, keyValue[2]);
		    	
		    	//add relationships between concepts
//		    	addRels(is,keyValue[0], doc);
		    	
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

//	/**
//	 * Add a relationship to document representing the concept.
//	 * 
//	 * @param is
//	 * @param guid
//	 * @param doc
//	 * @throws Exception
//	 */
//	private static void addRels(IndexSearcher is, String guid, Document doc) throws Exception {
//		Query q = new TermQuery(new Term("fromTaxon", guid));
//		TopDocs topDocs = is.search(q, 100);
//		
//		boolean hasParent = false;
//		
//		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
//			Document relDoc = is.doc(scoreDoc.doc);
//			Field toTaxon = relDoc.getField("toTaxon");
//			Field relType = relDoc.getField("relationship");
//			if(relType.stringValue().endsWith("IsChildTaxonOf")){
//				hasParent = true;
//			}
//			doc.add(new Field(relType.stringValue(),toTaxon.stringValue(),Store.YES,Index.ANALYZED));
//		}
//		
//		if(!hasParent){
//			//add a representative null value
//			doc.add(new Field("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf","NULL",Store.YES,Index.ANALYZED));
//		}
//	}
	
//	public static String stripQuotes(String field){
//		return field.substring(1, field.length()-1);
//	}
}
