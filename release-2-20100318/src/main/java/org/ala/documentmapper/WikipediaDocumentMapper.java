/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document mapper for Wikipedia 
 *
 * @author Tommy Wang
 */
public class WikipediaDocumentMapper extends XMLDocumentMapper {

	public WikipediaDocumentMapper() {

		String subject = MappingUtils.getSubject();

		final String[] DESCRIPTION_HEADING = {"Behaviour",
				"Behavior",
				"Description",
				"Composition"};
		final String[] CONSERVATION_TEXT_HEADING = {"Protection",
				"Conservation",
				"Extinction"};
		final String[] MORPHOLOGICAL_TEXT_HEADING = {"Physical description",
				"Appearance",
				"Growth",
				"Morphology",
				"Characteristics"};
		final String[] DIET_TEXT_HEADING = {"Diet"};
		final String[] REPRODUCTION_TEXT_HEADING = {"Breeding"};
		final String[] THREATS_TEXT_HEADING = {"Threats"};
		final String[] DISTRIBUTION_TEXT_HEADING = {"Distribution"};
		final String[] ECOLOGICAL_TEXT_HEADING = {"Ecology"};
		final String[] HABITAT_TEXT_HEADING = {"Habitat",
				"Harvesting"};

		final String H2_HEADING = "h2";
		final String H3_HEADING = "h3";

		setRecursiveValueExtraction(true);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//h1[@id=\"firstHeading\"]", 
				subject, Predicates.DC_TITLE);

		//		addTripleMapping("//h1[@id=\"firstHeading\"]", 	
		addTripleMapping("//tr[th[a[contains(.,\"Binomial name\")]]]/following-sibling::tr[1]//b",
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Kingdom:\")]/following-sibling::td[1]", 	
				subject, Predicates.KINGDOM);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Division:\")]/following-sibling::td[1]", 	
				subject, Predicates.DIVISION);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Phylum:\")]/following-sibling::td[1]", 	
				subject, Predicates.PHYLUM);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Class:\")]/following-sibling::td[1]", 	
				subject, Predicates.CLASS);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Order:\")]/following-sibling::td[1]", 	
				subject, Predicates.ORDER);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Suborder:\")]/following-sibling::td[1]", 	
				subject, Predicates.SUBORDER);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Parvorder:\")]/following-sibling::td[1]", 	
				subject, Predicates.PARVORDER);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Infraorder:\")]/following-sibling::td[1]", 	
				subject, Predicates.INFRAORDER);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Family:\")]/following-sibling::td[1]", 	
				subject, Predicates.FAMILY);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Subfamily:\")]/following-sibling::td[1]", 	
				subject, Predicates.SUBFAMILY);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Tribe:\")]/following-sibling::td[1]", 	
				subject, Predicates.TRIBE);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Subtribe:\")]/following-sibling::td[1]", 	
				subject, Predicates.SUBTRIBE);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Genus:\")]/following-sibling::td[1]//i", 	
				subject, Predicates.GENUS);

		addTripleMapping("//table[@class=\"infobox biota\"]/tbody[1]/tr/td[contains(.,\"Species:\")]/following-sibling::td[1]//i", 	
				subject, Predicates.SPECIES);

		//		addTripleMapping("//h2[span[contains(.,\"Behaviour\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Behavior\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Description\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Composition\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, DESCRIPTION_HEADING),
				subject, Predicates.DESCRIPTIVE_TEXT);

		//		addTripleMapping("//h2[span[contains(.,\"Protection\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Conservation\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Extinction\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, CONSERVATION_TEXT_HEADING),
				subject, Predicates.CONSERVATION_TEXT);

		addTripleMapping("//tr[th[a[contains(.,\"Conservation status\")]]]/following-sibling::tr[1]/descendant::a[1]/text()", 	
				subject, Predicates.CONSERVATION_STATUS);

//		addTripleMapping("//h2[span[contains(.,\"Physical description\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Appearance\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Growth\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Morphology\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Characteristics\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, MORPHOLOGICAL_TEXT_HEADING),
				subject, Predicates.MORPHOLOGICAL_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Diet\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, DIET_TEXT_HEADING),
				subject, Predicates.DIET_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Breeding\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, REPRODUCTION_TEXT_HEADING),
				subject, Predicates.REPRODUCTION_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Threats\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, THREATS_TEXT_HEADING),
				subject, Predicates.THREATS_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Distribution\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, DISTRIBUTION_TEXT_HEADING),
				subject, Predicates.DISTRIBUTION_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Ecology\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, ECOLOGICAL_TEXT_HEADING),
				subject, Predicates.ECOLOGICAL_TEXT);

//		addTripleMapping("//h2[span[contains(.,\"Habitat\")]]/following-sibling::p[1]|//h2[span[contains(.,\"Harvesting\")]]/following-sibling::p[1]",
		addTripleMapping(findUsingXpath(H2_HEADING, HABITAT_TEXT_HEADING),
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//a[@class=\"image\"]/img/attribute::src", 	
				subject, Predicates.IMAGE_URL);

		addTripleMapping("//h2[span[contains(.,\"References\")]]/following-sibling::ul[1]|//h2[span[contains(.,\"References\")]]/following-sibling::ol[1]|//h2[span[contains(.,\"Literature\")]]/following-sibling::ul[1]|//h2[span[contains(.,\"Literature\")]]/following-sibling::ol[1]", 	
				subject, Predicates.REFERENCE);
	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
		//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
		//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
		//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");

		documentStr = cleanup(documentStr);

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

		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();

		/* 
		 * The following two variables are used to avoid out of memory errors
		 * e.g. http://en.wikipedia.org/wiki/Acacia
		 */
		int imageCount = 0;
		final int imageMax = 10;
		String title = null;
		boolean sciNameExists = false;
		String subject = MappingUtils.getSubject();
		Map<String, String> dc = pd.getDublinCore();

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();

			if (predicate.endsWith("hasImageUrl")) {
				if (imageCount < imageMax) {
					String imageUrl = (String) triple.getObject();
					//imageUrl = source + imageUrl;

					// convert the thumb image url to the one for a large version
					if (imageUrl.contains(".jpg")) {
						imageUrl = imageUrl.replaceAll("thumb/", "");
						int tmp = imageUrl.indexOf(".jpg");
						imageUrl = imageUrl.substring(0, tmp + ".jpg".length());

						//triple.setObject(imageUrl);

						//retrieve the image and create new parsed document
						ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
						if(imageDoc!=null){
							pds.add(imageDoc);
							imageCount++;
						} 
					}
				}
				toRemove.add(triple);
			} else if (predicate.endsWith("hasScientificName")) {
				sciNameExists = true;
			}
		}

		title = dc.get(Predicates.DC_TITLE.toString());

		if (!sciNameExists) {
			triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), title));
		}

		//remove the triple from the triples
		triples.removeAll(toRemove);

		//replace the list of triples
		pd.setTriples(triples);
	}

	String findUsingXpath(String headingRank, String ... title) {
		String xpathStr = new String();

		for (String t : title) {
			if ("".equals(xpathStr)) {
				xpathStr += "//" + headingRank + "[span[contains(.,\"" + t + "\")]]/following-sibling::p[1]";
			} else {
				xpathStr += "|//" + headingRank + "[span[contains(.,\"" + t + "\")]]/following-sibling::p[1]";
			}
		}

		return xpathStr;
	}

	private String cleanup(String str) {
		String result = str;

		// remove the invalid strings
		result = result.replaceAll("\\]\\]>", "");
		result = result.replaceAll("<!\\[CDATA\\[", "");

		// combine multiple consecutive paragraphs into one 
		result = result.replaceAll("</p>[\\s]{0,}<p>", "");

		// remove the [1] strings
		result = result.replaceAll("<span>\\[</span>1<span>\\]</span>", "");

		return result;
	}
}
