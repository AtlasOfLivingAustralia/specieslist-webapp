/*
 * Copyright (C) 2012 Atlas of Living Australia
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

    String rawScientificName
    String dataResourceUid
    String guid
    String matchedName
    String author
    String imageUrl
    String kingdom
    String family
    Boolean isPublished //stores whether or not the species list for this item has been published to the BIE
    Date dateCreated
    Date lastUpdated
    Integer itemOrder
    String commonName

    static hasMany = [kvpValues: SpeciesListKVP]
    //allows the items to be sorted before they are extracted.
    SortedSet kvpValues
    //NC 2013-05-09: Changed the name for the list relationship because this is a reserved word in certain situations. This causes
    //issues when being used in a "criteria"
    static belongsTo = [mylist:SpeciesList]

    static constraints = {
        //guid unique:  'name' //AK for the table
        isPublished(nullable:true)
        guid(nullable:true)
        matchedName(nullable: true)
        kingdom(nullable:  true)
        family(nullable:  true)
        commonName(nullable:  true)
        imageUrl(nullable: true)
        author(nullable: true)
    }

    static mapping ={
        dataResourceUid index: 'idx_data_resource_uid'
        guid index: 'idx_guid'
        itemOrder index:  'idx_item_order'
        family index:  'idx_item_fam'
        //NC 2013-05-09: Needed to map the old FK column name to the new one
        mylist (column:'list_id', sort:'itemOrder')
        //kvpValues cascade: "all-delete-orphan"
        //kvpValues lazy: false
    }
    def beforeInsert() {

        // Conversion because the database column is "Latin1" and has some problems with special characters

        this.author = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.author)
        this.matchedName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.matchedName)
        this.commonName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.commonName)
        this.rawScientificName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.rawScientificName)

        //log.debug("--------------->beforeInsert")
    }

    def beforeUpdate() {
        this.author = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.author)
        this.matchedName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.matchedName)
        this.commonName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.commonName)
        this.rawScientificName = org.apache.commons.lang.StringEscapeUtils.escapeHtml(this.rawScientificName)
        //log.debug("--------------->beforeUpdate")
    }

    def afterLoad() {
        this.author = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(this.author)
        this.matchedName = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(this.matchedName)
        this.commonName = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(this.commonName)
        this.rawScientificName = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(this.rawScientificName)
        //log.debug("--------------->afterLoad")
    }


}
