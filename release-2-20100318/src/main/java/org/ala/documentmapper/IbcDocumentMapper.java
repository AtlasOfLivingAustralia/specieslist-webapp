package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

public class IbcDocumentMapper extends XMLDocumentMapper {

	public IbcDocumentMapper() {
		
		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//div[@class=\"status-and-map-species\"]/ul/li/text()", 
				subject, Predicates.CONSERVATION_STATUS);

		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h4[1]/a/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h4[1]/a/text()", 
				subject, Predicates.FAMILY_COMMON_NAME);

		/*
		 * It is hard to get just the English common name as the French, German and Spanish versions are in the same node
		 * as the English one. 
		 */

		addTripleMapping("//div[@id=\"species\"]/div[1]/div[1]/h1[1]/text()|//p[@class=\"other-languages\"]/text()", 
				subject, Predicates.COMMON_NAME);

		addTripleMapping("//div[@class=\"view-content view-content-pictures-in-species\"]/ul[1]/li/a[1]/img[1]/attribute::src", 
				subject, Predicates.IMAGE_PAGE_URL);

		addTripleMapping("//div[@class=\"view-content view-content-videos-in-species\"]/ul[1]/li/a[1]/attribute::href", 
				subject, Predicates.VIDEO_PAGE_URL);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String subject = triples.get(0).getSubject();

		String source = "http://ibc.lynxeds.com";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentCommonName = ((String)triple.getObject()).trim();
				String formattedCommonName = cleanBrackets(currentCommonName);

				if (!formattedCommonName.equals("")){
					triple.setObject(formattedCommonName);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("title")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split("\\|")[0];
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasFamilyCommonName")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split(" ")[0];
				newObj = cleanBrackets(newObj);
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasFamily")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = currentObj.split(" ")[1];
				if(! "".equals(newObj)) {
					triple.setObject(cleanBrackets(newObj));
				} else {
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasImagePageUrl")) {
				String imageUrl = (String) triple.getObject();
				//imageUrl = source + imageUrl;
				
				// Get the link for large images
				imageUrl = imageUrl.replaceAll("thumb", "node");
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				//Cannot do this as the image links contain special characters which is not accepted by apache.commons.httpclient
//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
//				if(imageDoc!=null){
//					pds.add(imageDoc);
//				}
//				tmpTriple.add(triple);
			} else if(predicate.endsWith("hasVideoPageUrl")) {
				String videoUrl = (String) triple.getObject();
				videoUrl = source + videoUrl;
				triple.setObject(videoUrl);
				
			}
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);
	}

	// Clean the ( and ) symbols in a String
	private String cleanBrackets(String src) {

		String result = src;

		result = result.replaceAll("\\(", "");
		result = result.replaceAll("\\)", "");

		return result;
	}
}
