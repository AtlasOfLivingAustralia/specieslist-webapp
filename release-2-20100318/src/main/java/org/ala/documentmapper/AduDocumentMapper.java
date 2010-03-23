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
 * Ants Down Under Document Mapper
 * 
 * @author Tommy Wang (twang@wollemisystems.com)
 */

public class AduDocumentMapper extends XMLDocumentMapper {
	
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	
	
	public AduDocumentMapper() {
		
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//span[@id=\"ctl00_ContentPlaceHolder1_PageTitle\"]/div[1]/text()",subject, Predicates.DC_TITLE);

		addTripleMapping("//span[@id=\"ctl00_ContentPlaceHolder1_PageTitle\"]/div[1]/text()", subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//div[@id=\"ctl00_ContentPlaceHolder1_TabContainer1_TabPanel1_TabContent0\"]/table[@border=\"1\"]/tbody[1]/tr[1]/td[1]/img/attribute::src", 
				subject, Predicates.DIST_MAP_IMG_URL);

		addTripleMapping("//div[@id=\"ctl00_ContentPlaceHolder1_TabContainer1_TabPanel1_TabContent0\"]/table[@border=\"0\"]/tbody[1]/tr[1]/td[1]/a/img/attribute::src", 
				subject, Predicates.IMAGE_URL);
//
//		addTripleMapping("getmultimedia\\.aspx\\?id=[0-9]{1,}", MappingType.REGEX, subject, Predicates.IMAGE_URL);
		
		addTripleMapping("//div[@id=\"ctl00_ContentPlaceHolder1_TabContainer1_TabPanel1_TabContent0\"]/div[contains(.,\"Overview\")]/following-sibling::div[1]", 
				subject, Predicates.DESCRIPTIVE_TEXT);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);

		documentStr = cleanupSrcString(documentStr);
		
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
		
		//System.out.println(documentStr);
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		String source = "http://anic.ento.csiro.au/ants/";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		
		String subject = triples.get(0).getSubject();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl") || predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}

				
				//toRemove.add(triple);
			}
		}
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
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
