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

    boolean checkListAccess(String listId) {
        boolean canAccess = true

        if (!localAuthService.isAdmin()) {
            SpeciesList list = SpeciesList.findByDataResourceUid(listId)
            if (list?.isPrivate) {
                String userId = authService.getUserId()
                if (!userId || (list.userId != userId && !list.editors?.contains(userId))) {
                    canAccess = false
                }
            }
        }

        canAccess
    }
}
