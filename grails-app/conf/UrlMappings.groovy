class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        //ws to obtain values for a specified species guid
        "/ws/species/$guid?" (controller: 'webService',action: 'getListItemsForSpecies')

        "/ws/speciesList/unpublished" (controller: 'webService', action: 'getBieUpdates')

        "/ws/speciesList/$druid/taxa" (controller: 'webService', action:  'getTaxaOnList')

        "/ws/speciesList/publish/$druid?" (controller: 'webService', action:  'markAsPublished')

//        "/"(view:"/index")

		"/"(controller: 'public' ,action:  'index')
		"500"(view:'/error')
	}
}
