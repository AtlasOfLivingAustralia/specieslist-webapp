package au.org.ala.specieslist

//import au.org.ala.names.model.NameSearchResult
import au.org.ala.names.ws.api.NameUsageMatch
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Ignore
/**
 * Tests to compare name match results from the local instance and from namematching-ws.ala.org.au
 * To run
 * <ul>
 *
 * <li> uncomment lines with NameSearchResult
 * <li> Add dependency to build.gradle
 *     compile ('au.org.ala:ala-name-matching:3.4') {
 *         exclude module: "log4j"
 *         exclude module: "slf4j-log4j12"
 *         exclude group: 'org.apache.bval', module: 'org.apache.bval.bundle'
 *     }
 *
 * <li> Replace (HelperService) relevant methods with the ones at the bottom of this class
 *
 * <li> Add config to external config file or in application.groovy
 *      bie.nameIndexLocation = "/data/lucene/namematching"
 * </ul>
 */
class HelperServiceSpec extends Specification {

    @Autowired HelperService helperService

    void setup() {}

    void tearDown() {}

    void "test findAcceptedLsidByCommonName"() {
        when:
        def lsidOld = helperService.findAcceptedLsidByCommonName("Red Kangaroo")
        def lsidNew = helperService.nameExplorerService.searchForLsidByCommonName("Red Kangaroo")

        then:
        assert lsidOld == lsidNew
    }

    void "test findAcceptedLsidByScientificName"() {
        when:
        def lsidOld = helperService.findAcceptedLsidByScientificName("Osphranter rufus")
        def lsidNew = helperService.nameExplorerService.searchForAcceptedLsidByScientificName("Osphranter rufus")

        then:
        assert lsidOld == lsidNew
    }

    void "test findAcceptedConceptByLSID" () {
        when:
//        NameSearchResult oldVal = helperService.findAcceptedConceptByLSID("urn:lsid:biodiversity.org.au:afd.taxon:9abbca14-edaa-4f2f-862b-7a628af7d156")
        NameUsageMatch newVal = helperService.nameExplorerService.searchForRecordByLsid("urn:lsid:biodiversity.org.au:afd.taxon:9abbca14-edaa-4f2f-862b-7a628af7d156")

        then:
        assert oldVal.getLsid() == newVal.taxonConceptID
        assert oldVal.getRankClassification().getFamily() == newVal.family
        assert oldVal.getRankClassification().getScientificName() == newVal.scientificName
        assert oldVal.getRankClassification().getAuthorship() == newVal.scientificNameAuthorship
    }

    void "test findAcceptedConceptByNameFamily" () {
        when:
//        NameSearchResult oldVal = helperService.findAcceptedConceptByNameFamily("Anas superciliosa superciliosa", "Anatidae")//"Dentimitrella austrina", "Columbellidae")
        NameUsageMatch newVal = helperService.nameExplorerService.searchForRecordByNameFamily("Anas superciliosa superciliosa", "Anatidae")//"Dentimitrella austrina", "Columbellidae")

        then:
        assert oldVal.getLsid() == newVal.taxonConceptID
        assert oldVal.getRankClassification().getFamily() == newVal.family
        assert oldVal.getRankClassification().getScientificName() == newVal.scientificName
        assert oldVal.getRankClassification().getAuthorship() == newVal.scientificNameAuthorship
    }

    void "test findAcceptedConceptByScientificName" () {
        when:
//        NameSearchResult oldVal = helperService.findAcceptedConceptByScientificName("Anas superciliosa superciliosa")
        NameUsageMatch newVal = helperService.nameExplorerService.searchForRecordByScientificName("Anas superciliosa superciliosa")

        then:
        assert oldVal.getLsid() == newVal.taxonConceptID
        assert oldVal.getRankClassification().getFamily() == newVal.family
        assert oldVal.getRankClassification().getScientificName() == newVal.scientificName
        assert oldVal.getRankClassification().getAuthorship() == newVal.scientificNameAuthorship
    }

    void "test findAcceptedConceptByCommonName" () {
        when:
//        NameSearchResult oldVal = helperService.findAcceptedConceptByCommonName("Red Kangaroo")
        NameUsageMatch newVal = helperService.nameExplorerService.searchForRecordByCommonName("Red Kangaroo")

        then:
        assert oldVal.getLsid() == newVal.taxonConceptID
        assert oldVal.getRankClassification().getFamily() == newVal.family
        assert oldVal.getRankClassification().getScientificName() == newVal.scientificName
        assert oldVal.getRankClassification().getAuthorship() == newVal.scientificNameAuthorship
    }

//R
//    def getNameSearcher(){
//        if(!cbIdxSearcher) {
//            cbIdxSearcher = new ALANameSearcher(grailsApplication.config.bie.nameIndexLocation)
//        }
//        cbIdxSearcher
//    }
//    //R
//    def findAcceptedLsidByCommonName(commonName){
//        String lsid = null
//        try {
//            lsid = getNameSearcher().searchForLSIDCommonName(commonName)
//        } catch(e){
//            log.error("findAcceptedLsidByCommonName -  " + e.getMessage())
//        }
//        lsid
//    }
//
//    //R
//    def findAcceptedLsidByScientificName(scientificName){
//        String lsid = null
//        try{
//            def cl = new LinnaeanRankClassification()
//            cl.setScientificName(scientificName)
//            lsid = getNameSearcher().searchForAcceptedLsidDefaultHandling(cl, true);
//        } catch(Exception e){
//             log.error(e.getMessage())
//        }
//        lsid
//    }
//
//    //R
//    def findAcceptedConceptByLSID(lsid){
//        NameSearchResult nameSearchRecord
//        try{
//            nameSearchRecord = getNameSearcher().searchForRecordByLsid(lsid)
//        }
//        catch(Exception e){
//            log.error(e.getMessage())
//        }
//        nameSearchRecord
//    }
//
//    //R
//    def findAcceptedConceptByNameFamily(String scientificName, String family) {
//        NameSearchResult nameSearchRecord
//        try{
//            def cl = new LinnaeanRankClassification()
//            cl.setScientificName(scientificName)
//            cl.setFamily(family)
//            nameSearchRecord = getNameSearcher().searchForAcceptedRecordDefaultHandling(cl, true)
//        }
//        catch(Exception e){
//            log.error(e.getMessage())
//        }
//        nameSearchRecord
//    }
//
//    //R
//    def findAcceptedConceptByScientificName(scientificName){
//        NameSearchResult nameSearchRecord
//        try{
//            def cl = new LinnaeanRankClassification()
//            cl.setScientificName(scientificName)
//            nameSearchRecord = getNameSearcher().searchForAcceptedRecordDefaultHandling(cl, true)
//        }
//        catch(Exception e){
//            log.error(e.getMessage())
//        }
//        nameSearchRecord
//    }
//
//    //R
//    def findAcceptedConceptByCommonName(commonName){
//        NameSearchResult nameSearchRecord
//        try{
//            nameSearchRecord = getNameSearcher().searchForCommonName(commonName)
//        }
//        catch(Exception e){
//            log.error(e.getMessage())
//        }
//        nameSearchRecord
//    }

}
