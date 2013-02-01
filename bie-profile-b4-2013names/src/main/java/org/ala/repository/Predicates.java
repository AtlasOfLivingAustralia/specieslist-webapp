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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An enum of predicates in use in document mapper implementations.
 *
 * Predicates should be referenced in this OWL document:
 * 
 * @link http://www2.ala.org.au/ontology/ALA.owl 
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public enum Predicates {

	// Dublin Core
	DC_IDENTIFIER(Namespaces.DC_ELEMENTS,"identifier",1000),
	DC_CREATOR(Namespaces.DC_ELEMENTS,"creator",1000),
	DC_TITLE(Namespaces.DC_ELEMENTS,"title",1000),
	DC_DESCRIPTION(Namespaces.DC_ELEMENTS,"description",1000),
	DC_FORMAT(Namespaces.DC_ELEMENTS,"format",1000),
    DC_MODIFIED(Namespaces.DC_ELEMENTS,"modified",1000),
    DC_PUBLISHER(Namespaces.DC_ELEMENTS,"publisher",1000),
    DC_SOURCE(Namespaces.DC_ELEMENTS,"source",1000),
    DC_SUBJECT(Namespaces.DC_ELEMENTS,"subject",1000),
    DC_IS_PART_OF(Namespaces.DC_ELEMENTS,"isPartOf",1000),
    DC_LICENSE(Namespaces.DC_ELEMENTS,"license",1000),
    DC_RIGHTS(Namespaces.DC_ELEMENTS,"rights",1000),
	
	// Darwin core
	LATITUDE(Namespaces.DWC_TERMS, "verbatimLatitude",1000),
	LONGITUDE(Namespaces.DWC_TERMS,"verbatimLongitude",1000),
	STATE_PROVINCE(Namespaces.DWC_TERMS,"stateProvince",1000),
	COUNTRY(Namespaces.DWC_TERMS, "country",1000),
	LOCALITY(Namespaces.DWC_TERMS, "verbatimLocality",1000),
	
	// ALA specific
	SCIENTIFIC_NAME(Namespaces.ALA, "hasScientificName",1000),
	COMMON_NAME(Namespaces.ALA, "hasCommonName",1000),
	FAMILY_COMMON_NAME(Namespaces.ALA, "hasFamilyCommonName",1000),
	NATIVE_NAME(Namespaces.ALA, "hasNativeName",1000),
	
	// Status info
	CONSERVATION_STATUS(Namespaces.ALA, "hasConservationStatus",400),
	PEST_STATUS(Namespaces.ALA, "hasPestStatus",410),
    // Text properties
    DESCRIPTIVE_TEXT(Namespaces.ALA, "hasDescriptiveText",500),
	DISTRIBUTION_TEXT(Namespaces.ALA, "hasDistributionText",510),
	MORPHOLOGICAL_TEXT(Namespaces.ALA, "hasMorphologicalText",520),
	HABITAT_TEXT(Namespaces.ALA, "hasHabitatText",530),
	ECOLOGICAL_TEXT(Namespaces.ALA, "hasEcologicalText",540),
	DIET_TEXT(Namespaces.ALA, "hasDietText",550),
	REPRODUCTION_TEXT(Namespaces.ALA, "hasReproductionText",560),
	FLOWERING_SEASON(Namespaces.ALA, "hasFloweringSeason",570),
	CONSERVATION_TEXT(Namespaces.ALA, "hasConservationText",580),
	THREATS_TEXT(Namespaces.ALA, "hasThreatsText",590),
	POPULATE_ESTIMATE(Namespaces.ALA, "hasPopulateEstimate",600),
	REFERENCE(Namespaces.ALA, "hasReference",700),
    // Misc
	SCIENTIFIC_DESCRIPTION_AUTHOR(Namespaces.ALA, "hasScientificDescriptionAuthor",1000),
	OCCURRENCES_IN_REGION(Namespaces.ALA, "hasOccurrencesInRegion",1000),
	NAME_STATUS(Namespaces.ALA, "hasNameStatus",1000),

	//taxonomic fields
	KINGDOM(Namespaces.ALA, "hasKingdom",1000),
	PHYLUM(Namespaces.ALA, "hasPhylum",1000),
	SUB_PHYLUM(Namespaces.ALA, "hasSubPhylum",1000),
	DIVISION(Namespaces.ALA, "hasDivision",1000),
	CLASS(Namespaces.ALA, "hasClass",1000),
	SUBCLASS(Namespaces.ALA, "hasSubClass",1000),
	ORDER(Namespaces.ALA, "hasOrder",1000),
	SUBORDER(Namespaces.ALA, "hasSubOrder",1000),
	PARVORDER(Namespaces.ALA, "hasParvorder",1000),
	INFRAORDER(Namespaces.ALA, "hasInfraorder",1000),
	SUPERFAMILY(Namespaces.ALA, "hasSuperFamily",1000),
	FAMILY(Namespaces.ALA, "hasFamily",1000),
	SUBFAMILY(Namespaces.ALA, "hasSubFamily",1000),
	TRIBE(Namespaces.ALA, "hasTribe",1000),
	SUBTRIBE(Namespaces.ALA, "hasSubtribe",1000),
	SUBGENUS(Namespaces.ALA, "hasSubgenus",1000),
	GENUS(Namespaces.ALA, "hasGenus",1000),
	SPECIES(Namespaces.ALA, "hasSpecies",1000),
	SUBSPECIES(Namespaces.ALA, "hasSubSpecies",1000),
	SPECIFIC_EPITHET(Namespaces.ALA, "hasSpecificEpithet",1000),
	INFRA_SPECIFIC_EPITHET(Namespaces.ALA, "hasInfraSpecificEpithet",1000),
	AUTHOR(Namespaces.ALA, "hasAuthor",1000),
	SYNONYM(Namespaces.ALA, "hasSynonym",1000),
	PUBLISHED_IN(Namespaces.TWDG_COMMON, "publishedInCitation",1000),

	// miscellaneous
	CAAB_CODE(Namespaces.ALA, "hasCAABCode",1000),
	CITATION(Namespaces.ALA, "hasCitationText",1000),
	SIMILAR_SPECIES(Namespaces.ALA, "hasSimilarSpecies",1000),
	
	// multimedia
	DIST_MAP_IMG_URL(Namespaces.ALA, "hasDistributionMapImageUrl",1000),
	IMAGE_URL(Namespaces.ALA, "hasImageUrl",1000),
	IMAGE_PAGE_URL(Namespaces.ALA, "hasImagePageUrl",1000),
	VIDEO_PAGE_URL(Namespaces.ALA, "hasVideoPageUrl",1000),
	IMAGE_LICENSE_INFO(Namespaces.ALA, "hasImageLicenseInfo",1000),
	PREFERRED_IMAGE(Namespaces.ALA, "isPreferredImage",1000),
	
	//bie image link back to biocache occurrence
	OCCURRENCE_UID(Namespaces.ALA, "hasOccurrenceUid",1000),
	OCCURRENCE_ROW_KEY(Namespaces.ALA, "hasOccurrenceRowKey",1000),
	
	// temp
	TMP(Namespaces.ALA, "tmp",1000);
	
    private String namespace;
    private String localPart;
    private int category;  // for sorting in UI
    
    private static List<String> taxonomicPredicates = null;

    /**
     * Allow reverse-lookup
     * (based on <a href="http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks">Enum Tricks</a>)
     */
    private static final Map<String,Predicates> predicateLookup = new HashMap<String,Predicates>();

    static {
         for (Predicates p : EnumSet.allOf(Predicates.class)) {
             predicateLookup.put(p.toString(), p);
         }
    }

    /**
     * Constructor for setting the 'value'
     * @param field value as String
     */
    private Predicates(String namespace, String localPart, int category) {
    	this.namespace = namespace;
    	this.localPart = localPart;
        this.category = category;
    }
    
	@Override
	public String toString() {
		return namespace+localPart;
	}
    
    public String getPredicate() {
        return this.toString();
    }

    /**
     * Lookup method for a predicate String
     *
     * @param predicate
     * @return
     */
    public static Predicates getForPredicate(String predicate) {
        return predicateLookup.get(predicate);
    }

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	
	/**
	 * @return the localPart
	 */
	public String getLocalPart() {
		return localPart;
	}

    /**
     * @return the category
     */
    public int getCategory() {
        return category;
    }

	/**
	 * @return the taxonomicPredicates
	 */
	public static List<String> getTaxonomicPredicates() {
		if(taxonomicPredicates!=null){
			return taxonomicPredicates;
		}
		taxonomicPredicates = new ArrayList<String>();
		taxonomicPredicates.add(KINGDOM.toString());
		taxonomicPredicates.add(PHYLUM.toString());
		taxonomicPredicates.add(SUB_PHYLUM.toString());
		taxonomicPredicates.add(DIVISION.toString());
		taxonomicPredicates.add(CLASS.toString());
		taxonomicPredicates.add(SUBCLASS.toString());
		taxonomicPredicates.add(ORDER.toString());
		taxonomicPredicates.add(SUBORDER.toString());
		taxonomicPredicates.add(PARVORDER.toString());
		taxonomicPredicates.add(INFRAORDER.toString());
		taxonomicPredicates.add(SUPERFAMILY.toString());
		taxonomicPredicates.add(FAMILY.toString());
		taxonomicPredicates.add(SUBFAMILY.toString());
		taxonomicPredicates.add(TRIBE.toString());
		taxonomicPredicates.add(TRIBE.toString());
		taxonomicPredicates.add(SUBTRIBE.toString());
		taxonomicPredicates.add(GENUS.toString());
		taxonomicPredicates.add(SUBGENUS.toString());
		taxonomicPredicates.add(SPECIES.toString());
		taxonomicPredicates.add(SUBSPECIES.toString());
		taxonomicPredicates.add(SPECIFIC_EPITHET.toString());
		taxonomicPredicates.add(SCIENTIFIC_NAME.toString());
		return taxonomicPredicates;
	}
}
