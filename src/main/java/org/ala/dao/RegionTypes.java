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

/**
 * Enum for geo region types
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public enum RegionTypes {

	STATE("state", 1, 2),
	LGA("lga", 3, 11),
	IBRA("ibra", 2000, 2999),
	IMCRA("imcra", 3000, 3999);
	
	private String name;
	private int lowerId, higherId;
	
    private RegionTypes(String name, int lowerId, int higherId) {
        this.name = name;
        this.lowerId = lowerId;
        this.higherId = higherId;
    }

    public static RegionTypes getRegionType(int regionTypeId){
    	for(RegionTypes rt :values()){
    		if(rt.getLowerId()<= regionTypeId && rt.getHigherId()>= regionTypeId)
    			return rt;
    	}
    	return null;
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLowerId() {
		return lowerId;
	}

	public void setLowerId(int lowerId) {
		this.lowerId = lowerId;
	}

	public int getHigherId() {
		return higherId;
	}

	public void setHigherId(int higherId) {
		this.higherId = higherId;
	}
    
	@Override
	public String toString() {
		return name;
	}
}
