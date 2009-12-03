/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: fri096 $
  * @version $Id: FedoraConstants.java 1012 2009-07-31 06:57:08Z fri096 $
  */


package csiro.diasb.fedora;

/**
 *
 * @author fri096
 */
public interface FedoraConstants {

  // From http://www.w3.org/TR/rdf-primer/
  
  /**
   * ALA Taxon Concept name space
   */
  public final String ALA_TAXONCONCEPT_NAMESPACE = "http://ala.org.au/ontology/TaxonConcept#";

  /**
   * ALA Taxon Name name space
   */
  public final String ALA_TAXONNAME_NAMESPACE = "http://ala.org.au/ontology/TaxonName#";

  /**
   * ALA Publication name space
   */
  public final String ALA_PUBLICATION_NAMESPACE = "http://ala.org.au/ontology/PublicationCitation#";

  /**
   * Dublin Core Elements name space
   */
  public final String DC_ELEMENTS_NAMESPACE="http://purl.org/dc/elements/1.1/";

  /**
   * Dublin Core Terms name space
   */
  public final String DC_TERMS_NAMESPACE = "http://purl.org/dc/terms/";
  
  /**
   * RDF name space
   */
  public final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  
  /**
   * OWL name space
   */
  public final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
  
  /**
   * TWDG Publication name space
   */
  public final String TWDG_PUBLICATION_NAMESPAE = "http://rs.tdwg.org/ontology/voc/PublicationCitation#";
  
  /**
   * TWDG Taxonomy Name name space
   */
  public final String TWDG_TAXONNAME_NAMESPACE = "http://rs.tdwg.org/ontology/voc/TaxonName#";
  
  /**
   * TWDG Taxonomy Concept name space
   */
   public final String TWDG_TAXONCONCEPT_NAMESPACE = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
  
  /**
   * TWDG Common name space
   */
  public final String TWDG_COMMON_NAMESPACE = "http://rs.tdwg.org/ontology/voc/Common#";
  
  /**
   * TDWG Taxonomy Concept Schema's namespace.  For XML elements with
   * prefix <code>tcs</code>
   */
  public final String TWDG_TAXONCONCEPT_SCHEMA_NAMESPACE = "http://www.tdwg.org/schemas/tcs/1.01";

  /**
   * FoXML format definition
   */
  public final String FOXML_FORMAT_1_1 = "info:fedora/fedora-system:FOXML-1.1";

  /**
   * The TWDG TCS name space.
   */
  public final String TWDG_TCS_NAMESPACE = "http://www.tdwg.org/schemas/tcs/1.01";

  /**
   * The TWDG Publication name space.
   */
  public final String TWDG_PUBLICATION_NAMESPACE = "http://rs.tdwg.org/ontology/voc/PublicationCitation#";

  /**
   * TWDG Collection name space.
   */
  public final String TWDG_COLLECTION_NAMESPACE = "http://rs.tdwg.org/ontology/voc/Collection#";

	/**
	 * The Fedora PID of the InfoSource content model.
	 */
	public final static String INFO_SOURCE_CM_PID = "ala:InfoSourceContentModel";

	/**
	 * The Fedora PID of the Publication content model.
	 */
	public final static String PUBLICATION_CM_PID = "ala:PublicationContentModel";
	
	/**
	 * The Fedora PID of the TaxonConcept content model.
	 */
	public final static String TAXON_CONCEPT_CM_PID = "ala:TaxonConceptContentModel";
	
	/**
	 * The Fedora PID of the TaxonName content model.
	 */
	public final static String TAXON_NAME_CM_PID = "ala:TaxonNameContentModel";

	/**
	 * The Fedora PID of the TaxonName content model.
   *
   * @since v0.4
	 */
	public final static String REFERENCE_CM_PID = "ala:ReferenceContentModel";
	
	/**
	 * The sDep for the SourcedProperties disseminator.
	 */
	public final static String SOURCED_PROPERTY_SDEP_PID = "ala:sourcedPropertiesServiceDep";

	/**
   * The sDef for the SourcedProperties disseminator.
   */
	public final static String SOURCED_PROPERTY_SDEF_PID = "ala:sourcedPropertiesServiceDef";

	/**
	 * The Fedora triple's predicate "hasModel" for ALA.
	 */
	public final static String ALA_HAS_MODEL = "info:fedora/fedora-system:def/model#hasModel";
	
	/**
	 * This prefix is to be put before any PID in a RELS_EXT relationship.
	 */
	public final static String FEDORA_RELATIONSHIP_PREFIX = "info:fedora/";
}
