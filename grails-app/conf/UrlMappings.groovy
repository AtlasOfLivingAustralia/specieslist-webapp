class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
        "/species/$guid"(controller: "species", action: "show")
        "/search"(controller: "species", action: "search")
        "/image-search/showSpecies"(controller: "species", action: "imageSearch")
        "/image-search/infoBox"(controller: "species", action: "infoBox")
        "/bhl-search"(controller: "species", action: "bhlSearch")
        "/logout"(controller: "species", action: "logout")
		"/"(view:"/home")
		"500"(view:'/error')
	}
}
