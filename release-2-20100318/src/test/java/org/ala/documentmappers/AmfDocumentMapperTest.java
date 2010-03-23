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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.ala.documentmapper.AmfDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 * Australian Museum Factsheet document mapper tests.
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class AmfDocumentMapperTest extends TestCase {

	final String hasSpecificEpithet = "clarkae";
	final String hasGenus = "Australonycteris";
	final String hasFamily = "Archaeonycteridae";
	final String hasSuborder = "Microchiroptera";
	final String hasOrder = "Chiroptera";
	final String hasMagnorder = "Boreoeutheria";
	final String hasCohort = "Placentalia";
	final String hasInfralegion = "Theria";
	final String hasSublegion = "Boreosphenida";
	final String hasSubdivision = "Theriimorpha";
	final String hasDivision = "Theriiformes";
	final String hasInfraclass = "Holotheria";
	final String hasSubclass = "Mammaliaformes";
	final String hasClass = "Mammalia";
	final String hasSuperclass = "Tetrapoda";
	final String hasSubphylum = "Vertebrata";
	final String hasPhylum = "Chordata";
	final String hasKingdom = "Animalia";
	final String hasDistributionText = "Australonycteris is only known from a single fossil site near the town of Murgon in southeastern Queensland.";
	final String hasHabitatText = "The Murgon area during the early Eocene was a shallow swamp or lake. The vegetation and climate of the period have not yet been determined.";
	final String hasMorphologicalText = "20 cm wingspan";
	
	public void test() throws Exception {
		AmfDocumentMapper dm = new AmfDocumentMapper();
		String uri = "http://australianmuseum.net.au/Australonycteris-clarkae";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasSpecificEpithet")) {
					Assert.assertEquals(object, hasSpecificEpithet);
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);
				}

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);
				}
				
				if (predicate.endsWith("hasSuborder")) {
					Assert.assertEquals(object, hasSuborder);
				}
				
				if (predicate.endsWith("hasOrder")) {
					Assert.assertEquals(object, hasOrder);
				}
				
				if (predicate.endsWith("hasMagnorder")) {
					Assert.assertEquals(object, hasMagnorder);
				}
				
				if (predicate.endsWith("hasCohort")) {
					Assert.assertEquals(object, hasCohort);
				}
				
				if (predicate.endsWith("hasInfralegion")) {
					Assert.assertEquals(object, hasInfralegion);
				}
				
				if (predicate.endsWith("hasSublegion")) {
					Assert.assertEquals(object, hasSublegion);
				}
				
				if (predicate.endsWith("hasSubdivision")) {
					Assert.assertEquals(object, hasSubdivision);
				}
				
				if (predicate.endsWith("hasDivision")) {
					Assert.assertEquals(object, hasDivision);
				}
				
				if (predicate.endsWith("hasInfraclass")) {
					Assert.assertEquals(object, hasInfraclass);
				}

				if (predicate.endsWith("hasSubclass")) {
					Assert.assertEquals(object, hasSubclass);
				}
				
				if (predicate.endsWith("hasClass")) {
					Assert.assertEquals(object, hasClass);
				}
				
				if (predicate.endsWith("hasSuperclass")) {
					Assert.assertEquals(object, hasSuperclass);
				}
				
				if (predicate.endsWith("hasSubphylum")) {
					Assert.assertEquals(object, hasSubphylum);
				}
				
				if (predicate.endsWith("hasPhylum")) {
					Assert.assertEquals(object, hasPhylum);
				}
				
				if (predicate.endsWith("hasKingdom")) {
					Assert.assertEquals(object, hasKingdom);
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);
				}
				
				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(object, hasHabitatText);
				}
				
				if (predicate.endsWith("hasMorphologicalText")) {
					Assert.assertEquals(object, hasMorphologicalText);
				}
			}
		}
	}
}
