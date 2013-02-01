package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

/**
 * JCU Flora and Fauna Document Mapper
 * 
 * @author Tommy Wang (twang@wollemisystems.com)
 */

public class JcuDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	

	public JcuDocumentMapper() {

		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();

		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//h1/text()",subject, Predicates.DC_TITLE);

		addTripleMapping("//h1/text()", subject, Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//td[p[contains(.,\"FAMILY\")]]/following-sibling::td[1]/p/text()", subject, Predicates.FAMILY);
		addTripleMapping("//td[p[contains(.,\"Common name\")]]/following-sibling::td[1]/p/text()", subject, Predicates.COMMON_NAME);
		addTripleMapping("//td[p[contains(.,\"Main colour\")]]/following-sibling::td[1]/p/text()" +
				"|//td[p[contains(.,\"Colour\")]]/following-sibling::td[1]/p/text()", subject, Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//td[p[contains(.,\"Distinctive feature\")]]/following-sibling::td[1]/p/text()" +
				"|//td[p[contains(.,\"Distinguishing features\")]]/following-sibling::td[1]/p/text()" +
				"|//td[p[contains(.,\"Mature weight\")]]/following-sibling::td[1]/p/text()" +
				"|//td[p[contains(.,\"Scat shape\")]]/following-sibling::td[1]/p/text()", subject, Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//td[p[contains(.,\"Habitat\")]]/following-sibling::td[1]/p/text()", subject, Predicates.HABITAT_TEXT);
		addTripleMapping("//td[p[contains(.,\"Description\")]]/following-sibling::td[1]/p/text()" +
				"|//h3[contains(.,\"Description\")]/following-sibling::p[1]", subject, Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//div[@id=\"content\"]//img/attribute::src", subject, Predicates.IMAGE_URL);
		addTripleMapping("//td[p[contains(.,\"Campus\")]]/following-sibling::td[1]/p/text()" +
				"|//td[p[contains(.,\"Location\")]]/following-sibling::td[1]/p/text()", subject, Predicates.LOCALITY);

	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		documentStr = removeJavascript(documentStr);

		//System.out.println(documentStr);

		content = documentStr.getBytes();

		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		String source = "http://www-public.jcu.edu.au";

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();

		String subject = MappingUtils.getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "DPA - non commercial");
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Commercial use by request. Discovernature, James Cook University. " +
				"http://cms.jcu.edu.au/discovernature/index.htm");

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				if (!imageUrl.contains("prev_slp") && ! imageUrl.contains("slp_next")) {
					triple.setObject(imageUrl);

					//retrieve the image and create new parsed document
					ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
					if(imageDoc!=null){

						pds.add(imageDoc);
					}
				}

				toRemove.add(triple);
			} 
		}

		//		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		//remove the triple from the triples
		triples.removeAll(toRemove);

		//replace the list of triples
		pd.setTriples(triples);
	}

	private String removeJavascript(String str) {
		String result = str;
		String commentStartsWith = "<script";
		String commentEndsWith = "</script>";

		int startIndex = result.indexOf(commentStartsWith);
		int endIndex = 0;

		while (startIndex != -1) {
			endIndex = result.indexOf(commentEndsWith) + 9;
			result = result.substring(0,startIndex) + result.substring(endIndex, result.length());

			startIndex = result.indexOf(commentStartsWith);
		}
		//System.out.println(result);

		return result;
	}

}
