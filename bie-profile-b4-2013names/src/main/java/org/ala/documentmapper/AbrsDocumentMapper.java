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
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

/**
 * Document mapper for SpeciesBank - ABRS
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class AbrsDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public AbrsDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//a[@id=\"top\"]/text()", subject, Predicates.SCIENTIFIC_NAME);
		// It's hard to get the distribution text in a single string as there may be random italic words inside it.
		addTripleMapping("//p[b[contains(.,\"Distribution\")]]/following::p[1]", 
				subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("//b[contains(.,\"Features\")]/following::p[1]", 
				subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("//b[contains(.,\"Ecology/Way of Life\")]/following::p[1]", 
				subject, Predicates.ECOLOGICAL_TEXT);
		
		addTripleMapping("//b[contains(.,\"Interaction with Humans/Threats\")]/following::p[1]", 
				subject, Predicates.THREATS_TEXT);
		
		addTripleMapping("//a[@id=\"top\"]/a/text()", subject, Predicates.FAMILY);
		
//		addTripleMapping("//b[contains(.,\"Distribution Map\")]/following::p[1]/a/attribute::href", 
		addTripleMapping("//img[@alt=\"Distribution\"]/attribute::src",
				subject, Predicates.DIST_MAP_IMG_URL);
		
//		addTripleMapping("//p[b[contains(.,\"Attached Images\")]]/following::p[1]/a/attribute::href",
		addTripleMapping("//img[@alt=\"Preferred Image\"]/attribute::src",
				subject, Predicates.IMAGE_URL);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
		
//		System.out.println(documentStr);
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}

	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		String source = "http://www.environment.gov.au";
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String titleStr = null; 
		String subject = triples.get(0).getSubject();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl") || predicate.endsWith("hasDistributionMapImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				/*
				 * The url cannot be recognized by our WebUtils due to special characters
				 */
				//retrieve the image and create new parsed document
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					
					pds.add(imageDoc);
				}

				//remove the triple from the triples
//				triples.remove(triple);
			} else if (predicate.endsWith("hasScientificName")) {
				String currentObj = (String) triple.getObject();
				currentObj = currentObj.split("\\(")[0].trim();		
				//System.out.println(currentObj);
				currentObj = cleanBrackets(currentObj);
				
				if (currentObj != null) {
					titleStr = currentObj;
				}
				
				tmpTriple.add(triple);
			} 
		}

		pd.getDublinCore().put(Predicates.DC_TITLE.toString(), titleStr);
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
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
