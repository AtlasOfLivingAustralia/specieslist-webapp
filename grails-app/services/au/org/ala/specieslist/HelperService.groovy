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
import au.org.ala.names.model.LinnaeanRankClassification
import au.org.ala.names.model.NameSearchResult
import au.org.ala.names.search.ALANameSearcher
import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.nibor.autolink.Autolink
import org.nibor.autolink.LinkExtractor
import org.nibor.autolink.LinkRenderer
import org.nibor.autolink.LinkSpan
import org.nibor.autolink.LinkType
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.annotation.PostConstruct

import static groovyx.net.http.ContentType.JSON
/**
 * Provides all the services for the species list webapp.  It may be necessary to break this into
 * multiple services if it grows too large
 */
class HelperService {

    def grailsApplication

    def localAuthService, authService, userDetailsService

    BieService bieService

    def cbIdxSearcher = null

    Integer BATCH_SIZE

    String[] speciesNameColumns = []
    String[] commonNameColumns = []
    String[] ambiguousNameColumns = []

    // Only permit URLs for added safety
    private final LinkExtractor extractor = LinkExtractor.builder().linkTypes(EnumSet.of(LinkType.URL)).build()

    @PostConstruct
    init(){
        BATCH_SIZE = Integer.parseInt((grailsApplication.config.batchSize?:200).toString())
        speciesNameColumns = grailsApplication.config.speciesNameColumns ?
                grailsApplication.config.speciesNameColumns.split(',') : []
        commonNameColumns = grailsApplication.config.commonNameColumns ?
                grailsApplication.config.commonNameColumns.split(',') : []
        ambiguousNameColumns = grailsApplication.config.ambiguousNameColumns ?
                grailsApplication.config.ambiguousNameColumns.split(',') : []
    }

    /**
     * Adds a data resource to the collectory for this species list
     * @param username
     * @param description
     * @return
     */
    def addDataResourceForList(map) {
        if(grailsApplication.config.collectory.enableSync?.toBoolean()){
            def postUrl = grailsApplication.config.collectory.baseURL + "/ws/dataResource"
            def http = new HTTPBuilder(postUrl)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            def jsonBody = createJsonForNewDataResource(map)
            log.debug(jsonBody)
            try {
               http.post(body: jsonBody, requestContentType:JSON){ resp ->
                 assert resp.status == 201
                 return resp.headers['location'].getValue()
               }
            } catch(ex){
                log.error("Unable to create a collectory entry for the species list. ", ex)
                return null
            }

        } else {
           //return a dummy URL
          grailsApplication.config.collectory.baseURL + "/tmp/drt" + System.currentTimeMillis()
        }
    }

    def deleteDataResourceForList(drId) {
        if(grailsApplication.config.collectory.enableSync?.toBoolean()){
            def deleteUrl = grailsApplication.config.collectory.baseURL +"/ws/dataResource/" + drId
            def http = new HTTPBuilder(deleteUrl)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            try {

                http.request(Method.DELETE) {
                    requestContentType = ContentType.JSON
                    headers."Authorization" = "${grailsApplication.config.registryApiKey}"
                    response.success = { resp ->
                        log.info(resp)
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
        if(grailsApplication.config.collectory.enableSync?.toBoolean()){
            def postUrl = grailsApplication.config.collectory.baseURL + "/ws/dataResource/" + drId
            def http = new HTTPBuilder(postUrl)
            http.getClient().getParams().setParameter("http.socket.timeout", new Integer(5000))
            def jsonBody = createJsonForNewDataResource(map)
            log.debug(jsonBody)
            try {
               http.post(body: jsonBody, requestContentType:JSON){ resp ->
                 log.debug("Response code: " + resp.status)
               }
            } catch(ex) {
                log.error("Unable to create a collectory entry for the species list.",ex)
            }
        } else {
           //return a dummy URL
          grailsApplication.config.collectory.baseURL + "/tmp/drt" + System.currentTimeMillis()
        }
    }

    def createJsonForNewDataResource(map){
        map.api_key = grailsApplication.config.registryApiKey
        map.resourceType = "species-list"
        map.user = 'Species list upload'
        map.firstName = localAuthService.firstname()?:""
        map.lastName = localAuthService.surname()?:""
        JsonOutput jo = new JsonOutput()
        jo.toJson(map)
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

    def getCSVReaderForCSVFileUpload(CommonsMultipartFile file, char separator) {
        new CSVReader(new InputStreamReader(file.getInputStream()), separator)
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

    def parseHeader(String[] header) {

        //first step check to see if scientificname or common name is provided as a header
        def hasName = false;
        def headerResponse = header.collect {
            if (speciesNameColumns.contains(it.toLowerCase().replaceAll(" ", ""))) {
                hasName = true
                "scientific name"
            } else if (commonNameColumns.contains(it.toLowerCase().replaceAll(" ", ""))) {
                hasName = true
                "vernacular name"
            } else if (commonNameColumns.contains(it.toLowerCase().replaceAll(" ", ""))) {
                hasName = true
                "vernacular name"
            } else if (ambiguousNameColumns.contains(it.toLowerCase().replaceAll(" ", ""))) {
                hasName = true
                "ambiguous name"
            } else {
                it
            }
        }

        headerResponse = parseHeadersCamelCase(headerResponse)

        if (hasName)
            [header: headerResponse, nameFound: hasName]
        else
            null
    }

    // specieslist-webapp#50
    def parseHeadersCamelCase(List header) {
        def ret = []
        header.each {String it ->
            StringBuilder word = new StringBuilder()
            if (Character.isUpperCase(it.codePointAt(0))) {
                for (int i = 0; i < it.size(); i++) {
                    if (Character.isUpperCase(it[i] as char) && i != 0) {
                        word << " "
                    }
                    word << it[i]
                }

                ret << word.toString()
            }
            else {
                ret << it
            }
        }

        ret
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

    def getSpeciesIndex(Object[] header){
        int idx =header.findIndexOf { speciesNameColumns.contains(it.toString().toLowerCase().replaceAll(" ","")) }
        if(idx <0)
            idx =header.findIndexOf { commonNameColumns.contains(it.toString().toLowerCase().replaceAll(" ",""))}
        return idx
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

    def loadSpeciesListFromJSON(Map json, String druid) {
        SpeciesList speciesList = SpeciesList.findByDataResourceUid(druid) ?: new SpeciesList(json)

        // updating an existing list
        if (speciesList.dataResourceUid) {
            // assume new list of species will replace existing one (no updates allowed for now)
            speciesList.items.clear()

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
            // lookup userId for username
            def emailLC = speciesList.username?.toLowerCase()
            Map userNameMap = userDetailsService.getFullListOfUserDetailsByUsername()

            if (userNameMap.containsKey(emailLC)) {
                def user = userNameMap.get(emailLC)
                speciesList.userId = user.userId
            }
        }

        // version 1 of this operation supports list items as a comma-separated string
        // version 2 of this operation supports list items as structured JSON elements with KVPs
        if (isSpeciesListJsonVersion1(json)) {
            loadSpeciesListItemsFromJsonV1(json, speciesList, druid)
        } else if (isSpeciesListJsonVersion2(json)) {
            loadSpeciesListItemsFromJsonV2(json, speciesList, druid)
        } else {
            throw new UnsupportedOperationException("Unsupported data structure")
        }

        if (!speciesList.validate()) {
            log.error(speciesList.errors.allErrors)
        }

        speciesList.save(flush: true)

        List sli = speciesList.getItems().toList()
        matchCommonNamesForSpeciesListItems(sli)

        speciesList
    }

    private static boolean isSpeciesListJsonVersion1(Map json) {
        // version 1 of this operation supports list items as a comma-separated string
        json.listItems in String
    }

    private loadSpeciesListItemsFromJsonV1(Map json, SpeciesList speciesList, String druid) {
        assert json.listItems, "Cannot create a Species List with no items"

        List items = json.listItems.split(",")

        items.eachWithIndex { item, i ->
            SpeciesListItem sli = new SpeciesListItem(dataResourceUid: druid, rawScientificName: item, itemOrder: i)
            matchNameToSpeciesListItem(sli.rawScientificName, sli)
            speciesList.addToItems(sli)
        }
    }

    private static boolean isSpeciesListJsonVersion2(Map json) {
        // version 2 of this operation supports list items as structured JSON elements with KVPs - i.e. a JSON Array
        json.listItems in JSONArray
    }

    private loadSpeciesListItemsFromJsonV2(Map json, SpeciesList speciesList, String druid) {
        assert json.listItems, "Cannot create a Species List with no items"

        List items = json.listItems
        items.eachWithIndex { item, i ->
            SpeciesListItem sli = new SpeciesListItem(dataResourceUid: druid, rawScientificName: item.itemName,
                    itemOrder: i)
            matchNameToSpeciesListItem(sli.rawScientificName, sli)

            item.kvpValues?.eachWithIndex { k, j ->
                SpeciesListKVP kvp = new SpeciesListKVP(value: k.value, key: k.key, itemOrder: j, dataResourceUid:
                        druid)
                sli.addToKvpValues(kvp)
            }

            speciesList.addToItems(sli)
        }
    }

    def loadSpeciesListFromCSV(CSVReader reader, druid, listname, ListType listType, description, listUrl, listWkt,
                               Boolean isBIE, Boolean isSDS, String region, String authority, String category,
                               String generalisation, String sdsType, String[] header, Map vocabs) {
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
        sl.isAuthoritative=false // default all new lists to isAuthoritative = false: it is an admin task to determine whether a list is authoritative or not
        sl.isInvasive=false
        sl.isThreatened=false
        String [] nextLine
        boolean checkedHeader = false
        int speciesValueIdx = getSpeciesIndex(header)
        int itemCount = 0
        int totalCount = 0
        while ((nextLine = reader.readNext()) != null) {
            totalCount++
            if(!checkedHeader){
                checkedHeader = true
                if(getSpeciesIndex(nextLine) > -1) {
                    nextLine = reader.readNext()
                }
            }
            if(nextLine.length > 0 && speciesValueIdx > -1 && StringUtils.isNotBlank(nextLine[speciesValueIdx])){
                itemCount++
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap, itemCount))
            }

        }
        if(!sl.validate()){
            log.error(sl.errors.allErrors)
        }
        if(sl.items && sl.items.size() > 0){
            sl.save()
        }

        List sli = sl.getItems()?.toList()
        matchCommonNamesForSpeciesListItems(sli)

        [totalRecords: totalCount, successfulItems: itemCount]
    }

    def loadSpeciesListFromFile(listname, druid, filename, boolean useHeader, header,vocabs){

        CSVReader reader = new CSVReader(new FileReader(filename),',' as char)
        header = header ?: reader.readNext()
        int speciesValueIdx = getSpeciesIndex(header)
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
        while ((nextLine = reader.readNext()) != null) {
            if(org.apache.commons.lang.StringUtils.isNotBlank(nextLine)){
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap))
                count++
            }

        }
        sl.save()

        List sli = sl.getItems().toList()
        matchCommonNamesForSpeciesListItems(sli)
    }

    def insertSpeciesItem(String[] values, druid, int speciesIdx, Object[] header,map, int order){
        values = parseRow(values as List)
        log.debug("Inserting " + values.toArrayString())

        SpeciesListItem sli = new SpeciesListItem()
        sli.dataResourceUid =druid
        sli.rawScientificName = speciesIdx > -1 ? values[speciesIdx] : null
        sli.itemOrder = order
        //lookup the raw
        //sli.guid = findAcceptedLsidByScientificName(sli.rawScientificName)?: findAcceptedLsidByCommonName(sli.rawScientificName)
        matchNameToSpeciesListItem(sli.rawScientificName, sli)
        int i = 0
        header.each {
            if(i != speciesIdx && values.length > i && values[i]?.trim()){
                //check to see if the common name is already an "accepted" name for the species
                String testLsid = commonNameColumns.contains(it.toLowerCase().replaceAll(" ",""))?findAcceptedLsidByCommonName(values[i]):""
                if(!testLsid.equals(sli.guid)) {
                    SpeciesListKVP kvp =map.get(it.toString()+"|"+values[i], new SpeciesListKVP(key: it.toString(), value: values[i], dataResourceUid: druid))
                    if  (kvp.itemOrder == null) {
                        kvp.itemOrder = i
                    }
                    sli.addToKvpValues(kvp)//createOrRetrieveSpeciesListKVP(it,values[i],druid))
                }
            }
            i++
        }

        sli
    }

    def  matchNameToSpeciesListItem(String name, SpeciesListItem sli){
        //includes matchedName search for rematching if nameSearcher lsids change.
        NameSearchResult nsr = findAcceptedConceptByScientificName(sli.rawScientificName) ?:
                findAcceptedConceptByCommonName(sli.rawScientificName) ?:
                        findAcceptedConceptByLSID(sli.rawScientificName) ?:
                                findAcceptedConceptByNameFamily(sli.matchedName, sli.family)
        if(nsr){
            sli.guid = nsr.getLsid()
            sli.family = nsr.getRankClassification().getFamily()
            sli.matchedName = nsr.getRankClassification().getScientificName()
            sli.author = nsr.getRankClassification().getAuthorship();
        }
    }

    def getNameSearcher(){
        if(!cbIdxSearcher) {
            cbIdxSearcher = new ALANameSearcher(grailsApplication.config.bie.nameIndexLocation)
        }
        cbIdxSearcher
    }

    def findAcceptedLsidByCommonName(commonName){
        String lsid = null
        try {
            lsid = getNameSearcher().searchForLSIDCommonName(commonName)
        } catch(e){
            log.error("findAcceptedLsidByCommonName -  " + e.getMessage())
        }
        lsid
    }

    def findAcceptedLsidByScientificName(scientificName){
        String lsid = null
        try{
            def cl = new LinnaeanRankClassification()
            cl.setScientificName(scientificName)
            lsid = getNameSearcher().searchForAcceptedLsidDefaultHandling(cl, true);
        } catch(Exception e){
             log.error(e.getMessage())
        }
        lsid
    }

    def findAcceptedConceptByLSID(lsid){
        NameSearchResult nameSearchRecord
        try{
            nameSearchRecord = getNameSearcher().searchForRecordByLsid(lsid)
        }
        catch(Exception e){
            log.error(e.getMessage())
        }
        nameSearchRecord
    }

    def findAcceptedConceptByNameFamily(String scientificName, String family) {
        NameSearchResult nameSearchRecord
        try{
            def cl = new LinnaeanRankClassification()
            cl.setScientificName(scientificName)
            cl.setFamily(family)
            nameSearchRecord = getNameSearcher().searchForAcceptedRecordDefaultHandling(cl, true)
        }
        catch(Exception e){
            log.error(e.getMessage())
        }
        nameSearchRecord
    }

    def findAcceptedConceptByScientificName(scientificName){
        NameSearchResult nameSearchRecord
        try{
            def cl = new LinnaeanRankClassification()
            cl.setScientificName(scientificName)
            nameSearchRecord = getNameSearcher().searchForAcceptedRecordDefaultHandling(cl, true)
        }
        catch(Exception e){
            log.error(e.getMessage())
        }
        nameSearchRecord
    }

    def findAcceptedConceptByCommonName(commonName){
        NameSearchResult nameSearchRecord
        try{
            nameSearchRecord = getNameSearcher().searchForCommonName(commonName)
        }
        catch(Exception e){
            log.error(e.getMessage())
        }
        nameSearchRecord
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

    def syncBieImage (sli, imageId) {
        boolean updateBieImage = false
        int imageIdPos = sli.getImageUrl()? sli.getImageUrl().toLowerCase().indexOf("?imageid=") : 0
        if (imageId && imageIdPos > 0) {
            String bieImage = sli.getImageUrl().substring(imageIdPos + "?imageId=".length())
            if (bieImage != imageId) {
                updateBieImage = true
            }
        } else if (imageId) {
            updateBieImage = true
        }

        if (updateBieImage) {
            List<Map> guidImageList = [["guid": sli.guid, "image": imageId]]
            def resp = bieService.updateBieIndex(guidImageList)
            resp.updatedTaxa?.each { Map profile ->
                if (profile && sli.guid == profile.guid) {
                    sli.imageUrl = profile.smallImageUrl
                    if (!sli.save()) {
                        log.error("Unable to save SpeciesListItem for ${sli.guid}: ${sli.dataResourceUid}")
                    }
                }
            }
        }
    }


    /**
     * This function finds common name for a guid and updates the corresponding SpeciesListItem record
     * @param sliBatch - list of SpeciesListItems
     * @param guidBatch - list of GUID strings
     */
    void getCommonNamesAndUpdateRecords(List sliBatch, List guidBatch) {
        try{
            List speciesProfiles = bieService.bulkSpeciesLookupWithGuids(guidBatch)
            speciesProfiles?.eachWithIndex { Map profile, index ->
                SpeciesListItem slItem = sliBatch[index]
                if (profile) {
                    slItem.commonName = profile.commonNameSingle
                    slItem.imageUrl = profile.smallImageUrl
                    if (!slItem.save()) {
                        log.error("Unable to save SpeciesListItem for ${slItem.guid}: ${slItem.dataResourceUid}")
                    }
                }
            }
        } catch (Exception e){
            log.error("an exception occurred during rematching: ${e.message}");
            log.error(e.stackTrace)
        }
    }
}
