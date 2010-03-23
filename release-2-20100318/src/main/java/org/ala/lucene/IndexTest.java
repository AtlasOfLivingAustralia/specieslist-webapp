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
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
/**
 * Command line utility for testing the lucene indicies.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class IndexTest {

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
				IndexSearcher is = new IndexSearcher("/data/lucene/taxonConcept");
				
				QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());
				Query scientificNameQuery = qp.parse("\""+input+"\"");
				
				qp  = new QueryParser("commonName", new KeywordAnalyzer());
				Query commonNameQuery = qp.parse("\""+input+"\"");
				
				Query guidQuery = new TermQuery(new Term("guid", input));
				
				scientificNameQuery = scientificNameQuery.combine(new Query[]{scientificNameQuery,guidQuery, commonNameQuery});
				
				TopDocs topDocs = is.search(scientificNameQuery, 20);
				
				for(ScoreDoc scoreDoc: topDocs.scoreDocs){
					Document doc = is.doc(scoreDoc.doc);
					
					List<Field> fields = doc.getFields();
					for(Field field: fields){
						System.out.println(field.name()+": "+field.stringValue());
					}
					System.out.println("---------------------------------------------");
				}
				System.out.println("Total hits: "+topDocs.totalHits);
				long finish = System.currentTimeMillis();
				System.out.println("Time taken: "+ ((float)(finish-start))/1000+" seconds.");
				System.out.println("---------------------------------------------");
				
				System.out.println();
			}
			System.out.print("Quick search test >>> ");
		}
		
		System.out.println("bye bye");
		System.exit(1);
	}
}
