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

import java.util.HashMap;
import java.util.Map;

/**
 * Conservation status for a species. 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ConservationStatus extends AttributableObject implements Comparable<ConservationStatus> {

	/** This is the raw status retrieved from the source document */
	protected String rawStatus;
	/** The conservation status - using a controlled vocabulary */
	protected String status;
	/** The system supplying the status e.g. IUCN */
	protected String system;
	/** The region where this status is valid. e.g. New South Wales */
	protected String region;
	/** This is the URI or identifier for the region */
	protected String regionId;
        /** A map of the region URI to display regions where the conservation is valid
         * This is used to store regions in which a national conservation code is
         * applicable
         */
        protected Map<String,String> otherRegions;
        /** The raw code that was used by the source record that maps to the raw status */
        protected String rawCode;
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}
	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}
	@Override
	public int compareTo(ConservationStatus o) {
		//check the infosources
		if(o.getStatus()!=null && status!=null){
			return o.getStatus().compareTo(status);
		}
		return -1;
	}
        /**
	 * @see java.lang.Object#equals(java.lang.Object)
         * Conservation Statuses are equal when the info sources and raw values are identical
	 */
	@Override
	public boolean equals(Object obj) {
            if(obj instanceof ConservationStatus){
                ConservationStatus csobj = (ConservationStatus)obj;
                return csobj.getInfoSourceId().equals(infoSourceId)
                        && csobj.getRawStatus().equals(rawStatus)
                        && (csobj.getRawCode() == null  || csobj.getRawCode().equals(rawCode));
            }
            return false;
        }
	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}
	/**
	 * @param system the system to set
	 */
	public void setSystem(String system) {
		this.system = system;
	}
	
	/**
	 * @return the rawStatus
	 */
	public String getRawStatus() {
		return rawStatus;
	}
	/**
	 * @param rawStatus the rawStatus to set
	 */
	public void setRawStatus(String rawStatus) {
		this.rawStatus = rawStatus;
	}
	/**
	 * @return the regionId
	 */
	public String getRegionId() {
		return regionId;
	}
	/**
	 * @param regionId the regionId to set
	 */
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
        public Map<String, String> getOtherRegions(){
            return otherRegions;
        }
        public void setOtherRegions(Map<String,String> regions){
            this.otherRegions = regions;
        }
        /**
         * Puts a new region into the map
         * @param region
         * @param uri
         */
        public void addOtherRegion(String region, String uri){
            if(otherRegions == null)
                otherRegions = new HashMap<String,String>();
            if(!otherRegions.containsKey(uri))
                otherRegions.put(uri, region);
        }

    public String getRawCode() {
        return rawCode;
    }

    public void setRawCode(String rawCode) {
        this.rawCode = rawCode;
    }


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ConservationStatus [rawCode="+rawCode+", rawStatus=" + rawStatus + ", region="
				+ region + ", regionId=" + regionId + ", status=" + status
				+ ", system=" + system + ", regions=" +otherRegions+ "]";
	}
}
