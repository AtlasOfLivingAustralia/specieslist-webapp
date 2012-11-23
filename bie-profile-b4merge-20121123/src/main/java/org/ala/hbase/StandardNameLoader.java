
package org.ala.hbase;

import au.org.ala.data.model.LinnaeanRankClassification;
import java.io.File;
import javax.inject.Inject;
import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.CommonName;
import org.ala.model.InfoSource;
import org.ala.util.SpringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.gbif.file.CSVReader;
import org.springframework.stereotype.Component;

/**
 *
 * Loads the standard names from a CSV file giving them a ranking of 100,000
 *
 * @author Natasha Carter
 */
@Component("standardNameLoader")
public class StandardNameLoader {
    protected static Logger logger = Logger.getLogger(StandardNameLoader.class);
    @Inject
    protected InfoSourceDAO infoSourceDAO;
    @Inject
    protected TaxonConceptDao taxonConceptDao;
    public static void main(String[] args){
        
        ApplicationContext context = SpringUtils.getContext();
        StandardNameLoader loader = (StandardNameLoader) context.getBean(StandardNameLoader.class);
        if(args.length ==1){
        try{
            if(args[0].equals("-caab") || args[0].equals("-all"))
                loader.loadCAAB();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try{
            if(args[0].equals("-ba") || args[0].equals("-all"))
                loader.loadBA();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        }
        else
            System.out.println("Please provide args: -all -ba -caab");
        System.exit(1);
    }
    public void loadBA() throws Exception{
        CSVReader reader = CSVReader.buildReader(new File("/data/bie-staging/vernacular/birds_aust/Checklist_birds_2008.csv"), "UTF-8", ',', '"', 1);
        InfoSource is = new InfoSource();
        is.setName("Birds Australia");
        is.setWebsiteUrl("http://www.birdsaustralia.com.au/images/stories/birds/checklist2008_sm.pdf");
        is.setId(600);
        is.setUid("dr359");
        while(reader.hasNext()){
            String[] values = reader.readNext();
            if(values != null && values.length > 2){
                String commonName = values[0].trim();
                LinnaeanRankClassification cl = new LinnaeanRankClassification("Animalia", null);
                cl.setScientificName(values[1]);
                loadCommonName(cl, commonName, is, null, true);


            }
        }
        taxonConceptDao.reportStats(System.out, "Final BA name stats ");
    }
    public void loadCAAB() throws Exception{
        CSVReader reader = CSVReader.buildReader(new File("/data/bie-staging/vernacular/caab/caab-fishes-standard-names-20101209.csv"), "UTF-8", ',', '"', 1);
        InfoSource is = infoSourceDAO.getById(1016);
        while(reader.hasNext()){
            String[] values = reader.readNext();
            if(values != null && values.length>=7){
                //only want to handle the "australian" region for now
                //Tony Rees comment:
                /*
                 FYI the "List Status Code" = "A" for fishes quoted as occurring in
                 Australian waters (not including sub/antarctic territories e.g. Macquarie I,
                 heard/MacDonald, and AAT), plus a few "R" which is in the Australian region
                 of interest adjacent to the EEZ but not actually in it
                 (maybe a bit north, or Tasman Sea). I have left these in the list
                 in case they are needed.
                 */
                if(values[0].equals("A")){
                    String identifier = "http://www.marine.csiro.au/caabsearch/caab_search.caab_report?spcode=" + values[1];
                    LinnaeanRankClassification cl = new LinnaeanRankClassification(null, null, null, null, values[6], null, values[2]);
                    String name = values[4];
                    boolean standard = true;
                    if(StringUtils.isEmpty(name)){
                        standard = false;
                        if(!values[5].startsWith("["))
                            name = values[5];
                    }
                    if(!StringUtils.isEmpty(name)){
                        loadCommonName(cl, name, is, identifier, standard);
                    }

                }

            }
        }
        taxonConceptDao.reportStats(System.out, "Final CAAB name stats: ");
    }
    private void loadCommonName(LinnaeanRankClassification cl,String commonName, InfoSource is, String identifier, boolean isStandard) throws Exception{
        String guid = taxonConceptDao.findLsidByName(cl.getScientificName(), cl, null);
        if(guid != null){
            CommonName cn = new CommonName();
            cn.setNameString(WordUtils.capitalizeFully(commonName));
            cn.setIsPreferred(true);
            //cn.setRanking(100000);
            cn.setInfoSourceURL(is.getWebsiteUrl());
            cn.setInfoSourceId(Integer.toString(is.getId()));
            cn.setInfoSourceName(is.getName());
            cn.setIdentifier(identifier);
            taxonConceptDao.addCommonName(guid, cn);
        }
        else{
            logger.info("Unable to locate " + cl.getScientificName());
        }
    }

}
