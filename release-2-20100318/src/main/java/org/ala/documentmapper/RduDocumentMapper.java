package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;

import org.ala.util.Response;
import org.ala.util.WebUtils;

// Document mapper for Reptiles Down Under
public class RduDocumentMapper extends XMLDocumentMapper {

	public RduDocumentMapper() {

		setRecursiveValueExtraction(true);
		
		String subject = MappingUtils.getSubject();
		
		/*
		 *  It's very likely that some of the pages may be badly structured. For example, the sample page we use doesn't have a <p> tag
		 *  for the habitat text whereas the other pages do. Therefore, sometimes the xPath expressions below may get wrong mappings. I 
		 *  haven't worked out a method to work around this issue as the <p> tag can be missing for every paragraph in the content div. 
		 */
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//h1[@id=\"speciesTitle\"]/span/text()", 
				subject, Predicates.DC_TITLE);
		
		addTripleMapping("//div[@class=\"node content\"]/table[1]/tbody[1]/tr[1]/td[2]/img/attribute::src", 
				subject, Predicates.IMAGE_URL);
		
		addTripleMapping("//h1[@id=\"speciesTitle\"]/span/text()", 
				subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//span[@id=\"scientificName\"]/text()", 
				subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//span[@id=\"author\"]/text()", 
				subject, Predicates.AUTHOR);
		
		addTripleMapping("//h3[contains(.,\"Description\")]/following-sibling::p[1]", 
				subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("concat(//h3[contains(.,\"Similar species\")]/following-sibling::p[1]/text(),//h3[contains(.,\"Similar species\")]/following-sibling::p[1]/i/text())", 
				subject, Predicates.SIMILAR_SPECIES);
		
		addTripleMapping("//h3[contains(.,\"Range\")]/following-sibling::p[1]/text()|//h3[contains(.,\"Found in the following Australian states/territories\")]/following-sibling::p[1]/text()", 
				subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("//h3[contains(.,\"Habitat\")]/following-sibling::p[1]/text()", 
				subject, Predicates.HABITAT_TEXT);
		
		addTripleMapping("//div[@id=\"map\"]/img[1]/attribute::src", 
				subject, Predicates.DIST_MAP_IMG_URL);
		
		addTripleMapping("//h3[contains(.,\"Conservation status\")]/following-sibling::p[1]", 
				subject, Predicates.CONSERVATION_STATUS);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		documentStr = cleanupSrcString(documentStr);


		//System.out.println(documentStr);

		content = documentStr.getBytes();

		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		String source = "http://www.reptilesdownunder.com";
		String subject = MappingUtils.getSubject();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasAuthor")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				newObj = currentObj.replaceAll("-", "");
				newObj = newObj.replaceAll("\\(", "");
				newObj = newObj.replaceAll("\\)", "");
				
				triple.setObject(newObj.trim());
					
			} else if(predicate.endsWith("hasSimilarSpecies")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.trim().replaceAll("\\( ", "(");
				newObj += ")";
				triple.setObject(newObj);
			} else if(predicate.endsWith("hasDistributionMapImageUrl")) {
				String currentObj = (String) triple.getObject();
				String newObj = source + currentObj;
				
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, newObj);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}

				tmpTriple.add(triple);
			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}

				//tmpTriple.add(triple);
			} 
		}
	
		triples.add(new Triple(subject,Predicates.KINGDOM.toString(), "Animalia"));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
	
		//replace the list of triples
		pd.setTriples(triples);
	}
	
	private String cleanupSrcString(String src) {
		String result = src;

		// Clean up invalid unicode characters		
		for (int i = 0; i < result.length(); i++) {
			if (result.charAt(i) > 0xFFFD){   
				result = result.substring(0, i) + result.substring(i+1);  
			} else if (result.charAt(i) < 0x20 && result.charAt(i) != '\t' && result.charAt(i) != '\n' && result.charAt(i) != '\r'){   
				result = result.substring(0, i) + result.substring(i+1);				
			}  
		}

		return result;
	}	
}
