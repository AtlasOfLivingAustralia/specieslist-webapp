package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;


// Weeds Document Mapper
public class WeedsOrgDocumentMapper extends XMLDocumentMapper {

	public WeedsOrgDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
		addDCMapping("//b/i/text()",subject,Predicates.DC_TITLE);
		addTripleMapping("//b/i/text()",subject,Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//b[contains(.,'Family:')]/following-sibling::text()[1]",subject,Predicates.FAMILY);
		addTripleMapping("//b[contains(.,'Flowers/Seedhead:')]/following-sibling::text()[1]",subject,Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//b[contains(.,'Description:')]/following-sibling::text()[1]",subject,Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//b[contains(.,'Dispersal:')]/following-sibling::text()[1]",subject,Predicates.REPRODUCTION_TEXT);
//		addTripleMapping("//table[@cellspacing='10']/tbody[1]/tr[1]/td[2]/img/attribute::src",subject,Predicates.DIST_MAP_IMG_URL);
		addTripleMapping("//img[@hspace!=0]/attribute::src",subject,Predicates.IMAGE_URL);
		
		
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		String subject = MappingUtils.getSubject();
		String source = "http://www.weeds.org.au";
		
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = imageUrl.replaceAll("\\.\\.", "");
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

