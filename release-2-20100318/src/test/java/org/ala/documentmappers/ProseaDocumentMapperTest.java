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
import org.ala.documentmapper.ProseaDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class ProseaDocumentMapperTest extends TestCase {
	
	final String hasScientificName = "Cajanus cajan (L.) Millsp.";
	final String hasDistributionText = "Pigeon pea originated in India and spread to South-East Asia in the early centuries of our era. " +
			"It reached Africa 2000 BC or earlier, and found its way to the Americas with the conquests and slave trade, probably through " +
			"both the Atlantic and the Pacific. It is now grown all over the tropics but especially in the Indian Subcontinent and East Africa.";
	final String hasDescriptiveText = "A glandular-pubescent, short-lived perennial (1-5 years) shrub, usually grown as an annual, 0.5-4 m high, " +
			"with thin roots up to 2 m deep. Stems up to 15 cm in diameter. Branches many, slender. Leaves alternate, trifoliolate, glandular " +
			"punctate; leaflets elliptical, 3-13.7 cm x 1.3-5.7 cm. Flowers in pseudoracemes, sometimes concentrated and synchronous (determinate), " +
			"usually scattered and flowering over a long period (indeterminate), papilionaceous, corolla yellow or cream, standard dorsally red, " +
			"orange or purple. Fruit a straight or sickle-shaped pod with (2-)4-9 globose to ellipsoid or squarish seeds. Seeds white, cream, brown, " +
			"purplish to almost black, plain or mottled; strophiole usually virtually absent. Seedlings with hypogeal germination; first leaves simple.";
	final String hasEcologicalText = "Flowering is triggered by short days and plants grow vegetatively with long days, as in the rainy season of India. " +
			"There are few truly day-neutral forms. Optimum temperatures range from 18 to 38 °C, frost is not tolerated. Above 29 °C, soil moisture and " +
			"fertility need to be adequate. Rainfall optimum is 600-1000 mm/year, waterlogging is harmful. Pigeon pea is rarely found above altitude 2000 " +
			"m. Drained soils of reasonable water-holding capacity and with pH 5-7 or more are favourable. The plant tolerates an electrical conductivity " +
			"(salinity) from 0.6 to 1.2 S/m.";
	//final String hasImageUrl = "http://proseanet.org/prosea/_images/proseabase/000502.jpg";
	final String[] hasSynonym = {"Cytisus cajan L. (1753)",
								"Cajanus indicus Spreng. (1826)."};
	final String[] hasCommonName = {"Pigeon pea (En)",
								"Pois d'Angole",
								"ambrévade (Fr)",
								"cay dau chieu",
								"dau sang",
								"dau thong",
								"kacang",
								"kacang Bali",
								"kacang dal",
								"kacang gude",
								"kacang hiris",
								"kacang kayu",
								"kardis",
								"kidis",
								"ma hae",
								"sândaèk dai",
								"sândaèk klöng",
								"sândaèk kroëb sâ",
								"tabios",
								"thua maetaai",
								"thua rae",
								"thwàx h'ê"};	
	
	public void test() throws Exception {
		ProseaDocumentMapper dm = new ProseaDocumentMapper();
		String uri = "http://proseanet.org/prosea/e-prosea_detail.php?frt=&id=133";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		//System.out.println(xml);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
//				if (predicate.endsWith("hasScientificName")) {
//					Assert.assertEquals(object, hasScientificName);		    	  
//				}
//
//				if (predicate.endsWith("hasCommonName")) {
//					Assert.assertTrue(arrayContainsElement(hasCommonName,object));		    	  
//				}
//
//				if (predicate.endsWith("hasEcologicalText")) {
//					Assert.assertEquals(object, hasEcologicalText);		    	  
//				}
//
//				if (predicate.endsWith("hasSynonym")) {
//					Assert.assertTrue(arrayContainsElement(hasSynonym,object));		    	  
//				}
//				
//				if (predicate.endsWith("hasDescriptiveText")) {
//					Assert.assertEquals(object, hasDescriptiveText);		    	  
//				}
//				
//				if (predicate.endsWith("hasDistributionText")) {
//					Assert.assertEquals(object, hasDistributionText);		    	  
//				}
				
				
				
			}			
		}
	}
	
	// Check Whether a String array cotains a String element 
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
