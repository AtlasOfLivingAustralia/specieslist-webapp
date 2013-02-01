package org.ala.documentmapper;

import java.util.ArrayList;
import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.w3c.dom.Document;

public class FishnamesDocumentMapper extends XMLDocumentMapper {

	public FishnamesDocumentMapper() {

		String subject = MappingUtils.getSubject();

		addDCMapping("//html/head/meta[@scheme=\"URL\" and @name=\"ALA.Guid\"]/attribute::content", subject, Predicates.DC_IDENTIFIER);
		addDCMapping("//title/text()", subject, Predicates.DC_TITLE);

		addTripleMapping("//table[@class=\"mittel\"]/tbody[1]/tr[1]/td[2]/div[1]/dir[1]/p[1]/text()", 
				subject, Predicates.CAAB_CODE);

		addTripleMapping("//table[@class=\"mittel\"]/tbody[1]/tr[1]/td[2]/div[1]/dir[1]/p[2]/text()", 
				subject, Predicates.COMMON_NAME);

		addTripleMapping("//table[@class=\"mittel\"]/tbody[1]/tr[1]/td[2]/div[1]/dir[1]/p[3]/i/text()", 
				subject, Predicates.SCIENTIFIC_NAME);

		addTripleMapping("//table[@class=\"mittel\"]/tbody[1]/tr[1]/td[2]/div[1]/dir[1]/p[4]/text()", 
				subject, Predicates.AUTHOR);

		addTripleMapping("//table[@class=\"mittel\"]/tbody[1]/tr[1]/td[2]/div[1]/dir[1]/p[5]/a/text()", 
				subject, Predicates.FAMILY);
	}

	/**
	 * @see ala.documentmapper.XMLDocumentMapper#extractProperties(org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds, Document xmlDocument) throws Exception {

		ParsedDocument pd = pds.get(0);
		List<Triple<String,String,String>> triples = pd.getTriples();

		List<Triple<String,String,String>> tmpTriple = new ArrayList<Triple<String,String,String>>();

		if(triples.size()==0){
			return;
		}

		String subject = triples.get(0).getSubject();

		//correct the DC title
		String title = pd.getDublinCore().get(Predicates.DC_TITLE.toString());
		if(title!=null) {
			String currentObj = title.trim();
			String newObj = currentObj.split(":")[0];
			if(! "".equals(newObj)) {
				pd.getDublinCore().put(Predicates.DC_TITLE.toString(), newObj);
			} 
		}

		for (Triple<String,String,String> triple: triples) {
			String predicate = triple.getPredicate().toString();
			if(predicate.endsWith("hasScientificName")) {
				String currentObj = ((String)triple.getObject()).trim();
				String newObj = null;
				if (currentObj.split(":").length>1) {
					newObj = currentObj.split(":")[1].trim();
				}
				if(newObj != null && ! "".equals(newObj)) {
					triple.setObject(newObj);
				} 
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
