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
import org.ala.documentmapper.AdfdDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AdfdDocumentMapperTest extends TestCase {

	final String hasScientificName = "Nematalosa erebi";
	final String hasCommonName = "bony bream";
	final String hasConservationStatus = "None";
	final String hasMorphologicalText = "In the Murray River, they have been recorded as growing to 470 mm (19in) " +
			"TL, however in Cooper Creek bony bream don't grow as large (J. Puckridge pers. comm.).";
	final String hasDistributionText = "Widespread throughout Central Australia. It also occurs throughout the " +
			"Murray-Darling Drainage Basin and most of northern Australia. It is Australia's second most widespread " +
			"freshwater fish species. They are typically abundant in most places they occur.";
	final String hasHabitatText = "Found in waterholes in all of the larger rivers and in many of the smaller, more " +
			"ephemeral tributaries. Bony bream are apparently fill a very similar ecological niche to the North " +
			"American genus of Dorosoma (J. Puckridge pers. comm.). They are usually more commonly associated with " +
			"still water environments (which is what central Australia is like for most of the time). They are one of " +
			"the few Australian fishes which are primarily herbivorous when mature. Their diet consists of mainly algae " +
			"and plant material, although some crustaceans and insects are also ingested. They can survive a wide range " +
			"of temperatures, between 9 and 38�C (46-100�F), and pH values from 4.8 to 8.6 (Merrick & Schmida 1984). " +
			"Annual kills are common during winter. This has been attributed to several causes such as achlya infections " +
			"(Puckridge, Walker, Langdon, Daley, & Beakes 1989), parasitic infections (Langdon, Gudkovs, Humphrey, & " +
			"Saxon 1985), and low water temperatures (Merrick & Schmida 1984).";
	final String hasReproductionText = "Spawning occurs in early summer independent of flooding. They are highly fecund, " +
			"producing 33,000 eggs for 199mm (8in) fish to 880,000 for fish 403mm (16in) TL (Puckridge & Walker 1990). In " +
			"Cooper Creek, they have a much broader spawning period (J. Puckridge pers. comm.). They probably spawn at " +
			"temperatures around 24-26�C (75-79�F).";
	final String hasImageUrl = "nemaereb.jpg";
	
	public void test() throws Exception {
		AdfdDocumentMapper dm = new AdfdDocumentMapper();
		String uri = "http://www.desertfishes.org/australia/fish/nemaereb.shtml";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(hasScientificName,object);
				}

				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(hasCommonName,object);
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(hasDistributionText,object);
				}
				
				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(hasConservationStatus,object);
				}
				
				if (predicate.endsWith("hasMorphologicalText")) {
					Assert.assertEquals(hasMorphologicalText,object);
				}
				
				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(hasHabitatText,object);
				}
				
				if (predicate.endsWith("hasReproductionText")) {
					Assert.assertEquals(hasReproductionText,object);
				}
			}
		}
	}
}
