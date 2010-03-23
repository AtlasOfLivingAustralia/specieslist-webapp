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
import org.ala.documentmapper.WikipediaDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class WikipediaDocumentMapperTest extends TestCase {

	final String hasScientificName = "Acacia acanthoclada";
	final String hasKingdom = "Plantae";
	final String hasDivision = "Magnoliophyta";
	final String hasClass = "Magnoliopsida";
	final String hasOrder = "Fabales";
	final String hasFamily = "Fabaceae";
	final String hasSubFamily = "Mimosoideae";
	final String hasGenus = "Acacia";
	final String hasSpecies = "A. acanthoclada";
	final String hasDistributionText = "The species is relatively uncommon and is found scattered at several sites in isolated clumps: Buronga , " +
			"Wentworth and Pooncarie districts in far south-western New South Wales , Victoria , South Australia and Arumpo Station and Montarna " +
			"Station in Western Australia . Arumpo Station in particular is home to a stunted sample, which is threantened by overgrazing by " +
			"kangaroos .";
	final String hasDescriptiveText = "It grows up to 2 metres high and has phyllodes which measure 0.2 to 0.6 cm long and 1 to 2 mm wide. The " +
			"phyllodes are straight, narrow-cuneate, slightly notched at the apex, and feature prominent midveins. Branchlets are terete, whitish " +
			"and densely pubescent; as the branch grows it becomes glabrous and terminates in rigid spinose point. The bark is grey, white or " +
			"occasionally greenish. The golden-yellow flowerheads are peduncles , 5–15 cm long, that appear at phyllode axils . Flower parts are " +
			"pentamerous, with the sepals fused into a synsepalous calyx . Flowers appear from August to October, followed by irregularly twisted, " +
			"glaucous, brown seed pods which are 3 to 6 cm long and 3 to 6 mm wide. Its occurs naturally in Western Australia , South Australia " +
			"and Victoria and is listed as endangered under the Threatened Species Conservation Act in New South Wales . The type was collected " +
			"near Kulkyne, Victoria by Ferdinand von Mueller .";
	final String hasHabitatText = "This species usually grows on deep, loose, sandy soil. Its inhabits undisturbed mallee areas, often on ridges " +
			"and dunes , and more rarely on rock outcrops.";
	final String hasReference = "Australian National Botanic Gardens: Photo of Acacia acanthoclada \" Acacia acanthoclada \" . FloraBase . " +
			"Department of Environment and Conservation , Government of Western Australia . http://florabase.dec.wa.gov.au/browse/profile/3195 ." +
			" NSW National Parks and Wildlife Service: Acacia acanthoclada - endangered species listing \" Acacia acanthoclada F. Muell.\" . " +
			"Australian Plant Name Index (APNI), IBIS database . Centre for Plant Biodiversity Research, Australian Government . " +
			"http://www.anbg.gov.au/cgi-bin/apni?taxon_id=3354 .";
	
	public void test() throws Exception {
		WikipediaDocumentMapper dm = new WikipediaDocumentMapper();
		String uri = "http://en.wikipedia.org/wiki/Acacia_acanthoclada";
		
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.endsWith("hasScientificName")) {
					Assert.assertEquals(object, hasScientificName);
				}

				if (predicate.endsWith("hasKingdom")) {
					Assert.assertEquals(object, hasKingdom);
				}
				
				if (predicate.endsWith("hasDivision")) {
					Assert.assertEquals(object, hasDivision);
				}
				
				if (predicate.endsWith("hasClass")) {
					Assert.assertEquals(object, hasClass);
				}
				
				if (predicate.endsWith("hasOrder")) {
					Assert.assertEquals(object, hasOrder);
				}
				
				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);
				}
				
				if (predicate.endsWith("hasSubFamily")) {
					Assert.assertEquals(object, hasSubFamily);
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);
				}
				
				if (predicate.endsWith("hasSpecies")) {
					Assert.assertEquals(object, hasSpecies);
				}
				
				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);
				}
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);
				}
				
				if (predicate.endsWith("hasHabitatText")) {
					Assert.assertEquals(object, hasHabitatText);
				}
				
				if (predicate.endsWith("hasReference")) {
					Assert.assertEquals(object, hasReference);
				}
			}
		}
	}

	public void testEasternBearded() throws Exception {
		
		// http://en.wikipedia.org/wiki/Blue-billed_Duck
		WikipediaDocumentMapper dm = new WikipediaDocumentMapper();
		String uri = "http://en.wikipedia.org/wiki/Eastern_bearded_dragon";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
		}
	}
	
	public void testTassieDevil() throws Exception {
		
		// http://en.wikipedia.org/wiki/Blue-billed_Duck
		WikipediaDocumentMapper dm = new WikipediaDocumentMapper();
		String uri = "http://en.wikipedia.org/wiki/Tasmanian_Devil";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
		}
	}
	
	public void testDuckBilled() throws Exception {
		
		// http://en.wikipedia.org/wiki/Blue-billed_Duck
		WikipediaDocumentMapper dm = new WikipediaDocumentMapper();
		String uri = "http://en.wikipedia.org/wiki/Blue-billed_Duck";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
		}
	}
}
