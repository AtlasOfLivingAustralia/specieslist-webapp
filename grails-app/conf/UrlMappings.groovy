class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
        // Redirects for BIE web services URLs
        "/search.json"(controller: "legacyWebServices", action: "searchJson")
        "/search.xml"(controller: "legacyWebServices", action: "searchXml")
        "/search/auto.json"(controller: "legacyWebServices", action: "autoCompleteJson")
        "/search/auto.jsonp"(controller: "legacyWebServices", action: "autoCompleteJson")
        //"/species/bulklookup.json"(controller: "redirect", action: "speciesJsonPost")
        //"/species/guids/bulklookup.json"(controller: "redirect", action: "speciesJsonPost"){ guids = true }
        "/species/${guid}.json"(controller: "legacyWebServices", action: "speciesJson")
        "/species/${guid}.xml"(controller: "legacyWebServices", action: "speciesXml")
        "/species/$path1/${guid}.json"(controller: "legacyWebServices", action: "speciesJson")
        "/image-search/${type}.json"(controller: "legacyWebServices", action: "imageSearchJson")
        "/rankTaxonImage.json"(controller: "legacyWebServices", action: "imageRankJson"){ withUser = false }
        "/rankTaxonImageWithUser.json"(controller: "legacyWebServices", action: "imageRankJson"){ withUser = true }
        "/rankTaxonCommonName.json"(controller: "legacyWebServices", action: "nameRankJson"){ withUser = false }
        "/rankTaxonCommonNameWithUser.json"(controller: "legacyWebServices", action: "nameRankJson"){ withUser = true }
        "/species/$path1/$path2/${guid}.json"(controller: "legacyWebServices", action: "speciesJson")
        "/admin/${path}**"(controller: "legacyWebServices", action: "admin")
        // webapp URLs
        "/species/$guid"(controller: "species", action: "show")
        "/search"(controller: "species", action: "search")
        "/image-search/showSpecies"(controller: "species", action: "imageSearch")
        "/image-search/infoBox"(controller: "species", action: "infoBox")
        "/bhl-search"(controller: "species", action: "bhlSearch")
        "/sound-search"(controller: "species", action: "soundSearch")
        "/logout"(controller: "species", action: "logout")
		"/"(view:"/home")
		"500"(view:'/error')
	}
}
