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
package org.ala.repository;

/**
 * Namespaces in use by Document Mappers for triple generation.
 *
 * Note: Please only add namespaces that are in use to this interface definition.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Namespaces {
	/**
	 * Dublin Core Elements name space
	 */
	public final String DC_ELEMENTS = "http://purl.org/dc/elements/1.1/";
	/**
	 * Dublin Core Terms name space
	 */
	public final String DC_TERMS = "http://purl.org/dc/terms/";
	/**
	 * RDF name space
	 */
	public final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	/**
	 * TWDG Taxonomy Name name space
	 */
	public final String TWDG_TAXONNAME = "http://rs.tdwg.org/ontology/voc/TaxonName#";
	/**
	 * TWDG Taxonomy Concept name space
	 */
	public final String TWDG_TAXONCONCEPT = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
	/**
	 * TWDG Common name space
	 */
	public final String TWDG_COMMON = "http://rs.tdwg.org/ontology/voc/Common#";
	/**
	 * Namespace for ALA TDWG extensions.
	 */
	public final String ALA = "http://ala.org.au/ontology/ALA#";
	/**
	 * Namespace for Darwin Core Terms
	 */
	public final String DWC_TERMS = "http://rs.tdwg.org/dwc/terms/";
}
