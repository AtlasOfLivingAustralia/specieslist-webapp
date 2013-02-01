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
 * Document mapper for Aus Wild Life
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class AuswildlifeDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */
    public AuswildlifeDocumentMapper() {

        setRecursiveValueExtraction(true);

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        //set an initial subject
        String subject = MappingUtils.getSubject();

        // subject, predicate namespace, predicate, 
        //addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        //        addDCMapping("//td[@class=\"title\"]/text()",subject, Predicates.DC_TITLE);
        //        addTripleMapping("//td[@class=\"title\"]/text()", subject, Predicates.SCIENTIFIC_NAME);
        //        addTripleMapping("//td[@class=\"title\"]/text()", subject, Predicates.GENUS);
        //        addTripleMapping("//div[@id='slideshow']/span/a/img/@src", subject, Predicates.IMAGE_URL);

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

        documentStr = documentStr.replaceAll("\\]\\]>", "");
        documentStr = documentStr.replaceAll("<!\\[CDATA\\[", "");
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
        String source = "http://www.auswildlife.com";

        String titleStr = null; 
        String subject = MappingUtils.getSubject();

        ParsedDocument pd = pds.get(0);
        List<Triple<String,String,String>> triples = pd.getTriples();
        List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
        
        String counter = pd.getGuid().split("#")[1];
        
        System.out.println("!!!" + counter);

        String tmpStr = getXPathSingleValue(xmlDocument, "//ul[@class='thumbs']/li[" + counter + "]//div[@class='image-title']/span/text()");
        String imageUrl = getXPathSingleValue(xmlDocument, "//a[@title=\"" + tmpStr + "\"]/@href");

        String[] tmpStrArray = tmpStr.split(",");

        String sciName = null;
        String commonName = null;

        if (tmpStrArray.length == 3) {
            commonName = tmpStrArray[0];
            sciName = tmpStrArray[1];
        }

        if (commonName != null && !"".equals(commonName)) {
            triples.add(new Triple(subject, Predicates.COMMON_NAME.toString(), commonName));
        }

        if (sciName != null && !"".equals(sciName)) {
            if (sciName.contains(":")) {
                sciName = sciName.split(":")[1];
            }
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), sciName));
            pd.getDublinCore().put(Predicates.DC_TITLE.toString(), sciName);
        }

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "Attribution-Noncommercial – CC BY-NC");
        pd.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Photo: Bruce Thomson, http://www.auswildlife.com");
        pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Bruce Thomson");

        if (imageUrl != null && !"".equals(imageUrl)) {
            imageUrl = source + imageUrl;

            triples.add(new Triple(subject, Predicates.IMAGE_URL.toString(), imageUrl));

            ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
            if(imageDoc!=null){
                imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Photo: Bruce Thomson, http://www.auswildlife.com");
                imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Bruce Thomson");
                pds.add(imageDoc);
            }
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
