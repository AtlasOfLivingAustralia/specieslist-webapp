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
 * Document mapper for Spiders of Australia
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class EnDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public EnDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//b//text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//img/@src", subject, Predicates.IMAGE_URL);
        addTripleMapping("//b[1]//text()", subject, Predicates.SCIENTIFIC_NAME);
        
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
		
		String documentRawStr = new String(content);
		
		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
		
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
//		documentStr = documentStr.replaceAll("</p>[\\s]*<p>", "");
		
//		System.out.println(documentStr);
		
		String[] documentStrArray = documentRawStr.split("<hr/>");
		
		if (documentStrArray.length > 1) {
		    for (int i = 1; i < documentStrArray.length - 1; i ++) {
//		        System.out.println(documentStrArray[i]);
		        
		        String documentStr = "<div>" + documentStrArray[i] + "</div>";
		        
		        byte[] tmpContent = documentStr.getBytes();
		        
		        pds.addAll(super.map(uri, tmpContent));
		    }
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
		
		ParsedDocument pd = pds.get(0);
		
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		List<String> imageUrlList = new ArrayList<String>();
		
		String subject = MappingUtils.getSubject();
		String baseUrl = pd.getGuid();
		String family = null;
		String genus = null;
		
		boolean gotSciName = false;
		
		baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/')+1);
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY-NC");
        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            
            if(predicate.endsWith("hasScientificName")) {
                gotSciName = true;
            }
        }
        
        if (!gotSciName) {
            String sciName = getXPathSingleValue(xmlDocument, "//strong[1]//text()");
            
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
        }
        
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
//                imageUrl = imageUrl.replaceAll(" ", "%20");
                
                imageUrl = baseUrl + imageUrl;
                
                imageUrlList.add(imageUrl);
                
                triple.setObject(imageUrl);
            }
			
			if(predicate.endsWith("hasScientificName")) {
			    String object = (String) triple.getObject();
			    
			    if (object.contains("formerly")) {
			        object = object.split("formerly")[0];
			    }
			    
			    if (object.contains("Genus")) {
			        object = object.replaceAll("Genus", "").trim();
			        
			        genus = object;
			    }
			    
			    if (object.contains("Family")) {
                    object = object.replaceAll("Family", "").trim();
                    
                    family = object;
                }
			    
			    pd.setGuid(pd.getGuid() + "#" + object);
			    
			    triple.setObject(object);
			}
		}
		
		
		
		if (genus != null && !"".equals(genus)) {
		    pd.getDublinCore().put(Predicates.DC_TITLE.toString(), genus);
		    triples.add(new Triple(subject, Predicates.GENUS.toString(), genus));
		}
		
		if (family != null && !"".equals(family)) {
		    pd.getDublinCore().put(Predicates.DC_TITLE.toString(), family);
		    triples.add(new Triple(subject, Predicates.FAMILY.toString(), family));
		}
//		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));
		
		for (String imageUrl : imageUrlList) {
		    //retrieve the image and create new parsed document
		    ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
		    if(imageDoc!=null){
		        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Ed Nieuwenhuys, Spiders of Australia(www.xs4all.nl/~ednieuw)");
		        imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Ed Nieuwenhuys");
		        pds.add(imageDoc);
		    }
		}
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
	}
	
}
