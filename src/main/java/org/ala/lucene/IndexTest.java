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
				IndexSearcher is = new IndexSearcher("/data/lucene/taxonConcept");
				
				QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());			
				Query query = qp.parse("\""+input+"\"");
				Query guidQuery = new TermQuery(new Term("guid", input));				
				query = query.combine(new Query[]{query,guidQuery});
				
				TopDocs topDocs = is.search(query, 20);
				
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
