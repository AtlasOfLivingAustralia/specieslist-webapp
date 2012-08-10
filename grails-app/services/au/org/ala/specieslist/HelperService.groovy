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
import au.org.ala.specieslist.SpeciesListItem
import au.org.ala.checklist.lucene.CBIndexSearch
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.JSON
import grails.web.JSONBuilder
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

    def commonValues = ["commmonname","vernacularname"]

    def ambiguousValues = ["name"]

    def collectoryKey="Venezuela"

    //"http://vpn-cbr-67.act.csiro.au:8080/Collectory"

    /*
       String name = "my test dataset";
    String api_key ="Venezuela";
    String user= "Sandbox upload services";
    String email = "";
    String firstName = "";
    String lastName = "";
     */

    /**
     * Adds a data resource to the collectory for this species list
     * @param username
     * @param description
     * @return
     */
    def addDataResourceForList(name) {
        def http = new HTTPBuilder(grailsApplication.config.colletory.baseURL +"/ws/dataResource")
        def jsonBody = createJsonForNewDataResource(name)
        log.debug(jsonBody)
        try{
         http.post(body: jsonBody, requestContentType:JSON){ resp ->
             assert resp.status == 201
             return resp.headers['location'].getValue()
         }
        }
        catch(ex){
            log.error("Unable to create a collectory enrey for the species list.",ex)
            return null
        }

    }

    def createJsonForNewDataResource(listname){
        def builder = new JSONBuilder()

        def result = builder.build{
            name = listname
            user = "Species List Upload"
            api_key = collectoryKey
            email = authService.email()
            firstName = ""
            lastName = ""
            resourceType = "uploads"

        }
        result.toString(false)
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
    def getCSVReaderForText(String raw) {
        def separator = getSeparator(raw)
        def csvReader = new CSVReader(new StringReader(raw), separator.charAt(0))
        csvReader
    }

    def getSeparator(String raw) {
        int tabs = raw.count("\t")
        int commas = raw.count(",")
        if(tabs > commas)
            return '\t'
        else
            return ','
    }
    def parseValues(String[] processedHeader,CSVReader reader){
        def sciIdx = indexOfName(processedHeader)
        if(sciIdx>=0){
            //now lets determine the possible values
            String[] nextLine
            Map map =[:]
            while ((nextLine = reader.readNext()) != null) {
                  nextLine.eachWithIndex {v,i ->
                      if(i != sciIdx){
                          def set =map.get(processedHeader[i], [] as Set)
                          if(v != processedHeader[i])
                            set.add(v)
                      }
                  }
            }
            return map;
        }
        else
            null
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
            }
            else if(findAcceptedLsidByCommonName(it)){
                hasName = true
                "vernacular name"
            }
            else{
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
            }
            else if(commonValues.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "vernacular name"
            }
            else if(ambiguousValues.contains(it.toLowerCase().replaceAll(" ",""))){
                hasName = true
                "ambiguous name"
            }
            else
                it

        }
        if(hasName)
            headerResponse
        else
            null
    }
    def getSpeciesIndex(Object[] header){
        header.findIndexOf { speciesValue.contains(it.toString().toLowerCase().replaceAll(" ",""))}
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

    def loadSpeciesList(CSVReader reader,druid,listname, String[] header, Map vocabs){
        def kvpmap = [:]
        addVocab(druid,vocabs,kvpmap)
        SpeciesList sl = new SpeciesList()
        sl.listName = listname
        sl.dataResourceUid=druid
        sl.username = authService.email()
        sl.firstName = authService.firstname()
        sl.surname = authService.surname()
        String [] nextLine
        boolean checkedHeader = false
        int speciesValueIdx = getSpeciesIndex(header)
        while ((nextLine = reader.readNext()) != null) {
            if(!checkedHeader){
                checkedHeader = true
                if(getSpeciesIndex(nextLine)>-1)
                    nextLine = reader.readNext()
            }
            if(nextLine.length>0){
                sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap))
            }

//            if(count%100){
//                def session = sessionFactory.currentSession
//                session.flush()
//                session.clear()
//                propertyInstanceMap.get().clear()
//            }
        }
        if(sl.items.size()>0)
            sl.save()
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
            sl.addToItems(insertSpeciesItem(nextLine, druid, speciesValueIdx, header,kvpmap))
            count++
//            if(count%100){
//                def session = sessionFactory.currentSession
//                session.flush()
//                session.clear()
//                propertyInstanceMap.get().clear()
//            }
        }
        sl.save()
    }

    def insertSpeciesItem(String[] values, druid, int speciesIdx, Object[] header,map){
        log.debug("Inserting " + values.toArrayString())
        SpeciesListItem sli = new SpeciesListItem()
        sli.dataResourceUid =druid
        sli.rawScientificName = values[speciesIdx]

        int i =0
        header.each {
            if(i != speciesIdx && values[i])
                sli.addToKvpValues(map.get(it.toString()+"|"+values[i], new SpeciesListKVP(key: it.toString(), value: values[i], dataResourceUid: druid)))//createOrRetrieveSpeciesListKVP(it,values[i],druid))
            i++
        }
        //lookup the raw
        sli.guid = findAcceptedLsidByScientificName(sli.rawScientificName)
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

    def getNameSearcher(){
        if(!cbIdxSearcher)
            cbIdxSearcher =new CBIndexSearch(grailsApplication.config.bie.nameIndexLocation)
        cbIdxSearcher
    }

    def findAcceptedLsidByCommonName(commonName){
        String lsid = null
        try{
            lsid = getNameSearcher().searchForLSIDCommonName(commonName)
        }
        catch(e){

        }
        lsid
    }

    def findAcceptedLsidByScientificName(scientificName){
        String lsid = null
        try{
            lsid = getNameSearcher().searchForLSID(scientificName, true);
        }
        catch(Exception e){

        }
        lsid
    }
}
