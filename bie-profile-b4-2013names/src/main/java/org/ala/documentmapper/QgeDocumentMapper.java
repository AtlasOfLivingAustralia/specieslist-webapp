package org.ala.documentmapper;

import java.util.*;

import javax.sql.rowset.Predicate;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;

import org.ala.util.Response;
import org.ala.util.WebUtils;

// Document mapper for Queensland Government - Endangered (Department of Environment and Resource Management)  
public class QgeDocumentMapper extends XMLDocumentMapper {

	public QgeDocumentMapper() {
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);
		
		addDCMapping("//h1/text()", 
				subject, Predicates.DC_TITLE);
		
		addTripleMapping("//h1/text()", subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//h1[1]/following-sibling::p[1]/i[1]/text()|//h2[1]/following-sibling::p[1]", 
				subject, Predicates.TMP);

		addTripleMapping("//h3[contains(.,\"Conservation status\")]/following-sibling::p[1]",
				subject, Predicates.CONSERVATION_STATUS);
		
		addTripleMapping("//h3[contains(.,\"Description\")]/following-sibling::p[1]" +
				"|//h3[contains(.,\"What does it look like?\")]/following-sibling::p[1]" +
				"|//h2[contains(.,\"What does it look like?\")]/following-sibling::p[1]",
				subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("//h3[contains(.,\"Diet\")]/following-sibling::p[1]" +
				"|//h3[contains(.,\"eat?\")]/following-sibling::p[1]" +
				"|//h2[contains(.,\"eat?\")]/following-sibling::p[1]",
				subject, Predicates.DIET_TEXT);
		
		addTripleMapping("//h3[contains(.,\"distribution\")]/following-sibling::p[1]" +
				"|//h2[contains(.,\"distribution\")]/following-sibling::p[1]", 	
				subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("//h3[contains(.,\"breed\")]/following-sibling::p[1]" +
				"|//h2[contains(.,\"breed\")]/following-sibling::p[1]", 	
				subject, Predicates.REPRODUCTION_TEXT);
		
		addTripleMapping("//h3[contains(.,\"Where does it live?\")]/following-sibling::p[1]" +
				"|//h2[contains(.,\"Where does it live?\")]/following-sibling::p[1]", 	
				subject, Predicates.HABITAT_TEXT);
		
		addTripleMapping("//h3[contains(.,\"Threatening processes\")]/following-sibling::p[1]", 	
				subject, Predicates.THREATS_TEXT);
		
		addTripleMapping("//p[@class=\"photoright\"]/img/attribute::src" +
				"|//div[@class=\"imageblock\"]/img/@src", 	
				subject, Predicates.IMAGE_URL);
		
		addTripleMapping("(?:<strong>[\\s]{0,}Scientific name:[\\s]{0,}</strong>[\\s]{0,})" 
				+ "([a-zA-Z0-9 ]{1,})", 
				MappingType.REGEX, subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("(?:<strong>[\\s]{0,}Other names:[\\s]{0,}</strong>[\\s]{0,})" 
				+ "([a-zA-Z0-9 \\,]{1,})",
				MappingType.REGEX, subject, Predicates.COMMON_NAME);
		
		addTripleMapping("(?:<b>[\\s]{0,}Description:[\\s]{0,}</b>[\\s]{0,})" 
				+ "([a-zA-Z0-9\\,\\.\\-\\(\\)\\s:\\&;]{1,})",
				MappingType.REGEX, subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("(?:<b>[\\s]{0,}Distribution:[\\s]{0,}</b>[\\s]{0,})" 
				+ "([a-zA-Z0-9\\,\\.\\-\\(\\)\\s:\\&;']{1,})",
				MappingType.REGEX, subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("(?:<b>[\\s]{0,}Threatening Processes:[\\s]{0,}</b>[\\s]{0,})" 
				+ "([a-zA-Z0-9\\,\\.\\-\\(\\)\\s:\\&;']{1,})" + 
				"(?:[\\s]{0,}<a[ \\w=\":/\\.]{1,}>[\\s]{0,})" +
				"([\\w:/\\.]{1,})",
				MappingType.REGEX, subject, Predicates.THREATS_TEXT);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
		documentStr = preProcessDocument(documentStr);
		
		
//		System.out.println(documentStr);
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}

	
	/**
	 * pre-process the document to clean up the format
	 * 
	 * @param str
	 */
	protected String preProcessDocument(String str) {
		
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == 0xA0){   
				str = str.substring(0, i) + str.substring(i+1);  
//				System.out.println("Got it");
			} 
		}
		
		// Clean the italic tags
		str = str.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s]{0,}</p>", "</p>0");
		str = str.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
		str = str.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
		// clean the break line tags
		str = str.replaceAll("[\\s&&[^ ]]{0,}<br/>[\\s&&[^ ]]{0,}", "");
		
		// Convert all the lists to paragraphs
		str = str.replaceAll("[\\s&&[^ ]]{0,}<ul>[\\s&&[^ ]]{0,}", "<p>");
		str = str.replaceAll("[\\s&&[^ ]]{0,}<ul id=\"[a-zA-Z0-9]{1,}\">[\\s&&[^ ]]{0,}", "<p>");
		str = str.replaceAll("[\\s&&[^ ]]{0,}</ul>[\\s&&[^ ]]{0,}", "</p>");
		str = str.replaceAll("[\\s&&[^ ]]{0,}<li>[\\s&&[^ ]]{0,}", "");
		str = str.replaceAll("[\\s&&[^ ]]{0,}<li style=\"[a-zA-Z0-9 \\-:]{1,}\">[\\s&&[^ ]]{0,}", "");
		str = str.replaceAll("[\\s&&[^ ]]{0,}</li>[\\s&&[^ ]]{0,}", "");
		
		// Combine two or more consecutive paragraphs into one
		str = str.replaceAll("</p>[\\s]{0,}<p>[\\s]{0,}<img", "</p>0<p><img");
		str = str.replaceAll("</p>[\\s]{0,}<p>", "");
		
//		System.out.println(str);
		
		return str;
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		String source = "http://www.derm.qld.gov.au";		
		String subject = triples.get(0).getSubject();
		String title = pd.getDublinCore().get(Predicates.DC_TITLE.toString());
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.derm.qld.gov.au/legal/copyright.html");
		String tmpSciName = null;
		boolean isSciNameValid = true;
		boolean gotSciName = false;
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				String[] commonNames = currentObj.split(",");
				for (String newObj : commonNames) {
					if(! "".equals(newObj)) {
						triple.setObject(newObj.trim());
					} 
				}
			} else if(predicate.endsWith("hasScientificName")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split("\\(")[0];
				
				if (!newObj.contains(",")) {
					gotSciName = true;
					triple.setObject(newObj.trim());
				} else {
					isSciNameValid = false;
					tmpTriple.add(triple);
				}
			} else if(predicate.endsWith("hasConservationStatus")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("\\(\\s", "\\(");
				newObj = newObj.replaceAll("\\s\\)", "\\)");
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasDietText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("\\(\\s", "\\(");
				newObj = newObj.replaceAll("\\s\\)", "\\)");
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasDescriptiveText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("\\&amp;", "\\&");
				newObj = newObj.replaceAll("[\\s&&[^ ]]", "");
				newObj = newObj.replaceAll("[ ]{2,}", " ");
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasDistributionText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("\\&amp;", "\\&");
				newObj = newObj.replaceAll("[\\s&&[^ ]]", "");
				newObj = newObj.replaceAll("[ ]{2,}", " ");
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasThreatsText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("[\\s&&[^ ]]", "");
				newObj = newObj.replaceAll("[ ]{2,}", " ");
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				String tmpstr = getXPathSingleValue(xmlDocument, "//img[@src='" + imageUrl + "']/following-sibling::p[1]/text()");
				String description = null;
				String creator = null;
				
				if (tmpstr != null && tmpstr.contains("Photo:")) {
					creator = tmpstr.split("Photo:")[1].trim();
					description = tmpstr.split("Photo:")[0].trim();
				}
				
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
				if(imageDoc!=null){
					imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
					if (description != null) {
						imageDoc.getTriples().add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
					}
					pds.add(imageDoc);
				}

				tmpTriple.add(triple);
			} else if(predicate.endsWith("tmp")) {
				tmpSciName = (String) triple.getObject();
				tmpTriple.add(triple);
			}
		}
		if (!isSciNameValid) {
			triples.add(new Triple(subject,Predicates.SCIENTIFIC_NAME.toString(), title));
		}
		
		if (!gotSciName && tmpSciName != null && !"".equals(tmpSciName)) {
			triples.add(new Triple(subject,Predicates.SCIENTIFIC_NAME.toString(), tmpSciName));
		}
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
	
		//replace the list of triples
		pd.setTriples(triples);
	}
	

}
