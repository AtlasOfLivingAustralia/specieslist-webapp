package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Australian Desert Fishes Descriptions Document Mapper
 * 
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AdfdDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	
	public AdfdDocumentMapper() {

		setRecursiveValueExtraction(true);

		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//center/h2/em/text()", subject, Predicates.DC_TITLE);
		
		addTripleMapping("//center/h2/em/text()", subject, Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//center/h2/text()", subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//h3[contains(.,\"Conservation Status\")]/following-sibling::p[1]", subject, Predicates.CONSERVATION_STATUS);	
		addTripleMapping("//h3[contains(.,\"Reproduction\")]/following-sibling::p[1]", subject, Predicates.REPRODUCTION_TEXT);
		addTripleMapping("//h3[contains(.,\"Size\")]/following-sibling::p[1]", subject, Predicates.MORPHOLOGICAL_TEXT);
		
		addTripleMapping("//h3[contains(.,\"Distribution & Abundance\")]/following-sibling::p[1]|//h3[contains(.,\"Habitat & Ecology\")]/preceding-sibling::p[1]",
				subject, Predicates.DISTRIBUTION_TEXT);

		/*
		 * It is hard to pick up the Habitat-related text from the Habitat & Ecology paragraph as it is mixed with the ecological 
		 * descriptions. Currently, the whole paragraph is retrieved from the web pages. 			
		 */
		
		addTripleMapping("//h3[contains(.,\"Habitat & Ecology\")]/following-sibling::p[1]",subject, Predicates.HABITAT_TEXT);
		addTripleMapping("//p[2]/a[1]/attribute::href",subject, Predicates.IMAGE_URL);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		String source = "http://www.desertfishes.org/australia/fish/";
		String appendix = "";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String subject = triples.get(0).getSubject();
		
		for (Triple<String, String, String> triple : triples) {

			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl") || predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + appendix + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				
				if (currentObj.contains("(") && currentObj.contains(")") || !currentObj.replaceAll("[0-9]{1,}", "").equals(currentObj)) {
					tmpTriple.add(triple);
				} 				
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
}
