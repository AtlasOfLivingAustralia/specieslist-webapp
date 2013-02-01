package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.ala.repository.Namespaces;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

// Document mapper for Fishes of Australia Online
public class FaoDocumentMapper extends XMLDocumentMapper {

	public FaoDocumentMapper() {

		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//b[contains(.,\"Scientific Name and Authority\")]/following::i[1]/text()", 
				subject, Predicates.DC_TITLE);

		addDCMapping("//h2/em/text()", 
				subject, Predicates.DC_TITLE);
		
		addDCMapping("//a[contains(.,\"copyright information\")]/@href", 
				subject, Predicates.DC_LICENSE);

		addTripleMapping("//b[contains(.,\"Scientific Name and Authority\")]/following::i[1]/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//h2/em/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//b[contains(.,\"CAAB Taxon Code:\")]/parent::*/text()", 
				subject, Predicates.CAAB_CODE);

		/*
		 * The &nbsp; characters are transformed to spaces in the family string, which cannot be trimmed. 
		 */
		addTripleMapping("//b[contains(.,\"Family:\")]/parent::*/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//b[contains(.,\"Other names\")]/following-sibling::i/text()", 
				subject, Predicates.SYNONYM);

		addTripleMapping("//table[1]/tbody[1]/tr[1]/td[2]//img[1]/attribute::src", 
				subject, Predicates.IMAGE_URL);

		addTripleMapping("//b[contains(.,\"Standard Name:\")]/parent::*/text()", 
				subject, Predicates.COMMON_NAME);

		addTripleMapping("//h3[contains(.,\"Distribution\")]/following-sibling::p[1]", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//h3[contains(.,\"Size\")]/following-sibling::p[1]", 
				subject, Predicates.MORPHOLOGICAL_TEXT);

		addTripleMapping("//h3[contains(.,\"Characteristics\")]/following-sibling::p[1]", 
				subject, Predicates.DESCRIPTIVE_TEXT);

		addTripleMapping("//h3[contains(.,\"Colour\")]/following-sibling::p[1]", 
				subject, Predicates.MORPHOLOGICAL_TEXT);

		addTripleMapping("//h3[contains(.,\"common names\")]/following-sibling::p[1]", 
				subject, Predicates.COMMON_NAME);
	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		documentStr = documentStr.replaceAll("<p/>", "");

		//		System.out.println(documentStr);

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

		List<String> imageUrls = new ArrayList<String>();

		boolean gotCAABCode = false;
		boolean gotFamily = false;
		boolean gotCommonName = false;

		String subject = MappingUtils.getSubject();
		
//		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Fishes of Australia Online");
//		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://plantnet.rbgsyd.nsw.gov.au/copyright.html");
		
		String source = "http://www.marine.csiro.au/";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCAABCode")) {
				String currentObj = (String) triple.getObject();
				if(!gotCAABCode) {
					triple.setObject(currentObj);
					gotCAABCode = true;
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasFamily")) {
				String currentObj = ((String)triple.getObject()).trim();
				if(gotFamily) {
					currentObj = currentObj.replaceAll("\\d", "");
					currentObj = currentObj.replaceAll("\\([a-zA-Z]{0,}\\)", "");
					currentObj = currentObj.replaceAll("\\s", "");
					currentObj = currentObj.replaceAll("\\W", "");
					triple.setObject(currentObj.trim());
				} else {
					tmpTriple.add(triple);
					gotFamily = true;
				}

			} else if(predicate.endsWith("hasCommonName")) {
				String currentObj = ((String)triple.getObject()).trim();
				if(!gotCommonName) {
					if (!currentObj.toLowerCase().contains("none")) {
						triple.setObject(currentObj);
						gotCommonName = true;
					} else {
						tmpTriple.add(triple);
					}
				} else {
					tmpTriple.add(triple);
				}

			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//imageUrl = source + imageUrl;
				//				triple.setObject(imageUrl);
				imageUrls.add(imageUrl);
				//retrieve the image and create new parsed document
				//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				//				if(imageDoc!=null){
				//					pds.add(imageDoc);
				//				}

				tmpTriple.add(triple);
			} 
		}

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		//remove the triple from the triples
		triples.removeAll(tmpTriple);

		//replace the list of triples
		pd.setTriples(triples);

		for (String imageUrl : imageUrls) {
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
				pds.add(imageDoc);
			}
		}
	}
}
