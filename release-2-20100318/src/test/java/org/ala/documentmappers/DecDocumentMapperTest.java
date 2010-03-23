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
import org.ala.documentmapper.DecDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class DecDocumentMapperTest extends TestCase {

	final String hasScientificName = "Acacia acanthoclada";
	final String hasCommonName = "Harrow Wattle";
	final String hasConservationStatus = "Conservation status in NSW: Endangered";
	final String hasDistributionText = "Found in the Buronga, Wentworth and Pooncarie districts in far south-western NSW, where it is uncommon. " +
			"Also has a limited distribution in Victoria, SA and WA. Sites include Arumpo Station (adjacent to Arumpo Bentonite mine) and Montarna " +
			"Station.";
	final String hasDescriptiveText = "Acacia acanthoclada was first described as a rigid, divaricate and spinescent shrub with small rigid phyllodes " +
			"which are narrow-cuneate and slightly notched at the apex (Bentham 1863-78). The Flora of NSW describes Acacia acanthoclada as an erect " +
			"or spreading shrub, 0.3-1.5 m high; bark smooth, grey or occasionally slightly greenish; branchlets ± terete, spinose, densely hairy. " +
			"Phyllodes ± straight, 0.2-0.6 cm long, 1-2 mm wide, midvein prominent, lateral veins sometimes conspicuous, apex acute to obtuse with a " +
			"mucro, hairy; glands absent; pulvinus < 2 mm long. Heads 20-35-flowered, golden yellow, 1 in axil of phyllodes; peduncle 2-8 mm long. Pod " +
			"twisted or coiled, ± flat, 3-6 cm long, 3-6 mm wide, brown, glaucous; seeds longitudinal; funicle expanded towards seed (Morrison & Davies " +
			"1991). In western NSW, Acacia acanthoclada plants are also described as low rigid shrubs with downy whitish branches and hard spiny branchlets " +
			"(Cunningham et al. 1992).";
	final String hasHabitatText = "Grows in mallee communities on ridges and dunes and very occasionally on rocky outcrops; generally grows in deep, " +
			"loose, sandy soil. Associated species include Eucalyptus dumosa, E. socialis, E. gracilis, E. costata, Callitris verrucosa, Codonocarpus " +
			"cotinifolius and Triodia scariosa . Flowers from August to October. Grows from seed, requiring a warm, well-drained position in full sun " +
			"or a little shade; grows well in well-drained sandy or loamy soils but will tolerate some clay and is considered to be quite long-lived " +
			"as plants can be very deeply rooted. Plants have been recorded in or adjacent to areas regenerating after fire. A population located at " +
			"Arumpo Station is 3 - 4 hectares in size and comprises stunted and very woody plants; plants also noted as scattered, occasional and very " +
			"sparse in populations.";
	final String hasThreatsText = "Clearing is a major threat, with many populations located near roads and in mallee areas that are being cleared. " +
			"The population at Arumpo Station is suffering overgrazing, mainly from kangaroos. Lack of knowledge and information about this species' " +
			"ecology, particularly seedling recruitment, life cycle and lifespan. Remaining populations in NSW are very fragmented due to the paucity " +
			"of available, non-degraded habitat; requires habitats with deep sandy soils in undisturbed mallee areas.";
	
	public void test() throws Exception {
		DecDocumentMapper dm = new DecDocumentMapper();
		String uri = "http://www.threatenedspecies.environment.nsw.gov.au/tsprofile/profile.aspx?id=10001";
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
			}
		}
	}
}
