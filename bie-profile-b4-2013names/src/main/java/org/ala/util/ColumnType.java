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
package org.ala.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.ala.model.BaseRanking;
import org.ala.model.Category;
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
import org.ala.model.Ranking;
import org.ala.model.Reference;
import org.ala.model.SensitiveStatus;
import org.ala.model.SimpleProperty;
import org.ala.model.SpecimenHolding;
import org.ala.model.SynonymConcept;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Column name and matching data type.
 * 
 * @author mok011
 */
public enum ColumnType {

    TAXONCONCEPT_COL("taxonConcept", TaxonConcept.class, false),
    TAXONNAME_COL("hasTaxonName", TaxonName.class, true),
    IDENTIFIER_COL("sameAsIdentifiers", String.class, true), 
    SAME_AS_COL("sameAs", TaxonConcept.class, true), // Now stores the taxonConcepts instead of identifiers to store publication information for the id's
    SYNONYM_COL("hasSynonym", SynonymConcept.class, true),
    IS_CONGRUENT_TO_COL("IsCongruentTo", TaxonConcept.class, true),
    VERNACULAR_COL("hasVernacularConcept", CommonName.class, true),
    CONSERVATION_STATUS_COL("hasConservationStatus", ConservationStatus.class, true),
    SENSITIVE_STATUS_COL("hasSensitiveStatus", SensitiveStatus.class, true),
    PEST_STATUS_COL("hasPestStatus", PestStatus.class, true),
    CATEGORY_COL("hasCategory", Category.class, true),
    REGION_COL("hasRegion", OccurrencesInGeoregion.class, true),
    EXTANT_STATUS_COL("hasExtantStatus", ExtantStatus.class, true),
    HABITAT_COL("hasHabitat", Habitat.class, true),
    IMAGE_COL("hasImage", Image.class, true),
    DIST_IMAGE_COL("hasDistributionImage", Image.class, true),
    SCREENSHOT_IMAGE_COL("hasScreenshotImage", Image.class, true),
    IS_CHILD_COL_OF("IsChildTaxonOf", TaxonConcept.class, true),
    IS_PARENT_COL_OF("IsParentTaxonOf", TaxonConcept.class, true),
    TEXT_PROPERTY_COL("hasTextProperty", SimpleProperty.class, true),
    CLASSIFICATION_COL("hasClassification", Classification.class, true),
    REFERENCE_COL("hasReference", Reference.class, true),
    EARLIEST_REFERENCE_COL("hasEarliestReference", Reference.class, false),
    PUBLICATION_REFERENCE_COL("hasPublicationReference", Reference.class, true),
    PUBLICATION_COL("hasPublication", Publication.class, true),
    IDENTIFICATION_KEY_COL("hasIdentificationKey", IdentificationKey.class, true),
    SPECIMEN_HOLDING_COL("hasSpecimenHolding", SpecimenHolding.class, true),
    IS_ICONIC("IsIconic", Boolean.class, false),
    IS_AUSTRALIAN("IsAustralian", Boolean.class, false),
    LINK_IDENTIFIER("linkIdentifier", String.class, false),
//    INFOSOURCE_UID("infosourceUid", String.class, false),
    OCCURRENCE_RECORDS_COUNT_COL("hasOccurrenceRecords", Integer.class, false),
    GEOREF_RECORDS_COUNT_COL("hasGeoReferencedRecords", Integer.class, false),
    // for rk Family Column
    DEFAULT_COMMON_NAME("defaultNameValue", BaseRanking.class, false),
    RANKING("TIMESTAMP", Ranking.class, true);
    public static final String COLUMN_FAMILY_NAME = "tc";
    public static final String SUPER_COLUMN_NAME = "tc";
    public static String[] columnsToIndex= null;

    private static final Map<String, ColumnType> columnTypeLookup = new HashMap<String, ColumnType>();

    static {
        for (ColumnType mt : EnumSet.allOf(ColumnType.class)) {
            columnTypeLookup.put(mt.getColumnName(), mt);
        }
        columnsToIndex = columnTypeLookup.keySet().toArray(new String[]{});
    }

    private String columnName;
    private Class clazz;
    private boolean isList;

	private ColumnType(String columnName, Class clazz, boolean isList){
        this.columnName = columnName;
        this.clazz = clazz;
        this.isList = isList;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean isList() {
        return isList;
    }

    public static ColumnType getColumnType(String columnName) {
        ColumnType subCol = columnTypeLookup.get(columnName);
        if (subCol == null) {
            //if the name is a long value it represents a ranking sub column
            try {
                Long.parseLong(columnName);
                //must be a ranking
                subCol = RANKING;
            } catch (NumberFormatException nfe) {
            }
        }
        return subCol;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
