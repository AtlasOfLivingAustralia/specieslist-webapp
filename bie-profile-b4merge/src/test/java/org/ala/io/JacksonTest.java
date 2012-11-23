/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
package org.ala.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.ala.model.CommonName;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
/**
 * JUnit tests for testing Jackson functionality.
 * 
 * @author Dave Martin
 */
public class JacksonTest extends TestCase {

	public void testJackson2() throws Exception {
		String json = "{\"sitemap\":\"http://www2.ala.org.au/sitemaps/abrsfloraozonline/siteMap.txt\"}";
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> result = mapper.readValue(json, new TypeReference<Map<String,String>>() { });
		assertEquals(result.get("sitemap"), "http://www2.ala.org.au/sitemaps/abrsfloraozonline/siteMap.txt");
	}
	
	public void testJackson() throws Exception {
		
		CommonName cn1 = new CommonName();
		cn1.setGuid("urn:lsid:afd.taxon:123");
		cn1.setNameString("Red Kangaroo");

		CommonName cn2 = new CommonName();
		cn2.setGuid("urn:lsid:afd.taxon:124");
		cn2.setNameString("Eastern Grey Kangaroo");
		
		CommonName cn3 = new CommonName();
		cn3.setNameString("Kangaroo");		
		
		List<CommonName> cns = new ArrayList<CommonName>();
		cns.add(cn1);
		cns.add(cn2);
		cns.add(cn3);		
		
		//serialise	
		ObjectMapper mapper = new ObjectMapper();
		//serialise to file
		File tmpFile = File.createTempFile("test", ".txt");
		mapper.writeValue(tmpFile, cns); 
		System.out.println("Content written to: "+tmpFile.getPath());
		
		//deserialise
		List<CommonName> values = mapper.readValue(tmpFile, new TypeReference<List<CommonName>>(){});
		for(CommonName commonName: values){
			System.out.println("Read content: "+commonName.getGuid());
			System.out.println("Read content: "+commonName.getNameString());
		}
	}
}
