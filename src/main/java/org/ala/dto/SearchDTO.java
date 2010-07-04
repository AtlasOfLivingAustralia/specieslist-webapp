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

/**
 * A generic search dto intended to be subclasses.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SearchDTO {

	protected String guid;
	protected String name;
	protected String idxType;
    protected Float score;
	
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
	 * @return the idxType
	 */
	public String getIdxType() {
		return idxType;
	}
	/**
	 * @param idxType the idxType to set
	 */
	public void setIdxType(String idxType) {
		this.idxType = idxType;
	}
	/**
	 * @return the score
	 */
	public Float getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(Float score) {
		this.score = score;
	}
}
