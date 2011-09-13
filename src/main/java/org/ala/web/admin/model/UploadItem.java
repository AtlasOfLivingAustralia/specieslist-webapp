/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.web.admin.model;

import java.util.List;
import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
/**
 * spring mvc backing bean
 * 
 * @author mok011
 *
 */
public class UploadItem {
	@NotEmpty
    private String scientificName;
	@Email(message = "Email address must be valid") @NotEmpty
    private String email;
    private String commonName;
    @NotEmpty
	private String userName;
    @NotEmpty
	private String rank;
    @NotEmpty
	private String attribn;
	private String description;
	private String source;
	@NotEmpty
	private String licence;
    @NotEmpty
	private String title;
    @NotNull
	private MultipartFile fileData;
	private List<FileItem> files;

	private String documentId;
	private String guid;

    public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public List<FileItem> getFiles() {
		return files;
	}

	public void setFiles(List<FileItem> files) {
		this.files = files;
	}

	public String getLicence() {
		return licence;
	}

	public void setLicence(String licence) {
		this.licence = licence;
	}
	
	public String getAttribn() {
		return attribn;
	}

	public void setAttribn(String attribn) {
		this.attribn = attribn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public MultipartFile getFileData() {
        return fileData;
    }

	public void setFileData(MultipartFile fileData) {
        this.fileData = fileData;
    }
}
