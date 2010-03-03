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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.lucene.LuceneUtils;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.Image;
import org.ala.model.PestStatus;
import org.ala.model.Rank;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.SimpleProperty;
import org.ala.model.Triple;
import org.ala.util.DublinCoreUtils;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scanner;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;
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
import org.apache.lucene.queryParser.QueryParser;
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
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.springframework.stereotype.Component;
/**
 * Prototype quality Dao in need of refactoring.
 * 
 * FIXME Move to interface and HBase specific implementation.
 * 
 * @author Dave Martin
 */
@Component("taxonConceptDao")
public class TaxonConceptDao {
	
	protected static Logger logger = Logger.getLogger(TaxonConceptDao.class);
	
	/** The location for the lucene index */
	public static final String TC_INDEX_DIR = "/data/lucene/taxonConcept";
	
	/** HBase columns */
	public static final String SYNONYM_COL = "tc:hasSynonym";
	public static final String VERNACULAR_COL = "tc:VernacularConcept";
	public static final String CONSERVATION_STATUS_COL = "tc:hasConservationStatus";
	public static final String PEST_STATUS_COL = "tc:hasPestStatus";
	public static final String IMAGE_COL = "tc:hasImage";
	public static final String IS_CHILD_COL_OF = "tc:IsChildTaxonOf";
	public static final String IS_PARENT_COL_OF = "tc:IsParentTaxonOf";
    public static final String TAXON_NAME_COL = "tc:hasTaxonName";
    public static final String TEXT_PROPERTY_COL = "tc:hasTextProperty";

    public static final String TC_COL_FAMILY = "tc:";
    public static final String RAW_COL_FAMILY = "raw:";
    
    static Pattern abbreviatedCanonical = Pattern.compile("([A-Z]\\. )([a-zA-ZÏËÖÜÄÉÈČÁÀÆŒïëöüäåéèčáàæœóú]{1,})");
    
    @Inject
    protected Vocabulary vocabulary;
    
	protected IndexSearcher tcIdxSearcher;
	
	protected HTable tcTable;

	/**
	 * Initialise the DAO, setting up the HTable instance.
	 * 
	 * @throws Exception
	 */
	public TaxonConceptDao() throws Exception {}
	
	/**
	 * Initialise the connection to HBase
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		HBaseConfiguration config = new HBaseConfiguration();
    	this.tcTable = new HTable(config, "taxonConcept");
	}
	
	/**
	 * Get the HBase table to perform operations on
	 * 
	 * @return
	 */
	public HTable getTable() {
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
	
	/**
	 * Support for the A-S-P-O simple method of adding data.
	 * 
	 * A = Attribution
	 * S = Subject
	 * P = Predicate
	 * o = Object
	 * 
	 * @param guid
	 * @param infoSourceId
	 * @param documentId
	 * @param predicate
	 * @param value
	 */
	public void addLiteralValue(String guid, String infoSourceId, String documentId, String predicate, String value) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(guid);
		batchUpdate.put(RAW_COL_FAMILY+infoSourceId+":"+documentId+":"+predicate, Bytes.toBytes(value));
		getTable().commit(batchUpdate);
	}
	
	/**
	 * Support for the A-S-P-O simple method of adding data.
	 * 
	 * @param guid
	 * @param infoSourceId
	 * @param documentId
	 * @param predicate
	 * @param value
	 * 
	 * @return true if successfully added
	 */	
	public boolean addLiteralValues(String guid, String infoSourceId, String documentId, Map<String, Object> values) throws Exception {
		
		if(!getTable().exists(Bytes.toBytes(guid))){
			logger.error("Warning: unable to find row for GUID: "+guid);
			return false;
		}
		
		BatchUpdate batchUpdate = new BatchUpdate(guid);
		Iterator<String> keys = values.keySet().iterator();
		while(keys.hasNext()){
			String predicate = keys.next();
			
			//FIXMEremove the base URL, to make the column header more succicient
			URI uri = new URI(predicate);
			String fragment = uri.getFragment();
			if(fragment==null){
				fragment = uri.getPath();
				fragment = fragment.replace("/", "");
			}
			
			batchUpdate.put(RAW_COL_FAMILY+infoSourceId+":"+documentId+":"+fragment, Bytes.toBytes(values.get(predicate).toString()));
		}
		getTable().commit(batchUpdate);
		return true;
	}

	/**
	 * Retrieve the synonyms for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getSynonymsFor(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getSynonyms(row);
	}

	private List<TaxonConcept> getSynonyms(RowResult row) throws IOException,
			JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(SYNONYM_COL));
		return getTaxonConceptsFrom(cell);
	}
	
	/**
	 * Retrieve the images associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<Image> getImages(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getImages(row);
	}

	private List<Image> getImages(RowResult row) throws IOException,
			JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(IMAGE_COL));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<Image>>(){});
		} 
		return new ArrayList<Image>();
	}
	
	/**
	 * Retrieve the pest status associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<PestStatus> getPestStatuses(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getPestStatus(row);
	}

	private List<PestStatus> getPestStatus(RowResult row) throws IOException,
			JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(PEST_STATUS_COL));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<PestStatus>>(){});
		} 
		return new ArrayList<PestStatus>();
	}
	
	/**
	 * Retrieve the conservation status associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<ConservationStatus> getConservationStatuses(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getConservationStatus(row);
	}

	private List<ConservationStatus> getConservationStatus(RowResult row)
			throws IOException, JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(CONSERVATION_STATUS_COL));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<ConservationStatus>>(){});
		} 
		return new ArrayList<ConservationStatus>();
	}

	/**
	 * Deserialise the taxon concepts in this cell.
	 * 
	 * @param cell
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private List<TaxonConcept> getTaxonConceptsFrom(Cell cell)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<TaxonConcept>>(){});
		} 
		return new ArrayList<TaxonConcept>();
	}
	
	/**
	 * Deserialise the taxon concepts in this cell.
	 * 
	 * @param cell
	 * @return
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private List<CommonName> getCommonNamesFrom(Cell cell)
			throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<CommonName>>(){});
		} 
		return new ArrayList<CommonName>();
	}
	
	/**
	 * Retrieve the child concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getChildConcepts(row);
	}

	private List<TaxonConcept> getChildConcepts(RowResult row)
			throws IOException, JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(IS_CHILD_COL_OF));
		return getTaxonConceptsFrom(cell);
	}
	
	/**
	 * Retrieve the parent concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */	
	public List<TaxonConcept> getParentConceptsFor(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getParentConcepts(row);
	}

	private List<TaxonConcept> getParentConcepts(RowResult row)
			throws IOException, JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(IS_PARENT_COL_OF));
		return getTaxonConceptsFrom(cell);
	}		
	
	/**
	 * Retrieve the common names for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<CommonName> getCommonNamesFor(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(TC_COL_FAMILY)});
		return getCommonNames(row);
	}

	private List<CommonName> getCommonNames(RowResult row) throws IOException,
			JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(VERNACULAR_COL));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<CommonName>>(){});
		} 
		return new ArrayList<CommonName>();
	}

    /**
	 * Retrieve the text properties for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<SimpleProperty> getTextPropertiesFor(String guid) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		return getTextProperties(row);
	}

	private List<SimpleProperty> getTextProperties(RowResult row) throws IOException,
			JsonParseException, JsonMappingException {
		Cell cell = row.get(Bytes.toBytes(TEXT_PROPERTY_COL));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<SimpleProperty>>(){});
		}
		return new ArrayList<SimpleProperty>();
	}
	
	/**
	 * Store the following taxon concept
	 * 
	 * FIXME Switch to using a single column for TaxonConcept
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	public boolean create(TaxonConcept tc) throws Exception {
		if(tc.guid==null){
			throw new IllegalArgumentException("Supplied GUID for the Taxon Concept is null.");
		}
		
		BatchUpdate batchUpdate = new BatchUpdate(tc.guid.getBytes());
		putIfNotNull(batchUpdate, "tc:parentGuid", tc.parentGuid);
		putIfNotNull(batchUpdate, "tc:nameGuid", tc.nameGuid);
		putIfNotNull(batchUpdate, "tc:nameString", tc.nameString);
		putIfNotNull(batchUpdate, "tc:author", tc.author);
		putIfNotNull(batchUpdate, "tc:authorYear", tc.authorYear);
		putIfNotNull(batchUpdate, "tc:publishedIn", tc.publishedIn);
		putIfNotNull(batchUpdate, "tc:publishedInCitation", tc.publishedInCitation);
		putIfNotNull(batchUpdate, "tc:acceptedConceptGuid", tc.acceptedConceptGuid);
		getTable().commit(batchUpdate);	
		return true;
	}

	/**
	 * What about multiple taxon names for each taxon concept???
	 * 
	 * FIXME Switch to using a single column for TaxonName(s)
	 * 
	 * @param guid
	 * @param tn
	 * @throws Exception
	 */
	public void addTaxonName(String guid, TaxonName tn) throws Exception {
		RowResult row = getTable().getRow(Bytes.toBytes(guid));
		if(row==null){
			logger.error("Unable to locate a row to add taxon name to for guid: "+guid);
		}
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		putIfNotNull(batchUpdate, "tn:guid", tn.guid);
		putIfNotNull(batchUpdate, "tn:nameComplete", tn.nameComplete);
		putIfNotNull(batchUpdate, "tn:authorship", tn.authorship);
		putIfNotNull(batchUpdate, "tn:nomenclaturalCode", tn.nomenclaturalCode);
		putIfNotNull(batchUpdate, "tn:typificationString", tn.typificationString);
		putIfNotNull(batchUpdate, "tn:publishedInCitation", tn.publishedInCitation);
		putIfNotNull(batchUpdate, "tn:rankString", tn.rankString);
		getTable().commit(batchUpdate);
	}
	
	/**
	 * Add this common name to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addCommonName(String guid, CommonName commonName) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, VERNACULAR_COL, commonName, new TypeReference<List<CommonName>>(){});
	}
	
	/**
	 * Add this conservation status to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addConservationStatus(String guid, ConservationStatus conservationStatus) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, CONSERVATION_STATUS_COL, conservationStatus, new TypeReference<List<ConservationStatus>>(){});
	}
	
	/**
	 * Add this pest status to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addPestStatus(String guid, PestStatus pestStatus) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, PEST_STATUS_COL, pestStatus, new TypeReference<List<PestStatus>>(){});
	}
	
	/**
	 * Add this pest status to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addImage(String guid, Image image) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, IMAGE_COL, image, new TypeReference<List<Image>>(){});
	}
		
	
	/**
	 * Add a synonym to this concept.
	 * 
	 * @param guid
	 * @param synonym
	 * @throws Exception
	 */
	public void addSynonym(String guid, TaxonConcept synonym) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, SYNONYM_COL, synonym, new TypeReference<List<TaxonConcept>>(){});
	}	
	
	/**
	 * Add a child taxon to this concept.
	 * 
	 * @param guid
	 * @param childConcept
	 * @throws Exception
	 */
	public void addChildTaxon(String guid, TaxonConcept childConcept) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, IS_PARENT_COL_OF, childConcept, new TypeReference<List<TaxonConcept>>(){});
	}	
	
	/**
	 * Add a parent taxon to this concept.
	 * 
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	public void addParentTaxon(String guid, TaxonConcept parentConcept) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, TC_COL_FAMILY, IS_CHILD_COL_OF, parentConcept, new TypeReference<List<TaxonConcept>>(){});
	}

    /**
	 * Add a text property to this concept.
	 *
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	public void addTextProperty(String guid, SimpleProperty textProperty) throws Exception {
		HBaseDaoUtils.storeComplexObject(getTable(), guid, "tc:", TEXT_PROPERTY_COL, textProperty, new TypeReference<List<SimpleProperty>>(){});
	}
	
	/**
	 * Add field update to the batch if the supplied value is not null.
	 * 
	 * @param batchUpdate
	 * @param fieldName
	 * @param value
	 */
	private void putIfNotNull(BatchUpdate batchUpdate, String fieldName, String value) {
		value = StringUtils.trimToNull(value);
		if(value!=null){
			batchUpdate.put(fieldName, Bytes.toBytes(value));
		}
	}
	
	/**
	 * Create a batch of taxon concepts.
	 * 
	 * @param taxonConcepts
	 * @throws Exception
	 */
	public void create(List<TaxonConcept> taxonConcepts) throws Exception {
		for(TaxonConcept tc: taxonConcepts){
			create(tc);
		}
	}
	
	/**
	 * Retrieve the taxon concept by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaxonConcept getByGuid(String guid) throws Exception {
		RowResult rowResult = getTable().getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}
		return getTaxonConcept(guid, rowResult);
	}

	/**
	 * Retrieve the entire profile data for a taxon concept by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid) throws Exception {

		RowResult row = getTable().getRow(guid.getBytes());
		if(row==null){
			return null;
		}
		ExtendedTaxonConceptDTO etc = new ExtendedTaxonConceptDTO();
		
		//populate the dto
		etc.setTaxonConcept(getTaxonConcept(guid, row));
		etc.setTaxonName(getTaxonName(row));
		etc.setSynonyms(getSynonyms(row));
		etc.setCommonNames(getCommonNames(row));
		etc.setChildConcepts(getChildConcepts(row));
		etc.setParentConcepts(getParentConcepts(row));
		etc.setPestStatuses(getPestStatus(row));
		etc.setConservationStatuses(getConservationStatus(row));
		etc.setImages(getImages(row));
        etc.setTextProperties(getTextProperties(row));
		
		//add taxonomic properties
		
		//add descriptive data
		
		//add geospatial data
		
		return etc;
	}	
	
	/**
	 * Create a taxon concept from the row result.
	 * 
	 * FIXME Use serialised taxon concept ??
	 * 
	 * @param guid
	 * @param rowResult
	 * @return
	 */
	private TaxonConcept getTaxonConcept(String guid, RowResult rowResult) {
		TaxonConcept tc = new TaxonConcept();
		tc.guid = guid;
		tc.author = HBaseDaoUtils.getField(rowResult, "tc:author");
		tc.authorYear = HBaseDaoUtils.getField(rowResult, "tc:authorYear");
		tc.nameGuid = HBaseDaoUtils.getField(rowResult, "tc:hasName"); 
		tc.nameString = HBaseDaoUtils.getField(rowResult, "tc:nameString");
		tc.publishedIn = HBaseDaoUtils.getField(rowResult, "tc:publishedIn");
		tc.publishedInCitation = HBaseDaoUtils.getField(rowResult, "tc:publishedInCitation");
		return tc;
	}	
	
	/**
	 * Retrieve the Taxon Name for the supplied GUID.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception {
		RowResult rowResult = getTable().getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}
		return getTaxonName(rowResult);
	}

	private TaxonName getTaxonName(RowResult rowResult) {
		TaxonName tn = new TaxonName();
		tn.guid = HBaseDaoUtils.getField(rowResult, "tn:guid");
		tn.authorship = HBaseDaoUtils.getField(rowResult, "tn:authorship");
		tn.nameComplete = HBaseDaoUtils.getField(rowResult, "tn:nameComplete");
		tn.nomenclaturalCode = HBaseDaoUtils.getField(rowResult, "tn:nomenclaturalCode");
		tn.rankString = HBaseDaoUtils.getField(rowResult, "tn:rankString");
		tn.typificationString = HBaseDaoUtils.getField(rowResult, "tn:typificationString");
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
		RowResult rowResult = getTable().getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}
		
		//treemaps are sorted
		TreeMap<String, String> properties = new TreeMap<String,String>();
		
		Set<Map.Entry<byte[], Cell>> entrySet = rowResult.entrySet();
		Iterator<Map.Entry<byte[], Cell>> iter = entrySet.iterator();
		while(iter.hasNext()){
			Map.Entry<byte[], Cell> entry = iter.next();
			properties.put(new String(entry.getKey()), new String(entry.getValue().getValue()));
		}
		
		//sort by key
		return properties;
	}
	
	/**
	 * Search for taxon concept with the following scientific name.
	 * 
	 * @param scientificName
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<SearchTaxonConceptDTO> findByScientificName(String input, int limit) throws Exception {
        SearchResultsDTO sr = findByScientificName(input, 0, limit, null, null);
        return sr.getTaxonConcepts();
	}

    /**
     * Search for taxon concept with the following scientific name
     *
     * @param input
     * @param startIndex
     * @param pageSize
     * @param sortField
     * @param sortDirection
     * @return
     * @throws Exception
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
        
		IndexSearcher tcIdxSearcher1 = getTcIdxSearcher();
        
        boolean direction = false;
        if (sortDirection!=null && !sortDirection.isEmpty() && sortDirection.equalsIgnoreCase("desc")) {
            direction = true;
        }

        Sort sort = new Sort();
        if (sortField!=null && !sortField.isEmpty() && !sortField.equalsIgnoreCase("score")) {
            sort.setSort(sortField, direction);
        } else {
            sort = Sort.RELEVANCE;
        }

		//TopDocs topDocs = tcIdxSearcher1.search(scientificNameQuery, pageSize);
        TopDocs topDocs = tcIdxSearcher1.search(scientificNameQuery, null, startIndex+pageSize, sort); // TODO ues sortField here
		logger.debug("Total hits: "+topDocs.totalHits);

		List<SearchTaxonConceptDTO> tcs = new ArrayList<SearchTaxonConceptDTO>();
        
		// for (ScoreDoc scoreDoc: topDocs.scoreDocs) {
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

        return searchResults;
    }
	
	/**
	 * Get TaxonConcept GUID by genus and scientific name.
	 * 
	 * TODO replace this with the indexes generated from ChecklistBank
	 * 
	 * @param scientificName
	 * @return
	 * @throws Exception
	 */
	public String findConceptIDForName(String kingdom, String genus, String scientificName) throws Exception {
		try {
			String searchName = scientificName;

			//change A. bus to Aus bus if it is abbreviated
			if(abbreviatedCanonical.matcher(scientificName).matches() && genus!=null){
				NameParser np = new NameParser();
				
				ParsedName parsedName = np.parse(scientificName);
				
				if(parsedName!=null){
					if(parsedName.isBinomial()){
						searchName = genus +" "+ parsedName.getSpecificEpithet();
					}
				}
			}
			
			IndexSearcher is = getTcIdxSearcher();
			QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());
			Query q = qp.parse("\""+searchName.toLowerCase()+"\"");
			
			TopDocs topDocs = is.search(q, 20);
			
			for(ScoreDoc scoreDoc: topDocs.scoreDocs){
				Document doc = is.doc(scoreDoc.doc);
//				Field hasSynonym = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym");
//				if(hasSynonym!=null){
//					logger.debug("Returning synonym");
//					return hasSynonym.stringValue();
//				}
//				Field hasVernacular = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular");
//				if(hasVernacular!=null){
//					logger.debug("Returning vernacular");
//					return hasVernacular.stringValue();
//				}
//				Field isCongruentTo = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo");
//				if(isCongruentTo!=null){
//					logger.debug("Returning congruent");
//					return isCongruentTo.stringValue();
//				}
//			logger.debug("Doc Id: "+scoreDoc.doc);
//			logger.debug("Guid: "+doc.getField("guid").stringValue());
//			logger.debug("Name: "+doc.getField("scientificName").stringValue());
//			logger.debug("Raw name: "+doc.getField("scientificNameRaw").stringValue());
//			logger.debug("#################################");
				return doc.getField("guid").stringValue();
			}
		} catch (Exception e) {
			logger.error("Problem searching with:"+scientificName+" : "+e.getMessage());
		}		
		return null;
	}	
	
	/**
	 * Retrieve a list of concepts with the supplied parent guid.
	 * 
	 * @param parentGuid
	 * @param limit
	 * @return
	 * @throws Exception
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
        } catch (NumberFormatException numberFormatException) {
        }

		return taxonConcept;
	}

	/**
	 * Delete the TaxonConcept for the supplied guid
	 * 
	 * @param guid
	 * @return true if a delete was performed
	 * @throws Exception
	 */
	public boolean delete(String guid) throws Exception {
		if(getTable().exists(Bytes.toBytes(guid))){
			getTable().deleteAll(Bytes.toBytes(guid));
			return true;
		}
		return false;
	}

	/**
	 * Synchronises these triples to a taxon concept in hbase.
	 * 
	 * 
	 * @param triples
	 * @throws Exception
	 */
	public boolean syncTriples(String infoSourceId, String documentId, String dcSource,
            String dcPublisher,List<Triple> triples, String filePath) throws Exception {

		String scientificName = null;
		String species = null;
		String genus = null;
		String family = null;
		String order = null;
		String kingdom = null;
		
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
			if(predicate.endsWith("hasScientificName")){
				scientificName = triple.object;
			}
		}
		
		if(scientificName==null
				&& species==null
				&& genus==null
				&& family==null
				&& order==null){
			logger.error("No classification found");
			return false; //we have nothing to work with, so give up
		}
		
		//FIXME - this should be replaced with improved sci name lookup coming out of the 
		//checklistbank works
		
		String guid = findConceptIDForName(kingdom, genus, scientificName);
		//if null try with the species name
		if(guid==null){
			guid = findConceptIDForName(kingdom, genus, species);
		}
		
		if(guid!=null){
			
			Map<String, Object> properties = new HashMap<String,Object>();
			
			for(Triple triple: triples){
				
				logger.trace(triple.predicate);
				
				//check here for predicates of complex objects
				if(triple.predicate.endsWith("hasCommonName")){
					
					CommonName commonName = new CommonName();
					commonName.setNameString(triple.object);
					commonName.setInfoSourceId(infoSourceId);
					commonName.setDocumentId(documentId);
                    commonName.setInfoSourceName(dcPublisher);
                    commonName.setInfoSourceURL(dcSource);
					addCommonName(guid, commonName);
					
				} else if(triple.predicate.endsWith("hasConservationStatus")){
					
					//FIXME At this stage we need to do a vocabulary lookup to rationalise the 
					// conservation status
					
					ConservationStatus cs = vocabulary.getConservationStatusFor(Integer.parseInt(infoSourceId), triple.object);
					if(cs==null){
						ConservationStatus conservationStatus = new ConservationStatus();
						conservationStatus.setStatus(triple.object);
					}

					cs.setInfoSourceId(infoSourceId);
					cs.setDocumentId(documentId);
                    cs.setInfoSourceName(dcPublisher);
                    cs.setInfoSourceURL(dcSource);
					addConservationStatus(guid, cs);
					
				} else if(triple.predicate.endsWith("hasPestStatus")){

					//FIXME At this stage we need to do a vocabulary lookup to rationalise the 
					// pest status
					
					PestStatus ps = vocabulary.getPestStatusFor(Integer.parseInt(infoSourceId), triple.object);
					if(ps==null){
						PestStatus pestStatus = new PestStatus();
						pestStatus.setStatus(triple.object);
					}
					
					ps.setInfoSourceId(infoSourceId);
					ps.setDocumentId(documentId);
                    ps.setInfoSourceName(dcPublisher);
                    ps.setInfoSourceURL(dcSource);
					addPestStatus(guid, ps);
					
                //} else if (triple.predicate.endsWith("Text")) {
                } else {    

                    SimpleProperty simpleProperty = new SimpleProperty();
                    simpleProperty.setName(triple.predicate);
                    simpleProperty.setValue(triple.object);
                    simpleProperty.setInfoSourceName(dcPublisher);
                    simpleProperty.setInfoSourceURL(dcSource);
                    addTextProperty(guid, simpleProperty);

                }
//                } else {
//					properties.put(triple.predicate, triple.object);
//				}
			}
			
			//retrieve the content type
			if(filePath!=null){
				String contentType = DublinCoreUtils.getContentType(filePath);
				
				//is it an image ???
				if(contentType!=null && MimeType.getImageMimeTypes().contains(contentType)){
					Image image = new Image();
					image.setContentType(contentType);
					image.setRepoLocation(filePath
							+File.separator
							+FileType.RAW.getFilename()
							+MimeType.getFileExtension(contentType));
					image.setInfoSourceId(infoSourceId);
					image.setDocumentId(documentId);
                    image.setInfoSourceName(dcPublisher);
                    image.setInfoSourceURL(dcSource);
					addImage(guid, image);
				}
			}
			
			logger.debug("Adding content to: "+guid+", using scientific name: "+scientificName+", genus: "+genus);
			addLiteralValues(guid, infoSourceId, documentId, properties);
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Clear the associated properties from each taxon concept.
	 * 
	 * Clear the triples in the "raw:" column family.
	 * 
	 * @throws Exception
	 */
	public void clearRawProperties() throws Exception {
		
    	Scanner scanner = getTable().getScanner(new String[]{TC_COL_FAMILY});
    	Iterator<RowResult> iter = scanner.iterator();
    	int i=0;
    	while(iter.hasNext()){
    		RowResult rowResult = iter.next();
    		byte[] row = rowResult.getRow();
    		getTable().deleteFamily(row, Bytes.toBytes(RAW_COL_FAMILY));
    		getTable().deleteAllByRegex(new String(row), CONSERVATION_STATUS_COL);
    		getTable().deleteAllByRegex(new String(row), PEST_STATUS_COL);
    		getTable().deleteAllByRegex(new String(row), IMAGE_COL);
    		getTable().deleteAllByRegex(new String(row), "tc:[0-9]{1,}:[0-9]{1,}hasScientificName");
    		
    		logger.debug(++i + " " + (new String(row)));
    	}
    	logger.debug("Raw triples cleared");
	}
	
	/**
	 * Create a index to support searching.
	 * 
	 * @throws Exception
	 */
	public void createIndex() throws Exception {
		
		long start = System.currentTimeMillis();
		
    	File file = new File(TC_INDEX_DIR);
    	if(file.exists()){
    		FileUtils.forceDelete(file);
    	}
    	FileUtils.forceMkdir(file);
    	
    	//Analyzer analyzer = new KeywordAnalyzer(); - works for exact matches
    	//KeywordAnalyzer analyzer = new KeywordAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();

    	IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
		
    	Scanner scanner = getTable().getScanner(new String[]{TC_COL_FAMILY});
    	Iterator<RowResult> iter = scanner.iterator();
    	int i=0;
    	while(iter.hasNext()){
    		i++;
    		RowResult rowResult = iter.next();
    		byte[] row = rowResult.getRow();
    		String guid = new String(row);

    		//get taxon concept details
    		TaxonConcept taxonConcept = getTaxonConcept(guid, rowResult);
            
            // get taxon name
            TaxonName taxonName = getTaxonNameFor(guid);

            //get synonyms
    		Cell synonymsCell = rowResult.get(Bytes.toBytes(SYNONYM_COL));
    		List<TaxonConcept> synonyms = getTaxonConceptsFrom(synonymsCell);
    		
    		//get common names
    		Cell commonNamesCell = rowResult.get(Bytes.toBytes(VERNACULAR_COL));
    		List<CommonName> commonNames = getCommonNamesFrom(commonNamesCell);
    		
    		Cell childrenCell = rowResult.get(Bytes.toBytes(IS_CHILD_COL_OF));
    		List<TaxonConcept> children = getTaxonConceptsFrom(childrenCell);
    		
    		//create the lucene doc
    		
    		//TODO this index should also include nub ids
    		Document doc = new Document();
    		if(taxonConcept.nameString!=null){
    			
    			doc.add(new Field("guid", taxonConcept.guid, Store.YES, Index.NO));
    			
    			//add multiple forms of the scientific name to the index
    			LuceneUtils.addScientificNameToIndex(doc, taxonConcept.nameString);
	    		
	    		if(taxonConcept.parentGuid!=null){
	    			doc.add(new Field("parentGuid", taxonConcept.parentGuid, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
	    		}
	    		
	    		for(TaxonConcept tc: synonyms){
	    			if(tc.nameString!=null){
	    				doc.add(new Field("synonym", tc.nameString.toLowerCase(), Store.YES, Index.ANALYZED));
	    			}
	    		}

                //StringBuffer cnStr = new StringBuffer();
                TreeSet<String> commonNameSet = new TreeSet<String>();

	    		for(CommonName cn: commonNames){
	    			if(cn.nameString!=null){
	    				commonNameSet.add(cn.nameString.toLowerCase());
                        //doc.add(new Field("commonName", cn.nameString.toLowerCase(), Store.YES, Index.ANALYZED));
                        //cnStr.append(cn.nameString.toLowerCase() + " ");
	    			}
	    		}

                if (commonNameSet.size() > 0) {
                    String commonNamesConcat = StringUtils.deleteWhitespace(StringUtils.join(commonNameSet, " "));
                    doc.add(new Field("commonNameSort", commonNamesConcat, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                    doc.add(new Field("commonName", StringUtils.join(commonNameSet, " "), Store.YES, Index.ANALYZED));
                }

                if (taxonName.getRankString()!=null) {
                    try {
                        Rank rank = Rank.getForField(taxonName.getRankString().toLowerCase());
                        doc.add(new Field("rank", rank.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                        doc.add(new Field("rankId", rank.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                    } catch (Exception e) {
                        logger.error("Rank not found: "+taxonName.getRankString()+" - "+e.getMessage());
                        // assign to Rank.TAXSUPRAGEN so that sorting still works reliably
                        doc.add(new Field("rank", Rank.TAXSUPRAGEN.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                        doc.add(new Field("rankId", Rank.TAXSUPRAGEN.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                    }
                }
	    		
    			doc.add(new Field("hasChildren", Boolean.toString(!children.isEmpty()), Store.YES, Index.NO));
	    		
		    	//add to index
		    	iw.addDocument(doc, analyzer);
    		}
	    	if(i%10000==0){
	    		iw.commit();
	    	}
	    	
    		if (i%100==0) logger.debug(i + " " + guid);
    	}
    	
    	iw.commit();
    	iw.close();
    	
    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds.");
	}

	/**
	 * @param vocabulary the vocabulary to set
	 */
	public void setVocabulary(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}
}