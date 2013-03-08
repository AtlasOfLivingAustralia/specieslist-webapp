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

    def helperService
    def authService
    def beforeInterceptor = [action:this.&prevalidate,only:['getListDetails','saveList']]

    private def prevalidate(){
        //ensure that the supplied druid is valid
        log.debug("Prevalidating...")
        if(params.druid){
            def list = SpeciesList.findByDataResourceUid(params.druid)
            if (list){
                params.splist=list
            }
            else{
                notFound "Unable to locate species list ${params.druid}"
                return false
            }
        }
        return true
    }



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


    /**
     *   Returns either a JSON list of species lists or a specific species list
     *
     *   @param druid - the data resource uid for the list to return  (optional)
     *   @param splist - optional instance (added by the beforeInterceptor)
     */
    def getListDetails ={
        log.debug("params" + params)
        if(params.splist) {
            def sl = params.splist
            log.debug("The speciesList: " +sl)
            def builder = new JSONBuilder()

            def retValue = builder.build{
                dataResourceUid = sl.dataResourceUid
                listName = sl.listName
                if(sl.listType) listType = sl.listType
            }
            log.debug(" The retvalue: " + retValue)
            render retValue

        }
        else{
            //we need to return a summary of all lists
            //def allLists = SpeciesList.list([sort: 'listName',fetch: [items: 'lazy']])
            //def allLists = params.user? SpeciesList.findAll("from SpeciesList sl order by case username when '" + params.user +"' then 0 else 1 end, listName",[max:4, offset:10]):SpeciesList.list([sort: 'listName',fetch: [items: 'lazy']])
            def allLists = params.user? SpeciesList.findAll("from SpeciesList sl order by case username when '" + params.user +"' then 0 else 1 end, listName"):SpeciesList.list([sort: 'listName',fetch: [items: 'lazy']])
            def retValue =allLists.collect{[dataResourceUid: it.dataResourceUid, listName: it.listName, listType:it?.listType?.toString(), username:it.username, firstName:it.firstName, surname:it.surname, fullName:it.getFullName()]}

            render retValue as JSON
        }
    }

    /**
     * Returns a summary list of items that form part of the supplied species list.
     */
    def getListItemDetails ={
        def list = params.nonulls? SpeciesListItem.findAllByDataResourceUidAndGuidIsNotNull(params.druid):SpeciesListItem.findAllByDataResourceUid(params.druid)
        def newList= list.collect{[id:it.id,name:it.rawScientificName, lsid: it.guid]}
        render newList as JSON
    }

    /**
     * Saves the details of the species list when no druid is provided in the JSON body
     * a new list is inserted.  Inserting a new list will fail if there are no items to be
     * included on the list.
     */
    def saveList = {
        log.debug("Saving a user list")
        if(params.splist || params.druid){
            //a list update is not supported at the moment
            badRequest "Updates to existing list are unsupported."
        }
        else{
            //create a new list
            log.debug("SAVE LIST " + params)
            log.debug(request)
            log.debug("COOKIES:" +request.cookies)
            try{
                def jsonBody =request.JSON
                log.debug("BODY : "+jsonBody)
                //request.cookies.each { log.debug(it.getName() + "##" + it.getValue())}
                def userCookie =request.cookies.find{it.name == 'ALA-Auth'}
                log.debug(userCookie.toString() + " " + userCookie.getValue())
                if(userCookie){
                    String username =java.net.URLDecoder.decode(userCookie.getValue(),'utf-8')
                    //test to see that the user is valid
                    if(authService.isValidUserName(username)){

                        if (jsonBody.listItems && jsonBody.listName){
                            jsonBody.username=username
                            log.warn(jsonBody)
                            def drURL = helperService.addDataResourceForList(jsonBody.listName, null, null, username)
                            if(drURL){
                                def druid = drURL.toString().substring(drURL.lastIndexOf('/') +1)
                                List<String> list = jsonBody.listItems.split(",")
                                helperService.loadSpeciesList(jsonBody,druid,list)
                                created druid
                            }
                            else
                                badRequest "Unable to generate collectory entry."
                        }
                        else
                            badRequest "Missing compulsory mandatory properties."
                    }
                    else
                        badRequest "Supplied username is invalid"
                }
                else
                    badRequest "User has not logged in or cookies are disabled"
            }
            catch (Exception e){
                log.error(e.getMessage(),e)
                render(status:  404, text: "Unable to parse JSON body")
            }
        }
    }


    def created = { uid ->
        response.addHeader 'druid', uid
        render(status:201, text:'added species list')
    }

    def badRequest = {text ->
        render(status:400, text: text)
    }

    def notFound = { text ->
        render(status:404, text: text)
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
