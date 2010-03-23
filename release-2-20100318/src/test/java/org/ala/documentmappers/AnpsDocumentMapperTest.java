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
import org.ala.documentmapper.AnpsDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AnpsDocumentMapperTest extends TestCase {

	final String hasFamily = "Myrtaceae";
	final String hasScientificName = "Eucalyptus regnans";
	final String hasCommonName = "Mountain Ash";
	final String hasDistributionText = "Open forest in southern and eastern Victoria and Tasmania.";
	final String hasConservationStatus = "Not considered to be at risk in the wild.";
	final String[] hasDescriptiveText = {
			"Eucalyptus regnans is an important timber tree and is widely used in building and in the paper industry. " +
			"In Australia, timber is harvested from both natural and cultivated stands. The species is also cultivated overseas.",
			"Mountain ash is the tallest hardwood tree in the world with specimens reaching 80 metres or more in height. Only the " +
			"softwood Californian redwoods ( Sequoia sempervirens ) are taller, one of these having been recorded at about 113 " +
			"metres. However, it is believed that specimens of E.regnans felled during the 1800s may have reached more than 140 " +
			"metres ( Guiness Book of Records ), making the species the tallest tree ever recorded on earth in historic times. " +
			"Sadly, all of these majestic giants have been felled.",
			"The following extract from \"Forests of Australia\" by Alexander Rule (1967) indicates the massive proportions of these " +
			"trees. It records the felling of a tree in the Derwent Valley, Tasmania in 1942: \"It is recorded that two expert " +
			"axemen, working on a platform 15 feet above the ground, took two and a half days to cut a scarf 6 feet deep into " +
			"the mighty butt as a preliminary to sending the giant toppling to earth. The crash of its fall resounded for " +
			"miles around and even hardened bushworkers are said to have downed tools in silent homage to the fallen monarch. " +
			"Its age was put at 400 years and it was calculated that when Abel Tasman discovered the island in 1642 this tree " +
			"was already a noble specimen of between 150 and 200 feet in height.\"",
			"The tree \"yielded 6770 cubic feet of wood which was pulped into 75 tons of newsprint.\"",
			"The species is native to wet sclerophyll forests (tall open forests) in the Otway Ranges in southern Victoria, " +
			"the Gippsland Forests in eastern Victoria and north-eastern and southern Tasmania. It occurs from sea level to " +
			"altitudes over 1000 metres. It is a fire-sensitive species. Unlike many other eucalypts, E.regnans is killed " +
			"outright by severe fires and does not regenerate from a lignotuber or from epicormic shoots under the bark. It " +
			"relies solely on seed for regeneration and can be eliminated from an area by fires which occur at frequent intervals.",
			"The tall trunk is smooth except for the lower few metres where the bark is retained. The trunk is white or grey in " +
			"colour. Given its size, this tree is not suitable for any but the largest gardens and parks.",
			"Propagation is from seed which germinates readily but, for optimum germination, stratification in a refrigerator for " +
			"3 weeks prior to sowing is recommended."
			
	};
	
	public void test() throws Exception {
		AnpsDocumentMapper dm = new AnpsDocumentMapper();
		String uri = "http://asgap.org.au/eregn.html";
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

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);		    	  
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);		    	  
				}
				
				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(object, hasConservationStatus);		    	  
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertTrue(arrayContainsElement(hasDescriptiveText, object));		    	  
				}
			}
		}
	}
	
	// Check Whether a String array contains a String element 
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
