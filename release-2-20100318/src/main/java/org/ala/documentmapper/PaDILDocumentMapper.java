/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: jia020 $
 * @version $Id: AfdPubDocumentMapper.java 350 2009-05-27 01:20:13Z jia020 $
 */
package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document Mapper for Pest and Diseases Image Library.
 */
public class PaDILDocumentMapper extends XMLDocumentMapper {

	public static final String SCIENTIFIC_NAME_PATTERN = "a-zA-ZÏËÖÜÄÉÈČÁÀÆŒâïëêöüäåéèčáàæœóú\\.\\-`'%\\(\\), ;:&#0-9°/=\"";

	public PaDILDocumentMapper() {
		
		setRecursiveValueExtraction(true);
		
		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);

		// === Obtains the Canonical Scientific Name ===

		addDCMapping("//div[@class=\"headingSpeciesDetail\"]/em/text()", subject,
				Predicates.DC_TITLE);

		
		// === Obtains the Common Name ===
		addTripleMapping("//div[@class=\"headingSpeciesDetail\"]/text()[1]", subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//div[@class=\"headingSpeciesDetail\"]/em/text()", subject,
				Predicates.SCIENTIFIC_NAME);
		
		// === Obtains the Diagnostic Characters ===

		addTripleMapping("//dl[3]/dd[1]", subject, Predicates.DESCRIPTIVE_TEXT);

		// === Obtains the Citation for Species ===

		addTripleMapping("//span[@id=\"lblCitation\"]",subject, Predicates.CITATION);

		// === Obtains the Status of the Species Characters ===

		addTripleMapping("//div[@class=\"headingSpeciesStatus\"]", subject, Predicates.PEST_STATUS);
		
		addTripleMapping("//div[@class=\"image\"]/dl[1]/dd[1]/img/attribute::src", subject, Predicates.IMAGE_URL);

		addTripleMapping("//dt[contains(.,\"Distribution - Regions\")]/following-sibling::dd[1]//text()", subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("(?:<dt>[\\s]{0,}Distribution \\- Notes[\\s]{0,}</dt>)" 
				+ "(?:[\\s]{0,}<dd>[\\s]{0,})"
				//+ "([" + SCIENTIFIC_NAME_PATTERN + "]{0,})"
				+ "([\\w\\W]{0,})"
				+ "?(?:</dd>)" , MappingType.REGEX,
				subject, Predicates.DISTRIBUTION_TEXT);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String source = "http://www.padil.gov.au/";
		String subject = triples.get(0).getSubject();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.equals(Predicates.PEST_STATUS.toString())) {
				String currentObj = (String) triple.getObject();

				if(currentObj.startsWith("Status")){
					currentObj = currentObj.substring(6);
					currentObj = currentObj.trim();
				}
				
				triple.setObject(currentObj);
								
			} else if(predicate.equals(Predicates.DISTRIBUTION_TEXT.toString())) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj;

				if (newObj.contains("</dd>")) {

					newObj = newObj.split("</dd>")[0];

					//newObj = cleanup(newObj);
					
					newObj = newObj.replaceAll("<[" + SCIENTIFIC_NAME_PATTERN + "]{0,}>", "");
					newObj = newObj.replaceAll("  ", "");
					newObj = newObj.replaceAll("[\\s&&[^ ]]{1,}", "");
					
				}
				
				triple.setObject(newObj);
			} else if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = imageUrl.split("/")[1];
				//triple.setObject(imageUrl);
				
				imageUrl = source + imageUrl;
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				tmpTriple.add(triple);
			}  
		}
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
	
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
	}
}
