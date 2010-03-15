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
package org.ala.dto;

import java.util.List;

import org.ala.model.Region;

/**
 * A DTO that encapsulates regional occurrences of a taxon concept grouped by region type.
 * 
 * @author Peter Flemming (peter.flemming@csiro.au)
 */
public class RegionTypeDTO {

	protected String regionType;
	protected String occurrencesInRegionType;
	protected List<Region> regions;
	
	/**
	 * @return the regionType
	 */
	public String getRegionType() {
		return this.regionType;
	}
	/**
	 * @param regionType the regionType to set
	 */
	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}
	/**
	 * @return the occurrencesInRegionType
	 */
	public String getOccurrencesInRegionType() {
		return this.occurrencesInRegionType;
	}
	/**
	 * @param occurrencesInRegionType the occurrencesInRegionType to set
	 */
	public void setOccurrencesInRegionType(String occurrencesInRegionType) {
		this.occurrencesInRegionType = occurrencesInRegionType;
	}
	/**
	 * @return the regions
	 */
	public List<Region> getRegions() {
		return this.regions;
	}
	/**
	 * @param regions the regions to set
	 */
	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

}
