package au.org.ala.specieslist

import java.text.SimpleDateFormat

class EditorController {
    def localAuthService
    def authService // from ala-web-theme
    def helperService
    def userDetailsService
    def bieService

    /**
     * Provides (ajax) content for the edit permissions modal popup
     */
    def editPermissions() {
        def speciesList = SpeciesList.findByDataResourceUid(params.id)
        log.debug "logged in user: " + localAuthService.email()
        if (!speciesList) {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), params.id])}"
            //redirect(controller: "public", action: "speciesLists")
            render(text: message, status: 404 )
        } else if (!isCurrentUserEditorForList(speciesList)) {
            def message = "You are not authorised to access this page"
            //redirect(controller: "public", action: "speciesLists")
            render(text: message, status: 403 )
        } else {
            if (params.message)
                flash.message = params.message
            render(view: "permissions", model: [
                    speciesList: speciesList,
                    mapOfUserNamesById: null, // deprecated
                    editorsWithDetails: populateEditorsDetails(speciesList.editors as List)
            ])
        }
    }

    /**
     * For the input list of editors (String userId), do a lookup via auth and
     * populate a new list with userDetails object (includes name and email, etc)
     *
     * @param editors
     * @return
     */
    private List populateEditorsDetails(editors) {
        List editorsWithDetails = []
        Map allUsersMap = userDetailsService.getFullListOfUserDetails()
        //log.debug "allUsersMap keys = ${allUsersMap.keySet()}"
        //log.debug "allUsersMap 13 = ${allUsersMap.get('13')}"

        editors.each { editor ->
            log.debug "editor = ${editor}"
            //def detailed = authService.getUserForUserId(editor) // currently busted in prod
            def detailed = allUsersMap.get(editor)
            log.debug "editor - detailed = ${detailed}"
            if (detailed) {
                editorsWithDetails.add(detailed)
            } else {
                editorsWithDetails.add([userId: editor, displayName:'', userName: ''])
            }
        }
        editorsWithDetails
    }

    /**
     * Provides (ajax) content for the edit (single species) record model popup
     */
    def editRecordScreen() {
        doEditAddRecordScreen(params)
    }

    /**
     * Provides (ajax) content for adding a (single species) record model popup
     */
    def addRecordScreen() {
        doEditAddRecordScreen(params)
    }

    /**
     * Common code for generating both edit and add record screens
     */
    private doEditAddRecordScreen(params) {
        def sli = null

        if (params.action == "addRecordScreen") {
            def speciesList = SpeciesList.findByDataResourceUid(params.id)
            log.debug "speciesList DRUid = " + speciesList.dataResourceUid
            // create new item (not actually saved in DB)
            sli = new SpeciesListItem(mylist: speciesList, dataResourceUid: speciesList.dataResourceUid)
            log.debug "List's DRUid = " + sli.dataResourceUid
        }
        else  {
            sli = SpeciesListItem.get(params.id)
        }

        if (!sli) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesListItem.label', default: 'Species List Item'), params.id])}"
            //redirect(controller: "public", action: "speciesLists")
            render(view: "editRecordScreen")
        }
        else {
            SpeciesList sl = sli.mylist
            log.debug "Item's list = " + sl.listName
            log.debug "Item's DRUid = " + sli.dataResourceUid
            log.debug "Item's Size = " + sl.items.size()
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=?", sl.dataResourceUid)
            def keyVocabs = [:]
            def kvpMap = [:]
            def kvpOrder = []
            keys.each { key ->
                def vocabValues = SpeciesListKVP.executeQuery("select distinct vocabValue from SpeciesListKVP where dataResourceUid=? and key=?", [sl.dataResourceUid, key])
                log.debug "vocabValues = " + vocabValues + " size: " + vocabValues.size()
                def kvp = SpeciesListKVP.findByDataResourceUidAndKey(sli.dataResourceUid, key)
                kvpOrder.add(kvp.itemOrder)
                log.debug key + ":kvp = " + kvp + " -> itemOrder = ${kvp.itemOrder}"
                if (vocabValues && vocabValues[0]) {
                    // ad blank value to vocabValues for drop-down list to have empty first option
                    vocabValues.add(0, "-- select --")
                    keyVocabs[key] = vocabValues
                    kvpMap[key] = kvp
                }
            }

            log.debug "list's keys = " + keys.join("|")
            log.debug "list's vocabs = " + keyVocabs
            render(view: "editRecordScreen", model: [record: sli, KVPKeys: keys, kvpOrder: kvpOrder, keyVocabs: keyVocabs, kvpMap: kvpMap])
        }
    }

    /**
     * Edit a SpeciesListItem
     */
    def editRecord() {
        def sli = SpeciesListItem.get(params.id)
        log.debug "editRecord params = " + params
        log.debug "sli KVPs = " + sli.kvpValues
        if (sli) {
            // check for changed values
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=?", sli.dataResourceUid)
            def kvpRemoveList = [] as Set

            keys.each { key ->
                def kvp = sli.kvpValues.find { it.key == key } // existing KVP if any

                if (params[key] != kvp?.value) {
                    log.debug "KVP has been changed: " + params[key] + " VS " + kvp?.value
                    def newKvp = SpeciesListKVP.findByDataResourceUidAndKeyAndValue(sli.dataResourceUid, key, params[key])

                    if (kvp) {
                        // old value was not empty - remove from this SLI
                        kvpRemoveList.add(kvp)
                    }

                    if (params[key]) {
                        // new value is empty
                        if (!newKvp) {
                            // There is no existing KVP for the new value
                            log.debug "Couldn't find an existing KVP, so creating a new one..."
                            newKvp = new SpeciesListKVP(
                                    dataResourceUid: sli.dataResourceUid,
                                    key: key,
                                    value: params[key],
                                    itemOrder: kvp?.itemOrder?:0,
                                    SpeciesListItem: sli ).save(failOnError: true, flush: true)
                        }

                        sli.addToKvpValues(newKvp)
                    }
                } else {
                    log.debug "KVP is unchanged: " + kvp.value
                }
            }

            // remove KVP items that have changed (need to do this separately to avoid java.util.ConcurrentModificationException)
            kvpRemoveList.each {
                log.debug "Removing outdated kvp value: ${it}"
                sli.removeFromKvpValues(it)
            }

            //check if rawScientificName has changed
            if (params.rawScientificName.trim() != sli.rawScientificName.trim()) {
                log.debug "rawScientificName is different: " + params.rawScientificName + " VS " + sli.rawScientificName
                sli.rawScientificName = params.rawScientificName
                // lookup guid
                helperService.matchNameToSpeciesListItem(sli.rawScientificName, sli)
                //sli.guid = helperService.findAcceptedLsidByScientificName(sli.rawScientificName)?: helperService.findAcceptedLsidByCommonName(sli.rawScientificName)
            }

            if (!sli.validate()) {
                def message = "Could not update SpeciesListItem: ${sli.rawScientificName} - " + sli.errors.allErrors
                log.error message
                render(text: message, status: 500)
            }
            else if (sli.save(flush: true)) {
                def msg = message(code:'public.lists.view.table.edit.messages', default:'Record successfully created')
                render(text: msg, status: 200)
            }
            else {
                def message = "Could not create SpeciesListItem: ${sli.rawScientificName} - " + sli.errors.allErrors
                render(text: message, status: 500)
            }
        } else {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesListItem.label', default: 'Species List Item'), params.id])}"
            render(text: message, status: 404)
        }
    }

    /**
     * Create a new SpeciesListItem
     */
    def createRecord() {
        def response = helperService.createRecord(params)
        render(text: response.text, status: response.status)
    }

    def deleteRecord() {
        def sli = SpeciesListItem.get(params.id)

        if (sli) {
            // remove attached KVP records
            // two step process to avoid java.util.ConcurrentModificationException
            def kvpRemoveList = [] as Set
            sli.kvpValues.each {
                kvpRemoveList.add(it)
            }
            kvpRemoveList.each {
                sli.removeFromKvpValues(it)
            }

            try {
                sli.delete(flush: true)
                render(text: message(code:'public.lists.view.table.delete.messages', default:'Record successfully deleted'), status: 200)

            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                def message = "Could not delete SpeciesListItem: ${sli.rawScientificName}"
                //redirect(action: "show", id: p.id)
                render(text: message, status: 500)
            }
        } else {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesListItem.label', default: 'Species List Item'), params.id])}"
            render(text: message, status: 404)
        }
    }

    /**
     * webservice to update a list of editors for a given list
     */
    def updateEditors() {
        log.debug "editors param = " + params.'editors[]'
        def speciesList = SpeciesList.findByDataResourceUid(params.id)
        if (!speciesList) {
            render(text: "Requested list with ID " + params.id + " was not found", status: 404);
        } else if (!localAuthService.isAdmin() && speciesList.userId != authService.userId) {
            render(text: "You are not authorised to modify permissions", status: 403 )
        } else {
            speciesList.editors = params.list('editors[]')
            log.debug("editors = " + speciesList.editors);
            if (!speciesList.save(flush: true)) {
                def errors = []
                speciesList.errors.each {
                    errors.add(it)
                }
                render(text: "Error occurred while saving specieslist: " + errors.join("; "), status: 500);
            } else {
                log.info("updated list of editors for id: " + params.id)
                log.info("list: " + params.id)
                render(text: "list.editors successfully updated")
            }
        }
    }

    def editSpeciesList() {
        def speciesList = SpeciesList.get(params.id)
        if (!speciesList) {
            render(text: "Requested list with ID " + params.id + " was not found", status: 404);
        } else if (!localAuthService.isAdmin() && speciesList.userId != authService.userId) {
            render(text: "You are not authorised to modify this list", status: 403 )
        } else {
            // fix date format
            if (params.dateCreated) {
                // assume format of yyyy-mm-dd
                params.dateCreated = new SimpleDateFormat("yyyy-MM-dd").parse(params.dateCreated)
            }
            speciesList.properties = params
            speciesList.lastUpdated = new Date()
            if (!speciesList.save(flush: true)) {
                def errors = []
                speciesList.errors.each {
                    errors.add(it)
                }
                render(text: "Error occurred while saving species list: " + errors.join("; "), status: 500);
            } else {
                render(text: "${message(code:'public.lists.view.listinfo.edit.messages', default:'species list successfully updated - reloading page.')}")
            }
        }
    }

    /**
     * Check if user is either owner, admin or on the specieslist's editors list.
     */
    private isCurrentUserEditorForList(SpeciesList sl) {
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
}
