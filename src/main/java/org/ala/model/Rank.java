/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum to store values of taxon rank extracted from namesIndex.txt dump
 *
 * Rank value definitions taken from
 * @link{ http://www.diversitycampus.net/Projects/TDWG-SDD/Minutes/2004NZ_schema/UBIF-Docu-Enumerations.html#TaxonomicRankEnum }
 * Ids taken from GBIF TaxonRank.java enum class
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public enum Rank {
    SER         (6800, "series", "ser"),
    VAR         (8010, "variety","var"),
    SUBSECT     (6700, "subsection", "subsect"),
    SUBORD      (4200, "suborder","subord"),
    ORD         (4000, "order", "ord", "ordo"),
    PHYL_DIV    (2000, "phylum/division", "phyl_div", "phylum", "division"), //Division in botany
    SUBPHYL_DIV (2200, "subphylum/subdivision", "subphyl_div", "subphylum"),
    SUBSER      (6900, "subseries","subser"),
    SUBTRIB     (5700, "subtribe", "subtrib"),
    SUBSP_AGGR  (6975, "subspecies aggregate", "subsp_aggr"),
    SUPERTRIB   (5700, "supertribe", "supertrib"),
    SUBVAR      (8015, "subvariety", "subvar"),
    SECT        (6600, "section", "sect"),
    SSP         (8000, "subspecies", "ssp", "subsp"), // ?
    FORM        (8020, "form", "forma", "f", "f.","fm.", "fm"),
    TAXSUPRAGEN (5999, "suprageneric tax. of undefined rank", "taxsupragen"),
    SUPERFAM    (4500, "superfamily", "superfam"),
    NUL         (9999, "(rank not specified)","null"),  // NULL is reserved word
    GEN         (6000, "genus", "gen"),
    SP          (7000, "species", "sp"),
    INFRACL     (3350, "infraclass", "infracl"),
    SUBGEN      (6500, "subgenus","subgen"),
    SUPERCL     (2800, "superclass", "supercl"),
    TRIB        (5600, "tribe","trib"),
    CV          (8050, "cultivar", "cv"),
    FAM         (5000, "family","fam"),
    SUBFAM      (5500, "subfamily", "subfam"),
    SUBCL       (3200, "subclass", "subcl"),
    SUPERORD    (4200, "superorder", "superord"),
    INFRAORD    (4350, "infraorder", "infraord"),
    REG         (1000, "kingdom", "reg"),
    CL          (3000, "class", "cl"),
    // non standard rank values encountered while indexing...
    DIVISION    (2000, "phylum/division", "division"),
    SUBDIVISION (2200, "subphylum/subdivision", "subdivision"),
    COHORT      (2400, "cohort", "cohort"),
    SECTION     (6600, "section", "section"),
    INCERTAE_SEDIS (5999, "Incertae Sedis (uncertain rank)", "incertae sedis"),
    SP_INQUIRENDA  (5999, "Species Inquirenda (doubtful identity)", "species inquirenda");

    private Integer id;   // used to sort by classification order
    private String name;  // human-readable version
    private String[] fields; // string used in database

    // Allow reverse-lookup (based on http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks)
    private static final Map<String,Rank> fieldLookup
          = new HashMap<String,Rank>();
    private static final Map<Integer,Rank> idLookup
          = new HashMap<Integer,Rank>();
    private static final Map<String,Rank> nameLookup
          = new HashMap<String,Rank>();

    static {
         for (Rank rank : EnumSet.allOf(Rank.class)) {
        	 for(String field: rank.getFields()){
        		 fieldLookup.put(field, rank);
        	 }
             idLookup.put(rank.getId(), rank);
             nameLookup.put(rank.getName(), rank);
         }
    }

    /**
     * Constructor for setting the 'value'
     * @param field value as String
     */
    private Rank(Integer id, String name, String ... fields) {
        this.id = id;
        this.fields = fields;
        this.name = name;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param field
     * @return RankFacet the RankFacet
     */
    public static Rank getForField(String field) {
        return fieldLookup.get(field);
    }

    public static Rank getForName(String name) {
        return nameLookup.get(name);
    }
    /**
     * @param id
     * @return RankFacet the RankFacet
     */
    public static Rank getForId(Integer id) {
        return idLookup.get(id);
     }
}

/* Values mined from namesIndex.txt
    ser
    var
    subsect
    subord
    ord
    phyl_div
    subser
    subtrib
    subsp_aggr
    supertrib
    subvar
    sect
    ssp
    taxsupragen
    superfam
    null
    gen
    sp
    infracl
    subgen
    supercl
    trib
    cv
    fam
    subfam
    subcl
    superord
    infraord
    reg
    cl
    subphyl_div
 */