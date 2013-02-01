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

import java.util.List;
import java.util.Map;

import org.ala.dto.ExtendedTaxonConceptDTO;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.dto.SpeciesProfileDTO;
import org.ala.model.BaseRanking;
import org.ala.model.Category;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.ExtantStatus;
import org.ala.model.Habitat;
import org.ala.model.IdentificationKey;
import org.ala.model.Image;
import org.ala.model.InfoSource;
import org.ala.model.OccurrencesInGeoregion;
import org.ala.model.PestStatus;
import org.ala.model.Publication;
import org.ala.model.Reference;
import org.ala.model.SensitiveStatus;
import org.ala.model.SimpleProperty;
import org.ala.model.SpecimenHolding;
import org.ala.model.SynonymConcept;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
import org.ala.util.ColumnType;
import org.ala.util.StatusType;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.SolrInputDocument;

import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.model.NameSearchResult;
import au.org.ala.data.model.LinnaeanRankClassification;

/**
 * Interface for creating, changing and searching taxon concept
 * profiles.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface TaxonConceptDao {

	/**
	 * Retrieve the synonyms for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<SynonymConcept> getSynonymsFor(String guid) throws Exception;

	/**
	 * Add an alternative identifier (GUID) for this taxon concept or 
	 * a concept asserted to be congruent.
	 * 
	 * @param guid
	 * @param alternativeIdentifier
	 * @return
	 * @throws Exception
	 */
	boolean addIdentifier(String guid, String alternativeIdentifier) throws Exception;
	
	/**
	 * Retrieve the link identifier value for this concept.
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public String getLinkIdentifier(String guid) throws Exception;
	
	/**
	 * Set the link identifer for this taxon concept. A link identifier is a value that allows
	 * unique linking. This is primarily to enable human readable URLs. Hence an example of a link identifier
	 * would be "Macropus rufus" if this string uniquely identifies the concept within the system.
	 * If it does not, then we roll back to using the GUID. 
	 * 
	 * @param guid
	 * @param linkIdentifier
	 * @throws Exception
	 */
	boolean setLinkIdentifier(String guid, String linkIdentifier) throws Exception;
	
	/**
	 * Retrieve the images associated with this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<Image> getImages(String guid) throws Exception;
	
	/**
	 * Retrieve the images illustrating the distribution of this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<Image> getDistributionImages(String guid) throws Exception;

	/**
	 * Retrieve a list of alternative identifiers (guids) associated with
	 * this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<String> getIdentifiers(String guid) throws Exception;
	
	/**
	 * Retrieve the pest status associated with this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<PestStatus> getPestStatuses(String guid) throws Exception;
	
	List<Category> getCategories(String guid) throws Exception;


	/**
	 * Retrieve the conservation status associated with this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<ConservationStatus> getConservationStatuses(String guid) throws Exception;

	/**
	 * Retrieve the extant status associated with this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<ExtantStatus> getExtantStatuses(String guid) throws Exception;

	/**
	 * Retrieve the habitat associated with this taxon concept.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<Habitat> getHabitats(String guid) throws Exception;
	
    /**
     * Retreive the region(s) associated with this taxon concept.
     * @param guid
     * @return
     * @throws Exception
     */
    List<OccurrencesInGeoregion> getRegions(String guid) throws Exception;
    
	/**
	 * Retrieve the child concepts for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<TaxonConcept> getChildConceptsFor(String guid) throws Exception;

	/**
	 * Retrieve the parent concepts for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<TaxonConcept> getParentConceptsFor(String guid) throws Exception;

	/**
	 * Retrieve the common names for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<CommonName> getCommonNamesFor(String guid) throws Exception;

	/**
	 * Retrieve the text properties for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<SimpleProperty> getTextPropertiesFor(String guid) throws Exception;

	/**
	 * Replace the text properties for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	void setTextPropertiesFor(String guid, List<SimpleProperty> simpleProperties) throws Exception;

	/**
	 * Retrieve the references for this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<Reference> getReferencesFor(String guid) throws Exception;

    /**
     * Retrieves the earliest reference for this taxon concept.
     *
     * @param guid
     * @return
     * @throws Exception
     */
    Reference getEarliestReferenceFor(String guid) throws Exception;

    /**
     * Retrieve the publications that are marked against this taxon concept.
     * 
     * @param guid
     * @return
     * @throws Exception
     */
    List<Reference> getPublicationReferencesFor(String guid) throws Exception;

    /**
     * Retrieves the occurrence count for this taxon concept
     * @param guid
     * @return
     * @throws Exception
     */
    Integer getOccurrenceRecordCount(String guid) throws Exception;
    
    /**
     * Retrieves the number of occurrences that are georeferenced for this taxon concept
     * @param guid
     * @return
     * @throws Exception
     */
    Integer getGeoreferencedRecordsCount(String guid) throws Exception;

	/**
	 * Store the following taxon concept
	 *
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	boolean create(TaxonConcept tc) throws Exception;

	/**
	 * Update the taxon concept.
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	boolean update(TaxonConcept tc) throws Exception;
	
	/**
	 * What about multiple taxon names for each taxon concept???
	 *
	 * @param guid
	 * @param tn
	 * @throws Exception
	 */
	boolean addTaxonName(String guid, TaxonName tn) throws Exception;
	
	boolean addSameAsTaxonConcept(String guid, TaxonConcept tc) throws Exception;
	
	List<TaxonConcept> getSameAsFor(String guid) throws Exception;

	/**
	 * Add this common name to the Taxon Concept.
	 *
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	boolean addCommonName(String guid, CommonName commonName) throws Exception;

	/**
	 * Add this conservation status to the Taxon Concept.
	 *
	 * @param guid
	 * @param conservationStatus
	 * @throws Exception
	 */
	boolean addConservationStatus(String guid, ConservationStatus conservationStatus) throws Exception;

	/**
	 * Add this pest status to the Taxon Concept.
	 *
	 * @param guid
	 * @param pestStatus
	 * @throws Exception
	 */
	boolean addPestStatus(String guid, PestStatus pestStatus) throws Exception;

	
	/**
     * Add this category to the Taxon Concept.
     *
     * @param guid
     * @throws Exception
     */
    boolean addCategory(String guid, Category category) throws Exception;

	/**
	 * Add extant status list to the Taxon Concept.
	 *
	 * @param guid
	 * @param extantStatusList
	 * @throws Exception
	 */
	boolean addExtantStatus(String guid, List<ExtantStatus> extantStatusList) throws Exception;

	/**
	 * Add habitat list to the Taxon Concept.
	 *
	 * @param guid
	 * @param habitatList
	 * @throws Exception
	 */
	boolean addHabitat(String guid, List<Habitat> habitatList) throws Exception;
	
	boolean addHabitat(String guid, List<Habitat> habitatList, boolean append) throws Exception;

	/**
	 * Add this list of regions to the Taxon Concept.
	 *
	 * @param guid
	 * @param regions
	 * @throws Exception
	 */
	boolean addRegions(String guid, List<OccurrencesInGeoregion> regions) throws Exception;

    /**
     * Adds the number of BioCache occurrences for the Taxon Concept.
     * @param guid
     * @param count
     * @return
     * @throws Exception
     */
    boolean setOccurrenceRecordsCount(String guid, Integer count) throws Exception;

    /**
     * Adds the number of BioCache occurrences for the Taxon Concept.
     * @param guid
     * @param count
     * @return
     * @throws Exception
     */
    boolean setGeoreferencedRecordsCount(String guid, Integer count) throws Exception;

	/**
	 * Add this image to the Taxon Concept.
	 * 
	 * @param guid
	 * @param image
	 * @throws Exception
	 */
	boolean addImage(String guid, Image image) throws Exception;

	/**
	 * Add this image illustrating the distribution of a the Taxon Concept.
	 * 
	 * @param guid
	 * @param image
	 * @throws Exception
	 */
	boolean addDistributionImage(String guid, Image image) throws Exception;
	
	/**
	 * Add a synonym to this concept.
	 *
	 * @param guid
	 * @param synonym
	 * @throws Exception
	 */
	boolean addSynonym(String guid, SynonymConcept synonym) throws Exception;

	/**
	 * Add a congruent concept.
	 * 
	 * @param guid
	 * @param congruent
	 * @throws Exception
	 */
	boolean addIsCongruentTo(String guid, TaxonConcept congruent) throws Exception;

	/**
	 * Add a child taxon to this concept.
	 *
	 * @param guid
	 * @param childConcept
	 * @throws Exception
	 */
	boolean addChildTaxon(String guid, TaxonConcept childConcept) throws Exception;
	
	/**
	 * Sets the child taxa for this concept.
	 *
	 * @param guid
	 * @throws Exception
	 */
	boolean setChildTaxa(String guid, List<TaxonConcept> childConcepts) throws Exception;

	/**
	 * Add a parent taxon to this concept.
	 *
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	boolean addParentTaxon(String guid, TaxonConcept parentConcept) throws Exception;

	/**
	 * Add a text property to this concept.
	 *
	 * @param guid
	 * @throws Exception
	 */
	boolean addTextProperty(String guid, SimpleProperty textProperty) throws Exception;

    /**
     * Add text properties. This will remove existing properties already added for this document.
     *
     * @param guid
     * @param textProperties
     * @return
     * @throws Exception
     */
    boolean addTextProperties(String guid, List<SimpleProperty> textProperties) throws Exception;

	/**
	 * Create a batch of taxon concepts.
	 *
	 * @param taxonConcepts
	 * @throws Exception
	 */
	void create(List<TaxonConcept> taxonConcepts) throws Exception;

	/**
	 * Retrieve the taxon concept by guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	TaxonConcept getByGuid(String guid) throws Exception;

	/**
	 * Retrieve the entire profile data for a taxon concept by guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid) throws Exception;
	
	/**
	 * Retrieve the entire profile data for a taxon concept by guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid, boolean checkedPreferred) throws Exception;
	
	/**
	 * Retrieve the entire profile data for a taxon concept by guid.
	 * When checkSynonym is true the guid is looked up as a synonym if the other search fails 
	 * 
	 * @param guid
	 * @param checkedPreferred
	 * @param checkSynonym
	 * @return
	 * @throws Exception
	 */
	ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(String guid, boolean checkedPreferred, boolean checkSynonym) throws Exception;

    /**
     * Retrieve the entire profile data for a taxon concept by guid.
     *
     * @param guids
     * @return
     * @throws Exception
     */
    List<ExtendedTaxonConceptDTO> getExtendedTaxonConceptByGuids(List<String> guids) throws Exception;

	/**
	 * Retrieve the Taxon Name for the supplied GUID.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	TaxonName getTaxonNameFor(String guid) throws Exception;

	/**
	 * Search for taxon concept with the following scientific name.
	 *
	 * @param scientificName
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	List<SearchTaxonConceptDTO> findByScientificName(String scientificName, int limit) throws Exception;

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
	SearchResultsDTO findByScientificName(String input,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws Exception;

	/**
	 * Find all TCs with a pest/conservation status (any value)
	 *
	 * @param statusType
	 * @param startIndex
	 * @param pageSize
	 * @param sortField
	 * @param sortDirection
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	SearchResultsDTO findAllByStatus(StatusType statusType,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws ParseException, Exception;

	/**
	 * Get LSID from Checklist Bank by kingdom, genus and scientific name.
	 * 
	 * @param scientificName Required.
	 * @param classification Required.
	 * @param taxonRank Can be null.
	 * @return LSID or null.
	 */
	String findLsidByName(String scientificName, LinnaeanRankClassification classification, String taxonRank);
	
	String findLsidByName(String scientificName, LinnaeanRankClassification classification, String taxonRank, boolean useSoundEx);
	
	/**
	 * Searches the name matching index for an LSID based on the supplied classification.  When addMissingName=true scientific names that can not be
	 * found are added to the index AND data store as an ALA concept.
	 * @param scientificName
	 * @param classification
	 * @param taxonRank
	 * @param useSoundEx
	 * @param addMissingName
	 * @return
	 */
	String findLsidByName(String scientificName, LinnaeanRankClassification classification, String taxonRank, String authority, InfoSource infoSource, boolean useSoundEx, boolean addMissingName);
	
	void refreshNameIndex() throws Exception;
	
	/**
	 * Get LSID from Checklist Bank by scientific name.
	 * 
	 * @param scientificName Required.
	 * @param taxonRank Can be null.
	 * @return LSID or null.
	 */
	String findLsidByName(String scientificName, String taxonRank);
	
	/**
	 * Find the LSID for the supplied name
	 * 
	 * @param scientificName
	 * @return
	 */
	String findLsidByName(String scientificName);
	
	String findLsidByName(String scientificName, boolean useSoundEx);
	
	/**
	 * Get Checklist Bank entry by scientific name.
	 * 
	 * @param scientificName Required.
	 * @param classification Required.
	 * @param rank Can be null.
	 * @return 
	 */
	NameSearchResult findCBDataByName(String scientificName, LinnaeanRankClassification classification, String rank) throws Exception;
	
	/**
	 * Attempts to find the lsid for the supplied species in the name matching. This method should only be used for 
	 * searching purposes because it allows homonyms to be reolved unde the following conditions:
	 * <ul>
	 * <li>The NSL only has one of the homonyms</li>
	 * <li>The NSL has multiple values but only one is accepted</li>
	 * </ul> 
	 * 
	 * @param scientificName The name to locate
	 * @return The lsid of the supplied name OR null if not found
	 * @throws Exception
	 */
	String findLsidForSearch(String scientificName);
	/**
	 * 
	 * @see #findLsidForSearch(String)
	 * Allows the search to use sound like expressions to perform a match.
	 * 
	 * @param scientificName
	 * @param useSoundEx
	 * @return
	 * @throws Exception
	 */
	String findLsidForSearch(String scientificName, boolean useSoundEx);

    /**
     * Reports the name matching statistics to the supplied output stream
     * @param outputStream
     * @throws Exception
     */
    void reportStats(java.io.OutputStream outputStream, String prefix) throws Exception;

    /**
     * Resets the name matching statistics
     */
    void resetStats();
	
	/**
	 * Retrieve a list of concepts with the supplied parent guid.
	 *
	 * @param parentGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	List<SearchTaxonConceptDTO> getByParentGuid(String parentGuid, int limit) throws Exception;

	/**
	 * Delete the TaxonConcept for the supplied guid
	 *
	 * @param guid
	 * @return true if a delete was performed
	 * @throws Exception
	 */
	boolean delete(String guid) throws Exception;
	
	/**
	 * Delete the TaxonConcept for the supplied guid
	 *
	 * @param infoSourceIds
	 * @return true if a delete was performed
	 * @throws Exception
	 */
	boolean deleteForInfosources(String[] infoSourceIds) throws Exception;

	/**
	 * Synchronises these triples to a taxon concept in hbase.
	 *
	 * @return true if we where able to add these properties to an existing
	 * taxon. False otherwise
	 *
	 * @param document the document supplying the triples
	 * @param triples the triples to add
     * @param statsOnly true when we only want to record statistics.
     * false when we want to add it to the repository
	 * @throws Exception
	 */
	boolean syncTriples(org.ala.model.Document document, List<Triple> triples, Map<String,String> dublinCore, boolean statsOnly) throws Exception;

	/**
	 * Synchronises these triples to a taxon concept in hbase.
	 *
	 * @return Not null if we where able to add these properties to an existing
	 * taxon. Null otherwise
	 *
	 * @param document the document supplying the triples
	 * @param triples the triples to add
     * @param statsOnly true when we only want to record statistics.
     * false when we want to add it to the repository
     * @param reindex true when we want to do reindex after update cassandra, otherwise no reindex
	 * @throws Exception
	 */	
	public String syncTriples(org.ala.model.Document document, List<Triple> triples, Map<String, String> dublinCore, boolean statsOnly, boolean reindex) throws Exception;
	
	/**
	 * Clear the associated properties from each taxon concept.
	 *
	 * Clear the triples in the "raw:" column family.
	 *
	 * @throws Exception
	 */
	void clearRawProperties() throws Exception;

	/**
	 * Create a index to support searching.
	 *
	 * @throws Exception
	 */
	void createIndex(String startKey, boolean remove) throws Exception;
	
	/**
	 * Reindexes a list of taxa based on the guid. 
	 * @param guids
	 * @throws Exception
	 */
	void reindexTaxa(List<String> guids) throws Exception;

	/**
	 * Add a classification to this taxon.
	 *
	 * @param guid
	 * @param classification
	 */
	boolean addClassification(String guid, Classification classification) throws Exception;

	/**
	 * Retrieve the classifications associated with this taxon.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<Classification> getClassifications(String guid) throws Exception;
	
	/**
	 * Adds a (literature) reference to this taxon.
	 * 
	 * @param references
	 */
	boolean addReferences(String guid, List<Reference> references) throws Exception;

    /**
     * Adds the "earliest" reference to this taxon.
     *
     * @param guid
     * @param reference
     * @return
     * @throws Exception
     */
    boolean addEarliestReference(String guid, Reference reference) throws Exception;

    /**
     * Adds the publication reference for this taxon concept.
     * 
     * @param guid
     * @param reference
     * @return
     * @throws Exception
     */
    boolean addPublicationReference(String guid, List<Reference> reference) throws Exception;

	/**
	 * Add a publication to the profile.
	 * 
	 * @param guid
	 * @param publication
	 */
	boolean addPublication(String guid, Publication publication) throws Exception;

	/**
	 * Add an image ranking
	 * 
	 * @param taxonGuid
	 * @param imageUri
	 * @param positive
	 * @return
	 * @throws Exception
	 */
	boolean setRankingOnImage(String taxonGuid, String imageUri, boolean positive) throws Exception;
	
	/**
	 * Set the ranking on the supplied image
	 * 
	 * @param taxonGuid
	 * @param imageUri
	 * @param positive
	 * @param blackList
	 * @return
	 * @throws Exception
	 */
	boolean setRankingOnImage(String taxonGuid, String imageUri, boolean positive, boolean blackList) throws Exception;

    /**
     * Sets the image ranking to the specified rank and count
     * @param taxonGuid
     * @param rankings
     * @return
     * @throws Exception
     */
    boolean setRankingOnImages(String taxonGuid, Map<String, Integer[]> rankings) throws Exception;
	
	/**
	 * Set the concept with the supplied guid to iconic
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	boolean setIsIconic(String guid) throws Exception;
	
	/**
	 * Is this concept iconic ?
	 * @param guid
	 * @return
	 * @throws Exception
	 */
    boolean isIconic(String guid) throws Exception;

    /**
     * Set the flag indicating this species is Australian
     * 
     * @param guid
     * @return
     * @throws Exception
     */
    boolean setIsAustralian(String guid) throws Exception;
	  
	boolean setIsAustralian(String guid, boolean bool) throws Exception;

    boolean setIsAustralian(String guid, boolean bool, boolean reindex) throws Exception;
    /**
     * Is this concept Aussie ?
     * 
     * @param guid
     * @return
     * @throws Exception
     */
    boolean isAustralian(String guid) throws Exception;
     
    /**
     * Retrieve the preferred GUID for this concept. This resolves CoL ids to AFD ids
     * for example.
     * 
     * @param guid
     * @return
     * @throws Exception
     */
    String getPreferredGuid(String guid) throws Exception;
    
    /**
     * Add identification keys
     * 
     * @param guid
     * @param identificationKeyList
     * @return
     * @throws Exception
     */
	boolean addIdentificationKeys(String guid, List<IdentificationKey> identificationKeyList) throws Exception;	
	
	/**
	 * Retrieve a list of identification keys for this concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<IdentificationKey> getIdentificationKeys(String guid) throws Exception;
    
	/**
	 * Add a specimen holding for this concept.
	 * 
	 * @param guid
	 * @param specimenHoldingList
	 * @return
	 * @throws Exception
	 */
	boolean addSpecimenHoldings(String guid, List<SpecimenHolding> specimenHoldingList) throws Exception;

	/**
	 * append new list into a specimen holding for this concept.
	 * 
	 * @param guid
	 * @param specimenHoldingList
	 * @return
	 * @throws Exception
	 */	
	public boolean appendSpecimenHoldings(String guid, List<SpecimenHolding> specimenHoldingList) throws Exception;
	
	/**
	 * Get the specimen holdings for this concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	List<SpecimenHolding> getSpecimenHoldings(String guid) throws Exception;

	/**
	 * Retrieve the synonyms for the Taxon Concept with the supplied guid.
	 *
	 * @param startGuid
	 * @throws Exception
	 */
	List<ExtendedTaxonConceptDTO> getPage(String startGuid, int pageSize) throws Exception;
	
	/**
	 * Retrieve the synonyms for the Taxon Concept with the supplied guid.
	 *
	 * @param startGuid
	 * @throws Exception
	 */
	List<SpeciesProfileDTO> getProfilePage(String startGuid, int pageSize) throws Exception;

	/**
	 * Add a sensitive status to the supplied taxon concept.
	 * 
	 * @param guid
	 * @param ss
	 */
	void addSensitiveStatus(String guid, SensitiveStatus ss) throws Exception;
	
	/**
     * Returns the LSID for the CB name usage for the supplied common name.
     *
     * When the common name returns more than 1 hit a result is only returned if all the scientific names match
     * 
     * @param commonName
     * @return the lsid, null if not found
     */	
	String findLSIDByCommonName(String commonName);
	
	String findLSIDByConcatName(String concatName);
	
	boolean setRanking(String guid, ColumnType columnType, BaseRanking ir)throws Exception;

    boolean setRanking(String guid, ColumnType columnType, BaseRanking baseRanking, boolean reindex)throws Exception;

    List<SolrInputDocument> indexTaxonConcept(String guid) throws Exception;

    List<SolrInputDocument> indexTaxonConcept(String guid,Scanner scanner) throws Exception;
	
	void resetRanking(String guid, ColumnType columnType, Integer value)throws Exception;
}