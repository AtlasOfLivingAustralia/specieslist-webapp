package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;


// OzAnimals Document Mapper
public class OzAnimalsDocumentMapper extends XMLDocumentMapper {

	public OzAnimalsDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
		addDCMapping("//title/text()",subject,Predicates.DC_TITLE);
		addTripleMapping("//h1/b/text()|//b[contains(.,'Other Names')]/following-sibling::text()[2]",subject,Predicates.COMMON_NAME);
		addTripleMapping("//h1/i",subject,Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//td[contains(.,'Class:')]/following-sibling::td[1]/text()",subject,Predicates.CLASS);
		addTripleMapping("//td[contains(.,'Order:')]/following-sibling::td[1]/text()",subject,Predicates.ORDER);
		addTripleMapping("//td[contains(.,'Family:')]/following-sibling::td[1]/text()",subject,Predicates.FAMILY);
		addTripleMapping("//td[contains(.,'Genus:')]/following-sibling::td[1]/text()",subject,Predicates.GENUS);
		addTripleMapping("//td[contains(.,'Species:')]/following-sibling::td[1]/text()",subject,Predicates.SPECIES);
		addTripleMapping("//b[contains(.,'Description')]/following-sibling::text()[2]",subject,Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//b[contains(.,'Size')]/following-sibling::text()[2]",subject,Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//b[contains(.,'Habitat')]/following-sibling::text()[2]",subject,Predicates.HABITAT_TEXT);
		addTripleMapping("//b[contains(.,'Food')]/following-sibling::text()[2]",subject,Predicates.DIET_TEXT);
		addTripleMapping("//b[contains(.,'Breeding')]/following-sibling::text()[2]",subject,Predicates.REPRODUCTION_TEXT);
		addTripleMapping("//b[contains(.,'Range')]/following-sibling::text()[2]",subject,Predicates.DISTRIBUTION_TEXT);
		addTripleMapping("//img[@width=500]/attribute::src",subject,Predicates.IMAGE_URL);
		addTripleMapping("//td[@class='TDgrey']/img/attribute::src",subject,Predicates.DIST_MAP_IMG_URL);
		addTripleMapping("concat(//img[@width=500]/following-sibling::font/text(),//img[@width=500]/following-sibling::font/a/attribute::href)",subject,Predicates.IMAGE_LICENSE_INFO);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		String subject = MappingUtils.getSubject();
//		String source = "http://www.weeds.gov.au";
		
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
//				imageUrl = source + imageUrl;
				
				String tmpStr = getXPathSingleValue(xmlDocument, "concat(//img[@width=500]/following-sibling::font/text(),//img[@width=500]/following-sibling::font/a/attribute::href)");
				
				String creator = null;
				String license = null;
				
				if (tmpStr != null && !"".equals(tmpStr) && tmpStr.contains("-")) {
					creator = tmpStr.split("\\-", 2)[0].replaceAll("Image by", "").trim();
					license = tmpStr.split("\\-", 2)[1].trim();
				}
				
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
								
				if(imageDoc!=null){
					imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), license);
					imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			} else if (predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
//				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
					imageDocTriples.add(new Triple(subject,Predicates.DIST_MAP_IMG_URL.toString(), imageDoc.getGuid()));
					imageDoc.setTriples(imageDocTriples);
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			} else if (predicate.endsWith("hasImageLicenseInfo")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("Image by", "");
				
				triple.setObject(newObj.trim());
				
			}
		}
		triples.removeAll(toBeRemoved);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullScientificName));
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
//		triples.add(new Triple(subject, Predicates.PEST_STATUS.toString(), "invasive"));
	}
}	

