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

/**
 * A DocumentMapper knows how to extract RDF triples and Dublin core properties
 * from a document and produce one or more <code>ParsedDocument</code> instances. In the case where
 * a separate artefact such as an image is referenced in a HTML page for example,
 * a separate <code>ParsedDocument</code> is created.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface DocumentMapper {

	/**
	 * Return a list of parsed documents. This returns 0...n documents.
	 * 
	 * For HTML pages we will return the HTML and triples as one <code>ParsedDocument</code>,
	 * and any other images that we have gathered as separate <code>ParsedDocument</code>.
	 * 
	 *
	 * @see ParsedDocument
	 *
	 * @param guid the guid for this document, typically a URL
	 * @param content the raw byte content of the document
	 * 
	 * @return 0..n ParsedDocument instances
	 */
	public List<ParsedDocument> map(String guid, byte[] content) throws Exception;
}
