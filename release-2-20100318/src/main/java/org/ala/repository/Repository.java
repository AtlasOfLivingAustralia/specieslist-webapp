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
package org.ala.repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.ala.model.Document;

/**
 * An interface for a repository.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Repository {
	
	/**
	 * Get document by the GUID/URI.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	public Document getDocumentByGuid(String guid) throws Exception;
	/**
	 * Store this parsed document, storing the raw content, dublin core and associated triples.
	 * 
	 * @param infoSourceId
	 * @param uri
	 * @param content
	 * @param mimeType
	 * @param parentDocumentId nullable
	 * @return
	 * @throws IOException
	 */
	public Document storeDocument(int infoSourceId, ParsedDocument parsedDocument) throws Exception;
	/**
	 * 
	 * @param infoSourceId
	 * @param guid
	 * @param mimeType
	 * @return
	 * @throws Exception
	 */
	public DocumentOutputStream getDocumentOutputStream(int infoSourceId, String guid, String mimeType) throws Exception;
	/**
	 * 
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public DocumentOutputStream getRDFOutputStream(int documentId) throws Exception;
	/**
	 * Store a raw document.
	 * 
	 * @param infoSourceId
	 * @param uri
	 * @param content
	 * @param mimeType
	 * @param parentDocumentId nullable
	 * @return Document giving details of where the raw content has been stored within the system.
	 * @throws IOException
	 */
	public Document storeDocument(int infoSourceId, String uri, byte[] content, String mimeType, Integer parentDocumentId) throws Exception;
	/**
	 * Store the derived triples for this document. Overwrites previously stored triples.
	 * 
	 * @param documentId
	 * @param content
	 * @param metadataFileName
	 * @throws IOException
	 */
	public void storeRDF(int documentId, List<Triple<String,String,String>> triples) throws Exception;
	/**
	 * Store the dublin core associated with this document.
	 * 
	 * @param documentId
	 * @param identifier
	 * @param title
	 * @param source
	 * @throws IOException
	 */
	public void storeDublinCore(int documentId, Map<String, String> dcProperties) throws Exception;
}
