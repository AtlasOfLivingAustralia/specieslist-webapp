package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;


// Weeds Document Mapper
public class WeedDocumentMapper extends XMLDocumentMapper {

	public WeedDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
		addDCMapping("//h1/em/text()",subject,Predicates.DC_TITLE);
		addTripleMapping("//h1/em/text()",subject,Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//tr[th[contains(.,'Description')]]/following-sibling::tr[1]",subject,Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//td[contains(.,'Distribution:')]/following-sibling::td[1]",subject,Predicates.DISTRIBUTION_TEXT);
		addTripleMapping("//td[contains(.,'Habit:')]/following-sibling::td[1]|//td[contains(.,'Where it grows:')]/following-sibling::td[1]",subject,Predicates.HABITAT_TEXT);
		addTripleMapping("//td[contains(.,'How it spreads:')]/following-sibling::td[1]",subject,Predicates.REPRODUCTION_TEXT);
		addTripleMapping("//td[contains(.,'Distribution Map:')]/following-sibling::td[1]/img/attribute::src",subject,Predicates.DIST_MAP_IMG_URL);
		addTripleMapping("//p[@id=\"photos\"]/img/attribute::src",subject,Predicates.IMAGE_URL);
		addTripleMapping("//td[contains(.,'Family:')]/following-sibling::td[1]",subject,Predicates.FAMILY);		
		addTripleMapping("//td[contains(.,'Genus:')]/following-sibling::td[1]",subject,Predicates.GENUS);
		addTripleMapping("//td[contains(.,'Species:')]/following-sibling::td[1]",subject,Predicates.SPECIES);
		addTripleMapping("//td[contains(.,'Common name:')]/following-sibling::td[1]/text()",subject,Predicates.COMMON_NAME);
		addTripleMapping("//td[contains(.,'synonyms:')]/following-sibling::td[1]/p/text()",subject,Predicates.SYNONYM);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		String subject = MappingUtils.getSubject();
		String source = "http://www.weeds.gov.au";
		
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if (predicate.endsWith("hasImageUrl") || predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			}
		}
		triples.removeAll(toBeRemoved);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullScientificName));
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
		triples.add(new Triple(subject, Predicates.PEST_STATUS.toString(), "invasive"));
	}
}	

