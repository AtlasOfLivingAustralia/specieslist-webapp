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
import org.ala.documentmapper.AusmarinvDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AusmarinvDocumentMapperTest extends TestCase {

	final String[] hasImageUrl = {	"http://www.ausmarinverts.net/images/Mollusca/Gastropoda/Orthogastropoda/A_praetermiss-AdventureBay1.jpg",
	"http://www.ausmarinverts.net/images/Mollusca/Gastropoda/Orthogastropoda/A_praetermiss-AdventureBay2.jpg"};
	final String hasScientificName = "Afrolittorina praetermissa";
	final String hasPhylum = "Mollusca";
	final String hasClass = "Gastropoda";
	final String hasSubclass = "Orthogastropoda";
	final String hasOrder = "Sorbeoconcha";
	final String hasFamily = "Littorinidae";
	final String hasGenus = "Afrolittorina";
	final String hasSpecificEpithet = "praetermissa";
	final String hasSimilarSpecies = "Austrolittorina unifasciata";

	public void test() throws Exception {
		AusmarinvDocumentMapper dm = new AusmarinvDocumentMapper();
		String uri = "http://www.ausmarinverts.net/Afrolittorina_praetermissa.html";
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
				
				if (predicate.endsWith("hasPhylum")) {
					Assert.assertEquals(object, hasPhylum);		    	  
				}
				
				if (predicate.endsWith("hasOrder")) {
					Assert.assertEquals(object, hasOrder);		    	  
				}
				
				if (predicate.endsWith("hasClass")) {
					Assert.assertEquals(object, hasClass);		    	  
				}
				
				if (predicate.endsWith("hasSubclass")) {
					Assert.assertEquals(object, hasSubclass);		    	  
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);		    	  
				}
				
				if (predicate.endsWith("hasSpecificEpithet")) {
					Assert.assertEquals(object, hasSpecificEpithet);		    	  
				}
				
				if (predicate.endsWith("hasSimilarSpecies")) {
					Assert.assertEquals(object, hasSimilarSpecies);		    	  
				}
			}
		}
	}
}
