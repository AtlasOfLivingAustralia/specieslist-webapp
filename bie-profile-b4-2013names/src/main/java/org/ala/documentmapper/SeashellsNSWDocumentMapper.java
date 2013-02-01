package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;
import org.w3c.dom.Document;

/**
 * Document mapper for the Seashells of New South Wales website.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SeashellsNSWDocumentMapper extends XMLDocumentMapper {

	public SeashellsNSWDocumentMapper() {
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", 
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);



		/*
		 * Not all Seashells of NSW pages have the Synonym item and some pages can have multiple synonyms. It will be hard to 
		 * make the structure correct if we include the author and year related to the synonyms. Therefore, I am just extracting
		 * the synonyms here.  
		 */
		addTripleMapping("//p[b[contains(.,\"Synonymy\")]]|//font[b[contains(.,\"Synonymy\")]]", 
				subject, Predicates.SYNONYM);

		//addDefaultSubjectMapping("//html/body/div[1]/center[1]/table[1]/tr[2]/td[2]/p[b[contains(.,\"Synonymy:\")]]/text()", 
		//		FedoraConstants.ALA_NAMESPACE, "hasSynonymTmp", false);

		addTripleMapping("//p[b[contains(.,\"Size\")]]|//font[b[contains(.,\"Size\")]]", 
				subject, Predicates.MORPHOLOGICAL_TEXT);
		
		addTripleMapping("//p[b[contains(.,\"Description:\")]]|//font[b[contains(.,\"Description:\")]]", 
				subject, Predicates.DESCRIPTIVE_TEXT);
		
		addTripleMapping("//html/body/div[1]/center[1]/table[1]/tbody[1]/tr[2]/td[2]/p[1]|//html/body/div[1]/center[1]/table[1]/tbody[1]/tr[2]/td[2]/i[1]", 
				subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//p[b[contains(.,\"Distribution:\")]]|//font[b[contains(.,\"Distribution:\")]]", 
				subject, Predicates.DISTRIBUTION_TEXT);
		
		addTripleMapping("//p[b[contains(.,\"Habitat\")]]|//font[b[contains(.,\"Habitat\")]]", 
				subject, Predicates.HABITAT_TEXT);
		
		addTripleMapping("//img/attribute::src", 
				subject, Predicates.IMAGE_URL);
	}


	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		String subject = MappingUtils.getSubject();
		
//		String sciName = getXPathSingleValue(xmlDocument, "//html/body/div[1]/center[1]/table[1]/tr[2]/td[2]/p[1]/descendant::text()");
////			addDefaultSubjectMapping("//html/body/div[1]/center[1]/table[1]/tr[2]/td[2]/p[1]/descendant::text()", 
////					Namespaces.ALA, "hasScientificName", false);
//
//		String sciName2 = getXPathSingleValue(xmlDocument, "//html/body/div[1]/center[1]/table[1]/tr[2]/td[2]/p[1]/text()");
//		
//		String fullSciName = "";
//		if(sciName!=null)
//			fullSciName+=sciName;
//
//		if(sciName2!=null){
//			if(fullSciName.length()>0)
//				fullSciName+=" ";
//			fullSciName+=sciName;
//		}
		
//			addDefaultSubjectMapping("//html/body/div[1]/center[1]/table[1]/tr[2]/td[2]/p[1]/text()", 
//					Namespaces.ALA, "hasScientificNameTmp", false);
		
		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();
//
		List<Triple<String,String,String>> toRemove = new ArrayList<Triple<String,String,String>>();
		
		String source = pd.getGuid().split("Pages")[0];
		
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Des Beechey - Seashells of New South Wales");
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "DPA - CC-BY-NC");
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Animalia"));
//		Triple<String,String,String> tmpScientificNameTriple = null;
//
//		String subject = triples.get(0).getSubject();
//
//		String fullScientificName = null;
//		String tmpScientificName = null;
//
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasSynonym")) {
				String currentObj = (String) triple.getObject();
				String[] tmp = currentObj.split(":");
				String newObj = tmp[tmp.length-1];
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasHabitatText")) {
				String currentObj = (String) triple.getObject();
				String[] tmp = currentObj.split(":");
				String newObj = tmp[tmp.length-1];
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasDistributionText")) {
				String currentObj = (String) triple.getObject();
				String[] tmp = currentObj.split(":");
				String newObj = tmp[tmp.length-1];
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasDescriptiveText")) {
				String currentObj = (String) triple.getObject();
				String[] tmp = currentObj.split(":");
				String newObj = tmp[tmp.length-1];
				
				triple.setObject(newObj.trim());
			} else if(predicate.endsWith("hasMorphologicalText")) {
				String currentObj = (String) triple.getObject();
				String[] tmp = currentObj.split(":");
				String newObj = tmp[tmp.length-1];
				
				triple.setObject(newObj.trim());
			} else if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = imageUrl.replaceAll("\\.\\./", "");
				imageUrl = imageUrl.replaceAll("_small", "");
				imageUrl = source + imageUrl;
//				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}
				
				toRemove.add(triple);
			}
			
		}
//		//remove the triple from the triples
//		for (Triple tri : tmpTriple) {
//			triples.remove(tri);
//		}
		triples.removeAll(toRemove);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullSciName));
		

		//replace the list of triples
		pd.setTriples(triples);
	}


}
