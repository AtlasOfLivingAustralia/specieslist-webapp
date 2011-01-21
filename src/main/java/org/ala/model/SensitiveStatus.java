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
 * A container for holding an assessment of sensitivity status.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SensitiveStatus extends AttributableObject implements Comparable<SensitiveStatus> {

	/** State(s) or National **/
	private String sensitivityZone;
	/** Endangered etc */
	private String sensitivityCategory;
	
	/**
	 * @return the sensitivityCategory
	 */
	public String getSensitivityCategory() {
		return sensitivityCategory;
	}

	/**
	 * @param sensitivityCategory the sensitivityCategory to set
	 */
	public void setSensitivityCategory(String sensitivityCategory) {
		this.sensitivityCategory = sensitivityCategory;
	}
	
	@Override
	public int compareTo(SensitiveStatus ss) {
		return 0;
	}

	/**
	 * @return the sensitivityZone
	 */
	public String getSensitivityZone() {
		return sensitivityZone;
	}

	/**
	 * @param sensitivityZone the sensitivityZone to set
	 */
	public void setSensitivityZone(String sensitivityZone) {
		this.sensitivityZone = sensitivityZone;
	}
}