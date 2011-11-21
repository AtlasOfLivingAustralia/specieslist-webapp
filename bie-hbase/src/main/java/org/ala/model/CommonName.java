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
 * Model object for a Common Name.
 * 
 * @author Dave Martin
 */
public class CommonName extends AttributableObject implements Comparable<CommonName>, Rankable{

	/** Nullable GUID for this common name */
	protected String guid; 
	protected String nameString;
    protected Integer noOfRankings;
    /** Ranking */
    protected Integer ranking;
    /** Indicates the name should be blacklisted, and hence removed from certain views */
    protected boolean isBlackListed = false;
    /** @deprecated this has been superceded by the ranking field */
	protected Boolean isPreferred = false;
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommonName o) {
		//check the infosources
    	if(ranking!=null && o.getRanking()==null){
    		return ranking *-1;
    	}
    	
    	if(o.getRanking()!=null && ranking==null){
    		return o.getRanking();
    	}
    	
    	//compare on rankings
    	if(ranking!=null && !ranking.equals(o.getRanking())){
    		return o.getRanking().compareTo(ranking);
    	}

    	//compare on number of rankings
    	if(o.getNoOfRankings()!=null && noOfRankings!=null && !noOfRankings.equals(o.getNoOfRankings())){
    		return o.getNoOfRankings().compareTo(noOfRankings);
    	}
    	
		if(o.getNameString()!=null && nameString!=null){
			return nameString.compareTo(o.getNameString());
		}
		return -1;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof CommonName){
			CommonName tc = (CommonName) obj;
			if(nameString!=null && nameString.equalsIgnoreCase(tc.getNameString())){
				//compare guids if not null
				if(tc.getGuid()!=null && guid!=null){
					return tc.getGuid().equals(guid);
				}
				if(documentId!=null && tc.getDocumentId()!=null){
					return documentId.equals(tc.getDocumentId());
				}
				if(infoSourceId!=null && tc.getInfoSourceId()!=null){
					return infoSourceId.equals(tc.getInfoSourceId());
				}
				//return true as the names are the same
				return true;
			}
		}
		return false;
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
	 * @return the nameString
	 */
	public String getNameString() {
		return nameString;
	}
	/**
	 * @param nameString the nameString to set
	 */
	public void setNameString(String nameString) {
		this.nameString = nameString;
	}
	/**
	 * @return the noOfRankings
	 */
	public Integer getNoOfRankings() {
		return noOfRankings;
	}

	/**
	 * @param noOfRankings the noOfRankings to set
	 */
	public void setNoOfRankings(Integer noOfRankings) {
		this.noOfRankings = noOfRankings;
	}

	/**
	 * @return the ranking
	 */
	public Integer getRanking() {
		return ranking;
	}

	/**
	 * @param ranking the ranking to set
	 */
	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	/**
	 * @return the isPreferred
	 */
	public Boolean getIsPreferred() {
		return isPreferred;
	}

	/**
	 * @return the isPreferred
	 */
	public Boolean isPreferred() {
		return isPreferred;
	}

	/**
	 * @param isPreferred the isPreferred to set
	 */
	public void setPreferred(Boolean isPreferred) {
		this.isPreferred = isPreferred;
	}
		
	/**
	 * @param isPreferred the isPreferred to set
	 */
	public void setIsPreferred(Boolean isPreferred) {
		this.isPreferred = isPreferred;
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CommonName [guid=" + guid + ", isPreferred=" + isPreferred
				+ ", nameString=" + nameString + ", noOfRankings="
				+ noOfRankings + ", ranking=" + ranking + "]";
	}
}