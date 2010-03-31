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
 * Simple POJO for a publication 
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Publication extends AttributableObject implements Comparable<Publication> {

	protected String guid;
	protected String title;
	protected String author;
	protected String datePublished;
	protected String publicationType;
	
	/**
	 * @see org.ala.model.AttributableObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof Publication){
			Publication pub = (Publication) obj;
			if(pub.getGuid()!=null){
				return pub.getGuid().equals(guid);
			}
			if(pub.getTitle()!=null){
				return pub.getTitle().equalsIgnoreCase(title);
			}
		}
		return false;
	}
	
	@Override
	public int compareTo(Publication o) {
		if(o.getTitle()!=null){
			o.getTitle().compareTo(o.getTitle());
		}
		return 0;
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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
	 * @return the publicationType
	 */
	public String getPublicationType() {
		return publicationType;
	}
	/**
	 * @param publicationType the publicationType to set
	 */
	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}
	/**
	 * @return the datePublished
	 */
	public String getDatePublished() {
		return datePublished;
	}
	/**
	 * @param datePublished the datePublished to set
	 */
	public void setDatePublished(String datePublished) {
		this.datePublished = datePublished;
	}
}
