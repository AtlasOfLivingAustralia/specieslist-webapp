package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * List of Australian Ant Genera Document Mapper
 * 
 * @author Tommy Wang (twang@wollemisystems.com)
 */

public class LaagDocumentMapper extends XMLDocumentMapper {
	
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	
	
	public LaagDocumentMapper() {
		
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//h1/text()",subject, Predicates.DC_TITLE);

		addTripleMapping("//h1/text()", subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//td[contains(.,'Phylum:')]/following-sibling::td[1]/span", subject, Predicates.PHYLUM);

		addTripleMapping("//td[contains(.,'Class:')]/following-sibling::td[1]/span", subject, Predicates.CLASS);
		
		addTripleMapping("//td[contains(.,'Order:')]/following-sibling::td[1]/span", subject, Predicates.ORDER);
		
		addTripleMapping("//td[contains(.,'Family:')]/following-sibling::td[1]/span", subject, Predicates.FAMILY);
		
		addTripleMapping("//td[contains(.,'Subfamily:')]/following-sibling::td[1]/span", subject, Predicates.SUBFAMILY);
		
		addTripleMapping("//td[contains(.,'Genus:')]/following-sibling::td[1]/span", subject, Predicates.GENUS);
		
		addTripleMapping("//h2[span[contains(.,'Identification')]]/following-sibling::p[1]", subject, Predicates.MORPHOLOGICAL_TEXT);
		
		addTripleMapping("//h2[span[contains(.,'Distribution')]]/following-sibling::p[1]/img[1]/@src", subject, Predicates.DIST_MAP_IMG_URL);

		addTripleMapping("//tr[td[b[contains(.,'Environments')]]]/following-sibling::tr[1]", subject, Predicates.HABITAT_TEXT);
		
		addTripleMapping("//a[@class='image']/img/@src", subject, Predicates.IMAGE_URL);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);

		documentStr = cleanupSrcString(documentStr);
		
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("</p>[\\s]*<p>", "");
		
		
//		System.out.println(documentStr);
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		String source = "http://ants.csiro.au";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		
		String subject = MappingUtils.getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.csiro.au/org/LegalNoticeAndDisclaimer.html");
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					
					pds.add(imageDoc);
				}

				
				//toRemove.add(triple);
			} else if(predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
					imageDocTriples.add(new Triple(subject,Predicates.DIST_MAP_IMG_URL.toString(), imageDoc.getGuid()));
					imageDoc.setTriples(imageDocTriples);
					pds.add(imageDoc);
				}

				
				//toRemove.add(triple);
			}
		}
		
		
		
		//remove the triple from the triples
		triples.remove(toRemove);
		
		//replace the list of triples
		pd.setTriples(triples);
	}
	
	private String cleanupSrcString(String src) {
		String result = src;

		result = removeJavascript(result);
		result = result.replaceAll("\\]\\]>", "");
		result = result.replaceAll("<!\\[CDATA\\[", "");
		// Clean up invalid unicode characters		
		for (int i = 0; i < result.length(); i++) {
			if (result.charAt(i) > 0xFFFD){   
				result = result.substring(0, i) + result.substring(i+1);  
			} else if (result.charAt(i) < 0x20 && result.charAt(i) != '\t' && result.charAt(i) != '\n' && result.charAt(i) != '\r'){   
				result = result.substring(0, i) + result.substring(i+1);				
			}  
		}

		return result;
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
