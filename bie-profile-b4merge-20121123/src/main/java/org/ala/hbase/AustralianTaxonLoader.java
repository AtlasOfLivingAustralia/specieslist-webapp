package org.ala.hbase;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.regex.Pattern;

import org.ala.dto.AustralianDTO;
import org.ala.client.util.RestfulClient;
import org.ala.dao.Scanner;
import org.ala.dao.StoreHelper;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Marks up taxon as Australian if they are of "interest" to people in Australia.
 *
 * Australian taxon concepts are identified in 2 manners:
 * - concepts sourced from AFD, APC or APNI
 * - concepts that have occurrence records from geographic regions in Australia
 *
 * The "isAustralian" status is determined from biocache-service.  In the future
 * we may need to store the AustralianDTO object in Cassandra...
 *
 * @author Natasha Carter (Natasha.Carter@csiro.au)
 */
@Component("australianTaxonLoader")
public class AustralianTaxonLoader {

    protected static Logger logger = Logger.getLogger(AustralianTaxonLoader.class);

    @Inject
    protected TaxonConceptDao taxonConceptDao;
    
    @Inject
    protected StoreHelper storeHelper;
    
    protected Pattern austLsidPattern = Pattern.compile("urn:lsid:biodiversity.org.au[a-zA-Z0-9\\.:-]*");
    
    public static final String WS_URL = "http://biocache.ala.org.au/ws/australian/taxon/";


    public static final String AUST_GUID_FILE = "/data/bie-staging/biocache/austTaxonConcepts.txt";
    
    public static final String REINDEX_FILE = "reindex_file.txt";
    
  //create restful client with no connection timeout.
    protected RestfulClient restfulClient = new RestfulClient(0);


    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        AustralianTaxonLoader l = context.getBean(AustralianTaxonLoader.class);
        l.load();
        System.exit(0);
    }
    
    public void load() throws Exception {
        Scanner scanner = storeHelper.getScanner("bie", "tc", "", "taxonConcept", "hasGeoReferencedRecords");
        FileOutputStream fos =FileUtils.openOutputStream(new File(REINDEX_FILE));
        byte[] guidAsBytes = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false); 
        int total = 0;
        int austCount=0;
//      Map<String, String> uidInfosourceIDMap = getUidInfosourceIdMap();

      while ((guidAsBytes = scanner.getNextGuid()) != null) {
          //if the guid is biodiversity.org set true otherwise query the biocache ws
          boolean isAust = false;
          String guid = new String(guidAsBytes);   
          if(scanner.getCurrentValues().containsKey("taxonConcept")){
              
              if(austLsidPattern.matcher(guid).matches())
                  isAust = true;
              else{
                  if(scanner.getCurrentValues().get("hasGeoReferencedRecords") != null){
                      Object[] resp = restfulClient.restGet(WS_URL+guid);
                      if((Integer)resp[0] == HttpStatus.SC_OK){
                          String content = resp[1].toString();
                          if(content != null && content.length()>3){
                              AustralianDTO adto = mapper.readValue(content, AustralianDTO.class);
                              if(adto != null)
                                  isAust = adto.getIsAustralian();
                          }
                      }
                  }
              }
//              else{
//                  Object[] resp = restfulClient.restGet(WS_URL+guid);
//                  if((Integer)resp[0] == HttpStatus.SC_OK){
//                      String content = resp[1].toString();
//                      if(content != null && content.length()>3){
//                          AustralianDTO adto = mapper.readValue(content, AustralianDTO.class);
//                          if(adto != null)
//                              isAust = adto.getIsAustralian();
//                      }
//                  }
//    
//              }
          }
          total++;
          if(total %10000 == 0)
              System.out.println("Processed " + total + " last id " + guid);
          if(isAust){
              //load the value in cassandra and then add to doc to reindex the concept
              taxonConceptDao.setIsAustralian(guid, true, false);
              fos.write((guid + "\n").getBytes());
              austCount++;
          }
              
      }
      
      fos.flush();
      fos.close();
      
      System.out.println("Total " + total + " australian: " + austCount);
//        logger.info("Starting to mark up the Australian Concepts...");
//        BufferedReader reader = new BufferedReader(new FileReader(AUST_GUID_FILE));
//        String guid = null;
//        while ((guid = reader.readLine()) != null) {
//        	String taxonGuid = taxonConceptDao.getPreferredGuid(guid);
//            taxonConceptDao.setIsAustralian(taxonGuid);
//        }
//        logger.info("Finished Australian Concept markup");
    }

    public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
        this.taxonConceptDao = taxonConceptDao;
    }
}
