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
package org.ala.documentmappers;

import java.util.List;

import junit.framework.TestCase;
import org.ala.documentmapper.AbrsDocumentMapper;
import org.ala.documentmapper.WtaDocumentMapper;
import org.ala.documentmapper.ZseaDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 * Junit test for the WtaDocumentMapper
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class WtaDocumentMapperTest extends TestCase {

	public void test() throws Exception {
		WtaDocumentMapper dm = new WtaDocumentMapper();
		String uri = "http://anic.ento.csiro.au/thrips/identifying_thrips/Erotidothrips_mirabilis.htm";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());

        for(ParsedDocument pd : parsedDocs){
			List<Triple<String,String,String>> triples = pd.getTriples();

			DebugUtils.debugParsedDoc(pd);
			
			for(Triple triple: triples){
                String predicate = triple.getPredicate().toString();

                if (predicate.endsWith("title")) {
                    assertEquals("title", (String) triple.getObject(), "Erotidothrips mirabilis");
                } else if (predicate.endsWith("hasScientificName")) {
                    assertEquals("hasScientificName", (String) triple.getObject(), "Erotidothrips mirabilis");
                } else if (predicate.endsWith("hasKingdom")) {
                    assertEquals("hasKingdom", (String) triple.getObject(), "Animalia");
                } else if (predicate.endsWith("hasImageUrl")) {
                    assertEquals("hasImageUrl", (String) triple.getObject(), "http://anic.ento.csiro.au/thrips/identifying_thrips/images/Erotidothrips_mirabilis/ErotidoFull.jpg");
                }
			}
		}
	}
}
