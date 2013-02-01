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
 * Document mapper for Birdway
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class BirdwayDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public BirdwayDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//td[@class='cell_species_heading']/i/text()",subject, Predicates.DC_TITLE);
//        addDCMapping("//font[contains(.,'Photograph by')]/b[1]/text()",subject, Predicates.DC_CREATOR);
        addTripleMapping("//td[@class='cell_species_heading']/i/text()", subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//div[@id='main_photo']/img/@src", subject, Predicates.IMAGE_URL);
        addDCMapping("//p[@class='copyright']/a/text()", subject, Predicates.DC_RIGHTS);
        
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
		
//		documentStr = documentStr.replaceAll("<![\\-a-zA-Z ]{1,}>", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
//		System.out.println(documentStr);
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		
		
		String titleStr = null; 
        String subject = MappingUtils.getSubject();
		
		ParsedDocument pd = pds.get(0);
		String source = pd.getGuid().substring(0, pd.getGuid().lastIndexOf('/')+1);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.birdway.com.au/printpurchases.htm#free");
//		pd.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Copyright Ian Montgomery http://www.birdway.com.au/printpurchases.htm#free");
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
		String rights = pd.getDublinCore().get(Predicates.DC_RIGHTS.toString());
		
//		System.out.println(rights);
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasImageUrl")) {
			    String imageUrl = (String) triple.getObject();
			    imageUrl = source + imageUrl;
                triple.setObject(imageUrl);
                
                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null && rights.contains("Ian Montgomery")){
                    List<Triple<String,String,String>> imageDocTriples = imageDoc.getTriples();
                    imageDoc.setTriples(imageDocTriples);
                    pds.add(imageDoc);
                }
//                tmpTriple.add(triple);
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
	
	private String cleanBrackets(String str) {
		str = str.replaceAll("\\(", "");
		str = str.replaceAll("\\)", "");
		
		if ("".equals(str.trim())) {
			return null;
		} else {
			return str.trim();
		}
		
	}
}
