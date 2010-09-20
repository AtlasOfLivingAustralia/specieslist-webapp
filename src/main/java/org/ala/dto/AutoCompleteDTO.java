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

/**
 * The DTO for storing the auto complete information
 * @author Natasha
 */
public class AutoCompleteDTO {

    protected String guid;
    protected String name;
    protected Integer occurrenceCount;
    protected Integer georeferencedCount;
    protected List<String> scientificNameMatches;
    protected List<String> commonNameMatches;
    protected String commonName;
    protected Integer rankId;
    protected String rankString;
    protected Integer left;
    protected Integer right;
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
	 * @return the occurrenceCount
	 */
	public Integer getOccurrenceCount() {
		return occurrenceCount;
	}
	/**
	 * @param occurrenceCount the occurrenceCount to set
	 */
	public void setOccurrenceCount(Integer occurrenceCount) {
		this.occurrenceCount = occurrenceCount;
	}
	/**
	 * @return the georeferencedCount
	 */
	public Integer getGeoreferencedCount() {
		return georeferencedCount;
	}
	/**
	 * @param georeferencedCount the georeferencedCount to set
	 */
	public void setGeoreferencedCount(Integer georeferencedCount) {
		this.georeferencedCount = georeferencedCount;
	}
	/**
	 * @return the scientificNameMatches
	 */
	public List<String> getScientificNameMatches() {
		return scientificNameMatches;
	}
	/**
	 * @param scientificNameMatches the scientificNameMatches to set
	 */
	public void setScientificNameMatches(List<String> scientificNameMatches) {
		this.scientificNameMatches = scientificNameMatches;
	}
	/**
	 * @return the commonNameMatches
	 */
	public List<String> getCommonNameMatches() {
		return commonNameMatches;
	}
	/**
	 * @param commonNameMatches the commonNameMatches to set
	 */
	public void setCommonNameMatches(List<String> commonNameMatches) {
		this.commonNameMatches = commonNameMatches;
	}
	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}
	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	/**
	 * @return the rankId
	 */
	public Integer getRankId() {
		return rankId;
	}
	/**
	 * @param rankId the rankId to set
	 */
	public void setRankId(Integer rankId) {
		this.rankId = rankId;
	}
	/**
	 * @return the rankString
	 */
	public String getRankString() {
		return rankString;
	}
	/**
	 * @param rankString the rankString to set
	 */
	public void setRankString(String rankString) {
		this.rankString = rankString;
	}
	/**
	 * @return the left
	 */
	public Integer getLeft() {
		return left;
	}
	/**
	 * @param left the left to set
	 */
	public void setLeft(Integer left) {
		this.left = left;
	}
	/**
	 * @return the right
	 */
	public Integer getRight() {
		return right;
	}
	/**
	 * @param right the right to set
	 */
	public void setRight(Integer right) {
		this.right = right;
	}
}
