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

import org.ala.dao.SolrUtils;
import org.ala.lucene.LuceneUtils;
import org.ala.model.Publication;
import org.ala.model.Reference;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * This class provides utilities for loading data from source files.
 * This includes creating temporary lucene indexes for loading
 * purposes, and then lookups against these indexes.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class LoadUtils {

	protected static Logger logger = Logger.getLogger(LoadUtils.class);
	
	public static final String BIE_STAGING_DIR = "/data/bie-staging/";
	public static final String BASE_DIR = "/data/lucene/loading/";
	public static final String TC_INDEX_DIR = BASE_DIR+"taxonConcept";
	public static final String TN_INDEX_DIR = BASE_DIR+"taxonName";
	public static final String REL_INDEX_DIR = BASE_DIR+"relationship";
	public static final String ACC_INDEX_DIR = BASE_DIR+"accepted";
	public static final String PUB_INDEX_DIR = BASE_DIR+"publication";
	public static final String REF_INDEX_DIR = BASE_DIR+"reference";
	public static final String ID_INDEX_DIR = "/data/lucene/alanames/id";
	public static final String ALA_NAMES_INDEX_DIR = "/data/lucene/alanames/tc";
	static Pattern p = Pattern.compile("\t");
	
	private IndexSearcher tcIdxSearcher;
	private IndexSearcher tnIdxSearcher;
	private IndexSearcher relIdxSearcher;
	private IndexSearcher accIdxSearcher;
	private IndexSearcher pubIdxSearcher;
	private IndexSearcher refIdxSearcher;
	private IndexSearcher idIdxSearcher;
	private IndexSearcher alaNameIdxSearcher;
	
	public LoadUtils() throws Exception {}
	
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
	
	public TaxonName getNameByGuid(String guid) throws Exception {
	    if(guid == null || guid =="")
	        return null;
	    Query query = new TermQuery(new Term("guid", guid));
	    TopDocs topDocs = getTnIdxSearcher().search(query, 1);
	    if(topDocs.totalHits>0){
	        return createTaxonNameFromIndex(getTnIdxSearcher().doc(topDocs.scoreDocs[0].doc));
	    }
	    else
	        return null;
	}

	/**
	 * Retrieve guids for concepts associated with this publication id.
	 *  
	 * @param publicationGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<String> getGuidsForPublicationGuid(String publicationGuid, int limit) throws Exception {
		
		Query query = new TermQuery(new Term("publishedInCitation", publicationGuid));
		TopDocs topDocs = getTcIdxSearcher().search(query, limit);
		List<String> guids = new ArrayList<String>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = getTcIdxSearcher().doc(scoreDoc.doc);
			guids.add(doc.get("guid"));
		}
		return guids;
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
	 * Retrieve the publication for this GUID.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public Publication getPublicationByGuid(String guid) throws Exception {
		if(guid==null)
			return null;
		Query query = new TermQuery(new Term("guid", guid));
		TopDocs topDocs = getPubIdxSearcher().search(query, 1);
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = getPubIdxSearcher().doc(scoreDoc.doc);
			Publication p = new Publication();
			p.setGuid(doc.get("guid"));
			p.setTitle(doc.get("title"));
			p.setAuthor(doc.get("authorship"));
			p.setDatePublished(doc.get("datePublished"));
			p.setPublicationType("publicationType");
			return p;
		}
		return null;
	}
	
	public Reference getReferenceByGuid(String guid) throws Exception {
	    if(guid == null)
	        return null;
	    Query query = new TermQuery(new Term("guid", guid));
        TopDocs topDocs = getRefIdxSearcher().search(query, 1);
        for(ScoreDoc scoreDoc: topDocs.scoreDocs){
            Document doc = getRefIdxSearcher().doc(scoreDoc.doc);
            Reference r = new Reference();
            r.setGuid(doc.get("guid"));
            r.setTitle(doc.get("title"));
            return r;
        }
	    
	    return null;
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
		TopDocs topDocs = getTcIdxSearcher().search(query, limit);
		List<TaxonConcept> tcs = new ArrayList<TaxonConcept>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = getTcIdxSearcher().doc(scoreDoc.doc);
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
		taxonConcept.setGuid(doc.get("guid"));
		taxonConcept.setParentGuid(doc.get("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf"));
		taxonConcept.setNameString(doc.get(LuceneUtils.SCI_NAME_RAW));
		taxonConcept.setPublishedIn(doc.get("publishedIn"));
		taxonConcept.setPublishedInCitation(doc.get("publishedInCitation"));
		return taxonConcept;
	}
	
	private TaxonName createTaxonNameFromIndex(Document doc){
	    TaxonName tn = new TaxonName();
	    tn.setGuid(doc.get("guid"));
	    tn.setAuthorship(doc.get("author"));
	    tn.setNameComplete(doc.get("name"));
	    tn.setNomenclaturalCode(doc.get("nomenCode"));
	    tn.setPublishedInCitation(doc.get("publishedInCitation"));
	    tn.setRankString(doc.get("rank"));
	    tn.setTypificationString(doc.get("typification"));
	    /*
	     * doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
                doc.add(new Field("name", keyValue[2], Store.YES, Index.NO));
                
                addToDocSafely(doc,"rank", keyValue[4], Store.YES, Index.NO);
                addToDocSafely(doc,"author", keyValue[5], Store.YES, Index.NO);
                addToDocSafely(doc,"year", keyValue[7], Store.YES, Index.NO);
                addToDocSafely(doc, "publishedInCitation", keyValue[8], Store.YES, Index.ANALYZED);
                addToDocSafely(doc,"microReference", keyValue[9], Store.YES, Index.NO);
                addToDocSafely(doc,"nomenCode", keyValue[10], Store.YES, Index.NO);
                addToDocSafely(doc,"typification", keyValue[19], Store.YES, Index.NO);
	     */
	    return tn;
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
		
		TopDocs topDocs = getRelIdxSearcher().search(query, 1);
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
		
		TopDocs topDocs = getRelIdxSearcher().search(query, 20);
		List<String> guids = new ArrayList<String>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = relIdxSearcher.doc(scoreDoc.doc);
			guids.add(doc.get("fromTaxon"));
		}
		return guids;
	}

	private IndexSearcher getRelIdxSearcher() throws Exception {
		//FIXME move to dependency injection
		if(this.relIdxSearcher==null){
			File file = new File(REL_INDEX_DIR);
			if (file.exists()) {
				Directory dir = FSDirectory.open(file); 
				this.relIdxSearcher = new IndexSearcher(dir);
			}
		}
		return this.relIdxSearcher;
	}
	
	private IndexSearcher getAccIdxSearcher() throws Exception {
		if(this.accIdxSearcher==null){
			File file = new File(ACC_INDEX_DIR);
			if (file.exists()) {
				Directory dir = FSDirectory.open(file); 
				this.accIdxSearcher = new IndexSearcher(dir);
			}
		}
		return this.accIdxSearcher;
	}	

	private IndexSearcher getTcIdxSearcher() throws Exception {
		if(this.tcIdxSearcher==null){
			File file = new File(TC_INDEX_DIR);
			if (file.exists()) {
				Directory dir = FSDirectory.open(file); 
				this.tcIdxSearcher = new IndexSearcher(dir);
			}
		}
		return this.tcIdxSearcher;
	}
	
	   private IndexSearcher getTnIdxSearcher() throws Exception {
	        if(this.tnIdxSearcher==null){
	            File file = new File(TN_INDEX_DIR);
	            if (file.exists()) {
	                Directory dir = FSDirectory.open(file); 
	                this.tnIdxSearcher = new IndexSearcher(dir);
	            }
	        }
	        return this.tnIdxSearcher;
	    }
	
	private IndexSearcher getPubIdxSearcher() throws Exception {
		if(this.pubIdxSearcher==null){
			File file = new File(PUB_INDEX_DIR);
			if (file.exists()) {
				Directory dir = FSDirectory.open(file); 
				this.pubIdxSearcher = new IndexSearcher(dir);
			}			
		}
		return this.pubIdxSearcher;
	}
	
    private IndexSearcher getRefIdxSearcher() throws Exception {
        if(this.refIdxSearcher==null){
            File file = new File(REF_INDEX_DIR);
            if (file.exists()) {
                Directory dir = FSDirectory.open(file); 
                this.refIdxSearcher = new IndexSearcher(dir);
            }           
        }
        return this.refIdxSearcher;
    }	
	
	private IndexSearcher getIdIdxSearcher() throws Exception {
	    if(this.idIdxSearcher==null){
            File file = new File(ID_INDEX_DIR);
            if (file.exists()) {
                Directory dir = FSDirectory.open(file); 
                this.idIdxSearcher = new IndexSearcher(dir);
            }           
        }
        return this.idIdxSearcher;
	}
	
    private IndexSearcher getAlaNameIdxSearcher() throws Exception {
        if (this.alaNameIdxSearcher == null) {
            File file = new File(ALA_NAMES_INDEX_DIR);
            if (file.exists()) {
                Directory dir = FSDirectory.open(file);
                this.alaNameIdxSearcher = new IndexSearcher(dir);
            }
        }
        return this.alaNameIdxSearcher;
    }

	/**
	 * Returns the preferred lsid for the supplied lsid
	 * 
	 * An lsid is preferred if it does not appear in the id index
	 * 
	 * @param lsid
	 * @return
	 */
	public String getPreferredGuid(String guid) throws Exception{
	    TermQuery tq = new TermQuery(new Term("guid",guid ));
	    TopDocs topDocs = getIdIdxSearcher().search(tq, 1);	    
        if(topDocs.scoreDocs.length>0){
            
            Document doc = idIdxSearcher.doc(topDocs.scoreDocs[0].doc);
            return doc.get("preferredGuid");
        } else {
            return guid;
        }
	}
	/**
	 * Returns the accepted concepts guid when the supplied guid is a synonym otherwise null
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public String getAlaAcceptedConcept(String guid) throws Exception{
	    TermQuery tq = new TermQuery(new Term("guid", guid));
	    TopDocs topDocs = getAlaNameIdxSearcher().search(tq, 1);
	    if(topDocs.scoreDocs.length>0){
            Document doc = alaNameIdxSearcher.doc(topDocs.scoreDocs[0].doc);
            return doc.get("acceptedGuid");
        } else {
            return null;
        }
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
		
		TopDocs topDocs = getAccIdxSearcher().search(guidQuery, 1);
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
		
		TopDocs topDocs = getRelIdxSearcher().search(query, 1);
		return topDocs.scoreDocs.length>0;
	}
	

	public boolean isSynonymFor(String guid) throws Exception {
		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", guid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = getRelIdxSearcher().search(query, 1);
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
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
    	IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig); 
    	iw.setMaxFieldLength(Integer.MAX_VALUE);    	
//    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		try {
	    	long start = System.currentTimeMillis();
	    	//add the relationships
	    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/acceptedConcepts.txt",false);
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
	    	logger.info(i+" loaded accepted guids, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
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
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
    	IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig); 
    	iw.setMaxFieldLength(Integer.MAX_VALUE);    			
//    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		try {
	    	long start = System.currentTimeMillis();
	    	//add the relationships
	    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/relationships.txt",false);
	    	String[] keyValue = null;
	    	
	    	while((keyValue=tr.readNext())!=null){
	    		if(keyValue.length==6 && keyValue[2] != null && keyValue[1] != null){
	    			i++;
			    	Document doc = new Document();
			    	doc.add(new Field("fromTaxon", keyValue[2], Store.YES, Index.ANALYZED));
			    	
			    	doc.add(new Field("toTaxon", keyValue[1], Store.YES, Index.ANALYZED));
			    	
			    	addToDocSafely(doc,"relationship", keyValue[3], Store.YES, Index.ANALYZED);
			    	addToDocSafely(doc,"description",keyValue[5], Store.YES, Index.ANALYZED);
			    	iw.addDocument(doc);
	    		}
	    		else{
	    		    //System.out.println(keyValue.length + " " + (keyValue[2] != null) + " " + (keyValue[1] != null));
	    		    logger.info("Unable to index: " + StringUtils.join(keyValue,","));
	    		}
			}
	    	tr.close();
			long finish = System.currentTimeMillis();
	    	logger.info(i+" loaded relationships, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
		} finally {
			iw.close();
		}
	}
	
	public void loadTaxonNames() throws Exception{
	    long start = System.currentTimeMillis();
        
        //create a name index
        File file = new File(TN_INDEX_DIR);
        if(file.exists()){
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
        IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig); 
        int i = 0;
        
        //names files to index
        TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/taxonNames.txt",false);
        String[] keyValue = null;
        while((keyValue=tr.readNext())!=null){
            
            if(keyValue.length>19 && keyValue[2] != null){

                Document doc = new Document();
                doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
                doc.add(new Field("name", keyValue[2], Store.YES, Index.NO));
                
                addToDocSafely(doc,"rank", keyValue[4], Store.YES, Index.NO);
                addToDocSafely(doc,"author", keyValue[5], Store.YES, Index.NO);
                addToDocSafely(doc,"year", keyValue[7], Store.YES, Index.NO);
                addToDocSafely(doc, "publishedInCitation", keyValue[8], Store.YES, Index.ANALYZED);
                addToDocSafely(doc,"microReference", keyValue[9], Store.YES, Index.NO);
                addToDocSafely(doc,"nomenCode", keyValue[10], Store.YES, Index.NO);
                addToDocSafely(doc,"typification", keyValue[19], Store.YES, Index.NO);
//              if(keyValue[6]!=null){
//                  doc.add(new Field("publishedIn", keyValue[6], Store.YES, Index.NO));
//              }
                //LuceneUtils.addScientificNameToIndex(doc, keyValue[3], keyValue[2]);
                
                //add relationships between concepts
//              addRels(is,keyValue[0], doc);
                
                //add to index
                iw.addDocument(doc, analyzer);
            }
            else{
                logger.info("Unable to index: " + StringUtils.join(keyValue,","));
            }
            i++;
        }
        tr.close();
        iw.close();
        
        long finish = System.currentTimeMillis();
        logger.info(i+" indexed taxon names in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");

	}
	private void addToDocSafely(Document doc, String field, String value, Store store, Index index){
	    if(StringUtils.isNotEmpty(value)){
	        doc.add(new Field(field, value, store, index));
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
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
    	IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig); 
    	iw.setMaxFieldLength(Integer.MAX_VALUE);    	    	
//    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
//    	IndexSearcher is = new IndexSearcher(REL_INDEX_DIR);
    	
    	int i = 0;
    	
    	//names files to index
    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/taxonConcepts.txt",false);
    	String[] keyValue = null;
    	while((keyValue=tr.readNext())!=null){
    		
    		if(keyValue.length>3){

    			Document doc = new Document();
		    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
		    	
		    	addToDocSafely(doc,"nameGuid", keyValue[4], Store.YES, Index.ANALYZED);
		    	
		    	
		    	if(keyValue[8]!=null){
		    		doc.add(new Field("publishedInCitation", keyValue[8], Store.YES, Index.ANALYZED));
		    	}
//		    	if(keyValue[6]!=null){
//		    		doc.add(new Field("publishedIn", keyValue[6], Store.YES, Index.NO));
//		    	}
		    	//LuceneUtils.addScientificNameToIndex(doc, keyValue[3], keyValue[2]);
		    	
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
    	logger.info(i+" indexed taxon concepts in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}
	
	public void loadReferences() throws Exception {
	    File file = new File(REF_INDEX_DIR);
        if(file.exists()){
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);
        int i=0;
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
        IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig);
        try {
            long start = System.currentTimeMillis();
            //add the relationships
            TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/references.txt",false);
            String[] keyValue = null;
            
            while((keyValue=tr.readNext())!=null){
                if(keyValue.length==4 && keyValue[2] != null){
                    i++;
                    Document doc = new Document();
                    doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
                    doc.add(new Field("title", keyValue[2], Store.YES, Index.NO));
                    if(keyValue[3] != null) doc.add(new Field("containedIn", keyValue[3], Store.YES, Index.NO));
                    
                    iw.addDocument(doc);
                }
                else{
                    //System.out.println(keyValue.length + " " + (keyValue[2] != null) + " " + (keyValue[1] != null));
                    logger.info("Unable to index: " + StringUtils.join(keyValue,","));
                }
            }
            tr.close();
            long finish = System.currentTimeMillis();
            logger.info(i+" loaded relationships, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
        } finally {
            iw.close();
        }
	}

	/**
	 * @throws Exception
	 */
	public void loadPublications() throws Exception {
		
		File file = new File(PUB_INDEX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	int i=0;
		KeywordAnalyzer analyzer = new KeywordAnalyzer();
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
    	IndexWriter iw = new IndexWriter(FSDirectory.open(file), indexWriterConfig); 
    	iw.setMaxFieldLength(Integer.MAX_VALUE);    			
//    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		try {
	    	long start = System.currentTimeMillis();
	    	//add the relationships
	    	TabReader tr = new TabReader(BIE_STAGING_DIR+"anbg/publications.txt",false);
	    	String[] keyValue = null;
	    	
	    	while((keyValue=tr.readNext())!=null){
	    		if(keyValue.length==10){
	    			i++;
			    	Document doc = new Document();
			    	doc.add(new Field("guid", keyValue[0], Store.YES, Index.ANALYZED));
			    	if(keyValue[2]!=null) doc.add(new Field("title", keyValue[1], Store.YES, Index.NO));
			    	//missing in new dumps as of 20111205
			    	//if(keyValue[2]!=null) doc.add(new Field("authorship", keyValue[2], Store.YES, Index.NO));
			    	if(keyValue[3]!=null) doc.add(new Field("year", keyValue[3], Store.YES, Index.NO));
			    	if(keyValue[4]!=null) doc.add(new Field("datePublished", keyValue[4], Store.YES, Index.NO));
			    	if(keyValue[5]!=null) doc.add(new Field("citation", keyValue[5], Store.YES, Index.NO));
			    	if(keyValue[6]!=null) doc.add(new Field("containedIn", keyValue[6], Store.YES, Index.ANALYZED));
			    	//missing in new dumps
			    	//if(keyValue[4]!=null) doc.add(new Field("publicationType", keyValue[4], Store.YES, Index.NO));
			    	iw.addDocument(doc);
	    		}
			}
	    	tr.close();
			long finish = System.currentTimeMillis();
	    	logger.info(i+" loaded relationships, Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
		} finally {
			iw.close();
		}
	}
}
