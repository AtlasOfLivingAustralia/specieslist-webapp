package au.org.ala.specieslist

import org.grails.core.DefaultGrailsDomainClass
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Order

class QueryService {

    public static final String EDITOR_SQL_RESTRICTION = "this_.id in (select species_list_id from species_list_editors e where e.editors_string = ?)"
    public static final String USER_ID = "userId"
    public static final String IS_PRIVATE = "isPrivate"
    public static final String LIST_NAME = "listName"
    public static final String ASC = "asc"
    public static final String LAST_UPDATED = "lastUpdated"

    def authService
    def localAuthService

    /** A regular expression to split the  */
    public final def filterRegEx = /([a-zA-Z]+):([\x00-\x7F\s]*)/
    /** A map of domain property names to data types */
    def speciesListProperties= new DefaultGrailsDomainClass(SpeciesList.class).persistentProperties.collectEntries{[it.name, it.type]}
    /** A map of criteria method names to Methods - allow the use of reflection to gain the criteria method to use */
    def criteriaMethods = grails.orm.HibernateCriteriaBuilder.class.getMethods().findAll {
        it.getParameterTypes().length < 3
    }.collectEntries{ [it.name, it] }

    def getFilterListResult(params){
        getFilterListResult(params, false)
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
    def getFilterListResult(params, boolean hidePrivateLists){
        //list should be based on the user that is logged in
        params.max = Math.min(params.max ? params.int('max') : 25, 1000)

        //remove sort from params
        def sort = params.sort ?: LIST_NAME
        def order = params.order ?: ASC

        params.fetch = [items: 'lazy']

        def c = SpeciesList.createCriteria()
        def lists = c.list(params){
            and {
                params.each { key, value ->
                    //the value suffix tells us which filter operation to perform
                    if ('q'.equals(key)) {
                        or {
                            ilike('listName', '%' + value + '%')
                            ilike('firstName', '%' + value + '%')
                            ilike('surname', '%' + value + '%')
                            ilike('description', '%' + value + '%')
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
                    } else {
                        log.debug("User is admin, so has visibility og private lists")
                    }
                } else {
                    // if there is no user, do no show any private records
                    or {
                        isNull(IS_PRIVATE)
                        eq(IS_PRIVATE, false)
                    }
                }
            }
            setSortOrder(sort, order, params.user, c)
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
            def query = booleanQueryFacets.get(key)
            if (query){
                selectedFacets << [query: key, facet: query]
            } else if (key == "listType"){
                def cleanedVaue = value.replaceAll("eq:", "")
                query = listTyoeFacets.get(cleanedVaue)
                selectedFacets << [query: key, facet: query]
            }
        }
        selectedFacets
    }

    //TODO make this more configurable
    def booleanQueryFacets = [
          "isAuthoritative" : [label:'authoritative.list'],
          "isThreatened": [label:'threatened.list'],
          "isInvasive": [label:'invasive.list'],
          "isSDS": [label:'sensitive.list']
    ]

    //TODO make this more configurable
    def listTyoeFacets = [
        (ListType.CONSERVATION_LIST.toString()): [listType: ListType.CONSERVATION_LIST, label: ListType.CONSERVATION_LIST.i18nValue],
        (ListType.COMMON_HABITAT.toString())   : [listType: ListType.COMMON_HABITAT, label: ListType.COMMON_HABITAT.i18nValue],
        (ListType.LOCAL_LIST.toString())     : [listType: ListType.LOCAL_LIST, label: ListType.LOCAL_LIST.i18nValue],
        (ListType.COMMON_TRAIT.toString())     : [listType: ListType.COMMON_TRAIT, label: ListType.COMMON_TRAIT.i18nValue]
    ]

    def getFacetCounts(params){
        getFacetCounts(params, false)
    }

    def getFacetCounts(params, boolean hidePrivateLists){
        def facets = []
        booleanQueryFacets.each { key, facet ->
            facets << [query: key + '=eq:true', label:  facet.label , count:  getFacetCount(params, key, true, hidePrivateLists)]
        }
        listTyoeFacets.each { key, facet ->
            facets <<  [query:'listType=eq:' + facet.listType.toString(), label: facet.label, count: getFacetCount(params, "listType", facet.listType, hidePrivateLists)]
        }
        return facets
    }

    def getFacetCount(params, facetField, facetValue, boolean hidePrivateLists) {

        def c = SpeciesList.createCriteria()
        def facetCount = c.get  {

            projections {
                count()
            }

            and {
                params.each { key, value ->
                    //the value suffix tells us which filter operation to perform
                    if ('q'.equals(key)) {
                        or {
                            ilike('listName', '%' + value + '%')
                            ilike('firstName', '%' + value + '%')
                            ilike('surname', '%' + value + '%')
                            ilike('description', '%' + value + '%')
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
                eq(facetField, facetValue)
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

    /**
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
        def c = SpeciesListItem.createCriteria()

        //log.debug("CRITERIA METHODS: " +criteriaMethods)
        c.list(props += params){
            //set the results transformer so that we don't get duplicate records because of
            // the 1:many relationship between a list item and KVP
            setResultTransformer(org.hibernate.criterion.CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            and {
                if(distinctField){
                    distinct(distinctField)
                    isNotNull(distinctField)
                }
                if(guid)eq('guid', guid)
                if(lists){
                    'in'('dataResourceUid', lists)
                }

                params.each { key, value ->
                    log.debug("KEYS: " +key+" " + speciesListProperties.containsKey(key))
                    if(speciesListProperties.containsKey(key)){
                        mylist {
                            //the value suffix tells us which filter operation to perform
                            def matcher = (value =~ filterRegEx)
                            if(matcher.matches()){
                                def fvalue = matcher[0][2] //
                                //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                                def method = criteriaMethods.get(matcher[0][1])
                                if(method){
                                    Object[] args =[getValueBasedOnType(speciesListProperties[key],fvalue)]
                                    if(method.getParameterTypes().size()>1)
                                        args = [key] +args[0]
                                    //log.debug("ARGS : " +args + " method : " + method)
                                    method.invoke(c, args)
                                }
                            }
                        }
                    } else {
                        //the value suffix tells us which filter operation to perform
                        def matcher = (value =~ filterRegEx)
                        if(matcher.matches()){
                            def fvalue = matcher[0][2]

                            //now handle the supported filter conditions by gaining access to the criteria methods using reflection
                            def method = criteriaMethods.get(matcher[0][1])
                            if(method){
                                Object[] args =[getValueBasedOnType(speciesListProperties[key], fvalue)]
                                if(method.getParameterTypes().size() > 1) {
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
        switch(type){
            case Boolean.class: Boolean.parseBoolean(value); break;
            case ListType.class: ListType.valueOf(value); break;
            default:value; break;
        }
    }
    
    /**
     * Constructs a query based on the filters that have been applied in the KVPs etc.
     * @param base
     * @param facets
     * @param dataResourceUid
     * @return
     */
    def constructWithFacets(String base, List facets, String dataResourceUid) {
        StringBuilder query = new StringBuilder(base)
        StringBuilder whereBuilder = new StringBuilder(" where sli.dataResourceUid=? ")
        //query.append(" from SpeciesListItem sli join sli.kvpValues kvp where sli.dataResourceUid=? ")
        def queryparams = [dataResourceUid]
        if (facets){
            facets.eachWithIndex { facet, index ->
                if (facet.startsWith("kvp")){
                    String sindex = index.toString();
                    facet = facet.replaceFirst("kvp ","")
                    String key = facet.substring(0,facet.indexOf(":"))
                    String value = facet.substring(facet.indexOf(":")+1)
                    query.append(" join sli.kvpValues kvp").append(sindex)
                    whereBuilder.append(" AND kvp").append(sindex).append(".key=? AND kvp").append(sindex).append(".value=?")
                    queryparams.addAll([key, value])
                    //println queryparams
                } else {
                    //must be a facet with the same table
                    boolean isSearch = false;
                    if (facet.startsWith("Search-")) {
                        isSearch = true;
                        facet = facet.replaceFirst("Search-","")
                    }
                    String key = facet.substring(0,facet.indexOf(":"))
                    String value = facet.substring(facet.indexOf(":")+1)
                    whereBuilder.append( "AND sli.").append(key)
                    if (value.equalsIgnoreCase("null")){
                        whereBuilder.append(" is null")
                    } else if(isSearch) {
                        whereBuilder.append(" like ? ")
                        queryparams.add("%" + value + "%")
                    } else {
                        whereBuilder.append("=?")
                        queryparams.add(value)
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
                'in' "dataResourceUid", drIds
            }

            or {
                'in' "matchedName", scientificNames
                'in' "rawScientificName", scientificNames
            }

            projections {
                distinct "dataResourceUid"
            }
        }
    }
}

