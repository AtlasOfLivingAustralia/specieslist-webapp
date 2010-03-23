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
import org.ala.documentmapper.QgeDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class QgeDocumentMapperTest extends TestCase {
	
	final String hasScientificName = "Onychogalea fraenata";
	final String[] hasCommonName = {"Bridled nailtail wallaby",
									"flashjack",
									"merrin",
									"waistcoat wallaby"};
	final String hasConservationStatus = "This species is listed as Endangered in Queensland (Nature Conservation Act 1992) and nationally (" +
			"Commonwealth Environment Protection and Biodiversity Conservation Act 1999).";
	final String hasDistributionText = "The bridled nailtail wallaby lives in semi-arid areas where dense acacia shrubland and grassy woodland meet. " +
			"At the time of European settlement this was a common species with a distribution reaching the west of the Great Dividing Range, north to " +
			"Charters Towers in Queensland, south to north-western Victoria, and possibly extending west to eastern South Australia. The bridled " +
			"nailtail wallaby now survives in a small percentage of the area it once inhabited. For over 30 years they were believed to be extinct as " +
			"there had been no confirmed sightings of individuals since 1937. Then, in 1973, the species was ‘re-discovered’ by a fencing contractor, " +
			"who after reading an article about Australia's extinct species in a magazine, reported that there was a population of bridled nailtail " +
			"wallabies on a property in central Queensland near the town of Dingo. This was confirmed by researchers from the Queensland Parks and " +
			"Wildlife Service and the property eventually became Taunton National Park (Scientific). Current population estimates for Taunton National " +
			"Park (Scientific), including neighbouring properties, are approximately 200 wallabies.";
	final String hasDescriptiveText = "The bridled nailtail wallaby is a small wallaby with males weighing an average of 5-6kg and females 4-5kg. It " +
			"is grey to light tan in colour with a distinct white line forming a \"bridle\" from the back of the neck to behind the forelimbs. Its " +
			"other distinctive markings are the white stripes along the sides of the face, and a black stripe down the length of the back. There are " +
			"three species of wallaby that have the characteristic \"nail-tail\", with a nail-like spur about 3-6mm long at the tip of the tail: the " +
			"bridled nailtail wallaby, crescent nailtail wallaby (believed to be extinct) and northern nailtail wallaby (common in northern Australia).";
	final String hasDietText = "The preferred diet of the bridled nailtail wallaby is largely non-woody broad-leafed plants, chenopods (succulents " +
			"including pigweed), flowering plants and grasses. Two potential competitors for this food include the black-striped wallaby (Macropus dorsalis) " +
			"and domestic stock.";
	final String hasThreatsText = "Declines in the range and numbers of bridled nailtail wallabies are believed to be from a combination of the following: " +
			"Predation, primarily by foxes and feral cats, and some predation from wild dogs; habitat loss, modification and degradation (through land clearing, " +
			"drought, fire, and buffel grass); and competition with introduced stock (mainly sheep) and rabbits. The Queensland Hunting and Conservation " +
			"division (H&C) of the Sporting Shooters’ Association of Australia (SSAA) undertake predator control activities at Taunton National Park and " +
			"Avocet Nature Refuge. The primary target species are foxes and feral cats although dingoes and rabbits are taken as well depending on " +
			"numbers present. H&C recently received a grant from the Threatened Species Network (TSN) of the World Wide Fund for Nature (WWF) to acquire " +
			"feral cat traps for use at Avocet Nature Refuge.";
	final String hasImageUrl = "http://www.derm.qld.gov.au/images/wildlife-ecosystems/wildlife/endangered/clip_image002_0010.jpg";
		
	public void test() throws Exception {
		QgeDocumentMapper dm = new QgeDocumentMapper();
//		String uri = "http://www.derm.qld.gov.au/wildlife-ecosystems/wildlife/threatened_plants_and_animals/endangered/cassowary.html";
		String uri = "http://www.derm.qld.gov.au/wildlife-ecosystems/wildlife/threatened_plants_and_animals/endangered/bridled_nailtail_wallaby.html";
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
					Assert.assertTrue(arrayContainsElement(hasCommonName,object));		    	  
				}

				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(object, hasConservationStatus);		    	  
				}

				if (predicate.endsWith("hasDietText")) {
					Assert.assertEquals(object, hasDietText);		    	  
				}
				
				if (predicate.endsWith("hasThreatsText")) {
					Assert.assertEquals(object, hasThreatsText);		    	  
				}
				
				if (predicate.endsWith("hasImageUrl")) {
					Assert.assertEquals(object, hasImageUrl);		    	  
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
