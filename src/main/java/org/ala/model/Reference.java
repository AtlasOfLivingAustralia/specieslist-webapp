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

import java.util.ArrayList;
import java.util.List;

/**
 * Model object that encapsulates a literature reference.
 * 
 * This is primarily aimed at modelling the artefacts coming out of 
 * BHL.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Reference extends AttributableObject implements Comparable<Reference>{

	protected String title;
	protected String authorship;
	protected String identifier;
	protected String scientificName;
	protected List<String> pageIdentifiers = new ArrayList<String>();

	/**
	 * @see org.ala.model.AttributableObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof Reference){
			Reference r = (Reference) obj;
			if(r.getTitle()!=null){
				if(!r.getTitle().equalsIgnoreCase(title)){
					return false;
				}
			}
			if(r.getIdentifier()!=null){
				if(!r.getIdentifier().equalsIgnoreCase(identifier)){
					return false;
				}
			}
			if(r.getScientificName()!=null){
				if(!r.getScientificName().equalsIgnoreCase(scientificName)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int compareTo(Reference o) {
		if(o!=null && o.getTitle()!=null){
			return o.getTitle().compareTo(this.title);
		}
		return -1;
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
	 * @return the authorship
	 */
	public String getAuthorship() {
		return authorship;
	}
	/**
	 * @param authorship the authorship to set
	 */
	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the pageIdentifiers
	 */
	public List<String> getPageIdentifiers() {
		return pageIdentifiers;
	}

	/**
	 * @param pageIdentifiers the pageIdentifiers to set
	 */
	public void setPageIdentifiers(List<String> pageIdentifiers) {
		this.pageIdentifiers = pageIdentifiers;
	}

	/**
	 * @return the scientificName
	 */
	public String getScientificName() {
		return scientificName;
	}

	/**
	 * @param scientificName the scientificName to set
	 */
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Reference [authorship=");
		builder.append(this.authorship);
		builder.append(", identifier=");
		builder.append(this.identifier);
		builder.append(", pageIdentifiers=");
		builder.append(this.pageIdentifiers);
		builder.append(", scientificName=");
		builder.append(this.scientificName);
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
