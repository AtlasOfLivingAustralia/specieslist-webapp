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
package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

/**
 * Document mapper for Unique Flora of Tasmania 
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class UftDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public UftDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//font[@face=\"Arial Black\"]//text()",subject, Predicates.DC_TITLE);
//        addTripleMapping("//font[@face=\"Arial Black\"]//text()", subject, Predicates.COMMON_NAME);
        addTripleMapping("([A-Z]{4,} [A-Z]{3,})", MappingType.REGEX, subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//big[1]/b/text()", subject, Predicates.SCIENTIFIC_NAME);
//        addTripleMapping("//td[@class=\"title\"]/text()", subject, Predicates.GENUS);
        addTripleMapping("//img/@src", subject, Predicates.IMAGE_URL);
        
//		
////		addTripleMapping("//p[b[contains(.,\"Attached Images\")]]/following::p[1]/a/attribute::href",
//		addTripleMapping("//img[@class=\"page\"]/attribute::src",
//				subject, Predicates.IMAGE_URL);
//		
//		addTripleMapping("//a[@class=\"mappopup\"]/span/img/attribute::src",
//                subject, Predicates.DIST_MAP_IMG_URL);
	}
	
	
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
		
		documentStr = documentStr.replaceAll("<![\\-a-zA-Z ]{1,}>", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
//		System.out.println(documentStr);
		
		String[] documentStringArray = documentStr.split("<table");
		
		if (documentStringArray.length >= 2) {
		    for (int i = 1; i < documentStringArray.length; i++) {
		        documentStringArray[i] = "<table" + documentStringArray[i];
		        documentStringArray[i] = documentStringArray[i].substring(0, documentStringArray[i].indexOf("</table") + 8);
		        content = documentStringArray[i].getBytes();
//		        System.out.println("!!!!" + documentStringArray[i]);
		        pds.addAll(super.map(uri, content));
		        
		    }
		}
		
		boolean gotSciName = false;
		
		for (ParsedDocument pd : pds) {
		    List<Triple<String,String,String>> triples = pd.getTriples();
		    
		    for (Triple<String,String,String> triple: triples) {
	            String predicate = triple.getPredicate().toString();
	            
	            if(predicate.endsWith("hasScientificName")) {
	                gotSciName = true;
	            }
		    }
		}
		
		if (!gotSciName) {
		    content = documentStr.getBytes();
		    
		    pds.addAll(super.map(uri, content));
		}
		
		return pds;
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@SuppressWarnings("unchecked")
    @Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		String source = "http://www.apstas.com/";
		
        String subject = MappingUtils.getSubject();
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC BY-NC-SA Attribution-Noncommercial-Share Alike");
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "J & R Coghlan, Australian Plants Society Tasmania");
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
		
		
		
		for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            
            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
                imageUrl = source + imageUrl;
                
                
                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null && imageUrl.endsWith(".jpg")){
                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "J & R Coghlan, Australian Plants Society Tasmania");
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "J & R Coghlan");
                    pds.add(imageDoc);
                    triple.setObject(imageUrl);
                } else {
                    tmpTriple.add(triple);
                }
            }
            
            if(predicate.endsWith("hasScientificName")) {
                String sciName = (String) triple.getObject();
                pd.setGuid(pd.getGuid() + "#" + sciName.replaceAll(" ", ""));
            }
		}
//		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
	}
}
