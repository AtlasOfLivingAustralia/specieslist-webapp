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
package org.ala.lucene;

import org.ala.util.LoadUtils;
import org.apache.log4j.Logger;

/**
 * Create a simple lucene index for Taxon Concept GUID lookups
 * to assist in the loading of HBase rows. 
 * 
 * These created indexes are for ANBG data loading purposes only.
 * 
 * @author Dave Martin
 */
public class CreateLoadingIndex {

	protected static Logger logger = Logger.getLogger(CreateLoadingIndex.class);
	
	/**
	 * Run this to create the index.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		logger.info("Starting the creation of temporary loading indicies...");
		LoadUtils l = new LoadUtils();
		logger.info("Loading relationships...");
		l.loadRelationships();
		logger.info("Loading taxon concepts...");
		l.loadTaxonConcepts();
		logger.info("Loading accepted concepts...");
		l.loadAccepted();
		logger.info("Loading publications...");
		l.loadPublications();
		logger.info("Loading complete.");
	}
}
