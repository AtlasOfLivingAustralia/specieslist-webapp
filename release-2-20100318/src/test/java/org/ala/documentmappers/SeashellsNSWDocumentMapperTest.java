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
import org.ala.documentmapper.SeashellsNSWDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class SeashellsNSWDocumentMapperTest extends TestCase {
	
	final String hasScientificName = "Smaragdia tragena (Iredale, 1936)";
	final String hasSynonym = "Smaragdia abakionigraphis Drivas & Jay, 1989, described from Reunion, is this species.";
	final String hasHabitatText = "In NSW, known only from estuaries, beach collected and down to 27 m. Rare.";
	final String hasMorphologicalText = "Up to 6 mm in height.";
	final String hasDistributionText = "Known from Australia, Papua New Guinea, Indonesia and Reunion. In Australia, a " +
			"discontinuous distribution from Torres Strait southwards to Capricorn Group, Queensland, and then Port " +
			"Stephens southwards to Sydney, NSW.";
	final String hasDescriptiveText = "Shell with low spire, width less than height. Surface smooth and " +
				"glossy. Columellar callus reflected onto body whorl as smooth, thick deck. Columella concave, with " +
				"about 8 denticles centrally, decreasing in size from top to bottom. Outer lip sharp, smooth, simple. " +
				"Colour white background, with reddish-brown flames descending from suture, and 6 or 7 spiral bands " +
				"of brown or reddish-brown square or rectangular patches, sometimes outlined with darker brown. " +
				"Operculum unknown.";
		
	public void test() throws Exception {
		SeashellsNSWDocumentMapper dm = new SeashellsNSWDocumentMapper();
		String uri = "http://seashellsofnsw.org.au/Neritidae/Pages/Smaragdia_tragena.htm";
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

				if (predicate.endsWith("hasSynonym")) {
					Assert.assertEquals(object, hasSynonym);			    	  
				}

				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(object, hasHabitatText);		    	  
				}
				
				if (predicate.endsWith("hasMorphologicalText")) {
					Assert.assertEquals(object, hasMorphologicalText);		    	  
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
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
