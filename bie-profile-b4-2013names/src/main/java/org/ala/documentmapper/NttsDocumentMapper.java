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

import javax.xml.parsers.DocumentBuilderFactory;

import org.ala.model.Licence;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.WebUtils;

/**
 * Document mapper for Northern Territory Threatened Species List
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class NttsDocumentMapper implements DocumentMapper{
	
	/** A map of licences */
	protected Map<String, Licence> licencesMap;
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
		//		System.out.println(documentStr);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);

		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
		pds.add(pd);

		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		String subject = MappingUtils.getSubject();

		documentStr = documentStr.replaceAll("[ ]{2,}", "");

		String[] doc = documentStr.split("[\\s&&[^ ]]{1,}");

		for (int i = 0; i < doc.length; i++) {

//						System.out.println("!!!!!" + doc[i]);
			if (doc[i].contains("THREATENED SPECIES OF THE NORTHERN TERRITORY")) {
				String title = null;
				for (int j = 1; ; j++) {

					if ("".equals(doc[i+j+1].trim())) {
						title = doc[i+j].trim();
						pd.getDublinCore().put(Predicates.DC_TITLE.toString(), title);
						break;
					}
				}

				triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), title));
			} else if (doc[i].trim().equals("Conservation status")) {
				String conservationStatus = new String();

				//				conservationStatus = conservationStatus.replaceFirst("[\\s]{0,}Conservation status[\\s]{0,}", "");
				//				conservationStatus = conservationStatus.replaceAll("[\\s&&[^ ]]{1,}", "");

				for (int j = 1; ; j++) {
					if ("".equals(doc[i+j].trim())) {
						break;
					} 
					
//					System.out.println(doc[i+j]);
					conservationStatus += " " + doc[i+j].trim();
				}
				conservationStatus = conservationStatus.replaceAll("\\([a-zA-Z0-9\\.\\s]{0,}\\)", "");
				String[] conservStatusArray = conservationStatus.split("\\.");

				for (String str : conservStatusArray) {
					triples.add(new Triple(subject,  Predicates.CONSERVATION_STATUS.toString(), str.trim()));
				}
			} else if (doc[i].contains("Description")) {
				String descriptionText = new String();

				//				descriptionText = descriptionText.replaceFirst("[\\s]{0,}Description[\\s]{0,}", "");
				//				descriptionText = descriptionText.replaceAll("[\\s&&[^ ]]{1,}", "");

				for (int j = 1; ; j++) {
					if ("".equals(doc[i+j].trim())) {
						break;
					}
					descriptionText += " " + doc[i+j].trim();
				}

				triples.add(new Triple(subject,  Predicates.DESCRIPTIVE_TEXT.toString(), descriptionText.trim()));
			} else if (doc[i].contains("Distribution")) {
				String distributionText = new String();

				//				distributionText = distributionText.replaceFirst("[\\s]{0,}Distribution[\\s]{0,}", "");
				//				distributionText = distributionText.replaceAll("[\\s&&[^ ]]{1,}", "");

				for (int j = 1; ; j++) {
					if ("".equals(doc[i+j].trim())) {
						break;
					}
					distributionText += " " + doc[i+j].trim();
				}

				triples.add(new Triple(subject, Predicates.DISTRIBUTION_TEXT.toString(), distributionText.trim()));
			} else if (doc[i].contains("Threatening processes")) {
				String threatsText = new String();
				//
				//				threatsText = threatsText.replaceFirst("[\\s]{0,}Threatening processes[\\s]{0,}", "");
				//				threatsText = threatsText.replaceAll("[\\s&&[^ ]]{1,}", "");

				for (int j = 1; ; j++) {
					if ("".equals(doc[i+j].trim())) {
						break;
					}
					threatsText += " " + doc[i+j].trim();
				}

				triples.add(new Triple(subject, Predicates.THREATS_TEXT.toString(), threatsText.trim()));
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
	
	/**
	 * @param licencesMap the licencesMap to set
	 */
	public void setLicencesMap(Map<String, Licence> licencesMap) {
		this.licencesMap = licencesMap;
	}
}
