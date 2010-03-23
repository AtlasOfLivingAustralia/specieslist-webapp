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

import javax.xml.parsers.DocumentBuilderFactory;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.WebUtils;

/**
 * Document mapper for Northern Territory Threatened Species List
 *
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class SabDocumentMapper implements DocumentMapper{

	/**
	 * This method is expecting PDF byte content
	 * 
	 * @see org.ala.documentmapper.DocumentMapper#map(java.lang.String, byte[])
	 */
	public List<ParsedDocument> map(String uri, byte[] content) throws Exception {

		//create PDF doc
		ParsedDocument pd = new ParsedDocument();
		pd.setGuid(uri);
		pd.setContent(content);
		pd.setContentType(MimeType.PDF.toString());

		//add the dublin core identifier
		pd.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), uri);

		String documentStr = WebUtils.getTextFromPDFPage(uri, content);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);

		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
		pds.add(pd);

		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		String subject = MappingUtils.getSubject();
		//System.out.println(documentStr);

		String[] doc = documentStr.split("[\\s&&[^ ]]{1,}");

		for (int i = 0; i < doc.length; i++) {
//			System.out.println("STR!!!" + i + "!!!" + doc[i]);
			if (i == 0) {
				String sciName = null;

				if (doc[i].contains("Threatened")){
					if (!doc[i+1].contains("What does it look like")) {
						if (!doc[i+1].contains("Sheet")) {
							sciName = doc[i+1].trim();
						} else {
							sciName = doc[i+2].trim();
						}
					}
				} else if (doc[i].contains("Sheet")){
					sciName = doc[i+3].trim();
				} else if (!doc[i].contains("Recovery Plan")){
					sciName = doc[i].trim();
				}
				pd.getDublinCore().put(Predicates.DC_TITLE.toString(), sciName);
				triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
			} 
		}

		//remove the triple from the triples
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);

		return pds;
	}
}
