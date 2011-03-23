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
 * Simple POJO representing a pest status for a species.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class PestStatus extends AttributableObject implements Comparable<PestStatus> {

	/** This is the raw status retrieved from the source document */
	protected String rawStatus;
	/** The status - using a controlled vocabulary */
	protected String status;
	/** The system supplying the status e.g. IUCN */
	protected String system;
	/** The region where this status is valid. e.g. New South Wales */
	protected String region;
	/** This is the URI or identifier for the region */
	protected String regionId;
	
	@Override
	public int compareTo(PestStatus o) {
		//check the infosources
		if(o.getStatus()!=null && status!=null){
			return o.getStatus().compareTo(status);
		}
		return -1;
	}	
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
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PestStatus [rawStatus=" + rawStatus + ", region=" + region
				+ ", regionId=" + regionId + ", status=" + status + ", system="
				+ system + "]";
	}
}
