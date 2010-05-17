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
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Image extends AttributableObject implements Comparable<Image>{

	protected String guid;
	protected String contentType;
	protected String repoLocation;
    /** The title of the resource contributing the property */
    protected String title;
    /** The title of the identifier (URI) contributing the property */
    protected String identifier;

    /**
     * Compare to method
     *
     * @param o
     * @return
     */
    @Override
	public int compareTo(Image o) {
		//check the infosources
		if(o.getRepoLocation()!=null && repoLocation!=null){
			return o.getRepoLocation().compareTo(repoLocation);
		}
		return -1;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Image [contentType=");
		builder.append(this.contentType);
		builder.append(", guid=");
		builder.append(this.guid);
		builder.append(", identifier=");
		builder.append(this.identifier);
		builder.append(", repoLocation=");
		builder.append(this.repoLocation);
		builder.append(", title=");
		builder.append(this.title);
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
