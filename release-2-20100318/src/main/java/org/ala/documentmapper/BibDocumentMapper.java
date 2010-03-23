package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

// Document mapper for Birds in Backyards (Australian Museum/BA) 
public class BibDocumentMapper extends XMLDocumentMapper {

	public BibDocumentMapper() {

		setRecursiveValueExtraction(true);

		/*
		 *  It's very likely that some of the pages may be badly structured. For example, the sample page we use doesn't have a <p> tag
		 *  for the habitat text whereas the other pages do. Therefore, sometimes the xPath expressions below may get wrong mappings. I 
		 *  haven't worked out a method to work around this issue as the <p> tag can be missing for every paragraph in the content div. 
		 */

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//h2/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Scientific name:\")]/em/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Family:\")]/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//div[@id=\"content\"]/p[contains(.,\"Order:\")]/text()", 
				subject, Predicates.ORDER);

		addTripleMapping("//h5[contains(.,\"Distribution\")]/following-sibling::p[1]", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//h5[contains(.,\"Habitat\")]/following-sibling::p[1]", 
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//h5[contains(.,\"Similar species\")]/following-sibling::p[1]", 
				subject, Predicates.SIMILAR_SPECIES);

		addTripleMapping("//h5[contains(.,\"Breeding\")]/following-sibling::p[1]", 
				subject, Predicates.REPRODUCTION_TEXT);

		addTripleMapping("//h5[contains(.,\"Feeding\")]/following-sibling::p[1]", 
				subject, Predicates.DIET_TEXT);

		addTripleMapping("//p[@class=\"featureimage\"]/img/attribute::src", 
				subject, Predicates.IMAGE_URL);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		List<String> imageUrls = new ArrayList<String>();

		String subject = triples.get(0).getSubject();

		String source = "http://www.birdsinbackyards.net";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasFamily")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = null;

				if(currentObj.contains("Family:")) {
					String[] tmp = currentObj.split(":");
					if (tmp.length > 1) {
						newObj = tmp[1].trim();
					}
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasOrder")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = null;

				if(currentObj.contains("Order:")) {
					String[] tmp = currentObj.split(":");
					if (tmp.length > 1) {
						newObj = tmp[1].trim();
						triple.setObject(newObj);
					} else {
						tmpTriple.add(triple);
					}
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				//				triple.setObject(imageUrl);
				imageUrls.add(imageUrl);

				//retrieve the image and create new parsed document
				//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
				//				if(imageDoc!=null){
				//					pds.add(imageDoc);
				//				}

				tmpTriple.add(triple);
			} 
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);

		for (String imageUrl : imageUrls) {
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
				pds.add(imageDoc);
			}
		}
	}
}
