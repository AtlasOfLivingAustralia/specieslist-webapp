package au.org.ala.specieslist
import grails.converters.*
import au.com.bytecode.opencsv.CSVWriter
import org.grails.datastore.mapping.query.Restrictions

class SpeciesListItemController {
    def bieService
    def loggerService
    def queryService
    def maxLengthForFacet = 15
    def index() { }
    /**
     *
     * @return
     */
    def list(){
        //can only show the list items for a specific list id.  List items do not make sense out of the context if their list
        if(params.id){
            try{
                //check to see if the list exists
                def speciesList = SpeciesList.findByDataResourceUid(params.id)
                if (!speciesList){
                    flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), params.id])}"
                    redirect(controller: "public", action: "speciesLists")
                }

                else{
                    if (params.message)
                        flash.message = params.message
                    params.max = Math.min(params.max ? params.int('max') : 10, 100)
                    params.sort = params.sort ?: "itemOrder"
                    params.fetch= [ kvpValues: 'select' ]

                    log.debug(params.toQueryString())
                    //println(params.facets)
                    def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
                    def queryParams = params.fq?"&fq="+fqs.join("&fq="):""
                    //println(queryService.constructWithFacets("select count(distinct guid)",facets, params.id))

                    def baseQueryAndParams = params.fq?queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id):null
                    log.debug(baseQueryAndParams)
                    //def queryparams = params.fq? queryService.constructWithFacets("select count(distinct guid)",fqs,params.id): ["select count(distinct guid) from SpeciesListItem where dataResourceUid=?",[params.id]]
                    //This is used for the stats - should these be for the whole list or just the fqed version?
                    def distinctCount =  params.fq?SpeciesList.executeQuery("select count(distinct guid) " + baseQueryAndParams[0],baseQueryAndParams[1]).head():SpeciesListItem.executeQuery("select count(distinct guid) from SpeciesListItem where dataResourceUid=?",params.id).head()//SpeciesListItem.executeQuery(queryparams[0],[queryparams[1]]).head()
                    //need to get all keys to be included in the table so no need to add the filter.
                    def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=?",params.id)

                    //def spqueries = params.fq ? queryService.constructWithFacets("select sli ", fqs, params.id):["select sli from SpeciesListItem as sli where sli.dataResourceUid=?",[params.id]]
                    //println(spqueries)
                    def speciesListItems =  params.fq? SpeciesListItem.executeQuery("select sli " + baseQueryAndParams[0], baseQueryAndParams[1],params): SpeciesListItem.findAllByDataResourceUid(params.id,params)

                    def totalCount= params.fq ? SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0], baseQueryAndParams[1]).head():SpeciesListItem.countByDataResourceUid(params.id)

                    def noMatchCount = params.fq ? SpeciesListItem.executeQuery("select count(*) " + baseQueryAndParams[0] + " AND sli.guid is null", baseQueryAndParams[1]).head(): SpeciesListItem.countByDataResourceUidAndGuidIsNull(params.id)



                    //println(speciesListItems)
                    //log.debug("KEYS: " + keys)
                    def guids = speciesListItems.collect{it.guid}
                    log.debug("guids " + guids)
                    def bieItems = bieService.bulkLookupSpecies(guids)
                    log.debug("Retrieved BIE Items")
                    def downloadReasons = loggerService.getReasons()
                    log.debug("Retrieved Logger Reasons")
                    def facets = generateFacetValues(fqs, baseQueryAndParams)
                    log.debug("Retrived facets")
                    render(view:'list', model:[speciesList: SpeciesList.findByDataResourceUid(params.id),queryParams:queryParams,results: speciesListItems,
                            totalCount:totalCount,
                            noMatchCount:noMatchCount,
                            distinctCount:distinctCount, keys:keys, bieItems:bieItems, downloadReasons:downloadReasons, facets:facets])
                }
            }
            catch(Exception e){
                log.error("Unable to view species list items.", e)
                render(view: '../error', model: [message: "Unable to retrieve species list items. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
            }
        }
        else{
            //redirect to the public species list page
            redirect(controller: "public", action: "speciesLists")
        }
    }

    private def generateFacetValues(List fqs, baseQueryParams){
        def map = [:]

        //handle the user defined properties -- this will also make up the facets
        String selectPart = "select distinct kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt";
        def middlePart = fqs ? queryService.constructWithFacets(" from SpeciesListItem as sli join sli.kvpValues kvp1 join sli.kvpValues kvp", fqs, params.id) : null
        def properties = null
        if(fqs){
            //get the ids for the query -- this allows correct counts when joins are being performed.
            def ids = SpeciesListItem.executeQuery("select distinct sli.id " + baseQueryParams[0], baseQueryParams[1])

            //println(ids)
            def results = SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt  from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=:druid and sli.id in (:list) group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc', [druid:params.id,list:ids])

            //println(results)
            properties = results.findAll{ it[1].length()<maxLengthForFacet }.groupBy { it[0] }.findAll{ it.value.size()>1}

        } else {
            def result = fqs? SpeciesListItem.executeQuery(selectPart+middlePart[0] +
                    " group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc", middlePart[1]):
                SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt  from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=? group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc', params.id)
            //def result = baseQueryParams? SpeciesListItem.executeQuery(selectPart + baseQueryParams[0] + " group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc", baseQueryParams[1]) : SpeciesListItem.executeQuery(selectPart +'  from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=? group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc', params.id)
            //println(result)
            properties = result.findAll{it[1].length()<maxLengthForFacet}.groupBy{it[0]}.findAll{it.value.size()>1 }
        }
        //if there was a facet included in the result we will need to divide the
        if(properties)
            map.listProperties = properties

        //handle the configurable facets
        map
    }

    def facetsvalues(){
        if(params.id){
          def result = SpeciesListItem.executeQuery('select kvp.key, kvp.value, kvp.vocabValue, count(sli) as cnt from SpeciesListItem as sli join sli.kvpValues  as kvp where sli.dataResourceUid=? group by kvp.key, kvp.value, kvp.vocabValue order by kvp.key,cnt desc', params.id)
          //group the same properties keys together
          def properties = result.groupBy {it[0]}
          def map =[:]
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
            params.fetch= [ kvpValues: 'join' ]
            log.debug("Downloading Species List")
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid='"+params.id+"'")
            def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
            def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id)
            def sli = SpeciesListItem.executeQuery("Select sli " + baseQueryAndParams[0], baseQueryAndParams[1])
            //def sli =SpeciesListItem.findAllByDataResourceUid(params.id,params)
            def out = new StringWriter()
            def csvWriter = new CSVWriter(out)
            def header =  ["Supplied Name","guid"]
            header.addAll(keys)
            log.debug(header)
            csvWriter.writeNext(header as String[])
            sli.each{
                def values = keys.collect{key->it.kvpValues.find {kvp ->kvp.key == key}}.collect{kvp->kvp?.vocabValue?:kvp?.value}
                def row = [it.rawScientificName,it.guid]
                row.addAll(values)
                csvWriter.writeNext(row as String[])
            }
            csvWriter.close()
            def filename = params.file?:"list.csv"
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
        }
        else
            null
    }
}
