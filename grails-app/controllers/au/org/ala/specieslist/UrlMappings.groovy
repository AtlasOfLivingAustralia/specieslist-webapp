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

class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
        "/iconic-species" controller: "speciesListItem", action: "iconicSpecies"
        //ws to support CRUD operations on lists
        "/ws/speciesList/${druid}?" (controller: 'webService'){
            action = [GET:'getListDetails',POST:'saveList']
        }

        "/ws/speciesListPost/${druid}?" (controller: 'webService'){
            action = [POST:'saveList']
        }

        "/ws/speciesListItems/${druid}?" (controller: 'webService'){
            action = [GET:'getListItemDetails']
        }

        "/ws/speciesListItemsInternal/${druid}?" (controller: 'webService'){
            action = [GET:'getListItemDetailsInternal']
        }

        "/ws/speciesListInternal/${druid}?" (controller: 'webService'){
            action = [GET:'getListDetailsInternal']
        }

        "/ws/speciesListInternal/download/$druid" (controller: 'webService', action:  'downloadListInternal')

        "/ws/createItem" (controller: 'webService', action: 'createItem')

        "/ws/deleteItem" (controller: 'webService', action: 'deleteItem')

        "/ws/queryListItemOrKVP" (controller: 'webService'){
            action = [GET:'queryListItemOrKVP']
        }

        //"/ws/speciesListItems" (controller: "webService", action: "getListItemDetails")
        "/ws/findSpeciesByName" (controller: "webService", action: "findSpeciesByName")
        "/ws/speciesListItems/keys" (controller: "webService", action: "listKeys")
        "/ws/speciesListItems/byKeys" (controller: "webService", action: "listItemsByKeys")

        "/ws/listCommonKeys" (controller: "webService", action: "listCommonKeys")

        "/ws/speciesList/filter" (controller: "webService", action: [POST: "filterLists"])

        //ws to obtain values for a specified species guid
        "/ws/species/$guid**?" (controller: 'webService',action: 'getListItemsForSpecies')

        "/ws/speciesList/unpublished" (controller: 'webService', action: 'getBieUpdates')

        "/ws/speciesList/$druid/taxa" (controller: 'webService', action:  'getTaxaOnList')

        "/ws/speciesList/publish/$druid?" (controller: 'webService', action:  'markAsPublished')

        "/ws/speciesListItems/distinct/$field?" (controller: 'webService' , action: 'getDistinctValues')

        "/ws"(view:'/webService/ws')

        "/ws/speciesListItemKvp/$druid" (controller: 'webService'){
            action = [GET: 'getSpeciesListItemKvp']
        }

        "/ws/rematchLogs" (controller: "webService", action: "rematchLogs")

            "/ws/rematchLog/$id"(controller: 'webService') {
                    action = [GET: 'rematchLog', DELETE: 'deleteRematchLog']
            }

//        "/ws/deleteRematchLog/$id" (controller: "webService", action: "deleteRematchLog")
//
//        "/ws/downloadRematchLog/$id" (controller: "webService", action: "downloadRematchLog")

		"/"(controller: 'public' ,action:  'index')
		"500"(view:'/error')
        "404"(view:'/404')
		"401"(view:'/notAuthorised')
	}
}
