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
package org.ala.hbase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Classification;
import org.ala.model.Rank;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Reads a Darwin Core extracted which contains a classification for a taxon,
 * and add this classification to the taxon profile.
 * 
 * Loads the concepts, synonyms, parent and child taxa references.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("checklistBankLoader")
public class ChecklistBankLoader {
	
    protected static Logger logger = Logger.getLogger(ChecklistBankLoader.class);
    
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	private static final String IDENTIFIERS_FILE="/data/bie-staging/checklistbank/cb_identifiers.txt";
    
	private static final String CB_EXPORT_DIR="/data/bie-staging/checklistbank/";
	
	private static final String CB_LOADING_IDX_DIR= "/data/lucene/checklistbankloading";
	
	protected IndexSearcher tcIdxSearcher;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = SpringUtils.getContext();
		ChecklistBankLoader l = context.getBean(ChecklistBankLoader.class);
		long start = System.currentTimeMillis();
		
		logger.info("Creating checklist bank loading index....");
		l.createLoadingIndex();
		
		logger.info("Initialise indexes....");
		l.initIndexes();
		
		logger.info("Loading concepts....");
		l.loadConcepts();
		
		logger.info("Loading synonyms....");
		l.loadSynonyms();
		
		logger.info("Loading identifiers....");
		l.loadIdentifiers();
		
		long finish = System.currentTimeMillis();
		
		logger.info("Finished loading checklistbank data. Time taken: "+((finish-start)/60000)+" minutes");
		
		System.exit(0);
	}
	
	/**
	 * Create a loading index for checklist bank data.
	 * 
	 * @throws Exception
	 */
	public void createLoadingIndex() throws Exception {
		long start = System.currentTimeMillis();
		
		//create a name index
    	File file = new File(CB_LOADING_IDX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
    	
    	int i = 0;
    	
    	//names files to index
    	TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_name_usages.txt", false);
    	String[] cols = null;
    	while((cols=tr.readNext())!=null){
    		
			Document doc = new Document();
	    	doc.add(new Field("id", cols[0], Store.YES, Index.ANALYZED));
	    	if(cols[1]!=null){
	    		doc.add(new Field("parentId", cols[1], Store.YES, Index.ANALYZED));
	    	}
	    	if(cols[2]!=null){
	    		doc.add(new Field("guid", cols[2], Store.YES, Index.NOT_ANALYZED));
	    	} else {
	    		doc.add(new Field("guid", cols[0], Store.YES, Index.NOT_ANALYZED));
	    	}
	    	doc.add(new Field("nameString", cols[6], Store.YES, Index.ANALYZED));
	    	
	    	//add to index
	    	iw.addDocument(doc, analyzer);
	    	i++;
	    	
	    	if(i%10000==0) {
	    		iw.flush();
	    		System.out.println(i+"\t"+cols[0]+"\t"+cols[2]);
	    	}
		}
    	
    	//close taxonConcept stream
    	tr.close();
    	iw.close();
    	
    	long finish = System.currentTimeMillis();
    	logger.info(i+" indexed taxon concepts in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}
	
	public void initIndexes() throws Exception {
		this.tcIdxSearcher = new IndexSearcher(CB_LOADING_IDX_DIR);
	}
	
	/**
	 * Retrieve by id
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public TaxonConcept getById(String id) throws Exception {
		List<TaxonConcept> tc = searchTaxonConceptIndexBy("id", id, 1);
		if(tc.isEmpty()){
			return null;
		} else {
			return tc.get(0);
		}
	}
	
	/**
	 * Retrieve the child concepts for this parent id.
	 * 
	 * @param parentId
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getChildConcepts(String parentId) throws Exception {
		return searchTaxonConceptIndexBy("parentId", parentId, 10000);
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
	private List<TaxonConcept> searchTaxonConceptIndexBy(String columnName, String value, int limit) throws Exception {
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
		taxonConcept.setId(Integer.parseInt(doc.get("id")));
		taxonConcept.setGuid(doc.get("guid"));
		taxonConcept.setParentId(doc.get("parentId"));
		taxonConcept.setNameString(doc.get("nameString"));
		return taxonConcept;
	}	
	
	/**
	 * Load the accepted concepts in the DwC Archive
	 * 
	 * @throws IOException
	 * @throws UnsupportedArchiveException
	 * @throws Exception
	 */
	public void loadConcepts() throws IOException, UnsupportedArchiveException, Exception {
		
		Archive archive = ArchiveFactory.openArchive(new File(CB_EXPORT_DIR),true);
		Iterator<DarwinCoreRecord> iter = archive.iteratorDwc();
		int numberRead = 0;
		int numberAdded = 0;
		long start = System.currentTimeMillis();
		
		while (iter.hasNext()) {
			numberRead++;
			DarwinCoreRecord dwc = iter.next();
			String guid = dwc.getTaxonID();
			String identifier = dwc.getIdentifier();
			if(guid == null){
				guid = identifier;
			}
			
			if (guid != null && StringUtils.isEmpty(dwc.getAcceptedNameUsageID())) {
				
				//add the base concept
				TaxonConcept tc = new TaxonConcept();
				tc.setId(Integer.parseInt(identifier));
				tc.setGuid(guid);
				tc.setParentId(dwc.getParentNameUsageID());
				tc.setNameString(dwc.getScientificName());
				tc.setAuthor(dwc.getScientificNameAuthorship());
				tc.setRankString(dwc.getTaxonRank());
				
				if (taxonConceptDao.create(tc)) {
					numberAdded++;
//					if(numberAdded>500000) System.exit(0);
					if(numberAdded % 1000 == 0){
						long current = System.currentTimeMillis();
						logger.info("Taxon concepts added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record, last guid: "+ tc.getGuid());
					}
				}
				
				//load the parent concept
				if(tc.getParentId()!=null){
					TaxonConcept parentConcept = getById(tc.getParentId());
					taxonConceptDao.addParentTaxon(tc.getGuid(), parentConcept);
				}
				
				//load the child concepts
				List<TaxonConcept> childConcepts = getChildConcepts(Integer.toString(tc.getId()));
				if(!childConcepts.isEmpty()){
					for(TaxonConcept childConcept: childConcepts){
						taxonConceptDao.addChildTaxon(tc.getGuid(), childConcept);
					}
				}
				
				//add the classification
				Classification c = new Classification();
				c.setGuid(dwc.getTaxonID());
				c.setScientificName(dwc.getScientificName());
				c.setRank(dwc.getTaxonRank());
                c.setSpecies(dwc.getSpecificEpithet());
                c.setGenus(dwc.getGenus());
                c.setFamily(dwc.getFamily());
                c.setOrder(dwc.getOrder());
                c.setClazz(dwc.getClasss());
                c.setPhylum(dwc.getPhylum());
                c.setKingdom(dwc.getKingdom());
                try {
                    // Attempt to set the rank Id via Rank enum
                    c.setRankId(Rank.getForName(dwc.getTaxonRank()).getId());
                } catch (Exception e) {
                    logger.warn("Could not set rankId for: "+dwc.getTaxonRank()+" in "+guid);
                }
				taxonConceptDao.addClassification(guid, c);
			}
		}
		logger.info(numberAdded + " concepts added from " + numberRead + " rows of Checklist Bank data.");
	}
		
	/**
	 * Load the synonyms in the DwC Archive
	 * 
	 * @throws IOException
	 * @throws UnsupportedArchiveException
	 * @throws Exception
	 */
	public void loadSynonyms() throws IOException, UnsupportedArchiveException, Exception {
		Archive archive = ArchiveFactory.openArchive(new File(CB_EXPORT_DIR),true);
		Iterator<DarwinCoreRecord> iter = archive.iteratorDwc();
		int numberRead = 0;
		int numberAdded = 0;
		long start = System.currentTimeMillis();
		
		while (iter.hasNext()) {
			numberRead++;
			DarwinCoreRecord dwc = iter.next();
			String guid = dwc.getTaxonID();
			String identifier = dwc.getIdentifier();
			if(guid == null){
				guid = identifier;
			}
			
			if (guid != null && StringUtils.isNotEmpty(dwc.getAcceptedNameUsageID())) {
				
				//add the base concept
				TaxonConcept tc = new TaxonConcept();
				tc.setId(Integer.parseInt(identifier));
				tc.setGuid(guid);
				tc.setParentId(dwc.getParentNameUsageID());
				tc.setNameString(dwc.getScientificName());
				tc.setAuthor(dwc.getScientificNameAuthorship());
				tc.setRankString(dwc.getTaxonRank());
				
				String acceptedGuid = dwc.getAcceptedNameUsageID();
				
				//FIXME get the publication information
				
				
				
				
				if (taxonConceptDao.addSynonym(acceptedGuid, tc)) {
					numberAdded++;
					if(numberAdded % 1000 == 0){
						long current = System.currentTimeMillis();
						logger.info("Synonyms added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record" );
					}
				}
			}
		}
		logger.info(numberAdded + " synonyms added from " + numberRead + " rows of Checklist Bank data.");
	}

	/**
	 * Load the alternative identifiers for these concepts.
	 * 
	 * @throws Exception
	 */
	private void loadIdentifiers() throws Exception {
		
		//read the identifiers file
		CSVReader reader = new CSVReader(new FileReader(IDENTIFIERS_FILE),'\t', '\n');
		String[] line = null;
		int numberRead = 0;
		int numberAdded = 0;
		long start = System.currentTimeMillis();
		while((line = reader.readNext())!=null){
			numberRead++;
			if(line[1]!=null && line[2]!=null){
				//add this guid somewhere
				if(taxonConceptDao.addIdentifier(line[1], line[2])){
					numberAdded++;
					if(numberAdded % 1000 == 0){
						long current = System.currentTimeMillis();
						logger.info("Number added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record" );
					}
				}
			}
		}
		logger.info(numberAdded + " identifiers added from " + numberRead + " rows of Checklist Bank data.");
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
