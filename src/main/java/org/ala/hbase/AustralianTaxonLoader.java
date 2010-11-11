package org.ala.hbase;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileReader;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Marks up taxon as Australian if they are of "interest" to people in Australia.
 *
 * Australian taxon concepts are identified in 2 manners:
 * - concepts sourced from AFD, APC or APNI
 * - concepts that have occurrence records from geographic regions in Australia
 *
 * The list of taxon concepts are currently generated from the portal database
 * by running the is_australian_export.sql script.
 *
 * In the future this may be changed to use Cassandra and the SOLR index...
 *
 * @author Natasha Carter (Natasha.Carter@csiro.au)
 */
@Component("australianTaxonLoader")
public class AustralianTaxonLoader {

    protected static Logger logger = Logger.getLogger(AustralianTaxonLoader.class);

    @Inject
    protected TaxonConceptDao taxonConceptDao;

    public static final String AUST_GUID_FILE = "/data/bie-staging/biocache/austTaxonConcepts.txt";

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        AustralianTaxonLoader l = context.getBean(AustralianTaxonLoader.class);
        l.load();
        System.exit(0);
    }
    
    public void load() throws Exception {
        logger.info("Starting to mark up the Australian Concepts...");
        BufferedReader reader = new BufferedReader(new FileReader(AUST_GUID_FILE));
        String guid = null;
        while ((guid = reader.readLine()) != null) {
        	String taxonGuid = taxonConceptDao.getPreferredGuid(guid);
            taxonConceptDao.setIsAustralian(taxonGuid);
        }
        logger.info("Finished Australian Concept markup");
    }

    public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
        this.taxonConceptDao = taxonConceptDao;
    }
}
