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
import org.ala.documentmapper.MotHtmlDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class MotHtmlDocumentMapperTest extends TestCase {

	final String hasScientificName = "Amygdalum beddomei";
	final String hasFamily = "Mytilidae";
	final String hasGenus = "Amygdalum";
	final String hasHabitatText = "Habitat and distribution Presence in Tasmanian waters: confirmed. Introduced?: no. Extinct?: no. " +
			"Occurrence on Tasmanian beaches?: occasional. Substrate: among byssal threads of mussels. Depth-range: in the shallow " +
			"subtidal. Tasmanian distribution: all round the coast -";
	final String hasDistributionText = "Australian range: NSW, TAS, VIC, SA and WA Global range: southern Australia";
	final String[] hasSynonym = {"beddomei",
								"aboerescens"};
	
	public void test() throws Exception {
		MotHtmlDocumentMapper dm = new MotHtmlDocumentMapper();
		String uri = "http://www.molluscsoftasmania.net/Species pages/Amygdalum beddomei.html";
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

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);		    	  
				}
				
				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(object, hasHabitatText);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasSynonym")) {
					Assert.assertTrue(arrayContainsElement(hasSynonym, object));		    	  
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
