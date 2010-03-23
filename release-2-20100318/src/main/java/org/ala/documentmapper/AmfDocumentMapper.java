package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Australian Moths Online Document Mapper. 
 */
public class AmfDocumentMapper extends XMLDocumentMapper {

	public AmfDocumentMapper() {
		// specify the namespaces

		String subject = MappingUtils.getSubject();
		this.contentType = MimeType.HTML.toString();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//title/text()",subject, Predicates.DC_TITLE);

		addTripleMapping("//dt[contains(.,\"Species\")]/following-sibling::dd[1]/em/text()", 
				subject, Predicates.SPECIFIC_EPITHET);

		addTripleMapping("//dt[contains(.,\"Genus\")]/following-sibling::dd[1]/em/text()", 
				subject, Predicates.GENUS);		

		addTripleMapping("//dt[contains(.,\"Family\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.FAMILY);	

//		addTripleMapping("//dt[contains(.,\"Suborder\")]/following-sibling::dd[1]/text()", 
//				subject, Predicates.SUBORDER);	

		addTripleMapping("//dt[contains(.,\"Order\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.ORDER);	

//		addTripleMapping("//dt[contains(.,\"Magnorder\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasMagnorder", false);
//
//		addTripleMapping("//dt[contains(.,\"Cohort\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasCohort", false);
//
//		addTripleMapping("//dt[contains(.,\"Infralegion\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasInfralegion", false);
//
//		addTripleMapping("//dt[contains(.,\"Sublegion\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasSublegion", false);
//
//		addTripleMapping("//dt[contains(.,\"Subdivision\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasSubdivision", false);
//
//		addTripleMapping("//dt[contains(.,\"Division\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasDivision ", false);

//		addTripleMapping("//dt[contains(.,\"Infraclass\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasInfraclass ", false);
//
//		addTripleMapping("//dt[contains(.,\"Subclass\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasSubclass", false);

		addTripleMapping("//dt[contains(.,\"Class\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.CLASS);

//		addTripleMapping("//dt[contains(.,\"Superclass\")]/following-sibling::dd[1]/text()", 
//				Namespaces.ALA, "hasSuperclass", false);

		addTripleMapping("//dt[contains(.,\"Subphylum\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.SUB_PHYLUM);

		addTripleMapping("//dt[contains(.,\"Phylum\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.PHYLUM);

		addTripleMapping("//dt[contains(.,\"Kingdom\")]/following-sibling::dd[1]/text()", 
				subject, Predicates.KINGDOM);

		addTripleMapping("//h3[contains(.,\"Distribution\")]/following-sibling::p[1]/em/text()", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//h3[contains(.,\"Distribution\")]/following-sibling::p[1]/text()", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//h3[contains(.,\"Habitat\")]/following-sibling::p[1]/text()", 
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//div[@id=\"content\"]/text()", 
				subject, Predicates.MORPHOLOGICAL_TEXT);
		
		addTripleMapping("//div[@class=\"related-images\"]/div[1]/img/attribute::src", 
				subject, Predicates.IMAGE_URL);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		Triple<String,String,String> tmpDistributionTextTriple = null;
		
		String subject = triples.get(0).getSubject();

		String fullDistributionText = "";
		String source = "http://australianmuseum.net.au";
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		String scientificName = getTripleObjectLiteral(pds, "hasGenus");

		if (scientificName == null || "".equals(scientificName)) {
			scientificName = getTripleObjectLiteral(pds, "hasFamily");
			
			if (scientificName == null || "".equals(scientificName)) {
				scientificName = getTripleObjectLiteral(pds, "hasSuborder");
				
				if (scientificName == null || "".equals(scientificName)) {
					scientificName = getTripleObjectLiteral(pds, "hasOrder");
				}
			}
		}
		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
		
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("title")) {
				String title = (String) triple.getObject();
				
				title = title.split("-")[0].trim();
				
				triple.setObject(title);
			} else if(predicate.endsWith("hasDistributionText")) {
				String currentObj = (String) triple.getObject();
				if(currentObj!=null){
					if(fullDistributionText.length()>0)
						fullDistributionText +=" ";
					
					fullDistributionText+=currentObj;
				}
				toBeRemoved.add(triple);
				
				
				//String newObj = null;

//				if (fullDistributionText != null) {
//					triple.setObject(fullDistributionText);
//				} else {
//					tmpDistributionTextTriple = triple;
//					tmpDistributionText = currentObj;
//				}
			} else if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + imageUrl;
				//triple.setObject(imageUrl);
				
				imageUrl = imageUrl.replaceAll("medium", "big");
				imageUrl = imageUrl.replaceAll(" ", "%20");
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			} 
//			} else if(predicate.endsWith("hasDistributionText")) {
//				String currentObj = (String) triple.getObject();
//				//String newObj = null;
//
//				if (tmpDistributionText != null) {
//					if (tmpDistributionTextTriple != null) { 
//						fullDistributionText = tmpDistributionText + " " + currentObj;
//						tmpDistributionTextTriple.setObject(fullDistributionText);
//						tmpDistributionTextTriple = triple;
//					} 
//				} else {
//					tmpDistributionText = currentObj;
//				}
//			} 
		}
		triples.removeAll(toBeRemoved);
		
		triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), fullDistributionText));
		
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
	}
}
