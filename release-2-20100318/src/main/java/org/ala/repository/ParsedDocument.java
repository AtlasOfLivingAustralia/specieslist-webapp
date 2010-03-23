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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsed document encapsulates a processed document. It consists of
 * the original byte content, extracted dublin core information
 * and extracted assertions (Triples).
 * 
 * Extracted content contained in this object includes: <br/>
 * 
 * <ul>
 * 	<li> Dublin core: This is information about the harvested artefact i.e. HTML page, image etc. </li> 
 *  <li> Triples: This are assertions that we have extracted about real life things we are trying to describe in
 *  the system. For example "Aus bus hasOccurredIn ACT". </li>
 *  <li> Raw content: The raw unaltered byte content </li>
 * </ul>
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ParsedDocument {
	/**
	 * The GUID for this document. This is typically the URL.
	 */
	private String guid = null;
	/**
	 * Used to hold a relationship to related document. 
	 * Example: the html, image.... 
	 */
	private String parentGuid;
	/**
	 * The raw content. This could be the html, jpeg data.
	 */
	private byte[] content = null;
	/**
	 * Examples:
	 * 
	 * URI	hasScientificName	"Aus bus"
	 */
	private List<Triple<String, String, String>> triples = new ArrayList<Triple<String, String, String>>();	
	/**
	 * Examples:
	 * 
	 * dc:identifer "http://......"
	 * dc:title	"Aus bus"
	 * dc:publisher "Australian Museum"
	 */
	private Map<String, String> dublinCore = new LinkedHashMap<String, String>();
	
	/**
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}
	/**
	 * @return the triples
	 */
	public List<Triple<String, String, String>> getTriples() {
		return triples;
	}
	/**
	 * @param triples the triples to set
	 */
	public void setTriples(List<Triple<String, String, String>> triples) {
		this.triples = triples;
	}
	/**
	 * @return the dublinCore
	 */
	public Map<String, String> getDublinCore() {
		return dublinCore;
	}
	/**
	 * @param dublinCore the dublinCore to set
	 */
	public void setDublinCore(Map<String, String> dublinCore) {
		this.dublinCore = dublinCore;
	}
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the parentGuid
	 */
	public String getParentGuid() {
		return parentGuid;
	}
	/**
	 * @param parentGuid the parentGuid to set
	 */
	public void setParentGuid(String parentGuid) {
		this.parentGuid = parentGuid;
	}
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		if(this.dublinCore==null)
			return null;
		return this.dublinCore.get(Predicates.DC_FORMAT.toString());
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		if(contentType!=null){
			this.dublinCore.put(Predicates.DC_FORMAT.toString(), contentType);
		}
	}
}