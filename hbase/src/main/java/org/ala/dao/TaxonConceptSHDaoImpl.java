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
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.dto.SpeciesProfileDTO;
import org.ala.model.AttributableObject;
import org.ala.model.BaseRanking;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.IdentificationKey;
import org.ala.model.Image;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.model.PestStatus;
import org.ala.model.Publication;
import org.ala.model.Rank;
import org.ala.model.Rankable;
import org.ala.model.Reference;
import org.ala.model.SensitiveStatus;
import org.ala.model.SimpleProperty;
import org.ala.model.SpecimenHolding;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
import org.ala.repository.Predicates;
import org.ala.util.ColumnType;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.springframework.stereotype.Component;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.HomonymException;
import au.org.ala.checklist.lucene.SearchResultException;
import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.model.LinnaeanRankClassification;
import au.org.ala.data.util.RankType;

import org.ala.util.RankingType;
/**
 * Database agnostic implementation if Taxon concept DAO.
 * 
 * This implementation hands off java objects to an instance of
 * <code>StoreHelper</code> that hides the complexities of the underlying
 * datastore in use.
 * 
 * @see StoreHelper
 * @see HBaseHelper
 * @see CassandraHelper
 * 
 * @author Dave Martin
 */
@Component("taxonConceptDao")
public class TaxonConceptSHDaoImpl implements TaxonConceptDao {

	static Logger logger = Logger.getLogger(TaxonConceptSHDaoImpl.class);

	/** FIXME To be moved to somewhere more maintainable */
	protected static List<String> regionList = null;
	protected static Set<String> fishTaxa = null;
	static {
		regionList = new ArrayList<String>();
		regionList.add("New South Wales");
		regionList.add("Victoria");
		regionList.add("Tasmania");
		regionList.add("Northern Territory");
		regionList.add("South Australia");
		regionList.add("Queensland");
		regionList.add("Australian Capital Territory");
		regionList.add("Western Australia");
		regionList.add("Australia");
		
		fishTaxa = new HashSet<String>();
		fishTaxa.add("Myxini".toLowerCase());
		fishTaxa.add("Chondrichthyes".toLowerCase());
		fishTaxa.add("Sarcopterygii".toLowerCase());
		fishTaxa.add("Actinopterygii".toLowerCase());
	}

	/** The location for the lucene index */
	public static final String TC_INDEX_DIR = "/data/solr/bie/index";

	/** Column families */
	private static final String TC_COL_FAMILY = "tc";

	/** The table name */
	private static final String TC_TABLE = "taxonConcept";

	@Inject
	protected Vocabulary vocabulary;

	protected IndexSearcher tcIdxSearcher;

	@Inject
	protected CBIndexSearch cbIdxSearcher;

	/** The spring wired store helper to use */
	protected StoreHelper storeHelper;

	@Inject
	protected SolrUtils solrUtils;

	/* Final fields */
	protected static final String DATASET = "dataset";
	protected static final String SCI_NAME = "scientificName";
	protected static final String SCI_NAME_RAW = "scientificNameRaw";
	protected static final String SCI_NAME_TEXT = "scientificNameText";

    /* stores the statistics to write to file */
    protected int anbgMatched;
    protected int otherMatched;
    protected int failedMatch;
    protected int homonyms;

	/**
	 * Initialise the DAO, setting up the HTable instance.
	 * 
	 * @throws Exception
	 */
	public TaxonConceptSHDaoImpl() throws Exception {}

	/**
	 * Initialise the connection to HBase
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		storeHelper.init();
	}

	/**
	 * FIXME Switch to using a single column for TaxonConcept
	 * 
	 * @see org.ala.dao.TaxonConceptDao#create(org.ala.model.TaxonConcept)
	 */
	public boolean create(TaxonConcept tc) throws Exception {
		if (tc == null) {
			throw new IllegalArgumentException(
					"Supplied TaxonConcept was null.");
		}

		if (tc.getGuid() == null) {
			throw new IllegalArgumentException(
					"Supplied TaxonConcept has a null Guid value.");
		}

		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName(),
				tc.getGuid(), tc);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#update(org.ala.model.TaxonConcept)
	 */
	public boolean update(TaxonConcept tc) throws Exception {

		if (tc == null) {
			return false;
		}

		if (tc != null && tc.getGuid() == null) {
			throw new IllegalArgumentException(
					"Supplied GUID for the Taxon Concept is null.");
		}

		// FIXME this is here to update some information not available in the
		// export from checklist bank
		// This should refactored out at some stage
		TaxonConcept current = (TaxonConcept) storeHelper.get(TC_TABLE,
				TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName(),
				tc.getGuid(), TaxonConcept.class);
		if (current == null) {
			return false;
		}
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName(),
				tc.getGuid(), current);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addTaxonName(java.lang.String,
	 *      org.ala.model.TaxonName)
	 */
	public boolean addTaxonName(String guid, TaxonName taxonName)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONNAME_COL.getColumnName(), guid,
				taxonName);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addCommonName(java.lang.String,
	 *      org.ala.model.CommonName)
	 */
	public boolean addCommonName(String guid, CommonName commonName)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.VERNACULAR_COL.getColumnName(), guid,
				commonName);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addConservationStatus(java.lang.String,
	 *      org.ala.model.ConservationStatus)
	 */
	public boolean addConservationStatus(String guid,
			ConservationStatus conservationStatus) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.CONSERVATION_STATUS_COL.getColumnName(),
				guid, conservationStatus);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addPestStatus(java.lang.String,
	 *      org.ala.model.PestStatus)
	 */
	public boolean addPestStatus(String guid, PestStatus pestStatus)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.PEST_STATUS_COL.getColumnName(), guid,
				pestStatus);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addRegions(java.lang.String,
	 *      java.util.List)
	 */
	public boolean addRegions(String guid, List<OccurrencesInGeoregion> regions)
			throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.REGION_COL.getColumnName(), guid,
				(List) regions, false);
	}

	public boolean setOccurrenceRecordsCount(String guid, Integer count)
			throws Exception {
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.OCCURRENCE_RECORDS_COUNT_COL
						.getColumnName(), guid, count);
	}

	public boolean setGeoreferencedRecordsCount(String guid, Integer count)
			throws Exception {
		return storeHelper
				.putSingle(TC_TABLE, TC_COL_FAMILY,
						ColumnType.GEOREF_RECORDS_COUNT_COL
								.getColumnName(), guid, count);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addImage(java.lang.String,
	 *      org.ala.model.Image)
	 */
	public boolean addImage(String guid, Image image) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IMAGE_COL.getColumnName(), guid, image);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addDistributionImage(java.lang.String,
	 *      org.ala.model.Image)
	 */
	public boolean addDistributionImage(String guid, Image image)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.DIST_IMAGE_COL.getColumnName(), guid,
				image);
	}
	
	public boolean addScreenshotImage(String guid, Image image)
	        throws Exception {
	    System.out.println("!!!!!ADDING SCREENSHOT TO GUID: " + guid);
	    
	    return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
	            ColumnType.SCREENSHOT_IMAGE_COL.getColumnName(), guid,
	            image);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addSynonym(java.lang.String,
	 *      org.ala.model.TaxonConcept)
	 */
	public boolean addSynonym(String guid, TaxonConcept synonym)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SYNONYM_COL.getColumnName(), guid,
				synonym);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addIsCongruentTo(java.lang.String,
	 *      org.ala.model.TaxonConcept)
	 */
	public boolean addIsCongruentTo(String guid, TaxonConcept congruent)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_CONGRUENT_TO_COL.getColumnName(),
				guid, congruent);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addChildTaxon(java.lang.String,
	 *      org.ala.model.TaxonConcept)
	 */
	public boolean addChildTaxon(String guid, TaxonConcept childConcept)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_PARENT_COL_OF.getColumnName(), guid,
				childConcept);
	}

	/**
	 * @param guid
	 * @param childConcepts
	 * @return
	 * @throws Exception
	 */
	public boolean setChildTaxa(String guid, List<TaxonConcept> childConcepts)
			throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_PARENT_COL_OF.getColumnName(), guid,
				(List) childConcepts, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addIdentifier(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean addIdentifier(String guid, String alternativeIdentifier)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IDENTIFIER_COL.getColumnName(), guid,
				alternativeIdentifier);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getIdentifiers(java.lang.String)
	 */
	public List<String> getIdentifiers(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IDENTIFIER_COL.getColumnName(), guid,
				String.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addParentTaxon(java.lang.String,
	 *      org.ala.model.TaxonConcept)
	 */
	public boolean addParentTaxon(String guid, TaxonConcept parentConcept)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_CHILD_COL_OF.getColumnName(), guid,
				parentConcept);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addTextProperty(java.lang.String,
	 *      org.ala.model.SimpleProperty)
	 */
	public boolean addTextProperty(String guid, SimpleProperty textProperty)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TEXT_PROPERTY_COL.getColumnName(), guid,
				textProperty);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addTextProperty(java.lang.String,
	 *      org.ala.model.SimpleProperty)
	 */
	public boolean setIsIconic(String guid) throws Exception {
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_ICONIC.getColumnName(), guid, true);
	}

	public boolean setIsAustralian(String guid) throws Exception {
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_AUSTRALIAN.getColumnName(), guid,
				true);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getSynonymsFor(java.lang.String)
	 */
	public List<TaxonConcept> getSynonymsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SYNONYM_COL.getColumnName(), guid,
				TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getSynonymsFor(java.lang.String)
	 */
	public List<TaxonConcept> getCongruentConceptsFor(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_CONGRUENT_TO_COL.getColumnName(),
				guid, TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getImages(java.lang.String)
	 */
	public List<Image> getImages(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IMAGE_COL.getColumnName(), guid,
				Image.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#isIconic(java.lang.String)
	 */
	public boolean isIconic(String guid) throws Exception {
		Boolean isIconic = (Boolean) storeHelper.get(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_ICONIC.getColumnName(), guid,
				Boolean.class);
		if (isIconic == null) {
			return false;
		}
		return isIconic;
	}

	public boolean isAustralian(String guid) throws Exception {
		Boolean isAustralian = (Boolean) storeHelper.get(TC_TABLE,
				TC_COL_FAMILY,
				ColumnType.IS_AUSTRALIAN.getColumnName(), guid,
				Boolean.class);
		if (isAustralian == null)
			return false;
		return isAustralian;
	}

	public String getLinkIdentifier(String guid) throws Exception {
		String linkIdentifier = storeHelper.getStringValue(TC_TABLE,
				TC_COL_FAMILY,
				ColumnType.LINK_IDENTIFIER.getColumnName(), guid);
		return linkIdentifier;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getDistributionImages(java.lang.String)
	 */
	public List<Image> getDistributionImages(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.DIST_IMAGE_COL.getColumnName(), guid,
				Image.class);
	}
	
	public List<Image> getScreenshotImages(String guid) throws Exception {
        return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
                ColumnType.SCREENSHOT_IMAGE_COL.getColumnName(), guid,
                Image.class);
    }

	/**
	 * @see org.ala.dao.TaxonConceptDao#getPestStatuses(java.lang.String)
	 */
	public List<PestStatus> getPestStatuses(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.PEST_STATUS_COL.getColumnName(), guid,
				PestStatus.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getConservationStatuses(java.lang.String)
	 */
	public List<ConservationStatus> getConservationStatuses(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.CONSERVATION_STATUS_COL.getColumnName(),
				guid, ConservationStatus.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getChildConceptsFor(java.lang.String)
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_PARENT_COL_OF.getColumnName(), guid,
				TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getParentConceptsFor(java.lang.String)
	 */
	public List<TaxonConcept> getParentConceptsFor(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IS_CHILD_COL_OF.getColumnName(), guid,
				TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getCommonNamesFor(java.lang.String)
	 */
	public List<CommonName> getCommonNamesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.VERNACULAR_COL.getColumnName(), guid,
				CommonName.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getTextPropertiesFor(java.lang.String)
	 */
	public List<SimpleProperty> getTextPropertiesFor(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TEXT_PROPERTY_COL.getColumnName(), guid,
				SimpleProperty.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#create(java.util.List)
	 */
	public void create(List<TaxonConcept> taxonConcepts) throws Exception {
		for (TaxonConcept tc : taxonConcepts) {
			create(tc);
		}
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getByGuid(java.lang.String)
	 */
	public TaxonConcept getByGuid(String guid) throws Exception {
		return (TaxonConcept) storeHelper.get(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName(), guid,
				TaxonConcept.class);
	}

	/**
	 * Use the Lucene indexes to find the correct (accepted) guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public String getPreferredGuid(String guid) throws Exception {
		// use the Lucene indexes to find the correct (accepted) guid.
		SearchResultsDTO<SearchTaxonConceptDTO> searchResults = findByGuid(
				guid, 0, 1, null, null);
		if (!searchResults.getResults().isEmpty()) {
			guid = searchResults.getResults().get(0).getGuid();
		}
		return guid;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getTaxonNameFor(java.lang.String)
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception {
		List taxonNames = storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONNAME_COL.getColumnName(), guid,
				TaxonName.class);
		if (taxonNames.isEmpty()) {
			return null;
		} else {
			return (TaxonName) taxonNames.get(0);
		}
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getTaxonNameFor(java.lang.String)
	 */
	public List<TaxonName> getTaxonNamesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONNAME_COL.getColumnName(), guid,
				TaxonName.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String,
	 *      int)
	 */
	public List<SearchTaxonConceptDTO> findByScientificName(String input,
			int limit) throws Exception {
		SearchResultsDTO<SearchTaxonConceptDTO> sr = findByScientificName(
				input, 0, limit, null, null);
		return sr.getResults();
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String,
	 *      java.lang.Integer, java.lang.Integer, java.lang.String,
	 *      java.lang.String)
	 */
	public SearchResultsDTO findByScientificName(String input,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws Exception {

		input = StringUtils.trimToNull(input);
		if (input == null) {
			return new SearchResultsDTO();
		}

		// lower case everything
		input = input.toLowerCase();

		// construct the query for scientific name
		QueryParser qp = new QueryParser("scientificName",
				new KeywordAnalyzer());
		Query scientificNameQuery = qp.parse("\"" + input + "\"");

		// construct the query for scientific name
		qp = new QueryParser("commonName", new SimpleAnalyzer());
		Query commonNameQuery = qp.parse("\"" + input + "\"");

		// include a query against the GUIDs
		Query guidQuery = new TermQuery(new Term("guid", input));
		Query otherGuidQuery = new TermQuery(new Term("otherGuid", input));

		// combine the query terms
		scientificNameQuery = scientificNameQuery
				.combine(new Query[] { scientificNameQuery, guidQuery,
						otherGuidQuery, commonNameQuery });
		// run the search
		return sortPageSearch(scientificNameQuery, startIndex, pageSize,
				sortField, sortDirection);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String,
	 *      java.lang.Integer, java.lang.Integer, java.lang.String,
	 *      java.lang.String)
	 */
	public SearchResultsDTO findByGuid(String input, Integer startIndex,
			Integer pageSize, String sortField, String sortDirection)
			throws Exception {

		input = StringUtils.trimToNull(input);
		if (input == null) {
			return new SearchResultsDTO();
		}

		// include a query against the GUIDs
		Query guidQuery = new TermQuery(new Term("guid", input));
		Query otherGuidQuery = new TermQuery(new Term("otherGuid", input));

		// combine the query terms
		Query fullQuery = guidQuery.combine(new Query[] { guidQuery,
				otherGuidQuery });
		// run the search
		return sortPageSearch(fullQuery, startIndex, pageSize, sortField,
				sortDirection);
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
	private SearchResultsDTO sortPageSearch(Query searchQuery,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws IOException, Exception {
		boolean direction = false;

		if (sortDirection != null && !sortDirection.isEmpty()
				&& sortDirection.equalsIgnoreCase("desc")) {
			direction = true;
		}

		Sort sort = new Sort();

		if (sortField != null && !sortField.isEmpty()
				&& !sortField.equalsIgnoreCase("score")) {
			sort.setSort(sortField, direction);
		} else {
			sort = Sort.RELEVANCE;
		}

		TopDocs topDocs = getTcIdxSearcher().search(searchQuery, null,
				startIndex + pageSize, sort); // TODO ues sortField here
		logger.debug("Total hits: " + topDocs.totalHits);
		List<SearchTaxonConceptDTO> tcs = new ArrayList<SearchTaxonConceptDTO>();

		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
			if (i >= startIndex) {
				ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				Document doc = getTcIdxSearcher().doc(scoreDoc.doc);
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
	 * @see org.ala.dao.TaxonConceptDao#findAllByStatus(org.ala.util.StatusType,
	 *      java.lang.Integer, java.lang.Integer, java.lang.String,
	 *      java.lang.String)
	 */
	public SearchResultsDTO findAllByStatus(StatusType statusType,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws ParseException, Exception {
		// List<TermQuery> statusTerms = new ArrayList<TermQuery>();
		// IndexSearcher tcIdxSearcher1 = getTcIdxSearcher();
		// TermEnum terms = tcIdxSearcher1.getIndexReader().terms(new
		// Term(statusType.toString(), ""));
		//
		// while (statusType.toString().equals(terms.term().field())) {
		// statusTerms.add(new TermQuery(new Term(statusType.toString(),
		// terms.term().text())));
		// if (!terms.next()) {
		// break;
		// }
		// }

		List<String> statusTerms = vocabulary.getTermsForStatusType(statusType);

		String query = StringUtils.join(statusTerms, "|");
		System.out.println(statusType + " query = " + query + ".");
		BooleanQuery searchQuery = new BooleanQuery();

		for (String st : statusTerms) {
			searchQuery.add(new TermQuery(new Term(statusType.toString(), st)),
					BooleanClause.Occur.SHOULD);
		}
		System.out.println("search query = " + searchQuery.toString());
		return sortPageSearch(searchQuery, startIndex, pageSize, sortField,
				sortDirection);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findLsidByName(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String findLsidByName(String scientificName,
			LinnaeanRankClassification classification, String taxonRank) {
		String lsid = null;
                boolean homonym = false;
		try {
			// System.out.println("Get LSID for sci name: " + scientificName +
			// ", and rank: " + taxonRank);
			lsid = cbIdxSearcher.searchForLSID(scientificName, classification,
					RankType.getForName(taxonRank));
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception (" + scientificName
					+ ") - " + e.getMessage() + e.getResults());
                        homonym = e instanceof HomonymException;
		}
                updateStats(lsid, homonym);
		return lsid;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findLsidByName(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public String findLsidByName(String scientificName, String taxonRank) {
		String lsid = null;
                boolean homonym = false;
		try {
			lsid = cbIdxSearcher.searchForLSID(scientificName,
					RankType.getForName(taxonRank));
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception - " + e.getMessage()
					+ e.getResults());
                        homonym = e instanceof HomonymException;
		}
                updateStats(lsid, homonym);
		return lsid;
	}

	public String findLsidByName(String scientificName) {
		String lsid = null;
                boolean homonym = false;
		try {
			lsid = cbIdxSearcher.searchForLSID(scientificName);
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception - " + e.getMessage()
					+ e.getResults());
                        homonym = e instanceof HomonymException;
		}
                updateStats(lsid, homonym);
		return lsid;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#findCBDataByName(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public NameSearchResult findCBDataByName(String scientificName,
			LinnaeanRankClassification classification, String rank)
			throws SearchResultException {
		return cbIdxSearcher.searchForRecord(scientificName, classification,
				RankType.getForName(rank));
	}

    /**
     * @see org.ala.dao.TaxonConceptDao#reportStats(java.io.OutputStream, java.lang.String, java.lang.String) 
     */
    public void reportStats(java.io.OutputStream output, String prefix) throws Exception{
        String line = prefix + "," + anbgMatched+"," +otherMatched + "," + failedMatch+"," +homonyms+"\n";
        output.write(line.getBytes());
        
    }
    /**
     * update the name matching statistics based on the supplied lsid
     * @param lsid
     */
    private void updateStats(String lsid, boolean isHomonym){
        if(isHomonym)
            homonyms++;
        else if(lsid == null)
            failedMatch++;
        else if(lsid.startsWith("urn:lsid:biodiversity.org.au"))
            anbgMatched++;
        else
            otherMatched++;
    }
    /**
     * @see org.ala.dao.TaxonConceptDao#resetStats() 
     */
    public void resetStats(){
        anbgMatched = 0;
        otherMatched = 0;
        failedMatch = 0;
        homonyms = 0;
    }

	/**
	 * @see org.ala.dao.TaxonConceptDao#getByParentGuid(java.lang.String, int)
	 */
	public List<SearchTaxonConceptDTO> getByParentGuid(String parentGuid,
			int limit) throws Exception {
		if (parentGuid == null) {
			parentGuid = "NULL";
		}
		return searchTaxonConceptIndexBy("parentGuid", parentGuid, limit);
	}

	public boolean setRankingOnImage(String guid, String imageUri,
			boolean positive) throws Exception {
		return setRankingOnImage(guid, imageUri, positive, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#setRankingOnImage(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public boolean setRankingOnImage(String guid, String imageUri,
			boolean positive, boolean blackList) throws Exception {
		List<Image> images = getImages(guid);
		
		for (Image image : images) {
			//TODO cassandra stored uri has escape charater, Single point fix should be
			// decode all escape charater inside cassandra.
			String decode = URLDecoder.decode(image.getIdentifier(), "UTF-8");
			logger.debug("setRankingOnImage(..) imageUri: " + imageUri + " , decode: " + decode);
			if (imageUri.equals(decode)) {
				logger.debug("setRankingOnImage(..) found image !!!");
				if(blackList){
					image.setIsBlackListed(blackList);
					// currently cassandra have duplicated image, 
					// TODO remove duplcate data in cassendra can put the break statement back'
					// break;
				}
				else{
					Integer ranking = image.getRanking();
					Integer noOfRankings = image.getNoOfRankings();
					if (ranking == null) {
						if (positive) {
							ranking = new Integer(1);
						} else {
							ranking = new Integer(-1);
						}
						noOfRankings = new Integer(1);
					} else {
						if (positive) {
							ranking++;
						} else {
							ranking--;
						}
						noOfRankings++;
					}
					image.setRanking(ranking);				
					image.setNoOfRankings(noOfRankings);
					
					// TODO remove duplcate data in cassendra can put the break statement back'
					// break;					
				}
			}			
		}

		// re-sort based on the rankings
		Collections.sort(images);

		logger.debug("setRankingOnImage(..) save to Cassandta !!!");
		// write back to database
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IMAGE_COL.getColumnName(), guid,
				(List) images, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#setRankingOnImages(java.lang.String,
	 *      java.util.Map)
	 */
	public boolean setRankingOnImages(String guid,
			Map<String, Integer[]> rankings) throws Exception {
		// get the list of available images for the guid
		List<Image> images = getImages(guid);
		// for each image check if it has a ranking
		for (Image image : images) {
			Integer[] rank = rankings.get(image.getIdentifier());
			if (rank != null) {
				image.setNoOfRankings(rank[0]);
				image.setRanking(rank[1]);
				System.out.println("Reinitialising the ranking for "
						+ image.getIdentifier());

			}
		}

		// re-sort based on the new rankings
		Collections.sort(images);

		// write back to the database
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IMAGE_COL.getColumnName(), guid,
				(List) images, false);
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
	private List<SearchTaxonConceptDTO> searchTaxonConceptIndexBy(
			String columnName, String value, int limit) throws Exception {
		Query query = new TermQuery(new Term(columnName, value));
		IndexSearcher tcIdxSearcher = getTcIdxSearcher();
		TopDocs topDocs = tcIdxSearcher.search(query, limit);
		List<SearchTaxonConceptDTO> tcs = new ArrayList<SearchTaxonConceptDTO>();
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = tcIdxSearcher.doc(scoreDoc.doc);
			tcs.add(createTaxonConceptFromIndex(doc, scoreDoc.score));
		}
		return tcs;
	}

	/**
	 * Retrieves the index search for taxon concepts, initialising if necessary.
	 * 
	 * @return
	 * @throws Exception
	 */
	private IndexSearcher getTcIdxSearcher() throws Exception {
		// FIXME move to dependency injection
		if (this.tcIdxSearcher == null) {
			File file = new File(TC_INDEX_DIR);
			if (file.exists()) {
				try {
					this.tcIdxSearcher = new IndexSearcher(TC_INDEX_DIR);
				} catch (Exception e) {
					new IndexWriter(TC_INDEX_DIR, new StandardAnalyzer());
				}
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
	private SearchTaxonConceptDTO createTaxonConceptFromIndex(Document doc,
			float score) {
		SearchTaxonConceptDTO taxonConcept = new SearchTaxonConceptDTO();
		taxonConcept.setGuid(doc.get("guid"));
		taxonConcept.setParentGuid(doc.get("parentGuid"));
		if (doc.get("parentId") != null) {
			taxonConcept.setParentId(doc.get("parentId"));
		}
		taxonConcept.setName(doc.get("scientificNameRaw"));
		taxonConcept.setAcceptedConceptName(doc.get("acceptedConceptName"));
		String hasChildrenAsString = doc.get("hasChildren");

		String[] commonNames = doc.getValues("commonName");
		if (commonNames.length > 0) {
			taxonConcept.setCommonName(commonNames[0]);
		}

		taxonConcept.setHasChildren(Boolean.parseBoolean(hasChildrenAsString));
		taxonConcept.setScore(score);
		taxonConcept.setRank(doc.get("rank"));
		try {
			taxonConcept.setLeft(Integer.parseInt(doc.get("left")));
			taxonConcept.setRight(Integer.parseInt(doc.get("right")));
		} catch (NumberFormatException e) {
			// expected if left and right values are unavailable
		}

		try {
			taxonConcept.setRankId(Integer.parseInt(doc.get("rankId")));
		} catch (NumberFormatException ex) {
			logger.error("Error parsing rankId: " + ex.getMessage() + " for taxon concept : " + taxonConcept.getGuid());
		}
		taxonConcept.setPestStatus(doc.get(StatusType.PEST.toString()));
		taxonConcept.setConservationStatus(doc.get(StatusType.CONSERVATION
				.toString()));

		// add image detais
		taxonConcept.setImage(doc.get("image"));
		taxonConcept.setThumbnail(doc.get("thumbnail"));
		return taxonConcept;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#delete(java.lang.String)
	 */
	public boolean delete(String guid) throws Exception {

		// TODO add to interface
		// if (getTable().exists(new Get(Bytes.toBytes(guid)))) {
		// getTable().delete(new Delete(Bytes.toBytes(guid)));
		// return true;
		// }
		return false;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addClassification(java.lang.String,
	 *      org.ala.model.Classification)
	 */
	@Override
	public boolean addClassification(String guid, Classification classification)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.CLASSIFICATION_COL.getColumnName(),
				guid, classification);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getClassifications(java.lang.String)
	 */
	public List<Classification> getClassifications(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.CLASSIFICATION_COL.getColumnName(),
				guid, Classification.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#syncTriples(org.ala.model.Document,
	 *      java.util.List)
	 */
	public boolean syncTriples(org.ala.model.Document document,
			List<Triple> triples, Map<String, String> dublinCore, boolean statsOnly)
			throws Exception {

		List<String> scientificNames = new ArrayList<String>();
		String specificEpithet = null;
		List<String> subspecies = new ArrayList<String>();
		List<String> species = new ArrayList<String>();
		String genus = null;
		String family = null;
		String superfamily = null;
		String order = null;
		String phylum = null;
		String klass = null;
		String kingdom = null;

		String dcSource = null;
		String dcPublisher = null;
		String dcIdentifier = null;
		String dcTitle = null;
		
		boolean isScreenshot = false;

		if (document != null) {
			dcPublisher = document.getInfoSourceName();
			dcSource = document.getInfoSourceUri();
			dcIdentifier = document.getIdentifier();
			dcTitle = document.getTitle();
		}

		// iterate through triples and find scientific names and genus
		for (Triple triple : triples) {

			String predicate = triple.predicate.substring(triple.predicate
					.lastIndexOf("#") + 1);

			if (predicate.endsWith("hasKingdom")) {
				kingdom = triple.object;
			}
			if (predicate.endsWith("hasPhylum")) {
				phylum = triple.object;
			}
			if (predicate.endsWith("hasClass")) {
				klass = triple.object;
			}
			if (predicate.endsWith("hasOrder")) {
				order = triple.object;
			}
			if (predicate.endsWith("hasFamily")) {
				family = triple.object;
			}
			if (predicate.endsWith("hasSuperFamily")) {
				superfamily = triple.object;
			}
			if (predicate.endsWith("hasGenus")) {
				genus = triple.object;
			}
			if (predicate.endsWith("hasSpecies")) {
				species.add(triple.object);
			}
			if (predicate.endsWith("hasSubSpecies")) {
				subspecies.add(triple.object);
			}
			if (predicate.endsWith("hasSpecificEpithet")) {
				specificEpithet = triple.object;
			}
			if (predicate.endsWith("hasScientificName")) {
				scientificNames.add(triple.object);
			}
			if (predicate.endsWith("hasVideoPageUrl")) {
			    isScreenshot = true;
			}
		}

		if (scientificNames.isEmpty() && subspecies.isEmpty()
				&& specificEpithet == null && species.isEmpty()
				&& genus == null && family == null && superfamily == null
				&& order == null && klass == null && phylum == null) {
			logger.info("No classification found for document at: "
					+ document.getFilePath());
			return false; // we have nothing to work with, so give up
		}
		
		// Lookup LSID in Checklist Bank data
		String rank = null;
		Rank rankObj = null;
		if (scientificNames.isEmpty()) {
			if (!subspecies.isEmpty()) {
				scientificNames.addAll(subspecies);
				rank = "subspecies";
				rankObj = Rank.SSP;
			} else if (!species.isEmpty()) {
				scientificNames.addAll(species);
				rank = "species";
				rankObj = Rank.SP;
			} else if (genus != null) {
				if (specificEpithet != null) {
					scientificNames.add(genus + " " + specificEpithet);
					rank = "species";
					rankObj = Rank.SP;
				} else {
					scientificNames.add(genus);
					rank = "genus";
					rankObj = Rank.GEN;
				}
			} else if (family != null) {
				scientificNames.add(family);
				rank = "family";
				rankObj = Rank.FAM;
			} else if (superfamily != null) {
				scientificNames.add(superfamily);
				rank = "superfamily";
				rankObj = Rank.SUPERFAM;
			} else if (order != null) {
				scientificNames.add(order);
				rank = "order";
				rankObj = Rank.ORD;
			} else if (klass != null) {
				scientificNames.add(klass);
				rank = "class";
				rankObj = Rank.CL;
			} else if (phylum != null) {
				scientificNames.add(phylum);
				rank = "phylum";
				rankObj = Rank.PHYLUM;
			} else if (kingdom != null) {
				scientificNames.add(kingdom);
				rank = "kingdom";
				rankObj = Rank.REG;
			} else {
				logger.info("Not enough search data for Checklist Bank found for document at: "
						+ document.getFilePath());
				return false;
			}
		}

		String guid = null;

		for (String scientificName : scientificNames) {
			LinnaeanRankClassification classification = new LinnaeanRankClassification(
					kingdom, phylum, klass, order, family, genus,
					scientificName);
			guid = findLsidByName(scientificName, classification, rank);
			if (guid != null)
				break;
		}

		if (guid == null) {
			for (String sp : species) {
				LinnaeanRankClassification classification = new LinnaeanRankClassification(
						kingdom, phylum, klass, order, family, genus, sp);
				guid = findLsidByName(sp, classification, rank);
				if (guid != null)
					break;
			}
		}

		if (guid == null && genus != null && specificEpithet != null) {
			LinnaeanRankClassification classification = new LinnaeanRankClassification(
			        kingdom, phylum, klass, order, family, genus, null);
			guid = findLsidByName(genus + " " + specificEpithet,
			        classification, "species");
		}
		//is statsOnly we can return whether or not the scientific name was found...
		if(statsOnly) {
		    return guid != null;
		}
		
		if (guid != null) {

			for (Triple triple : triples) {
				// check for an empty object
				String object = StringUtils.trimToNull(triple.object);

				if (object != null) {

					logger.trace(triple.predicate);

					// check here for predicates of complex objects
					if (triple.predicate.endsWith("hasCommonName")) {

						CommonName commonName = new CommonName();
						String commonNameString = WordUtils
								.capitalizeFully(triple.object);
						commonName.setNameString(commonNameString);
						commonName.setInfoSourceId(Integer.toString(document
								.getInfoSourceId()));
						commonName.setDocumentId(Integer.toString(document
								.getId()));
						commonName.setInfoSourceName(dcPublisher);
						commonName.setInfoSourceURL(dcSource);
						commonName.setTitle(dcTitle);
						commonName.setIdentifier(dcIdentifier);
						addCommonName(guid, commonName);

					} else if (triple.predicate.endsWith("hasConservationStatus")) {

						// dont add conservation status to higher ranks than
						// species
						if (rankObj != null
								&& rankObj.getId() >= Rank.SP.getId()) {

							// lookup the vocabulary term
							ConservationStatus cs = vocabulary
									.getConservationStatusFor(
											document.getInfoSourceId(),
											triple.object);
							if (cs == null) {
								cs = new ConservationStatus();
								cs.setStatus(triple.object);
							}

							cs.setInfoSourceId(Integer.toString(document
									.getInfoSourceId()));
							cs.setDocumentId(Integer.toString(document.getId()));
							cs.setInfoSourceName(dcPublisher);
							cs.setInfoSourceURL(dcSource);
							cs.setTitle(dcTitle);
							cs.setIdentifier(dcIdentifier);
							cs.setRawStatus(triple.object);
							addConservationStatus(guid, cs);
						}

					} else if (triple.predicate.endsWith("hasPestStatus")) {

						if (rankObj != null
								&& rankObj.getId() >= Rank.SP.getId()) {
							// lookup the vocabulary term
							PestStatus ps = vocabulary.getPestStatusFor(
									document.getInfoSourceId(), triple.object);
							if (ps == null) {
								ps = new PestStatus();
								ps.setStatus(triple.object);
							}

							ps.setInfoSourceId(Integer.toString(document
									.getInfoSourceId()));
							ps.setDocumentId(Integer.toString(document.getId()));
							ps.setInfoSourceName(dcPublisher);
							ps.setInfoSourceURL(dcSource);
							ps.setTitle(dcTitle);
							ps.setIdentifier(dcIdentifier);
							ps.setRawStatus(triple.object);
							addPestStatus(guid, ps);
						}

					} else if (triple.predicate.endsWith("hasImagePageUrl")) {
						// do nothing but prevent getting caught next - added
						// further down
					} else if (!Predicates.getTaxonomicPredicates().contains(
							triple.predicate)) {

						// FIXME - this feels mighty unscalable...
						// essentially we are putting all other field values in
						// one very
						// large cell
						// if this becomes a performance problem we should split
						// on the predicate value. i.e. tc:hasHabitatText,
						// this was the intention with the "raw:" column family
						// namespace
						SimpleProperty simpleProperty = new SimpleProperty();
						simpleProperty.setName(triple.predicate);
						simpleProperty.setValue(triple.object);
						simpleProperty.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
						simpleProperty.setDocumentId(Integer.toString(document.getId()));
						simpleProperty.setInfoSourceName(dcPublisher);
						simpleProperty.setInfoSourceURL(dcSource);
						simpleProperty.setTitle(dcTitle);
						simpleProperty.setIdentifier(dcIdentifier);
						addTextProperty(guid, simpleProperty);
					}
				}
			}

			// retrieve the content type
			if (document.getFilePath() != null) {

				// FIXME - we should be able to copy images up to the parent
				// is it an image ???
				if (document != null
						&& document.getMimeType() != null
						&& MimeType.getImageMimeTypes().contains(
								document.getMimeType())) {
					Image image = new Image();
					image.setContentType(document.getMimeType());
					if (!isScreenshot) {
					    image.setRepoLocation(document.getFilePath()
							+ File.separator + FileType.RAW.getFilename()
							+ MimeType.getFileExtension(document.getMimeType()));
					} else {
					    image.setRepoLocation(document.getFilePath()
	                            + File.separator + FileType.SCREENSHOT.getFilename()
	                            + MimeType.getFileExtension(document.getMimeType()));
					}
					image.setInfoSourceId(Integer.toString(document
							.getInfoSourceId()));
					image.setDocumentId(Integer.toString(document.getId()));
					image.setInfoSourceName(dcPublisher);
					image.setInfoSourceURL(dcSource);
					image.setIdentifier(dcIdentifier);
					image.setTitle(dcTitle);

					if (dublinCore != null) {
						image.setCreator(dublinCore.get(Predicates.DC_CREATOR
								.toString()));
						image.setLocality(dublinCore.get(Predicates.LOCALITY
								.toString()));
						image.setIsPartOf(dublinCore
								.get(Predicates.DC_IS_PART_OF.toString()));
						image.setLicence(dublinCore.get(Predicates.DC_LICENSE
								.toString()));
						image.setRights(dublinCore.get(Predicates.DC_RIGHTS
								.toString()));
						image.setIdentifier(dublinCore.get(Predicates.DC_IDENTIFIER
                                .toString()));
					}

					if (hasPredicate(triples, Predicates.DIST_MAP_IMG_URL)) {
						addDistributionImage(guid, image);
					} else if (hasPredicate(triples, Predicates.VIDEO_PAGE_URL)) {
                        addScreenshotImage(guid, image);
                    } else {
						addImage(guid, image);
//						logger.info("ADDING IMAGE TO: " + guid);
					}
					
					
				} else {
					// do something for videos.....
				}
			}

			logger.info("Adding content to: " + guid
					+ ", using scientific name: " + scientificNames.get(0));
			// addLiteralValues(guid,
			// infoSourceId,Integer.toString(document.getId()), properties);

			return true;
		} else {
			logger.info("GUID null");
			return false;
		}
	}

	/**
	 * Returns true if the predicate is present.
	 * 
	 * @param triples
	 * @param predicate
	 * @return
	 */
	private boolean hasPredicate(List<Triple> triples, Predicates predicate) {
		for (Triple triple : triples) {
			if (triple.predicate.equals(predicate.getPredicate().toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#clearRawProperties()
	 */
	public void clearRawProperties() throws Exception {

		// ResultScanner scanner =
		// getTable().getScanner(Bytes.toBytes(TC_COL_FAMILY));
		// Iterator<Result> iter = scanner.iterator();
		// int i = 0;
		// while (iter.hasNext()) {
		// Result result = iter.next();
		// byte[] row = result.getRow();
		// getTable().delete(new
		// Delete(row).deleteFamily(Bytes.toBytes(RAW_COL_FAMILY)));
		// logger.debug(++i + " " + (new String(row)));
		// }
		// logger.debug("Raw triples cleared");
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#deleteForInfosources(java.lang.String[])
	 */
	@Override
	public boolean deleteForInfosources(String[] infoSourceIds)
			throws Exception {

		Scanner scanner = storeHelper.getScanner(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName());

		List<String> ids = new ArrayList<String>();
		for (String id : infoSourceIds) {
			ids.add(id);
		}

		byte[] guidAsBytes = null;
		int i = 0;
		while ((guidAsBytes = scanner.getNextGuid()) != null) {

			String guid = new String(guidAsBytes);
			// get common names
			List<CommonName> commonNames = getCommonNamesFor(guid);
			removeForInfosources((List) commonNames, ids);
			storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
					ColumnType.VERNACULAR_COL.getColumnName(),
					guid, (List) commonNames, false);

			// get common names
			List<Image> images = getImages(guid);
			removeForInfosources((List) images, ids);
			storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
					ColumnType.IMAGE_COL.getColumnName(), guid,
					(List) images, false);
			i++;

			if (i % 1000 == 0) {
				logger.debug(i + " records processed. Last ID: " + guid);
			}
		}
		return false;
	}

	private void removeForInfosources(List<AttributableObject> objects,
			List<String> ids) {

		List<AttributableObject> toRemove = new ArrayList<AttributableObject>();
		for (AttributableObject object : objects) {
			if (object.getInfoSourceId() != null
					&& ids.contains(object.getInfoSourceId())) {
				toRemove.add(object);
			}
		}
		objects.removeAll(toRemove);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#createIndex()
	 */
	public void createIndex() throws Exception {

		long start = System.currentTimeMillis();

		// List<String> pestTerms =
		// vocabulary.getTermsForStatusType(StatusType.PEST);
		// List<String> consTerms =
		// vocabulary.getTermsForStatusType(StatusType.CONSERVATION);

		SolrServer solrServer = solrUtils.getSolrServer();

		logger.info("Clearing existing taxon entries in the search index...");
		solrServer.deleteByQuery("idxtype:" + IndexedTypes.TAXON); // delete
																	// everything!
		solrServer.commit();

		logger.info("Cleared existing taxon entries in the search index.");
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		int i = 0;

		Scanner scanner = storeHelper.getScanner(TC_TABLE, TC_COL_FAMILY,
				ColumnType.TAXONCONCEPT_COL.getColumnName());

		// load iconic species

		byte[] guidAsBytes = null;

		while ((guidAsBytes = scanner.getNextGuid()) != null) {
			// while(i==0){
			String guid = new String(guidAsBytes);
			// String guid =
			// "urn:lsid:biodiversity.org.au:afd.taxon:7bcdf6aa-4eb0-4184-bbd1-ca2b518b749f";
			i++;

			if (i % 1000 == 0) {
				logger.info("Indexed records: " + i + ", current guid: " + guid);
			}
			// if exception happened, it not stop whole index process
			try{
				// index each taxon
				List<SolrInputDocument> docsToAdd = indexTaxonConcept(guid);
				docs.addAll(docsToAdd);
	
				if (i > 0 && i % 1000 == 0) {
					// iw.commit();
					logger.debug(i + " " + guid + ", adding " + docs.size());
					if (!docs.isEmpty()) {
						solrServer.add(docs);
						solrServer.commit();
						docs.clear();
					}
				}
			}
			catch(Exception e){
				logger.error("*** ERROR -- guid: " + guid, e);
				continue;
			}
		}

		if (!docs.isEmpty()) {
			logger.debug(i + "  adding " + docs.size() + " documents to index");
			solrServer.add(docs);
			solrServer.commit();
		}

		long finish = System.currentTimeMillis();
		logger.info("Index created in: " + ((finish - start) / 1000)
				+ " seconds with " + i + " taxon concepts processed.");
	}

	/**
	 * Index the supplied taxon concept.
	 * 
	 * @param guid
	 * @return
	 */
	public List<SolrInputDocument> indexTaxonConcept(String guid)
			throws Exception {

		List<SolrInputDocument> docsToAdd = new ArrayList<SolrInputDocument>();

		// get taxon concept details
		TaxonConcept taxonConcept = getByGuid(guid);

		if (taxonConcept != null) {
			// get synonyms concepts
			List<TaxonConcept> synonyms = getSynonymsFor(guid);

			// get congruent concepts
			// List<TaxonConcept> congruentTcs = getCongruentConceptsFor(guid);

			// treat congruent objects the same way we do synonyms
			// synonyms.addAll(congruentTcs);

			// get common names
			List<CommonName> commonNames = getCommonNamesFor(guid);

			// add the parent id to enable tree browsing with this index
			List<TaxonConcept> children = getChildConceptsFor(guid);

			// add conservation and pest status'
			List<ConservationStatus> conservationStatuses = getConservationStatuses(guid);
			// List<PestStatus> pestStatuses = getPestStatuses(guid);

			// add text properties
			List<SimpleProperty> simpleProperties = getTextPropertiesFor(guid);

			// save all infosource ids to add in a Set to index at the end
			Set<String> infoSourceIds = new TreeSet<String>();

			// get alternative ids
			List<String> identifiers = getIdentifiers(guid);

			// TODO this index should also include nub ids
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("idxtype", IndexedTypes.TAXON);

			// is this species iconic
			boolean isIconic = isIconic(guid);
			// does this taxon have occurrence records associated with it?
			Integer count = getOccurrenceRecordCount(guid);
			// boolean isGs=count != null && count.size() >0 && count.get(0)>0;
			if (count != null) {
				doc.addField("occurrenceCount", count);
			}

			if (taxonConcept.getNameString() != null) {

				doc.addField("id", taxonConcept.getGuid());
				doc.addField("guid", taxonConcept.getGuid());

				for (String identifier : identifiers) {
					doc.addField("otherGuid", identifier);
				}
				// add the numeric checklistbank id
				doc.addField("otherGuid", taxonConcept.getId());

				addToSetSafely(infoSourceIds, taxonConcept.getInfoSourceId());

				TaxonName taxonName = getTaxonNameFor(guid);
				if (taxonName != null && taxonName.getNameComplete() != null) {
					doc.addField("nameComplete", taxonName.getNameComplete());
				} else {
					doc.addField("nameComplete", taxonConcept.getNameString());
				}

				// add multiple forms of the scientific name to the index
				addScientificNameToIndex(doc, taxonConcept.getNameString(),
						taxonConcept.getRankString());

				if (taxonConcept.getParentGuid() != null) {
					doc.addField("parentGuid", taxonConcept.getParentGuid());
				}
				if (taxonConcept.getParentId() != null) {
					doc.addField("parentId", taxonConcept.getParentId());
				}

				// add the nested set values
				doc.addField("left", taxonConcept.getLeft());
				doc.addField("right", taxonConcept.getRight());
				doc.addField("author", taxonConcept.getAuthor());

				for (ConservationStatus cs : conservationStatuses) {
					if (cs.getRawStatus() != null) {
						doc.addField("conservationStatus", cs.getRawStatus(),
								0.6f);
						addToSetSafely(infoSourceIds, cs.getInfoSourceId());
						if (cs.getRegion() != null
								&& Regions.getRegion(cs.getRegion()) != null) {
							Regions r = Regions.getRegion(cs.getRegion());
							doc.addField("conservationStatus" + r.getAcronym(),
									cs.getRawStatus());
						}
					}
				}

				// for (PestStatus ps : pestStatuses) {
				// // for (String psTerm : pestTerms) {
				// if
				// (ps.getStatus().toLowerCase().contains(psTerm.toLowerCase()))
				// {
				// // Field f = new Field("pestStatus", psTerm, Store.YES,
				// Index.NOT_ANALYZED);
				// // f.setBoost(0.6f);
				// doc.addField("pestStatus", psTerm, 0.6f);
				// addToSetSafely(infoSourceIds, ps.getInfoSourceId());
				// }
				// // }
				// }

				for (SimpleProperty sp : simpleProperties) {
					// index *Text properties
					if (sp.getName().endsWith("Text")) {
						// Field textField = new Field("simpleText",
						// sp.getValue(), Store.YES, Index.ANALYZED);
						// textField.setBoost(0.4f);
						doc.addField("simpleText", sp.getValue(), 0.4f);
						addToSetSafely(infoSourceIds, sp.getInfoSourceId());
					}
				}

				// StringBuffer cnStr = new StringBuffer();
				Set<String> commonNameSet = new TreeSet<String>();
				List<String> higherPriorityNames = new ArrayList<String>();
				for (CommonName cn : commonNames) {
					if (cn.getNameString() != null) {
						// normalise the common names for display
						String commonNameString = WordUtils.capitalizeFully(cn
								.getNameString());
						commonNameString.trim();
						commonNameSet.add(commonNameString);

						if (cn.isPreferred() != null && cn.isPreferred()) {
							higherPriorityNames.add(cn.getNameString());
						}
						addToSetSafely(infoSourceIds, cn.getInfoSourceId());
					}
				}

				if (commonNameSet.size() > 0) {
					String commonNamesConcat = StringUtils
							.deleteWhitespace(StringUtils.join(commonNameSet,
									" "));
					doc.addField("commonNameSort", commonNamesConcat);
					// add each common name separately
					// We need to add all common names to the commonNameExact
					// because the CommonName also stores the "parts" of the
					// common name
					for (String commonName : commonNameSet) {
						if (isIconic) {
							doc.addField("commonName", commonName, 100f);
							doc.addField("commonNameExact", commonName, 100f);
						} else if (higherPriorityNames.contains(commonName)) {
							doc.addField("commonName", commonName, 5f);
							doc.addField("commonNameExact", commonName, 5f);
						} else {
							doc.addField("commonName", commonName, 1.4f);
							doc.addField("commonNameExact", commonName, 1.4f);
						}
						// pull apart the common name
						String[] parts = commonName.split(" ");
						if (parts.length > 1) {
							String lastPart = parts[parts.length - 1];
							if (isIconic) {
								doc.addField("commonName", lastPart, 100f);
							} else {
								doc.addField("commonName", lastPart, 2.5f);
							}
						}
					}

					doc.addField("commonNameDisplay",
							StringUtils.join(commonNameSet, ", "));
					doc.addField("commonNameSingle", commonNames.get(0)
							.getNameString());
				}

				for (TaxonConcept synonym : synonyms) {
					if (synonym.getNameString() != null) {
						logger.debug("adding synonym to index: "
								+ synonym.getNameString());
						// add a new document for each synonym
						SolrInputDocument synonymDoc = new SolrInputDocument();
						synonymDoc.addField("id", synonym.getGuid());
						synonymDoc.addField("guid", taxonConcept.getGuid());
						synonymDoc.addField("idxtype", IndexedTypes.TAXON);
						addScientificNameToIndex(synonymDoc,synonym.getNameString(), null);
						synonymDoc.addField("acceptedConceptName",taxonConcept.getNameString());
						if (!commonNames.isEmpty()) {
							synonymDoc.addField("commonNameSort", commonNames.get(0).getNameString());
							synonymDoc.addField("commonNameDisplay",StringUtils.join(commonNameSet, ", "));
						}
						addRankToIndex(synonymDoc, taxonConcept.getRankString());
						// add the synonym as a separate document
						docsToAdd.add(synonymDoc);
						// store the source
						if (synonym.getInfoSourceId() != null) {
							infoSourceIds.add(synonym.getInfoSourceId()); // getting
							// NPE
						}
					}
				}

				// add the regions this species has occurred in
				List<OccurrencesInGeoregion> regions = getRegions(guid);
				for (OccurrencesInGeoregion region : regions) {
					if (region.getRegionTypeId() != null) {
						RegionTypes rt = RegionTypes.getRegionType(region
								.getRegionTypeId());
						if (rt != null) {
							doc.addField(rt.toString(), region.getName());
						}
					}
				}

				List<Classification> classifications = getClassifications(guid);
				for (Classification classification : classifications) {
					addIfNotNull(doc, "kingdom", classification.getKingdom());
					addIfNotNull(doc, "phylum", classification.getPhylum());
					addIfNotNull(doc, "class", classification.getClazz());
					addIfNotNull(doc, "bioOrder", classification.getOrder());
					addIfNotNull(doc, "family", classification.getFamily());
					addIfNotNull(doc, "genus", classification.getGenus());
					
					//speciesGroup
					if("Arthropoda".equals(classification.getPhylum())) doc.addField("speciesGroup", "Arthropods");
					if("Mollusca".equals(classification.getPhylum())) doc.addField("speciesGroup", "Molluscs");
					if("Magnoliophyta".equals(classification.getPhylum())) doc.addField("speciesGroup", "Flowering plants");
					if("Reptilia".equals(classification.getClazz())) doc.addField("speciesGroup", "Reptiles");
					if("Amphibia".equals(classification.getClazz())) doc.addField("speciesGroup", "Frogs");
					if("Aves".equals(classification.getClazz())) doc.addField("speciesGroup", "Birds");
					if("Mammalia".equals(classification.getClazz())) doc.addField("speciesGroup", "Mammals");
					if("Plantae".equals(classification.getKingdom())) doc.addField("speciesGroup", "Plants");
					if("Animalia".equals(classification.getKingdom())) doc.addField("speciesGroup", "Animals");
					if(classification.getClazz()!=null && fishTaxa.contains(classification.getClazz().toLowerCase())){
						doc.addField("speciesGroup", "Fish");
					}
				}
				
				List<Image> images = getImages(guid);
				boolean hasImages = !images.isEmpty();

				if (hasImages) {
					// FIXME should be replaced by the highest ranked image
					Image image = images.get(0);
					if (image.getRepoLocation() != null) {
						doc.addField("image", image.getRepoLocation());
						// Change to adding this in earlier
						String thumbnail = image.getRepoLocation().replace("raw", "thumbnail");
						doc.addField("thumbnail", thumbnail);
					} else {
						logger.error("Error adding image to concept: " + image);
					}
				}
				doc.addField("hasImage", hasImages);

				Boolean isAustralian = isAustralian(guid);
				if (isAustralian != null && isAustralian) {
					doc.addField("australian_s", "recorded");
					doc.addField("aus_s", "yes");
				}

				String linkIdentifier = getLinkIdentifier(guid);
				if (linkIdentifier != null) {
					doc.addField("linkIdentifier", linkIdentifier);
				}
				
				addRankToIndex(doc, taxonConcept.getRankString());

				doc.addField("hasChildren",Boolean.toString(!children.isEmpty()));
				doc.addField("dataset", StringUtils.join(infoSourceIds, " "));

				docsToAdd.add(doc);
			}
		}
		return docsToAdd;
	}

	/**
	 * Add field if the value is not null
	 * 
	 * @param doc
	 * @param classification
	 * @param fieldName
	 */
	private void addIfNotNull(SolrInputDocument doc, String fieldName,
			String fieldValue) {
		if (StringUtils.isNotEmpty(fieldValue)) {
			doc.addField(fieldName, fieldValue);
		}
	}

	/**
	 * Adds a scientific name to the lucene index in multiple forms to increase
	 * chances of matches
	 * 
	 * @param doc
	 * @param scientificName
	 * @param taxonRank
	 */
	public void addScientificNameToIndex(SolrInputDocument doc,
			String scientificName, String taxonRank) {

		NameParser nameParser = new NameParser();
		Integer rankId = -1;

		if (taxonRank != null) {
			Rank rank = Rank.getForField(taxonRank.toLowerCase());
			if (rank != null) {
				rankId = rank.getId();
			} else {
				logger.warn("Unknown rank string: " + taxonRank);
			}
		}
		// remove the subgenus
		String normalized = "";

		if (scientificName != null) {
			normalized = scientificName.replaceFirst("\\([A-Za-z]{1,}\\) ", "");
		}

		ParsedName parsedName = nameParser.parseIgnoreAuthors(normalized);
		// store scientific name values in a set before adding to Lucene so we
		// don't get duplicates
		TreeSet<String> sciNames = new TreeSet<String>();

		if (parsedName != null) {
			if (parsedName.isBinomial()) {
				// add multiple versions
				sciNames.add(parsedName.buildAbbreviatedCanonicalName().toLowerCase());
				sciNames.add(parsedName.buildAbbreviatedFullName().toLowerCase());
			}

			// add lowercased version
			sciNames.add(parsedName.buildCanonicalName().toLowerCase());
			// add to Lucene
			for (String sciName : sciNames) {
				// doc.add(new Field(SCI_NAME, sciName, Store.YES,
				// Index.NOT_ANALYZED_NO_NORMS));
				doc.addField(SCI_NAME, sciName);
			}

			Float boost = 0.8f;

			if (rankId != null) {
				if (rankId == 6000) {
					// genus higher than species so it appears first
					boost = 3f;
				} else if (rankId == 7000) {
					// species higher than subspecies so it appears first
					boost = 2f;
				}
			}

			// Field f = new Field(SCI_NAME_TEXT, StringUtils.join(sciNames,
			// " "), Store.YES, Index.ANALYZED);
			// f.setBoost(boost);
			// doc.add(f);
			doc.addField(SCI_NAME_TEXT, StringUtils.join(sciNames, " "), boost);
		} else {
			// add lowercased version if name parser failed
			// doc.add(new Field(SCI_NAME, normalized.toLowerCase(), Store.YES,
			// Index.NOT_ANALYZED_NO_NORMS));
			// doc.add(new Field(SCI_NAME_TEXT, normalized.toLowerCase(),
			// Store.YES, Index.ANALYZED));
			doc.addField(SCI_NAME, normalized.toLowerCase(), 0.8f);
			doc.addField(SCI_NAME_TEXT, normalized.toLowerCase(), 0.8f);
		}

		if (scientificName != null) {
			// doc.add(new Field(SCI_NAME_RAW, scientificName, Store.YES,
			// Index.NOT_ANALYZED_NO_NORMS));
			doc.addField(SCI_NAME_RAW, scientificName, 0.8f);
		}
	}

	/**
	 * Add the rank to the search document.
	 * 
	 * @param rankString
	 * @param doc
	 */
	private void addRankToIndex(SolrInputDocument doc, String rankString) {
		if (rankString != null) {
			try {
				Rank rank = Rank.getForField(rankString.toLowerCase());
				// doc.add(new Field("rank", rank.getName(), Store.YES,
				// Index.NOT_ANALYZED_NO_NORMS));
				// doc.add(new Field("rankId", rank.getId().toString(),
				// Store.YES, Index.NOT_ANALYZED_NO_NORMS));
				doc.addField("rank", rank.getName());
				doc.addField("rankId", rank.getId());
			} catch (Exception e) {
				logger.warn("Rank not found: " + rankString + " - ");
				// assign to Rank.TAXSUPRAGEN so that sorting still works
				// reliably
				// doc.add(new Field("rank", Rank.TAXSUPRAGEN.getName(),
				// Store.YES, Index.NOT_ANALYZED_NO_NORMS));
				// doc.add(new Field("rankId",
				// Rank.TAXSUPRAGEN.getId().toString(), Store.YES,
				// Index.NOT_ANALYZED_NO_NORMS));
				doc.addField("rank", Rank.TAXSUPRAGEN.getName());
				doc.addField("rankId", Rank.TAXSUPRAGEN.getId());
			}
		}
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addReference(org.ala.model.Reference)
	 */
	public boolean addReferences(String guid,
			List<org.ala.model.Reference> references) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.REFERENCE_COL.getColumnName(), guid,
				(List) references, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addEarliestReference(java.lang.String,
	 *      org.ala.model.Reference)
	 */
	public boolean addEarliestReference(String guid, Reference reference)
			throws Exception {
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY,
				ColumnType.EARLIEST_REFERENCE_COL.getColumnName(),
				guid, reference);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addPublicationReference(java.lang.String,
	 *      org.ala.model.Reference)
	 */
	public boolean addPublicationReference(String guid,
			List<Reference> references) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.PUBLICATION_REFERENCE_COL
						.getColumnName(), guid, (List) references, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addPublication(java.lang.String,
	 *      org.ala.model.Publication)
	 */
	public boolean addPublication(String guid, Publication publication)
			throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.PUBLICATION_COL.getColumnName(), guid,
				publication);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtantStatus(java.lang.String)
	 */
	@Override
	public List<ExtantStatus> getExtantStatuses(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.EXTANT_STATUS_COL.getColumnName(), guid,
				ExtantStatus.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getHabitats(java.lang.String)
	 */
	@Override
	public List<Habitat> getHabitats(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.HABITAT_COL.getColumnName(), guid,
				Habitat.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addExtantStatus(java.lang.String,
	 *      org.ala.model.ExtantStatus)
	 */
	@Override
	public boolean addExtantStatus(String guid,
			List<ExtantStatus> extantStatusList) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.EXTANT_STATUS_COL.getColumnName(), guid,
				(List) extantStatusList, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addHabitat(java.lang.String,
	 *      org.ala.model.Habitat)
	 */
	@Override
	public boolean addHabitat(String guid, List<Habitat> habitatList)
			throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.HABITAT_COL.getColumnName(), guid,
				(List) habitatList, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getRegions(java.lang.String)
	 */
	@Override
	public List<OccurrencesInGeoregion> getRegions(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.REGION_COL.getColumnName(), guid,
				OccurrencesInGeoregion.class);
	}

	/**
	 * Retrieve the references for the concept with the supplied GUID.
	 * 
	 * @see org.ala.dao.TaxonConceptDao#getReferencesFor(java.lang.String)
	 */
	public List<Reference> getReferencesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.REFERENCE_COL.getColumnName(), guid,
				Reference.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getEarliestReferenceFor(java.lang.String)
	 */
	public Reference getEarliestReferenceFor(String guid) throws Exception {
		return (Reference) storeHelper.get(TC_TABLE, TC_COL_FAMILY,
				ColumnType.EARLIEST_REFERENCE_COL.getColumnName(),
				guid, Reference.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getPublicationReferencesFor(java.lang.String)
	 */
	public List<Reference> getPublicationReferencesFor(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.PUBLICATION_REFERENCE_COL
						.getColumnName(), guid, Reference.class);
	}

	public Integer getOccurrenceRecordCount(String guid) throws Exception {
		return (Integer) storeHelper.get(TC_TABLE, TC_COL_FAMILY,
				ColumnType.OCCURRENCE_RECORDS_COUNT_COL
						.getColumnName(), guid, Integer.class);
	}

	public boolean addIdentificationKeys(String guid,
			List<IdentificationKey> identificationKeyList) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IDENTIFICATION_KEY_COL.getColumnName(),
				guid, (List) identificationKeyList, false);
	}

	public List<IdentificationKey> getIdentificationKeys(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.IDENTIFICATION_KEY_COL.getColumnName(),
				guid, IdentificationKey.class);
	}

	public boolean addSpecimenHoldings(String guid,
			List<SpecimenHolding> specimenHoldingList) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SPECIMEN_HOLDING_COL.getColumnName(),
				guid, (List) specimenHoldingList, false);
	}

	public List<SpecimenHolding> getSpecimenHoldings(String guid)
			throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SPECIMEN_HOLDING_COL.getColumnName(),
				guid, SpecimenHolding.class);
	}

	public boolean appendSpecimenHoldings(String guid,
			List<SpecimenHolding> specimenHoldingList) throws Exception {
		List<SpecimenHolding> list = getSpecimenHoldings(guid);
		specimenHoldingList.addAll(list);
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SPECIMEN_HOLDING_COL.getColumnName(),
				guid, (List) specimenHoldingList, false);
	}
	
	// ===============<ExtendedtaxonConcept>=========
	private Object getColumnValue(Map<String, Object> map,
			ColumnType columnType) {
		Object o = map.get(columnType.getColumnName());
		if (columnType.isList() && o == null) {
			o = new ArrayList();
		}
		return o;
	}

	private Object getFirstItem(List list) {
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtendedTaxonConceptByGuid(java.lang.String)
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid)
			throws Exception {
		return getExtendedTaxonConceptByGuid(guid, true);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtendedTaxonConceptByGuid(java.lang.String, boolean)
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid, boolean checkPreferred)
			throws Exception {
		logger.debug("Retrieving concept for guid: " + guid);
		if(checkPreferred){
			guid = getPreferredGuid(guid);
		}
		Map<String, Object> map = storeHelper.getSubColumnsByGuid(TC_COL_FAMILY, TC_COL_FAMILY, guid);
		ExtendedTaxonConceptDTO etc = createExtendedDTO(map);
		logger.debug("Returned concept for guid: " + guid);
		return etc;
	}
	
	/**
	 * 
	 * @param etc
	 * @param map
	 */
	private ExtendedTaxonConceptDTO createExtendedDTO(Map<String, Object> map) {
		// populate the dto
		ExtendedTaxonConceptDTO etc = new ExtendedTaxonConceptDTO();
		etc.setTaxonConcept((TaxonConcept) getColumnValue(map,ColumnType.TAXONCONCEPT_COL));
		etc.setTaxonName((TaxonName) getFirstItem((List) getColumnValue(map,ColumnType.TAXONNAME_COL)));
		etc.setClassification((Classification) getFirstItem((List<Classification>) getColumnValue(map, ColumnType.CLASSIFICATION_COL)));
		etc.setIdentifiers((List<String>) getColumnValue(map,ColumnType.IDENTIFIER_COL));
		etc.setSynonyms((List<TaxonConcept>) getColumnValue(map,ColumnType.SYNONYM_COL));
		etc.setCommonNames((List<CommonName>) getColumnValue(map,ColumnType.VERNACULAR_COL));
		etc.setChildConcepts((List<TaxonConcept>) getColumnValue(map,ColumnType.IS_PARENT_COL_OF));
		etc.setParentConcepts((List<TaxonConcept>) getColumnValue(map,ColumnType.IS_CHILD_COL_OF));
		etc.setPestStatuses((List<PestStatus>) getColumnValue(map,ColumnType.PEST_STATUS_COL));
		etc.setConservationStatuses((List<ConservationStatus>) getColumnValue(map, ColumnType.CONSERVATION_STATUS_COL));
		etc.setImages((List<Image>) getColumnValue(map,ColumnType.IMAGE_COL));
		etc.setDistributionImages((List<Image>) getColumnValue(map,ColumnType.DIST_IMAGE_COL));
		etc.setScreenshotImages((List<Image>) getColumnValue(map,ColumnType.SCREENSHOT_IMAGE_COL));
		etc.setExtantStatuses((List<ExtantStatus>) getColumnValue(map,ColumnType.EXTANT_STATUS_COL));
		etc.setHabitats((List<Habitat>) getColumnValue(map,ColumnType.HABITAT_COL));
		etc.setRegionTypes(OccurrencesInGeoregion.getRegionsByType((List<OccurrencesInGeoregion>) getColumnValue(map, ColumnType.REGION_COL)));
		etc.setReferences((List<Reference>) getColumnValue(map,ColumnType.REFERENCE_COL));
		etc.setEarliestReference((Reference) getColumnValue(map,ColumnType.EARLIEST_REFERENCE_COL));
		etc.setPublicationReference((List<Reference>) getColumnValue(map,ColumnType.PUBLICATION_REFERENCE_COL));
		etc.setIdentificationKeys((List<IdentificationKey>) getColumnValue(map,ColumnType.IDENTIFICATION_KEY_COL));
		etc.setSpecimenHolding((List<SpecimenHolding>) getColumnValue(map,ColumnType.SPECIMEN_HOLDING_COL));
		etc.setIsAustralian((Boolean) getColumnValue(map,ColumnType.IS_AUSTRALIAN));
		etc.setLinkIdentifier((String) getColumnValue(map,ColumnType.LINK_IDENTIFIER));
		
		// sort the list of SimpleProperties for display in UI
		List<SimpleProperty> simpleProperties = (List<SimpleProperty>) getColumnValue(map, ColumnType.TEXT_PROPERTY_COL);
		Collections.sort(simpleProperties);
		etc.setSimpleProperties(simpleProperties);
		return etc;
	}
	
	/**
	 * Retrieve a list of extended DTOs.
	 * @param startGuid the guid of the first concept to select
	 * @param pageSize the number to select.
	 * 
	 * @see org.ala.dao.TaxonConceptDao#getPage(java.lang.String, int)
	 */
	@Override
	public List<ExtendedTaxonConceptDTO> getPage(String startGuid, int pageSize) throws Exception {

		List<ExtendedTaxonConceptDTO> dtoList = new ArrayList<ExtendedTaxonConceptDTO>(pageSize);
		Map<String, Map<String,Object>> rowMaps = storeHelper.getPageOfSubColumns(TC_COL_FAMILY, TC_COL_FAMILY, startGuid, pageSize);
		for(Map<String, Object> row : rowMaps.values()){
			ExtendedTaxonConceptDTO e = createExtendedDTO(row);
			dtoList.add(e);
		}
		return dtoList;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getProfilePage(java.lang.String, int)
	 */
	@Override
	public List<SpeciesProfileDTO> getProfilePage(String startGuid, int pageSize)
			throws Exception {
		List<SpeciesProfileDTO> dtoList = new ArrayList<SpeciesProfileDTO>(pageSize);
		ColumnType[] columns = new ColumnType[]{
				ColumnType.TAXONCONCEPT_COL,
				ColumnType.VERNACULAR_COL,
				ColumnType.HABITAT_COL,
				ColumnType.CONSERVATION_STATUS_COL,
				ColumnType.SENSITIVE_STATUS_COL,
		};
		
		Map<String, Map<String,Object>> rowMaps = storeHelper.getPageOfSubColumns(TC_COL_FAMILY, TC_COL_FAMILY, columns, startGuid, pageSize);
		
		for(Map<String, Object> row : rowMaps.values()){
			SpeciesProfileDTO spDTO = new SpeciesProfileDTO();
			TaxonConcept tc = (TaxonConcept) getColumnValue(row,ColumnType.TAXONCONCEPT_COL);
			List<CommonName> cns = (List<CommonName>) getColumnValue(row,ColumnType.VERNACULAR_COL);
			List<Habitat> habs = (List<Habitat>) getColumnValue(row,ColumnType.HABITAT_COL);
			List<ConservationStatus> cons = (List<ConservationStatus>) getColumnValue(row,ColumnType.CONSERVATION_STATUS_COL);
			if(tc!=null){
				spDTO.setGuid(tc.getGuid());
				spDTO.setScientificName(tc.getNameString());
                                spDTO.setRank(tc.getRankString());
                                if(tc.getLeft() != null) spDTO.setLeft(tc.getLeft().toString());
                                if(tc.getRight() != null) spDTO.setRight(tc.getRight().toString());
				if(!cns.isEmpty()){
					spDTO.setCommonName(cns.get(0).getNameString());
				}
				for(Habitat habitat: habs){
					if(habitat.getStatusAsString()!=null){
						spDTO.getHabitats().add(habitat.getStatusAsString());
					}
				}
				for(ConservationStatus cs: cons){
				    //Do not add the international status
				    if(cs.getRegion() != null){
				        //TODO work out the best way to make conservation status available...
				        spDTO.getConservationStatus().add(cs);
				    }
				}
				spDTO.setSensitiveStatus((List<SensitiveStatus>)getColumnValue(row, ColumnType.SENSITIVE_STATUS_COL));
				dtoList.add(spDTO);
			}
		}
		return dtoList;
	}

	/**
	 * Add the supplied sensitive status to this concept.
	 *  
	 * @see org.ala.dao.TaxonConceptDao#addSensitiveStatus(java.lang.String, org.ala.model.SensitiveStatus)
	 */
	public void addSensitiveStatus(String guid, SensitiveStatus ss) throws Exception {
		storeHelper.put(TC_TABLE, TC_COL_FAMILY,
				ColumnType.SENSITIVE_STATUS_COL.getColumnName(),
				guid, ss);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#setLinkIdentifier(java.lang.String, java.lang.String)
	 */
	public boolean setLinkIdentifier(String guid, String linkIdentifier) throws Exception {
		return storeHelper.updateStringValue(TC_TABLE, TC_COL_FAMILY, 
				ColumnType.LINK_IDENTIFIER.getColumnName(), guid, linkIdentifier);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#setLinkIdentifier(java.lang.String, java.lang.String)
	 */
	public String getLinkIdentifier(String guid, String linkIdentifier) throws Exception {
		return storeHelper.getStringValue(TC_TABLE, TC_COL_FAMILY, 
				ColumnType.LINK_IDENTIFIER.getColumnName(), guid);
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
     * Returns the LSID for the CB name usage for the supplied common name.
     *
     * When the common name returns more than 1 hit a result is only returned if all the scientific names match
     * @see CBIndexSearch.getLSIDForUniqueCommonName
     * 
     * @param name
     * @return
     */	
	public String findLSIDByCommonName(String commonName){
		return cbIdxSearcher.searchForLSIDCommonName(commonName);
	}

	/**
	 * @param storeHelper
	 *            the storeHelper to set
	 */
	public void setStoreHelper(StoreHelper storeHelper) {
		this.storeHelper = storeHelper;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#setVocabulary(org.ala.vocabulary.Vocabulary)
	 */
	public void setVocabulary(Vocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

	/**
	 * @param solrUtils
	 *            the solrUtils to set
	 */
	public void setSolrUtils(SolrUtils solrUtils) {
		this.solrUtils = solrUtils;
	}
	
	/** ======================================
	 * Ranking functions
	 * 
	 =========================================*/
	public List getColumn(ColumnType columnType, String guid) throws Exception {
		return storeHelper.getList(TC_TABLE, TC_COL_FAMILY,
				columnType.getColumnName(), guid, columnType.getClazz());
	}
		
	private Rankable changeRanking(Rankable rankable, BaseRanking ir){
		if(ir.isBlackListed()){
			rankable.setIsBlackListed(ir.isBlackListed());
		}
		else{
			Integer ranking = rankable.getRanking();
			Integer noOfRankings = rankable.getNoOfRankings();
			if (ranking == null) {
				if (ir.isPositive()) {
					ranking = new Integer(1);
				} else {
					ranking = new Integer(-1);
				}
				noOfRankings = new Integer(1);
			} else {
				if (ir.isPositive()) {
					ranking++;
				} else {
					ranking--;
				}
				noOfRankings++;
			}
			rankable.setRanking(ranking);				
			rankable.setNoOfRankings(noOfRankings);
		}		
		return rankable;
	}
		
	
	public boolean setRanking(String guid, ColumnType columnType, BaseRanking baseRanking)throws Exception{
		List list = new ArrayList();
		
		RankingType rt = RankingType.getRankingTypeByTcColumnType(columnType);
		List<Rankable> objs = getColumn(columnType, guid);		
		//In common Name, it will increase/decrease ranking for all columns with same commonName.
		//In Image, it will increase/decrease ranking for one column with same uri.
		for (Rankable rankable : objs) {
			// All Rankable object are extended from AttributableObject, eg: 'identifier' is 
			// a field name of AttributableObject, it stored the uri value.			
			boolean ok = true;
			Map<String, String> map = baseRanking.getCompareFieldValue();				
			if(map != null){
				Set keys = map.keySet();
				Iterator itr = keys.iterator();
				while(itr.hasNext()){
					String key = (String) itr.next();
					String value = BeanUtils.getProperty(rankable, key);
					String compareValue = map.get(key);
					if(!compareValue.equalsIgnoreCase(value)){
						ok = false;
						break;
					}
				}
			}
			if(ok){
				list.add(changeRanking(rankable, baseRanking));	
			}
			else{
				list.add(rankable);	
			}			
		}

		// re-sort based on the rankings
		Collections.sort(list);
		
		//update solr index
		if(list.size() > 0){	
			Object o = list.get(0);
			if(RankingType.RK_IMAGE == rt){
				String value = BeanUtils.getProperty(o, "repoLocation");
				value = value.replace("raw", "thumbnail");
				updateSolrIndexRanking(guid, value, null);
			}
			else if(RankingType.RK_COMMON_NAME == rt){
				String value = BeanUtils.getProperty(o, "nameString");
				updateSolrIndexRanking(guid, null, value);
			}
		}
		logger.debug("setRanking(..) save to Cassandta !!!");
		// write back to database
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY,
				columnType.getColumnName(), guid, (List)list, false);
	}	
	
	private void updateSolrIndexRanking(String guid, String thumbnailUri, String commonNameSingle) throws Exception {
		String key = null;
		
		SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQueryType("standard");	        
	    
		if(guid == null || guid.length() < 1 || "*".equals(guid)){
			logger.info("Invalid guid: " + guid);
			return;
		}
		else{
			key = ClientUtils.escapeQueryChars(guid);
		}		
		solrQuery.setQuery("idxtype:" + IndexedTypes.TAXON + " AND id:" + key);
		
		// do the Solr search
        QueryResponse qr = solrUtils.getSolrServer().query(solrQuery); // can throw exception
        SolrDocumentList sdl = qr.getResults();

        //assume only one record in the list.
        logger.debug("*** sdl size: " + sdl.size());
        if(sdl != null && sdl.size() > 0){
        	SolrDocument d = sdl.get(0);
        	String id = (String)d.get("id");
        	SolrInputDocument doc = new SolrInputDocument();
        	
        	//populate new doc with new value
        	doc.addField("id", id);
        	Object o = d.get("thumbnail");
        	if(o != null){
        		if(thumbnailUri != null && !"".equals(thumbnailUri)){
        			doc.addField("thumbnail", thumbnailUri);
        		}
        		else{
        			doc.addField("thumbnail", o);
        		}
        	}
        	o = d.get("commonNameSingle");
        	if(o != null){
        		if(commonNameSingle != null && !"".equals(commonNameSingle)){
        			doc.addField("commonNameSingle", commonNameSingle);
        		}
        		else{
        			doc.addField("commonNameSingle", o);
        		}        	
        	}
        	
        	// copy old value into new doc.
        	Iterator<Map.Entry<String, Object>> i = d.iterator();
        	while(i.hasNext()){
        		Map.Entry<String, Object> e2 = i.next();
        		if(!"thumbnail".equals(e2.getKey()) && !"commonNameSingle".equals(e2.getKey()) && !"id".equals(e2.getKey())){
        			doc.addField(e2.getKey(), e2.getValue());
        		} 
        	}
        	if(id != null){
        		SolrServer server = solrUtils.getSolrServer();
        		//delete old doc
//        		server.deleteById(id);
//        		server.commit();
        		//add new doc
        		server.add(doc);
        		server.commit();
        		
        		logger.debug("updateSolrIndexRanking !!!");
        	}
        }
	}	
}
