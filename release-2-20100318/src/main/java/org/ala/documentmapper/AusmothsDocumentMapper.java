package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

/**
 * Document mapper for the *new* Australian Moths Online
 */
public class AusmothsDocumentMapper extends XMLDocumentMapper {

	public AusmothsDocumentMapper() {

		String subject = MappingUtils.getSubject();
		
		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content",
				subject, Predicates.DC_IDENTIFIER);

		addDCMapping("//title/text()", subject, Predicates.DC_IDENTIFIER);

		addTripleMapping("//title/text()", 
				subject, Predicates.SCIENTIFIC_NAME);
		
		addTripleMapping("//img[@id=\"IFid1\"]/attribute::src", 
				subject, Predicates.IMAGE_PAGE_URL);
		
		addTripleMapping("//a[@class=\"BreadCrumb-2\"]/text()", 
				subject, Predicates.FAMILY);

	}
	
	@Override
	public List<ParsedDocument> map(String uri, byte[] content)
		throws Exception {
		
		String documentStr = new String(content);

		documentStr = documentStr.replaceAll("// ]]>", "");;		
		
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
		
		String subject = triples.get(0).getSubject();
		
		String source = "http://www1.ala.org.au";
		
		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("title")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split("\\(")[0].trim();
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} 
			} else if(predicate.endsWith("hasScientificName")) {
				String currentObj = (String) triple.getObject();
				String newObj = currentObj.split("\\(")[0].trim();
				if(! "".equals(newObj)) {
					triple.setObject(newObj);
				} 
			} else if(predicate.endsWith("hasImagePageUrl")) {
				String imageUrl = (String) triple.getObject();
				imageUrl = source + imageUrl;
				triple.setObject(imageUrl);
				
				//retrieve the image and create new parsed document
				ParsedDocument imageDoc = MappingUtils.retrieveImageDocument(pd, imageUrl);
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
