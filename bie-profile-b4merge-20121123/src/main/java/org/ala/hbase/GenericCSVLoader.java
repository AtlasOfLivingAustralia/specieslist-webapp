package org.ala.hbase;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ala.client.util.RestfulClient;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Category;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.TypeReference;
import org.gbif.file.CSVReader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.org.ala.data.model.LinnaeanRankClassification;

@Component("genericCSVLoader")
public class GenericCSVLoader {

    @Inject
    protected TaxonConceptDao taxonConceptDao;
    protected RestfulClient restfulClient = new RestfulClient(0);
    protected static Logger logger = Logger.getLogger(GenericCSVLoader.class);
    private Set<String> lsidsToReindex;
    private FileOutputStream reindexOut;
    private static final String reindexFile = "/data/bie-staging/profile/reindex.out";
    public static void main(String args[]) throws Exception{
        ApplicationContext context = SpringUtils.getContext();
        GenericCSVLoader loader = (GenericCSVLoader) context.getBean(GenericCSVLoader.class);
        //loader.load("dr740","/data/bie-staging/profile/dr740/Master Weeds list.txt",'\t');
        if(args.length==2){
            loader.load(args[0],args[1],'\t');
        }
        else{
            System.out.println("Please supply data resource and file path.");
        }
    }
  
    public void load(String dataResource, String filename, char separator){
        System.out.println("Starting to load...");
        
        Map<String,Object> colMap=getDetailsFromCollectory(dataResource);
        lsidsToReindex = new HashSet<String>();
        try{
            reindexOut = FileUtils.openOutputStream(new File(reindexFile));
            CSVReader reader = CSVReader.buildReader(new File(filename), "UTF-8", separator, '"', 1);
            String[] header = reader.readNext();
            System.out.println("The header " + header[0]);
            //to do map the header row to valid DWC field names.
            while(reader.hasNext()){
                processLine(colMap, reader.readNext(), header);
            }
            for(String lsid : lsidsToReindex)
                reindexOut.write((lsid+"\n").getBytes());
            reindexOut.flush();
            reindexOut.close();
            System.exit(0);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
  
    private void processLine(Map resourceMap,String[] row, String[] header){      
        Category cat = new Category();
        cat.setInfoSourceUid((String)resourceMap.get("uid"));
        cat.setInfoSourceName((String)resourceMap.get("name"));
        cat.setInfoSourceURL((String)resourceMap.get("websiteUrl"));
        Map<String, String> values = toMap(header, row);
        if(values != null && values.size()>0){
            //cat.setInfoSourceName(getValue("datasetName",values));
            LinnaeanRankClassification cl = new LinnaeanRankClassification();
            cl.setKingdom(getValue("kingdom",values));
            cl.setPhylum(getValue("phylum",values));
            cl.setKlass(getValue("class",values));
            cl.setOrder(getValue("order",values));
            cl.setFamily(getValue("family",values));
            cl.setGenus(getValue("genus",values));
            cl.setScientificName(getValue("scientificName",values));
            
            cat.setStartDate(getValue("startDate",values));
            cat.setEndDate(getValue("endDate",values));
            cat.setIdentifier(getValue("references",values));
            cat.setAuthority(getValue("authority",values));
            cat.setCategory(getValue("category",values));
            cat.setCategoryRemarks(getValue("categoryRemarks",values));
            cat.setFootprintWKT(getValue("footprintWKT",values));
            cat.setLocality(getValue("locality",values));
            cat.setLocationID(getValue("locationID",values));
            cat.setReason(getValue("reason",values));
            cat.setStateProvince(getValue("stateProvince",values));
            //cat.set
            try{
                //System.out.println(taxonConceptDao.findCBDataByName(cl.getScientificName(), cl, null));
                String guid = taxonConceptDao.findLsidByName(cl.getScientificName(), cl, null, true);
                if(guid != null){
                    //add the category
                    lsidsToReindex.add(guid);
                    taxonConceptDao.addCategory(guid, cat);
                    //System.out.println(guid);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
  
  private String getValue(String field, Map<String,String> map){
    return StringUtils.stripToNull(map.get(field));
  }
  
  
  private Map<String,Object> getDetailsFromCollectory(String druid){
      String url = "http://collections.ala.org.au/ws/dataResource/"+druid+".json";
      ObjectMapper mapper = new ObjectMapper();
      
      try{
          Object[] resp = restfulClient.restGet(url);
          if((Integer)resp[0] == HttpStatus.SC_OK){
              String content = resp[1].toString();
              System.out.println(content);
              Map<String, Object> values =mapper.readValue(resp[1].toString(), new TypeReference<Map<String, Object>>() {});
              return values;
          }
      }
      catch(Exception e){
          e.printStackTrace();
      }
      return null;
  }
  
  private Map<String,String> toMap(String[] keys, String[] values) {
    int keysSize = (keys != null) ? keys.length : 0;
    int valuesSize = (values != null) ? values.length : 0;

    if (keysSize == 0 && valuesSize == 0) {
        // return mutable map
        return new HashMap<String,String>();
    }

    if (keysSize != valuesSize ) {
      logger.warn("Unable to load " + StringUtils.join(keys,",") + " as it does not have the same rows as the header.");
      return null;
//        throw new IllegalArgumentException(
//                "The number of keys doesn't match the number of values.");
    }

    Map<String,String> map = new HashMap<String,String>();
    for (int i = 0; i < keysSize; i++) {
        map.put(keys[i], values[i]);
    }

    return map;
}
  
}
