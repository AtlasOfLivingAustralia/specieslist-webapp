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
import org.ala.documentmapper.WeedDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class WeedDocumentMapperTest extends TestCase {

	final String hasScientificName = "Lycium ferocissimum";
	final String hasDescriptiveText = "African Boxthorn ( Lycium ferocissimum ) is a dense woody shrub up to 4 m high and 3 m wide, " +
			"without any hairs on any of the parts. Rigid branches end in long spines, up to 15 cm long. Leaves are small (up to 4 " +
			"cm long by 1 cm wide), fleshy and often clustered in groups. The tubular flowers are about 10 mm in diameter and 10-12 " +
			"mm long with five lobes, white or pale purplish with deeper purple inside the flower. The fruit is a red to orange, " +
			"shining berry on a short down-turned stalk. It is round, 5-10 mm diameter and slightly wider at the end away from the " +
			"green calyx which envelops the base of the fruit. Seeds are oval or irregular in outline shape, flattened, 2.5 by 1.5 " +
			"mm, light brown to yellow (Purdie et al. 1982; Navie 2004). For further information and assistance with identification " +
			"of African Boxthorn contact the herbarium in your state or territory.";
	final String hasDistributionText = "African Boxthorn is found across southern Australia in agricultural and pastoral areas and " +
			"waste places around towns and cities. It seems tolerant of most soil types and also of some salinity. It is especially " +
			"abundant in areas of high rainfall. Where its distribution enters drier regions the plants are generally found close to " +
			"permanent or seasonal water supplies (Haegi 1976).";
	final String[] hasHabitatText = {"Shrub",
									"African Boxthorn is generally found on waste-land, creek-beds, fence-lines and roadsides in " +
									"arid sub-humid and semi-arid subtropical regions (Navie 2004)."};
	final String hasReproductionText = "African Boxthorn reproduces mostly by seeds that are commonly dispersed when the fruit are " +
			"eaten by birds and other animals (e.g. foxes). Seeds may also be spread by water, machinery and in dumped garden waste " +
			"or contaminated soil. Suckers are also sometimes produced from root fragments; shoots are rarely produced from stem " +
			"fragments (Navie 2004).";
	final String hasFamily = "Solanaceae";
	final String hasGenus = "Lycium";
	final String hasSpecies = "ferocissimum";
	final String hasCommonName = "African Boxthorn, Boxthorn";
	final String[] hasSynonym = {"African Boxthorn",
								"Boxthorn"};
	
	
	public void test() throws Exception {
		WeedDocumentMapper dm = new WeedDocumentMapper();
		String uri = "http://www.weeds.gov.au/cgi-bin/weeddetails.pl?taxon_id=19235";
		String xml = WebUtils.getHTMLPageAsXML(uri);
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
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasReproductionText")) {
					Assert.assertEquals(object, hasReproductionText);		    	  
				}
				
				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);		    	  
				}	
				
				if (predicate.endsWith("hasSpecies")) {
					Assert.assertEquals(object, hasSpecies);		    	  
				}	
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);		    	  
				}
				
				if (predicate.endsWith("hasSynonym")) {
					Assert.assertTrue(arrayContainsElement(hasSynonym,object));		    	  
				}
				
				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertTrue(arrayContainsElement(hasHabitatText,object));		    	  
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
