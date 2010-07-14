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

import org.ala.dto.ExtendedGeoRegionDTO;
import org.ala.model.GeoRegion;
import org.ala.model.TaxonConcept;
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
	
	ExtendedGeoRegionDTO getExtendedGeoRegionByGuid(String guid) throws Exception;
	
	/**
	 * Create a index to support searching.
	 *
	 * @throws Exception
	 */
	void createIndex() throws Exception;

	boolean addBirdEmblem(String stateGuid, TaxonConcept tc) throws Exception;
	boolean addPlantEmblem(String stateGuid, TaxonConcept tc) throws Exception;
	boolean addAnimalEmblem(String stateGuid, TaxonConcept tc) throws Exception;
}