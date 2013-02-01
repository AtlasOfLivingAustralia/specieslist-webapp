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
 * Document mapper for Aussie Creatures
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class AussieCreaturesDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public AussieCreaturesDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//h2/b/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//div[@id=\"photobox\"]/a/img/@src", subject, Predicates.IMAGE_URL);
        addTripleMapping("//h2/b/text()", subject, Predicates.COMMON_NAME);
        addTripleMapping("//h2/following-sibling::div[@id='image_notes2']/p/i[1]/text()", subject, Predicates.SCIENTIFIC_NAME);
        
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
		
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		
		content = documentStr.getBytes();
		
		return super.map(uri, content);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@SuppressWarnings("unchecked")
    @Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		// extract details of images
		
		ParsedDocument pd = pds.get(0);
		
		String baseUrl = "http://www.aussiecreatures.net/";
		
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String subject = MappingUtils.getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "Attribution-Noncommercial – CC BY-NC");
        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            
            if(predicate.endsWith("hasScientificName")) {
                String sciName = (String) triple.getObject();
                if (sciName.contains(",")) {
                    sciName = sciName.replaceAll(",", "");
                }
                
                triple.setObject(sciName.trim());
            }
        }
        
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
                imageUrl = baseUrl + imageUrl;
                
                triple.setObject(imageUrl);
                
                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null){
                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Photo: Steve Dew, www.aussiecreatures.net");
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Steve Dew");
                    pds.add(imageDoc);
                }
                
                tmpTriple.add(triple);
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
