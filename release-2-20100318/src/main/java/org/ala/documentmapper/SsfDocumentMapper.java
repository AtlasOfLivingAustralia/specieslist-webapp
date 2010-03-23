/* *************************************************************************
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Sea Slug Forum (SSF) document mapper
 *
 * @see <a href="http://www.seaslugforum.net/">Sea Slug Forum</a>
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SsfDocumentMapper extends XMLDocumentMapper {
    /**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public SsfDocumentMapper() {
        //override the default content type
		this.contentType = MimeType.HTML.getMimeType();

		String subject = MappingUtils.getSubject();
		
        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
        		subject, Predicates.DC_IDENTIFIER);

        addDCMapping("//div[@id=\"content-main\"]/h1/em/text()",
				subject, Predicates.DC_TITLE);

		addTripleMapping("//div[@id=\"content-main\"]/h1/em/text()",
        		subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//div[@id=\"content-main\"]/h1/em/following-sibling::text()",
        		subject, Predicates.AUTHOR);

		addTripleMapping("//div[@class=\"imgright\"]/img/attribute::src|//img[@class=\"imgright\"]/attribute::src",
				subject, Predicates.IMAGE_URL);
        /*
         * <p class="taxa">
         *     <strong>Order:</strong> CEPHALASPIDEA<br>
		 *     <strong>Superfamily:</strong> ACTEONOIDEA<br>
		 *     <strong>Family:</strong> Acteonidae<br>
		 * </p>
         */

		addTripleMapping("//p[@class=\"taxa\"]/strong[contains(.,\"Order\")]/following-sibling::text()[1]",
        		subject, Predicates.ORDER);

		addTripleMapping("//p[@class=\"taxa\"]/strong[contains(.,\"Superfamily\")]/following-sibling::text()[1]",
        		subject, Predicates.SUPERFAMILY);

		addTripleMapping("//p[@class=\"taxa\"]/strong[contains(.,\"Family\")]/following-sibling::text()[1]",
        		subject, Predicates.FAMILY);

		addTripleMapping("//h4[@class=\"dist\"][contains(.,\"DISTRIBUTION\")]/following-sibling::p[1]//text()",
        		subject, Predicates.DISTRIBUTION_TEXT);

		addTripleMapping("//p[contains(.,\"Reference:\")]//text()",
        		subject, Predicates.PUBLISHED_IN); 
    }

    /**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
        ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
        String source = "http://www.seaslugforum.net";
        StringBuffer publishedInCitationText = new StringBuffer();

        List<Triple<String, String, String>> triplesToRemove = new ArrayList<Triple<String,String,String>>(); 
        
		for (Iterator<Triple<String,String,String>> iter = triples.iterator(); iter.hasNext(); ) {
			Triple<String,String,String> triple = iter.next();
            String predicate = triple.getPredicate().toString();

			if (predicate.equals(Predicates.IMAGE_URL.toString())) {
                // Fix relative URLs
                String imageUrl = source + (String) triple.getObject();
                triple.setObject(imageUrl);
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
                if(imageDoc!=null){
                	pds.add(imageDoc);
                }
                triplesToRemove.add(triple);
                
            } else if (predicate.equals(Predicates.PUBLISHED_IN.toString())) {
                String object = (String) triple.getObject();
                if (!object.contains("Reference:")) { 
                	publishedInCitationText.append(object + " ");
                }
                iter.remove();
            }
        }

		pd.getTriples().removeAll(triplesToRemove);
		
        String subject = triples.get(0).getSubject();
        // Add concenated hasDistributionText node to triples
        addNewTriple(subject, Predicates.PUBLISHED_IN.toString(), publishedInCitationText.toString(), triples);
        // Add hard-coded Kingdom Animalia as we know this is a Zoological resource
        addNewTriple(subject, Predicates.KINGDOM.toString(), "Animalia", triples);
    }

    /**
     * Add a new triple to the list of triples
     *
     * TODO: add throw exception instead of the if block?
     *
     * @param subject
     * @param predicate
     * @param object
     * @param triples
     */
    private void addNewTriple(String subject, String predicate, String object, List<Triple<String, String, String>> triples) {
        if (object != null && !object.isEmpty()) {
            object = object.replaceAll(" \\. ", ". ").trim(); // fix space before full stop
            object = object.replaceAll(" \\, ", ", ").trim(); // fix space before comma
            object = object.replaceAll("•", "").trim(); // remove bullet character
//            QName qn = new QName(Namespaces.ALA, predicate);
            Triple<String, String, String> trip2 = new Triple<String, String, String>(subject, predicate, object);
            triples.add(trip2);
        }
    }
}
