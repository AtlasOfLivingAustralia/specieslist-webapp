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
 * Simple POJO representing an image within the system.
 * FIXME We *should* be generating thumbnails at repository load
 * time. Hence the thumbnail property should be populated.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
//@JsonIgnoreProperties({"thumbnail", "dcLocation"})
public class Image extends AttributableObject implements Comparable<Image>, Rankable{

	/** The guid of this image, typically the URL from whence it came */
	protected String guid;
	/** The content type of this image */
	protected String contentType;
	/** The location in the repository */
	protected String repoLocation;
	/** The location in the repository */
	protected String dcLocation;
	/** The location in the repository */
	protected String thumbnail;
    /** The title of the resource contributing the property */
    protected String title;
    /** The title of the identifier (URI) contributing the property */
    protected String identifier;
    /** The creator of this image - for attribution */
    protected String creator;
    /** The locality this image was taken at as a free text string */
    protected String locality;
    /** The locality this image was taken at as a free text string */
    protected String isPartOf;
    /** The licence */
    protected String licence;
    /** The rights */
    protected String rights;
    /** Ranking */
    protected Integer noOfRankings;
    /** Ranking */
    protected Integer ranking;
    /** The id for the document in the repository */
    protected Integer repoId;
    /** Indicates the image should be blacklisted, and hence removed from certain views */
    protected boolean isBlackListed = false;
    /** Indicates the image is preferred */
    protected boolean isPreferred = false;
    

    /** The description */
    protected String description;
    
	public Image(String guid, String contentType, String repoLocation,
			String dcLocation, String thumbnail, String title,
			String identifier, String creator, String locality,
			String isPartOf, String licence, String rights,
			Integer noOfRankings, Integer ranking) {
    	this(guid, contentType, repoLocation, dcLocation, thumbnail, title,
    			identifier, creator, locality, isPartOf, licence, rights,
    			noOfRankings, ranking, false);
    }
	
	public Image(String guid, String contentType, String repoLocation,
            String dcLocation, String thumbnail, String title,
            String identifier, String creator, String locality,
            String isPartOf, String licence, String rights,
            Integer noOfRankings, Integer ranking, boolean isBlackListed) {
	    this(guid, contentType, repoLocation, dcLocation, thumbnail, title,
                identifier, creator, locality, isPartOf, licence, rights,
                noOfRankings, ranking, isBlackListed, false);
	}
    
	public Image(String guid, String contentType, String repoLocation,
			String dcLocation, String thumbnail, String title,
			String identifier, String creator, String locality,
			String isPartOf, String licence, String rights,
			Integer noOfRankings, Integer ranking, boolean isBlackListed, boolean isPreferred) {
		this.guid = guid;
		this.contentType = contentType;
		this.repoLocation = repoLocation;
		this.dcLocation = dcLocation;
		this.thumbnail = thumbnail;
		this.title = title;
		this.identifier = identifier;
		this.creator = creator;
		this.locality = locality;
		this.isPartOf = isPartOf;
		this.licence = licence;
		this.rights = rights;
		this.noOfRankings = noOfRankings;
		this.ranking = ranking;
		this.isBlackListed = isBlackListed;
		this.isPreferred = isPreferred;
	}

    public Image(){}

	/**
     * Compare to method to sort the images in descending order of preference.
     *
     * @param o
     * @return
     */
    @Override
	public int compareTo(Image o) {
    	return RankUtils.compareTo(this, o);
	}
    
    /**
     * Is this black listed
     * 
     * @return
     */
    public boolean getIsBlackListed() {
		return isBlackListed;
	}

    /**
     * @param isBlackListed
     */
	public void setIsBlackListed(boolean isBlackListed) {
		this.isBlackListed = isBlackListed;
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
	 * @return the repoLocation
	 */
	public String getRepoLocation() {
		return repoLocation;
	}
	/**
	 * @param repoLocation the repoLocation to set
	 */
	public void setRepoLocation(String repoLocation) {
		this.repoLocation = repoLocation;
	}
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
    /**
     * Get the value of title
     *
     * @return the value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the value of title
     *
     * @param title new value of title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the value of identifier
     *
     * @return the value of identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the value of identifier
     *
     * @param identifier new value of identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

	/**
	 * @return the dcLocation
	 */
	public String getDcLocation() {
		return dcLocation;
	}

	/**
	 * @param dcLocation the dcLocation to set
	 */
	public void setDcLocation(String dcLocation) {
		this.dcLocation = dcLocation;
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
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @return the locality
	 */
	public String getLocality() {
		return locality;
	}

	/**
	 * @param locality the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}

	/**
	 * @return the isPartOf
	 */
	public String getIsPartOf() {
		return isPartOf;
	}

	/**
	 * @param isPartOf the isPartOf to set
	 */
	public void setIsPartOf(String isPartOf) {
		this.isPartOf = isPartOf;
	}


	/**
	 * @return the licence
	 */
	public String getLicence() {
		return licence;
	}

	/**
	 * @param licence the licence to set
	 */
	public void setLicence(String licence) {
		this.licence = licence;
	}

	/**
	 * @return the rights
	 */
	public String getRights() {
		return rights;
	}

	/**
	 * @param rights the rights to set
	 */
	public void setRights(String rights) {
		this.rights = rights;
	}

	/**
	 * @see org.ala.model.Rankable#getRanking()
	 */
	public Integer getRanking() {
		return ranking;
	}

	/**
	 * @see org.ala.model.Rankable#setRanking(java.lang.Integer)
	 */
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	/**
	 * @see org.ala.model.Rankable#getNoOfRankings()
	 */
	public Integer getNoOfRankings() {
		return noOfRankings;
	}

	/**
	 * @see org.ala.model.Rankable#setNoOfRankings(java.lang.Integer)
	 */
	public void setNoOfRankings(Integer noOfRankings) {
		this.noOfRankings = noOfRankings;
	}

    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Image [contentType=" + contentType + ", creator=" + creator
				+ ", dcLocation=" + dcLocation + ", guid=" + guid
				+ ", identifier=" + identifier + ", isPartOf=" + isPartOf
				+ ", licence=" + licence + ", locality=" + locality
				+ ", repoLocation=" + repoLocation + ", rights=" + rights
				+ ", thumbnail=" + thumbnail + ", title=" + title + "]";
	}
	
    public String getDescription() {
		return description;
	}

    public void setDescription(String description) {
		this.description = description;
	}

    public boolean isPreferred() {
        return isPreferred;
    }

    public void setPreferred(boolean isPreferred) {
        this.isPreferred = isPreferred;
    }
	
}
