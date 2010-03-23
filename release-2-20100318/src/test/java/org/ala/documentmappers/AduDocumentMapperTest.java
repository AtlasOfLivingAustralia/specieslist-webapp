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
import org.ala.documentmapper.AduDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AduDocumentMapperTest extends TestCase {

	final String hasScientificName = "Acropyga myops Forel, 1910";
	final String hasDistributionMapImageUrl = "http://anic.ento.csiro.au/ants/getmap.aspx?biotaid=37173&w=600&h=400";
	final String hasDescriptionText = "This is the only species of Acropyga that is endemic to Australia. It is widespread," +
			" occurring mainly at drier forested sites and less commonly in wet sclerophyll and rainforest. Nests are in soil " +
			"under rocks. It is the only known species of Acropyga that tend non-mealybugs (in the family Ortheziidae rather " +
			"than Pseudococcidae).";
	final String[] hasImageUrl = {"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=4208",
								"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=4206",
								"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=4207",
								"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=877",
								"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=875",
								"http://anic.ento.csiro.au/ants/image_details.aspx?ImageID=876"
								};
	
	public void test() throws Exception {
		AduDocumentMapper dm = new AduDocumentMapper();
		String uri = "http://anic.ento.csiro.au/ants/biota_details.aspx?BiotaID=37173";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
//		Assert.assertEquals(7,parsedDocs.size()); //1 HTML doc and 6 images
		
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(object, hasScientificName);
				}

				if (predicate.endsWith("hasDescriptionText")) {
					Assert.assertEquals(object, hasDescriptionText);
				}
			}
		}
	}
}
