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
}
