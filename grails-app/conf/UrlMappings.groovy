class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        //ws to support CRUD operations on lists
        "/ws/speciesList/${druid}?" (controller: 'webService'){
            action = [GET:'getListDetails',POST:'saveList']
        }

        "/ws/speciesListItems/${druid}" (controller: 'webService'){
            action = [GET:'getListItemDetails']
        }

        //ws to obtain values for a specified species guid
        "/ws/species/$guid?" (controller: 'webService',action: 'getListItemsForSpecies')

        "/ws/speciesList/unpublished" (controller: 'webService', action: 'getBieUpdates')

        "/ws/speciesList/$druid/taxa" (controller: 'webService', action:  'getTaxaOnList')

        "/ws/speciesList/publish/$druid?" (controller: 'webService', action:  'markAsPublished')

        "/ws/speciesListItems/distinct/$field?" (controller: 'webService' , action: 'getDistinctValues')

        "/ws"(view:'/webService/ws')

//        "/"(view:"/index")

		"/"(controller: 'public' ,action:  'index')
		"500"(view:'/error')
	}
}
