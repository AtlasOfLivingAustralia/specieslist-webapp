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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.Image;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.model.PestStatus;
import org.ala.model.Publication;
import org.ala.model.Rank;
import org.ala.model.Reference;
import org.ala.model.SimpleProperty;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
import org.ala.repository.Predicates;
import org.ala.util.FileType;
import org.ala.util.MimeType;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.commons.lang.StringUtils;
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
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
import org.springframework.stereotype.Component;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.SearchResultException;
import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.model.LinnaeanRankClassification;
import au.org.ala.data.util.RankType;

/**
 * Database agnostic implementation if Taxon concept DAO.
 * 
 * This implementation hands off java objects to an instance of <code>StoreHelper</code>
 * that hides the complexities of the underlying datastore in use.
 * 
 * @see StoreHelper
 * @see HBaseHelper
 * @see CassandraHelper
 * 
 * @author Dave Martin
 */
@Component("taxonConceptDao")
public class TaxonConceptSHDaoImpl implements TaxonConceptDao {

	protected static Logger logger = Logger.getLogger(TaxonConceptSHDaoImpl.class);
	
	/** The location for the lucene index */
	public static final String TC_INDEX_DIR = "/data/solr/bie/index";

	/** Columns */
	private static final String TAXONCONCEPT_COL = "taxonConcept";
	private static final String TAXONNAME_COL = "hasTaxonName";
	private static final String IDENTIFIER_COL = "sameAs";
	private static final String SYNONYM_COL = "hasSynonym";
	private static final String IS_CONGRUENT_TO_COL = "IsCongruentTo";
	private static final String VERNACULAR_COL = "hasVernacularConcept";
	private static final String CONSERVATION_STATUS_COL = "hasConservationStatus";
	private static final String PEST_STATUS_COL = "hasPestStatus";
	private static final String REGION_COL = "hasRegion";
	private static final String EXTANT_STATUS_COL = "hasExtantStatus";
	private static final String HABITAT_COL = "hasHabitat";
	private static final String IMAGE_COL = "hasImage";
	private static final String DIST_IMAGE_COL = "hasDistributionImage";
	private static final String IS_CHILD_COL_OF = "IsChildTaxonOf";
	private static final String IS_PARENT_COL_OF = "IsParentTaxonOf";
    private static final String TEXT_PROPERTY_COL = "hasTextProperty";
    private static final String CLASSIFICATION_COL = "hasClassification";
    private static final String REFERENCE_COL = "hasReference";
    private static final String EARLIEST_REFERENCE_COL = "hasEarliestReference";
    private static final String PUBLICATION_REFERENCE_COL = "hasPublicationReference";
    private static final String PUBLICATION_COL = "hasPublication";

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
	 * @see org.ala.dao.TaxonConceptDao#getSynonymsFor(java.lang.String)
	 */
	public List<TaxonConcept> getSynonymsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, SYNONYM_COL, guid, TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getSynonymsFor(java.lang.String)
	 */
	public List<TaxonConcept> getCongruentConceptsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, IS_CONGRUENT_TO_COL, guid, TaxonConcept.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getImages(java.lang.String)
	 */
	public List<Image> getImages(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, IMAGE_COL, guid, Image.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getDistributionImages(java.lang.String)
	 */
	public List<Image> getDistributionImages(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, DIST_IMAGE_COL, guid, Image.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getPestStatuses(java.lang.String)
	 */
	public List<PestStatus> getPestStatuses(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, PEST_STATUS_COL, guid, PestStatus.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getConservationStatuses(java.lang.String)
	 */
	public List<ConservationStatus> getConservationStatuses(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, CONSERVATION_STATUS_COL, guid, ConservationStatus.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getChildConceptsFor(java.lang.String)
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, IS_PARENT_COL_OF, guid, TaxonConcept.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getParentConceptsFor(java.lang.String)
	 */	
	public List<TaxonConcept> getParentConceptsFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, IS_CHILD_COL_OF, guid, TaxonConcept.class);
	}		
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getCommonNamesFor(java.lang.String)
	 */
	public List<CommonName> getCommonNamesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, VERNACULAR_COL, guid, CommonName.class);
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#getTextPropertiesFor(java.lang.String)
	 */
	public List<SimpleProperty> getTextPropertiesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, TEXT_PROPERTY_COL, guid, SimpleProperty.class);
	}
	
	/**
	 * FIXME Switch to using a single column for TaxonConcept
	 * 
	 * @see org.ala.dao.TaxonConceptDao#create(org.ala.model.TaxonConcept)
	 */
	public boolean create(TaxonConcept tc) throws Exception {
		if(tc==null){
			throw new IllegalArgumentException("Supplied TaxonConcept was null.");
		}
		
		if(tc.getGuid()==null){
			throw new IllegalArgumentException("Supplied TaxonConcept has a null Guid value.");
		}
		
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY, TAXONCONCEPT_COL, tc.getGuid(), tc);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#update(org.ala.model.TaxonConcept)
	 */
	public boolean update(TaxonConcept tc) throws Exception {
		
		if (tc!=null && tc.getGuid() == null) {
			throw new IllegalArgumentException("Supplied GUID for the Taxon Concept is null.");
		}
		
		//FIXME this is here to update some information not available in the export from checklist bank
		// This should refactored out at some stage
		TaxonConcept current = (TaxonConcept) storeHelper.get(TC_TABLE, TC_COL_FAMILY, TAXONCONCEPT_COL, tc.getGuid(), TaxonConcept.class);
		if(current==null){ 
			return false;
		}
		return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY, TAXONCONCEPT_COL, tc.getGuid(), current);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addTaxonName(java.lang.String, org.ala.model.TaxonName)
	 */
	public boolean addTaxonName(String guid, TaxonName taxonName) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, TAXONNAME_COL, guid, taxonName);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addCommonName(java.lang.String, org.ala.model.CommonName)
	 */
	public boolean addCommonName(String guid, CommonName commonName) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, VERNACULAR_COL, guid, commonName);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addConservationStatus(java.lang.String, org.ala.model.ConservationStatus)
	 */
	public boolean addConservationStatus(String guid, ConservationStatus conservationStatus) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, CONSERVATION_STATUS_COL, guid, conservationStatus);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addPestStatus(java.lang.String, org.ala.model.PestStatus)
	 */
	public boolean addPestStatus(String guid, PestStatus pestStatus) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, PEST_STATUS_COL, guid, pestStatus);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addRegions(java.lang.String, java.util.List)
	 */
	public boolean addRegions(String guid, List<OccurrencesInGeoregion> regions) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY, REGION_COL, guid, (List) regions, false);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addImage(java.lang.String, org.ala.model.Image)
	 */
	public boolean addImage(String guid, Image image) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, IMAGE_COL, guid, image);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addDistributionImage(java.lang.String, org.ala.model.Image)
	 */
	public boolean addDistributionImage(String guid, Image image) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, DIST_IMAGE_COL, guid, image);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addSynonym(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addSynonym(String guid, TaxonConcept synonym) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, SYNONYM_COL, guid, synonym);
	}	
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addIsCongruentTo(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addIsCongruentTo(String guid, TaxonConcept congruent) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, IS_CONGRUENT_TO_COL, guid, congruent);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addChildTaxon(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addChildTaxon(String guid, TaxonConcept childConcept) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, IS_PARENT_COL_OF, guid, childConcept);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addIdentifier(java.lang.String, java.lang.String)
	 */
	public boolean addIdentifier(String guid, String alternativeIdentifier) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, IDENTIFIER_COL, guid, alternativeIdentifier);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getIdentifiers(java.lang.String)
	 */
	public List<String> getIdentifiers(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, IDENTIFIER_COL, guid, String.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addParentTaxon(java.lang.String, org.ala.model.TaxonConcept)
	 */
	public boolean addParentTaxon(String guid, TaxonConcept parentConcept) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, IS_CHILD_COL_OF, guid, parentConcept);
	}

    /**
	 * @see org.ala.dao.TaxonConceptDao#addTextProperty(java.lang.String, org.ala.model.SimpleProperty)
	 */
	public boolean addTextProperty(String guid, SimpleProperty textProperty) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, TEXT_PROPERTY_COL, guid, textProperty);
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
//		guid = getPreferredGuid(guid);
		return (TaxonConcept) storeHelper.get(TC_TABLE, TC_COL_FAMILY, TAXONCONCEPT_COL, guid, TaxonConcept.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtendedTaxonConceptByGuid(java.lang.String)
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid) throws Exception {
		
		logger.debug("Retrieving concept for guid: "+guid);
		
		ExtendedTaxonConceptDTO etc = new ExtendedTaxonConceptDTO();
		
		guid = getPreferredGuid(guid);
		
		//populate the dto
		etc.setTaxonConcept(getByGuid(guid));
		etc.setTaxonName(getTaxonNameFor(guid));
		
		List<Classification> classifications = getClassifications(guid);
		if(!classifications.isEmpty()){
			etc.setClassification(classifications.get(0));
		}
		
		etc.setIdentifiers(getIdentifiers(guid));
		etc.setSynonyms(getSynonymsFor(guid));
		etc.setCommonNames(getCommonNamesFor(guid));
		etc.setChildConcepts(getChildConceptsFor(guid));
		etc.setParentConcepts(getParentConceptsFor(guid));
		etc.setPestStatuses(getPestStatuses(guid));
		etc.setConservationStatuses(getConservationStatuses(guid));
		etc.setImages(getImages(guid));
		etc.setDistributionImages(getDistributionImages(guid));
        etc.setExtantStatuses(getExtantStatuses(guid));
        etc.setHabitats(getHabitats(guid));
        etc.setRegionTypes(OccurrencesInGeoregion.getRegionsByType(getRegions(guid)));
        etc.setReferences(getReferencesFor(guid));
		
        etc.setEarliestReference(getEarliestReferenceFor(guid));
        etc.setPublicationReference(getPublicationReferencesFor(guid));
        
		// sort the list of SimpleProperties for display in UI
        List<SimpleProperty> simpleProperties = getTextPropertiesFor(guid);
        Collections.sort(simpleProperties);
        etc.setSimpleProperties(simpleProperties);
        
        logger.debug("Returned concept for guid: "+guid);
        
		return etc;
	}

	/**
	 * Use the Lucene indexes to find the correct (accepted) guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	private String getPreferredGuid(String guid) throws Exception {
		//use the Lucene indexes to find the correct (accepted) guid.
		SearchResultsDTO<SearchTaxonConceptDTO> searchResults = findByGuid(guid, 0, 1, null, null);
		if(!searchResults.getResults().isEmpty()){
			guid = searchResults.getResults().get(0).getGuid();
		}
		return guid;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getTaxonNameFor(java.lang.String)
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception {
		List taxonNames = storeHelper.getList(TC_TABLE, TC_COL_FAMILY, TAXONNAME_COL, guid, TaxonName.class);
		if(taxonNames.isEmpty()){
			return null;
		} else {
			return (TaxonName) taxonNames.get(0);
		}
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getTaxonNameFor(java.lang.String)
	 */
	public List<TaxonName> getTaxonNamesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, TAXONNAME_COL, guid, TaxonName.class);
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
//		Result result = getTable().get(getTcGetter(guid));
//		if (result.isEmpty()) {
//			return null;
//		}
//		
//		//treemaps are sorted
//		TreeMap<String, String> properties = new TreeMap<String,String>();
//		
//		for (Map.Entry<byte[], NavigableMap<byte[], byte[]>> entry : result.getNoVersionMap().entrySet()) {
//			for (Map.Entry<byte[], byte[]> familyEntry : entry.getValue().entrySet()) {
//				properties.put(Bytes.toString(entry.getKey()) + ":" + Bytes.toString(familyEntry.getKey()), 
//						Bytes.toString(familyEntry.getValue()));
//			}
//		}
//		
//		//sort by key
//		return properties;
		return null;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String, int)
	 */
	public List<SearchTaxonConceptDTO> findByScientificName(String input, int limit) throws Exception {
        SearchResultsDTO<SearchTaxonConceptDTO> sr = findByScientificName(input, 0, limit, null, null);
        return sr.getResults();
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

		//include a query against the GUIDs
		Query guidQuery = new TermQuery(new Term("guid", input));
		Query otherGuidQuery = new TermQuery(new Term("otherGuid", input));

		//combine the query terms
		scientificNameQuery = scientificNameQuery.combine(new Query[]{scientificNameQuery, guidQuery, otherGuidQuery, commonNameQuery});
        // run the search
        return sortPageSearch(scientificNameQuery, startIndex, pageSize, sortField, sortDirection);
    }
    
    /**
	 * @see org.ala.dao.TaxonConceptDao#findByScientificName(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
	 */
    public SearchResultsDTO findByGuid(String input, Integer startIndex, Integer pageSize,
            String sortField, String sortDirection) throws Exception {

        input = StringUtils.trimToNull(input);
		if(input==null){
			return new SearchResultsDTO();
		}
		
		//include a query against the GUIDs
		Query guidQuery = new TermQuery(new Term("guid", input));
		Query otherGuidQuery = new TermQuery(new Term("otherGuid", input));

		//combine the query terms
		Query fullQuery = guidQuery.combine(new Query[]{guidQuery, otherGuidQuery});
        // run the search
        return sortPageSearch(fullQuery, startIndex, pageSize, sortField, sortDirection);
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

        TopDocs topDocs = getTcIdxSearcher().search(searchQuery, null, startIndex + pageSize, sort); // TODO ues sortField here
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

	/**
	 * @see org.ala.dao.TaxonConceptDao#findLsidByName(java.lang.String, java.lang.String)
	 */
	@Override
	public String findLsidByName(String scientificName, LinnaeanRankClassification classification, String taxonRank) {
		String lsid = null;
		try {
			lsid = cbIdxSearcher.searchForLSID(scientificName, classification, RankType.getForName(taxonRank));
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
			lsid = cbIdxSearcher.searchForLSID(scientificName, RankType.getForName(taxonRank));
		} catch (SearchResultException e) {
			logger.warn("Checklist Bank lookup exception - " + e.getMessage() + e.getResults());
		}
		return lsid;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#findCBDataByName(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public NameSearchResult findCBDataByName(String scientificName,
			LinnaeanRankClassification classification, String rank)
			throws SearchResultException {
		return cbIdxSearcher.searchForRecord(scientificName, classification,
				RankType.getForName(rank));
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
	    		try {
	    			this.tcIdxSearcher = new IndexSearcher(TC_INDEX_DIR);
	    		} catch (Exception e){
	    			new IndexWriter(TC_INDEX_DIR , new StandardAnalyzer());
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
	private SearchTaxonConceptDTO createTaxonConceptFromIndex(Document doc, float score) {
		SearchTaxonConceptDTO taxonConcept = new SearchTaxonConceptDTO();
		taxonConcept.setGuid(doc.get("guid"));
		taxonConcept.setParentGuid(doc.get("parentGuid"));
		taxonConcept.setName(doc.get("scientificNameRaw"));
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
	        taxonConcept.setLeft(Integer.parseInt(doc.get("left")));
	        taxonConcept.setRight(Integer.parseInt(doc.get("right")));
        } catch (NumberFormatException e) {
        	//expected if left and right values are unavailable
        }
        
        try {
            taxonConcept.setRankId(Integer.parseInt(doc.get("rankId")));
        } catch (NumberFormatException ex) {
            logger.error("Error parsing rankId: "+ex.getMessage());
        }
        taxonConcept.setPestStatus(doc.get(StatusType.PEST.toString()));
        taxonConcept.setConservationStatus(doc.get(StatusType.CONSERVATION.toString()));
        
        //add image detais
        taxonConcept.setImage(doc.get("image"));
        taxonConcept.setThumbnail(doc.get("thumbnail"));
		return taxonConcept;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#delete(java.lang.String)
	 */
	public boolean delete(String guid) throws Exception {
		
		//TODO add to interface
//		if (getTable().exists(new Get(Bytes.toBytes(guid)))) {
//			getTable().delete(new Delete(Bytes.toBytes(guid)));
//			return true;
//		}
		return false;
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addClassification(java.lang.String, org.ala.model.Classification)
	 */
	@Override
	public boolean addClassification(String guid, Classification classification) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, CLASSIFICATION_COL, guid, classification);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getClassifications(java.lang.String)
	 */
	public List<Classification> getClassifications(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, CLASSIFICATION_COL, guid, Classification.class);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#syncTriples(org.ala.model.Document, java.util.List)
	 */
	public boolean syncTriples(org.ala.model.Document document, List<Triple> triples, Map<String, String> dublinCore) throws Exception {

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

		LinnaeanRankClassification classification = new LinnaeanRankClassification(kingdom, null, null, order, family, genus, scientificName);

		String guid = findLsidByName(scientificName, classification, rank);

		// if null try with the species name
		if (guid == null && species != null) {
			guid = findLsidByName(species, classification, "species");
		}
		// FIX ME search with genus + specific epithet
		if (guid == null && genus != null && specificEpithet != null) {
			guid = findLsidByName(genus + " " + specificEpithet, classification, "species");
		}

		if (guid != null) {
			
//			Map<String, Object> properties = new HashMap<String,Object>();
			
			for(Triple triple: triples){
				
				//check for an empty object
				String object = StringUtils.trimToNull(triple.object);
				
				if(object!=null){
				
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
						addCommonName(guid, commonName);
						
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
	                    cs.setRawStatus(triple.object);
						addConservationStatus(guid, cs);
						
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
	                    ps.setRawStatus(triple.object);
						addPestStatus(guid, ps);
						
	                } else if (triple.predicate.endsWith("hasImagePageUrl")) {
	                    // do nothing but prevent getting caught next - added further down
	                } else if(!Predicates.getTaxonomicPredicates().contains(triple.predicate)){
	
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
	                    addTextProperty(guid, simpleProperty);
	                }
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
							+ File.separator
							+ FileType.RAW.getFilename()
							+ MimeType.getFileExtension(document.getMimeType()));
					image.setInfoSourceId(Integer.toString(document.getInfoSourceId()));
					image.setDocumentId(Integer.toString(document.getId()));
                    image.setInfoSourceName(dcPublisher);
                    image.setInfoSourceURL(dcSource);
                    image.setIdentifier(dcIdentifier);
                    image.setTitle(dcTitle);
                    
                    if(dublinCore!=null){
                    	image.setCreator(dublinCore.get(Predicates.DC_CREATOR.toString()));
                    	image.setLocality(dublinCore.get(Predicates.LOCALITY.toString()));
                    	image.setIsPartOf(dublinCore.get(Predicates.DC_IS_PART_OF.toString()));
                    	image.setLicence(dublinCore.get(Predicates.DC_LICENSE.toString()));
                    	image.setRights(dublinCore.get(Predicates.DC_RIGHTS.toString()));
                    }
                    
                    if(hasPredicate(triples,Predicates.DIST_MAP_IMG_URL)){
                    	addDistributionImage(guid, image);
                    } else {
                    	addImage(guid, image);
                    }
				}
			}
			
//			logger.debug("Adding content to: "+guid+", using scientific name: "+scientificName+", genus: "+genus);
//			addLiteralValues(guid, infoSourceId,Integer.toString(document.getId()), properties);
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns true if the predicate is present.
	 * @param triples
	 * @param predicate
	 * @return
	 */
	private boolean hasPredicate(List<Triple> triples, Predicates predicate) {
		for(Triple triple: triples){
			if(triple.predicate.equals(predicate.getPredicate().toString())){
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#clearRawProperties()
	 */
	public void clearRawProperties() throws Exception {
		
//    	ResultScanner scanner = getTable().getScanner(Bytes.toBytes(TC_COL_FAMILY));
//    	Iterator<Result> iter = scanner.iterator();
//		int i = 0;
//		while (iter.hasNext()) {
//    		Result result = iter.next();
//    		byte[] row = result.getRow();
//    		getTable().delete(new Delete(row).deleteFamily(Bytes.toBytes(RAW_COL_FAMILY)));
//    		logger.debug(++i + " " + (new String(row)));
//    	}
//    	logger.debug("Raw triples cleared");
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#createIndex()
	 */
	public void createIndex() throws Exception {
		
		long start = System.currentTimeMillis();
		
        List<String> pestTerms = vocabulary.getTermsForStatusType(StatusType.PEST);
        List<String> consTerms = vocabulary.getTermsForStatusType(StatusType.CONSERVATION);
    	
        SolrServer solrServer = solrUtils.getSolrServer();

        logger.info("Clearing existing taxon entries in the search index...");
        solrServer.deleteByQuery("idxtype:"+IndexedTypes.TAXON); // delete everything!
        
        logger.info("Cleared existing taxon entries in the search index.");
        List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

    	int i = 0;
		
		Scanner scanner = storeHelper.getScanner(TC_TABLE, TC_COL_FAMILY, TAXONCONCEPT_COL);
		
		byte[] guidAsBytes = null;
		
		while ((guidAsBytes = scanner.getNextGuid())!=null) {
    		
			String guid = new String(guidAsBytes);
			i++;
			
			if(i%1000==0){
				logger.info("Indexed records: "+i+", current guid: "+guid);
			}
    		
			List<SolrInputDocument> docsToAdd = indexTaxonConcept(guid, pestTerms, consTerms);
            docs.addAll(docsToAdd);
	    	
	    	if(i%1000==0){
	    		//iw.commit();
	    		logger.debug(i + " " + guid+", adding "+docs.size());
                solrServer.add(docs);
                solrServer.commit();
                docs.clear();
	    	}
    	}
    	
        if (!docs.isEmpty()) {
        	logger.debug(i + "  adding "+docs.size()+" documents to index");
        	solrServer.add(docs);
        	solrServer.commit();
        }
    	
    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds with "+ i +" taxon concepts processed.");
	}

	/**
	 * Index the supplied taxon concept.
	 * 
	 * @param guid
	 * @return
	 */
	public List<SolrInputDocument> indexTaxonConcept(String guid, List<String> pestTerms, List<String> consTerms) throws Exception {
		
		List<SolrInputDocument> docsToAdd = new ArrayList<SolrInputDocument>();
		
		//get taxon concept details
		TaxonConcept taxonConcept = getByGuid(guid);
		
        if(taxonConcept!=null){
        	//get synonyms concepts
    		List<TaxonConcept> synonyms = getSynonymsFor(guid);
    		
    		//get congruent concepts
    		List<TaxonConcept> congruentTcs = getCongruentConceptsFor(guid);
    		
    		//treat congruent objects the same way we do synonyms
    		synonyms.addAll(congruentTcs);
    		
    		//get common names
    		List<CommonName> commonNames = getCommonNamesFor(guid);
    		
    		//add the parent id to enable tree browsing with this index
    		List<TaxonConcept> children = getChildConceptsFor(guid);

    		//add conservation and pest status'
            List<ConservationStatus> conservationStatuses = getConservationStatuses(guid);
    		List<PestStatus> pestStatuses = getPestStatuses(guid);

            //add text properties
            List<SimpleProperty> simpleProperties = getTextPropertiesFor(guid);
            
    		// save all infosource ids to add in a Set to index at the end
    		Set<String> infoSourceIds = new TreeSet<String>();
            
    		//get alternative ids
    		List<String> identifiers = getIdentifiers(guid);
    		
    		//TODO this index should also include nub ids
    		SolrInputDocument doc = new SolrInputDocument();
    		doc.addField("idxtype", IndexedTypes.TAXON);
            
    		if(taxonConcept.getNameString()!=null){
    			
    			doc.addField("id", taxonConcept.getGuid());
    			doc.addField("guid", taxonConcept.getGuid());
    			
    			for(String identifier: identifiers){
    				doc.addField("otherGuid", identifier);
    			}
    			//add the numeric checklistbank id
    			doc.addField("otherGuid", taxonConcept.getId());
    			
    			addToSetSafely(infoSourceIds, taxonConcept.getInfoSourceId());
    			//add multiple forms of the scientific name to the index
    			addScientificNameToIndex(doc, taxonConcept.getNameString(), taxonConcept.getRankString());
	    		
	    		if(taxonConcept.getParentGuid()!=null){
	    			doc.addField("parentGuid", taxonConcept.getParentGuid());
	    		}
	    		
	    		//add the nested set values
	    		doc.addField("left", taxonConcept.getLeft());
	    		doc.addField("right", taxonConcept.getRight());
	    		
                for (ConservationStatus cs : conservationStatuses) {
                    for (String csTerm : consTerms) {
                        if (cs.getStatus()!=null && cs.getStatus().toLowerCase().contains(csTerm.toLowerCase())) {
//                            Field f = new Field("conservationStatus", csTerm, Store.YES, Index.NOT_ANALYZED);
//                            f.setBoost(0.6f);
                            doc.addField("conservationStatus", csTerm, 0.6f);
                			addToSetSafely(infoSourceIds, cs.getInfoSourceId());
                        }
                    }
                }

                for (PestStatus ps : pestStatuses) {
                    for (String psTerm : pestTerms) {
                        if (ps.getStatus().toLowerCase().contains(psTerm.toLowerCase())) {
//                            Field f = new Field("pestStatus", psTerm, Store.YES, Index.NOT_ANALYZED);
//                            f.setBoost(0.6f);
                            doc.addField("pestStatus", psTerm, 0.6f);
                            addToSetSafely(infoSourceIds, ps.getInfoSourceId());
                        }
                    }
                }

                for (SimpleProperty sp : simpleProperties) {
                    // index *Text properties
                    if (sp.getName().endsWith("Text")) {
//                        Field textField = new Field("simpleText", sp.getValue(), Store.YES, Index.ANALYZED);
//                        textField.setBoost(0.4f);
                        doc.addField("simpleText", sp.getValue(), 0.4f);
                        addToSetSafely(infoSourceIds, sp.getInfoSourceId());
                    }
                }

                //StringBuffer cnStr = new StringBuffer();
                Set<String> commonNameSet = new TreeSet<String>();
	    		for(CommonName cn: commonNames){
	    			if(cn.getNameString()!=null){
	    				commonNameSet.add(cn.getNameString().toLowerCase());
						addToSetSafely(infoSourceIds, cn.getInfoSourceId());
                        //doc.add(new Field("commonName", cn.nameString.toLowerCase(), Store.YES, Index.ANALYZED));
                        //cnStr.append(cn.nameString.toLowerCase() + " ");
	    			}
	    		}

                if (commonNameSet.size() > 0) {
                    String commonNamesConcat = StringUtils.deleteWhitespace(StringUtils.join(commonNameSet, " "));
                    doc.addField("commonNameSort", commonNamesConcat);
                    //add each common name separately
                    for(String commonName: commonNameSet){
                    	doc.addField("commonName", commonName);
                    }
                    doc.addField("commonNameDisplay", StringUtils.join(commonNameSet, ", "));
                    doc.addField("commonNameSingle", commonNames.get(0).getNameString());
                }
	    		
	    		for(TaxonConcept synonym: synonyms){
	    			if(synonym.getNameString()!=null){
	    				logger.debug("adding synonym to index: "+synonym.getNameString());
	    				//add a new document for each synonym
	    				SolrInputDocument synonymDoc = new SolrInputDocument();
	    				synonymDoc.addField("id", synonym.getGuid());
	    				synonymDoc.addField("guid", taxonConcept.getGuid());
	    				synonymDoc.addField("idxtype", IndexedTypes.TAXON);
	    				addScientificNameToIndex(synonymDoc, synonym.getNameString(), null);
	    				synonymDoc.addField("acceptedConceptName", taxonConcept.getNameString());
	    				if(!commonNames.isEmpty()){
	    					synonymDoc.addField("commonNameSort", commonNames.get(0).getNameString());
	    					synonymDoc.addField("commonNameDisplay", StringUtils.join(commonNameSet, ", "));
	    				}
	                    addRankToIndex(synonymDoc, taxonConcept.getRankString());
	    				//add the synonym as a separate document
	                    docsToAdd.add(synonymDoc);
	                    //store the source
                        if (synonym.getInfoSourceId()!=null){
                        	infoSourceIds.add(synonym.getInfoSourceId()); // getting NPE
                        }
	    			}
	    		}
	    		
	    		List<OccurrencesInGeoregion> regions = getRegions(guid);
	    		for(OccurrencesInGeoregion region : regions){
	    			//FIXME do this nicer using define region types etc....
	    			if("State".equalsIgnoreCase(region.getRegionType()) || "Territory".equalsIgnoreCase(region.getRegionType())){
	    				doc.addField("state", region.getName());
	    			}
	    			if(region.getRegionType()!=null && region.getRegionType().startsWith("IBRA")){
	    				doc.addField("ibra", region.getName());
	    			}
	    			if(region.getRegionType()!=null && region.getRegionType().startsWith("IMCRA")){
	    				doc.addField("imcra", region.getName());
	    			}
	    		}
	    		
	    		List<Classification> classifications = getClassifications(guid);
	    		for (Classification classification: classifications){
	    			addIfNotNull(doc, "kingdom", classification.getKingdom());
	    			addIfNotNull(doc, "phylum", classification.getPhylum());
	    			addIfNotNull(doc, "class", classification.getClazz());
	    			addIfNotNull(doc, "bioOrder", classification.getOrder());
	    			addIfNotNull(doc, "family", classification.getFamily());
	    		}
	    		
	    		List<Image> images = getImages(guid);
	    		if(!images.isEmpty()){
	    			//FIXME should be replaced by the highest ranked image
	    			Image image  = images.get(0);
	    			doc.addField("image", image.getRepoLocation());
	    			//Change to adding this in earlier
	    			String thumbnail = image.getRepoLocation().replace("raw", "thumbnail");
	    			doc.addField("thumbnail", thumbnail);
	    		}
	    		
                addRankToIndex(doc, taxonConcept.getRankString());
	    		
    			doc.addField("hasChildren", Boolean.toString(!children.isEmpty()));
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
	private void addIfNotNull(SolrInputDocument doc, String fieldName, String fieldValue) {
		if(StringUtils.isNotEmpty(fieldValue)){
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
	public void addScientificNameToIndex(SolrInputDocument doc, String scientificName, String taxonRank){

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
		//remove the subgenus
		String normalized = "";

		if (scientificName != null) {
			normalized = scientificName.replaceFirst("\\([A-Za-z]{1,}\\) ", "");
		}

		ParsedName parsedName = nameParser.parseIgnoreAuthors(normalized);
        // store scientific name values in a set before adding to Lucene so we don't get duplicates
        TreeSet<String> sciNames = new TreeSet<String>();

    	if (parsedName != null) {
    		if (parsedName.isBinomial()) {
    			//add multiple versions
                sciNames.add(parsedName.buildAbbreviatedCanonicalName().toLowerCase());
                sciNames.add(parsedName.buildAbbreviatedFullName().toLowerCase());
    		}

            //add lowercased version
            sciNames.add(parsedName.buildCanonicalName().toLowerCase());
            // add to Lucene
            for (String sciName : sciNames) {
                //doc.add(new Field(SCI_NAME, sciName, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                doc.addField(SCI_NAME, sciName);
            }

            Float boost = 1f;

            if (rankId != null) {
	            if (rankId == 6000) {
    	            // genus higher than species so it appears first
        	        boost = 3f;
            	} else if (rankId == 7000) {
                	// species higher than subspecies so it appears first
	                boost = 2f;
    	        }
    	    }

            //Field f = new Field(SCI_NAME_TEXT, StringUtils.join(sciNames, " "), Store.YES, Index.ANALYZED);
            //f.setBoost(boost);
            //doc.add(f);
            doc.addField(SCI_NAME_TEXT, StringUtils.join(sciNames, " "), boost);
    	} else {
    		//add lowercased version if name parser failed
	    	//doc.add(new Field(SCI_NAME, normalized.toLowerCase(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
            //doc.add(new Field(SCI_NAME_TEXT, normalized.toLowerCase(), Store.YES, Index.ANALYZED));
            doc.addField(SCI_NAME, normalized.toLowerCase());
            doc.addField(SCI_NAME_TEXT, normalized.toLowerCase());
    	}

    	if (scientificName!=null) {
    		//doc.add(new Field(SCI_NAME_RAW, scientificName, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
            doc.addField(SCI_NAME_RAW, scientificName);
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
		        //doc.add(new Field("rank", rank.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		        //doc.add(new Field("rankId", rank.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                doc.addField("rank", rank.getName());
                doc.addField("rankId", rank.getId());
		    } catch (Exception e) {
		        logger.warn("Rank not found: "+rankString+" - ");
		        // assign to Rank.TAXSUPRAGEN so that sorting still works reliably
		        //doc.add(new Field("rank", Rank.TAXSUPRAGEN.getName(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		        //doc.add(new Field("rankId", Rank.TAXSUPRAGEN.getId().toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
                doc.addField("rank", Rank.TAXSUPRAGEN.getName());
                doc.addField("rankId", Rank.TAXSUPRAGEN.getId());
		    }
		}
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addReference(org.ala.model.Reference)
	 */
	public boolean addReferences(String guid, List<org.ala.model.Reference> references) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY, REFERENCE_COL, guid, (List)references, false);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addEarliestReference(java.lang.String, org.ala.model.Reference)
	 */
	public boolean addEarliestReference(String guid, Reference reference) throws Exception {
        return storeHelper.putSingle(TC_TABLE, TC_COL_FAMILY, EARLIEST_REFERENCE_COL, guid, reference);
    }

	/**
	 * @see org.ala.dao.TaxonConceptDao#addPublicationReference(java.lang.String, org.ala.model.Reference)
	 */
	public boolean addPublicationReference(String guid, List<Reference> references) throws Exception{
    	return storeHelper.putList(TC_TABLE, TC_COL_FAMILY, PUBLICATION_REFERENCE_COL, guid, (List)references, false);
    }
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addPublication(java.lang.String, org.ala.model.Publication)
	 */
	public boolean addPublication(String guid, Publication publication) throws Exception {
		return storeHelper.put(TC_TABLE, TC_COL_FAMILY, PUBLICATION_COL, guid, publication);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getExtantStatus(java.lang.String)
	 */
	@Override
	public List<ExtantStatus> getExtantStatuses(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, EXTANT_STATUS_COL, guid, ExtantStatus.class);
	}
		
	/**
	 * @see org.ala.dao.TaxonConceptDao#getHabitats(java.lang.String)
	 */
	@Override
	public List<Habitat> getHabitats(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, HABITAT_COL, guid, Habitat.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#addExtantStatus(java.lang.String, org.ala.model.ExtantStatus)
	 */
	@Override
	public boolean addExtantStatus(String guid, List<ExtantStatus> extantStatusList) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY, EXTANT_STATUS_COL, guid, (List) extantStatusList, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#addHabitat(java.lang.String, org.ala.model.Habitat)
	 */
	@Override
	public boolean addHabitat(String guid, List<Habitat> habitatList) throws Exception {
		return storeHelper.putList(TC_TABLE, TC_COL_FAMILY, HABITAT_COL, guid, (List) habitatList, false);
	}

	/**
	 * @see org.ala.dao.TaxonConceptDao#getRegions(java.lang.String)
	 */
	@Override
	public List<OccurrencesInGeoregion> getRegions(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, REGION_COL, guid, OccurrencesInGeoregion.class);
	}

	/**
	 * Retrieve the references for the concept with the supplied GUID.
	 * 
	 * @see org.ala.dao.TaxonConceptDao#getReferencesFor(java.lang.String)
	 */
	public List<Reference> getReferencesFor(String guid) throws Exception {
		return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, REFERENCE_COL, guid, Reference.class);
	}
	
	/**
	 * @see org.ala.dao.TaxonConceptDao#getEarliestReferenceFor(java.lang.String)
	 */
    public Reference getEarliestReferenceFor(String guid) throws Exception{
        return (Reference) storeHelper.get(TC_TABLE, TC_COL_FAMILY, EARLIEST_REFERENCE_COL, guid, Reference.class);
        
    }
    
    /**
     * @see org.ala.dao.TaxonConceptDao#getPublicationReferencesFor(java.lang.String)
     */
    public List<Reference> getPublicationReferencesFor(String guid) throws Exception{
        return (List) storeHelper.getList(TC_TABLE, TC_COL_FAMILY, PUBLICATION_REFERENCE_COL, guid, Reference.class);
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
//    
//    /**
//     * @param guid
//     * @return
//     */
//    private Get getTcGetter(String guid) {
//    	Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(TC_COL_FAMILY));
//    	return getter;
//    }
//    
//    /**
//     * @param guid
//     * @return
//     */
//    private Get getTnGetter(String guid) {
//    	Get getter = new Get(Bytes.toBytes(guid)).addFamily(Bytes.toBytes(TN_COL_FAMILY));
//    	return getter;
//    }
    
	/**
	 * @param storeHelper the storeHelper to set
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
	 * @param solrUtils the solrUtils to set
	 */
	public void setSolrUtils(SolrUtils solrUtils) {
		this.solrUtils = solrUtils;
	}
}