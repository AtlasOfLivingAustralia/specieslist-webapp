/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
package org.ala.util;

import junit.framework.TestCase;

public class LoadUtilsTest extends TestCase {

	public void testVernacularName() throws Exception {
		LoadUtils loadUtils = new LoadUtils();
		boolean isVernacular = loadUtils.isVernacularConcept("urn:lsid:biodiversity.org.au:afd.taxon:3b915d97-2376-4c40-bd04-7ae0acbaa34b");
		assertTrue(isVernacular);
	}

	public void testCongruentConcept() throws Exception {
		LoadUtils loadUtils = new LoadUtils();
		boolean isCongruent = loadUtils.isCongruentConcept("urn:lsid:biodiversity.org.au:afd.taxon:6c4f09b8-f44c-4ea5-9aa6-1943894e6ae5");
		assertTrue(isCongruent);
		isCongruent = loadUtils.isCongruentConcept("urn:lsid:biodiversity.org.au:afd.taxon:558a729a-789b-4b00-a685-8843dc447319");
		assertFalse(isCongruent);
	}
}
