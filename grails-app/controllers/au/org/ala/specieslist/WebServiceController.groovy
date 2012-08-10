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
import grails.converters.*
import grails.web.JSONBuilder
/**
 * Provides all the webservices to be used from other sources eg the BIE
 */
class WebServiceController {

    def index() { }

    def getTaxaOnList(){
        def druid = params.druid
        def results = SpeciesListItem.executeQuery("select guid from SpeciesListItem where dataResourceUid=?",[druid])
        render results as JSON
    }

    def getListItemsForSpecies(){
        def guid = params.guid
        def lists = params.dr?.split(",")
        def props = [fetch:[kvpValues: 'join']]
        def results = lists ? SpeciesListItem.findAllByGuidAndDataResourceUidInList(guid, lists,props) : SpeciesListItem.findAllByGuid(guid,props)
        //def result2 =results.collect {[id: it.id, dataResourceUid: it.dataResourceUid, guid: it.guid, kvpValues: it.kvpValue.collect{ id:it.}]}
        def builder = new JSONBuilder()

        def listOfRecordMaps = results.collect{li ->
            [ dataResourceUid:li.dataResourceUid,
              guid: li.guid,
              list:[username:li.list.username,listName:li.list.listName],
              kvpValues: li.kvpValues.collect{kvp->
                  [ key:kvp.key,
                    value:kvp.value,
                    vocabValue:kvp.vocabValue
                  ]
              }
            ]
        }
        render builder.build{listOfRecordMaps}

    }

    def markAsPublished(){
        //marks the supplied data resource as published
        if (params.druid){
            SpeciesListItem.executeUpdate("update SpeciesListItem set isPublished=true where dataResourceUid='"+params.druid+"'")
            render "Species list " + params.druid + " has been published"
        }
        else{
            render(view: '../error', model: [message: "No data resource has been supplied"])
        }
    }

    def getBieUpdates(){
        //retrieve all the unpublished list items in batches of 100
        def max =100
        def offset=0
        def hasMore = true
        def out = response.outputStream
        //use a criteria so that paging can be supported.
        def criteria = SpeciesListItem.createCriteria()
        while(hasMore){
            def results = criteria.list([sort:"guid",max: max, offset:offset,fetch:[kvpValues: 'join']]) {
                isNull("isPublished")
                //order("guid")
            }
            //def results =SpeciesListItem.findAllByIsPublishedIsNull([sort:"guid",max: max, offset:offset,fetch:[kvpValues: 'join']])
            results.each {
                //each of the results are rendered as CSV
                def sb = ''<<''
                if(it.kvpValues){
                    it.kvpValues.each{ kvp ->
                        sb<<toCsv(it.guid)<<','<<toCsv(it.dataResourceUid)<<','<<toCsv(kvp.key)<<','<<toCsv(kvp.value)<<','<<toCsv(kvp.vocabValue)<<'\n'
                    }
                }
                else{
                    sb<<toCsv(it.guid)<<','<<toCsv(it.dataResourceUid)<<',,,\n'
                }
                out.print(sb.toString())
            }
            offset += max
            hasMore = offset<results.getTotalCount()
        }
        out.close()
    }
    def toCsv(value){
        if(!value) return ""
        return '"'+value.replaceAll('"','~"')+'"'
    }

}
