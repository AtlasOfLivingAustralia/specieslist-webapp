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

import au.org.ala.web.AuthService

class SecurityUtil {

    LocalAuthService localAuthService
    AuthService authService

    boolean checkViewAccess(String druid, request, response) {
        boolean canAccess = true

        if (!localAuthService.isAdmin()) {
            SpeciesList list = SpeciesList.findByDataResourceUid(druid)
            if (list?.isPrivate) {
                String userId = authService.getUserId()
                // use JWT token for userId. Not role or scope.
                if (!userId) userId = localAuthService.getJwtUserId(request, response)
                if (!userId || (list.userId != userId && !list.editors?.contains(userId)) ) {
                    canAccess = false
                }
            }
        }

        canAccess
    }

    boolean checkEditAccess(String druid, request, response) {
        boolean canAccess = true

        if (!localAuthService.isAdmin()) {
            SpeciesList list = SpeciesList.findByDataResourceUid(druid)
            String userId = authService.getUserId()
            // use JWT token for userId. Not role or scope.
            if (!userId) userId = localAuthService.getJwtUserId(request, response)
            if (!userId || (list.userId != userId && !list.editors?.contains(userId))) {
                canAccess = false
            }
        }

        canAccess
    }

    boolean checkListDeletePermission(String listId, request, response){
        boolean  canDelete  = false

        if(localAuthService.isAdmin()) {
            canDelete = true
        } else{
            SpeciesList list = SpeciesList.get(listId)
            String userId = authService.getUserId()
            // use JWT token for userId. Not role or scope.
            if (!userId) userId = localAuthService.getJwtUserId(request, response)
            // check if list exists and the userId is not null.
            if(list && userId){
                // can delete if the lists user matches authenticated user id OR if list's editor list contains authenticated user id.
                canDelete  = (list.userId == userId) || list.editors?.contains(userId)
            }
        }

        canDelete
    }
}
