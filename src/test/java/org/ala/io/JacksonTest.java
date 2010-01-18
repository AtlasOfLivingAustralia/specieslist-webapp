package org.ala.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ala.model.CommonName;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JacksonTest extends TestCase {

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
