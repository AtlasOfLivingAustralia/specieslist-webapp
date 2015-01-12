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
import au.org.ala.web.AuthService
import grails.converters.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class SpeciesListController {

    public static final String CSV_UPLOAD_FILE_NAME = "csvFile"
    public static final String INVALID_FILE_TYPE_MESSAGE = "Invalid file type: must be a tab or comma separated text file."
    private static final String[] ACCEPTED_CONTENT_TYPES = ["text/plain", "text/csv"]

    HelperService helperService
    AuthService authService
    BieService bieService
    BiocacheService biocacheService
    LoggerService loggerService
    QueryService queryService

    int noOfRowsToDisplay = 5

    static allowedMethods = [uploadList:'POST']

    def index() { redirect(action: 'upload')}

    def upload(){ /*maps to the upload.gsp */
        log.debug(ListType.values())
        if(params.id){
            //get the list if it exists and ensure that the user is an admin or the owner
            def list = SpeciesList.findByDataResourceUid(params.id)
            //if(list?.username == authService.getEmail() || authService.userInRole("ROLE_ADMIN")){
            if(list?.userId == authService.getUserId() || authService.userInRole("ROLE_ADMIN")){
                render(view: "upload", model: [resourceUid:params.id, list:  list, listTypes:ListType.values()])
            } else {
                flash.message = "${message(code: 'error.message.reloadListPermission', args: [params.id])}"
                redirect(controller: "public", action:"speciesLists")
            }
        } else {
            render(view:"upload",model:  [listTypes:ListType.values()])
        }
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
            //TODO remove the items from the collectory
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

    def createOrRetrieveDataResourceUid(dataResourceUid, speciesListName){
        if(!dataResourceUid){
            def drURL = helperService.addDataResourceForList([name:speciesListName])
            log.debug(drURL)
            drURL.toString().substring(drURL.lastIndexOf('/') +1)
        } else {
            dataResourceUid
        }
    }

    def uploadList() {
        //the raw data and list name must exist
        log.debug("upload the list....")
        log.debug(params)

        def file = isMultipartRequest() ? request.getFile(CSV_UPLOAD_FILE_NAME) : null

        JSONObject formParams = file ? JSON.parse(request.getParameter("formParams")) : JSON.parse(request.getReader())
        log.debug(formParams.toString() + " class : " + formParams.getClass())

        if(formParams.speciesListName && formParams.headers && (file || formParams.rawData)) {

            def druid = createOrRetrieveDataResourceUid(
                    formParams.id,
                    formParams.speciesListName
            )

            if(druid) {
                log.debug("Loading species list " + formParams.speciesListName)
                def vocabs = formParams.findAll { it.key.startsWith("vocab") && it.value } //map of vocabs

                log.debug("Vocabs: " +vocabs)
                CSVReader reader

                try {
                    if (file) {
                        reader = helperService.getCSVReaderForCSVFileUpload(file, detectSeparator(file) as char)
                    } else {
                        reader = helperService.getCSVReaderForText(formParams.rawData, helperService.getSeparator(formParams.rawData))
                    }
                    def header = formParams.headers

                    log.debug("Header: " +header)
                    def itemCount = helperService.loadSpeciesListFromCSV(reader,
                            druid,
                            formParams.speciesListName,
                            ListType.valueOf(formParams.get("listType")),
                            formParams.description,
                            formParams.listUrl,
                            formParams.listWkt,
                            formParams.isBIE,
                            formParams.isSDS,
                            formParams.region,
                            formParams.authority,
                            formParams.category,
                            formParams.generalisation,
                            formParams.sdsType,
                            header.split(","),
                            vocabs)

                    def url = createLink(controller:'speciesListItem', action:'list', id: druid) +"?max=15"
                    //update the URL for the list
                    helperService.updateDataResourceForList(druid,
                            [
                             pubDescription: formParams.description,
                             websiteUrl: grailsApplication.config.serverName + url,
                             techDescription: "This list was first uploaded by " + authService.userDetails()?.userDisplayName + " on the " + (new Date()) + "." + "It contains " + itemCount + " taxa.",
                             resourceType : "species-list",
                             status : "dataAvailable",
                             contentTypes : '["species list"]'
                            ]
                    )

                    if (itemCount.successfulItems == itemCount.totalRecords) {
                        flash.message = "All items have been successfully uploaded."
                    }
                    else {
                        flash.message = "${itemCount.successfulItems} out of ${itemCount.totalRecords} items have been " +
                                "successfully uploaded."
                    }

                    def map = [url: url, error: itemCount.successfulItems > 0 ? null : "Unable to upload species data. " +
                            "Please ensure the column containing the species name has been identified."]
                    render map as JSON
                }
                finally {
                    reader?.close()
                }

            } else {
                response.outputStream.write("Unable to add species list at this time. If this problem persists please report it.".getBytes())
                response.setStatus(500)
                response.sendError(500, "Unable to add species list at this time. If this problem persists please report it.")
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
            helperService.loadSpeciesListFromFile(params.speciesListTitle,druid,localFilePath,params.containsKey('rowIndicatesMapping'),header,vocabMap)
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
        //list should be based on the user that is logged in so add the filter condition
        //def username = authService.getEmail()
        def userId = authService.getUserId()
        if (userId){
            //params['username'] = "eq:"+username
            params['userId'] = "eq:"+userId
        }

        try {
            def lists = queryService.getFilterListResult(params)

            //now remove the params that were added
            //params.remove('username')
            params.remove('userId')
            log.debug("lists:" + lists)

            render(view: "list", model: [lists:lists, total:lists.totalCount])
        } catch(Exception e) {
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def test(){
        log.debug(helperService.addDataResourceForList("My test List"))
        log.debug(authService.getEmail())
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
        SpeciesListItem.executeQuery("select sli.guid  " + baseQueryAndParams[0] + " and sli.guid is not null", baseQueryAndParams[1] ,[max: limit])
    }

    def getUnmatchedNamesForList(id, limit) {
        def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
        def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ",fqs, params.id)
        def isdr =id.startsWith("dr")
        //def where = isdr? "dataResourceUid=?":"id = ?"
        def names = SpeciesListItem.executeQuery("select sli.rawScientificName  " + baseQueryAndParams[0] + " and sli.guid is null", baseQueryAndParams[1] ,[max: limit])
        return names
    }

    def spatialPortal(){
        if (params.id && params.type){
            def guids = getGuidsForList(params.id, grailsApplication.config.downloadLimit)
            def unMatchedNames = getUnmatchedNamesForList(params.id, grailsApplication.config.downloadLimit)
            def splist = SpeciesList.findByDataResourceUid(params.id)
            def title = "Species List: " + splist.listName
            log.debug "unMatchedNames = " + unMatchedNames
            def qid = biocacheService.getQid(guids, unMatchedNames, title, splist.wkt)
            if(qid.status == 200){
                redirect(url:"http://spatial.ala.org.au/?q=qid:"+qid.result)
            }
        }
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
     *
     * Data can be submitted either via a file upload or as copy and paste text
     */
    def parseData() {
        log.debug("Parsing for header")

        String separator
        CSVReader csvReader
        CommonsMultipartFile file = isMultipartRequest() ? request.getFile(CSV_UPLOAD_FILE_NAME) : null
        try {
            if (file) {
                if (ACCEPTED_CONTENT_TYPES.contains(file.getContentType())) {
                    separator = detectSeparator(file);
                    csvReader = helperService.getCSVReaderForCSVFileUpload(file, separator as char)
                } else {
                    render(view: 'parsedData', model: [error: INVALID_FILE_TYPE_MESSAGE])
                    return
                }
            } else {
                def rawData = request.getReader().readLines().join("\n").trim()
                separator = helperService.getSeparator(rawData)
                csvReader = helperService.getCSVReaderForText(rawData, separator)
            }

            parseDataFromCSV(csvReader, separator)
        }
        catch (e) {
            log.error("Failed to parse data", e)
            render(view: 'parsedData', model: [error: "Unable to parse species list data: ${e.getMessage()}"])
        }
        finally {
            csvReader?.close()
        }
    }

    private String detectSeparator(CommonsMultipartFile file) {
        file.getInputStream().withReader { r -> helperService.getSeparator(r.readLine()) }
    }

    private parseDataFromCSV(CSVReader csvReader, String separator) {
        def rawHeader = csvReader.readNext()
        log.debug(rawHeader.toList())
        def parsedHeader = helperService.parseHeader(rawHeader) ?: helperService.parseData(rawHeader)
        def processedHeader = parsedHeader.header
        log.debug(processedHeader)
        def dataRows = new ArrayList<String[]>()
        def currentLine = csvReader.readNext()
        for (int i = 0; i < noOfRowsToDisplay && currentLine != null; i++) {
            dataRows.add(currentLine)
            currentLine = csvReader.readNext()
        }
        if (processedHeader.find {
            it == "scientific name" || it == "vernacular name" || it == "common name" || it == "ambiguous name"
        } && processedHeader.size() > 0) {
            //grab all the unique values for the none scientific name fields to supply for potential vocabularies
            try {
                def listProperties = helperService.parseValues(processedHeader as String[], csvReader, separator)
                log.debug("names - " + listProperties)
                render(view: 'parsedData', model: [columnHeaders: processedHeader, dataRows: dataRows, listProperties: listProperties, listTypes: ListType.values()])
            } catch (Exception e) {
                log.debug(e.getMessage())
                render(view: 'parsedData', model: [error: e.getMessage()])
            }
        } else {
            render(view: 'parsedData', model: [columnHeaders: processedHeader, dataRows: dataRows, listTypes: ListType
                    .values(), nameFound: parsedHeader.nameFound])
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
            log.debug i + ". Rematching: " + rawName
            if(rawName && rawName.length()>0){
                helperService.matchNameToSpeciesListItem(rawName, it)
                if (!it.save(flush: true)) {
                        log.error "Error saving item (" + rawName +"): " + it.errors()
                }
            } else {
                it.guid = null
                if (!it.save(flush: true)) {
                    log.error "Error saving item (" + rawName +"): " + it.errors()
                }
            }
        }
        render(text: "Rematch complete")
    }

    private boolean isMultipartRequest() {
        request instanceof MultipartHttpServletRequest
    }
}

