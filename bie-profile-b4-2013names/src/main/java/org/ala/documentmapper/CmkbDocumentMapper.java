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

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Document mapper for Customary Medicinal Knowledgebase
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class CmkbDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public CmkbDocumentMapper() {

		setRecursiveValueExtraction(true);

		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();

		//set an initial subject
		String subject = MappingUtils.getSubject();
		addDCMapping("//td[contains(.,\"Authority\")]/following-sibling::td[1]", subject, Predicates.DC_CREATOR);
		
		addTripleMapping("//td[contains(.,\"Kingdom\")]/following-sibling::td[1]", subject, Predicates.KINGDOM);
		addTripleMapping("//td[contains(.,\"Genus\")]/following-sibling::td[1]", subject, Predicates.GENUS);
		addTripleMapping("//td[contains(.,\"Species\")]/following-sibling::td[1]", subject, Predicates.SPECIES);
		addTripleMapping("//td[contains(.,\"Synonym\")]/following-sibling::td[1]", subject, Predicates.SYNONYM);
		addTripleMapping("//td[contains(.,\"Family\")]/following-sibling::td[1]", subject, Predicates.FAMILY);
		addTripleMapping("//td[contains(.,\"Common Name\")]/following-sibling::td[1]", subject, Predicates.COMMON_NAME);
		addTripleMapping("//td[contains(.,\"Native Name\")]/following-sibling::td[1]", subject, Predicates.NATIVE_NAME);
		addTripleMapping("//td[contains(.,\"Authority\")]/following-sibling::td[1]", subject, Predicates.AUTHOR);
		addTripleMapping("//td[contains(.,\"Habit\")][not(contains(.,\"Habitat\"))]/following-sibling::td[1]", subject, Predicates.MORPHOLOGICAL_TEXT);
		addTripleMapping("//td[contains(.,\"Habitat\")]/following-sibling::td[1]", subject, Predicates.HABITAT_TEXT);
		addTripleMapping("//td[contains(.,\"Images\")]/following-sibling::td[1]/a/img/attribute::src", subject, Predicates.IMAGE_URL);
	}

	//	@Override
	//	public List<ParsedDocument> map(String uri, byte[] content)
	//		throws Exception {
	//		
	//		String documentStr = new String(content);
	//		
	//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
	//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
	//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
	//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
	//		
	//		
	//		//System.out.println(documentStr);
	//		
	//		content = documentStr.getBytes();
	//		
	//		return super.map(uri, content);
	//	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		// extract details of images
		String source = "http://biolinfo.org/cmkb/";
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		String titleStr = null; 
		String subject = MappingUtils.getSubject();
		String genus = null;
		String species = null;
		String scientificName = null;
		String title = null;
		String[] synonyms = null;
		String[] commonNames = null;
		String[] nativeNames = null;
		
		List<String> imageUrlList = new ArrayList<String>();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://biolinfo.org/cmkb/disclaimer.php");

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl") || predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = imageUrl.replaceAll("thumbs/", "multimedia/");
				imageUrl = imageUrl.replaceAll("tn_", "");
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				imageUrlList.add(imageUrl);
				
				//remove the triple from the triples
				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasSynonym")) {
				String currentObj = (String) triple.getObject();
				synonyms = currentObj.split(",");		

				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				commonNames = currentObj.split("[\\d]{1,}");		

				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasNativeName")) {
				String currentObj = (String) triple.getObject();
				nativeNames = currentObj.split("[\\d]{1,}");		
				
				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasGenus")) {
				genus = (String) triple.getObject();
			} else if (predicate.endsWith("hasSpecies")) {
				species = (String) triple.getObject();
			}  

		}

		//		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		//		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));

		scientificName = genus + " " + species;
		title = genus + " " + species;

		if (synonyms != null) {
			for (String synonym : synonyms) {		
				triples.add(new Triple(subject, Predicates.SYNONYM.toString(), synonym.trim()));
			}
		}

		if (commonNames != null) {
			for (String commonName : commonNames) {
				if (!"".equals(commonName.trim())) {
					triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName.trim()));
				}
			}
		}

		if (nativeNames != null) {
			for (String nativeName : nativeNames) {
				nativeName = nativeName.replaceAll("\\(","").trim();
				nativeName = nativeName.replaceAll("\\)","").trim();
				if (!"".equals(nativeName.trim())) {
					triples.add(new Triple(subject, Predicates.NATIVE_NAME.toString(), nativeName.trim()));
				}
			}
		}

		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
		pd.getDublinCore().put(Predicates.DC_TITLE.toString(), title);
		
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);
		
		for (String imageUrl : imageUrlList) {
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
				pds.add(imageDoc);
			}
		}
	}

	private String cleanBrackets(String str) {
		str = str.replaceAll("\\(", "");
		str = str.replaceAll("\\)", "");

		if ("".equals(str.trim())) {
			return null;
		} else {
			return str.trim();
		}

	}
}
