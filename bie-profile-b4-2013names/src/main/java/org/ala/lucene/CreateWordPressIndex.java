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
package org.ala.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.ala.dao.IndexedTypes;
import org.ala.dao.SolrUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Creates a SOLR index for the WordPress static pages by crawling site
 *
 * @author Nick dos Remedios (Nick.dosRemedios@csiro.au)
 */
@Component("createWordPressIndex")
public class CreateWordPressIndex {

    protected static Logger logger = Logger.getLogger(CreateWordPressIndex.class);
    @Inject
	protected SolrUtils solrUtils;
    protected static final String WP_SITEMAP_URI = "http://www.ala.org.au/sitemap.xml";
    protected static final String CONTENT_ONLY_PARAM = "?content-only=1&categories=1";
    protected static final String WP_BASE_URI = "http://www.ala.org.au/?page_id=";
    protected List<String> pageUrls = new ArrayList<String>();
    protected static final List<String> excludedCategories = new ArrayList<String>();
    
    static {
        // post categories that should not be indexed in SOLR
        excludedCategories.add("button");
    }

    public static void main(String[] args) throws Exception {
        // Spring config file locations
        String[] locations = {
				"classpath*:spring.xml"
		};
        // initialise Spring ApplicationContext 
        ApplicationContext context = new ClassPathXmlApplicationContext(locations);
		CreateWordPressIndex cwpi = (CreateWordPressIndex) context.getBean(CreateWordPressIndex.class);
        cwpi.loadWordpress();
        System.exit(0);
    }

    public int loadWordpress() throws Exception {
        logger.info("Start of crawling and indexing WP pages.");
        loadSitemap();
        int pagesLoaded = indexPages();
        logger.info("Crawled and indexed "+ pagesLoaded + " pages.");
        return pagesLoaded;
    }

    /**
     * Read the Google sitemap file on WP site and load up a list
     * of page URL.
     *
     * @throws IOException
     */
    protected void loadSitemap() throws IOException {
        Document doc = Jsoup.connect(WP_SITEMAP_URI).get();
        Elements pages = doc.select("loc");
        logger.info("Sitemap file lists " + pages.size() + " pages.");

        for (Element page : pages) {
            // add it to list of page urls Field
            this.pageUrls.add(page.text());
        }
    }

    /**
     * Index the WP pages by parsing with Jsoup and indexing into SOLR
     *
     * @return
     * @throws IOException
     */
    protected int indexPages() throws Exception {
        int documentCount = 0;
        // Initialise SOLR
        SolrServer solrServer = solrUtils.getSolrServer();
        logger.info("Deleting all WordPress documents in SOLR index...");
        solrServer.deleteByQuery("idxtype:"+IndexedTypes.WORDPRESS); // delete WP pages
        solrServer.commit();

        for (String pageUrl : this.pageUrls) {
            try {
                // Crawl and extract text from WP pages
                Document document = Jsoup.connect(pageUrl + CONTENT_ONLY_PARAM).get();
                String title = document.select("head > title").text();
                String id = document.select("head > meta[name=id]").attr("content");
                String bodyText = document.body().text();
                Elements postCategories = document.select("ul[class=post-categories]");
                List<String> categoriesOut = new ArrayList<String>();
                Boolean excludePost = false;

                if (!postCategories.isEmpty()) {
                    // Is a WP post (not page)
                    Elements categoriesIn = postCategories.select("li > a"); // get list of li elements
                    
                    for (Element cat : categoriesIn) {
                        String thisCat = cat.text();
                        
                        if (thisCat != null && excludedCategories.contains(thisCat)) {  // "button".equals(thisCat)
                            // exclude category "button" posts
                            excludePost = true;
                        } 
                        if (thisCat != null) {
                            // add category to list
                            categoriesOut.add(thisCat.replaceAll(" ", "_"));
                        }
                    }
                }
                
                if (excludePost) {
                    logger.debug("Excluding post (id: " + id + ") with category: " + StringUtils.join(categoriesOut, "|"));
                    continue;
                }
                
                documentCount++;
                // Index with SOLR
                logger.debug(documentCount+ ". Indexing WP page - id: " + id + " | title: " + title + " | text: " + StringUtils.substring(bodyText, 0, 100) + "... ");
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("idxtype", IndexedTypes.WORDPRESS);
                doc.addField("guid", WP_BASE_URI + id); // use page_id based URI instead of permalink in case permalink is too long for id field
                doc.addField("id", "wp" + id); // probably not needed but safer to leave in
                doc.addField("name", title, 1.2f);
                doc.addField("content", bodyText);
                doc.addField("australian_s", "recorded"); // so they appear in default QF search
                doc.addField("categories", categoriesOut);
                // add to index
                solrServer.add(doc);
                
                if (documentCount % 100 == 0) {
                    logger.info("Committing to SOLR (count = " + documentCount + ")...");
                    solrServer.commit();
                }
            } catch (IOException ex) {
                // catch it so we don't stop indexing other pages
                logger.warn("Problem accessing/reading WP page: "+ ex.getMessage(), ex);
            }
        }

        logger.info("Final Committing to SOLR...");
        solrServer.commit();
        //logger.info("Optimising SOLR index...");
        //solrServer.optimize(); // throws errors on my machine??
        logger.info("Committed to SOLR. Final document count: " + documentCount);
        return documentCount;
    }
}
