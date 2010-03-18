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
package org.ala.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ala.dto.RegionTypeDTO;


/**
 * Simple POJO to represent a geographical region for a taxon
 * 
 * @author Peter Flemming (peter.flemming@csiro.au)
 */
public class Region extends AttributableObject implements Comparable<Region> {

	protected String taxonId;
	protected String regionId;
	protected String regionName;
	protected String regionType;
	protected int occurrences;
	
	/**
	 * @param regionId
	 * @param regionName
	 * @param regionType
	 * @param occurrences
	 */
	public Region(String taxonId, String regionId, String regionName,
			String regionType, int occurrences) {
		super();
		this.taxonId = taxonId;
		this.regionId = regionId;
		this.regionName = regionName;
		this.regionType = regionType;
		this.occurrences = occurrences;
	}

	/**
	 * @return the regionId
	 */
	public String getRegionId() {
		return this.regionId;
	}

	/**
	 * @param regionId the regionId to set
	 */
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	/**
	 * Default constructor
	 */
	public Region() {
		super();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.regionName;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.regionName = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return this.regionType;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.regionType = type;
	}

	/**
	 * @return the occurrences
	 */
	public int getOccurrences() {
		return occurrences;
	}

	/**
	 * @param occurrences
	 *            the occurrences to set
	 */
	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}

	/**
	 * Creates a list of region lists grouped by region type.
	 * 
	 * @param regions
	 * @return List of RegionTypeDTOs
	 */
	public static List<RegionTypeDTO> getRegionsByType(List<Region> regions) {
		Map<String, RegionTypeDTO> regionMap = new TreeMap<String, RegionTypeDTO>();
		
		for (Region region : regions) {
			RegionTypeDTO regionType;
			if (regionMap.containsKey(region.getType())) {
			    regionType = regionMap.get(region.getType());
			} else {
				regionType = new RegionTypeDTO(region.getType());
				regionMap.put(region.getType(), regionType);
			}
			regionType.getRegions().add(region);
			regionType.setOccurrencesInRegionType(regionType.getOccurrencesInRegionType() + region.getOccurrences());
			
		}
		
		List<RegionTypeDTO> regionTypes = new ArrayList<RegionTypeDTO>();

		for (Map.Entry<String, RegionTypeDTO> entry : regionMap.entrySet()) {
	        regionTypes.add(entry.getValue());
	    }

		return regionTypes;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Region o) {
		if (o.getName() != null && regionName != null) {
			return regionName.compareTo(o.getName());
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.regionId == null) ? 0 : this.regionId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Region other = (Region) obj;
		if (this.regionId == null) {
			if (other.regionId != null)
				return false;
		} else if (!this.regionId.equals(other.regionId))
			return false;
		return true;
	}

	/**
	 * @return the taxonId
	 */
	public String getTaxonId() {
		return taxonId;
	}

	/**
	 * @param taxonId the taxonId to set
	 */
	public void setTaxonId(String taxonId) {
		this.taxonId = taxonId;
	}

	/**
	 * @return the regionName
	 */
	public String getRegionName() {
		return regionName;
	}

	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * @return the regionType
	 */
	public String getRegionType() {
		return regionType;
	}

	/**
	 * @param regionType the regionType to set
	 */
	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}
}
