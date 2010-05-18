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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Simple POJO for a taxon name. Closely mirrors the TDWG ontology (as of 2009).
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@JsonIgnoreProperties(value="rankLabel")
public class TaxonName extends AttributableObject implements Comparable<TaxonName>{

	public String guid;
	public String nameComplete;
	public String authorship;
	public String rankString;
	public String publishedInCitation; //lsid
	public String publishedIn; //readable title
	public String nomenclaturalCode;
	public String typificationString;

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaxonName o) {
		//check the infosources
		if(o.getNameComplete()!=null && nameComplete!=null){
			return nameComplete.compareTo(o.getNameComplete());
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
	 * @return the publishedInCitation
	 */
	public String getPublishedInCitation() {
		return publishedInCitation;
	}
	/**
	 * @param publishedInCitation the publishedInCitation to set
	 */
	public void setPublishedInCitation(String publishedInCitation) {
		this.publishedInCitation = publishedInCitation;
	}
	/**
	 * @return the nomenclaturalCode
	 */
	public String getNomenclaturalCode() {
		return nomenclaturalCode;
	}
	/**
	 * @param nomenclaturalCode the nomenclaturalCode to set
	 */
	public void setNomenclaturalCode(String nomenclaturalCode) {
		this.nomenclaturalCode = nomenclaturalCode;
	}
	/**
	 * @return the typificationString
	 */
	public String getTypificationString() {
		return typificationString;
	}
	/**
	 * @param typificationString the typificationString to set
	 */
	public void setTypificationString(String typificationString) {
		this.typificationString = typificationString;
	}
    /**
     * Custom getter field to output human readable form of rank
     *
     * @return the rankLabel
     */
    public String getRankLabel() {
        String rankLabel = null;
        try {
            rankLabel = Rank.getForField(rankString).getName();
        } catch (Exception e) {
            rankLabel = rankString;
        }
        return rankLabel;
    }
	/**
	 * @return the publishedIn
	 */
	public String getPublishedIn() {
		return publishedIn;
	}
	/**
	 * @param publishedIn the publishedIn to set
	 */
	public void setPublishedIn(String publishedIn) {
		this.publishedIn = publishedIn;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaxonName [authorship=");
		builder.append(this.authorship);
		builder.append(", guid=");
		builder.append(this.guid);
		builder.append(", nameComplete=");
		builder.append(this.nameComplete);
		builder.append(", nomenclaturalCode=");
		builder.append(this.nomenclaturalCode);
		builder.append(", publishedIn=");
		builder.append(this.publishedIn);
		builder.append(", publishedInCitation=");
		builder.append(this.publishedInCitation);
		builder.append(", rankString=");
		builder.append(this.rankString);
		builder.append(", typificationString=");
		builder.append(this.typificationString);
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
