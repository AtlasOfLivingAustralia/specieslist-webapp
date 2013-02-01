package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encyclopedia of Life Document Mapper
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 */

public class EolDocumentMapper extends XMLDocumentMapper {
	String subject = MappingUtils.getSubject();
	
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */	

	public EolDocumentMapper() {

		setRecursiveValueExtraction(true);		

		//set the content type this doc mapper handles
		this.contentType = MimeType.XML.toString();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//taxonConcept/ScientificName/text()",subject, Predicates.DC_TITLE);

		addTripleMapping("//taxonConcept/ScientificName/text()", subject, Predicates.SCIENTIFIC_NAME);
//		addTripleMapping("//taxonConcept/commonName[@lang=\"en\"]/text()", subject, Predicates.COMMON_NAME);
//		addTripleMapping("//dataObject/dataType[contains(.,\"http://purl.org/dc/dcmitype/StillImage\")]/following-sibling::source/text()", subject, Predicates.IMAGE_PAGE_URL);
//		addTripleMapping("//dataObject/dataType[contains(.,\"http://purl.org/dc/dcmitype/StillImage\")]/following-sibling::mediaURL/text()", subject, Predicates.IMAGE_URL);
		addTripleMapping("//dataObject/dataType[contains(.,\"http://purl.org/dc/dcmitype/Text\")]/following-sibling::description", subject, Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//dataObject/dataType[contains(.,\"http://purl.org/dc/dcmitype/MovingImage\")]/following-sibling::mediaURL/text()", subject, Predicates.VIDEO_PAGE_URL);
	}

	/**
	 * @see org.ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		
		extractNonFlickrImage(pd, xmlDocument);
		
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();

		String subject = MappingUtils.getSubject();

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();

				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					String imgGuid = imageDoc.getGuid();
					
					String imgCreator = getXPathSingleValue(xmlDocument, "//dataObject/mediaURL[contains(.,\"" + imgGuid + "\")]/preceding-sibling::agent[@role=\"photographer\"]/text()");
					String imgLicense = getXPathSingleValue(xmlDocument, "//dataObject/mediaURL[contains(.,\"" + imgGuid + "\")]/preceding-sibling::license/text()");
					String imgDescription = getXPathSingleValue(xmlDocument, "//dataObject/mediaURL[contains(.,\"" + imgGuid + "\")]/preceding-sibling::description/text()");
					
					if (imgCreator != null) {
						imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), imgCreator);
					}
					
					if (imgLicense != null) {
						imageDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), imgLicense);
					}
					
					if (imgDescription != null && !imgDescription.contains("<") && !imgDescription.contains(">")) {
						List<Triple<String,String,String>> imgTriples = imageDoc.getTriples();
						
						imgTriples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), imgDescription));
					}
					
					pds.add(imageDoc);
				}

				//toRemove.add(triple);
			} else if(predicate.endsWith("hasDescriptiveText")) {
				String currentObj = (String) triple.getObject();
//								System.out.println(currentObj.length());
				String newObj = cleanContent(currentObj);				
//				if (newObj.matches("<[a-zA-Z0-9\\s=\"';:/\\.\\%\\?\\-_]{1,}>")) {
				if (newObj.contains("<") && newObj.contains(">")) {
//					System.out.println("MATCH");
					toRemove.add(triple);
				} else {
					triple.setObject(newObj);
				}
			}
		}

		//		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		//remove the triple from the triples
		triples.removeAll(toRemove);

		//replace the list of triples
		pd.setTriples(triples);
	}

	private String cleanContent(String str) {
		String result = null;

		result = str.replaceAll("\\&[a-zA-Z]{1,};", "");
		//result = result.replaceAll("<[a-zA-Z\\s=\":/\\.%0-9?_]{1,}>", "");
		result = result.replaceAll("[\\s]{2,}", " ");
		result = result.trim();

		return result;
	}

	private void createTriples(ParsedDocument parsedDoc, Mapping mapping, String value) {		
		value = StringUtils.trimToNull(value);
		
		if(value!=null){
			for (Predicates predicate : mapping.predicates) {				
				Triple<String,String,String> triple = new Triple(mapping.subject,predicate.toString(), value);
				parsedDoc.getTriples().add(triple);
			}
		}
	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nl = eElement.getElementsByTagName(sTag);
		if(nl == null || nl.getLength() < 1){
			return null;
		}
		
		NodeList nlList = nl.item(0).getChildNodes();	 
	    Node nValue = (Node) nlList.item(0);	 
	    return nValue.getNodeValue();
	}

	private List<String> getTagValues(String sTag, Element eElement) {
		List<String> list = new ArrayList<String>();
		NodeList nl = eElement.getElementsByTagName(sTag);
		if(nl == null || nl.getLength() < 1){
			return null;
		}
		
		for(int i = 0; i < nl.getLength(); i++){
			NodeList nlList = nl.item(i).getChildNodes();	 
		    Node nValue = (Node) nlList.item(0);
		    list.add(nValue.getNodeValue());
		}
	    return list;
	}

	private void extractNonFlickrImage(ParsedDocument pd, Document document){
		NodeList nList = document.getElementsByTagName("dataObject");
		for (int temp = 0; temp < nList.getLength(); temp++) {	 
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
				Element eElement = (Element) nNode;
				String dataType = getTagValue("dataType", eElement);
				if(dataType != null && dataType.trim().toLowerCase().contains("http://purl.org/dc/dcmitype/StillImage".toLowerCase())){
					String source = getTagValue("dc:source", eElement);
					// image source != flickr, bold and wikipedia
					if(source != null && !source.contains("flickr.com")
							&& !source.contains("boldsystems.org") && !source.contains("wikipedia.org") && !source.contains("wikimedia.org")){
						List<String> mediaURLs = getTagValues("mediaURL", eElement);
						//eol local copied image url
						if(mediaURLs.size() > 1){
							createTriples(pd, new Mapping("", subject, Predicates.IMAGE_URL), mediaURLs.get(1));
						}
						//eol source image url
						else if(mediaURLs.size() > 0){
							createTriples(pd, new Mapping("", subject, Predicates.IMAGE_URL), mediaURLs.get(0));
						}
					}
				}			      
			}
		}				
	}	
}
