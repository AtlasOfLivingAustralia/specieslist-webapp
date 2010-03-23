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
import org.ala.documentmapper.EpbcDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class EpbcDocumentMapperTest extends TestCase {

	final String hasScientificName = "Rheobatrachus silus [1909]";
	final String hasFamily = "Myobatrachidae";
	final String hasCommonName = "Southern Gastric-brooding Frog";
	final String hasConservationStatus = "Listed as Extinct";
	final String hasDietText = "The Southern Gastric-brooding Frog was observed to forage and take insects from both land and water (Ingram 1983). " +
			"In an aquarium situation Lepidoptera , Diptera and Neuroptera were eaten (Liem 1973).";	
	final String hasPopulateEstimate = "The Southern Gastric-brooding Frog underwent a decline in winter 1979 (Czechura & Ingram 1990; Tyler & " +
			"Davies 1985) and the last sighting occurred September 1981 in the Blackall Range (Richards et al. 1993). Ingram (1983) studied a " +
			"population of the species in the headwaters of Booloumba Creek, Conondale Range, and estimated that approximately 78 were present " +
			"in 1976. No other estimates of population size are available for the species. The last known specimen died in captivity in November " +
			"1983 (Tyler & Davies 1985). Searches have continued unsuccessfully, most recently in November 1999 (Ingram & McDonald 1993). Other " +
			"frog species that have declined in south-east Queensland since the 1970s include the Southern Day Frog ( Taudactylus diurnus : " +
			"extinct), Fleay's Frog ( Mixophyes fleayi : endangered) and the Southern Barred Frog ( Mixophyes iteratus : endangered) (Ingram & " +
			"McDonald 1993).";
	final String hasDistributionText = "The Southern Gastric-brooding Frog has not been recorded in the wild since 1981 (Richards et al. 1993). " +
			"The Southern Gastric-brooding Frog was restricted to elevations between 400800 m in the Blackall and Conondale Ranges in south-east " +
			"Queensland (Sunshine Coast Hinterland) (Hines et al. 1999). Distributions occurred between Coonoon Gibber Creek and Kilcoy " +
			"Creek (Hines et al. 1999) in streams in the catchments of the Mary River, Stanley River and Mooloolah River (Ingram 1983). " +
			"The species was thought to have been first discovered in 1972 (Liem 1973) but Ingram (1991) reported a specimen collected in " +
			"1914 from the Blackall Range. The geographic distribution of the Southern Gastric-brooding Frog was less than 1000 km² (Hines " +
			"et al. 1999). The Southern Gastric-brooding Frog has been collected from Kondalilla National Park, Conondale National Park, " +
			"Sunday Creek, State Forest 311, Kenilworth State Forest and from private land adjacent to these areas (Hines et al. 1999).";
	final String hasDescriptiveText = "The Southern Gastric-brooding Frog was a moderately large, aquatic frog, with males 3044 mm in size " +
			"and females 4154 mm (Ingram 1983; Tyler & Davies 1983a). The dorsal surface was brown, or olive-brown to almost black, usually " +
			"with obscure darker blotches on the back. A dark streak ran from the eye to base of the forelimb. There were darker cross-bars " +
			"on the limbs, and pale and dark blotches and variegations on the digits and webbing. The ventral surface was white or cream with " +
			"yellow markings on the limbs. The skin was finely granular above and smooth below (Cogger 2000; Liem 1973; Tyler & Davies 1983a). " +
			"The Southern Gastric-brooding Frog's snout was blunt and rounded, with the eyes and nostrils directed upwards. The species' eyes " +
			"were large and prominent, located close together and close to the front of the head. The tongue was largely adherent to the floor " +
			"of mouth. The typanum (ear cavity) was hidden. The fingers lacked webbing, while the toes were fully webbed. Digits had small discs " +
			"(Cogger 2000; Liem 1973; Tyler & Davies 1983a).";
	final String hasHabitatText = "Habitat for the Southern Gastric-brooding Frog was permanent to ephemeral freshwater streams over 300 m in " +
			"altitude, in rainforest and wet sclerophyll forest communities of Blackall Range, Conondale Range and D'Aguilar Range (Hines & " +
			"SEQTFRT 2002). The Southern Gastric-brooding Frog was an aquatic species and was never located more than four metres from water. " +
			"This species was restricted to rocky perennial streams, soaks and pools in rainforest and tall open forest with a closed understorey. " +
			"It preferred rock pools and backwaters with leaf litter and rocks in which to shelter (Ingram 1983). The Southern Gastric-brooding " +
			"Frog was most active during the warmer months (SeptemberApril), with abundance decreasing as conditions become drier in winter (Ingram " +
			"1983). It is not known where these individuals went during this period, but it is believed they hibernated in deep crevices in the " +
			"rocks (Ingram 1983; Liem 1973). Individuals were active night or day, particularly after rain. They established home ranges in and " +
			"around suitable pools, spending extended periods partly submerged and immobile. When heavy rain fell, the males moved away from the " +
			"water and called from sheltered hollows or crevices above the pools (Ingram 1983).";
	final String hasThreatsText = "The reason(s) for the decline of the Southern Gastric-brooding Frog remains unknown (Tyler & Davies 1985). " +
			"Populations were present in logged catchments in 19721979. Although the species persisted in the streams during these activities, " +
			"the effects of timber harvesting on this aquatic species were never investigated. Its habitat is currently threatened by feral " +
			"Pigs ( Sus scrofa ), invasion of weeds (especially Mistflower ( Ageratina riparia )), and altered flow and water quality due to " +
			"upstream disturbances (Hines et al. 1999). Recent studies of amphibian disease have identified a chytrid fungus ( Batrachochytrium " +
			"dendrobatidis ) as a cause of frog mortality and as the cause of death of frogs collected during declines (Berger et al. 1998, " +
			"1999). The investigation of the role played by chytrid fungus in the decline of the Southern Gastric-brooding Frog is a focus of " +
			"the species recovery program (Hines & SEQTFRT 2002).";
	
	public void test() throws Exception {
		EpbcDocumentMapper dm = new EpbcDocumentMapper();
		String uri = "http://www.environment.gov.au/cgi-bin/sprat/public/publicspecies.pl?taxon_id=1909";
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
				
				if (predicate.endsWith("hasConservationStatus")) {
					Assert.assertEquals(object, hasConservationStatus);		    	  
				}
				
				if (predicate.endsWith("hasCommonName")) {
					Assert.assertEquals(object, hasCommonName);		    	  
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
				
				if (predicate.endsWith("hasThreatsText")) {
					Assert.assertEquals(object, hasThreatsText);		    	  
				}
				
				if (predicate.endsWith("hasDietText")) {
					Assert.assertEquals(object, hasDietText);		    	  
				}
				
				if (predicate.endsWith("hasPopulateEstimate")) {
					Assert.assertEquals(object, hasPopulateEstimate);		    	  
				}
			}
		}
	}
}
