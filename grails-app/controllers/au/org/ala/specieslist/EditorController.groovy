package au.org.ala.specieslist

import java.text.SimpleDateFormat

class EditorController {
    def authService
    def helperService

    /**
     * Provides (ajax) content for the edit permissions modal popup
     */
    def editPermissions() {
        def speciesList = SpeciesList.findByDataResourceUid(params.id)
        log.debug "logged in user: " + authService.email()
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
            render(view: "permissions", model: [speciesList: speciesList, mapOfUserNamesById: authService.getMapOFAllUserNamesById()])
        }
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
            sli = new SpeciesListItem(list: speciesList, dataResourceUid: speciesList.dataResourceUid)
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
            SpeciesList sl = sli.list
            log.debug "Item's list = " + sl.listName
            log.debug "Item's DRUid = " + sli.dataResourceUid
            log.debug "Item's Size = " + sl.items.size()
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=?", sl.dataResourceUid)
            def keyVocabs = [:]
            def kvpMap = [:]
            keys.each { key ->
                def vocabValues = SpeciesListKVP.executeQuery("select distinct vocabValue from SpeciesListKVP where dataResourceUid=? and key=?", [sl.dataResourceUid, key])
                log.debug "vocabValues = " + vocabValues + " size: " + vocabValues.size()
                def kvp = SpeciesListKVP.findAllByDataResourceUidAndKey(sli.dataResourceUid, key)
                log.debug key + ":kvp = " + kvp
                if (vocabValues && vocabValues[0]) {
                    // ad blank value to vocabValues for drop-down list to have empty first option
                    vocabValues.add(0, "-- select --")
                    keyVocabs[key] = vocabValues
                    kvpMap[key] = kvp
                }
            }

            log.debug "list's keys = " + keys.join("|")
            log.debug "list's vocabs = " + keyVocabs
            render(view: "editRecordScreen", model: [record: sli, KVPKeys: keys, keyVocabs: keyVocabs, kvpMap: kvpMap])
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
                            newKvp = new SpeciesListKVP(dataResourceUid: sli.dataResourceUid, key: key, value: params[key], SpeciesListItem: sli )
                        }

                        sli.addToKvpValues(newKvp)
                    }
                } else {
                    log.debug "KVP is unchanged: " + kvp.value
                }
            }

            // remove KVP items that have changed (need to do this separately to avoid java.util.ConcurrentModificationException)
            kvpRemoveList.each {
                sli.removeFromKvpValues(it)
            }

            //check if rawScientificName has changed
            if (params.rawScientificName != sli.rawScientificName) {
                log.debug "rawScientificName is different: " + params.rawScientificName + " VS " + sli.rawScientificName
                sli.rawScientificName = params.rawScientificName
                // lookup guid
                sli.guid = helperService.findAcceptedLsidByScientificName(sli.rawScientificName)?: helperService.findAcceptedLsidByCommonName(sli.rawScientificName)
            }

            if (!sli.validate()) {
                def message = "Could not update SpeciesListItem: ${sli.rawScientificName} - " + sli.errors.allErrors
                log.error message
                render(text: message, status: 500)
            }
            else if (sli.save(flush: true)) {
                render(text: "Record successfully created", status: 200)
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
        def sl = SpeciesList.get(params.id)
        log.debug "params = " + params

        if (!params.rawScientificName) {
            render(text: "Missing required field: rawScientificName", status: 400)
        }
        else if (sl) {
            def keys = SpeciesListKVP.executeQuery("select distinct key from SpeciesListKVP where dataResourceUid=?", sl.dataResourceUid)
            log.debug "keys = " + keys
            def sli = new SpeciesListItem(dataResourceUid: sl.dataResourceUid, rawScientificName: params.rawScientificName, itemOrder: sl.items.size() + 1)
            sli.guid = helperService.findAcceptedLsidByScientificName(sli.rawScientificName)?: helperService.findAcceptedLsidByCommonName(sli.rawScientificName)

            keys.each { key ->
                log.debug "key: " + key + " has value: " + params[key]
                def value = params[key]
                if (value) {
                    def newKvp = SpeciesListKVP.findByDataResourceUidAndKeyAndValue(sl.dataResourceUid, key, value)
                    if (!newKvp) {
                        log.debug "Couldn't find an existing KVP, so creating a new one..."
                        newKvp = new SpeciesListKVP(dataResourceUid: sli.dataResourceUid, key: key, value: params[key], SpeciesListItem: sli );
                    }

                    sli.addToKvpValues(newKvp)
                }
            }

            sl.addToItems(sli)

            if (!sl.validate()) {
                def message = "Could not update SpeciesList with new item: ${sli.rawScientificName} - " + sl.errors.allErrors
                log.error message
                render(text: message, status: 500)
            }
            else if (sl.save(flush: true)) {
                render(text: "Record successfully created", status: 200)
            }
            else {
                def message = "Could not create SpeciesListItem: ${sli.rawScientificName} - " + sl.errors.allErrors
                render(text: message, status: 500)
            }
        }
        else {
            def message = "${message(code: 'default.not.found.message', args: [message(code: 'speciesList.label', default: 'Species List'), params.id])}"
            render(text: message, status: 404)
        }
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
                render(text: "Record successfully deleted", status: 200)
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
        } else if (!authService.isAdmin() && speciesList.username != authService.email()) {
            render(text: "You are not authorised to modify permissions", status: 403 )
        } else {
            def editorsList = []
            editorsList.addAll(params.'editors[]')
            log.debug "params.editors[] = " + params.'editors[]'
            log.debug "editorsList = " + editorsList
            speciesList.editors = editorsList
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
        } else if (!authService.isAdmin() && speciesList.username != authService.email()) {
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
                render(text: "species list successfully updated")
            }
        }
    }

    /**
     * Check if user is either owner, admin or on the specieslist's editors list.
     */
    private isCurrentUserEditorForList(SpeciesList sl) {
        def isAllowed = false
        def loggedInUser = authService.email()
        log.debug "Checking isCurrentUserEditorForList: loggedInUser = " + loggedInUser
        if (!sl) {
            log.debug "speciesList is null"
            isAllowed = false // saves repeating this check in subsequent else if
        } else if (sl.username == loggedInUser) {
            log.debug "user is owner"
            isAllowed = true
        } else if (authService.isAdmin()) {
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
