package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.Namespaces;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document mapper for NIMPIS pages
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class NimpisDocumentMapper extends XMLDocumentMapper {

	public NimpisDocumentMapper() {
		
		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//div[@id=\"SpHeading\"]/h1/text()", subject, Predicates.DC_TITLE);
		
		addTripleMapping("//div[@id=\"generalInfo\"]/h3/text()", subject, Predicates.AUTHOR);

		addTripleMapping("//div[@id=\"SpHeading\"]/h1/i/text()", subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//th[b[contains(.,\"Phylum\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.PHYLUM);

		addTripleMapping("//th[b[contains(.,\"Subphylum\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.SUB_PHYLUM);

		addTripleMapping("//th[b[contains(.,\"Class\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.CLASS);
		
		addTripleMapping("//th[b[contains(.,\"Sub Class\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.TMP);

		addTripleMapping("//th[b[contains(.,\"Order\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.ORDER);
		
		addTripleMapping("//th[b[contains(.,\"Super Order\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.TMP);
		
		addTripleMapping("//th[b[contains(.,\"Infra Order\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.TMP);

		addTripleMapping("//th[b[contains(.,\"Family\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.FAMILY);
		
		addTripleMapping("//th[b[contains(.,\"Super Family\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.TMP);
		
		addTripleMapping("//th[b[contains(.,\"Sub Family\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.TMP);

		addTripleMapping("//th[b[contains(.,\"Genus\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.GENUS);

		addTripleMapping("//th[b[contains(.,\"Synonyms\")]]/following-sibling::td[1]/i/text()", 
				subject, Predicates.SYNONYM);

		addTripleMapping("//th[b[contains(.,\"Common names\")]]/following-sibling::td[1]/text()", 
				subject, Predicates.COMMON_NAME);
		
		addTripleMapping("//div[@id=\"SpHeading\"]/div[1]/b/text()", 
				subject, Predicates.PEST_STATUS);

		/*
		 * Some of the similar species have author/reference information beside it. However, they randomly appears in HTML 
		 * code and is hard to figure out. The XPATH expression below does not append the author/reference information to 
		 * the similar species. 
		 */

		addTripleMapping("//div[@id=\"taxoNames\"]/table[1]/tbody[1]/tr[3]/td[1]/em[position()<=last()]/text()", 
				//addMapping("//div[@id=\"taxoNames\"]/table[1]/tbody[1]/tr[3]/td[1]//text()", 
				subject, Predicates.SIMILAR_SPECIES);

//		addTripleMapping("//div[@id=\"taxoNames\"]/table[1]/tbody[1]/tr[5]/td[1]/table[1]/tbody[1]/tr[6]/td[1]/text()", 
//				Namespaces.ALA, "habitatTypeSubstrate", false);
//
//		addTripleMapping("//div[@id=\"taxoNames\"]/table[1]/tbody[1]/tr[5]/td[1]/table[1]/tbody[1]/tr[6]/td[2]/text()", 
//				Namespaces.ALA, "habitatTypeTidalRange", false);
//
//		addTripleMapping("//div[@id=\"taxoNames\"]/table[1]/tbody[1]/tr[5]/td[1]/table[1]/tbody[1]/tr[7]/td[1]/text()", 
//				Namespaces.ALA, "vectorsForIntroduction", false);

//		addTripleMapping("//div[@id=\"species_image\"]/table[1]/tbody[1]/tr[1]/td[1]/text()", 
//				Namespaces.ALA, "hasMinimumLength", false);
//
//		addTripleMapping("//div[@id=\"species_image\"]/table[1]/tbody[1]/tr[2]/td[1]/text()", 
//				Namespaces.ALA, "hasMaximumLength", false);

		addTripleMapping("//div[@id=\"species_image\"]/img[1]/attribute::src", 
				subject, Predicates.IMAGE_URL);

		addTripleMapping("//div[@id=\"species_image\"]/img[2]/attribute::src", 
				subject, Predicates.IMAGE_URL);

		addTripleMapping("//div[@id=\"generalInfo\"]/div[2]/span[1]/a[1]/attribute::href", 
				subject, Predicates.DIST_MAP_IMG_URL);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		Triple<String,String,String> titleTriple = null;
		String scName = null;
		String source = "http://adl.brs.gov.au/marinepests/";
		String subject = MappingUtils.getSubject();
		
		List<String> tmpList = new ArrayList<String>();
		List<String> imageUrls = new ArrayList<String>();
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.equals(Predicates.TMP.toString())) {
				tmpList.add(((String)triple.getObject()).trim());
				
				tmpTriple.add(triple);
			}
		}
//		System.out.println(getXPathSingleValue(xmlDocument, "//th[b[starts-with(.,\"Phylum\")]]/following-sibling::td[1]/text()"));
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			
			if(predicate.endsWith("hasScientificName")) {
				String currentObj = ((String)triple.getObject()).trim();
				scName = currentObj;
			} else if(predicate.equals(Predicates.DIST_MAP_IMG_URL.toString())) {
				// Convert all the spaces in links to %20
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = source + currentObj.replaceAll(" ", "%20");
				if (!"".equals(newObj)) {
					triple.setObject(newObj);
//				} else {
//					tmpTriple.add(triple);
				}	
			} else if(predicate.equals(Predicates.IMAGE_URL.toString())) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				
				imageUrls.add(imageUrl);
//				triple.setObject(imageUrl);
//				
//				//retrieve the image and create new parsed document
//				
//				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
//				if(imageDoc!=null){
//					pds.add(imageDoc);
//				}

				tmpTriple.add(triple);
			} else if (predicate.equals(Predicates.CLASS.toString())) {
				String currentObj = ((String)triple.getObject()).trim();
				
				if (tmpList.contains(currentObj)) {
					tmpTriple.add(triple);
				}
			} else if (predicate.equals(Predicates.ORDER.toString())) {
				String currentObj = ((String)triple.getObject()).trim();
				
				if (tmpList.contains(currentObj)) {
					tmpTriple.add(triple);
				}
			} else if (predicate.equals(Predicates.FAMILY.toString())) {
				String currentObj = ((String)triple.getObject()).trim();
				
				if (tmpList.contains(currentObj)) {
					tmpTriple.add(triple);
				}
			}
		}
		
		if(titleTriple!=null && scName!=null) {
			titleTriple.setObject(scName + " " + titleTriple.getObject());
		}
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
		
//		triples.add(titleTriple);
		
		//remove the triple from the triples
		triples.removeAll(tmpTriple);

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
