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

class SpeciesListItem {

    String rawScientificName //AKA supplied name
    String dataResourceUid
    String guid
    String commonName   //matched common name
    String matchedName //Matched scientific name
    String author //matched author
    String imageUrl
    String kingdom // matched kingdom (not used)
    String family  // matched family
    Boolean isPublished //stores whether or not the species list for this item has been published to the BIE
    Date dateCreated
    Date lastUpdated
    Integer itemOrder

    MatchedSpecies matchedSpecies
    //Impose hasOne relation for fixing session writing errors in rematch process
    //But it affect the reading speed significantly

    //static hasOne = [matchedSpecies: MatchedSpecies]
    static hasMany = [kvpValues: SpeciesListKVP]
    //allows the items to be sorted before they are extracted.
    SortedSet kvpValues
    //NC 2013-05-09: Changed the name for the list relationship because this is a reserved word in certain situations. This causes
    //issues when being used in a "criteria"
    static belongsTo = [mylist:SpeciesList]
    static embedded = ['homeAddress', 'workAddress']

    static constraints = {
        //guid unique:  'name' //AK for the table
        isPublished(nullable:true)
        guid(nullable:true)
        matchedName(nullable: true)
        commonName(nullable:  true)
        imageUrl(nullable: true)
        author(nullable: true)
        matchedSpecies(nullable: true)
        kingdom(nullable:  true)
        family(nullable:  true)
    }

    static mapping ={
        dataResourceUid index: 'idx_data_resource_uid'
        guid index: 'idx_guid'
        itemOrder index:  'idx_item_order'
        family index:  'idx_item_fam'
        matchedName index:  'idx_matched_name'
        rawScientificName index:  'idx_raw_scientific_name'
        commonName index: 'idx_common_name'
        //NC 2013-05-09: Needed to map the old FK column name to the new one
        mylist (column:'list_id', sort:'itemOrder')
        mylist index: 'idx_list_id'
        //kvpValues cascade: "all-delete-orphan"
        //kvpValues lazy: false
        matchedSpecies (column: 'matched_species_id', ignoreNotFound: true, nullable: true, lazy:true)
    }

    def toMap() {
        def map = this.class.declaredFields.findAll { it.modifiers == java.lang.reflect.Modifier.PRIVATE}.
                collectEntries { [it.name, this[it.name]] }

        map["matchedSpecies"] = this.matchedSpecies?.toMap()
        return map
    }
}
