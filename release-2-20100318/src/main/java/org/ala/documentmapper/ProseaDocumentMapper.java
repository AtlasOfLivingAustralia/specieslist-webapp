package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.ala.repository.Namespaces;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document mapper for Plant Resources of SE Asia 
 * 
 * @author Tommy Wang
 */
public class ProseaDocumentMapper extends XMLDocumentMapper {

	public static final String SCIENTIFIC_NAME_PATTERN = "a-zA-ZÏËÖÜÄÉÈČÁÀÆŒâïëêöüäåéèčáàæœóú\\.\\-`'%\\(\\), ;:&#0-9°/\\[\\]";

	public ProseaDocumentMapper() {
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//html/body/table[1]/tbody[1]/tr[1]/td[1]/div[1]/span/text()", 
				subject, Predicates.DC_TITLE);

		addTripleMapping("//html/body/table[1]/tbody[1]/tr[1]/td[1]/div[1]/span/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//img/attribute::src", 
				subject, Predicates.IMAGE_URL);

		addTripleMapping("(?:<b>[\\s]{0,}Synonyms[\\s]{0,}</b>[\\s]{0,}<i>[\\s]{0,})" 
				+ "([a-zA-Z ]{1,})" + "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "([a-zA-Z0-9 \\.\\(\\)]{1,})", MappingType.REGEX,
				subject, Predicates.SYNONYM);

		addTripleMapping("(?:<b>[\\s]{0,}Synonyms[\\s]{0,}</b>[\\s]{0,}<i>[\\s]{0,})" 
				+ "(?:[a-zA-Z ]{1,})" + "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "(?:[a-zA-Z0-9 \\.\\(\\)\\,]{1,})" 
				+ "(?:[\\s]{0,}<i>[\\s]{0,})" 
				+ "([a-zA-Z ]{1,})" 
				+ "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "([a-zA-Z0-9 \\.\\(\\)]{1,})", MappingType.REGEX,
				subject, Predicates.SYNONYM);

		addTripleMapping("(?:<b>[\\s]{0,}Synonyms[\\s]{0,}</b>[\\s]{0,}<i>[\\s]{0,})" 
				+ "(?:[a-zA-Z ]{1,})" + "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "(?:[a-zA-Z0-9 \\.\\(\\)\\,]{1,})" 
				+ "(?:[\\s]{0,}<i>[\\s]{0,})" 
				+ "(?:[a-zA-Z ]{1,})" 
				+ "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "(?:[a-zA-Z0-9 \\.\\(\\)\\,]{1,})"
				+ "(?:[\\s]{0,}<i>[\\s]{0,})" 
				+ "([a-zA-Z ]{1,})" 
				+ "(?:[\\s]{0,}</i>[\\s]{0,})" 
				+ "([a-zA-Z0-9 \\.\\(\\)]{1,})", MappingType.REGEX,	
				subject, Predicates.SYNONYM);

		addTripleMapping("(?:<b>[\\s]{0,}Vernacular names[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{1,})", MappingType.REGEX,
				subject, Predicates.COMMON_NAME);

		addTripleMapping("(?:<b>[\\s]{0,}Origin and geographic distribution[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}<i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}</i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}<i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}</i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})",MappingType.REGEX,
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("(?:<b>[\\s]{0,}Ecology[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}<i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}</i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}<i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,}</i>[\\s]{0,})?"
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})",MappingType.REGEX,
				subject, Predicates.ECOLOGICAL_TEXT);

		addTripleMapping("(?:<b>[\\s]{0,}Description[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "(?:[\\s]{0,})?",MappingType.REGEX,
				subject, Predicates.DESCRIPTIVE_TEXT);

		//addTripleMapping("//b[contains(.,\"Synonyms\")]/following-sibling::text()", 
		//		FedoraConstants.PROSEA_NAMESPACE, "hasSynonym", false);

	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		documentStr = cleanupSrcString(documentStr);


		//System.out.println(documentStr);

		content = documentStr.getBytes();

		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		List<Triple<String,String,String>> toAddTriples = new ArrayList<Triple<String,String,String>>();

		String subject = triples.get(0).getSubject();

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				String[] tmpCommonNameArr = currentObj.split("\\.");
				for (int i = 0; i < tmpCommonNameArr.length; i++) {
					String[] tmpCommonNameArr2 = tmpCommonNameArr[i].split(":");

					if (tmpCommonNameArr2.length > 1) {
						tmpCommonNameArr2 = tmpCommonNameArr2[1].split("\\,");

						for (int j = 0; j < tmpCommonNameArr2.length; j++) {
							toAddTriples.add(new Triple(subject,Predicates.COMMON_NAME.toString(), tmpCommonNameArr2[j].trim()));
						}
					} else {
						tmpCommonNameArr2 = tmpCommonNameArr2[0].split("\\,");

						for (int j = 0; j < tmpCommonNameArr2.length; j++) {
							toAddTriples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), tmpCommonNameArr2[j].trim()));
						}
					}
				}
				tmpTriple.add(triple);
			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//imageUrl = source + imageUrl;
				//triple.setObject(imageUrl);

				String[] imageUrlArr = imageUrl.split("\\|");

				//retrieve the image and create new parsed document
				//				Response response = WebUtils.getUrlContentAsBytes(imageUrl);
				//				ParsedDocument imageDoc = new ParsedDocument();
				//				imageDoc.setContentType(response.getContentType());
				//				imageDoc.setContent(response.getResponseAsBytes());

				for (String url : imageUrlArr) {
					ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, url);
					if(imageDoc!=null){
						pds.add(imageDoc);
					}
				}
				
				tmpTriple.add(triple);
			} 
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));

		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		for (Triple tri : toAddTriples) {
			triples.add(tri);
		}

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
