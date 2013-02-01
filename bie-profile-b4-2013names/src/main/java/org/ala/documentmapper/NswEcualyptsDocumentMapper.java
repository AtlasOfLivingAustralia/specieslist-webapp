package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;

import org.ala.util.Response;
import org.ala.util.WebUtils;

// Document mapper for NSW ecualypts
public class NswEcualyptsDocumentMapper extends XMLDocumentMapper {
	
	public static final String SCIENTIFIC_NAME_PATTERN = "a-zA-ZÏËÖÜÄÉÈČÁÀÆŒâïëêöüäåéèčáàæœóú\\.\\-`'%\\(\\), ;:&#0-9°/";

	public NswEcualyptsDocumentMapper() {

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//html/body/table[1]/tbody[1]/tr[1]/td[2]/table[1]/tbody[1]/tr[1]/td[1]/font/center//text()",
				subject, Predicates.DC_TITLE);

		addTripleMapping("//html/body/table[1]/tbody[1]/tr[1]/td[2]/table[1]/tbody[1]/tr[1]/td[1]/font/center//text()",
				subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//html/body/table[1]/tbody[1]/tr[1]/td[2]/table[1]/tbody[1]/tr[1]/td[1]/font[1]//a[1]/attribute::href",
				subject, Predicates.IMAGE_URL);
		
		addTripleMapping("(?:<b>[\\s]{0,}Distribution:[\\s]{0,}</b>[\\s]{0,})" 
				+ "([" + SCIENTIFIC_NAME_PATTERN + "]{1,})",MappingType.REGEX,
				subject, Predicates.DISTRIBUTION_TEXT);
		
		/*
		 * The page is badly formatted. The habit, bark, leaves and other information is included in a whole node, which is hard to be
		 * separated. 
		 */
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
		documentStr = documentStr.replaceAll("<!", "");
		documentStr = documentStr.replaceAll("-->", "");
		documentStr = documentStr.replaceAll("-", "");
				
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

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		Triple<String,String,String> titleTriple = null;
		if(triples.size()==0)
			return;
		
		String subject = triples.get(0).getSubject();
		
		String source = "http://plantnet.rbgsyd.nsw.gov.au";
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "DPA - CC-BY-SA");
		pd.getDublinCore().put(Predicates.DC_CREATOR.toString(), "Botanic Gardens Trust([current date in day month year]). " +
				"PlantNET - The Plant Information Network System of Botanic Gardens Trust, Sydney, Australia (version [number]). " +
				"http://plantnet.rbgsyd.nsw.gov.au");
		
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl.replaceAll("\\.\\.", "");
				triple.setObject(imageUrl);
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					pds.add(imageDoc);
				}

				tmpTriple.add(triple);
			}
		}		
		
		
				
		//remove the triple from the triples
		for (Triple tri : tmpTriple) {
			triples.remove(tri);
		}

		//replace the list of triples
		pd.setTriples(triples);
	}

}
