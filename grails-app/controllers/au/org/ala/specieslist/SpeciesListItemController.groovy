package au.org.ala.specieslist
import grails.converters.*
import au.com.bytecode.opencsv.CSVWriter

class SpeciesListItemController {
    def bieService
    def loggerService
    def authService
    def index() { }
    /**
     *
     * @return
     */
    def list(){
        //can only show the list items for a specific list id.  List items do not make sense out of the context if their list
        if(params.id){
            if (params.message)
                flash.message = params.message
            params.max = Math.min(params.max ? params.int('max') : 10, 100)
            params.sort = params.sort ?: "id"
            params.fetch= [ kvpValues: 'select' ]

            log.debug(params.toQueryString())
            def distinctCount = SpeciesListItem.executeQuery("select count(distinct guid) from SpeciesListItem where dataResourceUid='"+params.id+"'").head()
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid='"+params.id+"'")
            def speciesListItems =  SpeciesListItem.findAllByDataResourceUid(params.id,params)
            log.debug("KEYS: " + keys)
            def guids = speciesListItems.collect{it.guid}
            render(view:'list', model:[speciesList: SpeciesList.findByDataResourceUid(params.id),results: speciesListItems,
                    totalCount:SpeciesListItem.countByDataResourceUid(params.id),
                    noMatchCount:SpeciesListItem.countByDataResourceUidAndGuidIsNull(params.id),
                    distinctCount:distinctCount, keys:keys, bieItems:bieService.bulkLookupSpecies(guids), downloadReasons:loggerService.getReasons()])
        }
        else{
            //redirect to the public species list page
            redirect(controller: "public", action: "list")
        }

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
            def sli =SpeciesListItem.findAllByDataResourceUid(params.id,params)
            def out = new StringWriter()
            def csvWriter = new CSVWriter(out)
            def header =  ["scientificName","guid"]
            header.addAll(keys)
            log.debug(header)
            csvWriter.writeNext(header  as String[])
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
