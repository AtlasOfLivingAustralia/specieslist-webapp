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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.MimeType;
import org.w3c.dom.Document;

/**
 * Global Invasive Species Database document mapper
 *
 * @author "Tommy Wang <Tommy.Wang@csiro.au>"
 */
public class IssgDocumentMapper extends XMLDocumentMapper {
    /**
	 * Initialise the mapper, adding new XPath expressions
	 * for extracting content.
	 */
	public IssgDocumentMapper() {
        //override the default content type
		this.contentType = MimeType.HTML.toString();
		
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
        addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
        		subject, Predicates.DC_IDENTIFIER);

        addDCMapping("//span[@class=\"SpeciesName\"]/text()", subject, Predicates.DC_TITLE);
        
        addTripleMapping("//span[@class=\"SpeciesName\"]/text()",
        		 subject, Predicates.SCIENTIFIC_NAME);
        
        addTripleMapping("//b[contains(.,\"Synonyms\")]/following-sibling::text()[1]",
       		 subject, Predicates.SYNONYM);
        
        addTripleMapping("//b[contains(.,\"Common names\")]/following-sibling::text()[1]",
          		 subject, Predicates.COMMON_NAME);
        
        addTripleMapping("//span[@class=\"ListNote\"]/text()" +
        		"|//b[contains(.,\"Description\")]/following-sibling::text()[1]",
         		 subject, Predicates.DESCRIPTIVE_TEXT);
        
        addTripleMapping("//b[contains(.,\"Occurs in\")]/following-sibling::text()[1]" +
        		"|//b[contains(.,\"Habitat description\")]/following-sibling::text()[1]",
         		 subject, Predicates.HABITAT_TEXT);
        
        addTripleMapping("//b[contains(.,\"Geographical range\")]/following-sibling::text()[1]",
         		 subject, Predicates.DISTRIBUTION_TEXT);
        
        addTripleMapping("//b[contains(.,\"Reproduction\")]/following-sibling::text()[1]",
        		 subject, Predicates.REPRODUCTION_TEXT);
        
        addTripleMapping("//img[@class=\"SpeciesImage\"]/attribute::src",
          		 subject, Predicates.IMAGE_URL);

        
    }
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
	throws Exception {

		String documentStr = new String(content);
		
		// clean up the pages		
		documentStr = documentStr.replaceAll("<!\\-\\-", "");
		documentStr = documentStr.replaceAll("\\-\\->", "");
		documentStr = documentStr.replaceAll("\\-\\-", "");
		documentStr = documentStr.replaceAll("<blockquote>", "");
		documentStr = documentStr.replaceAll("</blockquote>", "");
		documentStr = documentStr.replaceAll("<br/>", "");
		documentStr = documentStr.replaceAll("<i>", "");
		documentStr = documentStr.replaceAll("</i>", "");
		documentStr = documentStr.replaceAll("<u>", "");
		documentStr = documentStr.replaceAll("</u>", "");
//		documentStr = documentStr.replaceAll("'[a-zA-Z \\)\\-]{1,}Click for full size'", "'");
		
//		System.out.println(documentStr);

		content = documentStr.getBytes();

		return super.map(uri, content);
	}

    /**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
        ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		List<Triple<String,String,String>> toAdd = new ArrayList<Triple<String,String,String>>();
		
        String source = "http://www.issg.org/database";
        String subject = MappingUtils.getSubject();
        
        pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.issg.org/database/welcome/disclaimer.asp");

		for (Iterator<Triple<String,String,String>> iter = triples.iterator(); iter.hasNext(); ) {
			Triple<String,String,String> triple = iter.next();
            String predicate = triple.getPredicate().toString();

			if (predicate.equals(Predicates.IMAGE_URL.toString())) {
                // Fix relative URLs
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.substring(2);
				newObj = newObj.replaceAll("\\.gif", "\\.jpg");
				newObj = newObj.replaceAll("_thumbnail", "");
				
                String imageUrl = source + newObj;
                triple.setObject(imageUrl);
                ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
                if(imageDoc!=null){
                	pds.add(imageDoc);
                }
            } else if (predicate.equals(Predicates.COMMON_NAME.toString())) {
                // Fix relative URLs
                String currentObj =(String) triple.getObject();
                String[] commonNames = currentObj.split(",");
                
                for (String commonName : commonNames) {
                    
                    if (commonName.contains("(") && !commonName.toLowerCase().contains("(english")) {
                        continue;
                    }
                    
                    commonName = commonName.replaceAll("\\([a-zA-Z \\-]*\\)", "").trim();
                    
                	Triple<String,String,String> tmpTriple = new Triple<String,String,String>(subject, Predicates.COMMON_NAME.toString(), commonName.trim());
                	toAdd.add(tmpTriple);
                }
                
                toRemove.add(triple);
            } else if (predicate.equals(Predicates.SYNONYM.toString())) {
                // Fix relative URLs
                String currentObj =(String) triple.getObject();
                String[] synonyms = currentObj.split(",");
                
                for (String synonym : synonyms) {
                	Triple<String,String,String> tmpTriple = new Triple<String,String,String>(subject, Predicates.SYNONYM.toString(), synonym.trim());
                	toAdd.add(tmpTriple);
                }
                
                toRemove.add(triple);
            }
        }

        Triple<String,String,String> newTriple = new Triple<String,String,String>(subject, Predicates.KINGDOM.toString(), "Plantae");
        triples.add(newTriple);
        triples.addAll(toAdd);
        triples.removeAll(toRemove);        
    }
}
