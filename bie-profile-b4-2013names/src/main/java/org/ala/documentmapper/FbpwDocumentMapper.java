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
 * Document mapper for Friends of Bushy Park Wetlands
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class FbpwDocumentMapper extends XMLDocumentMapper {

	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public FbpwDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		//set the content type this doc mapper handles
		this.contentType = MimeType.HTML.toString();
		
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		// subject, predicate namespace, predicate, 
		//addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//font[b[contains(.,'Description:')]]/following-sibling::i[1]/text()" +
                "|//p[@align='center']/font[@color='red']/b/i/text()" +
                "|//font[b[contains(.,'Description:')]]/following-sibling::font/i/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//font[b[contains(.,'Description:')]]/parent::p", subject, Predicates.DESCRIPTIVE_TEXT);
        addTripleMapping("//font[b[contains(.,'Habitat:')]]/parent::p", subject, Predicates.HABITAT_TEXT);
        addTripleMapping("//font[b[contains(.,'Food:')]]/parent::p", subject, Predicates.DIET_TEXT);
        addTripleMapping("//font[b[contains(.,'Breeding:')]]/parent::p", subject, Predicates.REPRODUCTION_TEXT);
        addTripleMapping("//font[b[contains(.,'Length:')]]/parent::p", subject, Predicates.MORPHOLOGICAL_TEXT);
        addTripleMapping("//font[b[contains(.,'Distribution:')]]/parent::p", subject, Predicates.DISTRIBUTION_TEXT);
        addTripleMapping("//font[b[contains(.,'Class:')]]/following-sibling::i[1]/text()", subject, Predicates.CLASS);
        addTripleMapping("//font[b[contains(.,'Class:')]]/following-sibling::i[2]/text()", subject, Predicates.ORDER);
        addTripleMapping("//font[b[contains(.,'Class:')]]/following-sibling::i[3]/text()", subject, Predicates.FAMILY);
        addTripleMapping("//font[b[contains(.,'Class:')]]/following-sibling::i[4]/text()", subject, Predicates.GENUS);
        addTripleMapping("//font[@color='purple']/b/text()" +
        		"|//p[@align='center']/font[@color='red']/b/text()", subject, Predicates.COMMON_NAME);
        addTripleMapping("//font[b[contains(.,'Description:')]]/following-sibling::i[1]/text()" +
        		"|//p[@align='center']/font[@color='red']/b/i/text()" +
        		"|//font[b[contains(.,'Description:')]]/following-sibling::font/i/text()", subject, Predicates.SCIENTIFIC_NAME);
        
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
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-By");
		pd.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Trevor Hudson, Friends of Bushy Park Wetlands http://home.vicnet.net.au/~fbpw/");
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
        
//		for (Triple<String,String,String> triple: triples) {
//			String predicate = triple.getPredicate().toString();
//			
//			if(predicate.endsWith("hasImageUrl")) {
//                String imageUrl = (String) triple.getObject();
//                imageUrl = baseUrl + imageUrl;
//                
//                triple.setObject(imageUrl);
//                
//                //retrieve the image and create new parsed document
//                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
//                if(imageDoc!=null){
//                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Photo: Steve Dew, www.aussiecreatures.net");
//                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Steve Dew");
//                    pds.add(imageDoc);
//                }
//                
//                tmpTriple.add(triple);
//            }
//		}

//		triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), titleStr));
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
	}
	
}
