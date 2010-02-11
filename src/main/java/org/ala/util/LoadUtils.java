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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ala.model.TaxonConcept;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
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

	protected IndexSearcher tcIdxSearcher;
	protected IndexSearcher relIdxSearcher;
	
	public LoadUtils() throws Exception {
		//FIXME move to dependency injection
		this.tcIdxSearcher = new IndexSearcher("/data/lucene/loading/taxonConcept");
		this.relIdxSearcher = new IndexSearcher("/data/lucene/loading/relationship");
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
		return searchTaxonConceptIndexBy("tc:hasName", nameGuid, limit); 
	}	
	
	/**
	 * Search the index with the supplied value targetting a specific column.
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
		taxonConcept.nameString = doc.get("scientificNameRaw");
		
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
}
