package org.ala.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.ala.io.CommonNamesProto.CommonNames;

public class ProtoBufsTest extends TestCase {

	public void testCommonName() throws Exception {
		
		//to create a names list
		CommonNamesProto.CommonNames.Builder builder = CommonNamesProto.CommonNames.newBuilder();
		CommonNamesProto.CommonNames.CommonName cn1 = CommonNamesProto.CommonNames.CommonName.newBuilder()
			.setGuid("urn:lsid:afd.taxon:123")
			.setLocality("New South Wales")
			.setNameString("Red Kangaroo")
			.setDocumentId("document-1")
			.setInfoSourceId("infosource-1")
			.build();
		
		CommonNamesProto.CommonNames.CommonName cn2 = CommonNamesProto.CommonNames.CommonName.newBuilder()
			.setGuid("urn:lsid:afd.taxon:124")
			.setLocality("New South Wales")
			.setNameString("Eastern Grey Kangaroo")
			.setDocumentId("document-2")
			.setInfoSourceId("infosource-1")
			.build();		
		
		//build the list
		builder.addNames(cn1);
		builder.addNames(cn2);
		CommonNames commonNames = builder.build();
		
		//serialise to file
		File tmpFile = File.createTempFile("test", ".txt");
		
		FileOutputStream fOut = new FileOutputStream(tmpFile);
		commonNames.writeTo(fOut);
		
		//read the protobufs from file
		FileInputStream fIn = new FileInputStream(tmpFile);
		CommonNamesProto.CommonNames deserialisedCommonNames = CommonNamesProto.CommonNames.parseFrom(fIn);
		System.out.println(deserialisedCommonNames.getNamesCount());
		
		for(CommonNamesProto.CommonNames.CommonName commonName: deserialisedCommonNames.getNamesList()){
			System.out.println("Deserialised: "+commonName.getNameString());
		}
		
		//add a common name
		CommonNamesProto.CommonNames.CommonName cn3 = CommonNamesProto.CommonNames.CommonName.newBuilder()
			.setGuid("urn:lsid:afd.taxon:124")
			.setLocality("New South Wales")
			.setNameString("Grey Kangaroo")
			.setDocumentId("document-2")
			.setInfoSourceId("infosource-1")
			.build();			
		
		deserialisedCommonNames = deserialisedCommonNames.toBuilder().addNames(cn3).build();
		tmpFile = File.createTempFile("test", ".txt");
		fOut = new FileOutputStream(tmpFile);
		deserialisedCommonNames.writeTo(fOut);
	}
}
