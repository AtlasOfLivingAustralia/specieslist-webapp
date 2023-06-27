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

import au.org.ala.web.AuthService
import au.org.ala.names.ws.api.SearchStyle
import com.opencsv.CSVReader
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.apache.commons.io.filefilter.FalseFileFilter
import org.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartHttpServletRequest

import javax.annotation.PostConstruct

class SpeciesListController {

    public static final String CSV_UPLOAD_FILE_NAME = "csvFile"
    public static final String INVALID_FILE_TYPE_MESSAGE = "Invalid file type: must be a tab or comma separated text file."
    private static final String[] ACCEPTED_CONTENT_TYPES = ["text/plain", "text/csv"]

    HelperService helperService
    ColumnMatchingService columnMatchingService
    AuthService authService
    LocalAuthService localAuthService
    BieService bieService
    BiocacheService biocacheService
    LoggerService loggerService
    QueryService queryService

    int noOfRowsToDisplay = 5

    Integer BATCH_SIZE

    @PostConstruct
    init(){
        BATCH_SIZE = Integer.parseInt((grailsApplication.config.batchSize?:200).toString())
    }

    static allowedMethods = [uploadList:'POST']

    def index() { redirect(action: 'upload')}

    def upload() { /*maps to the upload.gsp */
        log.debug(ListType.values()?.toString())
        if (params.id) {
            //get the list if it exists and ensure that the user is an admin or the owner
            def list = SpeciesList.findByDataResourceUid(params.id)
            if (list?.userId == authService.getUserId() || authService.userInRole("ROLE_ADMIN")) {
                render(view: "upload", model: [resourceUid: params.id, list: list, listTypes: ListType.values()])
            } else {
                flash.message = "${message(code: 'error.message.reloadListPermission', args: [params.id])}"
                redirect(controller: "public", action: "speciesLists")
            }
        } else {
            // list is private by default
            def list = new SpeciesList(isPrivate: true)
            render(view: "upload", model: [list: list, listTypes: ListType.values()])
        }
    }

    /**
     * Current mechanism for deleting a species list
     * @return
     *
     * Edit Access controlled by SpeciesListDeleteInterceptor
     */
    @Transactional
    def delete(){
        log.debug("Deleting from collectory...")
        def sl = SpeciesList.get(params.id)
        if(sl){
            helperService.deleteDataResourceForList(sl.dataResourceUid)
            sl.delete(flush: true)
        }
        redirect(action: 'list')
    }

    /**
     * OLD delete
     *
     * @return
     *
     * Permissions controlled by SpeciesListDeleteInterceptor
     */
    def deleteList(){
        log.debug("Deleting from collectory...")
        helperService.deleteDataResourceForList(params.id)

        //delete all the items that belong to the specified list
        //SpeciesListItem.where {dataResourceUid == params.id}.deleteAll()
        log.debug(params?.toString())
        //performs the cascade delete that is required.
        SpeciesListItem.findAllByDataResourceUid(params.id)*.delete()
        SpeciesListKVP.findAllByDataResourceUid(params.id)*.delete()

        flash.message = "Deleted Species List " + params.id
        redirect(action:  'upload')
    }

    private def createOrRetrieveDataResourceUid(dataResourceUid, speciesListName){
        if(!dataResourceUid){
            def drURL = helperService.addDataResourceForList([name:speciesListName])
            log.debug(drURL?.toString())
            if(!drURL){
                log.error("Unable to create entry in collectory....")
                throw new Exception("Problem communicating with collectory. Unable to create resource.")
            } else {
                drURL.toString().substring(drURL.lastIndexOf('/') +1)
            }
        } else {
            dataResourceUid
        }
    }

    def uploadList() {
        if (!authService.userId) {
            response.sendError(401, "Not logged in.")
            return
        }

        //the raw data and list name must exist
        log.debug("upload the list....")
        log.debug(params?.toString())

        def file = isMultipartRequest() ? request.getFile(CSV_UPLOAD_FILE_NAME) : null

        JSONObject formParams = file ? JSON.parse(request.getParameter("formParams")) : JSON.parse(request.getReader())
        log.debug(formParams.toString() + " class : " + formParams.getClass())

        if(formParams.speciesListName && formParams.headers && (file || formParams.rawData)) {

            // check access for existing list
            def speciesList = SpeciesList.findByDataResourceUid(formParams.id)
            if (speciesList && !isCurrentUserEditorForList(speciesList)) {
                response.sendError(401, "Not authorised.")
                return
            }

            def druid = createOrRetrieveDataResourceUid(
                    formParams.id,
                    formParams.speciesListName
            )

            // default all lists to isAuthoritative = false: it is an admin task to determine whether a list is authoritative or not
            if (!request.isUserInRole("ROLE_ADMIN")) {
                def list = SpeciesList.findByDataResourceUid(params.id)
                formParams.isAuthoritative = list?.isAuthoritative || Boolean.FALSE
                formParams.isThreatened = list?.isAuthoritative || Boolean.FALSE
                formParams.isInvasive = list?.isAuthoritative || Boolean.FALSE
            }

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
                            formParams.isAuthoritative,
                            formParams.isThreatened,
                            formParams.isInvasive,
                            formParams.isPrivate,
                            formParams.region,
                            formParams.authority,
                            formParams.category,
                            formParams.generalisation,
                            formParams.sdsType,
                            formParams.looseSearch== null || formParams.looseSearch.isEmpty() ? null : Boolean.parseBoolean(formParams.looseSearch),
                            formParams.searchStyle == null || formParams.searchStyle.isEmpty() ? null : SearchStyle.valueOf(formParams.searchStyle),
                            header.split(","),
                            vocabs)

                    def url = createLink(controller:'speciesListItem', action:'list', id: druid) +"?max=10"
                    //update the URL for the list
                    helperService.updateDataResourceForList(druid,
                        [
                             pubDescription: formParams.description,
                             websiteUrl: grailsApplication.config.serverName + url,
                             techDescription: "This list was first uploaded by " + authService.userDetails()?.getDisplayName() + " on the " + (new Date()) + "." + "It contains " + itemCount + " taxa.",
                             resourceType : "species-list",
                             status : "dataAvailable",
                             contentTypes : '["species list"]'
                        ]
                    )

                    if (itemCount.successfulItems == itemCount.totalRecords) {
                        flash.message = "${message(code: 'upload.lists.uploadprocess.success.message', default:'All items have been successfully uploaded.')}"
                    }
                    else {
                        flash.message = "${itemCount.successfulItems} ${message(code:'upload.lists.uploadprocess.partial01', default:'out of')} ${itemCount.totalRecords} ${message(code:'upload.lists.uploadprocess.partial02', default:'items have been successfully uploaded.')}"
                    }

                    def msg = message(code:'upload.lists.uploadprocess.errormessage', default:'Unable to upload species data. Please ensure the column containing the species name has been identified.')
                    def map = [url: url, error: itemCount.successfulItems > 0 ? null : msg]
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
        if (!authService.userId) {
            response.sendError(401, "Not logged in.")
            return
        }

        //ensure that the species list file exists before creating anything
        def uploadedFile = request.getFile('species_list')
        if(!uploadedFile.empty){
            //create a new temp data resource in the collectory for this species list
            def drURL = helperService.addDataResourceForList(params.speciesListTitle)
            def druid = drURL.toString().substring(drURL.lastIndexOf('/') +1)
            //download the supplied file to the local files system
            def localFilePath = helperService.uploadFile(druid, uploadedFile)
            log.debug("The local file " + localFilePath)
            //create a new species list item for each line of the file
            log.debug(params?.toString())
            //set of the columns if necessary
            def columns = params.findAll { it.key.startsWith("column") && it.value }.sort{it.key.replaceAll("column","") as int }
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
            log.debug(vocabMap?.toString())
            helperService.loadSpeciesListFromFile(params.speciesListTitle,druid,localFilePath,params.containsKey('rowIndicatesMapping'),header,vocabMap)
            //redirect the use to a summary of the records that they submitted - include links to BIE species page
            //also mention that the list will be loaded into BIE overnight.
            //def speciesListItems =  au.org.ala.specieslist.SpeciesListItem.findAllByDataResourceUid(druid)
            flash.params
            //[max:10, sort:"title", order:"desc", offset:100]
            //render(view:'list', model:[results: speciesListItems])
            redirect(controller: "speciesListItem",action: "list",id: druid,params: [max: 10, sort:"id"])//,id: druid, max: 10, sort:"id")
        }
    }

    def list(){
        //list should be based on the user that is logged in so add the filter condition
        def userId = authService.getUserId()
        if (userId){
            params['userId'] = "eq:"+userId
        }
        String searchTerm = null
        params.q = params.q?.trim()
        if (params.q && params.q.length() < 3) {
            searchTerm = params.q
            params.q = ""
        }

        try {
            // retrieve qualified SpeciesListItems for performance reason
            def itemsIds = queryService.getFilterSpeciesListItemsIds(params)

            def lists = queryService.getFilterListResult(params, true, itemsIds)
            def typeFacets = (params.listType) ? null : queryService.getTypeFacetCounts(params, true, itemsIds)
            def tagFacets = queryService.getTagFacetCounts(params, itemsIds)
            //now remove the params that were added
            //params.remove('username')
            params.remove('userId')
            log.debug("lists:" + lists)

            def model = [
                    lists: lists,
                    total: lists.totalCount,
                    typeFacets: typeFacets,
                    tagFacets : tagFacets,
                    selectedFacets: queryService.getSelectedFacets(params)]
            if (searchTerm) {
                params.q = searchTerm
                model.errors = "Error: Search terms must contain at least 3 characters"
            }
            render(view: "list", model: model)

        } catch(Exception e) {
            log.error "Error requesting species Lists: " ,e
            response.status = 404
            render(view: '../error', model: [message: "Unable to retrieve species lists. Please let us know if this error persists. <br>Error:<br>" + e.getMessage()])
        }
    }

    def showList(){
        def speciesList = SpeciesList.findByDataResourceUid(params.id)
        if (speciesList && !isViewable(speciesList)) {
            response.sendError(401, "Not authorised.")
            return
        }

        try{
            if (params.message)
                flash.message = params.message
            params.max = Math.min(params.max ? params.int('max') : 10, 100)
            params.sort = params.sort ?: "id"
            //force the SpeciesListItem to perform a join on the kvp table.
            //params.fetch = [kvpValues: 'join'] -- doesn't work for a 1 ro many query because it doesn't correctly obey the "max" param
            //params.remove("id")
            params.fetch= [ kvpValues: 'select' ]
            log.error(params.toQueryString()?.toString())
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

            if (speciesList && !isViewable(speciesList)) {
                response.sendError(401, "Not authorised.")
                return
            }

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

    private def getGuidsForList(id, limit){
        def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
        def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ", fqs, params.id)
        SpeciesListItem.executeQuery("select sli.guid  " + baseQueryAndParams[0] + " and sli.guid is not null", baseQueryAndParams[1] ,[max: limit])
    }

    def getUnmatchedNamesForList(id, limit) {
        def fqs = params.fq?[params.fq].flatten().findAll{ it != null }:null
        def baseQueryAndParams = queryService.constructWithFacets(" from SpeciesListItem sli ", fqs, params.id)
        def isdr =id.startsWith("dr")
        //def where = isdr? "dataResourceUid=?":"id = ?"
        def names = SpeciesListItem.executeQuery("select sli.rawScientificName  " + baseQueryAndParams[0] + " and sli.guid is null", baseQueryAndParams[1] ,[max: limit])
        return names
    }

    def spatialPortal(){
        if (params.id && params.type){
            //if the list is authoritative, then just do ?q=species_list_uid:
            SpeciesList splist = SpeciesList.findByDataResourceUid(params.id)
            if (splist && !isViewable(splist)) {
                response.sendError(401, "Not authorised.")
                return
            }

            if(grailsApplication.config.biocache.indexAuthoritative.toBoolean() && splist.isAuthoritative){
                redirect(url: grailsApplication.config.spatial.baseURL + "/?q=species_list_uid:" + splist.dataResourceUid)
            } else {
                def guids = getGuidsForList(params.id, grailsApplication.config.downloadLimit)
                def unMatchedNames = getUnmatchedNamesForList(params.id, grailsApplication.config.downloadLimit)
                def title = "Species List: " + splist.listName
                log.debug "unMatchedNames = " + unMatchedNames
                def qid = biocacheService.getQid(guids, unMatchedNames, title, splist.wkt)
                if(qid?.status == 200){
                    redirect(url: grailsApplication.config.spatial.baseURL + "/?q=qid:"+qid.result)
                } else {
                    render(view: '../error', model: [message: "Unable to view occurrences records. Please let us know if this error persists."])
                }
            }
        }
    }

    /**
     * Either downloads records from biocache or redirects to bicache depending on the type
     * @return
     */
    def occurrences(){

        if(biocacheService.isListIndexed(params.id)){
            redirect(url:biocacheService.getQueryUrlForList(params.id))
        } else if (params.id && params.type){
            def splist = SpeciesList.findByDataResourceUid(params.id)
            if (splist && !isViewable(splist)) {
                response.sendError(401, "Not authorised.")
                return
            }

            def guids = getGuidsForList(params.id, grailsApplication.config.downloadLimit)
            def unMatchedNames = getUnmatchedNamesForList(params.id, grailsApplication.config.downloadLimit)
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
        def file = isMultipartRequest() ? request.getFile(CSV_UPLOAD_FILE_NAME) : null
        try {
            if (file) {
                if (ACCEPTED_CONTENT_TYPES.contains(file.getContentType())) {
                    separator = detectSeparator(file);
                    csvReader = helperService.getCSVReaderForCSVFileUpload(file, separator as char)
                } else {
                    log.debug("Wrong File Content type: "+file.getContentType())
                    render(view: 'parsedData', model: [error: "${message(code:'upload.lists.checkdata.errormessage', default:'Invalid file type: must be a tab or comma separated text file.')}"])
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

    private String detectSeparator(file) {
        file?.getInputStream().withReader { r -> helperService.getSeparator(r.readLine()) }
    }

    /**
     * Rematches the scientific names in the supplied list
     */
    @Transactional
    def rematch() {
        if (params.id && !params.id.startsWith("dr")) {
            params.id = SpeciesList.get(params.id)?.dataResourceUid
            log.info("Rematching for " + params.id)
        } else {
            log.error("Rematching for ALL")
        }
        Integer totalRows, offset = 0;
        String id = params.id
        def splist = SpeciesList.findByDataResourceUid(params.id)
        if (splist && !isCurrentUserEditorForList(splist)) {
            response.sendError(401, "Not authorised.")
            return
        }

        if (id) {
            totalRows = SpeciesListItem.countByDataResourceUid(id)
        } else {
            totalRows = SpeciesListItem.count();
        }

        while (offset < totalRows) {
            List items
            List guidBatch = [], sliBatch = []
            Map<SpeciesList, List<SpeciesListItem>> batches = new HashMap<>()
            List<SpeciesListItem> searchBatch = new ArrayList<SpeciesListItem>()
            if (id) {
                items = SpeciesListItem.findAllByDataResourceUid(id, [max: BATCH_SIZE, offset: offset])
            } else {
                items = SpeciesListItem.list(max: BATCH_SIZE, offset: offset)
            }

            SpeciesListItem.withSession { session ->
                items.eachWithIndex { SpeciesListItem item, Integer i ->
                    SpeciesList speciesList = item.mylist
                    List<SpeciesListItem> batch = batches.get(speciesList)
                    if (batch == null) {
                        batch = new ArrayList<>();
                        batches.put(speciesList, batch)
                    }
                    String rawName = item.rawScientificName
                    log.debug i + ". Rematching: " + rawName + "/" + speciesList.dataResourceUid
                    if (rawName && rawName.length() > 0) {
                        batch.add(item)
                    } else {
                        item.guid = null
                        if (!item.save(flush: true)) {
                            log.error "Error saving item (" + rawName + "): " + item.errors()
                        }
                    }
                }
                batches.each { list, batch ->
                    helperService.matchAll(batch, list)
                    batch.each {SpeciesListItem item ->
                        if (item.guid) {
                            guidBatch.push(item.guid)
                            sliBatch.push(item)
                        }
                    }
                }

                if (!guidBatch.isEmpty()) {
                    helperService.getCommonNamesAndUpdateRecords(sliBatch, guidBatch)
                }

                session.flush()
                session.clear()
            }

            offset += BATCH_SIZE;
            log.info("Rematched ${offset} of ${totalRows} - ${Math.round(offset * 100 / totalRows)}% complete")
            if (offset > totalRows) {
                log.error("Rematched ${offset} of ${totalRows} - ${Math.round(offset * 100 / totalRows)}% complete")
            }
        }

        render(text: "${message(code: 'admin.lists.page.button.rematch.messages', default: 'Rematch complete')}")
    }

    private parseDataFromCSV(CSVReader csvReader, String separator) {
        def rawHeader = csvReader.readNext()
        log.debug(rawHeader.toList()?.toString())
        def parsedHeader = columnMatchingService.parseHeader(rawHeader) ?: helperService.parseData(rawHeader)
        def processedHeader = parsedHeader.header
        log.debug(processedHeader?.toString())
        def dataRows = new ArrayList<String[]>()
        def currentLine = csvReader.readNext()
        for (int i = 0; i < noOfRowsToDisplay && currentLine != null; i++) {
            dataRows.add(helperService.parseRow(currentLine.toList()))
            currentLine = csvReader.readNext()
        }
        def nameColumns = columnMatchingService.speciesNameMatcher.names + columnMatchingService.commonNameMatcher.names
        if (processedHeader.find {
            it == "scientific name" || it == "vernacular name" || it == "common name" || it == "ambiguous name"
        } && processedHeader.size() > 0) {
            //grab all the unique values for the none scientific name fields to supply for potential vocabularies
            try {
                def listProperties = helperService.parseValues(processedHeader as String[], csvReader, separator)
                log.debug("names - " + listProperties)
                render(view: 'parsedData', model: [columnHeaders: processedHeader, dataRows: dataRows,
                                                   listProperties: listProperties, listTypes: ListType.values(),
                                                   nameFound: parsedHeader.nameFound, nameColumns: nameColumns])
            } catch (Exception e) {
                log.debug(e.getMessage())
                render(view: 'parsedData', model: [error: e.getMessage()])
            }
        } else {
            render(view: 'parsedData', model: [columnHeaders: processedHeader, dataRows: dataRows,
                                               listTypes: ListType.values(), nameFound: parsedHeader.nameFound,
                                               nameColumns: nameColumns])
        }
    }

    private boolean isMultipartRequest() {
        request instanceof MultipartHttpServletRequest
    }

    /**
     * Check if user is either owner, admin or on the specieslist's editors list.
     */
    private boolean isCurrentUserEditorForList(SpeciesList sl) {
        def isAllowed = false
        def loggedInUser = authService.userId
        log.debug "Checking isCurrentUserEditorForList: loggedInUser = " + loggedInUser
        if (!sl) {
            log.debug "speciesList is null"
            isAllowed = false // saves repeating this check in subsequent else if
        } else if (sl.userId == loggedInUser) {
            log.debug "user is owner"
            isAllowed = true
        } else if (localAuthService.isAdmin()) {
            log.debug "user is ADMIN"
            isAllowed = true
        } else if (sl.editors.any { it == loggedInUser}) {
            log.debug "user is in editors list: " + sl.editors.join("|")
            isAllowed = true
        }

        log.debug "isAllowed = " + isAllowed
        return isAllowed
    }

    /**
     * Check if list is public OR private and user is either owner, admin or on the specieslist's editors list.
     */
    private boolean isViewable(SpeciesList sl) {
        def isAllowed = false
        def hidePrivateLists = grailsApplication.config.getProperty('publicview.hidePrivateLists', Boolean, false)
        def loggedInUser = authService.userId
        log.debug "Checking isCurrentUserEditorForList: loggedInUser = " + loggedInUser
        if (!sl) {
            log.debug "speciesList is null"
            isAllowed = false // saves repeating this check in subsequent else if
        } else if (!sl.isPrivate || !hidePrivateLists) {
            isAllowed = true
        } else if (sl.userId == loggedInUser) {
            log.debug "user is owner"
            isAllowed = true
        } else if (localAuthService.isAdmin()) {
            log.debug "user is ADMIN"
            isAllowed = true
        } else if (sl.editors.any { it == loggedInUser}) {
            log.debug "user is in editors list: " + sl.editors.join("|")
            isAllowed = true
        }

        log.debug "isAllowed = " + isAllowed
        return isAllowed
    }
}

