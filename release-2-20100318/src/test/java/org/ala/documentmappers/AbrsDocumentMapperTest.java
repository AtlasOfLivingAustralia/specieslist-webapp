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
import org.ala.documentmapper.AbrsDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Predicates;
import org.ala.repository.Triple;
import org.ala.util.WebUtils;

/**
 * A Junit test for the ABRD document mapper.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class AbrsDocumentMapperTest extends TestCase {

	final String hasScientificName = "Idanthyrsus pennatus";
	final String hasFamily = "Sabellariidae";
	final String hasDistributionText = "The Feather Tubeworm has an eastern and southern temperate Australian distribution. It ranges " +
			"from south-eastern Qld, around southern Australia, including NSW, Vic, Tas and SA to south-eastern WA (QLD, NSW, VIC, TAS, " +
			"SA, WA).";
	final String hasDescriptiveText = "The Feather Tubeworm constructs a sand-grain tube which may be single, or in vast, dense colonies. " +
			"The tubes of the Feather Tubeworm are irregular in form and very hard in consistency because they are composed of cemented " +
			"sand grains and shell particles. Inside the tube, the worm has a body that is up to 50 mm long. Its body is divided into " +
			"four segments. At the tube opening the worm has an operculum crown consisting of a pair of golden-coloured plumes that are " +
			"called peleae. The outer row resembles palm-leaves, while the inner row is smooth with tapering tips. If the worm is extracted " +
			"from its tube and examined with a lens, it has three large fin-like feet, called parapodia, each with a row of bristles " +
			"called setae on the sides of the third, fourth and fifth segments. Bright red coloured gills extend from the upper surface " +
			"of a long row of about 30 body segments.";
	final String hasEcologicalText = "The Feather Tubeworm is usually found in single tubes constructed under stones at low water mark on " +
			"rocky shores around southern Australia. North of Coffs Harbour, there is a tendency to form large, thick communities consisting " +
			"of a cemented together mass of Idanthyrsus pennatus tubes. This is called worm-coral. Sabellid worms are suspension feeders.";
	final String hasThreatsText = "The Feather Tubeworm is widespread and forms dense colonies in the north-eastern part of its range. It " +
			"probably isn't under threat from human activities.";
	
	public void test() throws Exception {
		AbrsDocumentMapper dm = new AbrsDocumentMapper();
		String uri = "http://www.environment.gov.au/cgi-bin/species-bank/sbank-treatment.pl?id=77198";
		String xml = WebUtils.getHTMLPageAsXML(uri);
		List<ParsedDocument> parsedDocs = dm.map(uri, xml.getBytes());
		for(ParsedDocument pd : parsedDocs){
			
			DebugUtils.debugParsedDoc(pd);
			
			List<Triple<String,String,String>> triples = pd.getTriples();
			for(Triple triple: triples){
				
				String predicate = (String) triple.getPredicate();
				String object = (String) triple.getObject();
				
				if (predicate.equals(Predicates.SCIENTIFIC_NAME.toString())) {
					Assert.assertEquals(hasScientificName,object);
				}

				if (predicate.equals(Predicates.FAMILY.toString())) {
					Assert.assertEquals(hasFamily,object);
				}
				
				if (predicate.equals(Predicates.DISTRIBUTION_TEXT.toString())) {
					Assert.assertEquals(hasDistributionText,object);
				}
				
				if (predicate.equals(Predicates.DESCRIPTIVE_TEXT.toString())) {
					Assert.assertEquals(hasDescriptiveText,object);
				}
				
				if (predicate.equals(Predicates.ECOLOGICAL_TEXT.toString())) {
					Assert.assertEquals(hasEcologicalText,object);
				}
				
				if (predicate.equals(Predicates.THREATS_TEXT.toString())) {
					Assert.assertEquals(hasThreatsText,object);
				}
			}
		}
	}
}
