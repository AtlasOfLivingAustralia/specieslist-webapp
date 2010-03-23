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
import org.ala.documentmapper.FaoDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class FaoDocumentMapperTest extends TestCase {

	final String hasScientificName = "Alepocephalus australis";
	final String hasCAABCode = "37 114014";
	final String hasFamily = "Alepocephalidae";
	final String hasCommonName = "Smallscale Slickhead";
	final String[] hasSynonym = {
								"Alepocephalus sp. 2",
								"Alepocephalus sp. B"
								};
	final String hasImageUrl = "http://www.marine.csiro.au/piaf/37114/37114014a-t.jpg";
	
	public void test() throws Exception {
		FaoDocumentMapper dm = new FaoDocumentMapper();
		String uri = "http://www.marine.csiro.au/caabsearch/caab_search.caab_report?spcode=37114014";
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
				
				if (predicate.endsWith("hasCAABCode")) {
					Assert.assertEquals(object, hasCAABCode);		    	  
				}
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);		    	  
				}
								
				if (predicate.endsWith("hasSynonym")) {
					Assert.assertTrue(arrayContainsElement(hasSynonym,object));		    	  
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
