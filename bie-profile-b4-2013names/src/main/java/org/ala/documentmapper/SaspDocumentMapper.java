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
 * Document mapper for Steve's Australian Spider Pics
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class SaspDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */
    public SaspDocumentMapper() {

        setRecursiveValueExtraction(true);

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        //set an initial subject
        String subject = MappingUtils.getSubject();

        // subject, predicate namespace, predicate, 
        //addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//td[font[contains(.,'Genus/Species')]]/following-sibling::td[1]//text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//img/@src", subject, Predicates.IMAGE_URL);
        addTripleMapping("//td[font[contains(.,'Genus/Species')]]/following-sibling::td[1]//text()|//h1[1]//i/text()", subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//td[font[contains(.,'Order')]]/following-sibling::td[1]//text()", subject, Predicates.ORDER);
        addTripleMapping("//td[font[contains(.,'Family')]]/following-sibling::td[1]//text()", subject, Predicates.FAMILY);
        addTripleMapping("//td[font[contains(.,'Common Name')]]/following-sibling::td[1]//text()|//h1[1]/text()", subject, Predicates.COMMON_NAME);

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

        //		List<ParsedDocument> pds = new ArrayList<ParsedDocument>();
        //		
        ////		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
        ////		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
        ////		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
        ////		documentStr = documentStr.replaceAll("</p>[\\s]*<p>", "");
        //		
        ////		System.out.println(documentStr);
        //		
        //		String[] documentStrArray = documentRawStr.split("<hr/>");
        //		
        //		if (documentStrArray.length > 1) {
        //		    for (int i = 1; i < documentStrArray.length - 1; i ++) {
        ////		        System.out.println(documentStrArray[i]);
        //		        
        //		        String documentStr = "<div>" + documentStrArray[i] + "</div>";
        //		        
        //		        byte[] tmpContent = documentStr.getBytes();
        //		        
        //		        pds.addAll(super.map(uri, tmpContent));
        //		    }
        //		}

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

        List<Triple<String,String,String>> triples = pd.getTriples();
        List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

        List<String> imageUrlList = new ArrayList<String>();

        String subject = MappingUtils.getSubject();
        String baseUrl = "http://spiders.zacharoo.com/";

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY-NC");
        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));


        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();

            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
                //                imageUrl = imageUrl.replaceAll(" ", "%20");

                if (imageUrl.endsWith(".jpg")) {
                    imageUrl = baseUrl + imageUrl;

                    imageUrlList.add(imageUrl);

                    triple.setObject(imageUrl);
                } else {
                    tmpTriple.add(triple);
                }
            }

        }

        for (String imageUrl : imageUrlList) {
            //retrieve the image and create new parsed document
            ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
            if(imageDoc!=null){
                imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Steve's Australian Spider Pics http://spiders.zacharoo.com/");
                imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Steve Clark");
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
