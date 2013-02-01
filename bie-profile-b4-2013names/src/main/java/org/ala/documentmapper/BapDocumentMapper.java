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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.w3c.dom.Document;

/**
 * Document mapper for Barry Armstead Photography
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class BapDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public BapDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//div[@class='fw-text']/h2/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//div[@class=\"fw-photo-frame\"]/a/img/@src", subject, Predicates.IMAGE_URL);
        addTripleMapping("//div[@class='fw-text']/h2/text()", subject, Predicates.COMMON_NAME);
        
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
		
		String sciName = null;
		
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String subject = MappingUtils.getSubject();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY-NC");
        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            
            if(predicate.endsWith("hasCommonName")) {
                
                String commonName = (String) triple.getObject();
                if (commonName.contains("(")) {
                    Pattern sciNamePattern = Pattern.compile("(?:\\()([a-zA-Z ]{1,})(?:\\))");
                    Matcher m = sciNamePattern.matcher(commonName);
                    if (m.find()) {
                        sciName = m.group(1);
                    }
                    
                }
                
                commonName = commonName.replaceAll("\\([a-zA-Z ]{1,}\\)", "");
                commonName = commonName.replaceAll("[0-9]*", "");
                
                triple.setObject(commonName.trim());
                tmpTriple.add(triple);
            }
        }
        
        if (sciName != null && !"".equals(sciName)) {
            sciName = sciName.replaceAll("genus", "").trim();
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName.trim()));
        }
        
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
                
                triple.setObject(imageUrl);
                
//                imageUrl = URLEncoder.encode(imageUrl, "UTF-8");
                
                
                
                imageUrl = imageUrl.substring(0, imageUrl.lastIndexOf("/")+1) + URLEncoder.encode(imageUrl.substring(imageUrl.lastIndexOf("/")+1, imageUrl.length()), "UTF-8");
                imageUrl = imageUrl.replaceAll("\\+", "%20");
                
                System.out.println(imageUrl);
                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null){
                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Barry Armstead Photography http://barryarmsteadphotography.webs.com");
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Barry Armstead Photography");
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
	
}
