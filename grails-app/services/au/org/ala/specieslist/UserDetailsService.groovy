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
        String TOTAL = "Number of lists that need to be processed"
        String VALID = "Number of lists with valid usernames."
        String DEFAULT = "Number of lists  where users are not registered but end with ala.org"
        String INVALID = "Number of lists without a username or with a registered username"
        String FAILED = "Number of failed database updates"
        def result = [(TOTAL):0, (VALID):0, (DEFAULT):0, (INVALID):0, (FAILED):0]

        try {
            listsWithoutUserIds = SpeciesList.findAllByUserIdIsNullOrUserId("",[max:9999])
        } catch (Exception ex) {
            log.error "Failed dynamic finder: ${ex}", ex
        }

        if (listsWithoutUserIds.size() > 0) {
            result[TOTAL] = listsWithoutUserIds.size()
            listsWithoutUserIds.each { list ->
                if (list.username) {
                    def user = authService.getUserForEmailAddress(list.username, false)
                    if (user && user.userId) {
                        list.userId = user.userId
                        result[VALID] += 1
                        log.warn "Saving userid: ${user.userId} (${list.username}) to list id: ${list.id}..."

                    } else if (list.username =~ /ala\.org/) {
                        log.error "No user or userId found for username: ${list.username} || user = ${user}"
                        log.error "Setting username to info@ala.org.au"
                        list.username = "info@ala.org.au"
                        list.userId = 2729
                        result[DEFAULT] += 1
                    } else {
                        log.error "No user or userId found for username: ${list.username} || user = ${user}"
                        log.error "Setting username to 0"
                        list.userId = 0
                        result[INVALID] += 1
                    }
                } else {
                    log.error "No user is defined for list: ${list.id}. Set user id to 0 "
                    list.userId = 0
                    result[INVALID] += 1
                }

                try {
                    list.save(flush: true)
                    isSuccessful = true
                }
                catch (Exception e) {
                    // deal with exception
                    result[FAILED] += 1
                    log.error "Error saving list: ${list.id} : ${e}", e
                }
            }
        }
        result
    }

}
