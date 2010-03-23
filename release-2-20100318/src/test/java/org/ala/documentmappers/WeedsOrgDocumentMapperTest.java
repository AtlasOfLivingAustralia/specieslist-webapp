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
import org.ala.documentmapper.WeedsOrgDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class WeedsOrgDocumentMapperTest extends TestCase {

	final String hasScientificName = "Lycium ferocissimum";
	final String hasMorphologicalText = "Flowers: Singly or in pairs at the leaf-stem junction. White with purplish throat, about 1 cm " +
			"diameter; 5-petalled; fragrant. Flowers to 12 mm long with male part of the flower (stamen) projecting to 4 mm past the " +
			"petals. Flowers mostly summer but some flowering throughout year.";
	final String hasDescriptiveText = "Much branched shrub to 6 m high. Leaves fleshy, elliptic to 4 cm long (see photo). Berry to 1 cm " +
			"wide on short drooping stalk. Seeds 2.5 mm long, dull yellow.";
	final String hasReproductionText = "Spread by seed. Fruit is commonly eaten by foxes and birds and viable seeds are excreted. Often " +
			"forms dense stands as a result of these animals feeding and remaining in the vicinity of fruiting boxthorn. Shoots readily " +
			"from broken roots.";
	final String hasFamily = "Solanaceae.";
	
	public void test() throws Exception {
		WeedsOrgDocumentMapper dm = new WeedsOrgDocumentMapper();
		String uri = "http://www.weeds.org.au/cgi-bin/weedident.cgi?tpl=plant.tpl&state=&s=&region=all&card=S10";
		String xml = WebUtils.getHTMLPageAsXML(uri);
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
				
				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);		    	  
				}
				
				if (predicate.endsWith("hasMorphologicalText")) {
					Assert.assertEquals(object, hasMorphologicalText);		    	  
				}
				
				if (predicate.endsWith("hasReproductionText")) {
					Assert.assertEquals(object, hasReproductionText);		    	  
				}
				
				if (predicate.endsWith("hasFamily")) {
					Assert.assertEquals(object, hasFamily);		    	  
				}
			}
		}
	}
	
}
