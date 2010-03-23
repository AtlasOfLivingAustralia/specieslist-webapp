/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: hwa002 $
 * @version $Id: FloraBaseHtmlDocumentMapper.java 1035 2009-08-04 11:35:39Z hwa002 $
 */

package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document Mapper for FloraBase - Western Australian Flora
 *
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class FloraBaseHtmlDocumentMapper extends XMLDocumentMapper {

	public FloraBaseHtmlDocumentMapper() {

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//html/head/title/text()", 
				subject, Predicates.DC_TITLE);

		// Needs to strip out the extra new line characters
		addTripleMapping("//html/body/div[@id=\"content\"]//span[@class=\"ref\"]/text()", 	
				subject, Predicates.REFERENCE);

		// common name.  Determines format and Needs to remove the leading `. `
		// Also remove trailing `. `
		// //html/body/div[@id="content"]/p[starts-with(., 'Common name')]/text()[1]
		// //html/body/div[@id="content"]//p/b[starts-with(text(), 'Common name')]/../text()
		addTripleMapping("//html/body/div[@id=\"content\"]/p/b[contains(text(), 'Common name')]/./../text()[2]", 	
				subject, Predicates.COMMON_NAME);

		addTripleMapping("//html/head/title/text()", 	
				subject, Predicates.SCIENTIFIC_NAME);

		// Needs to strip out the extra new line characters
		addTripleMapping("//html/body/div[@id=\"content\"]//span[@class=\"compiler\"]/text()", 	
				subject, Predicates.SCIENTIFIC_DESCRIPTION_AUTHOR);

		addTripleMapping("//html/body/div[@id=\"content\"]//acronym/@title", 	
				subject, Predicates.OCCURRENCES_IN_REGION);


		// Needs to remove extra end of line characters.
		addTripleMapping("//html/body/div[@id=\"content\"]//a[contains(@href, 'conservation')]/text()", 	
				subject, Predicates.CONSERVATION_STATUS);
		
		addTripleMapping("//html/body/div[@id=\"content\"]//strong[contains(text(), 'Name Status')]/../a[2]/text()", 	
				subject, Predicates.NAME_STATUS);
		
		addTripleMapping("//p[@id=\"photo\"]/img/attribute::src", 	
				subject, Predicates.IMAGE_URL);


	} // End of constructor.

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		
		List<String> imageUrls = new ArrayList<String>();
		
		String subject = triples.get(0).getSubject();
		String source = "http://florabase.dec.wa.gov.au";
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.substring(2, currentObj.length() - 1);
				if(! "".equals(newObj)) {
					triple.setObject(newObj.trim());
				} 
								
			} else if(predicate.endsWith("hasScientificName")) {
				String currentObj = (String) triple.getObject();
				int colIndex = currentObj.indexOf(":");
				if(colIndex>0) {
					triple.setObject(currentObj.substring(0,colIndex));
				} // else don't know, just remove.
			} else if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + imageUrl;
				imageUrls.add(imageUrl);
				
				//retrieve the image and create new parsed document
//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
//				if(imageDoc!=null){
//					pds.add(imageDoc);
//				}
				
				toRemove.add(triple);
			} 
		}
		
		triples.removeAll(toRemove);

		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
	
		//replace the list of triples
		pd.setTriples(triples);
		
		for (String imageUrl : imageUrls) {
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
				pds.add(imageDoc);
			}
		}
	}
} // End of `GenericXPathDocumentMapper` class.
