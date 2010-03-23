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
import org.ala.documentmapper.LoAHtmlDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class LoAHtmlDocumentMapperTest extends TestCase {

	final String hasScientificName = "Telsimia elainae";
	final String hasSynonym = "Telsimia elainae";
	final String hasDistributionText = "Distribution and Biology Known from northern Queensland and New Guinea. The larva is " +
			"undescribed. Chazeau (1984) recorded elainae as associated with Unaspis citri Comstock (Diaspididae) and Nipaecoccus sp. " +
			"(Pseudococcidae) on citrus. Most of the Australian material was collected from various trees and shrubs infested with " +
			"aleurodids and scales.";
	final String hasDescriptiveText = "Length 1.3-1.6 mm. Strongly convex, shortly oval; dorsal surfaces black except for testaceous " +
			"clypeus, at least anterior half of frons and, sometimes, small, anterolateral areas of pronotum; underside variably " +
			"coloured but with elytral epipleura and at least abdominal ventrites 2-5 testaceous, elsewhere pitchy, legs usually " +
			"testaceous. Head with strong, forwardly directed pubescence. Pronotum arcuate medially, distance between anterior " +
			"angles 0.6 times that between posterior angles, lateral borders evenly and arcuately convergent from bases to anterior " +
			"angles; discal punctures as large as eye facets, separated by 1 diameter or more, intervals smooth; punctures on " +
			"lateral margins not much larger than those on disc, shallow, separated as a rule by less than 1 diameter; pubescence " +
			"mostly subrecumbent, laterally directed on either side of midline, forwardly directed near base on either side of " +
			"scutellum. Elytra quadrate, external margins not explanate to very narrowly out-turned external borders; punctures of " +
			"disc and margins distinctly larger than those of pronotal disc, often separated by 1 diameter or less, intervals smooth; " +
			"pubescence curved, suberect for the most part, arranged in a distinctive whorled pattern. Prosternal surface coriaceous, " +
			"punctures barely perceptible; mesoventrite about twice as broad as median length, anterior border nearly straight, " +
			"punctures of disc and margins separated by about 1 diameter; metasternum with punctures larger than those of mesoventrite, " +
			"separated on disc by a little more than 1 diameter, somewhat closer laterally, discal intervals smooth, surface toward " +
			"lateral borders microsculptured. Abdomen with borders of postcoxal plates of first abdominal ventrite not recurved " +
			"externally, punctures of intercoxal process smaller than those of metasternum and generally separated by more than one " +
			"diameter. Legs: all tarsal claws appendiculate.";
	
	public void test() throws Exception {
		LoAHtmlDocumentMapper dm = new LoAHtmlDocumentMapper();
		String uri = "http://www.ento.csiro.au/biology/ladybirds/lucid/key/lucidKey/Media/Html/telsiElainae1.htm";
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
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
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
