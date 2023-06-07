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

import au.org.ala.names.ws.api.NameUsageMatch
import au.org.ala.names.ws.api.SearchStyle
import com.opencsv.CSVReader
import grails.gorm.transactions.Transactional
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.io.input.BOMInputStream
import org.apache.commons.lang.StringUtils
import org.grails.web.json.JSONArray
import org.nibor.autolink.*
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import javax.annotation.PostConstruct

/**
 * Provides all the services for the species list webapp.  It may be necessary to break this into
 * multiple services if it grows too large
 */
@Transactional
class HelperService {

    MessageSource messageSource

    def grailsApplication

    def localAuthService, authService, userDetailsService

    BieService bieService

    NameExplorerService nameExplorerService

    ColumnMatchingService columnMatchingService

    Integer BATCH_SIZE

    // Only permit URLs for added safety
    private final LinkExtractor extractor = LinkExtractor.builder().linkTypes(EnumSet.of(LinkType.URL)).build()

    @PostConstruct
    init(){
        BATCH_SIZE = Integer.parseInt((grailsApplication?.config?.batchSize?:200).toString())
     }

    /**
     * Adds a data resource to the collectory for this species list
     * @param username
     * @param description
     * @return
     */
    def addDataResourceForList(map) {
        if(grailsApplication.config.getProperty('collectory.enableSync', Boolean, false)){
            def postUrl = grailsApplication.config.collectory.baseURL + "/ws/dataResource"
            def http = new HTTPBuilder(postUrl)
            http.setHeaders([Authorization: "${grailsApplication.config.registryApiKey}"])
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            Map jsonBody = createJsonForNewDataResource(map)
            log.debug(jsonBody?.toString())
            String newDataResource = null
            try {
               http.request(Method.POST) {
                    uri.path = postUrl
                    body = jsonBody
                    requestContentType = ContentType.JSON
                    response.success = { resp ->
                        log.info("Created a collectory entry for the species list.  ${resp.status}")
                        newDataResource = resp.headers['location'].getValue()
                    }
                    response.failure = { resp ->
                        log.error("Unable to create a collectory entry for the species list.  ${resp.status}")
                    }
                }

            } catch (ex){
                log.error("Unable to create a collectory entry for the species list. ", ex)
            }

            newDataResource

        } else {
           //return a dummy URL
          grailsApplication.config.collectory.baseURL + "/tmp/drt" + System.currentTimeMillis()
        }
    }

    def deleteDataResourceForList(drId) {
        if(grailsApplication.config.getProperty('collectory.enableSync', Boolean, false)){
            def deleteUrl = grailsApplication.config.collectory.baseURL +"/ws/dataResource/" + drId
            def http = new HTTPBuilder(deleteUrl)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            try {

                http.request(Method.DELETE) {
                    requestContentType = ContentType.JSON
                    headers."Authorization" = "${grailsApplication.config.registryApiKey}"
                    response.success = { resp ->
                        log.info(resp?.toString())
                    }
                    response.failure = { resp ->
                        log.error("Delete request for ${drId} failed with status ${resp.status}")
                    }
                }
            } catch (ex){
                log.error("Unable to delete a collectory entry for the species list.", ex)
            }
        }
    }

    def updateDataResourceForList(drId, map) {
        if (grailsApplication.config.getProperty('collectory.enableSync', Boolean, false)){
            def postUrl = grailsApplication.config.collectory.baseURL + "/ws/dataResource/" + drId
            def http = new HTTPBuilder(postUrl)
            http.setHeaders([Authorization: "${grailsApplication.config.registryApiKey}"])
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            def jsonBody = createJsonForNewDataResource(map)
            log.debug(jsonBody?.toString())
            try {
               http.request(Method.POST) {
                    uri.path = postUrl
                    body = jsonBody
                    requestContentType = ContentType.JSON
                    response.success = { resp ->
                        log.info("Updated the collectory entry for the species list ${drId}.  ${resp.status}")
                    }
                    response.failure = { resp ->
                        log.error("Unable to update the collectory entry for the species list ${drId}.  ${resp.status}")
                    }
               }
            } catch(ex) {
                log.error("Unable to update a collectory entry for the species list.",ex)
            }
        } else {
           //return a dummy URL
          grailsApplication.config.collectory.baseURL + "/tmp/drt" + System.currentTimeMillis()
        }
    }

    Map createJsonForNewDataResource(map){
        map.api_key = grailsApplication.config.registryApiKey
        map.resourceType = "species-list"
        map.user = 'Species list upload'
        map.firstName = localAuthService.firstname()?:""
        map.lastName = localAuthService.surname()?:""
        map
    }

    def uploadFile(druid, uploadedFile){
        if(druid){
            def destDir = new File(grailsApplication.config.bie.download + File.separator + druid + File.separator)
            destDir.mkdirs()
            def destFile = new File(destDir, "species_list.csv")
            uploadedFile.transferTo(destFile)
            destFile.absolutePath
        }
    }

    def getCSVReaderForText(String raw, String separator) {
        new CSVReader(new StringReader(raw), separator.charAt(0))
    }

    def getCSVReaderForCSVFileUpload(file, char separator) {
        new CSVReader(new InputStreamReader(new BOMInputStream(file.getInputStream())), separator)
    }

    def getSeparator(String raw) {
        String firstLine = raw.indexOf("\n") > 0 ? raw.substring(0, raw.indexOf("\n")) : raw

        int tabs = firstLine.count("\t")
        int commas = firstLine.count(",")

        tabs > commas ? '\t' : ','
    }

    def parseValues(String[] processedHeader, CSVReader reader, String sep)throws Exception{
        def sciIdx = indexOfName(processedHeader)
        if(sciIdx>=0){
            //now lets determine the possible values
            String[] nextLine
            Map map =[:]
            while ((nextLine = reader.readNext()) != null) {
                  nextLine.eachWithIndex {v,i ->
                      if(i != sciIdx){
                          if(i>=processedHeader.length)
                              throw new Exception("Row length does NOT match header length. Problematic row is " + nextLine.join(sep))
                          def set =map.get(processedHeader[i], [] as Set)
                          if(v != processedHeader[i])
                            set.add(v)
                      }
                  }
            }
            return map;
        } else {
            null
        }
    }

    def indexOfName(String[] processedHeader){
        processedHeader.findIndexOf {it == "scientific name" || it == "vernacular name" || it == "ambiguous name"}
    }

    /**
     * determines what the header should be based on the data supplied
     * @param header
     */
    def parseData(String[] header){
        def hasName = false
        def unknowni =1
        def headerResponse = header.collect{
            if(findAcceptedLsidByScientificName(it)){
                hasName = true
                "scientific name"
            } else if(findAcceptedLsidByCommonName(it)){
                hasName = true
                "vernacular name"
            } else {
                "UNKNOWN" + (unknowni++)
            }
        }
        [header: headerResponse, nameFound: hasName]
    }

    def parseRow(List row) {
        def ret = []

        String item
        row.each {String it ->
            item = parseUrls(it)

            ret << item
        }

        ret
    }

    private String parseUrls(String item) {
        String ret = null

        Iterable<LinkSpan> links = extractor.extractLinks(item)
        if (links) {
            ret = Autolink.renderLinks(item, links, {LinkSpan ls, CharSequence text, StringBuilder sb ->
                sb.append("<a href=\"")
                sb.append(text, ls.beginIndex, ls.endIndex);
                sb.append("\">")
                sb.append(text, ls.beginIndex, ls.endIndex)
                sb.append("</a>")
            } as LinkRenderer)
        }
        else {
            ret = item
        }

        ret
    }

    private boolean hasValidData(Map map, String [] nextLine) {
        boolean result = false
        map.each { key, value ->
            if (StringUtils.isNotBlank(nextLine[value])){
                result =  true
            }
        }
        result
    }

    def vocabPattern = ~ / ?([A-Za-z0-9]*): ?([A-Z a-z0-9']*)(?:,|$)/

    //Adds the associated vocabulary
    def addVocab(druid, vocab, kvpmap){
        if(vocab){
            vocab.each{
                //parse the values of format <key1>: <vocabValue1>,<key2>: <vocab2>
                def matcher =vocabPattern.matcher(it.value)
                //pattern match based on the the groups first item is the complete match
                matcher.each{match, value, vocabValue ->
                    def key = it.key.replaceFirst("vocab_","")
                    kvpmap.put(key+"|"+value, new SpeciesListKVP(key: key, value: value, dataResourceUid: druid, vocabValue: vocabValue))
                }
            }
        }
    }

    def loadSpeciesListFromJSON(Map json, String druid, boolean replace = true) {
        SpeciesList speciesList = SpeciesList.findByDataResourceUid(druid) ?: new SpeciesList(json)

        if (replace) {
            // updating an existing list
            if (speciesList.dataResourceUid) {
                // assume new list of species will replace existing one (no updates allowed for now)
                speciesList.items?.clear()

                // update the list of editors (comma separated list of email addresses)
                if (json?.editors) {
                    // merge lists and remove duplicates
                    speciesList.editors = (speciesList.editors + json.editors.tokenize(',')).unique()
                }
                if (json?.listName) {
                    speciesList.listName = json.listName // always update the list name
                }
            } else {
                // create a new list
                speciesList.setDataResourceUid(druid)
            }

            if (speciesList.username && !speciesList.userId) {
                def currentUser = userDetailsService.getCurrentUserDetails()
                if(currentUser){
                    log.warn("Current user found. Updating species list user details")
                    speciesList.userId = currentUser.getUserId()
                    speciesList.firstName = currentUser.getFirstName()
                    speciesList.surname  = currentUser.getLastName()
                }


            }
        }

        List guidList = []
        // version 1 of this operation supports list items as a comma-separated string
        // version 2 of this operation supports list items as structured JSON elements with KVPs
        if (isSpeciesListJsonVersion1(json)) {
            guidList = loadSpeciesListItemsFromJsonV1(json, speciesList, druid)
        } else if (isSpeciesListJsonVersion2(json)) {
            guidList = loadSpeciesListItemsFromJsonV2(json, speciesList, druid)
        } else {
            throw new UnsupportedOperationException("Unsupported data structure")
        }
        speciesList.lastUploaded = new Date()

        if (!speciesList.validate()) {
            log.error(speciesList.errors.allErrors?.toString())
        }

        List sli = speciesList.getItems().toList()
        matchCommonNamesForSpeciesListItems(sli)
        speciesList.save(flush: true, failOnError: true)

        [speciesList: speciesList, speciesGuids: guidList]
    }

    private static boolean isSpeciesListJsonVersion1(Map json) {
        // version 1 of this operation supports list items as a comma-separated string
        json.listItems in String
    }

    private loadSpeciesListItemsFromJsonV1(Map json, SpeciesList speciesList, String druid) {
        assert json.listItems, "Cannot create a Species List with no items"

        List items = json.listItems.split(",")

        List guidList = []
        items.eachWithIndex { item, i ->
            SpeciesListItem sli = new SpeciesListItem(dataResourceUid: druid, rawScientificName: item, itemOrder: i)
            matchNameToSpeciesListItem(sli.rawScientificName, sli, speciesList)
            speciesList.addToItems(sli)
            guidList.push (sli.guid)
        }
        guidList
    }

    private static boolean isSpeciesListJsonVersion2(Map json) {
        // version 2 of this operation supports list items as structured JSON elements with KVPs - i.e. a JSON Array
        json.listItems in JSONArray
    }

    private loadSpeciesListItemsFromJsonV2(Map json, SpeciesList speciesList, String druid) {
        assert json.listItems, "Cannot create a Species List with no items"

        List speciesGuidKvp = []
        Map kvpMap = [:]
        List items = json.listItems
        items.eachWithIndex { item, i ->
            SpeciesListItem sli = new SpeciesListItem(dataResourceUid: druid, rawScientificName: item.itemName,
                    itemOrder: i)

            item.kvpValues?.eachWithIndex { k, j ->
                SpeciesListKVP kvp = new SpeciesListKVP(value: k.value, key: k.key, itemOrder: j, dataResourceUid:
                        druid)
                sli.addToKvpValues(kvp)
                kvpMap[k.key] = k.value
            }
            matchNameToSpeciesListItem(sli.rawScientificName, sli, speciesList)

            speciesList.addToItems(sli)

            speciesGuidKvp.push (["guid": sli.guid, "kvps": kvpMap])
        }
        speciesGuidKvp
    }

    def loadSpeciesListFromCSV(CSVReader reader, druid, listname, ListType listType, description, listUrl, listWkt,
                               Boolean isBIE, Boolean isSDS, Boolean isAuthoritative, Boolean isThreatened, Boolean isInvasive,
                               Boolean isPrivate, String region, String authority, String category,
                               String generalisation, String sdsType, Boolean looseSearch, SearchStyle searchStyle, String[] header, Map vocabs) {
        log.debug("Loading species list " + druid + " " + listname + " " + description + " " + listUrl + " " + header + " " + vocabs)
        def kvpmap = [:]
        addVocab(druid,vocabs,kvpmap)
        //attempt to retrieve an existing list first
        SpeciesList sl = SpeciesList.findByDataResourceUid(druid)?:new SpeciesList()
        if (sl.dataResourceUid){
            sl.items.clear()
        }
        sl.listName = listname
        sl.dataResourceUid=druid
        sl.username = localAuthService.email() ?: "info@ala.org.au"
        sl.userId = authService.userId ?: 2729
        sl.firstName = localAuthService.firstname()
        sl.surname = localAuthService.surname()
        sl.description = description
        sl.url = listUrl
        sl.wkt = listWkt
        sl.listType = listType
        sl.region = region
        sl.authority = authority
        sl.category = category
        sl.generalisation = generalisation
        sl.sdsType = sdsType
        sl.isBIE = isBIE
        sl.isSDS = isSDS
        sl.isAuthoritative = isAuthoritative
        sl.isThreatened = isThreatened
        sl.isInvasive = isInvasive
        sl.isPrivate = isPrivate
        sl.looseSearch = looseSearch
        sl.searchStyle = searchStyle
        sl.lastUploaded = new Date()
        String [] nextLine
        boolean checkedHeader = false
        Map termIdx = columnMatchingService.getTermAndIndex(header)
        int itemCount = 0
        int totalCount = 0
        while ((nextLine = reader.readNext()) != null) {
            totalCount++
            if(!checkedHeader){
                checkedHeader = true
                // only read next line if current line is a header line
                if(columnMatchingService.getTermAndIndex(nextLine).size() > 0) {
                    nextLine = reader.readNext()
                }
            }

            if(nextLine.length > 0 && termIdx.size() > 0 && hasValidData(termIdx, nextLine)){
                itemCount++
                sl.addToItems(insertSpeciesItem(nextLine, druid, termIdx, header, kvpmap, itemCount, sl))
            }

        }
        if(!sl.validate()){
            log.error(sl.errors.allErrors?.toString())
        }

        List sli = sl.getItems()?.toList()
        matchCommonNamesForSpeciesListItems(sli)

        sl.save()

        [totalRecords: totalCount, successfulItems: itemCount]
    }

    def loadSpeciesListFromFile(listname, druid, filename, boolean useHeader, header,vocabs){

        CSVReader reader = new CSVReader(new FileReader(filename),',' as char)
        header = header ?: reader.readNext()
        int speciesValueIdx = columnMatchingService.getSpeciesIndex(header)
        int count =0
        String [] nextLine
        def kvpmap =[:]
        //add vocab
        addVocab(druid,vocabs,kvpmap)
        SpeciesList sl = new SpeciesList()
        sl.listName = listname
        sl.dataResourceUid=druid
        sl.username = localAuthService.email()
        sl.firstName = localAuthService.firstname()
        sl.surname = localAuthService.surname()
        sl.lastUploaded = new Date()
        while ((nextLine = reader.readNext()) != null) {
            if(org.apache.commons.lang.StringUtils.isNotBlank(nextLine)){
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap, sl))
                count++
            }

        }

        List sli = sl.getItems().toList()
        matchCommonNamesForSpeciesListItems(sli)

        sl.save()
    }

    def insertSpeciesItem(String[] values, druid, int speciesIdx, Object[] header, map, int order, SpeciesList sl){
        values = parseRow(values as List)
        log.debug("Inserting " + values.toArrayString())

        SpeciesListItem sli = new SpeciesListItem()
        sli.dataResourceUid =druid
        sli.rawScientificName = speciesIdx > -1 ? values[speciesIdx] : null
        sli.itemOrder = order
         int i = 0
        header.each {
            if(i != speciesIdx && values.length > i && values[i]?.trim()){
                SpeciesListKVP kvp = map.get(it.toString()+"|"+values[i], new SpeciesListKVP(key: it.toString(), value: values[i], dataResourceUid: druid))
                if  (kvp.itemOrder == null) {
                    kvp.itemOrder = i
                }
                sli.addToKvpValues(kvp)
            }
            i++
        }
        matchNameToSpeciesListItem(sli.rawScientificName, sli, sl)
        sli
    }

    def insertSpeciesItem(String[] values, String druid, Map termIndex, Object[] header, Map map, int order, SpeciesList sl){
        values = parseRow(values as List)
        log.debug("Inserting " + values.toArrayString())

        SpeciesListItem sli = new SpeciesListItem()
        sli.dataResourceUid = druid
        sli.rawScientificName = termIndex.containsKey(QueryService.RAW_SCIENTIFIC_NAME) ? values[termIndex[QueryService.RAW_SCIENTIFIC_NAME]] : null
        sli.itemOrder = order

        int i = 0
        header.each {
            if(!termIndex.containsValue(i) && values.length > i && values[i]?.trim()){
                SpeciesListKVP kvp = map.get(it.toString()+"|"+values[i], new SpeciesListKVP(key: it.toString(), value: values[i], dataResourceUid: druid))
                if  (kvp.itemOrder == null) {
                    kvp.itemOrder = i
                }
                sli.addToKvpValues(kvp)
            }
            i++
        }
        matchNameToSpeciesListItem(sli.rawScientificName, sli, sl)
        sli
    }

    def  matchNameToSpeciesListItem(String name, SpeciesListItem sli, SpeciesList sl){
        // First match using all available data
        NameUsageMatch match = nameExplorerService.find(sli, sl)
        if (!match || !match.success) {
            match = nameExplorerService.searchForRecordByCommonName(sli.rawScientificName)
        }
        if (!match || !match.success) {
            match = nameExplorerService.searchForRecordByLsid(sli.rawScientificName)
        }
        if(match && match.success){
            sli.guid = match.getTaxonConceptID()
            sli.matchedName = match.getScientificName()
            sli.author = match.getScientificNameAuthorship()
            sli.commonName = match.getVernacularName()
            sli.family = match.getFamily()
            sli.kingdom = match.getKingdom()
        } else {
            sli.guid = null
            sli.matchedName = null
            sli.author = null
            sli.commonName = null
            sli.family = null
            sli.kingdom = null
        }
    }

    void matchAll(List searchBatch, SpeciesList speciesList) {
        List<NameUsageMatch> matches = nameExplorerService.findAll(searchBatch, speciesList);
        matches.eachWithIndex {  NameUsageMatch match, Integer index ->
            SpeciesListItem sli = searchBatch[index]
            if (!match.success) {
                match = nameExplorerService.searchForRecordByCommonName(sli.rawScientificName)
            }
            if (!match.success) {
                match = nameExplorerService.searchForRecordByLsid(sli.rawScientificName)
            }
            if (match && match.success) {
                sli.guid = match.getTaxonConceptID()
                sli.matchedName = match.getScientificName()
                sli.author = match.getScientificNameAuthorship()
                sli.commonName = match.getVernacularName()
                sli.family = match.getFamily()
                sli.kingdom = match.getKingdom()
            } else {
                log.info("Unable to match species list item - ${sli.rawScientificName}")
            }
        }
    }

    def findAcceptedLsidByCommonName(commonName) {
        String lsid = null
        try {
            lsid = nameExplorerService.searchForLsidByCommonName(commonName)
        } catch (Exception e) {
            log.error("findAcceptedLsidByCommonName -  " + e.getMessage())
        }
        lsid
    }

    def findAcceptedLsidByScientificName(scientificName) {
        String lsid = null
        try {
            lsid = nameExplorerService.searchForAcceptedLsidByScientificName(scientificName);
        } catch (Exception e) {
            log.error(e.getMessage())
        }
        lsid
    }

    // JSON response is returned as the unconverted model with the appropriate
    // content-type. The JSON conversion is handled in the filter. This allows
    // for universal JSONP support.
    def asJson(model, response)  {
        response.setContentType("application/json")
        model
    }

    /**
     * finds common name for a guid and saves it to the database. This is done in batches.
     * @param slItems
     */
    void matchCommonNamesForSpeciesListItems(List slItems){
        Integer batchSize = BATCH_SIZE;
        List guidBatch = [], sliBatch = []
        slItems?.each{ SpeciesListItem sli ->
            if(guidBatch.size() < batchSize){
                if(sli.guid){
                    guidBatch.push(sli.guid)
                    sliBatch.push(sli)
                }
            } else {
                getCommonNamesAndUpdateRecords(sliBatch, guidBatch)
                guidBatch = []
                sliBatch = []
            }
        }

        if(guidBatch.size()){
            getCommonNamesAndUpdateRecords(sliBatch, guidBatch)
        }
    }

    @Transactional
    def createRecord (params) {
        def sl = SpeciesList.get(params.id)
        log.debug "params = " + params

        if (!params.rawScientificName) {
            return [text: "Missing required field: rawScientificName", status: 400]
        }
        else if (sl) {
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=:dataResourceUid", [dataResourceUid: sl.dataResourceUid])
            log.debug "keys = " + keys
            def sli = new SpeciesListItem(dataResourceUid: sl.dataResourceUid, rawScientificName: params.rawScientificName, itemOrder: sl.items.size() + 1)
            matchNameToSpeciesListItem(sli.rawScientificName, sli, sl)

            keys.each { key ->
                log.debug "key: " + key + " has value: " + params[key]
                def value = params[key]
                def itemOrder = params["itemOrder_${key}"]
                if (value) {
                    def newKvp = SpeciesListKVP.findByDataResourceUidAndKeyAndValue(sl.dataResourceUid, key, value)
                    if (!newKvp) {
                        log.debug "Couldn't find an existing KVP, so creating a new one..."
                        newKvp = new SpeciesListKVP(dataResourceUid: sli.dataResourceUid, key: key, value: params[key], SpeciesListItem: sli, itemOrder: itemOrder );
                    }
                    sli.addToKvpValues(newKvp)
                }
            }

            sl.addToItems(sli)

            if (!sl.validate()) {
                def message = "Could not update SpeciesList with new item: ${sli.rawScientificName} - " + sl.errors.allErrors
                log.error message
                return [text: message, status: 500]
            }
            else if (sl.save()) {
                // find common name and save it
                matchCommonNamesForSpeciesListItems([sli])
                sl.save(flush: true)
                // Commented out as we would like to keep species list generic
                /*   def preferredSpeciesImageListName = grailsApplication.config.ala.preferred.species.name
                   if (sl.listName == preferredSpeciesImageListName) {
                       helperService.syncBieImage (sli, params.imageId)
                   }*/
                def msg = messageSource.getMessage('public.lists.view.table.edit.messages', [] as Object[], 'Default Message', LocaleContextHolder.locale)
                return[text: msg, status: 200, data: ["species_guid": sli.guid]]
            }
            else {
                def message = "Could not create SpeciesListItem: ${sli.rawScientificName} - " + sl.errors.allErrors
                return [text: message, status: 500]
            }
        }
        else {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), params.id])}"
            return [text: message, status: 404]
        }
    }

    /**
     * This function finds small image url for a guid and updates the corresponding SpeciesListItem record
     * @param sliBatch - list of SpeciesListItems
     * @param guidBatch - list of GUID strings
     */
    void getCommonNamesAndUpdateRecords(List sliBatch, List guidBatch) {
        try{
            List speciesProfiles = bieService.bulkSpeciesLookupWithGuids(guidBatch)
            speciesProfiles?.eachWithIndex { Map profile, index ->
                SpeciesListItem slItem = sliBatch[index]
                if (profile) {
                    slItem.imageUrl = profile.smallImageUrl
                }
            }
        } catch (Exception e){
            log.error("an exception occurred during rematching: ${e.message}");
            log.error(e.stackTrace?.toString())
        }
    }
}
