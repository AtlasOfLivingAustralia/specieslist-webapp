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

import org.ala.documentmapper.SabDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class SabDocumentMapperTest extends TestCase {

	final String hasScientificName = "Bayonet Spider-orchid Caladenia gladiolata";
	
	public void test() throws Exception {
		SabDocumentMapper dm = new SabDocumentMapper();
//		String uri = "http://www.environment.sa.gov.au/biodiversity/pdfs/bayonet.pdf";
		String uri = "http://www.environment.sa.gov.au/biodiversity/pdfs/ki_turpentine_bush.pdf";
		Response response = WebUtils.getUrlContentAsBytes(uri);
		byte[] contentBytes = response.getResponseAsBytes();
//		System.out.println(new String(contentBytes));
		List<ParsedDocument> parsedDocs = dm.map(uri, contentBytes);
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
//				if (predicate.endsWith("hasScientificName")) {
//					Assert.assertEquals(object, hasScientificName);
//				}

			}
		}
	}
}
