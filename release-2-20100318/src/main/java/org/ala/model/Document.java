/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
 * Simple bean representing a document within the system.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Document {

	/** The internal id for this document */
	protected int id;
	/** The internal parent id for this document */
	protected Integer parentDocumentId;
	/** The infosource id for this document */
	protected int infoSourceId;
    /** The infosource nae for this document */
    protected String infoSourceName;
    /** The infosource URI for this document */
    protected String infoSourceUri;
    /** The identifier for this document (from dc.identifier) */
    protected String identifier;
	/** The URI of this document */
	protected String uri;
	/** File path to the stored document on the file system */
	protected String filePath;
	/** The mime type for this document */
	protected String mimeType;
    /** The document title (dc.title) */
    protected String title;
	/** The date the document was harvested */
	protected java.util.Date created;
	/** The date the document was harvested */
	protected java.util.Date modified;

    /**
     * Constructor to set id
     *
     * @param docId the id to set
     */
    public Document(Integer docId) {
        this.id = docId;
    }

    /**
     * Default constructor
     */
    public Document() {}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}
	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the infoSourceId
	 */
	public int getInfoSourceId() {
		return infoSourceId;
	}
	/**
	 * @param infoSourceId the infoSourceId to set
	 */
	public void setInfoSourceId(int infoSourceId) {
		this.infoSourceId = infoSourceId;
	}
	/**
	 * @return the parentDocumentId
	 */
	public Integer getParentDocumentId() {
		return parentDocumentId;
	}
	/**
	 * @param parentDocumentId the parentDocumentId to set
	 */
	public void setParentDocumentId(Integer parentDocumentId) {
		this.parentDocumentId = parentDocumentId;
	}
	/**
	 * @return the created
	 */
	public java.util.Date getCreated() {
		return created;
	}
	/**
	 * @param created the created to set
	 */
	public void setCreated(java.util.Date created) {
		this.created = created;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	/**
	 * @return the modified
	 */
	public java.util.Date getModified() {
		return modified;
	}
	/**
	 * @param modified the modified to set
	 */
	public void setModified(java.util.Date modified) {
		this.modified = modified;
	}

    public String getInfoSourceName() {
        return infoSourceName;
    }

    public void setInfoSourceName(String infoSourceName) {
        this.infoSourceName = infoSourceName;
    }

    public String getInfoSourceUri() {
        return infoSourceUri;
    }

    public void setInfoSourceUri(String infoSourceUri) {
        this.infoSourceUri = infoSourceUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}