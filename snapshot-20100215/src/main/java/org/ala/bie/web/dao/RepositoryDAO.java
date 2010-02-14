/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package org.ala.bie.web.dao;

import org.ala.bie.web.dto.HtmlPageDTO;
import org.ala.bie.web.dto.ImageDTO;
import org.ala.bie.web.dto.TaxonNameDTO;
import java.util.List;
import org.ala.bie.web.dto.DocumentDTO;
import org.ala.bie.web.dto.OrderedDocumentDTO;
import org.ala.bie.web.dto.OrderedPropertyDTO;
import org.ala.bie.web.dto.SolrResultsDTO;
import org.ala.bie.web.dto.TaxonConceptDTO;

/**
 * Interface for the data access for BIE objects
 *
 * @author "Nick dos Remedios (dos009) <Nick.dosRemedios@csiro.au>"
 */
public interface RepositoryDAO {

	/**
     * Return a list of TaxonNameDTOs for a given list of Taxon Name idenitifiers (urn.*)
     *
     * @param hasTaxonNames
     * @return
     */
    public List<TaxonNameDTO> getTaxonNamesForUrns(List<String> hasTaxonNames);

    /**
     * Return a list of ImageDTOs for a given list of scientific names
     *
     * @param scientificNames
     * @return
     */
    public List<DocumentDTO> getDocumentsForName(List<String> scientificNames);

    /**
     * Returns a list of ordered documents.
     *
     * @param scientificNames
     * @return
     */
	public List<OrderedDocumentDTO> getOrderedDocumentsForName(List<String> scientificNames);

    /**
     * Returns a list of ordered documents.
     *
     * @param scientificNames
     * @return
     */
	public List<OrderedPropertyDTO> getOrderedPropertiesForName(List<String> scientificNames);

    /**
     * Return a list of HtmlPageDTOs for a given list of scientific names
     *
     * @param scientificNames
     * @return
     */
    public List<HtmlPageDTO> getHtmlPagesForScientificNames(List<String> scientificNames);

    /**
     * Get a FC TaxonConcept for a identifier string (either PID or LSID)
     *
     * @param lsid
     * @return TaxonConceptDTO
     */
    public TaxonConceptDTO getTaxonConceptForIdentifier(String lsid);

    /**
     * Return a list of ImageDTOs for a given list of scientific names
     *
     * @param scientificNames
     * @return
     */
    public List<ImageDTO> getImagesForScientificNames(List<String> scientificNames);
    /**
     * Temporary method/hack to set the URL in one place - TODO move into property file
     *
     * @return the SOLR URL
     */
    public String getServerUrl();
     /**
     * Return a SearchResultDTO from a full-text search
     *
      * @param query
      * @param facetQuery 
      * @param startIndex
      * @param pageSize 
      * @param sortField 
      * @param sortDirection
      * @return SolrResultsDTO
     */
    public SolrResultsDTO getFullTextSearchResults(String query, String facetQuery, Integer startIndex, Integer pageSize, String sortField, String sortDirection);
}
