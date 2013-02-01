package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.ala.repository.Namespaces;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

// Document mapper for Department of Environment and Conservation - NSW threatened species 
public class DecDocumentMapper extends XMLDocumentMapper {

	public DecDocumentMapper() {

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		
		addDCMapping("//strong[contains(.,\"Scientific name\")]/following-sibling::text()", 
				subject, Predicates.DC_TITLE);
		
		addTripleMapping("//p[@class=\"textBlack60\"]/text()", 
				subject, Predicates.TMP);

		addTripleMapping("//strong[contains(.,\"Scientific name\")]/following-sibling::text()", 	
				subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//h1/text()", 	
				subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//h2[contains(.,\"Description\")]/following-sibling::p[1]/span/text()", 	
				subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("//span[contains(child::strong,\"Distribution\")]/following-sibling::span/text()", 	
				subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("//span[contains(child::strong,\"Habitat and ecology\")]/following-sibling::ul[1]//text()", 	
				subject, Predicates.HABITAT_TEXT);
		
		addTripleMapping("concat(//strong[contains(.,\"Conservation status\")]/text(),//strong[contains(.,\"Conservation status\")]/following-sibling::a[1]/text())", 	
				subject, Predicates.CONSERVATION_STATUS);
		
		addTripleMapping("concat(//strong[contains(.,\"conservation status\")]/text(),//strong[contains(.,\"conservation status\")]/following-sibling::a[1]/text())", 	
				subject, Predicates.CONSERVATION_STATUS);
		
		addTripleMapping("//h2[contains(.,\"Threats\")]/following-sibling::ul[1]//text()", 	
				subject, Predicates.THREATS_TEXT);
		
		addTripleMapping("//p[@class=\"textBlack60\"]//img/attribute::src",	
				subject, Predicates.IMAGE_URL);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</i>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}<em>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("[\\s&&[^ ]]{0,}</em>[\\s&&[^ ]]{0,}", "");
		documentStr = documentStr.replaceAll("<br>", "");
		
		
		//System.out.println(documentStr);
		
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
		List<String> imageUrls = new ArrayList<String>();
		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();
		
		String source = "http://www.threatenedspecies.environment.nsw.gov.au";
		String fullHabitatText = new String();
		String fullThreatsText = new String();
		String subject = MappingUtils.getSubject();
		String commonName = null;
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://www.threatenedspecies.environment.nsw.gov.au/tsprofile/copyright.aspx");
		
//		System.out.println(subject);
		
		boolean gotSciName = false;
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasCommonName")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split(" - ")[0];
				newObj = newObj.split("[\\s]{1,}[a-z]{1,}[\\s]{1,}")[0];
				commonName = newObj;
				
				if(! "".equals(newObj)) {
					triple.setObject(newObj.trim());
				} 
								
			} else if(predicate.endsWith("hasHabitatText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");
				
				fullHabitatText += " " + newObj;
				
				tmpTriple.add(triple);

			} else if(predicate.endsWith("hasThreatsText")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll(" ,", ",");
				
				fullThreatsText += " " + newObj;

				tmpTriple.add(triple);
			} else if(predicate.endsWith("hasConservationStatus")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.replaceAll("[\\s&&[^ ]]{1,}", " ");
				newObj = newObj.replaceAll("[ ]{2,}", " ");
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} 
								
			} else if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				imageUrl = imageUrl.replaceAll("_small", "");
				
				imageUrls.add(imageUrl);
				
				//retrieve the image and create new parsed document
//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
//				if(imageDoc!=null){
//					pds.add(imageDoc);
//				}

				tmpTriple.add(triple);
			} else if (predicate.endsWith("hasScientificName")) {
				gotSciName = true;
			}  else if (predicate.endsWith("tmp")) {
				String currentObj = (String) triple.getObject();
				
				if (currentObj.toLowerCase().contains("image:")) {
					String creator = currentObj.split(":")[1].trim();
					
					pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
				}
				
				tmpTriple.add(triple);
			}
		}
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		triples.add(new Triple(subject, Predicates.HABITAT_TEXT.toString(), fullHabitatText.trim()));
		triples.add(new Triple(subject, Predicates.THREATS_TEXT.toString(), fullThreatsText.trim()));
		
		if (!gotSciName && commonName != null) {
			triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), commonName.trim()));
		} else if (!gotSciName && commonName == null) {
			pds.remove(pd);
		}
		
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}
		
		//replace the list of triples
		pd.setTriples(triples);
		
		for (String imageUrl : imageUrls) {
			ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
			if(imageDoc!=null){
				pds.add(imageDoc);
			}
		}
	}
	
}
