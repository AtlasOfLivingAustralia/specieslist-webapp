package org.ala.documentmappers;

import java.util.List;
import java.util.Map;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;

public class DebugUtils {

	public static void debugParsedDoc(ParsedDocument parsedDoc){
		
		System.out.println("===============================================================================");
		
		System.out.println("GUID: "+parsedDoc.getGuid());
		System.out.println("Content-Type: "+parsedDoc.getContentType());
		
		Map<String,String> dublinCore = parsedDoc.getDublinCore();
		for(String key: dublinCore.keySet()){
			System.out.println("DC: "+key+"\t"+dublinCore.get(key));
		}
		
		List<Triple<String,String,String>> triples = parsedDoc.getTriples(); 
		for(Triple<String,String,String> triple: triples){
			System.out.println("RDF: "+triple.getSubject()+"\t"+triple.getPredicate()+"\t"+triple.getObject());
		}
		
		System.out.println("===============================================================================");
	}
	
}
