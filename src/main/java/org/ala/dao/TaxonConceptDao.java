package org.ala.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ala.model.CommonName;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.BatchUpdate;
import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;

public class TaxonConceptDao {

	protected IndexSearcher tcIdxSearcher;
	
	protected IndexSearcher relIdxSearcher;
	
	protected HTable tcTable;
	
	public TaxonConceptDao() throws Exception {

		//FIXME move to dependency injection
		this.tcIdxSearcher = new IndexSearcher("/data/lucene/taxonConcept");
		this.relIdxSearcher = new IndexSearcher("/data/lucene/relationship");
		
		HBaseConfiguration config = new HBaseConfiguration();
    	this.tcTable = new HTable(config, "taxonConcept");
	}

	public boolean isVernacularConcept(String guid) throws Exception {
		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", guid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 1);
		return topDocs.scoreDocs.length>0;		
	}
	
	public List<String> getIsVernacularConceptFor(String vernacularGuid) throws Exception {

		TermQuery toTaxonQuery = new TermQuery(new Term("toTaxon", vernacularGuid));
		TermQuery relQuery = new TermQuery(new Term("relationship", "http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular"));
		BooleanQuery query = new BooleanQuery();
		query.add(toTaxonQuery, Occur.MUST);
		query.add(relQuery, Occur.MUST);
		
		TopDocs topDocs = relIdxSearcher.search(query, 20);
		List<String> guids = new ArrayList<String>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = relIdxSearcher.doc(scoreDoc.doc);
			guids.add(doc.get("fromTaxon"));
		}
		return guids;
	}	
	
	/**
	 * raw:infosource-id:document-id:namespace:propertyName	"value"
	 * 
	 * Examples:
	 * 
	 * raw:123:321:dc:title				"My Document Title"
	 * raw:123:321:dc:identifier			"http://wikipedia.org/12321"
	 * raw:123:321:dc:publisher			"Wikipedia"
	 * raw:123:321:tc:hasCommonName		"Cougar"
	 * raw:123:321:tc:hasDistribution		"USA"
	 * 
	 * Needs to handle multiple values in this format.
	 * 
	 * Document and Infosource IDs come from populated MySQL database
	 * but can be read from the directory structure of raw files.
	 * 
	 * /bie/infosource-id/document-id/RAW
	 * /bie/infosource-id/document-id/DC
	 * /bie/infosource-id/document-id/TRIPLES
	 * 
	 * @param guid for the TaxonConcept
	 * @param infoSourceId for the InfoSource
	 * @param documentId for the Document
	 * @param propertyName e.g. tc:hasName, includes a namespace
	 * @param value the value for the column
	 */
	public void addRawProperty(String guid, String infoSourceId, String documentId, String propertyName, String value) throws Exception {
		tcTable.getRow(guid).put(("raw:"+infoSourceId+documentId+propertyName).getBytes(), new Cell(value, System.currentTimeMillis()));
	}

	
	/**
	 * tn:guid:propertyName
	 */
	public void create(TaxonConcept tc) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(tc.guid.getBytes());
		putIfNotNull(batchUpdate, "tc:parentGuid", tc.parentGuid);
		putIfNotNull(batchUpdate, "tc:nameGuid", tc.nameGuid);
		putIfNotNull(batchUpdate, "tc:nameString", tc.nameString);
		putIfNotNull(batchUpdate, "tc:author", tc.author);
		putIfNotNull(batchUpdate, "tc:authorYear", tc.authorYear);
		putIfNotNull(batchUpdate, "tc:publishedIn", tc.publishedIn);
		putIfNotNull(batchUpdate, "tc:publishedInCitation", tc.publishedInCitation);
		putIfNotNull(batchUpdate, "tc:acceptedConceptGuid", tc.acceptedConceptGuid);
		tcTable.commit(batchUpdate);			
	}

	/**
	 * What about multiple taxon names for each taxon concept???
	 * 
	 * tn:guid:nameComplete
	 * tn:guid:authorship
	 * 
	 * @param guid
	 * @param tn
	 * @throws Exception
	 */
	public void addTaxonName(String guid, TaxonName tn) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		putIfNotNull(batchUpdate, "tn:guid", tn.guid);
		putIfNotNull(batchUpdate, "tn:nameComplete", tn.nameComplete);
		putIfNotNull(batchUpdate, "tn:authorship", tn.authorship);
		putIfNotNull(batchUpdate, "tn:nomenclaturalCode", tn.nomenclaturalCode);
		putIfNotNull(batchUpdate, "tn:typificationString", tn.typificationString);
		putIfNotNull(batchUpdate, "tn:publishedInCitation", tn.publishedInCitation);
		putIfNotNull(batchUpdate, "tn:rankString", tn.rankString);
		tcTable.commit(batchUpdate);			
	}
	
	public void addCommonName(String guid, CommonName cn) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		UUID uuid = UUID.randomUUID();
		putIfNotNull(batchUpdate, "tn:hasVernacularConcept:"+uuid+":guid", cn.guid); //FIXME
		putIfNotNull(batchUpdate, "tn:hasVernacularConcept:"+uuid+":nameString", cn.nameString);
		tcTable.commit(batchUpdate);			
	}	
	
	public void addSynonym(String guid, String synonymGuid, String synonymName) throws Exception {
		
		//check existing synonyms
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		if(row==null){
			System.err.println("Unable to add synonym to: "+guid+", synonymName:"+synonymName+",  synonymGuid:"+synonymGuid);
			return;
		}
		
		Set<byte[]> columns = row.keySet();
		
		int matches = 0;
		//add the synonym if not already there
		for(byte[] column: columns){
			String colString = new String(column);
			if(colString.matches("tc:hasSynonym:[a-z0-9\\-]{1,}:nameString")){
				matches++;
				//compare contents
				Cell cell = row.get(column);
				if(Bytes.toString(cell.getValue()).equals(synonymName)){
					//return
					//System.out.println("Synonym already added: "+synonymName);
					return;
				}
			}
		}
		
		//System.out.println("Synonym adding: "+synonymName+", to "+guid);
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		putIfNotNull(batchUpdate, "tc:hasSynonym:"+matches+":guid", synonymGuid);
		putIfNotNull(batchUpdate, "tc:hasSynonym:"+matches+":nameString", synonymName);
		tcTable.commit(batchUpdate);			
	}	
	
	public void addChildTaxon(String guid, String childGuid, String childName) throws Exception {
		
		
		//check existing synonyms
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		if(row==null){
			System.err.println("Unable to add child taxon to: "+guid+", childName:"+childName+",  childGuid:"+childGuid);
			return;
		}
		
		Set<byte[]> columns = row.keySet();
		int matches = 0;
		
		//add the synonyms already there ?
		for(byte[] column: columns){
			String colString = new String(column);
			if(colString.matches("tc:IsParentTaxonOf:[a-z0-9\\-]{1,}:guid")){
				matches++;
				//compare contents
				Cell cell = row.get(column);
				if(Bytes.toString(cell.getValue()).equals(childGuid)){
					//return
					//System.out.println("Child already added: "+childName);
					return;
				}
			}
		}
		
		//System.out.println("Child adding: "+childName+", to "+guid);
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		putIfNotNull(batchUpdate, "tc:IsParentTaxonOf:"+matches+":guid", childGuid);
		putIfNotNull(batchUpdate, "tc:IsParentTaxonOf:"+matches+":nameString", childName);
		tcTable.commit(batchUpdate);			
	}	
	
	public void addParentTaxon(String guid, String parentGuid, String parentName) throws Exception {
		
		//check existing synonyms
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		if(row==null){
			System.err.println("Unable to add parent taxon to: "+guid+", parentName:"+parentName+",  parentGuid:"+parentGuid);
			return;
		}
		Set<byte[]> columns = row.keySet();
		int matches = 0;
		//add the synonyms already there ?
		for(byte[] column: columns){
			String colString = new String(column);
			if(colString.matches("tc:IsChildTaxonOf:[a-z0-9\\-]{1,}:guid")){
				matches++;
				//compare contents
				Cell cell = row.get(column);
				if(Bytes.toString(cell.getValue()).equals(parentGuid)){
					//return
					//System.out.println("Parent already added: "+parentName);
					return;
				}
			}
		}
		
		//System.out.println("Parent adding: "+parentName+", to "+guid);
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		putIfNotNull(batchUpdate, "tc:IsChildTaxonOf:"+matches+":guid", parentGuid);
		putIfNotNull(batchUpdate, "tc:IsChildTaxonOf:"+matches+":nameString", parentName);
		tcTable.commit(batchUpdate);			
	}		
	
	
	private void putIfNotNull(BatchUpdate batchUpdate, String fieldName, String value) {
		value = StringUtils.trimToNull(value);
		if(value!=null){
			batchUpdate.put(fieldName, Bytes.toBytes(value));
		}
	}
	
	/**
	 * Create a batch of taxon concepts.
	 * 
	 * @param taxonConcepts
	 * @throws Exception
	 */
	public void create(List<TaxonConcept> taxonConcepts) throws Exception {
		for(TaxonConcept tc: taxonConcepts){
			create(tc);
		}
	}	
	
	public String getRDFfor(String guid) throws Exception {
	
		return null;
	}
	
	public TaxonConcept getByGuid(String guid) throws Exception {

		RowResult rowResult = tcTable.getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}
		
		TaxonConcept tc = new TaxonConcept();
		tc.guid = guid;
		tc.author = DaoUtils.getField(rowResult, "tc:author");
		tc.authorYear = DaoUtils.getField(rowResult, "tc:authorYear");
		tc.nameGuid = DaoUtils.getField(rowResult, "tc:hasName"); 
		tc.nameString = DaoUtils.getField(rowResult, "tc:nameString");
		tc.publishedIn = DaoUtils.getField(rowResult, "tc:publishedIn");
		tc.publishedInCitation = DaoUtils.getField(rowResult, "tc:publishedInCitation");
//		//REPLACE WITH HBASE QUERY
//		Query query = new TermQuery(new Term("guid", guid));				
//		TopDocs topDocs = is.search(query, 1);
//		if(topDocs.scoreDocs.length==1){
//			Document doc = is.doc(topDocs.scoreDocs[0].doc);
//			return createTaxonConcept(doc);
//		}
		return tc;
	}

	public List<TaxonConcept> getByScientificName(String scientificName, int limit) throws Exception {

		scientificName = StringUtils.trimToNull(scientificName).toLowerCase();
		if(scientificName==null){
			return new ArrayList<TaxonConcept>();
		}
		
		QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());
		
		String searchString = "\""+scientificName+"\"";
		System.out.println("Search string: "+searchString);
		
		Query query = qp.parse(searchString);
		
		TopDocs topDocs = tcIdxSearcher.search(query, limit);
		System.out.println("Total hits: "+topDocs.totalHits);
		
		List<TaxonConcept> tcs = new ArrayList<TaxonConcept>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = tcIdxSearcher.doc(scoreDoc.doc);
			tcs.add(createTaxonConceptFromIndex(doc));
		}
		System.out.println("TaxonConcepts returned: "+tcs.size());
		
		return tcs;
	}

	public List<TaxonConcept> getByNameGuid(String nameGuid, int limit) throws Exception {
		//FIXME Quickly becoming inconsistent - field names of lucene indexes need to match other properties
		return searchIndexBy("tc:hasName", nameGuid, limit); 
	}

	public List<TaxonConcept> getByParentGuid(String parentGuid, int limit) throws Exception {
		if(parentGuid==null){
			parentGuid = "NULL";
		}
		return searchIndexBy("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf", parentGuid, limit);
	}
	
	private List<TaxonConcept> searchIndexBy(String columnName, String value, int limit)
			throws IOException, CorruptIndexException {
		Query query = new TermQuery(new Term(columnName, value));				
		TopDocs topDocs = tcIdxSearcher.search(query, limit);
		List<TaxonConcept> tcs = new ArrayList<TaxonConcept>();
		for(ScoreDoc scoreDoc: topDocs.scoreDocs){
			Document doc = tcIdxSearcher.doc(scoreDoc.doc);
			tcs.add(createTaxonConceptFromIndex(doc));
		}	
		return tcs;
	}
	

	private TaxonConcept createTaxonConceptFromIndex(Document doc) {
		TaxonConcept taxonConcept = new TaxonConcept();
		taxonConcept.guid = doc.get("guid");
		taxonConcept.parentGuid = doc.get("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf");
		taxonConcept.nameString = doc.get("scientificNameRaw");
		
		Field[] fields = doc.getFields("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsParentTaxonOf");
		taxonConcept.hasChildren = (fields==null || fields.length==0) ? false : true;
		
		return taxonConcept;
	}	
}