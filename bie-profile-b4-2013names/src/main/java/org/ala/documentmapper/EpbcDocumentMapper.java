package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document mapper for EPBC Act List of Threatened
 * 
 * @author Tommy Wang
 */
public class EpbcDocumentMapper extends XMLDocumentMapper {

	public static final String SCIENTIFIC_NAME_PATTERN = "a-zA-ZÏËÖÜÄÉÈČÁÀÆŒâïëêöüäåéèčáàæœóú\\.\\-`'%\\(\\), ;:&#0-9°/";

	public EpbcDocumentMapper() {

		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);
		
		addDCMapping("//th[contains(.,\"Species author\")]/following-sibling::td[1]/text()", subject, Predicates.DC_CREATOR);

		addTripleMapping("//th[contains(.,\"Scientific name\")]/following-sibling::td[1]/text()", 	
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//th[contains(.,\"Family\")]/following-sibling::td[1]/text()",
				subject, Predicates.FAMILY);

		//addDefaultSubjectMapping("//b[contains(.,\"Common name:\")]/following-sibling::text()",
		//		FedoraConstants.EPBC_NAMESPACE, "hasCommonName", false);

		addTripleMapping("//div[contains(child::*/child::h2,\"Australian Distribution\")]/following-sibling::div[1]",
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//div[contains(child::*/child::h2,\"Description\")]/following-sibling::div[1]",
				subject, Predicates.DESCRIPTIVE_TEXT);

		addTripleMapping("//div[contains(child::*/child::h2,\"Habitat\")]/following-sibling::div[1]",
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//div[contains(child::*/child::h2,\"Threats\")]/following-sibling::div[1]",
				subject, Predicates.THREATS_TEXT);

		addTripleMapping("//div[contains(child::*/child::h2,\"Population Information\")]/following-sibling::div[1]",
				subject, Predicates.POPULATE_ESTIMATE);

		addTripleMapping("//div[contains(child::*/child::h2,\"Feeding\")]/following-sibling::div[1]",
				subject, Predicates.DIET_TEXT);

		addTripleMapping("//th[contains(.,\"EPBC Act Listing Status\")]/following-sibling::td[1]/a",
				subject, Predicates.CONSERVATION_STATUS);

		addTripleMapping("//th[contains(.,\"Distribution map\")]/following-sibling::td[1]/img/attribute::src",
				subject, Predicates.DIST_MAP_IMG_URL);

		addTripleMapping("(?:<b>[\\s]{0,}Common name:[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{1,})",MappingType.REGEX,
				subject, Predicates.COMMON_NAME);
	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

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

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		String source = "http://www.environment.gov.au";
		String fullDistributionText = new String();
		String fullDescriptiveText = new String();
		String fullHabitatText = new String();
		String fullThreatsText = new String();
		String subject = triples.get(0).getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.environment.gov.au/about/copyright.html");

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("title")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split("-")[0];
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} 

			} else if(predicate.endsWith("hasDistributionText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");

				fullDistributionText += " " + newObj;

				tmpTriple.add(triple);

			} else if(predicate.endsWith("hasDescriptiveText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");

				fullDescriptiveText += " " + newObj;

				tmpTriple.add(triple);

			} else if(predicate.endsWith("hasHabitatText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");

				fullHabitatText += " " + newObj;

				tmpTriple.add(triple);

			} else if(predicate.endsWith("hasThreatsText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");

				fullThreatsText += " " + newObj;

				tmpTriple.add(triple);
			} else if(predicate.endsWith("hasFamily")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split(":")[0];

				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} 

			} else if(predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);

				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
					imageDocTriples.add(new Triple(subject,Predicates.DIST_MAP_IMG_URL.toString(), imageDoc.getGuid()));
					imageDoc.setTriples(imageDocTriples);
					pds.add(imageDoc);
				}

				tmpTriple.add(triple);
			} 
		}
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		if (!"".equals(fullDistributionText.trim())) {
			triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), fullDistributionText.trim()));
		}
		if (!"".equals(fullDescriptiveText.trim())) {
			triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), fullDescriptiveText.trim()));
		}
		if (!"".equals(fullHabitatText.trim())) {
			triples.add(new Triple(subject, Predicates.HABITAT_TEXT.toString(), fullHabitatText.trim()));
		}
		if (!"".equals(fullThreatsText.trim())) {
			triples.add(new Triple(subject, Predicates.THREATS_TEXT.toString(), fullThreatsText.trim()));
		}

		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);
	}

}
