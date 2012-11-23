package org.ala.hbase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.ala.dao.InfoSourceDAO;
import org.ala.dao.TaxonConceptDao;
import org.ala.model.Classification;
import org.ala.model.CommonName;
import org.ala.model.InfoSource;
import org.ala.model.Rank;
import org.ala.model.SynonymConcept;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.util.SpringUtils;
import org.ala.util.TabReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Reads a Darwin Core CSV which contains a classification for a taxon, and add
 * this classification to the taxon profile.
 * 
 * @author Natasha Carter
 * 
 */
@Component("alaNamesLoader")
public class ALANamesLoader {

    protected static Logger logger = Logger.getLogger(ALANamesLoader.class);
    
    @Inject
    protected InfoSourceDAO infoSourceDAO;
    @Inject
    protected TaxonConceptDao taxonConceptDao;
    
    private static final String IDENTIFIERS_FILE = "/data/bie-staging/ala-names/identifiers.txt";
    private static final String COL_IDENTIFIERS_FILE = "/data/bie-staging/ala-names/col_identifiers.txt";
    
    private static final String AFD_COMMON_NAMES = "/data/bie-staging/anbg/AFD-common-names.csv";
    private static final String APNI_COMMON_NAMES = "/data/bie-staging/anbg/APNI-common-names.csv";
    public static  String ALA_NAMES_FILE = "/data/bie-staging/ala-names/ala_concepts_dump.txt";
    
    public static final String COL_HOME = "http://www.catalogueoflife.org/";
    public static final String APNI_HOME = "http://www.anbg.gov.au/apni/";
    public static final String APC_HOME = "http://www.anbg.gov.au/chah/apc/";
    public static final String AFD_HOME = "http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/home";
    
    
  //lucene indexes
    private static final String NAMES_LOADING_IDX_DIR= "/data/lucene/alanames/tc";
    private static final String NAMES_LOADING_ID_IDX_DIR= "/data/lucene/alanames/id";
    protected IndexSearcher tcIdxSearcher;
    protected IndexSearcher identifierIdxSearcher;

    public static void main(String[] args) throws Exception {

        ApplicationContext context = SpringUtils.getContext();
        ALANamesLoader l = context.getBean(ALANamesLoader.class);
        LinkIdentifierLoader lil = context.getBean(LinkIdentifierLoader.class);
        long start = System.currentTimeMillis();

        logger.info("Creating checklist bank loading index....");

        String skipIndexes = System.getProperty("skipIndexes");
        if (StringUtils.isEmpty(skipIndexes)) {
            l.createLoadingIndex();
            l.createIdentifierIndex();
        }

        logger.info("Initialise indexes....");
        l.initIndexes();

        if (args.length == 0 || "-sci".equals(args[0]) || "-update".equals(args[0])) {

            logger.info("Loading concepts....");
            boolean update = args.length>0 && "-update".equals(args[0]);
            if(args.length >1)
                ALA_NAMES_FILE = args[1];
            l.loadConcepts(lil, update);

            logger.info("Loading synonyms....");            
            
                l.loadSynonyms(update);
            
            
            //IDENTIFIERS are not being loaded separately because they will be loaded as taxonConcept "sameAs" in during the ANBG loading phase.
            //logger.info("Loading identifiers....");
            //l.loadIdentifiers();
        }
        
        if(args.length == 0 || "-sci".equals(args[0]) || "-id".equals(args[0])){
            logger.info("Loading identifiers....");
            l.loadIdentifiers(IDENTIFIERS_FILE, 1,2);
            l.loadIdentifiers(COL_IDENTIFIERS_FILE, 0, 1);
        }

        if (args.length == 0 || "-common".equals(args[0])) {
            logger.info("Loading afd common names....");
            l.loadCommonNames(AFD_COMMON_NAMES);

            logger.info("Loading apni common names....");
            l.loadCommonNames(APNI_COMMON_NAMES);
        }

        long finish = System.currentTimeMillis();

        logger.info("Finished loading ala names. Time taken: "
                + ((finish - start) / 60000) + " minutes");

        System.exit(0);
    }
    
    

    /**
     * Load the alternative identifiers for these concepts.
     * 
     * @throws Exception
     */
    private void loadIdentifiers(String fileName, int accIdx, int extraIdx) throws Exception {
        
        //read the identifiers file
        CSVReader reader = new CSVReader(new FileReader(fileName),'\t', '\n');
        String[] line = null;
        int numberRead = 0;
        int numberAdded = 0;
        long start = System.currentTimeMillis();
        while((line = reader.readNext())!=null){
            numberRead++;
            if(line[accIdx]!=null && line[extraIdx]!=null){
                //add this guid somewhere
                if(taxonConceptDao.addIdentifier(line[accIdx], line[extraIdx])){
                    numberAdded++;
                    if(numberAdded % 1000 == 0){
                        long current = System.currentTimeMillis();
                        logger.info("Number added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record" );
                    }
                }
            }
        }
        logger.info(numberAdded + " identifiers added from " + numberRead + " rows of Checklist Bank data.");
    }
    
    
    private boolean isLSID(String guid) {
        return guid!=null && guid.contains("lsid");
    }
    
    /**
     * Load the synonyms in the DwC Archive
     * 
     * @throws IOException
     * @throws UnsupportedArchiveException
     * @throws Exception
     */
    public void loadSynonyms(boolean update) throws IOException, UnsupportedArchiveException, Exception {
        
        InfoSource afd = infoSourceDAO.getByUri(AFD_HOME);
        InfoSource apc = infoSourceDAO.getByUri(APC_HOME);        
        InfoSource col = infoSourceDAO.getByUri(COL_HOME);
        
        //names files to index
        //TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_name_usages.txt", true);
        CSVReader tr = new CSVReader(new FileReader(ALA_NAMES_FILE), '\t', '"', '\\');
//      CSVReader tr = new CSVReader(new FileReader("/data/bie-staging/checklistbank/cb_name_usages.txt"), '\t', '"', '\\');
        String[] cols = tr.readNext(); //first line contains headers - ignore
        int numberRead = 0;
        int numberAdded = 0;
        long start = System.currentTimeMillis();
        while((cols=tr.readNext())!=null){
            numberRead++;
            if(cols.length==36){
                String identifier = cols[0];
                String parentNameUsageID = cols[1];
                final String guid = cols[2] != null ?cols[2]: identifier; //TaxonID
                String nameLsid = cols[5];
                String nameString =  cols[6];
                
                String scientificNameAuthorship = StringUtils.trimToNull(cols[10]);
                String authorYear = StringUtils.trimToNull(cols[11]);
                Integer rankID = null;
                if(StringUtils.isNotEmpty(cols[12])) rankID = NumberUtils.createInteger(cols[12]);
                String rankString =  cols[13];
                String acceptedGuid =  cols[4];
                String dataset = cols[30];
                Integer synonymType = null;
                if(StringUtils.isNotEmpty(cols[32])) synonymType = NumberUtils.createInteger(cols[32]);
                String synonymRelationship = cols[33];
                String synonymDescription = cols[34];
                
               
                
                if (guid != null && StringUtils.isNotEmpty(acceptedGuid)) {
                    
                    //add the base concept
                    SynonymConcept tc = new SynonymConcept();
                    if(update){
                        //get the current synonyms
                        java.util.List<SynonymConcept> synonyms = taxonConceptDao.getSynonymsFor(acceptedGuid);
                        Object existing =org.apache.commons.collections.CollectionUtils.find(synonyms, new org.apache.commons.collections.Predicate(){

                            @Override
                            public boolean evaluate(Object object) {
                                if(object instanceof SynonymConcept){
                                    return guid.equals(((SynonymConcept)object).getGuid());
                                }
                                return false;
                            }
                            
                        });
                        if(existing != null)
                            tc = (SynonymConcept)existing;
                    }
                    tc.setId(Integer.parseInt(identifier));
                    tc.setGuid(guid);
                    tc.setParentId(parentNameUsageID);
                    tc.setNameString(nameString);
                    tc.setNameGuid(nameLsid);
                    tc.setAuthor(scientificNameAuthorship);
                    tc.setAuthorYear(authorYear);
                    tc.setRankString(rankString);
                    tc.setRankID(rankID);
                    tc.setType(synonymType);
                    tc.setRelationship(synonymRelationship);
                    tc.setDescription(synonymDescription);
                    tc.setIsPreferred(true);
                    
                    
                    if("APNI".equalsIgnoreCase(dataset)){
                        tc.setInfoSourceId(Integer.toString(apc.getId()));
                        tc.setInfoSourceName(apc.getName());
                        if(isLSID(guid)){
                            String internalId = guid.substring(guid.lastIndexOf(":")+1);
                            tc.setInfoSourceURL("http://biodiversity.org.au/apni.taxon/"+internalId);
                        }                     
                    } else if("COL".equalsIgnoreCase(dataset)){
                        tc.setInfoSourceId(Integer.toString(col.getId()));
                        tc.setInfoSourceName(col.getName());
                        tc.setInfoSourceURL("http://www.catalogueoflife.org");// can't link to synonym as id's in CoL are not final/details/species/"+guid);
                    } else if("AFD".equalsIgnoreCase(dataset)){
                        tc.setInfoSourceId(Integer.toString(afd.getId()));
                        tc.setInfoSourceName(afd.getName());
                        
                        String internalId = guid.substring(guid.lastIndexOf(":")+1);
                        //tc.setInfoSourceURL("http://biodiversity.org.au/afd.taxon/"+internalId);
                        tc.setInfoSourceURL("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/"+nameString.replaceAll("\\+", "%20"));
//                      }
                    }
                    
                    
                    if (taxonConceptDao.addSynonym(acceptedGuid, tc)) {
                        numberAdded++;
                        if(numberAdded % 1000 == 0){
                            long current = System.currentTimeMillis();
                            logger.info("Synonyms added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record" );
                        }
                    }
                }
            } else {
                logger.error("Error reading line " + numberRead+", incorrect number of columns: " +cols.length);
            }
        }
        logger.info(numberAdded + " synonyms added from " + numberRead + " rows of Checklist Bank data.");
    }
    
    
    /**
     * Load the accepted concepts into the persistent data store
     * @throws Exception
     */
    public void loadConcepts(LinkIdentifierLoader lil,boolean update) throws Exception {
        
        long start = System.currentTimeMillis();
        
        InfoSource afd = infoSourceDAO.getByUri(AFD_HOME);
        InfoSource apc = infoSourceDAO.getByUri(APC_HOME);
        InfoSource apni = infoSourceDAO.getByUri(APNI_HOME);
        InfoSource col = infoSourceDAO.getByUri(COL_HOME);
        
        //names files to index
        //TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_name_usages.txt", true);
        CSVReader tr = new CSVReader(new FileReader(ALA_NAMES_FILE), '\t', '"', '\\');
//      CSVReader tr = new CSVReader(new FileReader("/data/bie-staging/checklistbank/cb_name_usages.txt"), '\t', '"', '\\');
        
        String[] cols = tr.readNext(); //first line contains headers - ignore
        int lineNumber = 1;
        while((cols=tr.readNext())!=null){
            try {
                if(cols.length==36){
                    
                    String identifier = cols[0];
                    String parentNameUsageID = cols[1];
                    String guid = cols[2]; //TaxonID
                    String parentGuid = cols[3];
                    String acceptedGuid = cols[4];
                    //String acceptedNameUsageID = cols[5]; //acceptedNameUsageID
                    String nameLsid = cols[5];
                    //String canonicalNameId = cols[6];
                    String scientificName = cols[6];
                    //String canonicalName = cols[7];
                    String genusOrHigher = cols[7];
                    String specificEpithet = cols[8];
                    String infraspecificEpithet = cols[9];
                    
                    String scientificNameAuthorship = StringUtils.trimToNull(cols[10]);
                    String authorYear = StringUtils.trimToNull(cols[11]);
                    
                    if(lineNumber%10000==0) {
                        logger.info("Added "+lineNumber +", identifier: "+cols[0]+", guid: "+cols[2]);
                    }
                    
                    Integer rankID = null;
                    try{
                    if(StringUtils.isNotEmpty(cols[12])) rankID = Integer.parseInt(cols[12]);
                    }
                    catch(NumberFormatException e){
                        System.out.println("UNABLE to add rank " + cols[12] + " for record " + identifier);
                    }
                    
                    String taxonRank = cols[13];
                    Integer left = null;
                    Integer right = null;
                        
                    if(StringUtils.isNotEmpty(cols[14])) left = NumberUtils.createInteger(cols[14]);
                    if(StringUtils.isNotEmpty(cols[15])) right = NumberUtils.createInteger(cols[15]);
                    
                    String kingdomID = cols[16];
                    String kingdom = cols[17];
                    String phylumID = cols[18];
                    String phylum = cols[19];
                    String clazzID = cols[20];
                    String clazz = cols[21];
                    String orderID = cols[22];
                    String order = cols[23];
                    String familyID = cols[24];
                    String family = cols[25];
                    String genusID = cols[26];
                    String genus = cols[27];
                    String speciesID = cols[28];
                    String species = cols[29];
                    String dataset = cols[30];
                    String parentSrc = cols[31];
                    String synonymTypeId = cols[32];
                    String synonymRelationship = cols[33];
                    String synonymDescription = cols[34];
                    String rawRank = cols[35];
        
                    if(StringUtils.isEmpty(guid)){
                        guid = identifier;
                    }
                    
                    int numberAdded = 0;
        
                    if (StringUtils.isNotEmpty(guid) && StringUtils.isEmpty(acceptedGuid)) {
                        
                        //add the base concept
                        TaxonConcept tc = update? taxonConceptDao.getByGuid(guid): new TaxonConcept();
                        tc.setId(Integer.parseInt(identifier));
                        tc.setGuid(guid);
                        tc.setParentId(parentNameUsageID);
                        tc.setParentGuid(parentGuid);
                        tc.setNameString(scientificName);
                        tc.setAuthor(scientificNameAuthorship);
                        tc.setAuthorYear(authorYear);
                        tc.setRankString(taxonRank);
                        tc.setRankID(rankID);
                        tc.setLeft(left);
                        tc.setRight(right);
                        tc.setIsPreferred(true);
                        tc.setNameGuid(nameLsid);
                        tc.setRawRankString(rawRank);
                        
                        //set the parent source  - indicates how the parent for the term was identified.
                        if(StringUtils.isNotEmpty(parentSrc)){
                            String parentSource =null;
                            int psrc = NumberUtils.createInteger(parentSrc);
                            switch(psrc){
                            case 50:parentSource = "Direct NSL parent"; break;
                            case 60: parentSource ="Direct NSL parent in same Taxon Name";break;
                            case 70: parentSource ="Another NSL concept with the same Taxon Name";break;
                            case 80: parentSource ="Parent calculated from genus and specific epithet";break;
                            case 90: parentSource = "CoL";break;
                              
                            }
                            tc.setParentSrc(parentSource);
                            tc.setParentSrcId(psrc);
                        }
                        
                        if("APC".equalsIgnoreCase(dataset)){
                            tc.setInfoSourceId(Integer.toString(apc.getId()));
                            tc.setInfoSourceName(apc.getName());
                            if(isLSID(guid)){
                                String internalId = guid.substring(guid.lastIndexOf(":")+1);
                                tc.setInfoSourceURL("http://biodiversity.org.au/apni.taxon/"+internalId);
                            }
                        } else if("APNI".equalsIgnoreCase(dataset)){
                            tc.setInfoSourceId(Integer.toString(apni.getId()));
                            tc.setInfoSourceName(apni.getName());
                            if(isLSID(guid)){
                                String internalId = guid.substring(guid.lastIndexOf(":")+1);
                                tc.setInfoSourceURL("http://biodiversity.org.au/apni.taxon/"+internalId);
                            }
                        } else if("COL".equalsIgnoreCase(dataset)){
                            tc.setInfoSourceId(Integer.toString(col.getId()));
                            tc.setInfoSourceName(col.getName());
                            tc.setInfoSourceURL(col.getWebsiteUrl());
                        } else if("AFD".equalsIgnoreCase(dataset)){
                            tc.setInfoSourceId(Integer.toString(afd.getId()));
                            tc.setInfoSourceName(afd.getName());
                            tc.setInfoSourceURL(afd.getWebsiteUrl());
//                          if(isLSID(guid)){
//                              String internalId = guid.substring(guid.lastIndexOf(":")+1);
                            TaxonName tn = taxonConceptDao.getTaxonNameFor(guid);
                            String sciFullName = null;
                            if (tn != null) {
                                sciFullName = URLEncoder.encode(tn.getNameComplete(), "UTF-8");
                            } else {
                                sciFullName = scientificName;
                            }
                            String internalId = guid.substring(guid.lastIndexOf(":")+1);
                            //tc.setInfoSourceURL("http://biodiversity.org.au/afd.taxon/"+internalId);
                            tc.setInfoSourceURL("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/"+sciFullName.replaceAll("\\+", "%20"));
//                          }
                        }
                        
                        if (taxonConceptDao.create(tc)) {
                            numberAdded++;
                            if(numberAdded % 1000 == 0){
                                long current = System.currentTimeMillis();
                                logger.info("Taxon concepts added: "+numberAdded+", insert rate: "+((current-start)/numberAdded)+ "ms per record, last guid: "+ tc.getGuid());
                            }
                        }
                        

                        
                        //add the classification
                        Classification c = new Classification();
                        c.setGuid(guid);
                        c.setScientificName(scientificName);
                        c.setRank(taxonRank);
                        c.setSpecies(species);
                        c.setSpeciesGuid(speciesID);
                        c.setGenus(genus);
                        c.setGenusGuid(genusID);
                        c.setFamily(family);
                        c.setFamilyGuid(familyID);
                        c.setOrder(order);
                        c.setOrderGuid(orderID);
                        c.setClazz(clazz);
                        c.setClazzGuid(clazzID);
                        c.setPhylum(phylum);
                        c.setPhylumGuid(phylumID);
                        c.setKingdom(kingdom);
                        c.setKingdomGuid(kingdomID);
                        c.setRankId(rankID);
//                        try {
//                            // Attempt to set the rank Id via Rank enum
//                            c.setRankId(Rank.getForName(taxonRank).getId());
//                        } catch (Exception e) {
//                            logger.warn("Could not set rankId for: "+taxonRank+" in "+guid);
//                        }
                        boolean success = taxonConceptDao.addClassification(guid, c);
                        if(!success) logger.error("Failed to add classification to "+guid+", line number: "+lineNumber);
                        
                        //add the link identifier for the taxon
                        lil.updateLinkIdentifier(guid,scientificName);
                        
                    } else {
                        if(StringUtils.isEmpty(acceptedGuid)){
                            logger.error("Failed to add line number: "+lineNumber+", guid:"+guid);
                        }
                    }
                } else {
                    logger.error("Error reading line " + lineNumber+", incorrect number of columns: " +cols.length);
                }
                lineNumber++;
            } catch (Exception e){
                logger.error("Error reading line " + lineNumber+", " + e.getMessage(), e);
            }
        }
    }
    
    
    /**
     * Create a loading index for checklist bank data.
     * 
     * @throws Exception
     */
    public void createIdentifierIndex() throws Exception {
        logger.info("Creating identifier index...");
        long start = System.currentTimeMillis();
        
        //create a name index
        File file = new File(NAMES_LOADING_ID_IDX_DIR);
        if(file.exists()){
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);
        
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
        IndexWriter iw = new IndexWriter(FSDirectory.open(file), analyzer, MaxFieldLength.UNLIMITED);
        
        int i = 0;
        
        //names files to index
//      TabReader tr = new TabReader("/data/bie-staging/checklistbank/cb_identifiers.txt", false);
        CSVReader tr = new CSVReader(new FileReader("/data/bie-staging/ala-names/identifiers.txt"), '\t', '"', '\\');
        
        String[] cols = tr.readNext(); //first line contains headers - ignore
        
        while((cols=tr.readNext())!=null){
            
            if(StringUtils.isNotEmpty(cols[1])){
                Document doc = new Document();
                doc.add(new Field("guid", cols[2], Store.YES, Index.ANALYZED));
                doc.add(new Field("preferredGuid", cols[1], Store.YES, Index.NO));
                
                //add to index
                iw.addDocument(doc, analyzer);
                i++;
                
                if(i%10000==0) {
                    iw.commit();
                    logger.info(i+"\t"+cols[0]+"\t"+cols[2]);
                }
            }
        }
        
        //close taxonConcept stream
        iw.commit();
        tr.close();
        iw.close();
        
        long finish = System.currentTimeMillis();
        logger.info(i+" indexed identifiers in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
    }
    
    
    /**
     * Create a loading index for the alanames.
     * 
     * @throws Exception
     */
    public void createLoadingIndex() throws Exception {
        logger.info("Creating loading index...");
        long start = System.currentTimeMillis();
        
        //create a name index
        File file = new File(NAMES_LOADING_IDX_DIR);
        if(file.exists()){
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);
        
        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
        IndexWriter iw = new IndexWriter(FSDirectory.open(file), analyzer, MaxFieldLength.UNLIMITED);
        
        int i = 0;
        
        //names files to index
        //TabReader tr = new TabReader("/data/bie-staging/ala-names/ala_concepts_dump.txt", true);
      CSVReader tr = new CSVReader(new FileReader("/data/bie-staging/ala-names/ala_concepts_dump.txt"), '\t', '"', '\\');
        String[] cols = tr.readNext(); //first line contains headers - ignore
        
        while((cols=tr.readNext())!=null){
            
            if(cols.length==36){
            
                String identifier = cols[0];
                String parentNameUsageID = cols[1];
                String guid = cols[2]; //TaxonID
                String acceptedGuid = cols[4]; //The accepted concept.
                
                Document doc = new Document();
                doc.add(new Field("id", cols[0], Store.YES, Index.ANALYZED));
                if(StringUtils.isNotEmpty(parentNameUsageID)){
                    doc.add(new Field("parentId", parentNameUsageID, Store.YES, Index.ANALYZED));
                }
                if(StringUtils.isNotEmpty(guid)){
                    doc.add(new Field("guid", guid, Store.YES, Index.NOT_ANALYZED));
                } else {
                    doc.add(new Field("guid", identifier, Store.YES, Index.NOT_ANALYZED));
                }
                doc.add(new Field("nameString", cols[6], Store.YES, Index.ANALYZED));
                if(StringUtils.isNotEmpty(acceptedGuid)){
                    doc.add(new Field("acceptedGuid", acceptedGuid, Store.YES, Index.ANALYZED));
                }
                
                //add to index
                iw.addDocument(doc, analyzer);
            
            } else {
                logger.error("Line "+i +", doesnt have the right no. of columns, has: "+cols.length);
            }
            
            i++;
            
            if(i%100000==0) {
                //iw.flush();
                iw.commit();
                logger.info(i+"\t"+cols[0]+"\t"+cols[2]);
            }
        }
        
        //close taxonConcept stream
        //logger.info("Creating loading index - flushing...");
        //iw.flush();
        logger.info("Creating loading index - commit...");
        iw.commit();
        logger.info("Creating loading index - close...");
        iw.close();
        logger.info("Creating loading index - file close...");
        tr.close();
        
        long finish = System.currentTimeMillis();
        logger.info(i+" indexed taxon concepts in: "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
    }
    

    /**
     * Initialise indexes for lookups.
     * 
     * @throws Exception
     */
    public void initIndexes() throws Exception {
        this.tcIdxSearcher = new IndexSearcher(FSDirectory.open(new File(NAMES_LOADING_IDX_DIR)), true);
        this.identifierIdxSearcher = new IndexSearcher(FSDirectory.open(new File(NAMES_LOADING_ID_IDX_DIR)), true);
    }

private String getPreferredGuid(String taxonConceptGuid) throws Exception {
        
        Query query = new TermQuery(new Term("guid", taxonConceptGuid));
        TopDocs topDocs = identifierIdxSearcher.search(query, 1);
        for(ScoreDoc scoreDoc: topDocs.scoreDocs){
            Document doc = identifierIdxSearcher.doc(scoreDoc.doc);
            return doc.get("preferredGuid");
        }
        return taxonConceptGuid;
    }

    public void loadCommonNames(String dataFile) throws Exception {
        
        InfoSource afd = infoSourceDAO.getByUri(AFD_HOME);
        InfoSource apni = infoSourceDAO.getByUri(APNI_HOME);
        
        logger.info("Starting to load common names from " + dataFile);
        
        long start = System.currentTimeMillis();
        
        // add the taxon concept regions
        //NC A TabReader can not be used because quoted fields can contain a comma
        //TabReader tr = new TabReader(dataFile, true, ',');
        CSVReader tr = new CSVReader(new FileReader(dataFile), '\t', '"',1);
        String[] values = null;
        Pattern p = Pattern.compile(",");
        int namesAdded = 0;
        int linenumber = 0;
        while ((values = tr.readNext()) != null) {
            linenumber++;
            if (values.length >= 6) {
                String guid = values[0];
                String commonNameString = values[2];
//              String taxonConceptGuid = values[5];
                //String scientificName = values[7];
                //retrieve the concept - this gets around the use of multiple guids for a single concept
                //this is the case for APNI/APC concepts.
//              taxonConceptGuid = getPreferredGuid(taxonConceptGuid);
                
//              if(taxonConceptGuid==null){
                    //try to find the concept for this scientific name
                String taxonConceptGuid = null;
//                try {
//                    taxonConceptGuid = cbIdxSearcher.searchForLSID(scientificName, null);
//                } catch (Exception e){
//                    logger.error(e.getMessage());
//                }
                
                //if null try and match using the supplied GUID
                if(taxonConceptGuid==null){
                    taxonConceptGuid = getPreferredGuid(values[3]);
                }
                
                if(taxonConceptGuid!=null){
                    //do a look up for the correct taxon
                    CommonName commonName = new CommonName();
                    commonName.setGuid(guid);
                    //set this common name to be the preferred name
                    commonName.setPreferred(true);
                    commonName.setRanking(2);
                    commonName.setNoOfRankings(2);
                    //set the attribution
                    if(values[3].contains(":apni.")){
                        commonName.setInfoSourceId(Integer.toString(apni.getId()));
                        commonName.setInfoSourceName(apni.getName());
                        commonName.setInfoSourceURL(apni.getWebsiteUrl());
                        if(isLSID(guid)){
                            String internalId = values[3].substring(values[3].lastIndexOf(":")+1);
                            commonName.setInfoSourceURL("http://biodiversity.org.au/apni.taxon/"+internalId);
                            commonName.setIdentifier("http://biodiversity.org.au/apni.taxon/"+internalId);
                        }
                    } else if(values[3].contains(":afd.")){
                        commonName.setInfoSourceId(Integer.toString(afd.getId()));
                        commonName.setInfoSourceName(afd.getName());
                        commonName.setInfoSourceURL(afd.getWebsiteUrl());
                        if(isLSID(guid)){
                            String internalId = values[3].substring(values[3].lastIndexOf(":")+1);
                            commonName.setInfoSourceURL("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/"+internalId);
                            commonName.setIdentifier("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/"+internalId);
                        }
                    }
                    //the common name string can be a comma separated list of names
                    String[] commonNameStrings = p.split(commonNameString);
                    for(String cn: commonNameStrings){
                        commonName.setNameString(cn);
                        boolean success = taxonConceptDao.addCommonName(taxonConceptGuid, commonName);
                        if(success) namesAdded++;
                        if(!success){
                            logger.error("Unable to add "+commonName);
                        }
                    }
                } else {
                    logger.error("Unable to add "+commonNameString+" to taxon: "+values[3]+" -  concept not found.");
                }
            } else {
                logger.error("Skipping line "+linenumber+", number of values: "+values.length);
            }
        }
        
        tr.close();
        long finish = System.currentTimeMillis();
        logger.info(namesAdded+" common names added to taxa. Time taken "+(((finish-start)/1000)/60)+" minutes, "+(((finish-start)/1000) % 60)+" seconds.");
    }
    
    
}
