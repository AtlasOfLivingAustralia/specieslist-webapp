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

import au.com.bytecode.opencsv.CSVReader
import au.org.ala.checklist.lucene.model.NameSearchResult
import grails.converters.*


class SpeciesListController {

    def helperService
    def authService
    def bieService
    def biocacheService
    def loggerService
    def queryService

    def noOfRowsToDisplay = 5

    def index() { redirect(action: 'upload')}

    def upload(){ /*maps to the upload.gsp */
        log.debug(ListType.values())
        render(view:"upload",model:  [listTypes:ListType.values()])
    }
    /**
     * Current mechnism for deleting a species list
     * @return
     */
    def delete(){
        //println("DELETING " + params)
        def sl = SpeciesList.get(params.id)
        if(sl){
            sl.delete()
        }
        redirect(action: 'list')

    }
    /**
     * OLD delete
     * @return
     */
    def deleteList(){
        //delete all the items that belong to the specified list
        //SpeciesListItem.where {dataResourceUid == params.id}.deleteAll()
        log.debug(params)
        //performs the cascade delete that is required.
        SpeciesListItem.findAllByDataResourceUid(params.id)*.delete()
        SpeciesListKVP.findAllByDataResourceUid(params.id)*.delete()
        //TODO should the the dr be deleted from the collectory??
        flash.message = "Deleted Species List " + params.id
        redirect(action:  'upload')
    }

    def uploadList(){
        //the raw data and list name must exist
        log.debug("upload the list....")
        log.debug(params)
        org.codehaus.groovy.grails.web.json.JSONObject formParams = JSON.parse(request.getReader())
        log.debug(formParams.toString() + " class : " + formParams.getClass())

        if(formParams.speciesListName && formParams.headers && formParams.rawData){
            def drURL = helperService.addDataResourceForList(formParams.speciesListName, formParams.description, formParams.listUrl,null)
            log.debug(drURL)
            if(drURL){
                def druid = drURL.toString().substring(drURL.lastIndexOf('/') +1)
                log.debug("Loading species list " + formParams.speciesListName)
                def vocabs = formParams.findAll { it.key.startsWith("vocab") && it.value } //map of vocabs
                log.debug("Vocabs: " +vocabs)
                CSVReader reader = helperService.getCSVReaderForText(formParams.rawData, helperService.getSeparator(formParams.rawData))
                def header = formParams.headers
                log.debug("Header: " +header)
                helperService.loadSpeciesList(reader,druid,formParams.speciesListName, ListType.valueOf(formParams.get("listType")), formParams.description, formParams.listUrl, formParams.listWkt, header.split(","),vocabs)
                def url =createLink(controller:'speciesListItem', action:'list', id: druid) +"?max=15"
                def map = [url:url]
//                println("THE URKL: "+url)
                render map as JSON
                //redirect(controller: "speciesListItem",action: "list",id: druid,params: [max: 15, sort:"id"])
            }
            else{
                response.outputStream.write("Unable to add species list at this time. If this problem persists please report it.".getBytes())
                response.setStatus(500)
                response.sendError(500, "Unable to add species list at this time. If this problem persists please report it.")
                //def map =[error: "Unable to add species list at this time. If this problem persists please report it."]
                //render map as JSON
                //render(view: "upload")
                //throw new Error("Unablel to add species list at this time. If this problem persists please report it.")
            }
        }
    }

    /**
     * Submits the supplied list
     *
     * This is the OLD method when supplying a file name
     */
    def submitList(){
        //ensure that the species list file exists before creating anything
        def uploadedFile = request.getFile('species_list')
        if(!uploadedFile.empty){
            //create a new temp data resource in the collectory for this species list
            //def udetails = new UserDetailsCommand()
            //udetails.name = params.speciesListTitle
            def drURL = helperService.addDataResourceForList(params.speciesListTitle)
            def druid = drURL.toString().substring(drURL.lastIndexOf('/') +1)
            //download the supplied file to the local files system
            def localFilePath = helperService.uploadFile(druid, uploadedFile)
            log.debug("The local file " + localFilePath)
            //create a new species list item for each line of the file
            log.debug(params)
            //set of the columns if necessary
            def columns = params.findAll { it.key.startsWith("column") && it.value }.sort{it.key.replaceAll("column","") as int}
            log.debug(columns.toString() + " " + columns.size())

            def header = (columns.size() > 0 && !params.containsKey('rowIndicatesMapping')) ? columns.values().toArray(["",""]) : null

            //sort out the vocabulary
            def vocabs = !params.containsKey('rowIndicatesMapping')?params.findAll { it.key.startsWith("vocab") && it.value }.sort{it.key.replaceAll("vocab","") as int}:null
            def vocabMap = [:]
            vocabs?.each{
                int i = it.key.replaceAll("vocab","") as int
                log.debug("header :" + header[i] + " value: "+ it.value)
                vocabMap.put(header[i] , it.value)
            }
            log.debug(vocabMap)
            helperService.loadSpeciesList(params.speciesListTitle,druid,localFilePath,params.containsKey('rowIndicatesMapping'),header,vocabMap)
            //redirect the use to a summary of the records that they submitted - include links to BIE species page
            //also mention that the list will be loaded into BIE overnight.
            //def speciesListItems =  au.org.ala.specieslist.SpeciesListItem.findAllByDataResourceUid(druid)
            flash.params
            //[max:10, sort:"title", order:"desc", offset:100]
            //render(view:'list', model:[results: speciesListItems])
            redirect(controller: "speciesListItem",action: "list",id: druid,params: [max: 15, sort:"id"])//,id: druid, max: 10, sort:"id")
        }
    }

    def list(){
        //list should be based on the user that is logged in
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ?: "listName"
        params.fetch = [items: 'lazy']
        try{
            def lists = SpeciesList.findAllByUsername(authService.email(),params)
            def count = SpeciesList.countByUsername(authService.email(),params)
            render(view: "list", model: [lists:lists, total:count])
        }
        catch(Exception e){
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }

    }

    def test(){
        log.debug(helperService.addDataResourceForList("My test List"))
        log.debug(authService.email())
        //bieService.bulkLookupSpecies(["urn:lsid:biodiversity.org.au:afd.taxon:31a9b8b8-4e8f-4343-a15f-2ed24e0bf1ae"])
        log.debug(loggerService.getReasons())
    }

    def showList(){
        try{
            if (params.message)
                flash.message = params.message
            params.max = Math.min(params.max ? params.int('max') : 10, 100)
            params.sort = params.sort ?: "id"
            //force the SpeciesListItem to perform a join on the kvp table.
            //params.fetch = [kvpValues: 'join'] -- doesn't work for a 1 ro many query because it doesn't correctly obey the "max" param
            //params.remove("id")
            params.fetch= [ kvpValues: 'select' ]
            log.error(params.toQueryString())
            def distinctCount = SpeciesListItem.executeQuery("select count(distinct guid) from SpeciesListItem where dataResourceUid='"+params.id+"'").head()
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid='"+params.id+"'")
            def speciesListItems =  SpeciesListItem.findAllByDataResourceUid(params.id,params)
            log.debug("KEYS: " + keys)
            render(view:'/speciesListItem/list', model:[results: speciesListItems,
                        totalCount:SpeciesListItem.countByDataResourceUid(params.id),
                        noMatchCount:SpeciesListItem.countByDataResourceUidAndGuidIsNull(params.id),
                        distinctCount:distinctCount, keys:keys])
        }
        catch(Exception e){
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }
    /**
     * Downloads the field guid for this species list
     * @return
     */
    def fieldGuide(){
        if (params.id){
            def isdr = params.id.startsWith("dr")
//            def where = isdr? "dataResourceUid='"+params.id+"'":"id = " + params.id
//            def guids = SpeciesListItem.executeQuery("select guid from SpeciesListItem where guid is not null and " + where)
            def guids = getGuidsForList(params.id,grailsApplication.config.downloadLimit)
            def speciesList = isdr?SpeciesList.findByDataResourceUid(params.id):SpeciesList.get(params.id)
            if(speciesList){
                def url = bieService.generateFieldGuide(speciesList.getDataResourceUid(), guids)
                log.debug("THE URL:: " + url)
                if(url)
                    redirect(url:url)
                else
                    redirect(controller: "speciesListItem", action: "list", id:params.id)
            }
            else
                redirect(controller: "speciesListItem", action: "list", id:params.id)
        }
    }

    def getGuidsForList(id, limit){
        def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
        def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id)
        def isdr =id.startsWith("dr")
        //def where = isdr? "dataResourceUid=?":"id = ?"
        def guids = SpeciesListItem.executeQuery("select sli.guid  " + baseQueryAndParams[0] + " and sli.guid is not null", baseQueryAndParams[1] ,[max: limit])

        return guids
    }

    def getUnmatchedNamesForList(id, limit) {
        def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
        def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id)
        def isdr =id.startsWith("dr")
        //def where = isdr? "dataResourceUid=?":"id = ?"
        def names = SpeciesListItem.executeQuery("select sli.rawScientificName  " + baseQueryAndParams[0] + " and sli.guid is null", baseQueryAndParams[1] ,[max: limit])

        return names
    }

    /**
     * Either downloads records from biocache or redirects to bicache depending on the type
     * @return
     */
    def occurrences(){
        if (params.id && params.type){
            def guids = getGuidsForList(params.id, grailsApplication.config.downloadLimit)
            def unMatchedNames = getUnmatchedNamesForList(params.id, grailsApplication.config.downloadLimit)
            def splist = SpeciesList.findByDataResourceUid(params.id)
            def title = "Species List: " + splist.listName
            def downloadDto = new DownloadDto()
            bindData(downloadDto, params)

            log.debug "downloadDto = " + downloadDto
            log.debug "unMatchedNames = " + unMatchedNames
            def url = biocacheService.performBatchSearchOrDownload(guids, unMatchedNames, downloadDto, title, splist.wkt)

//            if (splist.wkt) {
//                url = biocacheService.performBatchSearchOrDownload(guids, unMatchedNames, downloadDto, title, splist.wkt)
//            } else {
//                url = biocacheService.performBatchSearchOrDownload(guids, params.type, title)
//            }

            if(url){
                redirect(url:url)
            } else {
                redirect(controller: "speciesListItem", action: "list", id:params.id)
            }
        }
    }
    /**
     * Performs an initial parse of the species list to provide feedback on values. Allowing
     * users to supply vocabs etc.
     */
    def parseData(){
        log.debug("Parsing for header")
        def rawData = request.getReader().readLines().join("\n").trim()
        String separator = helperService.getSeparator(rawData)
        CSVReader csvReader = helperService.getCSVReaderForText(rawData, separator)
        def rawHeader =  csvReader.readNext()
        log.debug(rawHeader.toList())
        def processedHeader = helperService.parseHeader(rawHeader)?:helperService.parseData(rawHeader)
        log.debug(processedHeader)
        def dataRows = new ArrayList<String[]>()
        def currentLine = csvReader.readNext()
        for(int i=0; i<noOfRowsToDisplay && currentLine!=null; i++){
            dataRows.add(currentLine)
            currentLine = csvReader.readNext()
        }
        if (processedHeader.find{it == "scientific name" || it == "vernacular name" ||  it == "common name" || it == "ambiguous name"} && processedHeader.size()>0){
            //grab all the unique values for the none scientific name fields to supply for potential vocabularies
            try{
                def listProperties = helperService.parseValues(processedHeader as String[],helperService.getCSVReaderForText(rawData, separator), separator)
                log.debug("names - " + listProperties)
                render(view: 'parsedData', model: [columnHeaders:processedHeader, dataRows:dataRows, listProperties:listProperties, listTypes:ListType.values()])
            }
            catch(Exception e){
                e.printStackTrace()
                log.debug(e.getMessage())
                render(view: 'parsedData',model:[error: e.getMessage()])
            }


        }
        else{
            render(view: 'parsedData', model: [columnHeaders:processedHeader, dataRows:dataRows, listTypes:ListType.values()])
        }
    }
    /**
     * Rematches the scientific names in the supplied list
     */
    def rematch(){
        log.debug("Rematching for " + params.id)
        if (params.id && !params.id.startsWith("dr"))
            params.id = SpeciesList.get(params.id)?.dataResourceUid
        def items = params.id?SpeciesListItem.findAllByDataResourceUid(params.id):SpeciesListItem.list()
        //now for each item returned peform
        log.debug("total items = "+items.size)
        items.eachWithIndex { it, i ->
            String rawName = it.rawScientificName
            String currentLsid = it.guid
            log.debug i + ". Rematching: " + rawName
            if(rawName && rawName.length()>0){
                //String newLsid =helperService.findAcceptedLsidByScientificName(rawName)?:helperService.findAcceptedLsidByCommonName(rawName)
                NameSearchResult nsr = helperService.findAcceptedConceptByScientificName(rawName)?:helperService.findAcceptedConceptByCommonName(rawName)

                //if(newLsid && !currentLsid.equals(newLsid)){
                if(nsr){
                    log.debug("rematching lsid for " + rawName + " current: " + currentLsid + " new : " + nsr.lsid)
                    log.debug("Checking NSR - lsid: " + nsr.lsid + " | id: " + nsr.id + " | value: " + nsr.toString()) + "||"
                    it.guid = nsr.lsid
                    it.matchedName = nsr.getRankClassification()?.scientificName

                    if (!it.save(flush: true)) {
                        log.error "Error saving item (" + rawName +"): " + it.errors()
                    }
                }
            }
            else{
                it.guid=null
                it.guid=null
                if (!it.save(flush: true)) {
                    log.error "Error saving item (" + rawName +"): " + it.errors()
                }
            }
        }
        render(text: "Rematch complete")
    }
}

