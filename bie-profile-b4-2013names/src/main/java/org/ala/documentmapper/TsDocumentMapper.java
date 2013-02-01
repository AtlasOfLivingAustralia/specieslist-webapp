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
 * Tasmanian Spiders Document Mapper
 * 
 * @author Tommy Wang (twang.wang@csiro.au)
 */

public class TsDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */	

    public TsDocumentMapper() {

        setRecursiveValueExtraction(true);

        String subject = MappingUtils.getSubject();

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//p[strong[contains(.,'Species')]]/following-sibling::p[1]/text()",subject, Predicates.DC_TITLE);

        addTripleMapping("//p[strong[contains(.,'Species')]]/following-sibling::p[1]/text()", subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//p[strong[contains(.,'Family')]]/following-sibling::p[1]/text()", subject, Predicates.FAMILY);
        addTripleMapping("//p[strong[contains(.,'Body Length')]]/following-sibling::p[1]", subject, Predicates.MORPHOLOGICAL_TEXT);
        addTripleMapping("//p[strong[contains(.,'Habitat')]]/following-sibling::p[1]", subject, Predicates.HABITAT_TEXT);
        addTripleMapping("//p[strong[contains(.,'Toxicity')]]/following-sibling::p[1]", subject, Predicates.DESCRIPTIVE_TEXT);
        addTripleMapping("//div[@align='center']//img/attribute::src", subject, Predicates.IMAGE_URL);

    }

    /**
     * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
     */
    @Override
    protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

        String source = "http://www.tasmanianspiders.info/";
        boolean gotSciName = false;
        String family = null;

        ParsedDocument pd = pds.get(0);
        List<Triple<String,String,String>> triples = pd.getTriples();

        List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();

        String subject = MappingUtils.getSubject();

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY");

        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
        
        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            if(predicate.endsWith("hasScientificName")) {
                gotSciName = true;
            } else if (predicate.endsWith("hasFamily")) {
                family = (String) triple.getObject();
            }
        }
        
        if (!gotSciName && family != null && !"".equals(family)) {
            triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), family));
        }

        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();

                boolean noCredit = true;
                String creator = getXPathSingleValue(xmlDocument, "//p[a[img[@src=\"" + imageUrl + "\"]]]/following-sibling::p[1]//text()|//p[img[@src=\"" + imageUrl + "\"]]/following-sibling::p[1]//text()");

                if (creator != null && !"".equals(creator)) {
                    noCredit = false;
                }
                
                imageUrl = source + imageUrl;
                imageUrl = imageUrl.replaceAll(" ", "%20");
                triple.setObject(imageUrl);

                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null && noCredit){
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "John Douglas");
                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "John Douglas, tasmanian spiders.info");
                    pds.add(imageDoc);
                }


                toRemove.add(triple);
            } 
        }



        //remove the triple from the triples
        triples.removeAll(toRemove);

        //replace the list of triples
        pd.setTriples(triples);
    }

}
