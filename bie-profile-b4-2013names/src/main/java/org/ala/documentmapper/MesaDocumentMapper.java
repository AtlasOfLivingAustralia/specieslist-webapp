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
 * Document mapper for Marine Education Society of Australasia
 *
 * @author Tommy Wang (Tommy.Wang@csiro.au)
 */
public class MesaDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */
    public MesaDocumentMapper() {

        setRecursiveValueExtraction(true);

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        //set an initial subject
        String subject = MappingUtils.getSubject();

        // subject, predicate namespace, predicate, 
        //addDCMapping("//a[@id=\"top\"]/text()", subject, Predicates.DC_TITLE);

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//span[contains(.,\"Scientific name\")]/following-sibling::em[1]/text()" +
                "|//p[@class=\"bsh\"]/text()" +
                "|//p[@class=\"bssh\"]/text()",subject, Predicates.DC_TITLE);
        addTripleMapping("//span[contains(.,\"Scientific name\")]/following-sibling::em[1]/text()|//em/text()", subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//p[@class=\"bsh\"]/text()" +
                "|//p[@class=\"bssh\"]/text()", subject, Predicates.COMMON_NAME);
        addTripleMapping("//span[contains(.,\"Family\")]/following-sibling::text()[1]", subject, Predicates.FAMILY);
        addTripleMapping("//span[contains(.,\"Phylum\")]/following-sibling::text()[1]", subject, Predicates.PHYLUM);
        addTripleMapping("//span[contains(.,\"Class\")]/following-sibling::text()[1]", subject, Predicates.CLASS);
        addTripleMapping("//span[contains(.,\"Distribution\")]/following-sibling::text()[1]", subject, Predicates.DISTRIBUTION_TEXT);
        addTripleMapping("//span[contains(.,\"Description\")]/following-sibling::text()[1]" +
                "|//span[contains(.,\"Interesting facts\")]/following-sibling::text()[1]" +
                "|//span[contains(.,\"Interesting Facts\")]/following-sibling::text()[1]", subject, Predicates.DESCRIPTIVE_TEXT);
        addTripleMapping("//span[contains(.,\"Ecology\")]/following-sibling::text()[1]", subject, Predicates.ECOLOGICAL_TEXT);
        addTripleMapping("//span[contains(.,\"Status\")]/following-sibling::text()[1]", subject, Predicates.CONSERVATION_STATUS);
        addTripleMapping("//span[contains(.,\"Habitat\")]/following-sibling::text()[1]", subject, Predicates.HABITAT_TEXT);

        addTripleMapping("//td[@align=\"center\"]//img/@src", subject, Predicates.IMAGE_URL);

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

        documentStr = documentStr.replaceAll("<![\\-a-zA-Z ]{1,}>", "");
        //		documentStr = documentStr.replaceAll("</p>[\\s]*<p>", "");
        //		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
        //		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");

        //		System.out.println(documentStr);

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
        String source = "http://www.mesa.edu.au/AtoZ/";

        String subject = MappingUtils.getSubject();
        boolean gotDescription = false;
        boolean gotSciName = false;
        String phylum = null;
        String family = null;
        String klass = null;        

        ParsedDocument pd = pds.get(0);
        List<Triple<String,String,String>> triples = pd.getTriples();
        List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

        List<String> imageUrls = new ArrayList<String>();

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY (Creative Commons Attribution)");
        pd.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "© MESA");

        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));

        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();

            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();

                imageUrls.add(imageUrl);

                tmpTriple.add(triple);
                //                imageUrl = source + imageUrl;
                //                triple.setObject(imageUrl);
                //                
                //                //retrieve the image and create new parsed document
                //                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                //                if(imageDoc!=null){
                //                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "MESA");
                //                    pds.add(imageDoc);
                //                }
            } else if (predicate.endsWith("hasDescriptiveText")) {
                gotDescription = true;
            } else if (predicate.endsWith("hasScientificName")) {
                gotSciName = true;
            } else if (predicate.endsWith("hasPhylum")) {
                phylum = (String) triple.getObject();
            } else if (predicate.endsWith("hasClass")) {
                klass = (String) triple.getObject();
            } else if (predicate.endsWith("hasFamily")) {
                family = (String) triple.getObject();
            }
        }

        if (!gotDescription) {
            String description = getXPathSingleValue(xmlDocument, "//em/parent::p/following-sibling::p");

            if (description != null && !"".equals(description)) {
                triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
            } else {
                description = getXPathSingleValue(xmlDocument, "//span[@class=\"rssh\"]/parent::p/following-sibling::p");

                if (description != null && !"".equals(description)) {
                    triples.add(new Triple(subject, Predicates.DESCRIPTIVE_TEXT.toString(), description));
                }
            }
        }

        if (!gotSciName) {
            if (family != null) {
                triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), family));
                gotSciName = true;
            } else if (klass != null) {
                triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), klass));
                gotSciName = true;
            } else if (phylum != null) {
                triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), phylum));
                gotSciName = true;
            }
        }

        for (String imageUrl : imageUrls) {

            String tmp = getXPathSingleValue(xmlDocument, "//td[@align=\"center\"]//img[@src=\"" + imageUrl + "\"]/following-sibling::span[1]");
            //            System.out.println(tmp);

            String right = "© MESA";
            String creator = "MESA";

            if (tmp != null && tmp.contains("Flickr")) {
                continue;
            } else {
                if (tmp != null && tmp.split(":").length == 2) {
                    creator = tmp.split(":")[1];
                }
                imageUrl = source + imageUrl;

                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
                if(imageDoc!=null){
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator.trim());

                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), right.trim());
                    pds.add(imageDoc);
                }
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
