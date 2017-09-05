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

        "/ws/queryListItemOrKVP" (controller: 'webService'){
            action = [GET:'queryListItemOrKVP']
        }

        //"/ws/speciesListItems" (controller: "webService", action: "getListItemDetails")
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

        "/ws/checkEmailExists" (controller: 'webService' , action: 'checkEmailExists')

        "/ws/speciesListItemKvp/$druid" (controller: 'webService'){
            action = [GET: 'getSpeciesListItemKvp']
        }

//        "/"(view:"/index")

		"/"(controller: 'public' ,action:  'index')
		"500"(view:'/error')
		"401"(view:'/notAuthorised')
	}
}
