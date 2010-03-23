/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.documentmapper;

import java.util.List;

import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * A Document Mapper for Flickr.
 * 
 * Mapping multimedia related fields to http://rs.tdwg.org/mrtg/RDF/mrtg.n3
 * 
 * @author Dave Martin
 */
public class FlickrDocumentMapper extends XMLDocumentMapper {

	private static Logger logger = Logger.getLogger(FlickrDocumentMapper.class);

	public FlickrDocumentMapper() {

		String subject = MappingUtils.getSubject();
		
		addDCMapping("/rsp/photo/title/text()", subject, Predicates.DC_TITLE);
		addDCMapping("/rsp/photo/urls/url[@type=\"photopage\"]/text()", subject, Predicates.DC_IDENTIFIER);
		
		addTripleMapping("/rsp/photo/urls/url[@type=\"photopage\"]/text()", subject, Predicates.IMAGE_PAGE_URL);
		addDCMapping("/rsp/photo/description/text()",subject, Predicates.DC_DESCRIPTION);
		addDCMapping("/rsp/photo/location/@latitude",subject, Predicates.LATITUDE);
		addDCMapping("/rsp/photo/location/@longitude", subject, Predicates.LONGITUDE);
		addDCMapping("/rsp/photo/location/region/text()",subject, Predicates.STATE_PROVINCE);
		addDCMapping("/rsp/photo/location/country/text()",subject, Predicates.COUNTRY);
	}

	/**
	 * @see org.ala.documentmapper.XMLDocumentMapper#extractProperties(java.util.List, org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds,
			Document xmlDocument) throws Exception {

		String subject = MappingUtils.getSubject();
		
		ParsedDocument parsedDoc = pds.get(0);
		
		String photoId = getXPathSingleValue(xmlDocument, "/rsp/photo/@id");
		String mediaType = getXPathSingleValue(xmlDocument, "/rsp/photo/@media");
		String farmId = getXPathSingleValue(xmlDocument, "/rsp/photo/@farm");
		String serverId = getXPathSingleValue(xmlDocument, "/rsp/photo/@server");
		String photoSecret = getXPathSingleValue(xmlDocument, "/rsp/photo/@secret");
		
		String photoUrl = "http://farm" + farmId + ".static.flickr.com/"
			+ serverId + "/" + photoId + "_" + photoSecret + ".jpg";
		
		// taxonomy machine tags
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:trinomial')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:trinomial')]", "taxonomy:trinomial", 
				Predicates.SUBSPECIES );
		
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:binomial')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:binomial')]", "taxonomy:binomial", 
				Predicates.SCIENTIFIC_NAME );
		
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:common')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:common')]", "taxonomy:common", 
				Predicates.COMMON_NAME );

		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:species')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:species')]", "taxonomy:species", 
				Predicates.SPECIES );
		
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:genus')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:genus')]", "taxonomy:genus", 
				Predicates.GENUS );
		
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:family')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:family')]", "taxonomy:family", 
				Predicates.FAMILY );
		
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:kingdom')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:kingdom')]", "taxonomy:kingdom", 
				Predicates.KINGDOM );
		
				
		// If current media type is not a photo, return.
		// Ony media marked as media is ingested.
		if ("photo".equals(mediaType)) {
			logger.info("Preparing to create digital object for image from " + "`"
					+ photoUrl + "`" + " with scientific name " + "`" + mediaType
					+ "`");
	
			Response response = WebUtils.getUrlContentAsBytes(photoUrl);
			parsedDoc.setGuid(photoUrl);
			parsedDoc.setContent(response.getResponseAsBytes());
			parsedDoc.setContentType(response.getContentType());
			
//			DebugUtils.debugParsedDoc(parsedDoc);
		}
	} // End of `postProcessProperties` method.

	private void handleMachineTag(ParsedDocument parsedDoc, Document xmlDocument,
			String subject, String xpath, String machineTag, Predicates predicate) throws Exception {
		//"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:binomial')]"
		String tag = getXPathSingleValue(xmlDocument, xpath);
		if(tag!=null){
			String scientificName = tag.substring(machineTag.length()+1); // "taxonomy:binomial="
			parsedDoc.getTriples().add(new Triple(subject, predicate.toString(), scientificName));
		}
	}

}