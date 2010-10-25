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
 * A DTO used for returning search results.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SearchTaxonConceptDTO extends SearchDTO implements Comparable<SearchTaxonConceptDTO>{
	
	protected String parentId;
	protected String parentGuid;
	protected String commonName;
	protected String nameComplete;
	protected String commonNameSingle;
	protected String acceptedConceptGuid;
	protected String acceptedConceptName;
	protected boolean hasChildren;
    protected String rank;
    protected int rankId;
    protected String pestStatus;
    protected String conservationStatus;
    protected String highlight;
    protected String image;
    protected String thumbnail;
    protected Integer left;
    protected Integer right;
    protected String kingdom;
    protected String author;

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SearchTaxonConceptDTO o) {
		if(o.getName()!=null && this.name!=null){
			return this.name.compareTo(o.getName());
		}
		return 0;
	}
	
	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
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
	 * @return the acceptedConceptGuid
	 */
	public String getAcceptedConceptGuid() {
		return acceptedConceptGuid;
	}
	/**
	 * @param acceptedConceptGuid the acceptedConceptGuid to set
	 */
	public void setAcceptedConceptGuid(String acceptedConceptGuid) {
		this.acceptedConceptGuid = acceptedConceptGuid;
	}
	/**
	 * @return the acceptedConceptName
	 */
	public String getAcceptedConceptName() {
		return acceptedConceptName;
	}
	/**
	 * @param acceptedConceptName the acceptedConceptName to set
	 */
	public void setAcceptedConceptName(String acceptedConceptName) {
		this.acceptedConceptName = acceptedConceptName;
	}
	/**
	 * @return the parentGuid
	 */
	public String getParentGuid() {
		return parentGuid;
	}
	/**
	 * @param parentGuid the parentGuid to set
	 */
	public void setParentGuid(String parentGuid) {
		this.parentGuid = parentGuid;
	}
	/**
	 * @return the hasChildren
	 */
	public boolean isHasChildren() {
		return hasChildren;
	}
	/**
	 * @param hasChildren the hasChildren to set
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	/**
	 * @return the rank
	 */
	public String getRank() {
		return rank;
	}
	/**
	 * @param rank the rank to set
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}
	/**
	 * @return the rankId
	 */
	public int getRankId() {
		return rankId;
	}
	/**
	 * @param rankId the rankId to set
	 */
	public void setRankId(int rankId) {
		this.rankId = rankId;
	}
	/**
	 * @return the pestStatus
	 */
	public String getPestStatus() {
		return pestStatus;
	}
	/**
	 * @param pestStatus the pestStatus to set
	 */
	public void setPestStatus(String pestStatus) {
		this.pestStatus = pestStatus;
	}
	/**
	 * @return the conservationStatus
	 */
	public String getConservationStatus() {
		return conservationStatus;
	}
	/**
	 * @param conservationStatus the conservationStatus to set
	 */
	public void setConservationStatus(String conservationStatus) {
		this.conservationStatus = conservationStatus;
	}
	/**
	 * @return the highlight
	 */
	public String getHighlight() {
		return highlight;
	}
	/**
	 * @param highlight the highlight to set
	 */
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}
	/**
	 * @return the image
	 */
	public String getImage() {
		return image;
	}
	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}
	/**
	 * @return the thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}
	/**
	 * @param thumbnail the thumbnail to set
	 */
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
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
	/**
	 * @return the commonNameSingle
	 */
	public String getCommonNameSingle() {
		return commonNameSingle;
	}
	/**
	 * @param commonNameSingle the commonNameSingle to set
	 */
	public void setCommonNameSingle(String commonNameSingle) {
		this.commonNameSingle = commonNameSingle;
	}
    /**
	 * @return the kingdom
	 */
    public String getKingdom() {
        return kingdom;
    }
    /**
	 * @param kingdom the kingdom to set
	 */
    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }
    /**
	 * @return the author
	 */
    public String getAuthor() {
        return author;
    }
    /**
	 * @param author the author to set
	 */
    public void setAuthor(String author) {
        this.author = author;
    }

	/**
	 * @return the nameComplete
	 */
	public String getNameComplete() {
		return nameComplete;
	}

	/**
	 * @param nameComplete the nameComplete to set
	 */
	public void setNameComplete(String nameComplete) {
		this.nameComplete = nameComplete;
	}
}