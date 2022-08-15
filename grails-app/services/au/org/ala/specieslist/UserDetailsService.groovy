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

import au.org.ala.web.UserDetails
import grails.gorm.transactions.Transactional
import org.springframework.dao.DataIntegrityViolationException

@Transactional
class UserDetailsService {
    def authService


    UserDetails getUserDetailsById(String id){
        authService.getUserForUserId(id)
    }

    UserDetails getCurrentUserDetails(){
        authService.userDetails()

    }

    @Transactional
    def updateSpeciesListUserDetails() {
        List listsWithoutUserIds = []
        Boolean isSuccessful =  false

        try {
            listsWithoutUserIds = SpeciesList.findAllByUserIdIsNullOrUserId("",[max:9999])
            //listsWithoutUserIds = SpeciesList.findAll("from SpeciesList where userId is null" ,[max:9999])
        } catch (Exception ex) {
            log.error "Failed dynamic finder: ${ex}", ex
        }

        if (listsWithoutUserIds.size() > 0) {
            Map userNamesLookup = authService.getAllUserNameMap()
            //log.debug "userNamesLookup = ${userNamesLookup}"

            listsWithoutUserIds.each { list ->
                def user = userNamesLookup.get(list.username?.toLowerCase()) // should be cached after the first call

                if (user && user.userId) {
                    list.userId = user.userId
                    log.warn "Saving userid: ${user.userId} (${list.username}) to list id: ${list.id}..."

                    try {
                        list.save(flush: true)
                        isSuccessful = true
                    }
                    catch (DataIntegrityViolationException e) {
                        // deal with exception
                        log.error "Error saving list: ${e}", e
                    }
                } else if (list.username =~ /ala\.org/) {
                    log.error "No user or userId found for username: ${list.username} || user = ${user}"
                    log.error "Setting username to info@ala.org.au"
                    list.username = "info@ala.org.au"
                    list.userId = 2729
                    try {
                        list.save(flush: true)
                        isSuccessful = true
                    }
                    catch (DataIntegrityViolationException e) {
                        // deal with exception
                        log.error "Error saving list: ${e}", e
                    }
                } else {
                    log.error "No user or userId found for username: ${list.username} || user = ${user}"
                    log.error "Setting username to 0"
                    list.userId = 0
                    try {
                        list.save(flush: true)
                        isSuccessful = true
                    }
                    catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // deal with exception
                        log.error "Error saving list: ${e}", e
                    }
                }
            }
        } else {
            log.warn "All lists have userId values: ${listsWithoutUserIds}"
            isSuccessful = true
        }

        isSuccessful
    }

    @Transactional
    def updateEditorsList() {
        def listsEditorsWithoutUserIds
        def returnBool = false

        try {
            listsEditorsWithoutUserIds = SpeciesList.findAllByEditorsIsNotNull([max:9999])
        } catch (Exception ex) {
            log.error "Failed dynamic finder: ${ex}", ex
        }

        if (listsEditorsWithoutUserIds.size() > 0) {
            Map userNamesLookup = authService.getAllUserNameMap()

            listsEditorsWithoutUserIds.each { list ->
                def newEditors = []

                list.editors.each { e ->

                    def users = e.tokenize(",") // comma separated doh!

                    users.each { u ->
                        if (u =~ /\D+/) {
                            def user = userNamesLookup.get(u?.toLowerCase()) // should be cached after the first call
                            if (user && user.userId) {
                                newEditors.add(user.userId.toString())
                            } else {
                                log.error "No userId found for address: ${u?.toLowerCase()}"

                                if (u) {
                                    newEditors.add(u?.toLowerCase()) // unknown user
                                }
                            }
                        } else {
                            newEditors.add(u?.toLowerCase()) // pass though for existing numeric IDs
                        }

                    }
                }

                log.warn "Saving editors with userIds: ${newEditors} to list id: ${list.id}..."
                list.editors = newEditors

                try {
                    list.save(flush: true)
                    returnBool = true
                }
                catch (DataIntegrityViolationException e) {
                    // deal with exception
                    log.error "Error saving list: ${e}", e
                }
            }
        }

        returnBool
    }

}
