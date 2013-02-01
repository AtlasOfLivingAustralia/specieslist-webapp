package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Online photographic guide to marine invertebrates of Southern Australia Document Mapper
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class AusmarinvDocumentMapper extends XMLDocumentMapper {

	public AusmarinvDocumentMapper() {

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//title/text()", subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//p[contains(child::strong,\"Phylum\")]/a/text()|//p[contains(child::strong,\"Phylum\")]/text()", 
				subject, Predicates.PHYLUM);
		
		addTripleMapping("//p[contains(child::strong,\"Class\")]/a/text()|//p[contains(child::strong,\"Class\")]/text()", 
				subject, Predicates.CLASS);
		
		addTripleMapping("//p[contains(child::strong,\"Subclass\")]/a/text()|//p[contains(child::strong,\"Subclass\")]/text()", 
				subject, Predicates.SUBCLASS);
		
		addTripleMapping("//p[contains(child::strong,\"Order\")]/a/text()|//p[contains(child::strong,\"Order\")]/text()", 
				subject, Predicates.ORDER);
		
		addTripleMapping("//p[contains(child::strong,\"Family\")]/a/text()|//p[contains(child::strong,\"Family\")]/text()", 
				subject, Predicates.FAMILY);
		
		addTripleMapping("//p[contains(child::strong,\"Genus\")]/a/text()|//p[contains(child::strong,\"Genus\")]/text()", 
				subject, Predicates.GENUS);
		
		addTripleMapping("//p[contains(child::strong,\"Species\")]/a/text()|//p[contains(child::strong,\"Species\")]/text()", 
				subject, Predicates.SPECIFIC_EPITHET);
		
		addTripleMapping("//p[contains(child::strong,\"Similar species\")]/a/text()|//p[contains(child::strong,\"Similar species\")]/em/text()", 
				subject, Predicates.SIMILAR_SPECIES);
		
		addTripleMapping("//img/attribute::src", 
				subject, Predicates.IMAGE_URL);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<String> imageUrls = new ArrayList<String>();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String subject = triples.get(0).getSubject();
//		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Online Photographic Guide to Marine Invertebrates of Southern Australia");
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.ausmarinverts.net/About.html");

		final int habitatPos = 2;
		int habitatCounter = 0;
		String source = "http://www.ausmarinverts.net/";
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasOrder")) {
				String currentObj = (String) triple.getObject();
				//String newObj = null;

				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasFamily")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasGenus")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasSpecificEpithet")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasPhylum")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasClass")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasSubclass")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;
				
				String[] tmp = currentObj.split(" ");
				
				if(tmp.length > 1) {
					newObj = tmp[1].trim();
					triple.setObject(newObj);
				} else if(!"".equals(cleanColon(currentObj).trim())) {
					triple.setObject(currentObj);
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


	// Clean the : symbols in a String
	private String cleanColon(String src) {

		String result = src;

		result = result.replaceAll(":", "");
		
		return result;
	}
}
