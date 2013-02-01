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

import java.net.URLEncoder;
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
 * Document mapper for Girraween National Park
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class GnpDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */
    public GnpDocumentMapper() {

        setRecursiveValueExtraction(true);

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        //set an initial subject
        String subject = MappingUtils.getSubject();

        // subject, predicate namespace, predicate, 
        //addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//title/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//table[@class=\"ns-girraween-content\"]//img[@class='ns-girraween-photo']/@src", subject, Predicates.IMAGE_URL);

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

        String baseUrl = "http://www.rymich.com";

        List<Triple<String,String,String>> triples = pd.getTriples();
        List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

        String subject = MappingUtils.getSubject();

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC BY-NC-SA");
        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

        //        for (Triple<String,String,String> triple: triples) {
        //            String predicate = triple.getPredicate().toString();
        //            
        //            if(predicate.endsWith("hasScientificName")) {
        //                String sciName = (String) triple.getObject();
        //                if (sciName.contains(",")) {
        //                    sciName = sciName.replaceAll(",", "");
        //                }
        //                
        //                triple.setObject(sciName.trim());
        //            }
        //        }

        String commonName = pd.getDublinCore().get(Predicates.DC_TITLE.toString());
        if (commonName.contains("-")) {
            String [] tmp = commonName.split("\\-");
            commonName = tmp[tmp.length-1];

            triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName));
            
        }
        
        String tmp = pd.getGuid();
        
        if (tmp.contains("&page=")) {
            tmp = tmp.split("\\&page=")[0];
            tmp = tmp.split("=")[tmp.split("=").length-1];
            tmp = tmp.replaceAll("_", " ");
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), tmp));
            pd.getDublinCore().put(Predicates.DC_TITLE.toString(), tmp);
        }



        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();

            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();
                String copyright = getXPathSingleValue(xmlDocument, "//img[@src='" + imageUrl + "']/following-sibling::a[1]/text()" +
                        "|//img[@src='" + imageUrl + "']/following-sibling::span[1]/text()");

                if (copyright == null || (!URLEncoder.encode(copyright, "UTF8").contains("%C2%A9") || copyright.contains("Girraween National Park"))) {
                    System.out.println(copyright);
                    imageUrl = baseUrl + imageUrl;

                    triple.setObject(imageUrl);

                    //retrieve the image and create new parsed document
                    ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                    if(imageDoc!=null){
                        imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Vanessa and Chris Ryan http://www.rymich.com/girraween/");
                        imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Vanessa and Chris Ryan");
                        pds.add(imageDoc);
                    }
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
