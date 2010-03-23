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
import org.ala.documentmapper.IbcDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class IbcDocumentMapperTest extends TestCase {

	final String hasConservationStatus = "Near Threatened";
	final String hasFamily = "Rheidae";
	final String hasFamilyCommonName = "Rheas";
	final String[] hasImagePageUrl = {
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/adulto",
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/adultos",
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/dorsal-view-male-open-wings-close-female", 
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/close-shot-face-one-%C3%B1andu",
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/close-adult-bird",
								"http://ibc.lynxeds.com/photo/greater-rhea-rhea-americana/male-instalation"
								}; 
	final String[] hasVideoPageUrl = {
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/family-group-crop-crossing-road-one-adults-being-alert", 
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/group-males-walking-preening", 
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/juvenile-feeding-crop",
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/large-family-group-crop-walking-feeding",
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/male-foraging-ground",
								"http://ibc.lynxeds.com/video/greater-rhea-rhea-americana/three-birds-walking-then-male-displaying"
								}; 
	
	public void test() throws Exception {
		IbcDocumentMapper dm = new IbcDocumentMapper();
		String uri = "http://ibc.lynxeds.com/species/greater-rhea-rhea-americana";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(object, hasConservationStatus);		    	  
				}

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}
				
				if (predicate.endsWith("hasFamilyCommonName")) {
					Assert.assertEquals(object, hasFamilyCommonName);		    	  
				}
								
				if (predicate.endsWith("hasVideoPageUrl")) {
					Assert.assertTrue(arrayContainsElement(hasVideoPageUrl,object));		    	  
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
