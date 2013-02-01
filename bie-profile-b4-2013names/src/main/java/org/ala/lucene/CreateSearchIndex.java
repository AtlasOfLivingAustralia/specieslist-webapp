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

import org.ala.dao.GeoRegionDao;
import org.ala.dao.TaxonConceptDao;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * Creates a SOLR index for the taxon concepts and regions.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class CreateSearchIndex {

	protected static Logger logger = Logger.getLogger(CreateSearchIndex.class);
	
	public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringUtils.getContext();
        if(args.length==0 || "-geo".equals(args[0])){
	        logger.info("Loading geo regions into search indexes....");
	        GeoRegionDao grDao = (GeoRegionDao) context.getBean(GeoRegionDao.class);
	        grDao.createIndex();
	        logger.info("Finished loading geo regions into search indexes.");
        }
        if(args.length==0 || "-taxa".equals(args[0])){
	        logger.info("Creating species indexes...");
	        TaxonConceptDao tcDao = (TaxonConceptDao) context.getBean(TaxonConceptDao.class);
	        boolean replace = args.length >1 && "-replace".equals(args[1]);
	        String start = "";
	        if(args.length>0 && !args[0].startsWith("-"))
	            start = args[0];
	        if(args.length>1 && !args[1].startsWith("-"))
	            start = args[1];
	        if(args.length>2 && !args[2].startsWith("-"))
	            start = args[2];
	        tcDao.createIndex(start, !replace);
	        logger.info("Finished creating species indexes.");
        }
		System.exit(0);
	}
}
