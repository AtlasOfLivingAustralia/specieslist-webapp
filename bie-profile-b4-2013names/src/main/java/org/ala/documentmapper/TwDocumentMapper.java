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
 * tumbi wetlands Document Mapper
 * 
 * @author Tommy Wang (twang.wang@csiro.au)
 */

public class TwDocumentMapper extends XMLDocumentMapper {

    /**
     * Initialise the mapper, adding new XPath expressions
     * for extracting content.
     */	

    public TwDocumentMapper() {

        setRecursiveValueExtraction(true);

        String subject = MappingUtils.getSubject();

        //set the content type this doc mapper handles
        this.contentType = MimeType.HTML.toString();

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
        addDCMapping("//center/text()",subject, Predicates.DC_TITLE);

        addTripleMapping("//center/text()", subject, Predicates.SCIENTIFIC_NAME);
        addTripleMapping("//center/big/text()", subject, Predicates.COMMON_NAME);
//        addTripleMapping("//td[@class='big']", subject, Predicates.DESCRIPTIVE_TEXT);
        addTripleMapping("//img/@src", subject, Predicates.IMAGE_URL);

    }

    /**
     * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
     */
    @Override
    protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

        String source = "http://www.tumbiwetlands.com.au/";
        boolean gotSciName = false;
        String family = null;

        ParsedDocument pd = pds.get(0);
        List<Triple<String,String,String>> triples = pd.getTriples();

        List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();

        String subject = MappingUtils.getSubject();

        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "CC-BY-NC-SA");

        triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
        
        for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            if(predicate.endsWith("hasImageUrl")) {
                String imageUrl = (String) triple.getObject();

                imageUrl = source + imageUrl;
                imageUrl = imageUrl.replaceAll(" ", "%20");
                triple.setObject(imageUrl);

                //retrieve the image and create new parsed document
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
                if(imageDoc!=null){
                    imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Ian Robb");
                    imageDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), "Ian Robb, www.tumbiwetlands.com.au");
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
