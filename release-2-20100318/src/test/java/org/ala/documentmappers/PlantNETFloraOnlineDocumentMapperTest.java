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
import org.ala.documentmapper.PlantNETFloraOnlineDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class PlantNETFloraOnlineDocumentMapperTest extends TestCase {

	final String hasCommonName = "Sycamore Maple";
	final String hasScientificName = "Acer pseudoplatanus";
	final String hasFamily = "Sapindaceae";
	final String hasSynonym = "Acer pseudoplatanus L.";
	final String hasDescriptionText = "Deciduous tree to 30 m high with scaly bark; monoecious.";
	final String hasFloweringSeason = "spring";
	final String hasDescriptiveText = "Deciduous tree to 30 m high with scaly bark; monoecious.";
	final String hasDistributionText = "Cultivated as an ornamental; sometimes naturalised, recorded near Jenolan. Native of Europe. NSW subdivisions: *NT, *CT Other Australian states: *Vic. *Tas. *S.A.";
	final String hasDistributionMapImageUrl = "/avh/tmp/NSWsubmap04210281385.gif";

	public void test() throws Exception {
		PlantNETFloraOnlineDocumentMapper dm = new PlantNETFloraOnlineDocumentMapper();
		String uri = "http://plantnet.rbgsyd.nsw.gov.au/cgi-bin/NSWfl.pl?page=nswfl&lvl=sp&name=Acer~pseudoplatanus";
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

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}

				if (predicate.endsWith("hasSynonym")) {
					Assert.assertEquals(object, hasSynonym);		    	  
				}
				
				if (predicate.endsWith("hasDescriptionText")) {
					Assert.assertEquals(object, hasDescriptionText);		    	  
				}
				
				if (predicate.endsWith("hasFloweringSeason")) {
					Assert.assertEquals(object, hasFloweringSeason);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
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
