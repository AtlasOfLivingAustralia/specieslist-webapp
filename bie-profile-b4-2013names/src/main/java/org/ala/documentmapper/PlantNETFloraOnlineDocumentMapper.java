package org.ala.documentmapper;

/**
*
* @author Tommy Wang (tommy.wang@csiro.au)
*/

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

public class PlantNETFloraOnlineDocumentMapper extends XMLDocumentMapper {

	public PlantNETFloraOnlineDocumentMapper() {

		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//table[@class='normal']/tbody[1]/tr[1]/td[1]/i[1]/text()", 
				subject, Predicates.DC_TITLE);
		
		addDCMapping("//font[contains(.,\"Text by\")]/text()[1]", 
				subject, Predicates.DC_CREATOR);

		//		addTripleMapping("//html/body/table[2]/tr[1]/td[2]/table[1]/tr[1]/td[1]/text()", 
		//				Namespaces.DC_ELEMENTS, "titleTmp", false);

		addTripleMapping("//table[@class='normal']/tbody[1]/tr[1]/td[1]/i[1]/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//td[contains(.,\"Family\")]/b[1]/a/text()", 
				subject, Predicates.FAMILY);

		addTripleMapping("//div[contains(child::b,\"Common name:\")]/text()", 
				subject, Predicates.COMMON_NAME);

		addTripleMapping("//p[contains(child::b,\"Synonyms:\")]/i/text()", 
				subject, Predicates.SYNONYM);

		//		addTripleMapping("//html/body/table[2]/tr[1]/td[2]/table[1]/tr[1]/td[1]/p[1]/text()", 
		//				Namespaces.ALA, "hasSynonymTmp", false);

		/*
		 * A decent way to retrieve all the descriptive text is still to be found as these texts are included
		 * in multiple <p> paragraphs without any attributes or keywords. 
		 */

		addTripleMapping("//p[contains(child::b,\"Description:\")]/text()", 
				subject, Predicates.DESCRIPTIVE_TEXT);

		addTripleMapping("//p[contains(child::b,\"Flowering:\")]/text()", 
				subject, Predicates.FLOWERING_SEASON);

		addTripleMapping("//b[contains(.,\"Distribution and occurrence\")]/following-sibling::text()", 
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//table[@align=\"left\"]/tbody[1]/tr[1]/td[1]/img/attribute::src", 
				subject, Predicates.DIST_MAP_IMG_URL);

	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		List<String> titleParts = getXPathValues(xmlDocument, "//html/body/table[2]/tr[1]/td[2]/table[1]/tr[1]/td[1]/text()");
		List<String> synonymParts = getXPathValues(xmlDocument, "//p[contains(child::b,\"Synonyms:\")]/text()");


		//get the currently mapped triples
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();


		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		String subject = null;
		
//		String creator = pd.getDublinCore().get(Predicates.DC_CREATOR.toString());
//        
//        if (creator != null && !"".equals(creator)) {
//        	creator = creator.replaceAll("Text by", "").trim();        	
//        	pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
//        }
		
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Botanic Gardens Trust([current date in day month year]). " +
				"PlantNET - The Plant Information Network System of Botanic Gardens Trust, Sydney, Australia (version [number]). " +
				"http://plantnet.rbgsyd.nsw.gov.au");
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "DPA - CC-BY-SA");
		
		if(triples.size()>0){
			subject = triples.get(0).getSubject();
		} else {
			subject = MappingUtils.getSubject();
		}
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
		
		boolean documentEmpty = true;

		String fullDistributionText = "";

		String source = "http://plantnet.rbgsyd.nsw.gov.au";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("title")) {
				String currentObj = (String) triple.getObject();

				for (String titlePart: titleParts)
					currentObj+=" "+titlePart;

				triple.setObject(currentObj.trim());

				documentEmpty = false;

			} else if(predicate.endsWith("hasSynonym")) {
				String currentObj = (String) triple.getObject();

				for (String synonymPart: synonymParts)
					currentObj+=" "+synonymPart;

				triple.setObject(currentObj.trim());
				
				documentEmpty = false;

			} else if(predicate.endsWith("hasDistributionText")) {
				String currentObj = (String) triple.getObject();
				if ("".equals(fullDistributionText)) {
					fullDistributionText += currentObj;
				} else {
					fullDistributionText += " " + currentObj;
				}
				tmpTriple.add(triple);			
				
			} else if(predicate.endsWith("hasFloweringSeason")) {
				String currentObj = (String) triple.getObject();
				String newObj = null;

				newObj = currentObj.replaceAll("\\.", "");
				triple.setObject(newObj);


			} else if(predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);

				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
				if(imageDoc!=null){
					List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
					imageDocTriples.add(new Triple(subject,Predicates.DIST_MAP_IMG_URL.toString(), imageDoc.getGuid()));
					imageDoc.setTriples(imageDocTriples);
					pds.add(imageDoc);
				}

				tmpTriple.add(triple);
			} 
		}

		if (!"".equals(fullDistributionText) && fullDistributionText != null) {
			triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), fullDistributionText));
		}
		

		//		//remove the triple from the triples
		triples.removeAll(tmpTriple);

		//replace the list of triples
		pd.setTriples(triples);

		if (documentEmpty) {
			pds.remove(pd);
		}
	}

}
