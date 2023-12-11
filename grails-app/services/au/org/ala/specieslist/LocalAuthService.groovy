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

import au.org.ala.ws.security.client.AlaAuthClient
import org.pac4j.core.config.Config
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.UserProfile
import org.pac4j.core.util.FindBest
import org.pac4j.jee.context.JEEContextFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LocalAuthService {
    public static final  String ROLE_ADMIN="ROLE_ADMIN"

    def grailsApplication, authService

    @Autowired
    AlaAuthClient alaAuthClient

    @Autowired
    Config config

    def email(){
        authService.getEmail() ?:authService.getUserName()
    }

    def firstname(){
        authService.getFirstName()
    }

    def surname(){
        authService.getLastName()
    }

    def isAdmin() {
        return (grailsApplication.config.security.cas.bypass).toBoolean() || authService.userInRole(ROLE_ADMIN)
    }

    UserProfile checkJWT(HttpServletRequest request, HttpServletResponse response) {
        def sessionStore = org.pac4j.jee.context.session.JEESessionStoreFactory.INSTANCE.newSessionStore()
        WebContext ctx = FindBest.webContextFactory(null, config, JEEContextFactory.INSTANCE).newContext(request, response)
        Optional<Credentials> credentials = alaAuthClient.getCredentials(ctx, sessionStore)

        Optional<UserProfile> userProfile = alaAuthClient.getUserProfile(credentials.get(), ctx, sessionStore)

        if (userProfile.isPresent()) {
            return userProfile.get()
        } else {
            return null
        }
    }

    def getJwtUserId(HttpServletRequest request, HttpServletResponse response){
        def bearer = request?.getHeader("Authorization")
        if (bearer) {
            UserProfile up = checkJWT(request, response)
            if (up) {
                return up.userId
            }
        }

        return null
    }
}
