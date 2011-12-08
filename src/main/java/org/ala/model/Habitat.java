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
 * Simple POJO to represent the habitat of a taxon
 * 
 * @author Peter Flemming (peter.flemming@csiro.au)
 */
public class Habitat extends AttributableObject implements Comparable<Habitat> {

	/**
	 * These are the current habitats derived from IRMNG.
	 * 
	 * M  = Known marine (including brackish)
	 * MN = Known marine & known nonmarine
	 * N  = Known nonmarine only (includes freshwater, terrestrial etc.)
	 * 
	 * The following status is derive from alternative source:
	 * Limnetic 
	 */
	protected String status;
	
	/**
	 * @param status
	 */
	public Habitat(String status) {
		super();
		this.status = status;
	}

	/**
	 * Default constructor
	 */
	public Habitat() {
		super();
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 *
	 */
	public String getStatusAsString() {
		if (this.status == null) {
			return null;
		} else {
			if (this.status.equalsIgnoreCase("M")) {
				return "Marine";
			} else if (this.status.equalsIgnoreCase("N")) {
				return "Non-marine";
			} else if (this.status.equalsIgnoreCase("MN")) {
				return "Marine and Non-marine";
			} else if (this.status.equalsIgnoreCase("Limnetic")){
				return "Limnetic";
			}
			else {
				return null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Habitat o) {
		if (o.getStatus() != null && this.status != null) {
			return this.status.compareTo(o.getStatus());
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
				+ ((this.status == null) ? 0 : this.status.hashCode());
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
		Habitat other = (Habitat) obj;
		if (this.status == null) {
			if (other.getStatus() != null)
				return false;
		} else if (!this.status.equals(other.getStatus()))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Habitat [status=");
		builder.append(this.status);
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
