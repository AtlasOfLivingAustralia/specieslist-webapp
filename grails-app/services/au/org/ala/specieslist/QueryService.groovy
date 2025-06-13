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

import groovy.time.*
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Order
import org.hibernate.FetchMode

class QueryService {

    public static final String EDITOR_SQL_RESTRICTION = "this_.id in (select species_list_id from species_list_editors e where e.editors_string = ?)"
    public static final String USER_ID = "userId"
    public static final String IS_PRIVATE = "isPrivate"
    public static final String LIST_NAME = "listName"
    public static final String FIRST_NAME = "firstName"
    public static final String SURNAME = "surname"
    public static final String DESCRIPTION = "description"
    public static final String GUID = "guid"
    public static final String COMMON_NAME = "commonName"
    public static final String MATCHED_NAME = "matchedName"
    public static final String RAW_SCIENTIFIC_NAME = "rawScientificName"
    public static final String DATA_RESOURCE_UID = "dataResourceUid"
    public static final String ASC = "asc"
    public static final String LAST_UPDATED = "lastUpdated"
    public static final String LIST_TYPE = "listType"
    public static final String WKT = "wkt"
    public static final String WKT_QUERY = "wkt=isNotNull"
    public static final String MATCHED_FAMILY = "family(matched)"

    def authService
    def localAuthService
    def grailsApplication

    /** A regular expression to split the  */
    public final def filterRegEx = /([a-zA-Z]+):([\x00-\x7F\s]*)/

    /** A map of criteria method names to Methods - allow the use of reflection to gain the criteria method to use */
    def criteriaMethods = grails.orm.HibernateCriteriaBuilder.class.getMethods().findAll {
        it.getParameterTypes().length < 3
    }.collectEntries{ [it.name, it] }

    // This method is only used for testing
    def getFilterListResult(params){
        getFilterListResult(params, false, null, null, null)
    }

    /** A map of domain property names to data types */
    def getSpeciesListProperties() {
        def entity = grailsApplication.mappingContext.getPersistentEntity(SpeciesList.name)
        def speciesListProperties= entity.persistentProperties.collectEntries{[it.name, it.type]}
        speciesListProperties
    }

    /**
     * retrieves the lists that obey the supplied filters
     *
     * Filters are supplied in the Criteria specific manner- eg eq:
     *
     * AC 20141218: Moved sorting rules into setSortOrder
     * AC 20141218: Added support for params.q search term so as to be consistent with PublicController.speciesLists()
     *
     * @param params
     */
    def getFilterListResult(params, boolean hidePrivateLists, List itemIds, request, response, boolean isApiCall = false) {
        //list should be based on the user that is logged in
        params.max = Math.min(params.max ? params.int('max') : 25, 1000)

        //remove sort from params
        def sort = params.user ? null : (params.sort ?: LIST_NAME)
        def order = params.order ?: ASC

        params.fetch = [items: 'lazy']

        def speciesListProperties = getSpeciesListProperties()

        def userId = authService.getUserId()
        if (!userId && request) userId = localAuthService.getJwtUserId(request, response)

        def c = SpeciesList.createCriteria()
        def lists = c.list(params){
            and {
                params.each { key, value ->
                    //the value suffix tells us which filter operation to perform
                    if ('q'.equals(key) && value) {
                        or {
                            ilike(LIST_NAME, '%' + value + '%')
                            ilike(FIRST_NAME, '%' + value + '%')
                            ilike(SURNAME, '%' + value + '%')
                            ilike(DESCRIPTION, '%' + value + '%')
                            if (itemIds) {
                                'in'('id', itemIds)
                            }
                        }
                    } else if (WKT.equals(key)) {
                        and {
                            isNotNull(WKT)
                            ne(WKT, "")
                        }
                    } else {
                        def matcher = (value =~ filterRegEx)
                        if (matcher.matches()) {
                            def filterMethod = matcher[0][1]
                            def filterValue = matcher[0][2]
                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method = criteriaMethods.get(filterMethod)
                            Object[] args = null
                            if (method) {
                                args = [getValueBasedOnType(speciesListProperties[key], filterValue)]
                                if (method.getParameterTypes().size() > 1) {
                                    args = [key] + args[0]
                                }
                            }

                            // special case for searching by userId: we need to find lists where the user is the
                            // owner OR lists where they are the editor
                            if (USER_ID.equals(key) && "eq".equals(filterMethod)) {
                                or {
                                    if (method) {
                                        method.invoke(c, args)
                                    }
                                    sqlRestriction(EDITOR_SQL_RESTRICTION, [filterValue])
                                }
                            } else {
                                if (method) {
                                    def result = method.invoke(c, args)
                                    result
                                }
                            }
                        }
                    }
                }
            }
            and {
                if (userId) {
                    if (hidePrivateLists || !localAuthService.isAdmin()) {
                        // the user is not an admin, so only show lists that are not private OR are owned by the user
                        // OR where the user is an editor
                        or {
                            isNull(IS_PRIVATE)
                            eq(IS_PRIVATE, false)
                            eq(USER_ID, userId)
                            sqlRestriction(EDITOR_SQL_RESTRICTION, [userId])
                        }
                    } else {
                        log.debug("User is admin, so has visibility of private lists")
                    }
                } else if (!isApiCall || hidePrivateLists) {
                    // if there is no user, hidePrivateLists is true and its not an API call, do no show any private records
                    or {
                        isNull(IS_PRIVATE)
                        eq(IS_PRIVATE, false)
                    }
                }
            }
            setSortOrder(sort, order, userId, c)
        }

        //remove the extra condition "fetch" condition
        params.remove('fetch')

        // these parameters are needed in the gsp page
        params.sort = sort
        params.order = order
        lists
    }

    def getSelectedFacets(params){
        def selectedFacets = []
        params.each { key, value ->
            def query = getBooleanQueryFacets().get(key)
            if (query){
                selectedFacets << [query: key, facet: query]
            } else if (key == LIST_TYPE){
                if (value) {
                    def cleanedValue = value.replaceAll("eq:", "")
                    query = listTyoeFacets.get(cleanedValue)
                    selectedFacets << [query: key, facet: query]
                }
            } else if (key == WKT){
                query = WKT_QUERY
                selectedFacets << [query: WKT, facet: [label:'spatialBounds.list.label']]
            }
        }
        selectedFacets
    }

    // get boolean query facets
    // the user is an admin, show private list filter
    def getBooleanQueryFacets() {
        if (localAuthService.isAdmin()) {
            return booleanQueryFacets + adminBooleanQueryFacets
        } else {
            return booleanQueryFacets
        }
    }

    //TODO make this more configurable
    def booleanQueryFacets = [
        "isAuthoritative": [label:'authoritative.list.label', tooltip: 'authoritative.list.tooltip'],
        "isThreatened": [label:'threatened.list.label', tooltip: 'threatened.list.tooltip'],
        "isInvasive": [label:'invasive.list.label', tooltip: 'invasive.list.tooltip'],
        "isSDS": [label:'sds.list.label', tooltip: 'sds.list.tooltip'],
        "isBIE": [label:'speciesPages.list.label', tooltip: 'speciesPages.list.tooltip']
    ]

    def adminBooleanQueryFacets = [
        "isPrivate": [label:'private.list.label', tooltip: 'private.list.tooltip']
    ]

    //TODO make this more configurable
    def listTyoeFacets = [
        (ListType.SPECIES_CHARACTERS.toString()): [listType: ListType.SPECIES_CHARACTERS,
                          label: ListType.SPECIES_CHARACTERS.i18nValue, tooltip: ListType.SPECIES_CHARACTERS.toolTip],
        (ListType.CONSERVATION_LIST.toString()): [listType: ListType.CONSERVATION_LIST,
                          label: ListType.CONSERVATION_LIST.i18nValue, tooltip: ListType.CONSERVATION_LIST.toolTip],
        (ListType.SENSITIVE_LIST.toString()): [listType: ListType.SENSITIVE_LIST,
                          label: ListType.SENSITIVE_LIST.i18nValue, tooltip: ListType.SENSITIVE_LIST.toolTip],
        (ListType.COMMON_HABITAT.toString()): [listType: ListType.COMMON_HABITAT,
                          label: ListType.COMMON_HABITAT.i18nValue, tooltip: ListType.COMMON_HABITAT.toolTip],
        (ListType.LOCAL_LIST.toString()) : [listType: ListType.LOCAL_LIST,
                          label: ListType.LOCAL_LIST.i18nValue, tooltip: ListType.LOCAL_LIST.toolTip],
        (ListType.COMMON_TRAIT.toString()): [listType: ListType.COMMON_TRAIT,
                          label: ListType.COMMON_TRAIT.i18nValue, tooltip: ListType.COMMON_TRAIT.toolTip],
        (ListType.SPATIAL_PORTAL.toString()): [listType: ListType.SPATIAL_PORTAL,
                          label: ListType.SPATIAL_PORTAL.i18nValue, tooltip: ListType.SPATIAL_PORTAL.toolTip],
        (ListType.PROFILE.toString()): [listType: ListType.PROFILE,
                          label: ListType.PROFILE.i18nValue, tooltip: ListType.PROFILE.toolTip],
        (ListType.TEST.toString()): [listType: ListType.TEST,
                          label: ListType.TEST.i18nValue, tooltip: ListType.TEST.toolTip],
        (ListType.OTHER.toString()): [listType: ListType.OTHER,
                          label: ListType.OTHER.i18nValue, tooltip: ListType.OTHER.toolTip]
    ]

    def getTypeFacetCounts(params, List itemIds){
        getTypeFacetCounts(params, false, itemIds)
    }

    def getTagFacetCounts(params, List itemIds){
        getTagFacetCounts(params, false, itemIds)
    }

    def getTypeFacetCounts(params, boolean hidePrivateLists, List itemIds) {
        def facets = []
        listTyoeFacets.each { key, facet ->
            facets <<  [query:'listType=eq:' + facet.listType.toString(), label: facet.label, tooltip: facet.tooltip, count: getFacetCount(params, LIST_TYPE, facet.listType, hidePrivateLists, itemIds)]
        }
        return facets
    }

    def getTagFacetCounts(params, boolean hidePrivateLists, List itemIds) {
        def facets = []
        getBooleanQueryFacets().each { key, facet ->
            // only add tagfacet to selectable facet  list if it is not already selected
            if(params.get(key) == null){
                facets << [query: key + '=eq:true', label: facet.label, tooltip: facet.tooltip, count:  getFacetCount(params, key, true, hidePrivateLists, itemIds)]
            }
        }
        // only add WKT fact to selectable facet list if it is not already selected
        if(params.get(WKT) == null){
            facets << [query: WKT_QUERY, label:'spatialBounds.list.label', tooltip: 'spatialBounds.list.tooltip', count:  getFacetCount(params, WKT, null, hidePrivateLists, itemIds)]
        }
        return facets
    }

    /**
     * Fina all private lists that the current user can view.
     *
     * @param includePublicLists
     * @param hidePrivateLists
     */
    def visibleLists(boolean includePublicLists, boolean hidePrivateLists, request, response) {
        def userId = authService.getUserId()
        if (!userId && request) userId = localAuthService.getJwtUserId(request, response)

        def c = SpeciesList.createCriteria()
        def lists = c.list {
            projections {
                property(DATA_RESOURCE_UID)
            }
            or {
                if (includePublicLists) {
                    // Include public lists.
                    isNull(IS_PRIVATE)
                    eq(IS_PRIVATE, false)
                }

                if (hidePrivateLists && !localAuthService.isAdmin()) {

                    if (userId) {
                        // Include only permitted private lists when logged in
                        and {
                            // Find private lists owned by the user or where the user is an editor.
                            eq(IS_PRIVATE, true)
                            or {
                                eq(USER_ID, userId)
                                sqlRestriction(EDITOR_SQL_RESTRICTION, [userId])
                            }
                        }
                    }
                } else {
                    // Include private lists.
                    eq(IS_PRIVATE, true)
                }
            }
        }

        lists
    }

    def getFacetCount(params, facetField, facetValue, boolean hidePrivateLists, List itemIds) {
        def speciesListProperties = getSpeciesListProperties()
        def c = SpeciesList.createCriteria()
        def facetCount = c.get  {
            projections {
                count()
            }
            and {
                params.each { key, value ->
                    //the value suffix tells us which filter operation to perform
                    if ('q'.equals(key) && value) {
                        or {
                            ilike(LIST_NAME, '%' + value + '%')
                            ilike(FIRST_NAME, '%' + value + '%')
                            ilike(SURNAME, '%' + value + '%')
                            ilike(DESCRIPTION, '%' + value + '%')
                            if (itemIds) {
                                'in' ('id', itemIds)
                            }
                        }
                    } else {
                        def matcher = (value =~ filterRegEx)
                        if (matcher.matches()) {
                            def filterMethod = matcher[0][1]
                            def filterValue = matcher[0][2]
                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method = criteriaMethods.get(filterMethod)
                            Object[] args = null
                            if (method) {
                                args = [getValueBasedOnType(speciesListProperties[key], filterValue)]
                                if (method.getParameterTypes().size() > 1) {
                                    args = [key] + args[0]
                                }
                            }

                            // special case for searching by userId: we need to find lists where the user is the
                            // owner OR lists where they are the editor
                            if (USER_ID.equals(key) && "eq".equals(filterMethod)) {
                                or {
                                    if (method) {
                                        method.invoke(c, args)
                                    }
                                    sqlRestriction(EDITOR_SQL_RESTRICTION, [filterValue])
                                }
                            } else {
                                if (method) {
                                    def result = method.invoke(c, args)
                                    result
                                }
                            }
                        }
                    }
                }
            }
            and {
                if (authService.getUserId()) {
                    if (hidePrivateLists || !localAuthService.isAdmin()) {
                        // the user is not an admin, so only show lists that are not private OR are owned by the user
                        // OR where the user is an editor
                        or {
                            isNull(IS_PRIVATE)
                            eq(IS_PRIVATE, false)
                            eq(USER_ID, authService.getUserId())
                            sqlRestriction(EDITOR_SQL_RESTRICTION, [authService.getUserId()])
                        }
                    }
                } else {
                    // if there is no user, do no show any private records
                    or {
                        isNull(IS_PRIVATE)
                        eq(IS_PRIVATE, false)
                    }
                }
            }
            and {
                if (facetField == WKT){
                    and {
                        isNotNull(WKT)
                        ne(WKT, "")
                    }
                } else {
                    eq(facetField, facetValue)
                }
            }
        }

        facetCount
    }

    /**
     * A merging of sort ordering rules. This function adds Order terms to a Criteria.
     *
     * 1. special case: 'user' present, 'sort' absent.
     *      first order: 'username'='user' records first
     *      second order: 'lastUpdated' descending
     *      third order: 'listName' ascending
     * 2. no parameters case: 'both 'sort' and 'user' absent.
     *      first order: 'listName' ascending
     * 3. general case: 'sort' present.
     *      first order: 'sort' 'order' ('order' defaults to ascending if absent)
     *
     *
     * @param sort SpeciesList field or null
     * @param order "asc" or "desc" or null
     * @param user SpeciesList.username or null
     * @param c Criteria
     * @return
     */
    private def setSortOrder(sort, order, user, c) {
        //Bring user's lists to the front if no sort order is defined
        //countBy does 'user' param validation
        if (sort == null && user != null && SpeciesList.countByUsername(user) > 0) {
            def userSortSql = "case username when '" + user + "' then 0 else 1 end"
            def orderUser = new Order(userSortSql, true) {
                @Override
                public String toString() {
                    return userSortSql
                }

                @Override
                public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
                    return userSortSql
                }
            }

            c.order(orderUser)
            c.order(Order.desc(LAST_UPDATED))
        }

        //append default sorting
        sort = sort ?: LIST_NAME
        order = order ?: ASC
        c.order(new Order(sort, ASC.equalsIgnoreCase(order)))
    }
    /*
    * Retrieves the species list items by given guid.
    *
    * @param queryParams : only supports: max, offset, sort
    * @param guid
    * @param isBIE
    * @param lists: data resource ids
    * @return
    */
    def getListForSpecies(guid, isBIE, lists, queryParams, filters ) {
        def speciesListProperties = getSpeciesListProperties()
        def c = SpeciesListItem.createCriteria()

        def results = c.list(queryParams) {
            eq(GUID, guid)
            if (isBIE) {
                mylist {
                    eq("isBIE", isBIE.toBoolean())
                }
            }
            if (lists) {
                'in'(DATA_RESOURCE_UID, lists)
            }

            filters.each { key, value ->
                if (speciesListProperties.containsKey(key)) {
                    mylist {
                        //the value suffix tells us which filter operation to perform
                        def matcher = (value =~ filterRegEx)
                        if (matcher.matches()) {
                            def fvalue = matcher[0][2] //
                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method = criteriaMethods.get(matcher[0][1])
                            if (method) {
                                Object[] args = [getValueBasedOnType(speciesListProperties[key], fvalue)]
                                if (method.getParameterTypes().size() > 1)
                                    args = [key] + args[0]
                                //log.debug("ARGS : " +args + " method : " + method)
                                method.invoke(c, args)
                            }
                        }
                    }
                } else {
                    //the value suffix tells us which filter operation to perform
                    def matcher = (value =~ filterRegEx)
                    if (matcher.matches()) {
                        def fvalue = matcher[0][2]

                        //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                        def method = criteriaMethods.get(matcher[0][1])
                        if (method) {
                            Object[] args = [getValueBasedOnType(speciesListProperties[key], fvalue)]
                            if (method.getParameterTypes().size() > 1) {
                                args = [key] + args
                            }
                            method.invoke(c, args)
                        }
                    }
                }
            }
        }


        return results
    }

    /**
     * @Todo
     * Fix bug when param: max is set
     * @See issue #271
     *
     * retrieves the species list items that obey the supplied filters.
     *
     * When a distinct field is provided the values of the field are returned rather than a SpeciesListItem
     * @param props
     * @param params
     * @param guid
     * @param lists
     * @param distinctField
     * @return
     */
    def getFilterListItemResult(props, params, guid, lists, distinctField){
        def speciesListProperties = getSpeciesListProperties()
        def c = SpeciesListItem.createCriteria()

        //params – pagination parameters (max, offset, etc...) closure – The closure to execute
        //sort, max, offset
        // max parameter does not work, @see #271
        c.list(props += params) {
            //set the results transformer so that we don't get duplicate records because of
            // the 1:many relationship between a list item and KVP
            setResultTransformer(org.hibernate.criterion.CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            and {
                if (distinctField) {
                    distinct(distinctField)
                    isNotNull(distinctField)
                }
                if (guid) eq(GUID, guid)
                if (lists) {
                    'in'(DATA_RESOURCE_UID, lists)
                }

                params.each { key, value ->
                    log.debug("KEYS: " +key+" " + speciesListProperties.containsKey(key))
                    if (speciesListProperties.containsKey(key)) {
                        mylist {
                            //the value suffix tells us which filter operation to perform
                            def matcher = (value =~ filterRegEx)
                            if (matcher.matches()) {
                                def fvalue = matcher[0][2] //
                                //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                                def method = criteriaMethods.get(matcher[0][1])
                                if (method) {
                                    Object[] args = [getValueBasedOnType(speciesListProperties[key], fvalue)]
                                    if (method.getParameterTypes().size() > 1)
                                        args = [key] + args[0]
                                    //log.debug("ARGS : " +args + " method : " + method)
                                    method.invoke(c, args)
                                }
                            }
                        }
                    } else {
                        //the value suffix tells us which filter operation to perform
                        def matcher = (value =~ filterRegEx)
                        if (matcher.matches()) {
                            def fvalue = matcher[0][2]

                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method = criteriaMethods.get(matcher[0][1])
                            if (method) {
                                Object[] args = [getValueBasedOnType(speciesListProperties[key], fvalue)]
                                if (method.getParameterTypes().size() > 1) {
                                    args = [key] + args
                                }
                                method.invoke(c, args)
                            }
                        }
                    }
                }
            }
        }
    }

    def getValueBasedOnType(type, value){
        switch (type) {
            case Boolean.class: Boolean.parseBoolean(value); break;
            case ListType.class: ListType.valueOf(value); break;
            default: value; break;
        }
    }

    def validMatchedSpeciesField(String field) {
        return new String[] {"taxonConceptID", "scientificName", "scientificNameAuthorship", "vernacularName", "kingdom",
                "phylum", "taxonClass", "taxonOrder", "taxonRank", "family", "genus"}.contains(field) ? field : "scientificName";
    }

    /**
     * Constructs a query based on the filters that have been applied in the KVPs etc.
     * @param base
     * @param facets
     * @param dataResourceUid
     * @param q
     * @return
     */
    def constructWithFacets(String base, List facets, String dataResourceUid, String q = null) {
        StringBuilder query = new StringBuilder(base)
        StringBuilder whereBuilder = new StringBuilder(" where sli.dataResourceUid= :dataResourceUid ")
        //query.append(" from SpeciesListItem sli join sli.kvpValues kvp where sli.dataResourceUid=? ")
        def queryparams = [dataResourceUid: dataResourceUid]
        if (q) {
            whereBuilder.append("AND (sli.matchedName like :matchedName or sli.commonName like :commonName or sli.rawScientificName like :rawScientificName) ")
            queryparams << [matchedName: "%" + q + "%"]
            queryparams << [commonName: "%" + q + "%"]
            queryparams << [rawScientificName: "%" + q + "%"]
        }
        if (facets) {
            facets.eachWithIndex { facet, index ->
                int pos = facet.indexOf(":")
                if (pos != -1) {
                    if (facet.startsWith("kvp")){
                        String sindex = index.toString()
                        facet = facet.replaceFirst("kvp ","")
                        pos = facet.indexOf(":")
                        String key = facet.substring(0, pos)
                        String value = facet.substring(pos + 1)
                        query.append(" join sli.kvpValues kvp").append(sindex)
                        whereBuilder.append(" AND kvp").append(sindex).append(".key=:key AND kvp").append(sindex).append(".value=:value")
                        queryparams << [key: key, value: value]
                    } else if (facet.startsWith("matched ")) {
                        String sindex = index.toString()
                        facet = facet.replaceFirst("matched ", "")
                        pos = facet.indexOf(":")
                        String key = facet.substring(0, pos)
                        String value = facet.substring(pos + 1)
                        query.append(" join sli.matchedSpecies matched").append(sindex)
                        whereBuilder.append(" AND matched").append(sindex).append(".").append(validMatchedSpeciesField(key)).append("=:value")
                        queryparams << [value: value]
                    } else {
                        String key = facet.substring(0, pos)
                        String value = facet.substring(pos + 1)
                        whereBuilder.append(" AND sli.").append(key)
                        if (value.equalsIgnoreCase("null")) {
                            whereBuilder.append(" is null")
                        } else {
                            whereBuilder.append("=:value ")
                            queryparams << [value: value]
                        }
                    }
                }
            }
        }
        query.append(whereBuilder.toString())
        log.debug(query.toString())
        [query.toString(), queryparams]
    }

    /**
     * Find all lists that contain any of the specified scientific names (matching against either the matchedName OR the
     * rawScientificName). A list of data resource ids can be provided, in which case the method will return a subset of that list.
     *
     * @param scientificNames Mandatory list of at least 1 scientific name to search for
     * @param drIds Optional list of data resource ids to filter
     * @return List of data resource ids for lists which contain at least 1 of the specified scientific names
     */
    List<String> filterLists(List scientificNames, List drIds = null) {
        if (!scientificNames) {
            throw new IllegalArgumentException("At least 1 scientific name is required")
        }

        SpeciesListItem.withCriteria {
            if (drIds) {
                'in' DATA_RESOURCE_UID, drIds
            }

            or {
                'in' MATCHED_NAME, scientificNames
                'in' RAW_SCIENTIFIC_NAME, scientificNames
            }

            projections {
                distinct DATA_RESOURCE_UID
            }
        }
    }

    List<Integer> getFilterSpeciesListItemsIds(params) {
        def itemIds = null

        def qValue = params.q
        if (qValue) {
            String value = '%' + qValue + '%'
            def criteria = SpeciesListItem.createCriteria()
            itemIds = criteria.list {
                projections {
                    property "mylist.id"
                }
                or {
                    ilike(MATCHED_NAME, value)
                    ilike(RAW_SCIENTIFIC_NAME, value)
                    like(GUID, value)
                    ilike(COMMON_NAME, value)
                }
            }

            itemIds = itemIds.unique()
        }
        itemIds
    }

    /**
     * Retrieves SpeciesList by given data resource uid
     * @param id data resource uid
     * @return SpeciesList
     */
    SpeciesList getSpeciesListByDataResourceUid(String id) {
        return SpeciesList.findByDataResourceUid(id)
    }

    /**
     * Retrieves SpeciesListItems by request parameters
     * @param requestParams request parameters
     * @param baseQueryAndParams query fragments
     * @return SpeciesListItems
     */
    def getSpeciesListItemsByParams(requestParams, baseQueryAndParams) {
        def speciesListItems
        if (requestParams.fq) {
            speciesListItems = SpeciesListItem.executeQuery("select sli " + baseQueryAndParams[0], baseQueryAndParams[1], requestParams)
        } else {
            def criteria = SpeciesListItem.createCriteria()

            def q = requestParams.q
            speciesListItems = criteria.list(requestParams) {
                and {
                    eq(DATA_RESOURCE_UID, requestParams.id)
                    if (q) {
                        def queryParam = "%" + q + "%"
                        or {
                            ilike(COMMON_NAME, queryParam)
                            ilike(MATCHED_NAME, queryParam)
                            ilike(RAW_SCIENTIFIC_NAME, queryParam)
                        }
                    }
                }
            }
        }
        speciesListItems
    }

    /**
     * Get total number of SpeciesListItems by request parameters
     * @param requestParams request parameters
     * @param baseQueryAndParams query fragments
     * @return total count
     */
    int getTotalCountByParams(requestParams, baseQueryAndParams) {
        def totalCount
        if (requestParams.fq) {
            totalCount = SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0], baseQueryAndParams[1]).head()
        } else {
            def criteria = SpeciesListItem.createCriteria()
            def q = requestParams.q
            totalCount = criteria.get {
                projections {
                    count()
                }
                and {
                    eq(DATA_RESOURCE_UID, requestParams.id)
                    if (q) {
                        def queryParam = "%" + q + "%"
                        or {
                            ilike(COMMON_NAME, queryParam)
                            ilike(MATCHED_NAME, queryParam)
                            ilike(RAW_SCIENTIFIC_NAME, queryParam)
                        }
                    }
                }
            }
        }
        totalCount
    }

    /**
     * Get total number of SpeciesListItems that not matched by request parameters
     * @param requestParams request parameters
     * @param baseQueryAndParams query fragments
     * @return total count
     */
    int getNoMatchCountByParams(requestParams, baseQueryAndParams) {
        def noMatchCount
        if (requestParams.fq) {
            noMatchCount = SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0] + " AND sli.guid is null", baseQueryAndParams[1]).head()
        } else {
            def criteria = SpeciesListItem.createCriteria()
            def q = requestParams.q
            noMatchCount = criteria.get {
                projections {
                    count()
                }
                and {
                    eq(DATA_RESOURCE_UID, requestParams.id)
                    isNull(GUID)
                    if (q) {
                        def queryParam = "%" + q + "%"
                        or {
                            ilike(COMMON_NAME, queryParam)
                            ilike(MATCHED_NAME, queryParam)
                            ilike(RAW_SCIENTIFIC_NAME, queryParam)
                        }
                    }
                }
            }
        }
        noMatchCount
    }

    /**
     * Get total number of guid of the SpeciesListItems by request parameters
     * @param requestParams request parameters
     * @param baseQueryAndParams query fragments
     * @return total count
     */
    int getDistinctCountByParams(requestParams, baseQueryAndParams) {
        def distinctCount
        if (requestParams.fq) {
            distinctCount = SpeciesListItem.executeQuery("select count(distinct guid) " + baseQueryAndParams[0], baseQueryAndParams[1]).head()
        } else {
            def criteria = SpeciesListItem.createCriteria()
            def q = requestParams.q
            distinctCount = criteria.get {
                projections {
                    countDistinct(GUID)
                }
                and {
                    eq(DATA_RESOURCE_UID, requestParams.id)
                    if (q) {
                        def queryParam = "%" + q + "%"
                        or {
                            ilike(COMMON_NAME, queryParam)
                            ilike(MATCHED_NAME, queryParam)
                            ilike(RAW_SCIENTIFIC_NAME, queryParam)
                        }
                    }
                }
            }
        }
        distinctCount
    }

    def getSpeciesListKVPKeysByDataResourceUid(String id) {
        def kvpKeys = SpeciesListKVP.executeQuery("select distinct key, itemOrder from SpeciesListKVP where dataResourceUid = :dataResourceUid order by itemOrder", [dataResourceUid: id]).collect { it[0] }
        return sortTaxonHeader(kvpKeys)
    }

    def sortTaxonHeader(header) {
        def taxons = ['vernacularName','vernacular Name','commonName','common Name','kingdom','family','order','class', 'rank', 'phylum','genus', 'taxonRank'].reverse()
        List headers = header.toList()
        def sortedHeader = []
        taxons.forEach {
            if (headers.stream().anyMatch(it::equalsIgnoreCase)) {
                sortedHeader.push(it)
                headers.removeIf(value->value.equalsIgnoreCase(it));
            }
        }
        sortedHeader.addAll(headers)
        sortedHeader
    }

    def getUsersForList() {
        SpeciesList.executeQuery("select distinct sl.username from SpeciesList sl")
    }

    def generateFacetValues(List fqs, baseQueryParams, String id, String q, int maxLengthForFacet) {
        def map = [:]
        //handle the user defined properties -- this will also make up the facets
        def properties = null
        if (fqs) {
            //get the ids for the query -- this allows correct counts when joins are being performed.
            def ids = SpeciesListItem.executeQuery("select distinct sli.id " + baseQueryParams[0], baseQueryParams[1])

            //println(ids)
            Map queryParameters = [druid: id]
            if (ids) {
                queryParameters.ids = ids
            }
            if (q) {
                queryParameters.qMatchedName = '%'+q+'%'
                queryParameters.qCommonName = '%'+q+'%'
                queryParameters.qRawScientificName = '%'+q+'%'
            }
            def timeStart = new Date()
            def results = SpeciesListItem.executeQuery("select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt from SpeciesListItem as sli " +
                    "join sli.kvpValues  as kvp where sli.dataResourceUid = :druid ${ids ? 'and sli.id in (:ids)' : ''} " +
                    "${q ? 'and (sli.matchedName like :qMatchedName or sli.commonName like :qCommonName or sli.rawScientificName like :qRawScientificName) ' : ''} " +
                    "group by kvp.key, kvp.value, kvp.vocabValue, kvp.itemOrder, kvp.key order by kvp.itemOrder, kvp.key, cnt desc",
                    queryParameters)
            def timeStop = new Date()
            log.info("Query KVP of ${fqs} took " + TimeCategory.minus(timeStop, timeStart))

            //obtain the families from the common list facets
            def commonResults = SpeciesListItem.executeQuery("select sli.family, count(sli) as cnt from SpeciesListItem sli " +
                    "where sli.family is not null AND sli.dataResourceUid = :druid ${ids ? 'and sli.id in (:ids)' : ''} " +
                    "${q ? 'and (sli.matchedName like :qMatchedName or sli.commonName like :qCommonName or sli.rawScientificName like :qRawScientificName) ' : ''} " +
                    "group by sli.family order by cnt desc",
                    queryParameters)

            if (commonResults.size() > 1) {
                map[MATCHED_FAMILY] = commonResults
            }


            //println(results)
            properties = results.findAll{ it[1] && it[1]?.length()<maxLengthForFacet }.groupBy { it[0] }.findAll{ it.value.size()>1}

        } else {
            def qParam = '%'+q+'%'
            def queryParameters = q ? [dataResourceUid: id, matchedName: qParam, commonName: qParam, rawScientificName: qParam] : [dataResourceUid: id]
            def timeStart = new Date()
            def results = SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt from SpeciesListItem as sli ' +
                    'join sli.kvpValues as kvp where sli.dataResourceUid = :dataResourceUid ' +
                    "${q ? 'and (sli.matchedName like :matchedName or sli.commonName like :commonName or sli.rawScientificName like :rawScientificName) ' : ''} " +
                    'group by kvp.key, kvp.value, kvp.vocabValue, kvp.itemOrder order by kvp.itemOrder, kvp.key, cnt desc',
                    queryParameters)
            def timeStop = new Date()
            log.info("Query KVP of ${id} took " + TimeCategory.minus(timeStop, timeStart))
            properties = results.findAll{it[1] && it[1]?.length()<maxLengthForFacet}.groupBy{it[0]}.findAll{it.value.size()>1 }
            //obtain the families from the common list facets
            def commonResults = SpeciesListItem.executeQuery('select family, count(*) as cnt from SpeciesListItem ' +
                    'where family is not null AND dataResourceUid = :dataResourceUid ' +
                    "${q ? 'and (matchedName like :matchedName or commonName like :commonName or rawScientificName like :rawScientificName) ' : ''} " +
                    'group by family order by cnt desc',
                    queryParameters)
            if(commonResults.size() > 1) {
                map[MATCHED_FAMILY] = commonResults
            }
        }
        //if there was a facet included in the result we will need to divide the
        if(properties) {
            map.listProperties = properties
        }

        //handle the configurable facets
        map
    }
}

