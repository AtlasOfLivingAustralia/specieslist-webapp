package org.ala.util;

import java.io.FileReader;

import javax.inject.Inject;

import org.ala.dao.CassandraPelopsHelper;
import org.ala.dao.StoreHelper;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.checklist.lucene.CBIndexSearch;
import au.org.ala.checklist.lucene.model.NameSearchResult;

/**
 * 
 * 
 * Loads the Rankings from a CSV file. Allows migration of rankings between OLD 
 * cassandra schema and new.
 * 
 * The lsid or scientific name need to be located in the new names.
 * 
 * @author Natasha Carter
 *
 */

public class LoadRankingColumnFamily {
    protected Logger logger = Logger.getLogger(this.getClass());
    
    protected StoreHelper storeHelper;
    protected CBIndexSearch cbIdxSearcher;
    
    
    
    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        LoadRankingColumnFamily loader = new LoadRankingColumnFamily();
        loader.storeHelper = (StoreHelper)context.getBean(CassandraPelopsHelper.class);
        loader.cbIdxSearcher = (CBIndexSearch)context.getBean(CBIndexSearch.class);
        if(args.length == 1){
            loader.load(args[0]);
            
        }
        else{
            System.out.println("Usage:");
            System.out.println("LoadRankingColumnFamily <filename>");
        }
        System.exit(0);
    }
    
    public void load(String file) throws Exception{
        CSVReader tr = new CSVReader(new FileReader(file), ';', '"', '~');
        String[] values = null;
        int i=0,lsidct=0,matched=0,homonym=0,unmatched=0;
        long startId=System.currentTimeMillis();
        while((values = tr.readNext()) != null){
            String oldLsid = values[0];
            String lsid = null;
            String scientificName = values[1];
            //attempt to find the lsid
            NameSearchResult nsr = cbIdxSearcher.searchForRecordByLsid(oldLsid);
            if(nsr != null){
                lsidct++;
                lsid = oldLsid;
                //logger.info(oldLsid + " is in new system. " + nsr.getRankClassification().getScientificName() + " : old name : " + scientificName);
            }
            else{
                try{
                nsr = cbIdxSearcher.searchForRecord(scientificName, null);
                }
                catch(Exception e){
                    logger.info("HOMONYM: " + scientificName);
                    homonym++;
                }
                if(nsr != null){
                    //logger.info("Located name: " + scientificName + " " + nsr);
                    matched++;
                    lsid = nsr.getAcceptedLsid() == null?nsr.getLsid():nsr.getAcceptedLsid();
                }
                else{
                    logger.info("Unable to locate " + scientificName);
                    unmatched++;
                }
            }
            if(lsid != null){
                //insert the value into the rankings table
                storeHelper.updateStringValue("rk","rk",values[3], lsid+"|"+values[2].trim(), values[4].trim());
            }
            
            i++;
        }
        logger.info("Total records: " + i + " LSID matched: " + lsidct + " Name matched: " + matched + " Homonyms: " + homonym + " Unmatched: "+ unmatched);
    }
    
}
