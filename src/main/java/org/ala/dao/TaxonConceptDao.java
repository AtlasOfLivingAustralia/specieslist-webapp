package org.ala.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ala.model.CommonName;
import org.ala.model.TaxonConcept;
import org.ala.model.TaxonName;
import org.ala.model.Triple;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.parser.NameParser;
/**
 * Prototype quality Dao in need of refactoring.
 * 
 * @author Dave Martin
 */
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
	
	/**
	 * Support for the A-S-P-O simple method of adding data.
	 * 
	 * @param guid
	 * @param infoSourceId
	 * @param documentId
	 * @param predicate
	 * @param value
	 */
	public void addLiteralValue(String guid, String infoSourceId, String documentId, String predicate, String value) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(guid);
		batchUpdate.put("raw:"+infoSourceId+":"+documentId+":"+predicate, Bytes.toBytes(value));
		tcTable.commit(batchUpdate);
	}
	
	/**
	 * Support for the A-S-P-O simple method of adding data.
	 * 
	 * @param guid
	 * @param infoSourceId
	 * @param documentId
	 * @param predicate
	 * @param value
	 */	
	public void addLiteralValues(String guid, String infoSourceId, String documentId, Map<String, Object> values) throws Exception {
		BatchUpdate batchUpdate = new BatchUpdate(guid);
		Iterator<String> keys = values.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			batchUpdate.put("raw:"+infoSourceId+":"+documentId+":"+key, Bytes.toBytes(guid));
		}
		tcTable.commit(batchUpdate);
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
	 * Retrieve the synonyms for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getSynonymsFor(String guid) throws Exception {
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		Cell cell = row.get(Bytes.toBytes("tc:hasSynonym"));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<TaxonConcept>>(){});
		} 
		return new ArrayList<TaxonConcept>();		
	}
	
	/**
	 * Retrieve the child concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getChildConceptsFor(String guid) throws Exception {
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		Cell cell = row.get(Bytes.toBytes("tc:IsParentTaxonOf"));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<TaxonConcept>>(){});
		} 
		return new ArrayList<TaxonConcept>();
	}	
	
	/**
	 * Retrieve the parent concepts for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */	
	public List<TaxonConcept> getParentConceptsFor(String guid) throws Exception {
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		Cell cell = row.get(Bytes.toBytes("tc:IsChildTaxonOf"));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<TaxonConcept>>(){});
		} 
		return new ArrayList<TaxonConcept>();
	}		
	
	/**
	 * Retrieve the common names for the Taxon Concept with the supplied guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public List<CommonName> getCommonNamesFor(String guid) throws Exception {
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes("tc:")});
		Cell cell = row.get(Bytes.toBytes("tc:VernacularConcept"));
		ObjectMapper mapper = new ObjectMapper();
		if(cell!=null){
			byte[] value = cell.getValue();
			return mapper.readValue(value, 0, value.length, new TypeReference<List<CommonName>>(){});
		} 
		return new ArrayList<CommonName>();
	}	
	
	/**
	 * Store the following taxon concept
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	public boolean create(TaxonConcept tc) throws Exception {
		if(tc.guid==null){
			throw new IllegalArgumentException("Supplied GUID for the Taxon Concept is null.");
		}
		
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
		return true;
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
	
	/**
	 * Add this common name to the Taxon Concept.
	 * 
	 * @param guid
	 * @param commonName
	 * @throws Exception
	 */
	public void addCommonName(String guid, CommonName commonName) throws Exception {
		storeComplexObject(guid, "tc:", "tc:VernacularConcept", commonName, new TypeReference<List<CommonName>>(){});
	}	
	
	/**
	 * Store a complex object, handling duplicates in the list and sorting.
	 * 
	 * TODO this is generic DAO logic. Move to separate util code.
	 * 
	 * @param guid
	 * @param columnFamily
	 * @param columnName
	 * @param object
	 * @param typeReference
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public boolean storeComplexObject(String guid, String columnFamily, String columnName, Comparable object, TypeReference typeReference) throws Exception {
		RowResult row = tcTable.getRow(Bytes.toBytes(guid), new byte[][]{Bytes.toBytes(columnFamily)});
		Cell cell = row.get(Bytes.toBytes(columnName));
		List objectList = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
		
		if(cell!=null){
			byte[] value = cell.getValue();
			objectList = mapper.readValue(value, 0, value.length, typeReference);
		} else {
			objectList = new ArrayList();
		}
		
		if(objectList.contains(object)){
			int idx = objectList.indexOf(object);
			//replace with this version
			objectList.remove(idx);
			objectList.add(object);
		} else {
			objectList.add(object);
		}
		
		//sort the common names
		Collections.sort(objectList);
		//save in HBase
		BatchUpdate batchUpdate = new BatchUpdate(Bytes.toBytes(guid));
		//serialise to json
		String commonNamesAsJson = mapper.writeValueAsString(objectList); 
		batchUpdate.put(columnName, Bytes.toBytes(commonNamesAsJson));
		tcTable.commit(batchUpdate);
		return true;			
	}
	
	/**
	 * Add a synonym to this concept.
	 * 
	 * @param guid
	 * @param synonym
	 * @throws Exception
	 */
	public void addSynonym(String guid, TaxonConcept synonym) throws Exception {
		storeComplexObject(guid, "tc:", "tc:hasSynonym", synonym, new TypeReference<List<TaxonConcept>>(){});
	}	
	
	/**
	 * Add a child taxon to this concept.
	 * 
	 * @param guid
	 * @param childConcept
	 * @throws Exception
	 */
	public void addChildTaxon(String guid, TaxonConcept childConcept) throws Exception {
		storeComplexObject(guid, "tc:", "tc:IsParentTaxonOf", childConcept, new TypeReference<List<TaxonConcept>>(){});
	}	
	
	/**
	 * Add a parent taxon to this concept.
	 * 
	 * @param guid
	 * @param parentConcept
	 * @throws Exception
	 */
	public void addParentTaxon(String guid, TaxonConcept parentConcept) throws Exception {
		storeComplexObject(guid, "tc:", "tc:IsChildTaxonOf", parentConcept, new TypeReference<List<TaxonConcept>>(){});
	}		
	
	/**
	 * Add field update to the batch if the supplied value is not null.
	 * 
	 * @param batchUpdate
	 * @param fieldName
	 * @param value
	 */
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
	
	/**
	 * Retrieve the taxon concept by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
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
		return tc;
	}
	
	/**
	 * Retrieve the Taxon Name for the supplied GUID.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public TaxonName getTaxonNameFor(String guid) throws Exception {
		
		RowResult rowResult = tcTable.getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}		
		
		TaxonName tn = new TaxonName();
		tn.guid = DaoUtils.getField(rowResult, "tn:guid");
		tn.nameComplete = DaoUtils.getField(rowResult, "tn:nameComplete");
		tn.nomenclaturalCode = DaoUtils.getField(rowResult, "tn:nomenclaturalCode");
		tn.rankString = DaoUtils.getField(rowResult, "tn:rankString");
		tn.typificationString = DaoUtils.getField(rowResult, "tn:typificationString");
		return tn;
	}

	/**
	 * Retrieve all properties for this row as a Map. Useful for debug
	 * interfaces only.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getPropertiesFor(String guid) throws Exception {
		RowResult rowResult = tcTable.getRow(guid.getBytes());
		if(rowResult==null){
			return null;
		}
		
		//treemaps are sorted
		TreeMap<String, String> properties = new TreeMap<String,String>();
		
		Set<Map.Entry<byte[], Cell>> entrySet = rowResult.entrySet();
		Iterator<Map.Entry<byte[], Cell>> iter = entrySet.iterator();
		while(iter.hasNext()){
			Map.Entry<byte[], Cell> entry = iter.next();
			properties.put(new String(entry.getKey()), new String(entry.getValue().getValue()));
		}
		
		//sort by key
		return properties;
	}
	
	/**
	 * Search for taxon concept with the following scientific name.
	 * 
	 * @param scientificName
	 * @param limit
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * Get TaxonConcept GUID by genus and scientific name.
	 * 
	 * @param scientificName
	 * @return
	 * @throws Exception
	 */
	public static String findConceptIDForName(String genus, String scientificName) throws Exception {
		try {
			String searchName = scientificName;
			//change A. bus to Aus bus
			if(genus!=null){
				NameParser np = new NameParser();
				ParsedName parsedName = np.parse(scientificName);
				if(parsedName!=null){
					if(parsedName.isBinomial()){
						searchName = genus +" "+ parsedName.getSpecificEpithet();
					}
				}
			}
			
			IndexSearcher is = new IndexSearcher("/data/lucene/taxonConcept");
			QueryParser qp  = new QueryParser("scientificName", new KeywordAnalyzer());			
			Query q = qp.parse("\""+searchName.toLowerCase()+"\"");
			
			TopDocs topDocs = is.search(q, 20);
			
			for(ScoreDoc scoreDoc: topDocs.scoreDocs){
				Document doc = is.doc(scoreDoc.doc);
//				Field hasSynonym = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasSynonym");
//				if(hasSynonym!=null){
//					System.out.println("Returning synonym");
//					return hasSynonym.stringValue();
//				}
//				Field hasVernacular = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#HasVernacular");
//				if(hasVernacular!=null){
//					System.out.println("Returning vernacular");
//					return hasVernacular.stringValue();
//				}
//				Field isCongruentTo = doc.getField("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsCongruentTo");
//				if(isCongruentTo!=null){
//					System.out.println("Returning congruent");
//					return isCongruentTo.stringValue();
//				}
//			System.out.println("Doc Id: "+scoreDoc.doc);
//			System.out.println("Guid: "+doc.getField("guid").stringValue());
//			System.out.println("Name: "+doc.getField("scientificName").stringValue());
//			System.out.println("Raw name: "+doc.getField("scientificNameRaw").stringValue());
//			System.out.println("#################################");
				return doc.getField("guid").stringValue();
			}
		} catch (Exception e) {
			System.err.println("Problem searching with:"+scientificName+" : "+e.getMessage());
		}		
		return null;
	}	
	
	/**
	 * Retrieve a list of concepts associated with this name guid.
	 *  
	 * @param nameGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getByNameGuid(String nameGuid, int limit) throws Exception {
		return searchTaxonConceptIndexBy("tc:hasName", nameGuid, limit); 
	}

	/**
	 * Retrieve a list of concepts with the supplied parent guid.
	 * 
	 * @param parentGuid
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public List<TaxonConcept> getByParentGuid(String parentGuid, int limit) throws Exception {
		if(parentGuid==null){
			parentGuid = "NULL";
		}
		return searchTaxonConceptIndexBy("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf", parentGuid, limit);
	}
	
	/**
	 * Search the index with the supplied value targetting a specific column.
	 * 
	 * @param columnName
	 * @param value
	 * @param limit
	 * @return
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	private List<TaxonConcept> searchTaxonConceptIndexBy(String columnName, String value, int limit)
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
	
	/**
	 * Populate a TaxonConcept from the data in the lucene index.
	 * 
	 * @param doc
	 * @return
	 */
	private TaxonConcept createTaxonConceptFromIndex(Document doc) {
		TaxonConcept taxonConcept = new TaxonConcept();
		taxonConcept.guid = doc.get("guid");
		taxonConcept.parentGuid = doc.get("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsChildTaxonOf");
		taxonConcept.nameString = doc.get("scientificNameRaw");
		
		Field[] fields = doc.getFields("http://rs.tdwg.org/ontology/voc/TaxonConcept#IsParentTaxonOf");
		taxonConcept.hasChildren = (fields==null || fields.length==0) ? false : true;
		
		return taxonConcept;
	}

	/**
	 * Delete the TaxonConcept for the supplied guid
	 * 
	 * @param guid
	 * @return true if a delete was performed
	 * @throws Exception
	 */
	public boolean delete(String guid) throws Exception {
		if(tcTable.exists(Bytes.toBytes(guid))){
			tcTable.deleteAll(Bytes.toBytes(guid));
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param triples
	 * @throws Exception
	 */
	public boolean syncTriples(String infoSourceId, String documentId, List<Triple> triples) throws Exception {

		String scientificName = null;
		String genus = null;
		String family = null;
		String order = null;
		
		//iterate through triples and find scientific names and genus
		for(Triple triple: triples){
			
			String predicate = triple.predicate.substring(triple.predicate.indexOf("}")+1);
			
			if("hasOrder".equals(predicate)){
				order = triple.object;
			}
			if("hasFamily".equals(predicate)){
				family = triple.object;
			}
			if("hasGenus".equals(predicate)){
				genus = triple.object;
			}
			if("hasScientificName".equals(predicate)){
				scientificName = triple.object;
			}
		}
		
		if(scientificName==null 
				&& genus==null
				&& family==null
				&& order==null){
			System.err.println("No classification found");
		}
		
		String guid = findConceptIDForName(genus, scientificName);
		if(guid!=null){
			
			Map<String, Object> properties = new HashMap<String,Object>();
			
			for(Triple triple: triples){
				properties.put(triple.predicate, triple.object);
			}
			
			addLiteralValues(guid, infoSourceId, documentId, properties);
			
			return true;
		} else {
			return false;
		}
		
		//split into separate triples
		
		//find the hasScientificName, hasGenus triple
		
		//lookup concept
	}	
}