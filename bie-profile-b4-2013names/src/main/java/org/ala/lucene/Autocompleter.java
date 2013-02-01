/* *************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.lucene;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ala.dao.SolrUtils;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter.Side;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.context.ApplicationContext;

/**
 * Note: not currently in use.
 * 
 * Search term auto-completer, works for single terms (so use on the last term
 * of the query).
 * <p>
 * Returns more popular terms first.
 *
 * @author Mat Mannion, M.Mannion@warwick.ac.uk
 */

/**
 * @since 1 Nov 2011
 * @author mok011
 * 
 * updated code for lucene 3.4.0.
 * 
 */
public final class Autocompleter {

    private static final String GRAMMED_WORDS_FIELD = "words";

    private static final String SOURCE_WORD_FIELD = "sourceWord";

    private static final String COUNT_FIELD = "count";

    private static final String[] ENGLISH_STOP_WORDS = {
    "an", "and", "are", "as", "at", "be", "but", "by", // "a", 
    "for", "if", "in", "into", "is", //  "i",
    "no", "not", "of", "on", "or", "such", //  "s",
    "that", "the", "their", "then", "there", "these", // "t",
    "they", "this", "to", "was", "will", "with"
    };

    private final static String INDEX_DIR_NAME = "/data/lucene/autocomplete";

    private Directory autoCompleteDirectory;

    private IndexReader autoCompleteReader;

    private IndexSearcher autoCompleteSearcher;

    public Autocompleter(String autoCompleteDir) throws IOException {
        this.autoCompleteDirectory = FSDirectory.open(new File(autoCompleteDir), null);       
        File file = new File(autoCompleteDir);

        // Create a dummy index so that we don't get an exception further down
        if (!file.exists()) {
            FileUtils.forceMkdir(file);
            Analyzer analyzer = new StandardAnalyzer(SolrUtils.BIE_LUCENE_VERSION);
        	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, analyzer);
        	Directory dir = FSDirectory.open(file);
        	IndexWriter iw = new IndexWriter(dir, indexWriterConfig);
//            IndexWriter iw = new IndexWriter(file, analyzer, MaxFieldLength.UNLIMITED);
            iw.commit();
            iw.close();
        }

        reOpenReader();
    }
    
    public Autocompleter() throws IOException {
        this(INDEX_DIR_NAME);
    }

    public List<String> suggestTermsFor(String term, Integer maxHits) throws IOException {
        // get the top 5 terms for query
        Query query = new TermQuery(new Term(GRAMMED_WORDS_FIELD, ClientUtils.escapeQueryChars(term)));
        SortField sf = new SortField(COUNT_FIELD, SortField.INT, true);
        Sort sort = new Sort(sf);

        TopDocs docs = autoCompleteSearcher.search(query, null, maxHits, sort);
        List<String> suggestions = new ArrayList<String>();
        for (ScoreDoc doc : docs.scoreDocs) {
            suggestions.add(autoCompleteReader.document(doc.doc).get(SOURCE_WORD_FIELD));
        }

        return suggestions;
    }

    @SuppressWarnings("unchecked")
    public void reIndex(Directory sourceDirectory, String fieldToAutocomplete, boolean createNewIndex)
                throws CorruptIndexException, IOException {
        // build a dictionary (from the spell package)
        IndexReader sourceReader = IndexReader.open(sourceDirectory);

        LuceneDictionary dict = new LuceneDictionary(sourceReader,
                        fieldToAutocomplete);

        // code from
        // org.apache.lucene.search.spell.SpellChecker.indexDictionary(
        // Dictionary)
        IndexWriter.unlock(autoCompleteDirectory);

        // use a custom analyzer so we can do EdgeNGramFiltering
    	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, new Analyzer() {
            public TokenStream tokenStream(String fieldName, Reader reader) {
				TokenStream result = new StandardTokenizer(SolrUtils.BIE_LUCENE_VERSION, reader);
				
				result = new StandardFilter(SolrUtils.BIE_LUCENE_VERSION, result);
				result = new LowerCaseFilter(SolrUtils.BIE_LUCENE_VERSION, result);
				result = new ISOLatin1AccentFilter(result);
				result = new StopFilter(SolrUtils.BIE_LUCENE_VERSION, result, new HashSet<String>(Arrays.asList(ENGLISH_STOP_WORDS)));
				result = new EdgeNGramTokenFilter(result, Side.FRONT,1, 20);
				
				return result;
		    }
    	});
    	if(createNewIndex){
    		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    	}
    	else{
    		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    	}
    	indexWriterConfig.setMaxBufferedDocs(150);
    	IndexWriter writer = new IndexWriter(autoCompleteDirectory, indexWriterConfig);
//        writer.setMergeFactor(300);

        // go through every word, storing the original word (incl. n-grams)
        // and the number of times it occurs
        Map<String, Integer> wordsMap = new HashMap<String, Integer>();

        Iterator<String> iter = (Iterator<String>) dict.getWordsIterator();
        while (iter.hasNext()) {
                String word = iter.next();

                int len = word.length();
                if (len < 3) {
                        continue; // too short we bail but "too long" is fine...
                }

                if (wordsMap.containsKey(word)) {
                        throw new IllegalStateException(
                                        "This should never happen in Lucene 2.3.2");
                        // wordsMap.put(word, wordsMap.get(word) + 1);
                } else {
                        // use the number of documents this word appears in
                        wordsMap.put(word, sourceReader.docFreq(new Term(
                                        fieldToAutocomplete, word)));
                }
        }

        for (String word : wordsMap.keySet()) {
                // ok index the word
                Document doc = new Document();
                doc.add(new Field(SOURCE_WORD_FIELD, word, Field.Store.YES, Field.Index.NOT_ANALYZED)); // orig term
                doc.add(new Field(GRAMMED_WORDS_FIELD, word, Field.Store.YES, Field.Index.ANALYZED)); // grammed
                doc.add(new Field(COUNT_FIELD,
                                Integer.toString(wordsMap.get(word)), Field.Store.NO,
                                Field.Index.NOT_ANALYZED)); // count

                writer.addDocument(doc);
        }

        sourceReader.close();

        // close writer
        writer.optimize();
        writer.close();

        // re-open our reader
        reOpenReader();
    }

    private void reOpenReader() throws CorruptIndexException, IOException {
        if (autoCompleteReader == null) {
            autoCompleteReader = IndexReader.open(autoCompleteDirectory);
        } else {
            autoCompleteReader.reopen();
        }

        autoCompleteSearcher = new IndexSearcher(autoCompleteReader);
    }

    public static void main(String[] args) throws Exception {
        // run this to re-index from the current index, shouldn't need to do
        // this very often
        //autocomplete.reIndex(FSDirectory.getDirectory("/index/live", null), "content");
        Autocompleter autocomplete = new Autocompleter();

        if (true) {
            ApplicationContext context = SpringUtils.getContext();
            TaxonConceptDao tcDao = (TaxonConceptDao) context.getBean(TaxonConceptDao.class);
            System.out.println("Starting re-indexing...");
            System.out.println("creating scientificName index");
            autocomplete.reIndex(FSDirectory.open(new File(INDEX_DIR_NAME), null), "scientificName", true);
            Thread.sleep(2000);
            System.out.println("creating commonName index");
            autocomplete.reIndex(FSDirectory.open(new File(INDEX_DIR_NAME), null), "commonName", false);
            System.out.println("Finished re-indexing...");
        }

        Thread.sleep(2000);
        String term = "rufus";
        System.out.println("autocompleting: "+term+" = "+autocomplete.suggestTermsFor(term, 5));
        term = "frog";
        System.out.println("autocompleting: "+term+" = "+autocomplete.suggestTermsFor(term, 5));
        // prints [steve, steven, stevens, stevenson, stevenage]
    }

}

