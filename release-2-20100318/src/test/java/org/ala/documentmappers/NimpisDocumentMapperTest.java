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
import org.ala.documentmapper.NimpisDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class NimpisDocumentMapperTest extends TestCase {

	final String hasAuthorReference = "Lutken, 1871";
	final String hasScientificName = "Asterias amurensis";
	final String hasPhylum = "Echinodermata";
	final String hasSubPhylum = "Asterozoa";
	final String hasClass = "Asteroidea";
	final String hasOrder = "Forcipulatida";
	final String hasFamily = "Asteriidae";
	final String hasGenus = "Asterias";
	final String hasMinimumLength = "36 mm";
	final String hasMaximumLength = "500 mm";
	final String hasPestStatus = "Known introduction to Australia";
	final String hasDistributionMapPageUrl = "http://adl.brs.gov.au/marinepests/index.cfm?fa=main.distribution&sp=6000005721&sn=Asterias%20amurensis";
    
	final String[] hasSynonym = {	"Allasterias anomala", "Allasterias rathbuni", "Allasterias rathbuni nortonens", 
    						"Allasterias rathbuni var. anom", "Allasterias rathbuni var. nort", "Asterias amurensis f. acervispinis", 
    						"Asterias amurensis f. flabellifera", "Asterias amurensis f. gracilispinis", "Asterias amurensis f. latissima",
    						"Asterias amurensis f. robusta", "Asterias anomala", "Asterias nortonensis", 
    						"Asterias rubens", "Parasterias albertensis"};
    
	final String[] hasCommonName = {	"northern Pacific seastar", "Japanese starfish", "flatbottom seastar",
    							"north Pacific seastar", "purple-orange seastar", "Japanese seastar"};
    	    
	final String[] hasSimilarSpecies = { 	"Uniophora granifera", "Uniophora dyscrita", "Pisaster brevispinus", 
    								"Pisaster giganteus", "Pisaster ochraceus"}; 
    
	final String[] habitatTypeSubstrate = {"Coarse-Sand", "Silt"};
	final String[] habitatTypeTidalRange = {"High-Tide", "Low-Tide", "Mid-Tide", "Sub-Tidal"};
	final String[] vectorsForIntroduction = {"Vessels", "Natural dispersal"};
	
	public void test() throws Exception {
		NimpisDocumentMapper dm = new NimpisDocumentMapper();
		String uri = "http://adl.brs.gov.au/marinepests/index.cfm?fa=main.spDetailsDB&sp=6000005721";
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

				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);
				}
				
				if (predicate.endsWith("hasAuthorReference")) {
					Assert.assertEquals(object, hasAuthorReference);
				}
				
				if (predicate.endsWith("hasPhylum")) {
					Assert.assertEquals(object, hasPhylum);
				}
				
				if (predicate.endsWith("hasSubPhylum")) {
					Assert.assertEquals(object, hasSubPhylum);
				}
				
				if (predicate.endsWith("hasClass")) {
					Assert.assertEquals(object, hasClass);
				}
				
				if (predicate.endsWith("hasOrder")) {
					Assert.assertEquals(object, hasOrder);
				}
				
				if (predicate.endsWith("hasGenus")) {
					Assert.assertEquals(object, hasGenus);
				}
				
				if (predicate.endsWith("hasMinimumLength")) {
					Assert.assertEquals(object, hasMinimumLength);
				}
				
				if (predicate.endsWith("hasMaximumLength")) {
					Assert.assertEquals(object, hasMaximumLength);
				}
				
				if (predicate.endsWith("hasPestStatus")) {
					Assert.assertEquals(object, hasPestStatus);
				}
				
				if (predicate.endsWith("habitatTypeSubstrate")) {
					Assert.assertTrue(arrayContainsElement(habitatTypeSubstrate,object));
				}
				
				if (predicate.endsWith("habitatTypeTidalRange")) {
					Assert.assertTrue(arrayContainsElement(habitatTypeTidalRange,object));
				}
				
				if (predicate.endsWith("vectorsForIntroduction")) {
					Assert.assertTrue(arrayContainsElement(vectorsForIntroduction,object));
				}
				
				if (predicate.endsWith("hasSynonym")) {
					Assert.assertTrue(arrayContainsElement(hasSynonym,object));
				}
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertTrue(arrayContainsElement(hasCommonName,object));
				}
				
				if (predicate.endsWith("hasSimilarSpecies")) {
					Assert.assertTrue(arrayContainsElement(hasSimilarSpecies,object));
				}
			}
		}
	}
	
	// Check Whether a String array contains a String element 
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
