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
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.ConservationStatus;
import org.ala.model.Image;
import org.ala.model.OccurrencesInRegion;
import org.ala.model.PestStatus;
import org.ala.model.SimpleProperty;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
import org.ala.util.StatusType;
import org.ala.vocabulary.Vocabulary;
import org.apache.lucene.queryParser.ParseException;
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
	public List<TaxonConcept> getSynonymsFor(String guid)
			throws Exception;

	/**
	 * Retrieve the images associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<Image> getImages(String guid) throws Exception;

	/**
	 * Retrieve the pest status associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<PestStatus> getPestStatuses(String guid)
			throws Exception;

	/**
	 * Retrieve the conservation status associated with this taxon concept.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<ConservationStatus> getConservationStatuses(String guid)
			throws Exception;

	/**
	 * Retrieve the child concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid)
			throws Exception;

	/**
	 * Retrieve the parent concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getParentConceptsFor(String guid)
			throws Exception;

	/**
	 * Retrieve the common names for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<CommonName> getCommonNamesFor(String guid)
			throws Exception;

	/**
	 * Retrieve the text properties for the Taxon Concept with the supplied guid.
	 *
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<SimpleProperty> getTextPropertiesFor(String guid)
			throws Exception;

	/**
	 * Store the following taxon concept
	 * 
	 * FIXME Switch to using a single column for TaxonConcept
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	public boolean create(TaxonConcept tc) throws Exception;

	/**
	 * What about multiple taxon names for each taxon concept???
	 * 
	 * FIXME Switch to using a single column for TaxonName(s)
	 * 
	 * @param guid
	 * @param tn
	 * @throws Exception
	 */
	public void addTaxonName(String guid, TaxonName tn)
			throws Exception;

	/**
	 * Add this common name to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addCommonName(String guid, CommonName commonName)
			throws Exception;

	/**
	 * Add this conservation status to the Taxon Concept.
	 * 
	 * @param guid
	 * @param conservationStatus
	 * @throws Exception
	 */
	public void addConservationStatus(String guid,
			ConservationStatus conservationStatus) throws Exception;

	/**
	 * Add this pest status to the Taxon Concept.
	 * 
	 * @param guid
	 * @param pestStatus
	 * @throws Exception
	 */
	public void addPestStatus(String guid, PestStatus pestStatus)
			throws Exception;

	/**
	 * Add this occurrences in region to the Taxon Concept.
	 * 
	 * @param guid
	 * @param occurrencesInRegion
	 * @throws Exception
	 */
	public void addOccurrencesInRegion(String guid, OccurrencesInRegion occurrencesInRegion) 
			throws Exception;

	/**
	 * Add this image to the Taxon Concept.
	 * 
	 * @param guid
	 * @param image
	 * @throws Exception
	 */
	public void addImage(String guid, Image image) throws Exception;

	/**
	 * Add a synonym to this concept.
	 * 
	 * @param guid
	 * @param synonym
	 * @throws Exception
	 */
	public void addSynonym(String guid, TaxonConcept synonym)
			throws Exception;

	public void addIsSynonymFor(String guid,
			TaxonConcept acceptedConcept) throws Exception;

	public void addIsCongruentTo(String guid, TaxonConcept congruent)
			throws Exception;

	/**
	 * Add a child taxon to this concept.
	 * 
	 * @param guid
	 * @param childConcept
	 * @throws Exception
	 */
	public void addChildTaxon(String guid, TaxonConcept childConcept)
			throws Exception;

	/**
	 * Add a parent taxon to this concept.
	 * 
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	public void addParentTaxon(String guid, TaxonConcept parentConcept)
			throws Exception;

	/**
	 * Add a text property to this concept.
	 *
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	public void addTextProperty(String guid,
			SimpleProperty textProperty) throws Exception;

	/**
	 * Create a batch of taxon concepts.
	 * 
	 * @param taxonConcepts
	 * @throws Exception
	 */
	public void create(List<TaxonConcept> taxonConcepts)
			throws Exception;

	/**
	 * Retrieve the taxon concept by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaxonConcept getByGuid(String guid) throws Exception;

	/**
	 * Retrieve the entire profile data for a taxon concept by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public ExtendedTaxonConceptDTO getExtendedTaxonConceptByGuid(
			String guid) throws Exception;

	/**
	 * Retrieve the Taxon Name for the supplied GUID.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception;

	/**
	 * Search for taxon concept with the following scientific name.
	 * 
	 * @param scientificName
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<SearchTaxonConceptDTO> findByScientificName(
			String input, int limit) throws Exception;

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
	public SearchResultsDTO findByScientificName(String input,
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
	public SearchResultsDTO findAllByStatus(StatusType statusType,
			Integer startIndex, Integer pageSize, String sortField,
			String sortDirection) throws ParseException, Exception;

	/**
	 * Get TaxonConcept GUID by genus and scientific name.
	 * 
	 * TODO replace this with the indexes generated from ChecklistBank
	 * 
	 * @param scientificName
	 * @return
	 * @throws Exception
	 */
	public String findConceptIDForName(String kingdom, String genus,
			String scientificName) throws Exception;

	/**
	 * Retrieve a list of concepts with the supplied parent guid.
	 * 
	 * @param parentGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<SearchTaxonConceptDTO> getByParentGuid(
			String parentGuid, int limit) throws Exception;

	/**
	 * Delete the TaxonConcept for the supplied guid
	 * 
	 * @param guid
	 * @return true if a delete was performed
	 * @throws Exception
	 */
	public boolean delete(String guid) throws Exception;

	/**
	 * Synchronises these triples to a taxon concept in hbase.
	 * 
	 * @return true if we where able to add these properties to an existing
	 * taxon. False otherwise
	 * 
	 * @param infosourceId the infosource supplying the triples
	 * @param document the document supplying the triples
	 * @param triples the triples to add
	 * @param the filepath of the document
	 * @throws Exception
	 */
	public boolean syncTriples(org.ala.model.Document document,
			List<Triple> triples) throws Exception;

	/**
	 * Clear the associated properties from each taxon concept.
	 * 
	 * Clear the triples in the "raw:" column family.
	 * 
	 * @throws Exception
	 */
	public void clearRawProperties() throws Exception;

	/**
	 * Create a index to support searching.
	 * 
	 * @throws Exception
	 */
	public void createIndex() throws Exception;

	/**
	 * @param vocabulary the vocabulary to set
	 */
	public void setVocabulary(Vocabulary vocabulary);
	
	/**
	 * Retrieve the raw properties
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPropertiesFor(String guid) throws Exception;

	/**
	 * Add a classification to this taxon.
	 * 
	 * @param guid
	 * @param classification
	 */
	public void addClassification(String guid, Classification classification) throws Exception;
	
	/**
	 * Retrieve the classifications associated with this taxon.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<Classification> getClassifications(String guid) throws Exception;
	
	/**
	 * Sets a new Lucene IndexSearcher for the supplied index directory.
	 * 
	 * @param indexDir the location of the lucene index
	 * @throws Exception
	 */
	public void setLuceneIndexLocation(String indexDir) throws Exception;
}