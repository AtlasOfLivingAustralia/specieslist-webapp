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

/**
 * Represents a geo region within the system.
 * 
 * A geographic region maybe a politically defined boundary or an area
 * of interest that can be described by a MultiPolygon.
 * 
 * The primary source of these objects *should* be the gazetteer.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class GeoRegion extends AttributableObject implements Comparable<GeoRegion>{

	protected String id;
	protected String guid;
	protected String acronym;
	protected String name;
	protected String regionType; //this is an identifier for the type of region
	protected String regionTypeName;
	protected String wellKnownText;
	protected String bounds;
	protected Float latitude;
	protected Float longitude;
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GeoRegion o) {
		if(o.getRegionType()!=null && regionType!=null && !o.getRegionType().equalsIgnoreCase(regionType)){
			return o.getRegionType().compareTo(regionType);
		}
		
		if(o.getName()!=null && name!=null){
			return name.compareTo(o.getName());
		}
		return -1;
	}	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof GeoRegion){
			GeoRegion gr = (GeoRegion) obj;
			if(gr.getGuid()!=null && guid!=null){
				return gr.getGuid().equals(guid);
			}
		}
		return false;
	}
	

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid the guid to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}
	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	/**
	 * @return the wellKnownText
	 */
	public String getWellKnownText() {
		return wellKnownText;
	}
	/**
	 * @param wellKnownText the wellKnownText to set
	 */
	public void setWellKnownText(String wellKnownText) {
		this.wellKnownText = wellKnownText;
	}
	/**
	 * @return the latitude
	 */
	public Float getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public Float getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the bounds
	 */
	public String getBounds() {
		return bounds;
	}
	/**
	 * @param bounds the bounds to set
	 */
	public void setBounds(String bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the regionTypeName
	 */
	public String getRegionTypeName() {
		return regionTypeName;
	}

	/**
	 * @param regionTypeName the regionTypeName to set
	 */
	public void setRegionTypeName(String regionTypeName) {
		this.regionTypeName = regionTypeName;
	}
}