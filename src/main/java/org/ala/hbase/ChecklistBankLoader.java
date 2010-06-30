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
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.Rank;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
	
	//data files
	private static final String IDENTIFIERS_FILE="/data/bie-staging/checklistbank/cb_identifiers.txt";
	private static final String CB_EXPORT_DIR="/data/bie-staging/checklistbank/";
	private static final String AFD_COMMON_NAMES = "/data/bie-staging/anbg/AFD-common-names.csv";
	private static final String APNI_COMMON_NAMES = "/data/bie-staging/anbg/APNI-common-names.csv";
	
	//lucene indexes
	private static final String CB_LOADING_IDX_DIR= "/data/lucene/checklistbankloading/tc";
	private static final String CB_LOADING_ID_IDX_DIR= "/data/lucene/checklistbankloading/id";
	
	protected IndexSearcher tcIdxSearcher;
	protected IndexSearcher identifierIdxSearcher;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		ApplicationContext context = SpringUtils.getContext();
		ChecklistBankLoader l = context.getBean(ChecklistBankLoader.class);
		long start = System.currentTimeMillis();
		
		logger.info("Creating checklist bank loading index....");
		l.createLoadingIndex();
		l.createIdentifierIndex();
		
		logger.info("Initialise indexes....");
		l.initIndexes();
		
		logger.info("Loading concepts....");
		l.loadConcepts();
		
		logger.info("Loading synonyms....");
		l.loadSynonyms();
//		
		logger.info("Loading identifiers....");
		l.loadIdentifiers();
		
		logger.info("Loading afd common names....");
		l.loadCommonNames(AFD_COMMON_NAMES);
		
		logger.info("Loading apni common names....");
		l.loadCommonNames(APNI_COMMON_NAMES);
		
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
    	
    	String[] cols = tr.readNext(); //first line contains headers - ignore
    	
    	while((cols=tr.readNext())!=null){
    		
    		String identifier = cols[0];
    		String parentNameUsageID = cols[1];
			String guid = cols[2]; //TaxonID
    		
			Document doc = new Document();
	    	doc.add(new Field("id", cols[0], Store.YES, Index.ANALYZED));
	    	if(StringUtils.isNotEmpty(parentNameUsageID)){
	    		doc.add(new Field("parentId", parentNameUsageID, Store.YES, Index.ANALYZED));
	    	}
	    	if(StringUtils.isNotEmpty(guid)){
	    		doc.add(new Field("guid", guid, Store.YES, Index.NOT_ANALYZED));
	    	} else {
	    		doc.add(new Field("guid", identifier, Store.YES, Index.NOT_ANALYZED));
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
	
	/**
	 * Create a loading index for checklist bank data.
	 * 
	 * @throws Exception
	 */
	public void createIdentifierIndex() throws Exception {
		long start = System.currentTimeMillis();
		
		//create a name index
    	File file = new File(CB_LOADING_ID_IDX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
    	
    	int i = 0;
    	
    	//names files to index
    	TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_identifiers.txt", false);
    	
    	String[] cols = tr.readNext(); //first line contains headers - ignore
    	
    	while((cols=tr.readNext())!=null){
    		
    		if(StringUtils.isNotEmpty(cols[1])){
				Document doc = new Document();
		    	doc.add(new Field("guid", cols[2], Store.YES, Index.ANALYZED));
	    		doc.add(new Field("preferredGuid", cols[1], Store.YES, Index.NO));
		    	
		    	//add to index
		    	iw.addDocument(doc, analyzer);
		    	i++;
		    	
		    	if(i%10000==0) {
		    		iw.flush();
		    		System.out.println(i+"\t"+cols[0]+"\t"+cols[2]);
		    	}
    		}
		}
    	
    	//close taxonConcept stream
    	tr.close();
    	iw.close();
    	
    	long finish = System.currentTimeMillis();
    	logger.info(i+" indexed identifiers in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}
	
	/**
	 * Initialise indexes for lookups.
	 * 
	 * @throws Exception
	 */
	public void initIndexes() throws Exception {
		this.tcIdxSearcher = new IndexSearcher(CB_LOADING_IDX_DIR, true);
		this.identifierIdxSearcher = new IndexSearcher(CB_LOADING_ID_IDX_DIR, true);
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
	 * Load the concepts from the export file into the BIE.
	 * 
	 * @throws Exception
	 */
	public void loadConcepts() throws Exception {
		
		long start = System.currentTimeMillis();
		
    	//names files to index
    	TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_name_usages.txt", false);
    	
    	String[] cols = tr.readNext(); //first line contains headers - ignore
    	
    	while((cols=tr.readNext())!=null){
    		String identifier = cols[0];
    		String parentNameUsageID = cols[1];
			String guid = cols[2]; //TaxonID
			String nubId = cols[3];
			String acceptedNameUsageID = cols[4]; //acceptedNameUsageID
			String scientificNameId = cols[5];
			String scientificName = cols[6];
			String scientificNameAuthorship = cols[7];
			String rankID = cols[8];
			String taxonRank = cols[9];
			Integer left = NumberUtils.createInteger(cols[10]);
			Integer right = NumberUtils.createInteger(cols[11]);
			String kingdomID = cols[12];
			String kingdom = cols[13];
			String phylumID = cols[14];
			String phylum = cols[15];
			String clazzID = cols[16];
			String clazz = cols[17];
			String orderID = cols[18];
			String order = cols[19];
			String familyID = cols[20];
			String family = cols[21];
			String genusID = cols[22];
			String genus = cols[23];
			String speciesID = cols[24];
			String species = cols[25];
			String dataset = cols[26];

			if(guid == null){
				guid = identifier;
			}
			
			int numberAdded = 0;

			if (guid != null && StringUtils.isEmpty(acceptedNameUsageID)) {
				
				//add the base concept
				TaxonConcept tc = new TaxonConcept();
				tc.setId(Integer.parseInt(identifier));
				tc.setGuid(guid);
				tc.setParentId(parentNameUsageID);
				tc.setNameString(scientificName);
				tc.setAuthor(scientificNameAuthorship);
				tc.setRankString(taxonRank);
				tc.setLeft(left);
				tc.setRight(right);
				
				if (taxonConceptDao.create(tc)) {
					numberAdded++;
					if(numberAdded % 1000 == 0){
						long current = System.currentTimeMillis();
						logger.info("Taxon concepts added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record, last guid: "+ tc.getGuid());
					}
				}
				
				//load the parent concept
				if(StringUtils.isNotEmpty(tc.getParentId())){
					TaxonConcept parentConcept = getById(tc.getParentId());
					if(parentConcept!=null){
						taxonConceptDao.addParentTaxon(tc.getGuid(), parentConcept);
					}
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
				c.setGuid(guid);
				c.setScientificName(scientificName);
				c.setRank(taxonRank);
                c.setSpecies(species);
                c.setSpeciesGuid(speciesID);
                c.setGenus(genus);
                c.setGenusGuid(genusID);
                c.setFamily(family);
                c.setFamilyGuid(familyID);
                c.setOrder(order);
                c.setOrderGuid(orderID);
                c.setClazz(clazz);
                c.setClazzGuid(clazzID);
                c.setPhylum(phylum);
                c.setPhylumGuid(phylumID);
                c.setKingdom(kingdom);
                c.setKingdomGuid(kingdomID);
                try {
                    // Attempt to set the rank Id via Rank enum
                    c.setRankId(Rank.getForName(taxonRank).getId());
                } catch (Exception e) {
                    logger.warn("Could not set rankId for: "+taxonRank+" in "+guid);
                }
				taxonConceptDao.addClassification(guid, c);
			}
		}
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
	 * Loads the common names in the supplied file.
	 * 
	 * @param dataFile
	 * @throws Exception
	 */
	private void loadCommonNames(String dataFile) throws Exception {
		logger.info("Starting to load common names from " + dataFile);
		
    	long start = System.currentTimeMillis();
    	
    	// add the taxon concept regions
        //NC A TabReader can not be used because quoted fields can contain a comma
    	//TabReader tr = new TabReader(dataFile, true, ',');
        CSVReader tr = new CSVReader(new FileReader(dataFile), ',', '"',1);
    	String[] values = null;
        Pattern p = Pattern.compile(",");
    	int namesAdded = 0;
		while ((values = tr.readNext()) != null) {
    		if (values.length > 5) {
    			String guid = values[1];
    			String commonNameString = values[2];
    			String taxonConceptGuid = values[5];
    			//retrieve the concept - this gets around the use of multiple guids for a single concept
    			//this is the case for APNI/APC concepts.
    			taxonConceptGuid = getPreferredGuid(taxonConceptGuid);
    			//do a look up for the correct taxon
    			CommonName commonName = new CommonName();
    			commonName.setGuid(guid);
                //the common name string can be a comma separated list of names
    			String[] commonNameStrings = p.split(commonNameString);
                for(String cn: commonNameStrings){
                    commonName.setNameString(cn);
                    boolean success = taxonConceptDao.addCommonName(taxonConceptGuid, commonName);
                    if(success) namesAdded++;
                    if(!success){
                    	logger.error("Unable to add "+commonName);
                    }
                }
    		}
		}
		
    	tr.close();
		long finish = System.currentTimeMillis();
		logger.info(namesAdded+" common names added to taxa. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
	}
	
	/**
	 * Retrieve the preferred guid for this taxon concept.
	 * 
	 * @param taxonConceptGuid
	 * @return
	 * @throws Exception
	 */
	private String getPreferredGuid(String taxonConceptGuid) throws Exception {
		
		Query query = new TermQuery(new Term("guid", taxonConceptGuid));
		TopDocs topDocs = identifierIdxSearcher.search(query, 1);
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = identifierIdxSearcher.doc(scoreDoc.doc);
			return doc.get("preferredGuid");
		}
		return taxonConceptGuid;
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
