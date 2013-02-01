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
 * Model object for a vocabulary.
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class Vocabulary {
	
	protected int id;
	protected String websiteUrl;
	protected String name;
	protected String description;
	protected String predicate;
	protected int infosourceId;
	
	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return websiteUrl
	 */
	public String getWebsiteUrl() {
		return websiteUrl;
	}
	/**
	 * @param websiteUrl
	 */
	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}
	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return predicate
	 */
	public String getPredicate() {
		return predicate;
	}
	/**
	 * @param predicate
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	/**
	 * @return infosourceId
	 */
	public int getInfosourceId() {
		return infosourceId;
	}
	/**
	 * @param infosourceId
	 */
	public void setInfosourceId(int infosourceId) {
		this.infosourceId = infosourceId;
	}	
}
