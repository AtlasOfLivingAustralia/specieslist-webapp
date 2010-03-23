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
import org.ala.documentmapper.RduDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class RduDocumentMapperTest extends TestCase {
	
	final String hasScientificName = "Acrochordus arafurae";
	final String hasImageUrl = "http://www.reptilesdownunder.com/arod/pictures/squamata/acrochordidae/acrochordus/A_arafurae_thumb.jpg";
	final String hasCommonName = "Arafura file snake";
	final String hasScientificNameAuthorship = "McDowell, 1979";
	final String hasDescriptiveText = "Grey/brown above with a broad, dark mesh-like pattern. Pale below, with the mesh-like pattern " +
			"extending to the belly. Very loose skin with numerous, fine scales. Scales are pointed. No noticeable mid-ventral fold. " +
			"11-14 scales between nasal scale and eye. 9-11 scales between lip and eye. Deeply forked tongue, so much so that when the " +
			"tongue is protruded, only the two forks are seen.";
	final String hasSimilarSpecies = "Little file snake (Acrochordus granulatus)";
	final String[] hasDistributionText = {	"Coastal regions and waterways of northern Australia.",
											"Northern Territory, Queensland, Western Australia",
											"Also found in New Guinea."};
	final String hasHabitatText = "Mainly freshwater streams and billabongs, but they will enter estuarine areas and the sea.";
	final String hasDistributionMapUrl = "http://www.reptilesdownunder.com/arod/pictures/stateMapWithCoords.php?speciesID=613";
	final String hasConservationStatus = "This species does not appear to be listed as of conservation concern.";
		
	public void test() throws Exception {
		RduDocumentMapper dm = new RduDocumentMapper();
		String uri = "http://www.reptilesdownunder.com/arod/reptilia/Squamata/Acrochordidae/Acrochordus/arafurae";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(object, hasScientificName);		    	  
				}

				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);			    	  
				}

				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(object, hasConservationStatus);		    	  
				}

				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(object, hasHabitatText);		    	  
				}
				
				if (predicate.endsWith("hasImageUrl")) {
					Assert.assertEquals(object, hasImageUrl);		    	  
				}
								
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertTrue(arrayContainsElement(hasDistributionText, object));		    	  
				}
				
				if (predicate.endsWith("hasSimilarSpecies")) {
					Assert.assertEquals(object, hasSimilarSpecies);		    	  
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
