package au.org.ala.bie.webapp2



import grails.test.mixin.*
import grails.converters.JSON

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(UtilityService)
class UtilityServiceTests {
    def utilityService

    def etc1String = "{commonNames\": [\n" +
            "\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": \"urn:lsid:biodiversity.org.au:afd.name:277712\",\n" +
            "        \"ranking\": 8,\n" +
            "        \"noOfRankings\": 8,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/31a9b8b8-4e8f-4343-a15f-2ed24e0bf1ae\",\n" +
            "        \"infoSourceURL\": \"http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/31a9b8b8-4e8f-4343-a15f-2ed24e0bf1ae\",\n" +
            "        \"infoSourceName\": \"Australian Faunal Directory\",\n" +
            "        \"infoSourceId\": \"1\",\n" +
            "        \"infoSourceUid\": null,\n" +
            "        \"title\": null,\n" +
            "        \"documentId\": null\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": null,\n" +
            "        \"infoSourceURL\": \"http://www.iucn.org/\",\n" +
            "        \"infoSourceName\": \"International Union for Conservation of Nature\",\n" +
            "        \"infoSourceId\": \"510\",\n" +
            "        \"infoSourceUid\": null,\n" +
            "        \"title\": null,\n" +
            "        \"documentId\": null\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#68\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617758\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#69\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617759\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#71\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617761\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#72\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617762\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#66\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617756\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#63\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617752\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#73\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617763\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#67\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617757\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.auswildlife.com/marsupials-monotremes/#65\",\n" +
            "        \"infoSourceURL\": \"http://www.auswildlife.com/\",\n" +
            "        \"infoSourceName\": \"Aus Wild Life \",\n" +
            "        \"infoSourceId\": \"1078\",\n" +
            "        \"infoSourceUid\": \"dr457\",\n" +
            "        \"title\": \" Macropus rufus\",\n" +
            "        \"documentId\": \"1617755\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://bie.ala.org.au/uploads/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f5371291180637011\",\n" +
            "        \"infoSourceURL\": \"http://www.ala.org.au\",\n" +
            "        \"infoSourceName\": \"ALA website image uploads\",\n" +
            "        \"infoSourceId\": \"1061\",\n" +
            "        \"infoSourceUid\": \"dr440\",\n" +
            "        \"title\": \"Red Kangaroo Test 6\",\n" +
            "        \"documentId\": \"1282746\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://bie.ala.org.au/uploads/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537^:^1324250592162\",\n" +
            "        \"infoSourceURL\": \"http://www.ala.org.au\",\n" +
            "        \"infoSourceName\": \"ALA website image uploads\",\n" +
            "        \"infoSourceId\": \"1061\",\n" +
            "        \"infoSourceUid\": \"dr440\",\n" +
            "        \"title\": \"Macropus rufus male and female\",\n" +
            "        \"documentId\": \"1754226\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://bie.ala.org.au/uploads/urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537^:^1324250836910\",\n" +
            "        \"infoSourceURL\": \"http://www.ala.org.au\",\n" +
            "        \"infoSourceName\": \"ALA website image uploads\",\n" +
            "        \"infoSourceId\": \"1061\",\n" +
            "        \"infoSourceUid\": \"dr440\",\n" +
            "        \"title\": \"Macropus rufus male and female\",\n" +
            "        \"documentId\": \"1754227\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Red Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": 6,\n" +
            "        \"noOfRankings\": 6,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.catalogueoflife.org/commonNames\",\n" +
            "        \"infoSourceURL\": \"http://www.catalogueoflife.org/\",\n" +
            "        \"infoSourceName\": \"Catalogue of Life: 2010 Annual Checklist\",\n" +
            "        \"infoSourceId\": \"3\",\n" +
            "        \"infoSourceUid\": null,\n" +
            "        \"title\": null,\n" +
            "        \"documentId\": \"36\"\n" +
            "    },\n" +
            "    {\n" +
            "        \"preferred\": false,\n" +
            "        \"isBlackListed\": false,\n" +
            "        \"nameString\": \"Plains Kangaroo\",\n" +
            "        \"guid\": null,\n" +
            "        \"ranking\": -1,\n" +
            "        \"noOfRankings\": 1,\n" +
            "        \"isPreferred\": false,\n" +
            "        \"identifier\": \"http://www.eol.org/api/pages/1.0/122672?common_names=1&details=1&images=2&subjects=all&text=2\",\n" +
            "        \"infoSourceURL\": \"http://www.eol.org/\",\n" +
            "        \"infoSourceName\": \"Encyclopedia of Life\",\n" +
            "        \"infoSourceId\": \"1051\",\n" +
            "        \"infoSourceUid\": \"dr430\",\n" +
            "        \"title\": \"Macropus rufus (Desmarest, 1822)\",\n" +
            "        \"documentId\": \"620925\"\n" +
            "    }\n" +
            "\n" +
            "]}"
    def etc1 = JSON.parse(etc1String)

    void testUnDuplicateNames() {
        def commonNamesDeduped = utilityService.unDuplicateNames(etc1.commonNames)
        log.info "commonNamesDeduped = " + commonNamesDeduped
    }
}
