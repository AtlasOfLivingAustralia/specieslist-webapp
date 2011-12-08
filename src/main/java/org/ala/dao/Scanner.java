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
package org.ala.dao;

import java.util.List;
import java.util.Map;

/**
 * A scanner allows full table scans over the backend storage, row by row.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Scanner {

	/**
	 * Get the next guid. Return null if scan is complete.
	 * 
	 * @return guid as byte[], null if end of table reached
	 * @throws Exception
	 */
	byte[] getNextGuid() throws Exception;
	
	/**
	 * Retrieves the current values for the record in the scanner.
	 * Call after getNextGuid to retrieve the values associated with the current guid.
	 * 
	 * @return
	 * @throws Exception
	 */
	//Map<String,String> getCurrentValues() throws Exception;
	
	/**
	 * Retrieves the current value for the specified column.  Will return null if 
	 * the column does not exist OR was not requested in the scan.
	 * 
	 * @param column
	 * @param theClass
	 * @return
	 * @throws Exception
	 */
	Comparable getValue(String column, Class theClass)throws Exception;
	
	List<Comparable> getListValue(String column, Class theClass)throws Exception;
}
