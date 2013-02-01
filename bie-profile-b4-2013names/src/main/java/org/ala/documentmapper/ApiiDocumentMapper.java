package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;
import org.apache.commons.lang.StringUtils;


// Australian Plant Image Index Document Mapper
public class ApiiDocumentMapper extends XMLDocumentMapper {

	public ApiiDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
		addDCMapping("//h2/text()",subject,Predicates.DC_TITLE);
		addTripleMapping("//h2/text()",subject,Predicates.SCIENTIFIC_NAME);
//		addTripleMapping("//a[small[contains(.,\"enlarge image\")]]/following-sibling::img[1]/attribute::src",subject,Predicates.IMAGE_URL);
		addTripleMapping("//img[contains(@alt,\"photo\")]/@src",subject,Predicates.IMAGE_URL);
	}
	
	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
		
		String subject = MappingUtils.getSubject();
		String source = "http://www.anbg.gov.au";
		
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.anbg.gov.au/copyright.html");
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
		
		String photographer = getXPathSingleValue(xmlDocument, "//b[contains(.,\"Photographer\")]/following-sibling::text()[1]");
		if(!StringUtils.isEmpty(photographer)){
			photographer = photographer.replaceAll(":", "").trim();
			pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), photographer);
		} 
		
		String locality = getXPathSingleValue(xmlDocument, "//b[contains(.,\"Taken at\")]/following-sibling::text()[1]");
		if(!StringUtils.isEmpty(locality)){
			locality = locality.replaceAll(":", "").trim();
			pd.getDublinCore().put(Predicates.LOCALITY.toString(), locality);
		} 
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
		String sciName = null;

		for (Triple<String,String,String> triple: triples) {
            String predicate = triple.getPredicate().toString();
            if (predicate.endsWith("hasScientificName")) {
                sciName = (String) triple.getObject();
            }
        }
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				String genus = null;
				String species = null;				
				
				if (sciName != null && sciName.contains(" ")) {
				    genus = sciName.split(" ")[0];
				    species = sciName.split(" ")[1];
				}
				ParsedDocument imageDoc = null;
				if (genus != null && species != null) {
				//retrieve the image and create new parsed document
				    String sourceUrl = "http://www.anbg.gov.au/cgi-bin/apiiGenus?genus=" + genus + "&species=" + species;
				    
				    imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl, sourceUrl);
				} else {
				    imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				}
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			}
		}
		triples.removeAll(toBeRemoved);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullScientificName));
		
	}
}	

