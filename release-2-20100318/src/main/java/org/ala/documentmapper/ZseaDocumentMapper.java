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
import org.w3c.dom.Document;

/**
 * Zooplankton of South Eastern Australia (ZSEA) Document Mapper
 * 
 * @see <a href="http://www.tafi.org.au/zooplankton/">Guide to the Marine Zooplankton of South Eastern Australia</a>
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class ZseaDocumentMapper extends XMLDocumentMapper {
    
	/**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public ZseaDocumentMapper() {
        //override the default content type
		this.contentType = "text/html";
		
		String subject = MappingUtils.getSubject();

        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
        		subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()",
				subject, Predicates.DC_TITLE); // TODO: use hasScientificName XPath intead?

        addTripleMapping("//span[@class=\"fs-head\"]/text()",
        		subject, Predicates.SCIENTIFIC_NAME);// <span class="fs-head">

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Phylum\")]]/td[2]/text()",
        		subject, Predicates.PHYLUM);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Subphylum\")]]/td[2]/text()",
        		subject, Predicates.SUB_PHYLUM);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Class\")]]/td[2]/text()",
        		subject, Predicates.CLASS);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Subclass\")]]/td[2]/text()",
        		subject, Predicates.SUBCLASS);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Order\")]]/td[2]/text()",
        		subject, Predicates.ORDER);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Family\")]]/td[2]/text()",
        		subject, Predicates.FAMILY);

        addTripleMapping("//table[@class=\"taxonomytable\"]/tbody[1]/tr[child::td/child::strong[contains(.,\"Genus\")]]/td[2]/text()",
        		subject, Predicates.GENUS);
        
        addTripleMapping("//a[@id=\"fsimg\"]/attribute::href",
        		subject, Predicates.IMAGE_URL);

        addTripleMapping("//span[@class=\"fs-subhead\"][contains(.,\"Size\")]/following::ul[1]/li[1]/text()",
        		subject, Predicates.MORPHOLOGICAL_TEXT);
        
        addTripleMapping("//span[@class=\"fs-subhead\"][contains(.,\"Distinguishing characteristics\")]/following::ul[1]/li//text()",
        		subject, Predicates.MORPHOLOGICAL_TEXT);

        addTripleMapping("//span[@class=\"fs-subhead\"][contains(.,\"Distribution\")]/following::ul[1]/li//text()",
        		subject, Predicates.DISTRIBUTION_TEXT);
        
        addTripleMapping("//span[@class=\"fs-subhead\"][contains(.,\"Ecology\")]/following::ul[1]//text()",
        		subject, Predicates.ECOLOGICAL_TEXT);
    }

    /**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
        ParsedDocument pd = pds.get(0);
        
        String urlForPage = pd.getGuid();
        
		List<Triple<String,String,String>> triples = pd.getTriples();
        StringBuffer hasMorphologicalTextAll = new StringBuffer();
        StringBuffer hasDistributionTextAll = new StringBuffer();
        StringBuffer hasEcologicalTextAll = new StringBuffer();

		for (Iterator<Triple<String,String,String>> iter = triples.iterator(); iter.hasNext(); ) {
			Triple<String,String,String> triple = iter.next();
            String predicate = triple.getPredicate().toString();
            
			if (predicate.equals(Predicates.DC_TITLE.toString())) {
                // remove text after the first "-"
                String titleText = (String) triple.getObject();
                String trimmedTitleText = titleText.split("-")[0].trim();
                triple.setObject(trimmedTitleText);
            } else if (predicate.equals(Predicates.IMAGE_URL.toString())) {
                // images URLs are relative, so we have to add the individual page URLs to the beginning
                String imageUrl = (String) triple.getObject();
                String pageUrlDirectory = urlForPage.replaceAll("\\/[a-zA-Z0-9\\-_]*\\.html", "/");
                triple.setObject(pageUrlDirectory + imageUrl);
            } else if (predicate.equals(Predicates.MORPHOLOGICAL_TEXT.toString())) {
                // Concatenate all instances of hasMorphologicalText into a single triple
                hasMorphologicalTextAll.append((String) triple.getObject() + " ");
                iter.remove();
            } else if (predicate.equals(Predicates.DISTRIBUTION_TEXT.toString())) {
                // Concatenate all instances of hasDistributionText into a single triple
                hasDistributionTextAll.append((String) triple.getObject() + " ");
                iter.remove();
            } else if (predicate.equals(Predicates.ECOLOGICAL_TEXT.toString())) {
                // Concatenate all instances of hasEcologicalText into a single triple
                hasEcologicalTextAll.append((String) triple.getObject() + " ");
                iter.remove();
            }
        }

        String subject = triples.get(0).getSubject();
        // Add concenated hasMorphologicalText node to triples
        addNewTriple(subject, Predicates.MORPHOLOGICAL_TEXT.toString(), hasMorphologicalTextAll.toString(), triples);
        // Add concenated hasDistributionText node to triples
        addNewTriple(subject, Predicates.DISTRIBUTION_TEXT.toString(), hasDistributionTextAll.toString(), triples);
        // Add concenated hasDistributionText node to triples
        addNewTriple(subject, Predicates.ECOLOGICAL_TEXT.toString(), hasEcologicalTextAll.toString(), triples);
        // Add hard-coded Kingdom Animalia as we know this is a Zoological resource
        addNewTriple(subject, Predicates.KINGDOM.toString(), "Animalia", triples);
        //pd.setTriples(triples);
    }

    /**
     * Add a new triple to the list of triples
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
            Triple<String, String, String> trip2 = new Triple<String, String, String>(subject, predicate, object);
            triples.add(trip2);
        }
    }
}
