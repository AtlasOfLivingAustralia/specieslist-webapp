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

import org.ala.model.GeoRegion;
/**
 * A DAO interface for Geographic regions.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface GeoRegionDao {

	/**
	 * Store the following taxon concept
	 *
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	boolean create(GeoRegion geoRegion) throws Exception;
	
	/**
	 * Retrieve Geo region details by guid.
	 * 
	 * @param guid
	 * @return
	 * @throws Exception
	 */
	GeoRegion getByGuid(String guid) throws Exception;
	
	/**
	 * Retrieve a full listing of geographic regions.
	 * 
	 * @return
	 * @throws Exception
	 */
	List<GeoRegion> getAll() throws Exception;
	
	/**
	 * Retrieve a full listing of geographic regions for the specified region type
	 * 
	 * @param regionType
	 * @return
	 * @throws Exception
	 */
	List<GeoRegion> getByType(String regionType) throws Exception;
}