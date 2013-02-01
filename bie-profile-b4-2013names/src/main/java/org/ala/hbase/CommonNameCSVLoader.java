package org.ala.hbase;

import au.org.ala.data.model.LinnaeanRankClassification;
import org.ala.model.CommonName;
import org.ala.util.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("commonNameCSVLoader")
public class CommonNameCSVLoader extends GenericCSVLoader {

    public static void main(String args[]) throws Exception{
        ApplicationContext context = SpringUtils.getContext();
        GenericCSVLoader loader = (GenericCSVLoader) context.getBean("commonNameCSVLoader");
        if(args.length == 1){
            loader.load(args[0]);
        } else {
            System.out.println("Please supply data resource UID.");
        }
    }

    public void processLine(Map resourceMap,String[] row, String[] header){
        CommonName cat = new CommonName();
        cat.setInfoSourceUid((String)resourceMap.get("uid"));
        cat.setInfoSourceName((String)resourceMap.get("name"));
        cat.setInfoSourceURL((String)resourceMap.get("websiteUrl"));
        Map<String, String> values = toMap(header, row);
        if(values != null && !values.isEmpty()){
            LinnaeanRankClassification cl = new LinnaeanRankClassification();
            cl.setKingdom(getValue("kingdom", values));
            cl.setPhylum(getValue("phylum", values));
            cl.setKlass(getValue("class", values));
            cl.setOrder(getValue("order", values));
            cl.setFamily(getValue("family", values));
            cl.setGenus(getValue("genus", values));
            cl.setScientificName(getValue("scientificName", values));
            cat.setNameString(getValue("commonName",values));
            cat.setIsPreferred(true);
            cat.setRanking(999999999);
            try {
                String guid = taxonConceptDao.findLsidByName(cl.getScientificName(), cl, null, true);
                if(guid != null){
                    lsidsToReindex.add(guid);
                    taxonConceptDao.addCommonName(guid, cat);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
