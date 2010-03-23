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
import org.ala.documentmapper.PaDILDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class PaDILDocumentMapperTest extends TestCase {

	final String hasScientificName = "Elsinoë australis";
	final String hasCommonName = "Sweet Orange Fruit Scab";
	final String hasDescriptiveText = "Symptoms E lsinoë australis affects the fruit and leaves of host plants and rarely the stems. Fruit and leaf lesions " +
			"are dark in colour, round, flattened and smooth. On younger fruits it causes a deformation of rind, forming corky, round/irregular, protuberant " +
			"lesions 2–6 mm. Leaf and twig lesions are initially funnel-shaped pockets, later scab-like, smooth, glossy, amphigenous, up to 2 mm diam. (Sivanesan " +
			"& Critchett 1974, Wilson 2007). It can be distinguished from E lsinoë fawcettii which has lighter lesions that are irregular in shape, are raised " +
			"and rough and infect fruit, leaves and stems. The fungus: Ascomata globose, sometimes flattened or irregular, occasionally confluent, 40–120 µm in " +
			"diam., buff, embedded in the tissues of the perfect stage, erumpent, consisting of a hyaline or slightly yellowish pseudoparenchyma devoid of a well " +
			"defined epithecium. Asci often distributed in the upper part of the ascoma, globose to obclavate, inner wall thickened apically, 15–27 × 13–21 µm. " +
			"Ascospores hyaline, variable, straight or more or less curved, 2–4 celled, often markedly constricted not only at median septum, but also at the other " +
			"two, sometimes with a longitudinal septum in the upper middle cell, which is frequently slightly larger than the other cells, 12–20 × 4–8 µm. Conidial " +
			"stage, Sphaceloma australis ( Bitancourt & Jenkins 1936).";
	final String hasCitationText = "McTaggart, A. (2007) Sweet Orange Fruit Scab (Elsinoë australis) Pest and Diseases Image Library. Updated on 12/21/2007 " +
			"9:19:30 AM. Available online: http://www.padil.gov.au";
	final String[] hasDistributionText = {"The anamorph is found primarily in South America: Argentina, Bolivia, Brazil, Ecuador, Paraguay, Uruguay and " +
			"the Pacific (Cook Islands, Samoa, Fiji, Niue). The teleomorph is known only from Brazil.",
								"Central and South America"};
	
	public void test() throws Exception {
		PaDILDocumentMapper dm = new PaDILDocumentMapper();
		String uri = "http://www.padil.gov.au/viewPest.aspx?id=1031";
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

				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertTrue(arrayContainsElement(hasDistributionText, object));			    	  
				}

				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
				}

				if (predicate.endsWith("hasCitationText")) {
					Assert.assertEquals(object, hasCitationText);		    	  
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
