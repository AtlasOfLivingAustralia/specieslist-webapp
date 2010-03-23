/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.documentmapper;

import java.util.Iterator;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * World Thysanoptera, Australasia (WTA) document mapper
 *
 * @see <a href="http://anic.ento.csiro.au/thrips/index.html">World Thysanoptera</a>
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class WtaDocumentMapper extends XMLDocumentMapper {
    /**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public WtaDocumentMapper() {
        //override the default content type
		this.contentType = MimeType.HTML.toString();

		String subject = MappingUtils.getSubject();
		
        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
        		subject, Predicates.DC_IDENTIFIER);

        addDCMapping("//title/text()", subject, Predicates.DC_TITLE);
        
        addTripleMapping("//div[@class=\"content plus-images\"]/h1/em/text()",
        		 subject, Predicates.SCIENTIFIC_NAME);

        // hasSynonym XPATH not working reliably
        // addDefaultSubjectMapping("//h3[contains(.,\"Original name and synonyms\")]/following-sibling::ul[1]/li[1]//text()",
        //        TripleConstants.ALA_NAMESPACE, "hasSynonyms", false);
        
        addTripleMapping("//h2[contains(.,\"Distribution data\")]/following-sibling::p[1]/text()",
        		subject, Predicates.DISTRIBUTION_TEXT);

        addTripleMapping("//h2[contains(.,\"Recognition data\")]/following-sibling::p[1]/text()",
        		subject, Predicates.MORPHOLOGICAL_TEXT);
        
        addTripleMapping("//div[@id=\"imgContainer\"]/ul/li[1]/a/attribute::href",
        		subject, Predicates.IMAGE_URL);

    }

    /**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
        ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
        String source = "http://anic.ento.csiro.au/thrips/identifying_thrips/";

		for (Iterator<Triple<String,String,String>> iter = triples.iterator(); iter.hasNext(); ) {
			Triple<String,String,String> triple = iter.next();
            String predicate = triple.getPredicate().toString();

			if (predicate.equals(Predicates.DC_TITLE.toString())) {
                // remove text after the first "-"
                String titleText = (String) triple.getObject();
                String trimmedTitleText = titleText.split(" \\|")[0].trim();
                triple.setObject(trimmedTitleText);
            } else if (predicate.equals(Predicates.IMAGE_URL.toString())) {
                // Fix relative URLs
                String imageUrl = source + (String) triple.getObject();
                triple.setObject(imageUrl);
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
                if(imageDoc!=null){
                	pds.add(imageDoc);
                }
            }
        }

        // Add hard-coded Kingdom Animalia as we know this is a Zoological resource
        String subject = triples.get(0).getSubject();
        Triple<String,String,String> newTriple = new Triple<String,String,String>(subject, Predicates.KINGDOM.toString(), "Animalia");
        triples.add(newTriple);
    }
}
