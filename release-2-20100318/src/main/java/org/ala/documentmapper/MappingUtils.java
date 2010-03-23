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
import org.ala.util.MimeType;
import org.ala.util.Response;
import org.ala.util.WebUtils;
import org.apache.log4j.Logger;

/**
 * Static utilities for Document Mappers.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class MappingUtils {

	public static Logger logger = Logger.getLogger(MappingUtils.class);

	public static String topicPrefix = "topic_";

	/**
	 * Method that provides a placeholder subject for the extracted triples.
	 * These subjects are meaningless and only serve to separate topics of triples.
	 * 
	 * For example an harvested HTML page, with multiple species on each page.
	 * Each species is effectively a separate topic
	 * 
	 * @return
	 */
	public static String getSubject(){
		return topicPrefix+0;
	}

	/**
	 * Retrieve the next topic.
	 * 
	 * @param currentSubject
	 * @return a new subject
	 */
	public static String getNextSubject(String currentSubject){
		//get current idx
		String idxAsString = currentSubject.substring(topicPrefix.length());
		int idx = Integer.parseInt(idxAsString);
		return topicPrefix+(++idx);
	}


	/**
	 * Retrieve an image document using the supplied image URL. This also
	 * copies across taxonomic assertions.
	 * 
	 * @param originalDoc
	 * @param imageUrl
	 * @return
	 * @throws Exception
	 */
	public static ParsedDocument retrieveImageDocument(ParsedDocument originalDoc, String imageUrl) throws Exception {
		Response response = WebUtils.getUrlContentAsBytes(imageUrl);
		ParsedDocument imageDoc = new ParsedDocument();
		imageDoc.setGuid(imageUrl);
		String contentType = response.getContentType();

		//check the content type - may have supplied HTML 404
		if(!MimeType.getImageMimeTypes().contains(contentType)){
			logger.warn("Unrecognised mime type for image: "+contentType+" for image URL "+imageUrl+". Returning null parsed document.");
			return null;
		}

		imageDoc.setContentType(contentType);
		imageDoc.setContent(response.getResponseAsBytes());
		//copy across taxonomic information from original document		
		List<Triple<String,String,String>> triples = originalDoc.getTriples();
		
		List<String> taxonomicPredicates = Predicates.getTaxonomicPredicates();
		for(Triple<String,String,String> triple:triples){
			
			if(taxonomicPredicates.contains(triple.getPredicate())){
				imageDoc.getTriples().add(triple);
			}
		}

		imageDoc.getDublinCore().put(Predicates.DC_IDENTIFIER.toString(), imageUrl);

		imageDoc.setParentGuid(originalDoc.getGuid());
		return imageDoc;
	}
}
