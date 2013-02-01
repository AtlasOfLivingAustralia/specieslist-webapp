package org.ala.documentmapper;

import java.util.*;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.repository.Namespaces;


// Australian Native Plants Society Document Mapper
public class AnpsDocumentMapper extends XMLDocumentMapper {

	public AnpsDocumentMapper() {
		// specify the namespaces
		setRecursiveValueExtraction(true);

		String subject = MappingUtils.getSubject();
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",subject,Predicates.DC_IDENTIFIER );
		addDCMapping("//h2/text()",subject,Predicates.DC_TITLE);
		addTripleMapping("//td[b[contains(.,\"Family:\")]]/following-sibling::td[1]/text()",subject,Predicates.FAMILY);
		addTripleMapping("//h2/text()",subject,Predicates.SCIENTIFIC_NAME);
		//addTripleMapping("//div[@id=\"contents\"]/h2/span/text()",subject,Predicates.SCIENTIFIC_NAME);
		addTripleMapping("//td[b[contains(.,\"Distribution:\")]]/following-sibling::td[1]/text()",subject,Predicates.DISTRIBUTION_TEXT);
		addTripleMapping("//td[b[contains(.,\"Common Name:\")]]/following-sibling::td[1]/text()",subject,Predicates.COMMON_NAME);
		addTripleMapping("//td[b[contains(.,\"Conservation Status:\")]]/following-sibling::td[1]/text()",subject,Predicates.CONSERVATION_STATUS);
		addTripleMapping("//h3[contains(.,\"General Description:\")]/following-sibling::p",subject,Predicates.DESCRIPTIVE_TEXT);
		addTripleMapping("//img[@class=\"gallery\"]/attribute::src",subject,Predicates.IMAGE_URL);
	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);
		
		documentStr = documentStr.replaceAll("[\\s]{0,}</p>[\\s]{0,}<blockquote>", "");
		documentStr = documentStr.replaceAll("</blockquote>", "</p>");
		
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
		
		String subject = MappingUtils.getSubject();
		String source = "http://asgap.org.au/";
		
		pd.getDublinCore().put(Predicates.DC_LICENSE.toString(), "http://asgap.org.au/copy.html");
		
		List<Triple> toBeRemoved = new ArrayList<Triple>();
		
		//String scientificName = getTripleObjectLiteral(pds, "title");
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if (predicate.endsWith("hasImageUrl")) {
				String imageUrl = (String) triple.getObject();
				//correct the image URL
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				String creator = getXPathSingleValue(xmlDocument, "//span[contains(.,\"Photo:\")]/text()");
				
				if (creator != null && !"".equals(creator)) {
					creator = creator.replaceAll("Photo:", "").trim();
				}
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd,imageUrl);
				if(imageDoc!=null){
					imageDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
					pds.add(imageDoc);
				}
				
				toBeRemoved.add(triple);
			}
		}
		triples.removeAll(toBeRemoved);
		//triples.add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), fullScientificName));
		triples.add(new Triple(subject, Predicates.KINGDOM.toString(), "Plantae"));
	}
}	

