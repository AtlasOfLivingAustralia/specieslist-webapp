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
 * Conservation status for a species. 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class ConservationStatus extends AttributableObject implements Comparable<ConservationStatus> {
	
	/** The conservation status - using a controlled vocabulary */
	protected String status;
	/** The system supplying the status e.g. IUCN */
	protected String system;
	/** The region where this status is valid. e.g. New South Wales */
	protected String region;
	
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConservationStatus [region=");
		builder.append(this.region);
		builder.append(", status=");
		builder.append(this.status);
		builder.append(", system=");
		builder.append(this.system);
		builder.append(", documentId=");
		builder.append(this.documentId);
		builder.append(", infoSourceId=");
		builder.append(this.infoSourceId);
		builder.append(", infoSourceName=");
		builder.append(this.infoSourceName);
		builder.append(", infoSourceURL=");
		builder.append(this.infoSourceURL);
		builder.append("]");
		return builder.toString();
	}
}
