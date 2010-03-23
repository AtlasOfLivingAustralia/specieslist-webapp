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
package org.ala.documentmappers;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.ala.documentmapper.FloraBaseHtmlDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 * A Junit test for FloraBase - Western Australian Flora
 *
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class FloraBaseHtmlDocumentMapperTest extends TestCase {

	final String hasScientificName = "Isoetes coromandelina L.f.";
	final String hasReference = "Suppl. 447 (1782)";
	final String hasScientificDescriptionAuthor = "Amanda Spooner, Monday 9 August 1999";
	final String hasConservationStatus = "Not threatened";
	final String hasNameStatus = "Current";
	final String[] hasOccurrencesInRegion = {
			"Northern Botanical Province",
			"Northern Kimberley",
			"Victoria Bonaparte"
	};
	
	public void test() throws Exception {
		FloraBaseHtmlDocumentMapper dm = new FloraBaseHtmlDocumentMapper();
		String uri = "http://florabase.dec.wa.gov.au/browse/profile/10";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.equals(Predicates.SCIENTIFIC_NAME.toString())) {
					Assert.assertEquals(hasScientificName,object);
				}

				if (predicate.equals(Predicates.REFERENCE.toString())) {
					Assert.assertEquals(hasReference,object);
				}
				
				if (predicate.equals(Predicates.SCIENTIFIC_DESCRIPTION_AUTHOR.toString())) {
					Assert.assertEquals(hasScientificDescriptionAuthor,object);
				}
				
				if (predicate.equals(Predicates.CONSERVATION_STATUS.toString())) {
					Assert.assertEquals(hasConservationStatus,object);
				}
				
				if (predicate.equals(Predicates.NAME_STATUS.toString())) {
					Assert.assertEquals(hasNameStatus,object);
				}
				
				if (predicate.equals(Predicates.OCCURRENCES_IN_REGION.toString())) {
					Assert.assertTrue(arrayContainsElement(hasOccurrencesInRegion,object));
				}
			}
		}
	}
	
	// Check Whether a String array cotains a String element 
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
