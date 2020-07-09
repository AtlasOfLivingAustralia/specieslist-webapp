package au.org.ala.specieslist

import au.org.ala.web.AuthService
import com.opencsv.CSVWriter
import grails.converters.JSON

class SpeciesListItemController {

    BieService bieService
    BiocacheService biocacheService
    LoggerService loggerService
    QueryService queryService
    LocalAuthService localAuthService
    AuthService authService
    int maxLengthForFacet = 30 // Note: is length of _name_ of the facet category/field

    def index() { }

    /**
     * Public display of a species list
     */
    def list(){
        doListDisplay(params)
    }

    /**
     * There is no functional difference between listAuth and list. This method has been retained to support existing
     * links/bookmarks/etc that may refer to it. Both URLs are in the authenticateOnlyIfLoggedInFilterPattern list
     * for CAS authentication.
     */
    def listAuth() {
        doListDisplay(params)
    }

    /**
     * Special (simple) page for displaying "Australia's Species", linked from homepage.
     *
     * @return
     */
    def iconicSpecies() {
        params.id = params.id?:grailsApplication.config.iconicSpecies?.uid?:""
        params.max = params.max?:25

        if (!params.fq || !params.fq.startsWith("kvp")) {
            redirect(action: 'iconicSpecies', params: [fq:'kvp group:Birds'])
        } else {
            try {
                def speciesList = SpeciesList.findByDataResourceUid(params.id)
                if (!speciesList) {
                    flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), params.id])}"
                    render(view: "iconic-list")
                } else {
                    params.max = Math.min(params.max ? params.int('max') : 25, 100)
                    params.sort = params.sort ?: "itemOrder"
                    params.fetch = [kvpValues: 'select']
                    def fqs = params.fq ? [params.fq].flatten().findAll { it != null } : null
                    def baseQueryAndParams = params.fq ? queryService.constructWithFacets(" from SpeciesListItem sli ", fqs, params.id) : null
                    //need to get all keys to be included in the table so no need to add the filter.
                    def speciesListItems = params.fq ? SpeciesListItem.executeQuery("select sli " + baseQueryAndParams[0], baseQueryAndParams[1], params) : SpeciesListItem.findAllByDataResourceUid(params.id, params)
                    def totalCount = params.fq ? SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0], baseQueryAndParams[1]).head() : SpeciesListItem.countByDataResourceUid(params.id)
                    def guids = speciesListItems.collect { it.guid }
                    def bieItems = bieService.bulkLookupSpecies(guids)
                    def facets = generateFacetValues(null, baseQueryAndParams)
                    log.debug "speciesListItems = ${speciesListItems as JSON}"
                    render(view: 'iconic-list', model: [
                            results: speciesListItems,
                            totalCount: totalCount,
                            bieItems: bieItems,
                            facets: facets
                    ])
                }
            }  catch (Exception e) {
                def msg = "Unable to retrieve species list items. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()
                log.error(msg, e)
                flash.message = msg
                render(view: 'iconic-list')
            }
        }
    }

    private doListDisplay(requestParams) {
        if (requestParams.id) {
            try {
                //check to see if the list exists
                def speciesList = SpeciesList.findByDataResourceUid(requestParams.id)
                if (!speciesList) {
                    flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), requestParams.id])}"
                    redirect(controller: "public", action: "speciesLists")
                } else {
                    if (requestParams.message)
                        flash.message = requestParams.message
                    requestParams.max = Math.min(requestParams.max ? requestParams.int('max') : 10, 100)
                    requestParams.sort = requestParams.sort ?: "itemOrder"
                    requestParams.offset = requestParams.int('offset') ?: 0
                    requestParams.fetch = [kvpValues: 'select']

                    log.debug(requestParams.toQueryString())

                    if(requestParams.q) {
                        requestParams.fq = "Search-" + "rawScientificName:" + requestParams.q
                    }
                    //println(params.facets)
                    def fqs = requestParams.fq ? [requestParams.fq].flatten().findAll { it != null } : null
                    //println(queryService.constructWithFacets("select count(distinct guid)",facets, params.id))

                    if(fqs) {
                        def reqSearchParam = fqs.find { item -> item.startsWith('Search-')}
                        if(reqSearchParam) {
                            int index = reqSearchParam.indexOf(":") + 1
                            params.q = reqSearchParam.substring(index);
                        }
                    }

                    def baseQueryAndParams = requestParams.fq ? queryService.constructWithFacets(" from SpeciesListItem sli ", fqs, requestParams.id) : null
                    log.debug(baseQueryAndParams?.toString())

                    // to sort on a column 'order by' clause has to be added explicitly since executeQuery function does
                    // not accept sort and order as named parameters. Named parameters accepted by executeQuery includes
                    // max and offset. check documentation for more details.
                    List baseQueryAndParamsForListingSLI = baseQueryAndParams?.clone()
                    if(requestParams.sort && baseQueryAndParams){
                        baseQueryAndParamsForListingSLI[0] +=" order by sli.${requestParams.sort} ${requestParams.order?:'asc'}"
                    }

                    //This will take in to account and search user has done in addition to the filters
                    def distinctCount = requestParams.fq ? SpeciesList.executeQuery("select count(distinct guid) " + baseQueryAndParams[0], baseQueryAndParams[1]).head(): SpeciesListItem.executeQuery("select count(distinct guid) from SpeciesListItem where dataResourceUid=?", requestParams.id).head()

                    //need to get all keys to be included in the table so no need to add the filter.
                    def keys = SpeciesListKVP.executeQuery("select distinct key, itemOrder from SpeciesListKVP where dataResourceUid=? order by itemOrder", requestParams.id).collect { it[0] }

                    //list of species by search criteria and filters applied
                    def speciesListItems = requestParams.fq ? SpeciesListItem.executeQuery("select sli " + baseQueryAndParamsForListingSLI[0], baseQueryAndParamsForListingSLI[1], requestParams): SpeciesListItem.findAllByDataResourceUid(requestParams.id, requestParams)

                    //Count of species list item after applying filter and search criteria
                    def totalCount = requestParams.fq ? SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0], baseQueryAndParams[1]).head() :SpeciesListItem.countByDataResourceUid(requestParams.id)

                    def noMatchCount = requestParams.fq ? SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0] + " AND sli.guid is null", baseQueryAndParams[1]).head() : SpeciesListItem.countByDataResourceUidAndGuidIsNull(requestParams.id)

                    def users = SpeciesList.executeQuery("select distinct sl.username from SpeciesList sl")

                    //println(speciesListItems)
                    //log.debug("KEYS: " + keys)
                    def guids = speciesListItems.collect { it.guid }
                    log.debug("guids " + guids)
                    def downloadReasons = loggerService.getReasons()
                    log.debug("Retrieved Logger Reasons")
                    def facets = generateFacetValues(fqs, baseQueryAndParams)
                    log.debug("Retrieved facets")
                    log.debug("Checking speciesList: " + speciesList)
                    log.debug("Checking editors: " + speciesList.editors)
                    render(view: 'list', model: [
                            speciesList: speciesList,
                            params: requestParams,
                            results: speciesListItems,
                            totalCount: totalCount,
                            noMatchCount: noMatchCount,
                            distinctCount: distinctCount,
                            hasUnrecognised: noMatchCount > 0,
                            keys: keys,
                            downloadReasons: downloadReasons,
                            users: users,
                            facets: facets,
                            fqs : fqs
                    ])
                }
            } catch (Exception e) {
                log.error("Unable to view species list items.", e)
                render(view: '../error', model: [message: "Unable to retrieve species list items. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
            }
        } else {
            //redirect to the public species list page
            redirect(controller: "public", action: "speciesLists")
        }
    }

    private def generateFacetValues(List fqs, baseQueryParams){
        def map = [:]

        //handle the user defined properties -- this will also make up the facets
        String selectPart = "select distinct kvp.key, kvp.value, kvp.vocabValue, sli.itemOrder, count(sli) as cnt";
        def middlePart = fqs ? queryService.constructWithFacets(" from SpeciesListItem as sli join sli.kvpValues kvp1 join sli.kvpValues kvp", fqs, params.id) : null
        def properties = null
        if(fqs){
            //get the ids for the query -- this allows correct counts when joins are being performed.
            def ids = SpeciesListItem.executeQuery("select distinct sli.id " + baseQueryParams[0], baseQueryParams[1])

            //println(ids)
            Map queryParameters = [druid: params.id]
            if (ids) {
                queryParameters.ids = ids
            }

            def results = SpeciesListItem.executeQuery("select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=:druid ${ids ? 'and sli.id in (:ids)' : ''} group by kvp.key, kvp.value, kvp.vocabValue, kvp.itemOrder, kvp.key order by kvp.itemOrder, kvp.key, cnt desc", queryParameters)

            //obtain the families from the common list facets
            def commonResults = SpeciesListItem.executeQuery("select sli.family, count(sli) as cnt from SpeciesListItem sli where sli.family is not null AND sli.dataResourceUid=:druid ${ids ? 'and sli.id in (:ids)' : ''} group by sli.family order by cnt desc", queryParameters)
            if(commonResults.size() > 1) {
                map.family = commonResults
            }

            //println(results)
            properties = results.findAll{ it[1].length()<maxLengthForFacet }.groupBy { it[0] }.findAll{ it.value.size()>1}

        } else {
            def result = fqs? SpeciesListItem.executeQuery(selectPart + middlePart[0] +
                    " group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc", middlePart[1]):
                SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt  from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=? group by kvp.key, kvp.value, kvp.vocabValue, kvp.itemOrder order by kvp.itemOrder, kvp.key, cnt desc', params.id)

            properties = result.findAll{it[1].length()<maxLengthForFacet}.groupBy{it[0]}.findAll{it.value.size()>1 }

            //obtain the families from the common list facets
            def commonResults = SpeciesListItem.executeQuery('select family, count(*) as cnt from SpeciesListItem where family is not null AND dataResourceUid=? group by family order by cnt desc',params.id)
            if(commonResults.size() > 1) {
                map.family = commonResults
            }
        }
        //if there was a facet included in the result we will need to divide the
        if(properties) {
            map.listProperties = properties
        }

        //handle the configurable facets
        map
    }

    def facetsvalues(){
        if(params.id){
          def result = SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=? group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc', params.id)
          //group the same properties keys together
          def properties = result.groupBy { it[0] }
          def map = [:]
          map.listProperties = properties
          render map as JSON
        }
        null
    }

    /**
     * Downloads the records for the supplied species list
     * @return
     */
    def downloadList(){
        if (params.id){
            params.fetch = [ kvpValues: 'join' ]
            log.debug("Downloading Species List")
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid='"+params.id+"'")
            def fqs = params.fq?[params.fq].flatten().findAll{ it != null } : null
            def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id)
            def sli = SpeciesListItem.executeQuery("Select sli " + baseQueryAndParams[0], baseQueryAndParams[1])
            //def sli =SpeciesListItem.findAllByDataResourceUid(params.id,params)
            def out = new StringWriter()
            def csvWriter = new CSVWriter(out)
            def header =  ["Supplied Name","guid","scientificName","family","kingdom"]
            header.addAll(keys)
            log.debug(header?.toString())
            csvWriter.writeNext(header as String[])
            sli.each {
                def values = keys.collect{key->it.kvpValues.find {kvp -> kvp.key == key}}.collect { kvp -> kvp?.vocabValue?:kvp?.value}
                def row = [it.rawScientificName, it.guid, it.matchedName, it.family, it.kingdom]
                row.addAll(values)
                csvWriter.writeNext(row as String[])
            }
            csvWriter.close()
            def filename = params.file?:"list.csv"
            if(!filename.toLowerCase().endsWith('.csv')){
                filename += '.csv'
            }

            response.addHeader("Content-Disposition", "attachment;filename="+filename);
            render(contentType: 'text/csv', text:out.toString())
        }
    }

    /**
     * Returns BIE details about the supplied guid
     * @return
     */
    def itemDetails(){
        log.debug("Returning item details for " + params)
        if(params.guids){
            render bieService.bulkLookupSpecies(params.guid) as JSON
        } else {
            null
        }
    }
}