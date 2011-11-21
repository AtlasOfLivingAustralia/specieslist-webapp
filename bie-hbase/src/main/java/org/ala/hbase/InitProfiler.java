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
package org.ala.hbase;

import org.ala.dao.SystemDao;
import org.ala.dao.SystemDaoImpl;
import org.apache.log4j.Logger;

/**
 * Initialise a HBase instance.
 */
public class InitProfiler {
	
	protected static Logger logger = Logger.getLogger(InitProfiler.class);
	
	public static void main( String[] args ) throws Exception {
		logger.info("Initialise profiler...");
		SystemDao systemDao = new SystemDaoImpl();
		systemDao.init();
		logger.info("Initialise completed.");
//		System.exit(1);
    }
}
