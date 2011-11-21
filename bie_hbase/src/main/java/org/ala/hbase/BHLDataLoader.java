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
import java.util.ArrayList;
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
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
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
    public static String BHL_EXPORT = "/data/bie-staging/bhl/item_au_sn_ref-full.txt";
    private static final String BHL_LOADING_IDX_DIR = "/data/lucene/bhlloading/bhl";
    private static final String ANBG_BHL_LOADING_IDX_DIR = "/data/lucene/bhlloading/anbg";
    @Inject
    protected TaxonConceptDao taxonConceptDao;
    protected IndexSearcher bhlIdxSearcher, anbgIdxSearcher;
    private int maxDocs = 10;
    private Pattern yearPattern = Pattern.compile("[12][0-9][0-9][0-9]");
    private Pattern sepPattern = Pattern.compile(",");
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
		System.exit(0);
    }
    public void printDistinctLsids()throws Exception{
        
        org.apache.lucene.index.TermEnum te =  bhlIdxSearcher.getIndexReader().terms(new Term("lsid"));
        Term t = te.term();
        int i =0;
        while (t != null &&"lsid".equals(t.field())){
            System.out.println("processing LSID: " + t.text()) ;
            i++;
            t = te.next()? te.term():null;
            
        }
        System.out.println("Finished processing " + i + " distinct lsids");
    }

    /**
     * Loads the BHL literature references. 
     * @throws Exception
     */
    private void load() throws Exception{
        logger.info("Starting to load literature references from: " + BHL_EXPORT);

        long start = System.currentTimeMillis();
        //get the distinct LSID's for the species that are contained in the BHL index
        org.apache.lucene.index.TermEnum te =  bhlIdxSearcher.getIndexReader().terms(new Term("lsid"));
        Term term = te.term();
        int i=0,e=0,p =0;
        while (term != null &&"lsid".equals(term.field())){
            i++;
            String guid = term.text();
            //get the maxDoc most populated documents
            List<Reference> topReferences = searchForReferences(guid, "count", maxDocs, true, null);
            //get the earliest reference document
            Reference earlyReference = getEarliestItem(guid);
            if(earlyReference!=null){
                topReferences.remove(earlyReference);
                //add the earliest reference
                taxonConceptDao.addEarliestReference(guid, earlyReference);
                logger.debug("Add earliestReference to " + guid);
                e++;
            }
            //get the item ids for the publication title
            //get the taxonConcept for this guid to obgtain the published in citation
            TaxonConcept tc = taxonConceptDao.getByGuid(guid);
            String pubLsid = tc == null?null :tc.getPublishedInCitation();
            String titleId = pubLsid == null ? null : getTitlePublishedIn(pubLsid);
            if(titleId !=  null){
                List<Reference> publishedReferences = searchReferenceForTitle(guid, titleId);
                if(publishedReferences != null){
                    topReferences.removeAll(publishedReferences);
                    //add the published references
                    taxonConceptDao.addPublicationReference(guid, publishedReferences);
                    logger.debug("Add publcationReference to " + guid);
                    p++;
                }
            }
            //add the references
            guid = taxonConceptDao.getPreferredGuid(guid);
            taxonConceptDao.addReferences(guid, topReferences);
            logger.debug("Added " + topReferences.size() + " references to " + guid);
          
            //move onto the next lsid
            term = te.next()? te.term():null;
            if(i%1000==0){
                long now = System.currentTimeMillis();
                logger.info("Processed " + i + " guids. ["+guid+"]. Time taken " + (((now - start) / 1000) / 60) + " minutes, " + (((now - start) / 1000) % 60) + " seconds.");
            }
        }
        logger.info(i + " references loaded. " + e + " earliest references loaded. " + p +" publication references loaded");
        
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
            doc.add(new Field("titleid", cols[0], Store.YES, Index.NOT_ANALYZED));
            doc.add(new Field("lsid", cols[7], Store.YES, Index.ANALYZED));
            doc.add(new Field("name", cols[6], Store.YES, Index.NO));
            doc.add(new Field("count", cols[8], Store.YES, Index.NOT_ANALYZED));
            if(year != null)
                doc.add(new Field("year", year, Store.YES, Index.NOT_ANALYZED));
            doc.add(new Field("itemid", cols[2], Store.YES, Index.NO));
            doc.add(new Field("pages", cols[5], Store.YES, Index.NO));
            if(cols[4] != null)
                doc.add(new Field("yeartxt", cols[4], Store.YES, Index.NO));
            doc.add(new Field("volume", cols[3], Store.YES, Index.NO));
            doc.add(new Field("title", cols[1], Store.YES, Index.NO));


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
        iw.optimize();
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
     * Retrieves the BHL title id for the specified ANBG publication LSID.
     *
     * Null is returned when no corresponding BHL title could be found.
     *
     * @param pubLsid
     * @return
     */
    public String getTitlePublishedIn(String pubLsid){
        try{
            TermQuery query = new TermQuery(new Term("lsid", pubLsid));
            TopDocs docs = anbgIdxSearcher.search(query, 1);
            if(docs.totalHits>0){
                return anbgIdxSearcher.doc(docs.scoreDocs[0].doc).get("titleid");
            }
        }
        catch(Exception e){}
        return null;
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
    private Reference getEarliestItem(String lsid){
        List<Reference> results = searchForReferences(lsid, "year", 1, false, yearFilter);
        if(results != null && results.size() >0)
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
     * Searches the bhl item index ordering the results by the sortField optionally
     * in reverse order.
     * @param lsid
     * @param sortField
     * @param limit The number of results to return
     * @param reverseSort When true the sort is reversed.
     * @param filter The filter to be applied to the search - used to remove null values in the search field
     * @return The list of publications that match these results
     */
    private List<Reference> searchForReferences(String lsid, String sortField, int limit, boolean reverseSort, Filter filter){
        try{
            Query query = new TermQuery(new Term("lsid", lsid));
            TopFieldDocs topDocs = bhlIdxSearcher.search(query, filter, limit, new Sort(new SortField(sortField, SortField.INT, reverseSort)));
            return createReferenceList(topDocs);
        }
        catch(Exception e){
            logger.error("searchForPublications: " +e.getMessage());
        }
        return null;
    }
    private List<Reference> searchReferenceForTitle(String lsid, String titleId){
        try{
            BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term("lsid", lsid)), Occur.MUST);
            query.add(new TermQuery(new Term("titleid", titleId)), Occur.MUST);
            TopDocs topDocs  = bhlIdxSearcher.search(query, maxDocs);
            return createReferenceList(topDocs);
        }
        catch(Exception e){

        }
        return null;
    }
    private List<Reference> createReferenceList(TopDocs topDocs) throws Exception{
        if (topDocs.totalHits > 0) {
            List<Reference> results = new ArrayList<Reference>(maxDocs);
            Document doc = null;
            for (ScoreDoc sdoc : topDocs.scoreDocs) {
                doc = bhlIdxSearcher.doc(sdoc.doc);
                Reference r = new Reference();
                r.setIdentifier(doc.get("itemid"));
                r.setScientificName(doc.get("name"));
                r.setTitle(doc.get("title"));
                String year = StringUtils.trimToNull(doc.get("yeartxt"));
                if (year == null) {
                    year = doc.get("year");
                }
                r.setYear(year);
                r.setVolume(doc.get("volume"));
                CollectionUtils.addAll(r.getPageIdentifiers(), sepPattern.split(doc.get("pages")));
                results.add(r);
            }
            return results;
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
