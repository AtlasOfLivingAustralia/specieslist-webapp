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
 * Australasian Bird Image Database document mapper
 *
 * @author "Tommy Wang <Tommy.Wang@csiro.au>"
 */
public class AbidDocumentMapper extends XMLDocumentMapper {
    /**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public AbidDocumentMapper() {
        //override the default content type
		this.contentType = MimeType.HTML.toString();

		String subject = MappingUtils.getSubject();
		
        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
        		subject, Predicates.DC_IDENTIFIER);

        addDCMapping("//td[contains(.,\"Bird Name\")]/following-sibling::td[1]//text()", subject, Predicates.DC_TITLE);
        
        addDCMapping("//td[contains(.,\"Photographer\")]/following-sibling::td[1]//text()", subject, Predicates.DC_CREATOR);
        
        addDCMapping("//td[contains(.,\"Location\")]/following-sibling::td[1]//text()", subject, Predicates.LOCALITY);
        
        addTripleMapping("//td[contains(.,\"Bird Name\")]/following-sibling::td[1]//text()",
        		 subject, Predicates.SCIENTIFIC_NAME);
        
        addTripleMapping("//td[contains(.,\"Bird Name\")]/following-sibling::td[1]//text()",
       		 subject, Predicates.COMMON_NAME);
        
        addTripleMapping("//td[contains(.,\"Bird Family\")]/following-sibling::td[1]//text()",
          		 subject, Predicates.FAMILY);

        addTripleMapping("//td[@class=\"group\"]/img/attribute::src",
        		subject, Predicates.IMAGE_URL);

    }

    /**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
        ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
        String source = "http://www.aviceda.org/abid/";

		for (Iterator<Triple<String,String,String>> iter = triples.iterator(); iter.hasNext(); ) {
			Triple<String,String,String> triple = iter.next();
            String predicate = triple.getPredicate().toString();

			if (predicate.equals(Predicates.SCIENTIFIC_NAME.toString())) {
                // remove text after the first "-"
                String currentObj = (String) triple.getObject();
                String newObj = currentObj.split(" \\-")[0].trim();
                triple.setObject(newObj);
            } else if (predicate.equals(Predicates.COMMON_NAME.toString())) {
                // remove text after the first "-"
                String currentObj = (String) triple.getObject();
                String newObj = currentObj.split(" \\-")[1].trim();
                triple.setObject(newObj);
            } else if (predicate.equals(Predicates.FAMILY.toString())) {
                // remove text after the first "-"
                String currentObj = (String) triple.getObject();
                String newObj = currentObj.split(" \\-")[0].trim();
                triple.setObject(newObj);
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
        String subject = MappingUtils.getSubject();
        Triple<String,String,String> newTriple = new Triple<String,String,String>(subject, Predicates.KINGDOM.toString(), "Animalia");
        triples.add(newTriple);
    }
}
