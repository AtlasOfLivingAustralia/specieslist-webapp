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
import org.ala.documentmapper.AbrsFloraOfOzOnlineDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AbrsFloraOfOzOnlineDocumentMapperTest extends TestCase {

	final String publishedInCitation = "Bot. Jahrb. Syst.";
	final String hasScientificName = "Noahdendron nicholasii";
	final String hasGenus = "Noahdendron";
	final String hasSpecificEpithet = "nicholasii";
	final String[] hasDescriptiveText = {"Tree to 10 m tall; indumentum stellate. Leaves oblong to elliptic, acuminate; lamina to 30 " +
			"cm long and 10 cm wide, attenuate at base; petiole 0.8–1.5 cm long; stipules asymmetrically ovate, attenuate at base, " +
			"to 2 cm long, 1 cm wide. Spikes on peduncle to 5 cm long, pendent; rachis to 7 cm long; inflorescence bracts to 1 cm " +
			"long; flower bracts c. 4 mm long. Sepals ovate-triangular, c. 3 mm long, densely stellate-hairy. Petals glabrous, c. " +
			"5 mm long (unrolled), red or purple. Stamens c. equal to unrolled petals, red; anther appendage long-apiculate, c. 1 " +
			"mm long. Styles c. 1 mm long; stigma capitate. Capsule 1 cm long and wide. Seeds c. 7 mm long. Fig. 25E–F .",
			"Restricted to the Cape Tribulation area, Qld. Found near streams in rainforest at altitudes to 100 m. Map 3 .",
			"This species is unique in the family with its inrolled and bent petals. The large stipules are not characteristic of " +
			"the subfamily Hamamelidoideae."};
	
	public void test() throws Exception {
		AbrsFloraOfOzOnlineDocumentMapper dm = new AbrsFloraOfOzOnlineDocumentMapper();
		String uri = "http://www.anbg.gov.au/abrs/online-resources/flora/stddisplay.xsql?pnid=9";
//		String uri = "http://larval-fishes.australianmuseum.net.au/descriptions/macquaria-colonorum.htm";
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

				if (predicate.endsWith("publishedInCitation")) {
					Assert.assertEquals(object, publishedInCitation);		    	  
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);		    	  
				}
				
				if (predicate.endsWith("hasSpecificEpithet")) {
					Assert.assertEquals(object, hasSpecificEpithet);		    	  
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertTrue(arrayContainsElement(hasDescriptiveText, object));		    	  
				}
			}
		}
	}
	
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
