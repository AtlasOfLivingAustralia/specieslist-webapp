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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.ala.dao.SolrUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;
/**
 * Command line utility for testing the lucene indicies.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SolrIndexTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		System.out.print("Quick search test >>> ");
		String input = "";
			
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(!"q".equals((input = br.readLine()))){
			long start = System.currentTimeMillis();
			if(StringUtils.trimToNull(input)!=null){
				System.out.println("---------------------------------------------");
				input = StringUtils.trimToNull(input).toLowerCase();
//				IndexSearcher is = new IndexSearcher("/data/lucene/taxonConcept");
				
				
				String solrHome = "/data/solr/bie";
				
				/**
			     * Initialise the SOLR server instance
			     */
		        System.setProperty("solr.solr.home", solrHome);
		        CoreContainer coreContainer = null;
		        try {
			        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			        coreContainer = initializer.initialize();
		        } catch (Exception e) {
		        	//FIXME this is a hack - there must be a better way of initialising SOLR here
		        	Directory dir = FSDirectory.open(new File(solrHome+"/index")); 
		        	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SolrUtils.BIE_LUCENE_VERSION, new StandardAnalyzer(SolrUtils.BIE_LUCENE_VERSION));
			        IndexWriter idxWriter = new IndexWriter(dir, indexWriterConfig);
			        idxWriter.commit();
			        idxWriter.close();
			        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
			        coreContainer = initializer.initialize();				
		        }
		        
		        EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
		        
		        SolrQuery query = new SolrQuery(input);
		        QueryResponse qr = server.query(query);
		        
		        SolrDocumentList sdl = qr.getResults();
		        Iterator iter = sdl.iterator();
		        while(iter.hasNext()){
		        	SolrDocument doc = (SolrDocument) iter.next();
		        	System.out.print(doc.getFieldValue("guid"));
		        	System.out.print("\t\t");
		        	Object valuesList = doc.getFieldValue("name");
		        	
		        	if(valuesList instanceof List){
			        	List<String> values = (List<String>) valuesList;
			        	for(String s: values){
			        		System.out.println(">>  name: "+ s);
			        	}
		        	} else {
		        		System.out.println(">>  name: "+ valuesList);
		        	}
			        	
		        	System.out.print(doc.getFieldValue("guid"));
		        	System.out.println();
		        }
				
				System.out.println();
			}
			System.out.print("Quick search test >>> ");
		}
		
		System.out.println("bye bye");
		System.exit(1);
	}
}
