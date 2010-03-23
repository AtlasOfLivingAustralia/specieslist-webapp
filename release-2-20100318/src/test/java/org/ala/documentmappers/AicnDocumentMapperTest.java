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
import org.ala.documentmapper.AicnDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AicnDocumentMapperTest extends TestCase {

	final String currentUrl = "http://www.ento.csiro.au/aicn/name_s/b_4201.htm";
	final String hasCommonName = "tree lucerne moth";
	// final String hasImageUrl = "../images/cain2344.jpg";
	final String hasImageUrl = "http://www.ento.csiro.au/aicn/images/cain2344.jpg";
	final String hasPestStatus = "Native";
	final String hasPhylum = "ARTHROPODA";
	final String hasClass = "HEXAPODA";
	final String hasOrder = "Lepidoptera";
	final String hasFamily = "Pyralidae";
	final String hasScientificName = "Uresiphita ornithopteralis";
	
	public void test() throws Exception {
		AicnDocumentMapper dm = new AicnDocumentMapper();
		String uri = "http://www.ento.csiro.au/aicn/name_s/b_4201.htm";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);		    	  
				}
				
				if (predicate.endsWith("hasPestStatus")) {
					Assert.assertEquals(object, hasPestStatus);		    	  
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
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(object, hasScientificName);		    	  
				}
			}
		}
	}
}
