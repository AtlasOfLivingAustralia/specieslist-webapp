package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;


// Anatomical Atlas
public class AnatomicalAtlasDocumentMapper extends XMLDocumentMapper {

	public AnatomicalAtlasDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
//		addDCMapping("//h1[1]/em/text()|//h2[1]/em/text()",subject,Predicates.DC_TITLE);
//		addTripleMapping("//div[@id=\"contents\"]/p[strong[contains(.,\"Family\")]]/text()",subject,Predicates.FAMILY);
//		addTripleMapping("//h1[1]/em/text()|//h2[1]/em/text()",subject,Predicates.SCIENTIFIC_NAME);
//		//addTripleMapping("//div[@id=\"contents\"]/h2/span/text()",subject,Predicates.SCIENTIFIC_NAME);
//		addTripleMapping("//h2[contains(.,\"Distribution\")]/following-sibling::p[1]",subject,Predicates.DISTRIBUTION_TEXT);
//		addTripleMapping("//h2[em[contains(.,\"Morphology\")]]/following-sibling::p[1]",subject,Predicates.MORPHOLOGICAL_TEXT);
//		addTripleMapping("//img[@alt=\"\"]/attribute::src",subject,Predicates.IMAGE_URL);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		String subject = MappingUtils.getSubject();
//		String source = "http://larval-fishes.australianmuseum.net.au";
//		
//		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

//		for (Triple<String,String,String> triple: triples) {
//			String predicate = triple.getPredicate().toString();
//			if (predicate.endsWith("hasImageUrl")) {
//				String imageUrl = (String) triple.getObject();
//				//correct the image URL
//				imageUrl = source + imageUrl;
//				triple.setObject(imageUrl);
//				
//				//retrieve the image and create new parsed document
//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
//				if(imageDoc!=null){
//					pds.add(imageDoc);
//				}
//				
//				toBeRemoved.add(triple);
//			}
//		}
//		triples.removeAll(toBeRemoved);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullScientificName));
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), "Diptera"));
		triples.add(new Triple(subject, Predicates.ORDER.toString(), "Diptera"));
		triples.add(new Triple(subject, Predicates.CLASS.toString(), "INSECTA"));
		triples.add(new Triple(subject, Predicates.PHYLUM.toString(), "ARTHROPODA"));
		triples.add(new Triple(subject, Predicates.IMAGE_URL.toString(), "http://www.ces.csiro.au/biology/fly/flyGlossary.html"));
	}
}	

