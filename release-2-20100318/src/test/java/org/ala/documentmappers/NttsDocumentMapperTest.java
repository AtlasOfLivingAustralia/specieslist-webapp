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

import org.ala.documentmapper.NttsDocumentMapper;
import org.ala.repository.ParsedDocument;
import org.ala.repository.Triple;
import org.ala.util.Response;
import org.ala.util.WebUtils;

/**
 *
 * @author Tommy Wang (twang@wollemisystems.com)
 */
public class NttsDocumentMapperTest extends TestCase {

	final String hasScientificName = "Bettongia lesueur graii";
	final String[] hasConservationStatus = {"Australia:The subspecies Bettongia lesueur graii is Extinct", 
										"Northern Territory: Extinct"};
	final String hasDistributionText = "Burrowing bettongs once lived in burrows excavated in sandy, calcareous and lateritic country over a range " +
			"that encompassed nearly half of the continent, including most of Western Australia (with the exception of the north Kimberley) and " +
			"South Australia, western New South Wales and the Victorian mallee.In the Northern Territory, the burrowing bettong was found " +
			"extensively in the dune and sandplain deserts of the southern arid region. Early naturalists noted that it was common and, in many " +
			"areas, the most abundant mammal.The mainland subspecies is now extinct; however, two subspecies occur on islands off the coast of " +
			"Western Australia; one subspecies on Boodie and Barrow Islands off the Pilbara coast; the other on Bernier and Dorre Islands off " +
			"Shark Bay.Both these subspecies are listed nationally as vulnerable.The decline of this species on the mainland commenced in the " +
			"nineteeth century.It disappeared from Victoria in the 1860s, but persisted in the central and western deserts until the mid " +
			"twentieth century.";
	final String hasDescriptiveText = "The burrowing bettong is a small thickset macropod (body mass 0.9-1.6 kg).It is yellow-grey above and " +
			"paler grey below.The ears are short and rounded, and the tail relatively robust.";
	final String hasThreatsText = "The disappearance of the burrowing bettong from central and western Australia seems to have coincided with " +
			"the establishment of the fox and the feral cat.Interestingly, bettong numbers were probably not seriously affected by rabbits, " +
			"because both species sometimes shared warrens.";
	
	public void testBilby() throws Exception {
		
		String uri ="http://www.nt.gov.au/nreta/wildlife/animals/threatened/pdf/mammals/greater_bilby_vu.pdf";
		Response response = WebUtils.getUrlContentAsBytes(uri);
		byte[] contentBytes = response.getResponseAsBytes();
		NttsDocumentMapper dm = new NttsDocumentMapper();
		List<ParsedDocument> parsedDocs = dm.map(uri, contentBytes);
		for(ParsedDocument pd : parsedDocs){
			DebugUtils.debugParsedDoc(pd);
		}
	}
	
	public void test() throws Exception {
		NttsDocumentMapper dm = new NttsDocumentMapper();
		String uri = "http://www.nt.gov.au/nreta/wildlife/animals/threatened/pdf/mammals/burrowing_bettong_ex.pdf";
//		String uri = "http://www.nt.gov.au/nreta/wildlife/animals/threatened/pdf/mammals/cresttail_mulgara_vu.pdf";
		Response response = WebUtils.getUrlContentAsBytes(uri);
		byte[] contentBytes = response.getResponseAsBytes();
//		System.out.println(new String(contentBytes));
		List<ParsedDocument> parsedDocs = dm.map(uri, contentBytes);
		
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
					Assert.assertTrue(arrayContainsElement(hasConservationStatus, object));
				}

				if (predicate.endsWith("hasDistributionText")) {
					Assert.assertEquals(object, hasDistributionText);
				}

				if (predicate.endsWith("hasDescriptiveText")) {
					Assert.assertEquals(object, hasDescriptiveText);
				}

				if (predicate.endsWith("hasThreatsText")) {
					Assert.assertEquals(object, hasThreatsText);
				}
			}
		}
	}
	
	private boolean arrayContainsElement(String[] strArray, String element) {

		for (int i = 0; i < strArray.length; i++) {
			if (element.equals(strArray[i])) {
				return true;
			}
		}

		return false;
	}
}
