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
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.ala.documentmapper.FishnamesDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class FishnamesDocumentMapperTest extends TestCase {

	final String hasCAABCode = "37 310009";
	final String hasCommonName = "Agassiz's Glassfish";
	final String hasScientificName = "Ambassis agassizii";
	final String hasAuthor = "Steindachner, 1867";
	final String hasFamily = "Ambassidae";
	
	public void test() throws Exception {
		FishnamesDocumentMapper dm = new FishnamesDocumentMapper();
		String uri = "http://www.fishnames.com.au//fishnames/fishnames.php?pid=2013";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			List<Triple<String,String,String>> triples = pd.getTriples();
			DebugUtils.debugParsedDoc(pd);
			
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasCAABCode")) {
					Assert.assertEquals(object, hasCAABCode);
				}

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);
				}
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);
				}
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(object, hasScientificName);
				}
				
				if (predicate.endsWith("hasAuthor")) {
					Assert.assertEquals(object, hasAuthor);
				}
			}
		}
	}
}
