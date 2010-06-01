/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.hbase;


import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.ala.model.Reference;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ala.model.TaxonConcept;
import org.ala.util.TabReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;

/**
 * Loads the references in BHL into the taxon concept 
 * profiles in the BIE
 *
 * The following references are being added
 * - The top maxDoc for each scientific name
 * - The earliest document reference for each scientific name
 * - The document that the scientific name was published in 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 * @author Natasha Carter (Natasha.Carter@csiro.au)
 */
@Component
public class BHLDataLoader {

    protected static Logger logger = Logger.getLogger(BHLDataLoader.class);
    public static String BHL_EXPORT = "/data/bie-staging/bhl/item_au_sn_ref.txt";
    private static final String BHL_LOADING_IDX_DIR = "/data/lucene/bhlloading/bhl";
    private static final String ANBG_BHL_LOADING_IDX_DIR = "/data/lucene/bhlloading/anbg";
    @Inject
    protected TaxonConceptDao taxonConceptDao;
    protected IndexSearcher bhlIdxSearcher, anbgIdxSearcher;
    private int maxDocs = 10;
    private Pattern yearPattern = Pattern.compile("[12][0-9][0-9][0-9]");
    private CachingWrapperFilter yearFilter;
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ApplicationContext c = SpringUtils.getContext();
        BHLDataLoader l = c.getBean(BHLDataLoader.class);
        l.createLoadingIndex();
        l.createAnbgBhlIndex();
        l.initIndexes();
        l.load();
    }


    

    /**
     *
     * @throws Exception
     */
    private void load() throws Exception {

        logger.info("Starting to load literature references from: " + BHL_EXPORT);

        long start = System.currentTimeMillis();

        // add the BHL publications to the taxon concepts
    	TabReader tr = new TabReader(BHL_EXPORT,false); 
        tr.readNext();//the header line
        String[] values = null;
        int i = 0,l=0;
        Reference r = null;
        List<String> topXDocs = null;
        while((values = tr.readNext()) != null){
            l++;
            //Each entry in the source file represents a unique species Item relationship
            //Each entry is a potential new refernece
            if(values.length == 9){
                String guid = values[7];
               //get the taxonConcept for this guid to obgtain the published in citation
                TaxonConcept tc = taxonConceptDao.getByGuid(guid);
                
                String pubLsid = tc == null?null :tc.getPublishedInCitation();
                
                topXDocs = searchItems(guid, "count", maxDocs, true, null);
                boolean isEarliest = values[2].equals(getEarliestItem(guid));
                boolean isPublishedIn = isPublishedInTitle(values[0], pubLsid);
               //Add the reference to the appropriate location
               if(topXDocs.contains(values[2]) || isEarliest || isPublishedIn){
                   r = new Reference();
                   r.setTitle(values[1]);
                   r.setIdentifier(values[2]);
                   r.setScientificName(values[6]);
                   String year = StringUtils.trimToNull(values[4]) == null ? getYear(values[3], values[4]):values[4];
                   r.setYear(year);
                   r.setVolume(values[3]);
                   //add all the page identifiers
                   CollectionUtils.addAll(r.getPageIdentifiers(), values[5].split(","));
                   logger.debug("Add reference to " + guid
                                    + " for document with id: " + r.getIdentifier()
                                    + ", scientificName: " + r.getScientificName() + ", isEarliest: "
                                    + isEarliest + ", publishedIn: "+ isPublishedIn);
                   if(isEarliest)
                       taxonConceptDao.addEarliestReference(guid, r);
                   if(isPublishedIn){ //the reference may be the earliest and the published in reference
                       taxonConceptDao.addPublicationReference(guid, r);
                   }
                   else if(!isEarliest && !isPublishedIn)
                     taxonConceptDao.addReference(guid, r);
                   i++;
               }else{
                  logger.debug("Item " + values[2] + " does not appear in the top " + maxDocs + " for guid: "+ guid);
               }
            if(l%100000==0){
                long now = System.currentTimeMillis();
                logger.info("Processed " + l + " records. Time taken " + (((now - start) / 1000) / 60) + " minutes, " + (((now - start) / 1000) % 60) + " seconds.");
            }
            }else {
                logger.error("Incorrect number of fields in tab file - " + BHL_EXPORT + " at line " + l);
            }
        }
        tr.close();
        long finish = System.currentTimeMillis();
        logger.info(i + " literature references loaded. Time taken " + (((finish - start) / 1000) / 60) + " minutes, " + (((finish - start) / 1000) % 60) + " seconds.");
    }

    /**
     * Create a loading index for BHL data.
     *
     * @throws Exception
     */
    public void createLoadingIndex() throws Exception {
        long start = System.currentTimeMillis();

        //create a name index
        File file = new File(BHL_LOADING_IDX_DIR);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);

        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
        IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);

        int i = 0;

        //names files to index
        TabReader tr = new TabReader(BHL_EXPORT, false);

        String[] cols = tr.readNext(); //first line contains headers - ignore

        while ((cols = tr.readNext()) != null) {

            Document doc = new Document();
            
            String year = getYear(cols[3],cols[4]);

            doc.add(new Field("lsid", cols[7], Store.YES, Index.ANALYZED));
            //doc.add(new Field("name", cols[6], Store.YES, Index.ANALYZED));
            doc.add(new Field("count", cols[8], Store.YES, Index.NOT_ANALYZED));
            if(year != null)
                doc.add(new Field("year", year, Store.YES, Index.NOT_ANALYZED));
            doc.add(new Field("itemid", cols[2], Store.YES, Index.NO));
            //add to index
            iw.addDocument(doc, analyzer);
            i++;

            if (i % 10000 == 0) {
                iw.flush();
                System.out.println(i + "\t" + cols[0] + "\t" + cols[2] + "\t" + cols[7]);
            }
        }

        //close taxonConcept stream
        tr.close();
        iw.close();

        long finish = System.currentTimeMillis();
        logger.info(i + " indexed taxon concepts in: " + (((finish - start) / 1000) / 60) + " minutes, " + (((finish - start) / 1000) % 60) + " seconds.");
    }

    /**
     * Create an index for ANBG publication to BHL title mapping
     * @throws Exception
     */
    public void createAnbgBhlIndex() throws Exception {
        
        //create a name index
        File file = new File(ANBG_BHL_LOADING_IDX_DIR);
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
        FileUtils.forceMkdir(file);

        KeywordAnalyzer analyzer = new KeywordAnalyzer();
        //initialise lucene
        IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);

        int i = 0;
        TabReader tr = new TabReader("/data/bie-staging/bhl/anbg_publication_map.txt", true);
        String[] values = tr.readNext(); //first line is a header
        while((values = tr.readNext())!=null){
            if(values.length == 4){
                i++;
                Document doc = new Document();
                doc.add(new Field("lsid", values[0], Store.NO, Index.NOT_ANALYZED));
                doc.add(new Field("titleid", values[2],Store.YES, Index.NO));
                iw.addDocument(doc);
            }
        }
        tr.close();
        iw.commit();
        iw.close();
        
    }
    /**
     *
     * @param titleId
     * @param pubLsid
     * @return true when the supplied titleId is mapped to the supplied publication LSID 
     */
    public boolean isPublishedInTitle(String titleId, String pubLsid){
        try{
            TermQuery query = new TermQuery(new Term("lsid", pubLsid));
            TopDocs docs =anbgIdxSearcher.search(query, 1);
            if(docs.totalHits>0){
                Document doc = anbgIdxSearcher.doc(docs.scoreDocs[0].doc);
                logger.debug("Title publication LSID : " + pubLsid + " doc.titleId: "+ doc.get("titleid") + " supplied titleID: "+ titleId);
                return titleId.equals(doc.get("titleid"));
            }
        }
        catch(Exception e){}
        return false;
    }

    /**
     * Get the year for the BHL reference.
     * 
     * The first 4 digit number in the year or the first 4 digit number in the volume
     * 
     * @param volume
     * @param year
     * @return
     */
    private String getYear(String volume, String year){
        String value = StringUtils.trimToNull(year) == null? volume:year;

            //attempt to locate a year in the volume
            //assume that a year is any 4 digits in a row
            Matcher matcher =yearPattern.matcher(volume);
            if(matcher.find()){
                value = matcher.group();
            }else{
                value =null;
            }

            return value;
    }

    

    /**
     * Initialise indexes for lookups.
     *
     * @throws Exception
     */
    public void initIndexes() throws Exception {
        this.bhlIdxSearcher = new IndexSearcher(BHL_LOADING_IDX_DIR, true);
        this.anbgIdxSearcher = new IndexSearcher(ANBG_BHL_LOADING_IDX_DIR, true);
        //cache the non empty year filter for performance reasons
        yearFilter = new CachingWrapperFilter(TermRangeFilter.More("year", ""));
    }
    /**
     * 
     * @param lsid
     * @return The itemId for the earliest published item for the supplied lsid
     */
    private String getEarliestItem(String lsid){
        List<String> results =  searchItems(lsid, "year", 1, false, yearFilter);
        if(results != null && results.size()>0)
            return results.get(0);
        return null;
    }
    /**
     * Searches the bhl item index ordering the results by the sortField optionally
     * in reverse order.
     * @param lsid
     * @param sortField
     * @param limit The number of results to return
     * @param reverseSort When true the sort is reversed.
     * @param filter The filter to be applied to the search - used to remove null values in the search field
     * @return
     */
    private List<String> searchItems(String lsid, String sortField, int limit, boolean reverseSort, Filter filter) {
        try {
            Query query = new TermQuery(new Term("lsid", lsid));
            List<String> results = new java.util.ArrayList<String>();
            TopFieldDocs topDocs = bhlIdxSearcher.search(query, filter, limit, new Sort(new SortField(sortField, SortField.INT, reverseSort)));
            for (ScoreDoc sc : topDocs.scoreDocs) {
                Document doc = bhlIdxSearcher.doc(sc.doc);
                results.add(doc.get("itemid"));
            }
            return results;
        } catch (java.io.IOException e) {
        }
        return null;
    }

    /**
     * @param taxonConceptDao the taxonConceptDao to set
     */
    public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
        this.taxonConceptDao = taxonConceptDao;
    }
}
