class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

//        "/species/$guid"{
//            controller = "species"
//            action = "show"
//        }
        "/species/$guid"(controller: "species", action: "show")
        "/logout"(controller: "species", action: "logout")
		"/"(view:"/index")
		"500"(view:'/error')
	}
}
