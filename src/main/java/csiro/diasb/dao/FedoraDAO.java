/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */
package csiro.diasb.dao;

import java.util.List;

import csiro.diasb.datamodels.DocumentDTO;
import csiro.diasb.datamodels.HtmlPageDTO;
import csiro.diasb.datamodels.ImageDTO;
import csiro.diasb.datamodels.OrderedDocumentDTO;
import csiro.diasb.datamodels.TaxonConceptDTO;
import csiro.diasb.datamodels.TaxonNameDTO;

/**
 * Interface for the data access of Fedora Commons documents/objects
 *
 * @author "Nick dos Remedios (dos009) <Nick.dosRemedios@csiro.au>"
 */
public interface FedoraDAO {

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
     * @return
     */
    public TaxonConceptDTO getTaxonConceptForIdentifier(String lsid);
    
    /**
     * Return a list of ImageDTOs for a given list of scientific names
     * 
     * @param scientificNames
     * @return
     */
    public List<ImageDTO> getImagesForScientificNames(List<String> scientificNames);
}
