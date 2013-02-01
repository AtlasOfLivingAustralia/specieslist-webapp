package org.ala.documentmapper;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ala.repository.Namespaces;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Document mapper for Fishes of Australian Aquatic Biological
public class AabioDocumentMapper extends XMLDocumentMapper {

	public static final String SCIENTIFIC_NAME_PATTERN = "a-zA-ZÏËÖÜÄÉÈČÁÀÆŒïëöüäåéèčáàæœóú\\.\\-`'%, ;:&#0-9";

	public AabioDocumentMapper() {

	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		//		documentStr = documentStr.replaceAll("<p>[\\s]{0,}</p>", "");

		//		System.out.println(documentStr);

		content = documentStr.getBytes();

		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		String subject = MappingUtils.getSubject();

		DOMSource source = new DOMSource(xmlDocument);
		StringWriter writer = new StringWriter();
		Result result = new StreamResult(writer);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(source, result);
		String documentStr = writer.getBuffer().toString();  

		// Clean up the document string
		documentStr = documentStr.replaceAll("<p>[\\s]{0,}</p>", "");
		documentStr = documentStr.replaceAll("&rsquo;", "'");
		documentStr = documentStr.replaceAll("<st1:[a-zA-Z :\\=\"]{1,}>", "");
		documentStr = documentStr.replaceAll("</st1:[a-zA-Z]{0,}>", "");
		documentStr = documentStr.replaceAll("[\\s]{2,}", " ");
		//        System.out.println(documentStr);

		pds.remove(0);

		Pattern p = Pattern.compile(
				"(?:<p)" +
				"(?: align=\"center\")?" +
				"(?:>[\\s]{0,}<b>[\\s]{0,})" +
				"([" + SCIENTIFIC_NAME_PATTERN + "\\(\\)]{1,})" +
		"(?:[\\s]{0,}</b>[\\s]{0,}</p>)");

		Pattern p2 = Pattern.compile(
				"(?:<td align=\"center\" height=\"40\" valign=\"top\" width=\"212\">[\\s]{0,}<div align=\"center\">[\\s]{0,}<p>[\\s]{0,}<i>[\\s]{0,})" +
				"([" + SCIENTIFIC_NAME_PATTERN + "]{1,})" +
				"(?:[\\s]{0,}</i>[\\s]{0,}</p>[\\s]{0,}</div>[\\s]{0,}</td>[\\s]{0,}<td align=\"center\" height=\"40\" valign=\"top\" width=\"246\">[\\s]{0,}<div align=\"center\">[\\s]{0,}<p>[\\s]{0,})" + 
				"([" + SCIENTIFIC_NAME_PATTERN + "]{1,})" +
				"(?:[\\s]{0,}</p>[\\s]{0,}</div>[\\s]{0,}</td>[\\s]{0,}<td align=\"center\" height=\"40\" valign=\"top\" width=\"172\">[\\s]{0,}<div align=\"center\">[\\s]{0,}<p>[\\s]{0,})?" +
				"([" + SCIENTIFIC_NAME_PATTERN + "]{1,})" +
		"(?:[\\s]{0,}</p>)?");

		Matcher m = p.matcher(documentStr);	
		int searchIdx = 0;
		String family = new String();
		this.uri = getXPathSingleValue(xmlDocument, "//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content");

		while(m.find(searchIdx)){
			int endIdx = m.end();
			//			String found = content.substring(startIdx, endIdx);

			int searchIdx2 = searchIdx;

			//			System.out.println(searchIdx + " " + searchIdx2 + " " + endIdx);
			Matcher m2 = p2.matcher(documentStr);

			while(m2.find(searchIdx2) && m2.end() <= endIdx){
				int endIdx2 = m2.end();
				String speciesName = m2.group(1);
				String commonName = m2.group(2);
				String distribution = m2.group(3);

				ParsedDocument parsedD = new ParsedDocument();

				parsedD.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), uri);
				parsedD.setGuid(uri);
				parsedD.setContentType(contentType);


				List<Triple<String,String,String>> triples = parsedD.getTriples();

				triples.add(new Triple(subject, Predicates.FAMILY.toString(), family));
				triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), speciesName));
				triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), distribution));

				String[] commonNames = commonName.split(",");
				for (String cName : commonNames) {
					triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), cName));
				}

				parsedD.setTriples(triples);

				pds.add(parsedD);

				//				System.out.println("Family: " + family + "! Species: " + speciesName + "! common name: " + commonName + "! distribution: " + distribution);

				searchIdx2 = endIdx2;
			}

			family = m.group(1);

			//			System.out.println(family);
			searchIdx = endIdx;
		}
		if (pds.isEmpty()) {
			String xpathAsString = "//td[@width=\"140\"]/em/text()";
			List<ParsedDocument> pdocs = extractNode(subject, xpathAsString, xmlDocument);

			pds.addAll(pdocs);
		}

		//		for (String imageUrl : imageUrls) {
		//			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
		//			if(imageDoc!=null){
		//				pds.add(imageDoc);
		//			}
		//		}
	}

	protected List<ParsedDocument> extractNode(String subject, String xpathAsString, Document xmlDocument) throws Exception {
		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();

		setRecursiveValueExtraction(true);

		String source = "http://www.aabio.com.au/";

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		NodeList nodes = (NodeList) xpath.evaluate(xpathAsString, xmlDocument, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); i++) {

			ParsedDocument parsedD = new ParsedDocument();

			parsedD.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), uri);
			parsedD.setGuid(uri);
			parsedD.setContentType(contentType);


			List<Triple<String,String,String>> triples = parsedD.getTriples();

			Node node = nodes.item(i);
			//			System.out.println(node.getNodeValue());
			String genus = node.getNodeValue();
			triples.add(new Triple(subject, Predicates.GENUS.toString(), genus.trim()));

			Node parentNode = node.getParentNode().getParentNode();
			Node nextNode = parentNode.getNextSibling().getNextSibling();
			Node speciesNode = nextNode.getChildNodes().item(1);

			if (speciesNode == null) {
				speciesNode = nextNode.getChildNodes().item(0);
			} else {
				speciesNode = speciesNode.getFirstChild();
			}

			String sciName = speciesNode.getNodeValue();
			triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName.trim()));

			nextNode = nextNode.getNextSibling().getNextSibling();
			Node locationNode = nextNode.getChildNodes().item(1);

			if (locationNode == null) {
				locationNode = nextNode.getChildNodes().item(0);
			} 

			StringBuilder sb = new StringBuilder();
			getNodeValue(sb, locationNode);
			String location = sb.toString().trim();
			location = location.replaceAll("[\\s]{2,}", " ");
			triples.add(new Triple(subject, Predicates.LOCALITY.toString(), location.trim()));

			nextNode = nextNode.getNextSibling().getNextSibling();
			Node imageNode = nextNode.getChildNodes().item(1);

			String imageUrl = null;
			if (imageNode != null) {
				//				imageNode = imageNode.getChildNodes().item(1);
				//				System.out.println(imageNode.getNodeName());
				//				System.out.println("!!!!");
				if (imageNode.getNodeName().equals("a")) {
					imageUrl = imageNode.getAttributes().getNamedItem("href").getNodeValue();
//					System.out.println(imageUrl);
				} else {
					imageNode = imageNode.getChildNodes().item(1);
					if (imageNode != null) {
						imageUrl = imageNode.getAttributes().getNamedItem("href").getNodeValue();
//						System.out.println(imageUrl);
					}
				}
			} 

			//			imageNode = imageNode.getFirstChild();
			//			System.out.println(imageNode.getNodeName());
			if (imageUrl != null) {

//				ParsedDocument imageDoc;
				imageUrl = source + imageUrl;				
//				try {
//					imageDoc = MappingUtils.retrieveImageDocument(parsedD, imageUrl);
//					if(imageDoc!=null){
//						pds.add(imageDoc);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				
				triples.add(new Triple(subject, Predicates.IMAGE_PAGE_URL.toString(), imageUrl.trim()));
			}
			pds.add(parsedD);
		}

		return pds;
	}


	private void getNodeValue(StringBuilder sb, Node node) {
		String value = node.getNodeValue();
		if (value != null && value.length() > 0) {
			sb.append(value);
		}

		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			sb.append(" ");
			getNodeValue(sb, nodes.item(i));
		}
	}
}
