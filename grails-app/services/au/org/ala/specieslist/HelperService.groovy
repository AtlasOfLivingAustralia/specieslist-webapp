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
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.checklist.lucene.CBIndexSearch
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import grails.web.JSONBuilder
import au.org.ala.data.model.LinnaeanRankClassification
/**
 * Provides all the services for the species list webapp.  It may be necessary to break this into
 * multiple services if it grows too large
 */
class HelperService {

    def grailsApplication

    def authService

    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def cbIdxSearcher = null

    def speciesValue = ["species", "scientificname", "taxonname"]

    def commonValues = ["commonname","vernacularname"]

    def ambiguousValues = ["name"]

    def collectoryKey = "Venezuela"

    /**
     * Adds a data resource to the collectory for this species list
     * @param username
     * @param description
     * @return
     */
    def addDataResourceForList(map) {
        if(grailsApplication.config.collectory.enableSync){
            def postUrl = grailsApplication.config.collectory.baseURL +"/ws/dataResource"
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
                log.error("Unable to create a collectory entry for the species list.",ex)
                return null
            }

        } else {
           //return a dummy URL
          "http://collections.ala.org.au/tmp/drt" + System.currentTimeMillis()
        }
    }

    def updateDataResourceForList(drId, map) {
        if(grailsApplication.config.collectory.enableSync){
            def postUrl = grailsApplication.config.collectory.baseURL +"/ws/dataResource/" + drId

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
          "http://collections.ala.org.au/tmp/drt" + System.currentTimeMillis()
        }
    }

    def createJsonForNewDataResource(map){
        map.api_key = collectoryKey
        map.user = 'Species list upload'
        map.firstName = authService.firstname()?:""
        map.lastName = authService.surname()?:""
        JsonOutput jo = new JsonOutput()
        jo.toJson(map)
    }

    def uploadFile(druid, uploadedFile){
        if(druid){
            def destDir = new File(grailsApplication.config.bie.download + File.separator + druid + File.separator)
            destDir.mkdirs()
            def destFile = new File( destDir,"species_list.csv")
            uploadedFile.transferTo(destFile)
            destFile.absolutePath
        }
    }

    def getCSVReaderForText(String raw, String separator) {
        new CSVReader(new StringReader(raw), separator.charAt(0))
    }

    def getSeparator(String raw) {
        def firstline = ""
        if(raw.indexOf("\n")>0){
            firstline = raw.substring(0, raw.indexOf("\n"))
        } else {
            firstline = raw
        }
        int tabs = firstline.count("\t")
        int commas = firstline.count(",")
        if(tabs > commas) {
            '\t'
        } else {
            ','
        }
    }

    def parseValues(String[] processedHeader,CSVReader reader, String sep)throws Exception{
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
        headerResponse
    }

    def parseHeader(String[] header){

        //first step check to see if scientificname or common name is provided as a header
        def hasName = false;
        def headerResponse =header.collect{
            if(speciesValue.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "scientific name"
            } else if(commonValues.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "vernacular name"
            } else if(commonValues.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "vernacular name"
            } else if(ambiguousValues.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "ambiguous name"
            } else {
                it
            }
        }

        if(hasName)
            headerResponse
        else
            null
    }

    def getSpeciesIndex(Object[] header){
        int idx =header.findIndexOf { speciesValue.contains(it.toString().toLowerCase().replaceAll(" ","")) }
        if(idx <0)
            idx =header.findIndexOf { commonValues.contains(it.toLowerCase().replaceAll(" ",""))}
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

    def loadSpeciesList(Map json, String druid, List<String> items){
        SpeciesList sl = new SpeciesList(json)
        sl.setDataResourceUid(druid)
        items.eachWithIndex { item, i ->
            //look it up
            SpeciesListItem sli = new SpeciesListItem()
            sli.dataResourceUid =druid
            sli.rawScientificName = item
            sli.itemOrder = i
            //sli.guid = findAcceptedLsidByScientificName(sli.rawScientificName)?: findAcceptedLsidByCommonName(sli.rawScientificName)
            matchNameToSpeciesListItem(sli.rawScientificName, sli)
            sl.addToItems(sli)
        }
        if(!sl.validate()){
            log.error(sl.errors.allErrors)
        }
        sl.save()
    }

    def loadSpeciesList(CSVReader reader,druid,listname,ListType listType,description, listUrl, listWkt, Boolean isBIE, Boolean isSDS, String region, String authority, String category, String generalisation, String sdsType, String[] header, Map vocabs){
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
        sl.username = authService.email()
        sl.firstName = authService.firstname()
        sl.surname = authService.surname()
        sl.description = description
        sl.url = listUrl
        sl.wkt = listWkt
        sl.listType = listType
        sl.region = region
        sl.authority = authority
        sl.category = category
        sl.generalisation = generalisation
        sl.sdsType = sdsType
        if(isBIE)sl.isBIE=true
        if(isSDS)sl.isSDS=true
        String [] nextLine
        boolean checkedHeader = false
        int speciesValueIdx = getSpeciesIndex(header)
        int count = 0
        while ((nextLine = reader.readNext()) != null) {
            if(!checkedHeader){
                checkedHeader = true
                if(getSpeciesIndex(nextLine)>-1)
                    nextLine = reader.readNext()
            }
            if(nextLine.length>0 && org.apache.commons.lang.StringUtils.isNotBlank(nextLine[speciesValueIdx])){
                count+=1;
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap, count))
            }

        }
        if(!sl.validate()){
            log.error(sl.errors.allErrors)
        }
        if(sl.items.size()>0){
            sl.save()
        }

        count
    }

    def loadSpeciesList(listname,druid, filename, boolean useHeader, header,vocabs){

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
        sl.username = authService.email()
        sl.firstName = authService.firstname()
        sl.surname = authService.surname()
        while ((nextLine = reader.readNext()) != null) {
            if(org.apache.commons.lang.StringUtils.isNotBlank(nextLine)){
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap))
                count++
            }

        }
        sl.save()
    }

    def insertSpeciesItem(String[] values, druid, int speciesIdx, Object[] header,map, int order){
        log.debug("Inserting " + values.toArrayString())
        SpeciesListItem sli = new SpeciesListItem()
        sli.dataResourceUid =druid
        sli.rawScientificName = values[speciesIdx]
        sli.itemOrder = order
        //lookup the raw
        //sli.guid = findAcceptedLsidByScientificName(sli.rawScientificName)?: findAcceptedLsidByCommonName(sli.rawScientificName)
        matchNameToSpeciesListItem(sli.rawScientificName, sli)
        int i = 0
        header.each {
            if(i != speciesIdx && values.length > i && values[i]){
                //check to see if the common name is already an "accepted" name for the species
                String testLsid = commonValues.contains(it.toLowerCase().replaceAll(" ",""))?findAcceptedLsidByCommonName(values[i]):""
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
        //sli.save()
    }

    def createOrRetrieveSpeciesListKVP(String key, String value, String dataResourceUid){
//        SpeciesListKVP kvp =
       SpeciesListKVP.findByKeyAndValueAndDataResourceUid(key,value,dataResourceUid) ?: new SpeciesListKVP(key: key, value: value, dataResourceUid: dataResourceUid)
//        SpeciesListKVP test = kvp ?: new SpeciesListKVP(key: key, value: value, dataResourceUid: dataResourceUid)
//        if(kvp?.id != null)
//            return kvp
//        else
//            return new SpeciesListKVP(key: key, value: value, dataResourceUid: dataResourceUid)
    }

    def withSessionCleaner(def items, Closure c){
        items.eachWithIndex { obj, index ->
            c(obj)

            if (index % 100 == 0) {
                def session = sessionFactory.currentSession
                session.flush()
                session.clear()
                propertyInstanceMap.get().clear()
            }
        }
    }

    def matchNameToSpeciesListItem(String name, SpeciesListItem sli){
        NameSearchResult nsr = findAcceptedConceptByScientificName(sli.rawScientificName)?: findAcceptedConceptByCommonName(sli.rawScientificName)
        if(nsr){
            sli.guid = nsr.getLsid()
            sli.family = nsr.getRankClassification().getFamily()
            sli.matchedName = nsr.getRankClassification().getScientificName()
        }
    }

    def getNameSearcher(){
        if(!cbIdxSearcher)
            cbIdxSearcher =new CBIndexSearch(grailsApplication.config.bie.nameIndexLocation)
        cbIdxSearcher
    }

    def findAcceptedLsidByCommonName(commonName){
        String lsid = null
        try {
            lsid = getNameSearcher().searchForLSIDCommonName(commonName)
        } catch(e){
            log.error(e.getMessage())
        }
        lsid
    }

    def findAcceptedLsidByScientificName(scientificName){
        String lsid = null
        try{
            def cl = new LinnaeanRankClassification()
            cl.setScientificName(scientificName)
            lsid = getNameSearcher().searchForAcceptedLsidDefaultHandling(cl, true);
//            def cl = new LinnaeanRankClassification()
//            cl.setScientificName(scientificName)
//            def metric = getNameSearcher().searchForRecordMetrics(cl, false,true);
//            if(metric && metric.getResult()){
//                def result = metric.getResult()
//                lsid = result.isSynonym()? getNameSearcher().searchForRecordByLsid(result.getAcceptedLsid())?.getLsid():result.getLsid()
//            }
        } catch(Exception e){
             log.error(e.getMessage())
        }
        lsid
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

}
