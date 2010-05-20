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
package org.ala.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.lucene.LuceneUtils;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.Image;
import org.ala.model.PestStatus;
import org.ala.model.Publication;
import org.ala.model.Rank;
import org.ala.model.Reference;
import org.ala.model.Region;
import org.ala.model.SimpleProperty;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
import org.ala.repository.Predicates;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.SearchResultException;
import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.util.RankType;
/**
 * HBase implementation if Taxon concept DAO.
 * 
 * @author Dave Martin
 */
//@Component("taxonConceptDao")
public class TaxonConceptDaoImpl implements TaxonConceptDao {
	
	protected static Logger logger = Logger.getLogger(TaxonConceptDaoImpl.class);
	
	/** The location for the lucene index */
	public static final String TC_INDEX_DIR = "/data/lucene/taxonConcept";

	/** HBase columns */
	private static final String IDENTIFIER_COL = "tc:sameAs";
	private static final String SYNONYM_COL = "tc:hasSynonym";
	private static final String IS_SYNONYM_FOR_COL = "tc:IsSynonymFor";
	private static final String IS_CONGRUENT_TO_COL = "tc:IsCongruentTo";
	private static final String VERNACULAR_COL = "tc:hasVernacularConcept";
	private static final String CONSERVATION_STATUS_COL = "tc:hasConservationStatus";
	private static final String PEST_STATUS_COL = "tc:hasPestStatus";
	private static final String REGION_COL = "tc:hasRegion";
	private static final String EXTANT_STATUS_COL = "tc:hasExtantStatus";
	private static final String HABITAT_COL = "tc:hasHabitat";
	private static final String IMAGE_COL = "tc:hasImage";
	private static final String IS_CHILD_COL_OF = "tc:IsChildTaxonOf";
	private static final String IS_PARENT_COL_OF = "tc:IsParentTaxonOf";
    private static final String TEXT_PROPERTY_COL = "tc:hasTextProperty";
    private static final String CLASSIFICATION_COL = "tc:hasClassification";
    private static final String REFERENCE_COL = "tc:hasReference";
    private static final String PUBLICATION_COL = "tc:hasPublication";

	private static final String TC_ACCEPTED_CONCEPT_GUID = "tc:acceptedConceptGuid";
	private static final String TC_AUTHOR = "tc:author";
	private static final String TC_AUTHOR_YEAR = "tc:authorYear";
	private static final String TC_HAS_NAME = "tc:hasName";
	private static final String TC_ID = "tc:id";
	private static final String TC_INFO_SOURCE_ID = "tc:infoSourceId";
	private static final String TC_INFO_SOURCE_NAME = "tc:infoSourceName";
	private static final String TC_INFO_SOURCE_URL = "tc:infoSourceURL";
	private static final String TC_NAME_STRING = "tc:nameString";
	private static final String TC_PARENT_GUID = "tc:parentGuid";
	private static final String TC_PARENT_ID = "tc:parentId";
	private static final String TC_PUBLISHED_IN = "tc:publishedIn";
	private static final String TC_PUBLISHED_IN_CITATION = "tc:publishedInCitation";
	private static final String TC_RANK_STRING = "tc:rankString";
	private static final String TN_AUTHORSHIP = "tn:authorship";
	private static final String TN_GUID = "tn:guid";
	private static final String TN_NAME_COMPLETE = "tn:nameComplete";
	private static final String TN_NOMENCLATURAL_CODE = "tn:nomenclaturalCode";
	private static final String TN_PUBLISHED_IN = "tn:publishedIn";
	private static final String TN_PUBLISHED_IN_CITATION = "tn:publishedInCitation";
	private static final String TN_RANK_STRING = "tn:rankString";
	private static final String TN_TYPIFICATION_STRING = "tn:typificationString";

	private static final String TC_COL_FAMILY = "tc";
    private static final String TN_COL_FAMILY = "tn";
    private static final String RAW_COL_FAMILY = "raw";
    
    static Pattern abbreviatedCanonical = Pattern.compile("([A-Z]\\. )([a-zA-ZÏËÖÜÄÉÈČÁÀÆŒïëöüäåéèčáàæœóú]{1,})");
    
    @Inject
    protected Vocabulary vocabulary;
    
	protected IndexSearcher tcIdxSearcher;
	
	@Inject
	protected CBIndexSearch cbIdxSearcher;
	
	protected HTable tcTable;
	
	/**
	 * Initialise the DAO, setting up the HTable instance.
	 * 
	 * @throws Exception
	 */
	public TaxonConceptDaoImpl() throws Exception {}

	/**
	 * Initialise the connection to HBase
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		HBaseConfiguration config = new HBaseConfiguration();
    	this.tcTable = new HTable(config, "taxonConcept");
//    	this.tcTable.setAutoFlush(false);
    	this.tcTable.setWriteBufferSize(1024*1024*12);

	}
	
	/**
	 * Get the HBase table to perform operations on
	 * 
	 * @return
	 */
	private HTable getTable() {
		if(this.tcTable==null){
			try {
				init();
				return this.tcTable;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Unable to connect to HBase."+ e.getMessage(), e);
			}
		}
		return this.tcTable;
	}
//	
//	/**
//	 * Support for the A-S-P-O simple method of adding data.
//	 * 
//	 * A = Attribution
//	 * S = Subject
//	 * P = Predicate
//	 * o = Object
//	 * 
//	 * @param guid
//	 * @param infoSourceId
//	 * @param documentId
//	 * @param predicate
//	 * @param value
//	 */
//	public void addLiteralValue(String guid, String infoSourceId, String documentId, String predicate, String value) throws Exception {
//		BatchUpdate batchUpdate = new BatchUpdate(guid);
//		batchUpdate.put(RAW_COL_FAMILY+infoSourceId+":"+documentId+":"+predicate, Bytes.toBytes(value));
//		getTable().commit(batchUpdate);
//	}
//	
//	/**
//	 * Support for the A-S-P-O simple method of adding data.
//	 * 
//	 * @param guid
//	 * @param infoSourceId
//	 * @param documentId
//	 * @param predicate
//	 * @param value
//	 * 
//	 * @return true if successfully added
//	 */	
//	public boolean addLiteralValues(String guid, String infoSourceId, String documentId, Map<String, Object> values) throws Exception {
//		
//		if(!getTable().exists(Bytes.toBytes(guid))){
//			logger.error("Warning: unable to find row for GUID: "+guid);
//			return false;
//		}
//		
//		BatchUpdate batchUpdate = new BatchUpdate(guid);
//		Iterator<String> keys = values.keySet().iterator();
//		while(keys.hasNext()){
//			String predicate = keys.next();
//			
//			//FIXMEremove the base URL, to make the column header more succicient
//			URI uri = new URI(predicate);
//			String fragment = uri.getFragment();
//			if(fragment==null){
//				fragment = uri.getPath();
//				fragment = fragment.replace("/", "");
//			}
//			
//			batchUpdate.put(RAW_COL_FAMILY+infoSourceId+":"+documentId+":"+fragment, Bytes.toBytes(values.get(predicate).toString()));
//		}
//		getTable().commit(batchUpdate);
//		return true;
//	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getSynonymsFor(java.lang.String)
	 */
	public List<TaxonConcept> getSynonymsFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getSynonyms(result);
		} else {
			return new ArrayList<TaxonConcept>();
		}
	}

	private List<TaxonConcept> getSynonyms(Result result) throws IOException,
			JsonParseException, JsonMappingException {
		byte [] synonym = result.getValue(Bytes.toBytes(SYNONYM_COL));
		return getTaxonConceptsFrom(synonym);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getImages(java.lang.String)
	 */
	public List<Image> getImages(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getImages(result);
		} else {
			return new ArrayList<Image>();
		}
	}

	private List<Image> getImages(Result result) throws IOException,
			JsonParseException, JsonMappingException {
		byte [] images = result.getValue(Bytes.toBytes(IMAGE_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (images != null) {
			return mapper.readValue(images, 0, images.length, new TypeReference<List<Image>>(){});
		} 
		return new ArrayList<Image>();
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getPestStatuses(java.lang.String)
	 */
	public List<PestStatus> getPestStatuses(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getPestStatus(result);
		} else {
			return new ArrayList<PestStatus>();
		}
	}

	private List<PestStatus> getPestStatus(Result result) throws IOException,
			JsonParseException, JsonMappingException {
		byte [] pestStatus = result.getValue(Bytes.toBytes(PEST_STATUS_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (pestStatus != null) {
			return mapper.readValue(pestStatus, 0, pestStatus.length, new TypeReference<List<PestStatus>>(){});
		} 
		return new ArrayList<PestStatus>();
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getConservationStatuses(java.lang.String)
	 */
	public List<ConservationStatus> getConservationStatuses(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getConservationStatus(result);
		} else {
			return new ArrayList<ConservationStatus>();
		}
	}

	private List<ConservationStatus> getConservationStatus(Result result)
			throws IOException, JsonParseException, JsonMappingException {
		byte [] consStatus = result.getValue(Bytes.toBytes(CONSERVATION_STATUS_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (consStatus != null) {
			return mapper.readValue(consStatus, 0, consStatus.length, new TypeReference<List<ConservationStatus>>(){});
		} 
		return new ArrayList<ConservationStatus>();
	}

	/**
	 * Deserialise the taxon concepts from value.
	 * 
	 * @param value
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private List<TaxonConcept> getTaxonConceptsFrom(byte [] value)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<TaxonConcept>>(){});
		} 
		return new ArrayList<TaxonConcept>();
	}
	
	/**
	 * Deserialise the common names from value.
	 * 
	 * @param value
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private List<CommonName> getCommonNamesFrom(byte [] value)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<CommonName>>() {});
		}
		return new ArrayList<CommonName>();
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getChildConceptsFor(java.lang.String)
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getChildConcepts(result);
		} else {
			return new ArrayList<TaxonConcept>();
		}
	}

	private List<TaxonConcept> getChildConcepts(Result result)
			throws IOException, JsonParseException, JsonMappingException {
		byte [] value = result.getValue(Bytes.toBytes(IS_PARENT_COL_OF));
		return getTaxonConceptsFrom(value);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getParentConceptsFor(java.lang.String)
	 */	
	public List<TaxonConcept> getParentConceptsFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getParentConcepts(result);
		} else {
			return new ArrayList<TaxonConcept>();
		}
	}

	private List<TaxonConcept> getParentConcepts(Result result)
			throws IOException, JsonParseException, JsonMappingException {
		byte [] value = result.getValue(Bytes.toBytes(IS_CHILD_COL_OF));
		return getTaxonConceptsFrom(value);
	}		
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getCommonNamesFor(java.lang.String)
	 */
	public List<CommonName> getCommonNamesFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getCommonNames(result);
		} else {
			return new ArrayList<CommonName>();
		}
	}

	private List<CommonName> getCommonNames(Result result) throws IOException,
			JsonParseException, JsonMappingException {
		byte [] value = result.getValue(Bytes.toBytes(VERNACULAR_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<CommonName>>(){});
		} 
		return new ArrayList<CommonName>();
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#getTextPropertiesFor(java.lang.String)
	 */
	public List<SimpleProperty> getTextPropertiesFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getTextProperties(result);
		} else {
			return new ArrayList<SimpleProperty>();
		}
	}

	private List<SimpleProperty> getTextProperties(Result result) throws IOException,
			JsonParseException, JsonMappingException {
		byte [] value = result.getValue(Bytes.toBytes(TEXT_PROPERTY_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<SimpleProperty>>(){});
		}
		return new ArrayList<SimpleProperty>();
	}
	
	/**
	 * FIXME Switch to using a single column for TaxonConcept
	 * 
	 * @see org.ala.dao.TaxonConceptDao#create(org.ala.model.TaxonConcept)
	 */
	public boolean create(TaxonConcept tc) throws Exception {
		
		if (tc.getGuid() == null) {
			throw new IllegalArgumentException("Supplied GUID for the Taxon Concept is null.");
		}
		
		logger.debug("Creating Taxon Concept - " + tc);
		
		Put putter = new Put(Bytes.toBytes(tc.getGuid()));
		putIfNotNull(putter, TC_ID, Integer.toString(tc.getId()));
		putIfNotNull(putter, TC_PARENT_ID, tc.getParentId());
		putIfNotNull(putter, TC_PARENT_GUID, tc.getParentGuid());
		putIfNotNull(putter, TC_NAME_STRING, tc.getNameString());
		putIfNotNull(putter, TC_AUTHOR, tc.getAuthor());
		putIfNotNull(putter, TC_AUTHOR_YEAR, tc.getAuthorYear());
		putIfNotNull(putter, TC_PUBLISHED_IN, tc.getPublishedIn());
		putIfNotNull(putter, TC_PUBLISHED_IN_CITATION, tc.getPublishedInCitation());
		putIfNotNull(putter, TC_ACCEPTED_CONCEPT_GUID, tc.getAcceptedConceptGuid());
		putIfNotNull(putter, TC_RANK_STRING, tc.getRankString());
		
		putIfNotNull(putter, TC_INFO_SOURCE_ID, tc.getInfoSourceId());
		putIfNotNull(putter, TC_INFO_SOURCE_NAME, tc.getInfoSourceName());
		putIfNotNull(putter, TC_INFO_SOURCE_URL, tc.getInfoSourceURL());
		getTable().put(putter);	
		
		return true;
	}
	
	public boolean update(TaxonConcept tc) throws Exception {
		
		if (tc.getGuid() == null) {
			throw new IllegalArgumentException("Supplied GUID for the Taxon Concept is null.");
		}
		
		logger.debug("Updating Taxon Concept - " + tc);

		Put putter = new Put(Bytes.toBytes(tc.getGuid()));
		putIfNotNull(putter, TC_AUTHOR_YEAR, tc.getAuthorYear());
		putIfNotNull(putter, TC_PUBLISHED_IN, tc.getPublishedIn());
		putIfNotNull(putter, TC_PUBLISHED_IN_CITATION, tc.getPublishedInCitation());
		putIfNotNull(putter, TC_ACCEPTED_CONCEPT_GUID, tc.getAcceptedConceptGuid());
		putIfNotNull(putter, TC_RANK_STRING, tc.getRankString());
		putIfNotNull(putter, TC_INFO_SOURCE_ID, tc.getInfoSourceId());
		putIfNotNull(putter, TC_INFO_SOURCE_NAME, tc.getInfoSourceName());
		putIfNotNull(putter, TC_INFO_SOURCE_URL, tc.getInfoSourceURL());
		getTable().put(putter);	
		return true;
	}

	/**
	 * FIXME Switch to using a single column for TaxonName(s)
	 * 
	 * @see org.ala.dao.TaxonConceptDao#addTaxonName(java.lang.String, org.ala.model.TaxonName)
	 */
	public boolean addTaxonName(String guid, TaxonName tn) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (result.isEmpty()) {
			logger.error("Unable to locate a row to add taxon name to for guid: "+guid);
			return false;
		}
		Put putter = new Put(Bytes.toBytes(guid));
		putIfNotNull(putter, TN_GUID, tn.guid);
		putIfNotNull(putter, TN_NAME_COMPLETE, tn.nameComplete);
		putIfNotNull(putter, TN_AUTHORSHIP, tn.authorship);
		putIfNotNull(putter, TN_NOMENCLATURAL_CODE, tn.nomenclaturalCode);
		putIfNotNull(putter, TN_TYPIFICATION_STRING, tn.typificationString);
		putIfNotNull(putter, TN_PUBLISHED_IN_CITATION, tn.publishedInCitation);
		putIfNotNull(putter, TN_PUBLISHED_IN, tn.publishedIn);
		putIfNotNull(putter, TN_RANK_STRING, tn.rankString);
		getTable().put(putter);
		return true;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addCommonName(java.lang.String, org.ala.model.CommonName)
	 */
	public boolean addCommonName(String guid, CommonName commonName) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, VERNACULAR_COL, commonName, new TypeReference<List<CommonName>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addConservationStatus(java.lang.String, org.ala.model.ConservationStatus)
	 */
	public boolean addConservationStatus(String guid, ConservationStatus conservationStatus) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, CONSERVATION_STATUS_COL, conservationStatus, new TypeReference<List<ConservationStatus>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addPestStatus(java.lang.String, org.ala.model.PestStatus)
	 */
	public boolean addPestStatus(String guid, PestStatus pestStatus) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, PEST_STATUS_COL, pestStatus, new TypeReference<List<PestStatus>>(){});
	}
	
	/**
	 * Add list of regions to the Taxon Concept.
	 * 
	 * @param guid
	 * @param region
	 * @throws Exception
	 */
	public boolean addRegions(String guid, List<Region> regions) throws Exception {
		return HBaseDaoUtils.putComplexObject(getTable(), guid, REGION_COL, regions);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addImage(java.lang.String, org.ala.model.Image)
	 */
	public boolean addImage(String guid, Image image) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IMAGE_COL, image, new TypeReference<List<Image>>(){});
	}
		
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addSynonym(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addSynonym(String guid, TaxonConcept synonym) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, SYNONYM_COL, synonym, new TypeReference<List<TaxonConcept>>(){});
	}	
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addIsSynonymFor(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addIsSynonymFor(String guid, TaxonConcept acceptedConcept) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IS_SYNONYM_FOR_COL, acceptedConcept, new TypeReference<List<TaxonConcept>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addIsCongruentTo(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addIsCongruentTo(String guid, TaxonConcept congruent) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IS_CONGRUENT_TO_COL, congruent, new TypeReference<List<TaxonConcept>>(){});
	}
	
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addChildTaxon(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addChildTaxon(String guid, TaxonConcept childConcept) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IS_PARENT_COL_OF, childConcept, new TypeReference<List<TaxonConcept>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addIdentifier(java.lang.String, java.lang.String)
	 */
	public boolean addIdentifier(String guid, String alternativeIdentifier) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IDENTIFIER_COL, alternativeIdentifier, new TypeReference<List<String>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addParentTaxon(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addParentTaxon(String guid, TaxonConcept parentConcept) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, IS_CHILD_COL_OF, parentConcept, new TypeReference<List<TaxonConcept>>(){});
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#addTextProperty(java.lang.String, org.ala.model.SimpleProperty)
	 */
	public boolean addTextProperty(String guid, SimpleProperty textProperty) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, TEXT_PROPERTY_COL, textProperty, new TypeReference<List<SimpleProperty>>(){});
	}
	
	/**
	 * Add field update to the put if the supplied value is not null.
	 * 
	 * @param put
	 * @param fieldName
	 * @param value
	 */
	private void putIfNotNull(Put put, String fieldName, String value) {
		value = StringUtils.trimToNull(value);
		if (value != null) {
			put.add(Bytes.toBytes(fieldName), put.getTimeStamp(), Bytes.toBytes(value));
		}
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#create(java.util.List)
	 */
	public void create(List<TaxonConcept> taxonConcepts) throws Exception {
		for(TaxonConcept tc: taxonConcepts){
			create(tc);
		}
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getByGuid(java.lang.String)
	 */
	public TaxonConcept getByGuid(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (result.isEmpty()) {
			return null;
		}
		return getTaxonConcept(result);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtendedTaxonConceptByGuid(java.lang.String)
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid) throws Exception {

		Result result = getTable().get(new Get(Bytes.toBytes(guid)));
		if (result.isEmpty()) {
			return null;
		}
		ExtendedTaxonConceptDTO etc = new ExtendedTaxonConceptDTO();
		
		//populate the dto
		etc.setTaxonConcept(getTaxonConcept(result));
		etc.setTaxonName(getTaxonName(result));
        etc.setClassification(getClassification(result));
		etc.setSynonyms(getSynonyms(result));
		etc.setCommonNames(getCommonNames(result));
		etc.setChildConcepts(getChildConcepts(result));
		etc.setParentConcepts(getParentConcepts(result));
		etc.setPestStatuses(getPestStatus(result));
		etc.setConservationStatuses(getConservationStatus(result));
		etc.setImages(getImages(result));
        etc.setExtantStatuses(getExtantStatuses(result));
        etc.setHabitats(getHabitats(result));
        etc.setRegionTypes(Region.getRegionsByType(getRegions(result)));
        etc.setReferences(getReferences(result));
		
		// sort the list of SimpleProperties for display in UI
        List<SimpleProperty> simpleProperties = getTextProperties(result);
        Collections.sort(simpleProperties);
        etc.setSimpleProperties(simpleProperties);
		return etc;
	}
	
	/**
	 * Create a taxon concept from the row result.
	 * 
	 * FIXME Use serialised taxon concept ??
	 * 
	 * @param rowResult
	 * @return
	 */
	private TaxonConcept getTaxonConcept(Result result) {
		TaxonConcept tc = new TaxonConcept();
		String guid = new String(result.getRow());
		tc.setGuid(guid);
		tc.setAuthor(HBaseDaoUtils.getField(result, TC_AUTHOR));
		tc.setAuthorYear(HBaseDaoUtils.getField(result, TC_AUTHOR_YEAR));
		tc.setNameGuid(HBaseDaoUtils.getField(result, TC_HAS_NAME)); 
		tc.setNameString(HBaseDaoUtils.getField(result, TC_NAME_STRING));
		tc.setPublishedIn(HBaseDaoUtils.getField(result, TC_PUBLISHED_IN));
		tc.setPublishedInCitation(HBaseDaoUtils.getField(result, TC_PUBLISHED_IN_CITATION));
		tc.setInfoSourceId(HBaseDaoUtils.getField(result, TC_INFO_SOURCE_ID));
		tc.setInfoSourceName(HBaseDaoUtils.getField(result, TC_INFO_SOURCE_NAME));
		tc.setInfoSourceURL(HBaseDaoUtils.getField(result, TC_INFO_SOURCE_URL));
		return tc;
	}	
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getTaxonNameFor(java.lang.String)
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception {
		Result result = getTable().get(getTnGetter(guid));
		if (result.isEmpty()) {
			return null;
		}
		return getTaxonName(result);
	}

	/**
	 * Retrieve the Taxon Name from a row.
	 * 
	 * @param result
	 * @return
	 */
	private TaxonName getTaxonName(Result result) {
		TaxonName tn = new TaxonName();
		tn.guid = HBaseDaoUtils.getField(result, TN_GUID);
		tn.authorship = HBaseDaoUtils.getField(result, TN_AUTHORSHIP);
		tn.nameComplete = HBaseDaoUtils.getField(result, TN_NAME_COMPLETE);
		tn.nomenclaturalCode = HBaseDaoUtils.getField(result, TN_NOMENCLATURAL_CODE);
		tn.rankString = HBaseDaoUtils.getField(result, TN_RANK_STRING);
		tn.typificationString = HBaseDaoUtils.getField(result, TN_TYPIFICATION_STRING);
		tn.publishedInCitation = HBaseDaoUtils.getField(result, TN_PUBLISHED_IN_CITATION);
		tn.publishedIn = HBaseDaoUtils.getField(result, TN_PUBLISHED_IN);
		return tn;
	}

	/**
	 * Retrieve all properties for this row as a Map. Useful for debug
	 * interfaces only.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPropertiesFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (result.isEmpty()) {
			return null;
		}
		
		//treemaps are sorted
		TreeMap<String, String> properties = new TreeMap<String,String>();
		
		for (Map.Entry<byte[], NavigableMap<byte[], byte[]>> entry : result.getNoVersionMap().entrySet()) {
			for (Map.Entry<byte[], byte[]> familyEntry : entry.getValue().entrySet()) {
				properties.put(Bytes.toString(entry.getKey()) + ":" + Bytes.toString(familyEntry.getKey()), 
						Bytes.toString(familyEntry.getValue()));
			}
		}
		
		//sort by key
		return properties;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String, int)
	 */
	public List<SearchTaxonConceptDTO> findByScientificName(String input, int limit) throws Exception {
        SearchResultsDTO sr = findByScientificName(input, 0, limit, null, null);
        return sr.getTaxonConcepts();
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
    public SearchResultsDTO findByScientificName(String input, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {

        input = StringUtils.trimToNull(input);
		if(input==null){
			return new SearchResultsDTO();
		}
		
		//lower case everything
		input = input.toLowerCase();

		//construct the query for scientific name
		QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());
		Query scientificNameQuery = qp.parse("\""+input+"\"");

		//construct the query for scientific name
		qp  = new QueryParser("commonName", new SimpleAnalyzer());
		Query commonNameQuery = qp.parse("\""+input+"\"");

		//include a query against the GUID
		Query guidQuery = new TermQuery(new Term("guid", input));

		//combine the query terms
		scientificNameQuery = scientificNameQuery.combine(new Query[]{scientificNameQuery, guidQuery, commonNameQuery});
        // run the search
        SearchResultsDTO searchResults = sortPageSearch(scientificNameQuery, startIndex, pageSize, sortField, sortDirection);

        return searchResults;
    }

    /**
     * Perform Lucene search with params for sorting and paging
     *
     * @param searchQuery
     * @param startIndex
     * @param pageSize
     * @param sortDirection
     * @param sortField
     * @return
     * @throws IOException
     * @throws Exception
     */
    private SearchResultsDTO sortPageSearch(Query searchQuery, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws IOException, Exception {
        IndexSearcher tcIdxSearcher1 = getTcIdxSearcher();
        boolean direction = false;

        if (sortDirection != null && !sortDirection.isEmpty() && sortDirection.equalsIgnoreCase("desc")) {
            direction = true;
        }
        
        Sort sort = new Sort();

        if (sortField != null && !sortField.isEmpty() && !sortField.equalsIgnoreCase("score")) {
            sort.setSort(sortField, direction);
        } else {
            sort = Sort.RELEVANCE;
        }

        TopDocs topDocs = tcIdxSearcher1.search(searchQuery, null, startIndex + pageSize, sort); // TODO ues sortField here
        logger.debug("Total hits: " + topDocs.totalHits);
        List<SearchTaxonConceptDTO> tcs = new ArrayList<SearchTaxonConceptDTO>();

        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            if (i >= startIndex) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = tcIdxSearcher1.doc(scoreDoc.doc);
                tcs.add(createTaxonConceptFromIndex(doc, scoreDoc.score));
            }
        }

        SearchResultsDTO searchResults = new SearchResultsDTO(tcs);
        searchResults.setTotalRecords(topDocs.totalHits);
        searchResults.setStartIndex(startIndex);
        searchResults.setStatus("OK");
        searchResults.setSort(sortField);
        searchResults.setDir(sortDirection);
        searchResults.setQuery(searchQuery.toString());
        
        return searchResults;
    }

    /**
	 * @see org.ala.dao.TaxonConceptDao#findAllByStatus(org.ala.util.StatusType, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
    public SearchResultsDTO findAllByStatus(StatusType statusType, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws ParseException, Exception {
//        List<TermQuery> statusTerms = new ArrayList<TermQuery>();
//        IndexSearcher tcIdxSearcher1 = getTcIdxSearcher();
//        TermEnum terms = tcIdxSearcher1.getIndexReader().terms(new Term(statusType.toString(), ""));
//
//        while (statusType.toString().equals(terms.term().field())) {
//            statusTerms.add(new TermQuery(new Term(statusType.toString(), terms.term().text())));
//            if (!terms.next()) {
//                break;
//            }
//        }

        List<String> statusTerms = vocabulary.getTermsForStatusType(statusType);

        String query = StringUtils.join(statusTerms, "|");
        System.out.println(statusType+" query = "+query+".");
        BooleanQuery searchQuery = new BooleanQuery();

        for (String st : statusTerms) {
            searchQuery.add(new TermQuery(new Term(statusType.toString(), st)), BooleanClause.Occur.SHOULD);
        }
        System.out.println("search query = "+searchQuery.toString());
        return sortPageSearch(searchQuery, startIndex, pageSize, sortField, sortDirection);
    }
	
//	/**
//	 * @see org.ala.dao.TaxonConceptDao#findConceptIDForName(java.lang.String, java.lang.String, java.lang.String)
//	 */
//	public String findConceptIDForName(String kingdom, String genus, String scientificName) throws Exception {
//		try {
//			String searchName = scientificName;
//
//			searchName = expandAbbreviation(genus, scientificName);
//			
//			IndexSearcher is = getTcIdxSearcher();
//			QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());
//			Query q = qp.parse("\""+searchName.toLowerCase()+"\"");
//			
//			TopDocs topDocs = is.search(q, 20);
//			
//			for(ScoreDoc scoreDoc: topDocs.scoreDocs){
//				Document doc = is.doc(scoreDoc.doc);
////				Field hasSynonym = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym");
////				if(hasSynonym!=null){
////					logger.debug("Returning synonym");
////					return hasSynonym.stringValue();
////				}
////				Field hasVernacular = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular");
////				if(hasVernacular!=null){
////					logger.debug("Returning vernacular");
////					return hasVernacular.stringValue();
////				}
////				Field isCongruentTo = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo");
////				if(isCongruentTo!=null){
////					logger.debug("Returning congruent");
////					return isCongruentTo.stringValue();
////				}
////			logger.debug("Doc Id: "+scoreDoc.doc);
////			logger.debug("Guid: "+doc.getField("guid").stringValue());
////			logger.debug("Name: "+doc.getField("scientificName").stringValue());
////			logger.debug("Raw name: "+doc.getField("scientificNameRaw").stringValue());
////			logger.debug("#################################");
//				return doc.getField("guid").stringValue();
//			}
//		} catch (Exception e) {
//			logger.error("Problem searching with:"+scientificName+" : "+e.getMessage());
//		}		
//		return null;
//	}
//
//	/**
//	 * @param genus
//	 * @param scientificName
//	 * @param searchName
//	 * @return
//	 */
//	private String expandAbbreviation(String genus, String scientificName) {
//		String expandedName = null;
//		
//		//change A. bus to Aus bus if it is abbreviated
//		if (abbreviatedCanonical.matcher(scientificName).matches()
//				&& genus != null) {
//			NameParser np = new NameParser();
//
//			ParsedName parsedName = np.parse(scientificName);
//
//			if (parsedName != null) {
//				if (parsedName.isBinomial()) {
//					expandedName = genus + " "
//							+ parsedName.getSpecificEpithet();
//				}
//			}
//		}
//		return expandedName;
//	}	

	/**
	 * @see org.ala.dao.TaxonConceptDao#findLsidByName(java.lang.String, java.lang.String)
	 */
	@Override
	public String findLsidByName(String kingdom, String genus, String scientificName, String taxonRank) {
		String lsid = null;
		try {
			lsid = cbIdxSearcher.searchForLSID(lc(scientificName), lc(genus), lc(kingdom), RankType.getForName(taxonRank));
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception - " + e.getMessage() + e.getResults());
		}
		return lsid;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#findLsidByName(java.lang.String, java.lang.String)
	 */
	@Override
	public String findLsidByName(String scientificName, String taxonRank) {
		String lsid = null;
		try {
			lsid = cbIdxSearcher.searchForLSID(lc(scientificName), RankType.getForName(taxonRank));
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception - " + e.getMessage() + e.getResults());
		}
		return lsid;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#findCBDataByName(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public NameSearchResult findCBDataByName(String kingdom, String genus,
			String scientificName, String rank) throws SearchResultException {
		return cbIdxSearcher.searchForRecord(scientificName, kingdom, genus, RankType.getForName(rank));
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getByParentGuid(java.lang.String, int)
	 */
	public List<SearchTaxonConceptDTO> getByParentGuid(String parentGuid, int limit) throws Exception {
		if(parentGuid==null){
			parentGuid = "NULL";
		}
		return searchTaxonConceptIndexBy("parentGuid", parentGuid, limit);
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
	private List<SearchTaxonConceptDTO> searchTaxonConceptIndexBy(String columnName, String value, int limit)
			throws Exception {
		Query query = new TermQuery(new Term(columnName, value));
		IndexSearcher tcIdxSearcher = getTcIdxSearcher();
		TopDocs topDocs = tcIdxSearcher.search(query, limit);
		List<SearchTaxonConceptDTO> tcs = new ArrayList<SearchTaxonConceptDTO>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = tcIdxSearcher.doc(scoreDoc.doc);
			tcs.add(createTaxonConceptFromIndex(doc, scoreDoc.score));
		}	
		return tcs;
	}
	
	/**
	 * Retrieves the index search for taxon concepts, initialising if
	 * necessary.
	 * @return
	 * @throws Exception
	 */
	private IndexSearcher getTcIdxSearcher() throws Exception {
		//FIXME move to dependency injection
		if(this.tcIdxSearcher==null){
	    	File file = new File(TC_INDEX_DIR);
	    	if(file.exists()){
	    		this.tcIdxSearcher = new IndexSearcher(TC_INDEX_DIR);
	    	}
		}
		return this.tcIdxSearcher;
	}

	/**
	 * Populate a TaxonConcept from the data in the lucene index.
	 * 
	 * @param doc
	 * @return
	 */
	private SearchTaxonConceptDTO createTaxonConceptFromIndex(Document doc, float score) {
		SearchTaxonConceptDTO taxonConcept = new SearchTaxonConceptDTO();
		taxonConcept.setGuid(doc.get("guid"));
		taxonConcept.setParentGuid(doc.get("parentGuid"));
		taxonConcept.setNameString(doc.get("scientificNameRaw"));
		taxonConcept.setAcceptedConceptName(doc.get("acceptedConceptName"));
		String hasChildrenAsString = doc.get("hasChildren");
		String[] commonNames = doc.getValues("commonName");
		if(commonNames.length>0){
			taxonConcept.setCommonName(commonNames[0]);
		}

		taxonConcept.setHasChildren(Boolean.parseBoolean(hasChildrenAsString));
        taxonConcept.setScore(score);
        taxonConcept.setRank(doc.get("rank"));
        try {
            taxonConcept.setRankId(Integer.parseInt(doc.get("rankId")));
        } catch (NumberFormatException ex) {
            logger.error("Error parsing rankId: "+ex.getMessage());
        }
        taxonConcept.setPestStatus(doc.get(StatusType.PEST.toString()));
        taxonConcept.setConservationStatus(doc.get(StatusType.CONSERVATION.toString()));

		return taxonConcept;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#delete(java.lang.String)
	 */
	public boolean delete(String guid) throws Exception {
		if (getTable().exists(new Get(Bytes.toBytes(guid)))) {
			getTable().delete(new Delete(Bytes.toBytes(guid)));
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addClassification(java.lang.String, org.ala.model.Classification)
	 */
	@Override
	public boolean addClassification(String guid, Classification classification) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, CLASSIFICATION_COL, classification, new TypeReference<List<Classification>>(){});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getClassifications(java.lang.String)
	 */
	public List<Classification> getClassifications(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getClassificationsForRow(result);
		} else {
			return new ArrayList<Classification>();
		}
	}

    private List<Classification> getClassificationsForRow(Result result) throws IOException {
        byte [] value = result.getValue(Bytes.toBytes(CLASSIFICATION_COL));
        ObjectMapper mapper = new ObjectMapper();
        if (value != null) {
            return mapper.readValue(value, 0, value.length, new TypeReference<List<Classification>>() {
            });
        }
        return new ArrayList<Classification>();
    }

    private Classification getClassification(Result result) throws IOException {
        List<Classification> cs = getClassificationsForRow(result);
        Classification c = null; // new Classification();
        if (cs != null && cs.size() > 0) {
            c = cs.get(0);
        }
        return c;
    }

	/**
	 * @see org.ala.dao.TaxonConceptDao#syncTriples(org.ala.model.Document, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public boolean syncTriples(org.ala.model.Document document, List<Triple> triples) throws Exception {

		String scientificName = null;
		String specificEpithet = null;
		String species = null;
		String genus = null;
		String family = null;
		String order = null;
		String kingdom = null;

        String dcSource = null;
        String dcPublisher = null;
        String dcIdentifier = null;
        String dcTitle = null;

        if (document!=null) {
            dcPublisher = document.getInfoSourceName();
            dcSource = document.getInfoSourceUri();
            dcIdentifier = document.getIdentifier();
            dcTitle = document.getTitle();
        }
		
		//iterate through triples and find scientific names and genus
		for(Triple triple: triples){
			
			String predicate = triple.predicate.substring(triple.predicate.lastIndexOf("#")+1);

			if(predicate.endsWith("hasKingdom")){
				kingdom = triple.object;
			}
			if(predicate.endsWith("hasOrder")){
				order = triple.object;
			}
			if(predicate.endsWith("hasFamily")){
				family = triple.object;
			}
			if(predicate.endsWith("hasGenus")){
				genus = triple.object;
			}
			if(predicate.endsWith("hasSpecies")){
				species = triple.object;
			}
			if(predicate.endsWith("hasSpecificEpithet")){
				specificEpithet = triple.object;
			}
			if(predicate.endsWith("hasScientificName")){
				scientificName = triple.object;
			}
		}
		
		if (scientificName == null 
				&& species == null 
				&& genus == null
				&& family == null 
				&& order == null
				&& specificEpithet == null) {
			logger.error("No classification found for document at: " + document.getFilePath());
			return false; // we have nothing to work with, so give up
		}
		
		// Lookup LSID in Checklist Bank data 
		String rank = null;
		if (scientificName == null) {
			if (species != null) {
				scientificName = species;
				rank = "species";
			} else if (genus != null) {
				if (specificEpithet != null) {
					scientificName = genus + " " + specificEpithet;
					rank = "species";
				} else {
					scientificName = genus;
					rank = "genus";
				}
			} else if (family != null) {
				scientificName = family;
				rank = "family";
			} else if (order != null) {
				scientificName = order;
				rank = "order";
			} else if (kingdom != null) {
				scientificName = kingdom;
				rank = "kingdom";
			} else {
				logger.error("Not enough search data for Checklist Bank found for document at: "+document.getFilePath());
				return false;
			}
		}
		String guid = findLsidByName(kingdom, genus, scientificName, rank);

		// if null try with the species name
		if (guid == null && species != null) {
			guid = findLsidByName(kingdom, genus, species, "species");
		}
		// FIX ME search with genus + specific epithet
		if (guid == null && genus != null && specificEpithet != null) {
			guid = findLsidByName(kingdom, genus, genus + " " + specificEpithet, "species");
		}

		if (guid != null) {
			
			// Create a list of key-value items where the key is the hbase column and 
			// the value is a list of model objects to be written to the hbase column.
			List<KeyValue> kvList = new ArrayList<KeyValue>();

			for(Triple triple: triples){
				
				logger.trace(triple.predicate);
				
				//check here for predicates of complex objects
				if(triple.predicate.endsWith("hasCommonName")){
					
					CommonName commonName = new CommonName();
					commonName.setNameString(triple.object);
					commonName.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
					commonName.setDocumentId(Integer.toString(document.getId()));
                    commonName.setInfoSourceName(dcPublisher);
                    commonName.setInfoSourceURL(dcSource);
                    commonName.setTitle(dcTitle);
                    commonName.setIdentifier(dcIdentifier);

                    List<CommonName> vernacularList = null;
					int idx = indexOfKeyInList(kvList, VERNACULAR_COL);
					if (idx == -1) {
						vernacularList = new ArrayList<CommonName>();
						kvList.add(new DefaultKeyValue(VERNACULAR_COL, vernacularList));
					} else {
						vernacularList = (List<CommonName>) kvList.get(idx).getValue();
					}
                    vernacularList.add(commonName);
					
				} else if(triple.predicate.endsWith("hasConservationStatus")){
					
					//lookup the vocabulary term
					ConservationStatus cs = vocabulary.getConservationStatusFor(document.getInfoSourceId(), triple.object);
					if(cs==null){
						cs = new ConservationStatus();
						cs.setStatus(triple.object);
					}

					cs.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
					cs.setDocumentId(Integer.toString(document.getId()));
                    cs.setInfoSourceName(dcPublisher);
                    cs.setInfoSourceURL(dcSource);
                    cs.setTitle(dcTitle);
                    cs.setIdentifier(dcIdentifier);

                    List<ConservationStatus> conservationStatusList = null;
					int idx = indexOfKeyInList(kvList, CONSERVATION_STATUS_COL);
					if (idx == -1) {
						conservationStatusList = new ArrayList<ConservationStatus>();
						kvList.add(new DefaultKeyValue(CONSERVATION_STATUS_COL, conservationStatusList));
					} else {
						conservationStatusList = (List<ConservationStatus>) kvList.get(idx).getValue();
					}
                    conservationStatusList.add(cs);
					
				} else if(triple.predicate.endsWith("hasPestStatus")){

					//lookup the vocabulary term
					PestStatus ps = vocabulary.getPestStatusFor(document.getInfoSourceId(), triple.object);
					if(ps==null){
						ps = new PestStatus();
						ps.setStatus(triple.object);
					}
					
					ps.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
					ps.setDocumentId(Integer.toString(document.getId()));
                    ps.setInfoSourceName(dcPublisher);
                    ps.setInfoSourceURL(dcSource);
                    ps.setTitle(dcTitle);
                    ps.setIdentifier(dcIdentifier);

                    List<PestStatus> pestStatusList = null;
					int idx = indexOfKeyInList(kvList, PEST_STATUS_COL);
					if (idx == -1) {
						pestStatusList = new ArrayList<PestStatus>();
						kvList.add(new DefaultKeyValue(PEST_STATUS_COL, pestStatusList));
					} else {
						pestStatusList = (List<PestStatus>) kvList.get(idx).getValue();
					}
                    pestStatusList.add(ps);
					
                } else if (triple.predicate.endsWith("hasImagePageUrl")) {
                    // do nothing but prevent getting caught next - added further down
                } else if (!Predicates.getTaxonomicPredicates().contains(triple.predicate)) {  
                	// Ignoring Taxonomic Predicates for tc:hasTextProperty column.
                	
                	// FIXME - this feels mighty unscalable...
                	// essentially we are putting all other field values in one very 
                	// large cell
                	// if this becomes a performance problem we should split
                	// on the predicate value. i.e. tc:hasHabitatText,
                	// this was the intention with the "raw:" column family namespace
                	
                    SimpleProperty simpleProperty = new SimpleProperty();
                    simpleProperty.setName(triple.predicate);
                    simpleProperty.setValue(triple.object);
                    simpleProperty.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
                    simpleProperty.setDocumentId(Integer.toString(document.getId()));
                    simpleProperty.setInfoSourceName(dcPublisher);
                    simpleProperty.setInfoSourceURL(dcSource);
                    simpleProperty.setTitle(dcTitle);
                    simpleProperty.setIdentifier(dcIdentifier);

                    List<SimpleProperty> textPropertyList = null;
					int idx = indexOfKeyInList(kvList, TEXT_PROPERTY_COL);
					if (idx == -1) {
						textPropertyList = new ArrayList<SimpleProperty>();
						kvList.add(new DefaultKeyValue(TEXT_PROPERTY_COL, textPropertyList));
					} else {
						textPropertyList = (List<SimpleProperty>) kvList.get(idx).getValue();
					}
                    textPropertyList.add(simpleProperty);
                }
			}
			
			//retrieve the content type
			if(document.getFilePath()!=null){

				//FIXME - we should be able to copy images up to the parent
				
				//is it an image ???
				if(document!=null && document.getMimeType()!=null && MimeType.getImageMimeTypes().contains(document.getMimeType())){
					Image image = new Image();
					image.setContentType(document.getMimeType());
					image.setRepoLocation(document.getFilePath()
							+File.separator
							+FileType.RAW.getFilename()
							+MimeType.getFileExtension(document.getMimeType()));
					image.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
					image.setDocumentId(Integer.toString(document.getId()));
                    image.setInfoSourceName(dcPublisher);
                    image.setInfoSourceURL(dcSource);
                    image.setIdentifier(dcIdentifier);
                    image.setTitle(dcTitle);

                    List<Image> imageList = null;
					int idx = indexOfKeyInList(kvList, IMAGE_COL);
					if (idx == -1) {
						imageList = new ArrayList<Image>();
						kvList.add(new DefaultKeyValue(IMAGE_COL, imageList));
					} else {
						imageList = (List<Image>) kvList.get(idx).getValue();
					}
                    imageList.add(image);
				}
			}
			
//			logger.debug("Adding content to: "+guid+", using scientific name: "+scientificName+", genus: "+genus);
//			addLiteralValues(guid, infoSourceId,Integer.toString(document.getId()), properties);
			
			// Update hbase row
			return HBaseDaoUtils.update(getTable(), guid, kvList);
		} else {
			return false;
		}
	}

	/**
	 * @param kvList
	 * @param key
	 * @return
	 */
	private int indexOfKeyInList(List<KeyValue> kvList, String key) {
		for (int i = 0; i < kvList.size(); i++) {
			if (kvList.get(i).getKey().equals(key)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#clearRawProperties()
	 */
	public void clearRawProperties() throws Exception {
		
    	ResultScanner scanner = getTable().getScanner(Bytes.toBytes(TC_COL_FAMILY));
    	Iterator<Result> iter = scanner.iterator();
		int i = 0;
		while (iter.hasNext()) {
    		Result result = iter.next();
    		byte[] row = result.getRow();
    		getTable().delete(new Delete(row).deleteFamily(Bytes.toBytes(RAW_COL_FAMILY)));
    		logger.debug(++i + " " + (new String(row)));
    	}
    	logger.debug("Raw triples cleared");
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#createIndex()
	 */
	public void createIndex() throws Exception {
		
		long start = System.currentTimeMillis();
		
        List<String> pestTerms = vocabulary.getTermsForStatusType(StatusType.PEST);
        List<String> consTerms = vocabulary.getTermsForStatusType(StatusType.CONSERVATION);
		
    	File file = new File(TC_INDEX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	//Analyzer analyzer = new KeywordAnalyzer(); - works for exact matches
    	//KeywordAnalyzer analyzer = new KeywordAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();

    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		
    	ResultScanner scanner = getTable().getScanner(Bytes.toBytes(TC_COL_FAMILY));
    	Iterator<Result> iter = scanner.iterator();
		int i = 0;
		while (iter.hasNext()) {
    		i++;
    		Result result = iter.next();

    		//get taxon concept details
    		TaxonConcept taxonConcept = getTaxonConcept(result);
            
            //get synonyms
    		byte [] synonymsValue = result.getValue(Bytes.toBytes(SYNONYM_COL));
    		List<TaxonConcept> synonyms = getTaxonConceptsFrom(synonymsValue);
    		
    		byte [] isCongruentValue = result.getValue(Bytes.toBytes(IS_CONGRUENT_TO_COL));
    		List<TaxonConcept> congruentTcs = getTaxonConceptsFrom(isCongruentValue);
    		
    		//treat congruent objects the same way we do synonyms
    		synonyms.addAll(congruentTcs);
    		
    		//get common names
    		byte [] commonNamesValue = result.getValue(Bytes.toBytes(VERNACULAR_COL));
    		List<CommonName> commonNames = getCommonNamesFrom(commonNamesValue);
    		
    		//add the parent id to enable tree browsing with this index
    		byte [] childrenValue = result.getValue(Bytes.toBytes(IS_CHILD_COL_OF));
    		List<TaxonConcept> children = getTaxonConceptsFrom(childrenValue);

    		//add conservation and pest status'
            List<ConservationStatus> conservationStatuses = getConservationStatus(result);
    		List<PestStatus> pestStatuses = getPestStatus(result);

            //add text properties
            List<SimpleProperty> simpleProperties = getTextProperties(result);
            
    		// save all infosource ids to add in a Set to index at the end
    		Set<String> infoSourceIds = new TreeSet<String>();
            
    		//TODO this index should also include nub ids
    		Document doc = new Document();
            
    		if(taxonConcept.getNameString()!=null){
    			
    			doc.add(new Field("guid", taxonConcept.getGuid(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
    			addToSetSafely(infoSourceIds, taxonConcept.getInfoSourceId());
    			//add multiple forms of the scientific name to the index
    			LuceneUtils.addScientificNameToIndex(doc, taxonConcept.getNameString(), taxonConcept.getRankString());
	    		
	    		if(taxonConcept.getParentGuid()!=null){
	    			doc.add(new Field("parentGuid", taxonConcept.getParentGuid(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
	    		}
	    		
                for (ConservationStatus cs : conservationStatuses) {
                    for (String csTerm : consTerms) {
                        if (cs.getStatus()!=null && cs.getStatus().toLowerCase().contains(csTerm.toLowerCase())) {
                            Field f = new Field("conservationStatus", csTerm, Store.YES, Index.NOT_ANALYZED);
                            f.setBoost(0.6f);
                            doc.add(f);
                			addToSetSafely(infoSourceIds, cs.getInfoSourceId());
                        }
                    }
                }

                for (PestStatus ps : pestStatuses) {
                    for (String psTerm : pestTerms) {
                        if (ps.getStatus().toLowerCase().contains(psTerm.toLowerCase())) {
                            Field f = new Field("pestStatus", psTerm, Store.YES, Index.NOT_ANALYZED);
                            f.setBoost(0.6f);
                            doc.add(f);
                            addToSetSafely(infoSourceIds, ps.getInfoSourceId());
                        }
                    }
                }

                for (SimpleProperty sp : simpleProperties) {
                    // index *Text properties
                    if (sp.getName().endsWith("Text")) {
                        Field textField = new Field("simpleText", sp.getValue(), Store.YES, Index.ANALYZED);
                        textField.setBoost(0.4f);
                        doc.add(textField);
                        addToSetSafely(infoSourceIds, sp.getInfoSourceId());
                    }
                }

                //StringBuffer cnStr = new StringBuffer();
                Set<String> commonNameSet = new TreeSet<String>();
	    		for(CommonName cn: commonNames){
	    			if(cn.nameString!=null){
	    				commonNameSet.add(cn.nameString.toLowerCase());
						addToSetSafely(infoSourceIds, cn.getInfoSourceId());
                        //doc.add(new Field("commonName", cn.nameString.toLowerCase(), Store.YES, Index.ANALYZED));
                        //cnStr.append(cn.nameString.toLowerCase() + " ");
	    			}
	    		}

                if (commonNameSet.size() > 0) {
                    String commonNamesConcat = StringUtils.deleteWhitespace(StringUtils.join(commonNameSet, " "));
                    doc.add(new Field("commonNameSort", commonNamesConcat, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                    doc.add(new Field("commonName", StringUtils.join(commonNameSet, " "), Store.YES, Index.ANALYZED));
                    doc.add(new Field("commonNameDisplay", StringUtils.join(commonNameSet, ", "), Store.YES, Index.ANALYZED));
                }
	    		
	    		for(TaxonConcept synonym: synonyms){
	    			if(synonym.getNameString()!=null){
	    				logger.debug("adding synonym to index: "+synonym.getNameString());
	    				//add a new document for each synonym
	    				Document synonymDoc = new Document();
	    				synonymDoc.add(new Field("guid", taxonConcept.getGuid(), Store.YES, Index.NO));
	    				LuceneUtils.addScientificNameToIndex(synonymDoc, synonym.getNameString(), null);
	    				synonymDoc.add(new Field("acceptedConceptName", taxonConcept.getNameString(), Store.YES, Index.NO));
	    				if(!commonNames.isEmpty()){
	    					synonymDoc.add(new Field("commonNameSort", commonNames.get(0).nameString, Store.YES, Index.NO));
	    					synonymDoc.add(new Field("commonNameDisplay", StringUtils.join(commonNameSet, ", "), Store.YES, Index.ANALYZED));
	    				}
	                    addRankToIndex(taxonConcept.getRankString(), synonymDoc);
	    				//add the synonym as a separate document
	    				iw.addDocument(synonymDoc, analyzer);
                        if (synonym.getInfoSourceId()!=null){
                        	infoSourceIds.add(synonym.getInfoSourceId()); // getting NPE
                        }
	    			}
	    		}
	    		
                addRankToIndex(taxonConcept.getRankString(), doc);
	    		
    			doc.add(new Field("hasChildren", Boolean.toString(!children.isEmpty()), Store.YES, Index.NO));
                
                doc.add(new Field("dataset", StringUtils.join(infoSourceIds, " "), Store.YES, Index.ANALYZED_NO_NORMS));
	    		
		    	//add to index
		    	iw.addDocument(doc, analyzer);
    		}
	    	if(i%10000==0){
	    		iw.commit();
	    	}
	    	
    		if (i%100==0) logger.debug(i + " " + taxonConcept.getGuid());
    	}
    	
    	iw.commit();
    	iw.close();
    	
    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds with "+ i +" documents");
	}

	/**
	 * Add the rank to the search document.
	 * 
	 * @param rankString
	 * @param doc
	 */
	private void addRankToIndex(String rankString, Document doc) {
		if (rankString!=null) {
		    try {
		        Rank rank = Rank.getForField(rankString.toLowerCase());
		        doc.add(new Field("rank", rank.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		        doc.add(new Field("rankId", rank.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		    } catch (Exception e) {
		        logger.error("Rank not found: "+rankString+" - ");
		        // assign to Rank.TAXSUPRAGEN so that sorting still works reliably
		        doc.add(new Field("rank", Rank.TAXSUPRAGEN.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		        doc.add(new Field("rankId", Rank.TAXSUPRAGEN.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		    }
		}
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addReference(org.ala.model.Reference)
	 */
	public boolean addReference(String guid, Reference reference) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, REFERENCE_COL, reference, new TypeReference<List<Reference>>() {});
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addPublication(java.lang.String, org.ala.model.Publication)
	 */
	public boolean addPublication(String guid, Publication publication) throws Exception {
		return HBaseDaoUtils.storeComplexObject(getTable(), guid, PUBLICATION_COL, publication, new TypeReference<List<Publication>>(){});
	}
	
//	/**
//	 * Sets a new Lucene IndexSearcher for the supplied index directory.
//	 * 
//	 * @param indexDir the location of the lucene index
//	 * @throws IOException 
//	 * @throws CorruptIndexException 
//	 */
//	public void setLuceneIndexLocation(String indexDir)
//			throws CorruptIndexException, IOException {
//		File file = new File(indexDir);
//		if (file.exists()) {
//			this.tcIdxSearcher = new IndexSearcher(indexDir);
//		}
//	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtantStatus(java.lang.String)
	 */
	@Override
	public List<ExtantStatus> getExtantStatuses(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getExtantStatuses(result);
		} else {
			return new ArrayList<ExtantStatus>();
		}
	}
		
	private List<ExtantStatus> getExtantStatuses(Result result) throws JsonParseException, JsonMappingException, IOException {
		byte [] value = result.getValue(Bytes.toBytes(EXTANT_STATUS_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<ExtantStatus>>(){});
		} 
		return new ArrayList<ExtantStatus>();
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getHabitats(java.lang.String)
	 */
	@Override
	public List<Habitat> getHabitats(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getHabitats(result);
		} else {
			return new ArrayList<Habitat>();
		}
	}
	
	private List<Habitat> getHabitats(Result result) throws JsonParseException, JsonMappingException, IOException {
		byte [] value = result.getValue(Bytes.toBytes(HABITAT_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<Habitat>>(){});
		} 
		return new ArrayList<Habitat>();
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addExtantStatus(java.lang.String, org.ala.model.ExtantStatus)
	 */
	@Override
	public boolean addExtantStatus(String guid, List<ExtantStatus> extantStatusList)
			throws Exception {
		return HBaseDaoUtils.putComplexObject(getTable(), guid, EXTANT_STATUS_COL, extantStatusList);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addHabitat(java.lang.String, org.ala.model.Habitat)
	 */
	@Override
	public boolean addHabitat(String guid, List<Habitat> habitatList) throws Exception {
		return HBaseDaoUtils.putComplexObject(getTable(), guid, HABITAT_COL, habitatList);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getRegions(java.lang.String)
	 */
	@Override
	public List<Region> getRegions(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getRegions(result);
		} else {
			return new ArrayList<Region>();
		}
	}
	
	private List<Region> getRegions(Result result) throws JsonParseException, JsonMappingException, IOException {
		byte [] regionValue = result.getValue(Bytes.toBytes(REGION_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (regionValue != null) {
			return mapper.readValue(regionValue, 0, regionValue.length, new TypeReference<List<Region>>(){});
		} 
		return new ArrayList<Region>();
	}

	/**
	 * Retrieve the references for the concept with the supplied GUID.
	 * 
	 * @see org.ala.dao.TaxonConceptDao#getReferencesFor(java.lang.String)
	 */
	public List<Reference> getReferencesFor(String guid) throws Exception {
		Result result = getTable().get(getTcGetter(guid));
		if (!result.isEmpty()) {
			return getReferences(result);
		} else {
			return new ArrayList<Reference>();
		}
	}
	
	private List<Reference> getReferences(Result result) throws JsonParseException, JsonMappingException, IOException {
		byte [] value = result.getValue(Bytes.toBytes(REFERENCE_COL));
		ObjectMapper mapper = new ObjectMapper();
		if (value != null) {
			return mapper.readValue(value, 0, value.length, new TypeReference<List<Reference>>(){});
		} 
		return new ArrayList<Reference>();
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#setVocabulary(org.ala.vocabulary.Vocabulary)
	 */
	public void setVocabulary(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

    /**
     * @see org.ala.dao.TaxonConceptDao#getIndexLocation()
     */
    @Override
    public String getIndexLocation() {
        return TC_INDEX_DIR;
    }
    
    /**
     * Prevent adding a null to a set.
     * 
     * @param set 
     * @param object
     */
    private void addToSetSafely(Set set, Object object) {
    	if (object != null) {
    		set.add(object);
    	}
    }
    
    /**
     * @param guid
     * @return
     */
    private Get getTcGetter(String guid) {
    	Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(TC_COL_FAMILY));
    	return getter;
    }
    
    /**
     * @param guid
     * @return
     */
    private Get getTnGetter(String guid) {
    	Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(TN_COL_FAMILY));
    	return getter;
    }
    
    /**
     * @param string
     * @return
     */
    private String lc(String string) {
    	if (string == null) {
    		return null;
    	} else {
    		return string.toLowerCase();
    	}
    }
}