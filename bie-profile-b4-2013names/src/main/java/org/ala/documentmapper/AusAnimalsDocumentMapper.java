/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Australian Animals document mapper
 *
 * @author "Tommy Wang <Tommy.Wang@csiro.au>"
 */
public class AusAnimalsDocumentMapper extends XMLDocumentMapper {
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public AusAnimalsDocumentMapper() {
		//override the default content type
		this.contentType = MimeType.HTML.toString();

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//title/text()",
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//font[font[contains(.,\"Habitat\")]]/following-sibling::text()[1]" +
				"|//u[contains(.,\"HABITAT\")]/following-sibling::text()[1]",
				subject, Predicates.HABITAT_TEXT);

		addTripleMapping("//font[font[contains(.,\"Description\")]]/following-sibling::text()[1]" +
				"|//font[font[contains(.,\"Defence\")]]/following-sibling::text()[1]" +
				"|//font[contains(.,\"DESCRIPTION\")]/following-sibling::text()[1]" +
				"|//font[font[contains(.,\"Behaviour\")]]/following-sibling::text()[1]" +
				"|//u[contains(.,\"DESCRIPTION\")]/following-sibling::text()[1]" +
				"|//u[contains(.,\"BEHAVOIUR\")]/following-sibling::text()[1]",
				subject, Predicates.DESCRIPTIVE_TEXT);

		addTripleMapping("//font[font[contains(.,\"Breeding\")]]/following-sibling::text()[1]" +
				"|//u[contains(.,\"BREEDING\")]/following-sibling::text()[1]",
				subject, Predicates.REPRODUCTION_TEXT);

		addTripleMapping("//font[font[contains(.,\"Location\")]]/following-sibling::text()[1]" +
				"|//font[contains(.,\"LOCATION\")]/following-sibling::text()[1]",
				subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//font[font[contains(.,\"Diet\")]]/following-sibling::text()[1]" +
				"|//font[font[contains(.,\"Feeding\")]]/following-sibling::text()[1]" +
				"|//u[contains(.,\"FEEDING\")]/following-sibling::text()[1]",
				subject, Predicates.DIET_TEXT);

		addTripleMapping("//font[font[contains(.,\"Status\")]]/following-sibling::text()[1]",
				subject, Predicates.CONSERVATION_STATUS);

		addTripleMapping("//img/attribute::src",
				subject, Predicates.IMAGE_URL);       

	}

	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);

		documentStr = documentStr.replaceAll("<br/>", "");
		documentStr = documentStr.replaceAll("<b>", "");
		documentStr = documentStr.replaceAll("</b>", "");
		documentStr = documentStr.replaceAll("[\\s]{2,}", " ");
		documentStr = documentStr.replaceAll("> <", "><");
		//				System.out.println(documentStr);

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

		// Add hard-coded Kingdom Animalia as we know this is a Zoological resource
		String subject = MappingUtils.getSubject();
		String source = "http://members.optusnet.com.au/~alreadman/";

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();

				if (!imageUrl.contains("/") && imageUrl.endsWith(".jpg") && !imageUrl.contains("3xbig")) {

					imageUrl = source + imageUrl;
					imageUrl = imageUrl.replaceAll(" ", "%20");					
					triple.setObject(imageUrl);

					//retrieve the image and create new parsed document
					ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
					if(imageDoc!=null){

						pds.add(imageDoc);
					}

				} else if (imageUrl.contains("tpg")) {
					imageUrl = imageUrl.replaceAll(" ", "%20");					
					triple.setObject(imageUrl);

					//retrieve the image and create new parsed document
					ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
					if(imageDoc!=null){

						pds.add(imageDoc);
					}

				}

				toRemove.add(triple);
			} 
		}

		Triple<String,String,String> newTriple = new Triple<String,String,String>(subject, Predicates.KINGDOM.toString(), "Animalia");
		triples.add(newTriple);
		triples.removeAll(toRemove);
	}
}
