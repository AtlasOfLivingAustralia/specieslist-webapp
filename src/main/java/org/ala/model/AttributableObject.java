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

public class AttributableObject {

	//attribution
	protected String infoSourceId;
	protected String documentId;
    protected String infoSourceName;
    protected String infoSourceURL;

    /**
	 * Custom equals method to use documentId for uniqueness
     *
     * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof Image){
			Image img = (Image) obj;
			if(img.getDocumentId()!=null && documentId!=null){
				return img.getDocumentId().equals(documentId);
			}
		}
		return false;
	}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.documentId != null ? this.documentId.hashCode() : 0);
        return hash;
    }

	/**
	 * @return the infoSourceId
	 */
	public String getInfoSourceId() {
		return infoSourceId;
	}

	/**
	 * @param infoSourceId the infoSourceId to set
	 */
	public void setInfoSourceId(String infoSourceId) {
		this.infoSourceId = infoSourceId;
	}

	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}

	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

    /**
     * @return the infoSourceName
     */
    public String getInfoSourceName() {
        return infoSourceName;
    }

    /**
     * @param infoSourceName the infoSourceName to set
     */
    public void setInfoSourceName(String infoSourceName) {
        this.infoSourceName = infoSourceName;
    }

    /**
     * @return the infoSourceURL
     */
    public String getInfoSourceURL() {
        return infoSourceURL;
    }

    /**
     *
     * @param infoSourceURL
     */
    public void setInfoSourceURL(String infoSourceURL) {
        this.infoSourceURL = infoSourceURL;
    }
}
