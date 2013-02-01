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
 * Wolf Spiders of Australia Document Mapper
 * 
 * @author Tommy Wang (twang@wollemisystems.com)
 */

public class WsaDocumentMapper extends XMLDocumentMapper {
	
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	
	
	public WsaDocumentMapper() {
		
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//h1",subject, Predicates.DC_TITLE);

		addTripleMapping("//h1/i/u/span/text()", subject, Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//h3[contains(.,\"Systematics and Taxonomy\")]/following-sibling::p[1]/i/text()", subject, Predicates.SYNONYM);
		addTripleMapping("//h3[contains(.,\"Identification\")]/following-sibling::p[1]", subject, Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//h3[contains(.,\"Distribution\")]/following-sibling::p[1]", subject, Predicates.DISTRIBUTION_TEXT);
		addTripleMapping("//img/attribute::src", subject, Predicates.IMAGE_URL);

	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		String source = "http://www.lycosidae.info/identification/australia/";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		
		String subject = MappingUtils.getSubject();

		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "DPA - CC-BY-NC");
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Framenau, V.W., The Wolf Spiders of Australia " +
				"(http://www.lycosidae.info/identification/australia/) - Checklist, Taxonomy and Identification. ");
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				
				String creator = getXPathSingleValue(xmlDocument, "//img[@src=\"" + imageUrl + "\"]/preceding-sibling::a/text()");
				
				if (creator == null || "".equals(creator)) {
					creator = getXPathSingleValue(xmlDocument, "//img[@src=\"" + imageUrl + "\"]/following-sibling::a/text()");
				}
				
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
//					imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
					pds.add(imageDoc);
				}

				
				//toRemove.add(triple);
			} else if(predicate.endsWith("hasSynonym")) {
				String currentObj = (String) triple.getObject();
//				System.out.println(currentObj.length());
				if (currentObj.length() <= 1) {
					toRemove.add(triple);
				}
			}
		}
		
		
		
		//remove the triple from the triples
		triples.removeAll(toRemove);
		
		//replace the list of triples
		pd.setTriples(triples);
	}
	
}
