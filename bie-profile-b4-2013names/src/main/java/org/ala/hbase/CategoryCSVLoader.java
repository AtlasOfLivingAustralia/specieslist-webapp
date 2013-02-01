package org.ala.hbase;

import au.org.ala.data.model.LinnaeanRankClassification;
import org.ala.client.util.RestfulClient;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Category;
import org.ala.util.SpringUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gbif.file.CSVReader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component("categoryCSVLoader")
public class CategoryCSVLoader extends GenericCSVLoader {
    public static void main(String args[]) throws Exception{
        ApplicationContext context = SpringUtils.getContext();
        GenericCSVLoader loader = (GenericCSVLoader) context.getBean("categoryCSVLoader");
        if(args.length == 1){
            loader.load(args[0]);
        } else {
            System.out.println("Please supply data resource UID");
        }
    }

    public void processLine(Map resourceMap,String[] row, String[] header){
        Category cat = new Category();
        cat.setInfoSourceUid((String)resourceMap.get("uid"));
        cat.setInfoSourceName((String)resourceMap.get("name"));
        cat.setInfoSourceURL((String)resourceMap.get("websiteUrl"));
        Map<String, String> values = toMap(header, row);
        if(values != null && !values.isEmpty()){
            //cat.setInfoSourceName(getValue("datasetName",values));
            LinnaeanRankClassification cl = new LinnaeanRankClassification();
            cl.setKingdom(getValue("kingdom", values));
            cl.setPhylum(getValue("phylum", values));
            cl.setKlass(getValue("class", values));
            cl.setOrder(getValue("order", values));
            cl.setFamily(getValue("family", values));
            cl.setGenus(getValue("genus", values));
            cl.setScientificName(getValue("scientificName", values));

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
}
