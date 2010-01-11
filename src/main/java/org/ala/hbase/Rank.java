/* **************************************************************************
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

package org.ala.hbase;

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
    SER         (6800, "ser", "series"),
    VAR         (8010, "var", "variety"),
    SUBSECT     (6700, "subsect", "subsection"),
    SUBORD      (4200, "subord", "suborder"),
    ORD         (4000, "ord", "order"),
    PHYL_DIV    (2000, "phyl_div", "phylum/division"),
    SUBSER      (6900, "subser", "subseries"),
    SUBTRIB     (5700, "subtrib", "subtribe"),
    SUBSP_AGGR  (6975, "subsp_aggr", "subspecies aggregate"),
    SUPERTRIB   (5700, "supertrib", "supertribe"),
    SUBVAR      (8015, "subvar", "subvariety"),
    SECT        (6600, "sect", "section"),
    SSP         (8000, "ssp", "subspecies"), // ?
    TAXSUPRAGEN (5999, "taxsupragen", "suprageneric tax. of undefined rank"),
    SUPERFAM    (4500, "superfam", "superfamily"),
    NUL         (9999, "null", "(rank not specified)"),  // NULL is reserved word
    GEN         (6000, "gen", "genus"),
    SP          (7000, "sp", "species"),
    INFRACL     (3350, "infracl", "infraclass"),
    SUBGEN      (6500, "subgen", "subgenus"),
    SUPERCL     (2800, "supercl", "superclass"),
    TRIB        (5600, "trib", "tribe"),
    CV          (8050, "cv", "cultivar"),
    FAM         (5000, "fam", "family"),
    SUBFAM      (5500, "subfam", "subfamily"),
    SUBCL       (3200, "subcl", "subclass"),
    SUPERORD    (4200, "superord", "superorder"),
    INFRAORD    (4350, "infraord", "infraorder"),
    REG         (1000, "reg", "kingdom"),
    CL          (3000, "cl", "class"),
    SUBPHYL_DIV (2200, "subphyl_div", "subphylum/subdivision");

    private Integer id;   // used to sort by classification order
    private String field; // string used in database
    private String name;  // human-readable version

    // Allow reverse-lookup (based on http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks)
    private static final Map<String,Rank> fieldLookup
          = new HashMap<String,Rank>();
    private static final Map<Integer,Rank> idLookup
          = new HashMap<Integer,Rank>();
    private static final Map<String,Rank> nameLookup
    = new HashMap<String,Rank>();    

    static {
         for (Rank rank : EnumSet.allOf(Rank.class)) {
             fieldLookup.put(rank.getField(), rank);
             idLookup.put(rank.getId(), rank);
             nameLookup.put(rank.getName(), rank);
         }
    }

    /**
     * Constructor for setting the 'value'
     * @param field value as String
     */
    private Rank(Integer id, String field, String name) {
        this.id = id;
        this.field = field;
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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