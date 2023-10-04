/*
 * Copyright (C) 2022 Atlas of Living Australia
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
 */

package au.org.ala.specieslist

//import au.org.ala.names.ws.api.SearchStyle

class MatchedSpecies {
    String id
    String taxonConceptID
    String scientificName
    String scientificNameAuthorship
    String vernacularName
    String kingdom
    String phylum
    String taxonClass
    String taxonOrder
    String taxonRank
    String family
    String genus

    static constraints = {
        vernacularName(nullable:  true)
        taxonConceptID(nullable:  true)
        scientificName(nullable:  true)
        scientificNameAuthorship(nullable:  true)
        kingdom(nullable:  true)
        family(nullable:  true)
        taxonOrder(nullable:  true)
        taxonClass(nullable:  true)
        taxonRank(nullable: true)
        phylum(nullable: true)
        genus(nullable: true)
    }


    static mapping = {
        taxonConceptID (column: 'taxon_concept_id')
        scientificName (column: 'scientific_name')
        scientificNameAuthorship (column: 'scientific_name_authorship')
        vernacularName column: 'vernacular_name'
        taxonClass column: 'taxon_Class'
        taxonOrder column: 'taxon_order'
        taxonRank column: 'taxon_rank'
    }

    def toMap() {
        this.class.declaredFields.findAll { it.modifiers == java.lang.reflect.Modifier.PRIVATE }.
                collectEntries { [it.name, this[it.name]] }
    }

}
