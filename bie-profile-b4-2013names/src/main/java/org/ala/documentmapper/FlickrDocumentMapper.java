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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.ala.model.Licence;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		addDCMapping("/rsp/photo/urls/url[@type=\"photopage\"]/text()", subject, Predicates.DC_IS_PART_OF);
		addDCMapping("/rsp/photo/description/text()",subject, Predicates.DC_DESCRIPTION);
		addDCMapping("/rsp/photo/location/@latitude",subject, Predicates.LATITUDE);
		addDCMapping("/rsp/photo/location/@longitude", subject, Predicates.LONGITUDE);
		addDCMapping("/rsp/photo/location/locality/text()",subject, Predicates.LOCALITY);
		addDCMapping("/rsp/photo/location/region/text()",subject, Predicates.STATE_PROVINCE);
		addDCMapping("/rsp/photo/location/country/text()",subject, Predicates.COUNTRY);
//		addTripleMapping("/rsp/photo/urls/url[@type=\"photopage\"]/text()", subject, Predicates.IMAGE_PAGE_URL);
	}

	/**
	 * @see org.ala.documentmapper.XMLDocumentMapper#extractProperties(java.util.List, org.w3c.dom.Document)
	 */
	@Override
	protected void extractProperties(List<ParsedDocument> pds,
			Document xmlDocument) throws Exception {

		String subject = MappingUtils.getSubject();
		ParsedDocument parsedDoc = pds.get(0);
		String guid = getXPathSingleValue(xmlDocument, "/rsp/photo/urls/url[@type=\"photopage\"]/text()");
		parsedDoc.setGuid(guid);
		
		//set the licencing information in the dublin core
		String licenceID = getXPathSingleValue(xmlDocument, "/rsp/photo/@license");
		Licence licence = licencesMap.get(licenceID);
		if(licence!=null){
			parsedDoc.getDublinCore().put(Predicates.DC_LICENSE.toString(), licence.getUrl());
			parsedDoc.getDublinCore().put(Predicates.DC_RIGHTS.toString(), licence.getName());
		}
		
		//	<owner location="Exmouth 6707, Western Australia" nsid="48991563@N06" realname="Bill &amp; Mark Bell" username="Bill &amp; Mark Bell"/>
		String creator = getXPathSingleValue(xmlDocument, "/rsp/photo/owner/@realname");
		String flickrUsername = getXPathSingleValue(xmlDocument, "/rsp/photo/owner/@username");
		if(!StringUtils.isEmpty(creator)){
			parsedDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), creator);
		} else if(!StringUtils.isEmpty(flickrUsername)){
			parsedDoc.getDublinCore().put(Predicates.DC_CREATOR.toString(), flickrUsername);
		}
		
		String photoId = getXPathSingleValue(xmlDocument, "/rsp/photo/@id");
		String mediaType = getXPathSingleValue(xmlDocument, "/rsp/photo/@media");
		String farmId = getXPathSingleValue(xmlDocument, "/rsp/photo/@farm");
		String serverId = getXPathSingleValue(xmlDocument, "/rsp/photo/@server");
		String photoSecret = getXPathSingleValue(xmlDocument, "/rsp/photo/@secret");
		
		// taxonomy machine tags
		handleMachineTag(parsedDoc, xmlDocument, subject,
				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw");
		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:binomial')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:binomial')]", "taxonomy:binomial", 
//				Predicates.SCIENTIFIC_NAME );
//		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:common')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:common')]", "taxonomy:common", 
//				Predicates.COMMON_NAME );
//
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:species')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:species')]", "taxonomy:species", 
//				Predicates.SPECIES );
//		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:genus')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:genus')]", "taxonomy:genus", 
//				Predicates.GENUS );
//		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:family')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:family')]", "taxonomy:family", 
//				Predicates.FAMILY );
//		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:kingdom')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Taxonomy:kingdom')]", "taxonomy:kingdom", 
//				Predicates.KINGDOM );
//		
//		handleMachineTag(parsedDoc, xmlDocument, subject,
//				"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'geo:country')]|/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'Geo:country')]", "geo:country", 
//				Predicates.COUNTRY );
		
				
		// If current media type is not a photo, return.
		// Ony media marked as media is ingested.
		if ("photo".equals(mediaType)) {
			String photoUrl = "http://farm" + farmId + ".static.flickr.com/"
			+ serverId + "/" + photoId + "_" + photoSecret + ".jpg";
			
			logger.info("Preparing to create digital object for image from " + "`"
					+ photoUrl + "`" + " with scientific name " + "`" + mediaType
					+ "`");
			
			Response response = WebUtils.getUrlContentAsBytes(photoUrl);
			parsedDoc.setContent(response.getResponseAsBytes());
			parsedDoc.setContentType(response.getContentType());
			
			parsedDoc.getTriples().add(new Triple(subject, Predicates.IMAGE_PAGE_URL.toString(), guid));
//			DebugUtils.debugParsedDoc(parsedDoc);
		} else if ("video".equals(mediaType)) {
			String photoUrl = "http://farm" + farmId + ".static.flickr.com/"
			+ serverId + "/" + photoId + "_" + photoSecret + ".jpg";
			
			logger.info("Preparing to create digital object for video from " + "`"
					+ photoUrl + "`" + " with scientific name " + "`" + mediaType
					+ "`");
	
			Response response = WebUtils.getUrlContentAsBytes(photoUrl);
			parsedDoc.setContent(response.getResponseAsBytes());
			parsedDoc.setContentType(response.getContentType());
			parsedDoc.setScreenShot(photoUrl);
			parsedDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), guid);
			parsedDoc.getTriples().add(new Triple(subject, Predicates.VIDEO_PAGE_URL.toString(), guid));
		}
	} // End of `postProcessProperties` method.

	@SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleMachineTag(ParsedDocument parsedDoc, Document xmlDocument,
			String subject, String xpathString) throws Exception {
		//"/rsp/photo/tags/tag[@machine_tag=1]/@raw[starts-with(., 'taxonomy:binomial')]"
//		String tag = getXPathSingleValue(xmlDocument, xpath);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList nodes = (NodeList) xpath.evaluate(xpathString, xmlDocument, XPathConstants.NODESET);
		boolean gotSciName = false;
		String subspecies = null;
		String genus = null;
		String family = null;
		String order = null;
		String suborder = null;
		String kingdom = null;
		
		for(int i=0; i<nodes.getLength(); i++){
			
			Node node = nodes.item(i);
			String machineTag = node.getNodeValue();
			int charIdx = machineTag.indexOf('=');
			if(charIdx>0){
				String scientificName = machineTag.substring(charIdx + 1);
				scientificName = scientificName.trim();	
				if( machineTag!=null){
					machineTag = machineTag.toLowerCase();
					if(machineTag.contains("binomial")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.SPECIES.toString(), scientificName));
						parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
						gotSciName = true;
					} else if(machineTag.contains("trinomial")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.SUBSPECIES.toString(), scientificName));
						subspecies = scientificName;
//					} else if(machineTag.contains("common")){
//						parsedDoc.getTriples().add(new Triple(subject, Predicates.COMMON_NAME.toString(), scientificName));
					} else if(machineTag.contains("genus")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.GENUS.toString(), scientificName));
						genus = scientificName;
					}else if(machineTag.contains("family")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.FAMILY.toString(), scientificName));
						family = scientificName;
					}else if(machineTag.contains("order")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.ORDER.toString(), scientificName));
						order = scientificName;
					}else if(machineTag.contains("suborder")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.SUBORDER.toString(), scientificName));
						suborder = scientificName;
					}else if(machineTag.contains("kingdom")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.KINGDOM.toString(), scientificName));
						kingdom = scientificName;
					}else if(machineTag.contains("scientific")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), scientificName));
						gotSciName = true;
					}else if(machineTag.contains("country")){
						parsedDoc.getTriples().add(new Triple(subject, Predicates.COUNTRY.toString(), scientificName));
					}
				}
			}
		}
		
		if (!gotSciName) {
		    if (subspecies != null) {
		        parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), subspecies));
		    } else if (genus != null) {
                parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), genus));
            } else if (family != null) {
                parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), family));
            } else if (order != null) {
                parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), order));
            } else if (suborder != null) {
                parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), suborder));
            } else if (kingdom != null) {
                parsedDoc.getTriples().add(new Triple(subject, Predicates.SCIENTIFIC_NAME.toString(), kingdom));
            }
		}
	}
}