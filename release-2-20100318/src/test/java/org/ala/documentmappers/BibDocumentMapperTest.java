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
import org.ala.documentmapper.BibDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class BibDocumentMapperTest extends TestCase {

	final String hasScientificName = "Pygoscelis adeliae";
	final String hasFamily = "Spheniscidae";
	final String hasOrder = "Sphenisciformes";
	final String hasSimilarSpecies = "The Chinstrap Penguin, P. antarctica , has a longer bill, and a white face crossed " +
			"by a thin black line. The Gentoo Penguin, P. papua , is larger (80 cm long) with a black and yellow or orange " +
			"bill, and a white patch over the top of the head.";
	final String hasReproductionText = "Adelie Penguins breed in summer, mainly on the rocky platforms of islands of " +
			"southern oceans. The nests are made out of small stones, and males and females share incubation and care " +
			"of the young almost equally.";	
	final String hasDietText = "Adelie Penguins feed mainly on fish, crustaceans, amphipods and cephalopods. They can " +
			"dive to about 175 m but usually feed up to 70 m below the water's surface.";
	final String hasDistributionText = "Adelie Penguins are found mainly at sea in southern oceans " +
			"around the coasts and islands of Antarctica, and only very rarely appear on the Australian coast, in winter.";
	final String hasImageUrl = "http://www.birdsinbackyards.net/images/factsheets/full/Pygoscelis/adeliae/adelie_penguin_e_woehler.jpg";
	
	public void test() throws Exception {
		BibDocumentMapper dm = new BibDocumentMapper();
		String uri = "http://www.birdsinbackyards.net/bird/216";
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
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasOrder")) {
					Assert.assertEquals(object, hasOrder);		    	  
				}
				
				if (predicate.endsWith("hasSimilarSpecies")) {
					Assert.assertEquals(object, hasSimilarSpecies);		    	  
				}
				
				if (predicate.endsWith("hasReproductionText")) {
					Assert.assertEquals(object, hasReproductionText);		    	  
				}
				
				if (predicate.endsWith("hasDietText")) {
					Assert.assertEquals(object, hasDietText);		    	  
				}
			}
		}
	}
}
