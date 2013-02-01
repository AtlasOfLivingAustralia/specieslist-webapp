package org.ala.documentmapper;

import org.ala.repository.Predicates;
import org.ala.util.MimeType;

public class IdentifyLifeDocumentMapper extends XMLDocumentMapper{
	public IdentifyLifeDocumentMapper() {
		setRecursiveValueExtraction(true);			
		//set the content type this doc mapper handles
		this.contentType = MimeType.XML.toString();			
		//set an initial subject
		String subject = MappingUtils.getSubject();
		
		addDCMapping("//key/title", subject, Predicates.DC_TITLE);
		addDCMapping("//key/publisher", subject, Predicates.DC_PUBLISHER);
		addDCMapping("//key/description", subject, Predicates.DC_DESCRIPTION);
		addDCMapping("//key/creator", subject, Predicates.DC_CREATOR);
		addDCMapping("//key/CCRights", subject, Predicates.DC_RIGHTS);
		
		addTripleMapping("//key/description", subject, Predicates.DISTRIBUTION_TEXT);
//		addTripleMapping("//key/creator", subject, Predicates.AUTHOR);
		addTripleMapping("//key/taxonomicscope", subject, Predicates.PHYLUM);
		addTripleMapping("//key/geographicscope", subject, Predicates.COUNTRY);
	}
}
