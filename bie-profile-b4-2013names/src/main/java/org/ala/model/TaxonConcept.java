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
 * Simple POJO for a taxon concept.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class TaxonConcept extends AttributableObject implements Comparable<TaxonConcept>{

	/** internal identifier for this concept */
	protected int id = -1;
	/** Every taxonconcept should have a guid of some sort */
	protected String guid;
	/** internal identifier for the parent concept */
	protected String parentId;
	protected String parentGuid;
	protected String parentSrc;
	protected Integer parentSrcId;
	protected String acceptedConceptGuid;
	protected String nameGuid; //one to many
	protected String nameString;
	protected String author;
	protected String authorYear;
	protected String publishedInCitation;
	protected String publishedIn;
	protected String referencedInGuid;
	protected String referencedIn;
	protected Integer rankID;
	protected String rankString;
	/** The rank as supplied by the source data resource */
	protected String rawRankString;
	protected Integer left;
	protected Integer right;
	/** Indicates that this is the preferred concept.  Ie the guid for the concept matches the unique id for the record **/
	protected boolean isPreferred=false;//stores whether or not the concept is preferred
	protected boolean isDraft=false;
	protected boolean isProtologue=false;
	

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TaxonConcept o) {
		//check the infosources
	    if(o.isPreferred != isPreferred){
	        if(isPreferred)
	            return -1;
	        else return 1;
	    }
	    else if(o.getNameString()!=null && nameString!=null){
			return nameString.compareTo(o.getNameString());
		}
		return -1;
	}	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof TaxonConcept){
			TaxonConcept tc = (TaxonConcept) obj;
			if(tc.getGuid()!=null && guid!=null){
				return tc.getGuid().equals(guid);
			}
			if(tc.getId()!=-1 && id!=-1){
				return tc.getId()==id;
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
	 * @return the parentGuid
	 */
	public String getParentGuid() {
		return parentGuid;
	}
	/**
	 * @param parentGuid the parentGuid to set
	 */
	public void setParentGuid(String parentGuid) {
		this.parentGuid = parentGuid;
	}
	/**
	 * @return the nameGuid
	 */
	public String getNameGuid() {
		return nameGuid;
	}
	/**
	 * @param nameGuid the nameGuid to set
	 */
	public void setNameGuid(String nameGuid) {
		this.nameGuid = nameGuid;
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
	 * @return the authorYear
	 */
	public String getAuthorYear() {
		return authorYear;
	}
	/**
	 * @param authorYear the authorYear to set
	 */
	public void setAuthorYear(String authorYear) {
		this.authorYear = authorYear;
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
	 * @return the acceptedConceptGuid
	 */
	public String getAcceptedConceptGuid() {
		return acceptedConceptGuid;
	}
	/**
	 * @param acceptedConceptGuid the acceptedConceptGuid to set
	 */
	public void setAcceptedConceptGuid(String acceptedConceptGuid) {
		this.acceptedConceptGuid = acceptedConceptGuid;
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
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
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
	 * @return the left
	 */
	public Integer getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(Integer left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public Integer getRight() {
		return right;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(Integer right) {
		this.right = right;
	}

	/**
	 * @return the rankID
	 */
	public Integer getRankID() {
		return rankID;
	}

	/**
	 * @param rankID the rankID to set
	 */
	public void setRankID(Integer rankID) {
		this.rankID = rankID;
	}
	
	/**
     * @return the parentSrc
     */
    public String getParentSrc() {
        return parentSrc;
    }

    /**
     * @param parentSrc the parentSrc to set
     */
    public void setParentSrc(String parentSrc) {
        this.parentSrc = parentSrc;
    }

    /**
     * @return the parentSrcId
     */
    public Integer getParentSrcId() {
        return parentSrcId;
    }

    /**
     * @param parentSrcId the parentSrcId to set
     */
    public void setParentSrcId(Integer parentSrcId) {
        this.parentSrcId = parentSrcId;
    }
    
    

    /**
     * @return the isPreferred
     */
    public boolean getIsPreferred() {
        return isPreferred;
    }

    /**
     * @param isPreferred the isPreferred to set
     */
    public void setIsPreferred(boolean isPreferred) {
        this.isPreferred = isPreferred;
    }
    

    /**
     * @return the isDraft
     */
    public boolean getIsDraft() {
        return isDraft;
    }

    /**
     * @param isDraft the isDraft to set
     */
    public void setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    /**
     * @return the isProtologue
     */
    public boolean getIsProtologue() {
        return isProtologue;
    }

    /**
     * @param isProtologue the isProtologue to set
     */
    public void setIsProtologue(boolean isProtologue) {
        this.isProtologue = isProtologue;
    }

    /**
     * @return the referencedInGuid
     */
    public String getReferencedInGuid() {
        return referencedInGuid;
    }

    /**
     * @param referencedInGuid the referencedInGuid to set
     */
    public void setReferencedInGuid(String referencedInGuid) {
        this.referencedInGuid = referencedInGuid;
    }

    /**
     * @return the referencedIn
     */
    public String getReferencedIn() {
        return referencedIn;
    }

    /**
     * @param referencedIn the referencedIn to set
     */
    public void setReferencedIn(String referencedIn) {
        this.referencedIn = referencedIn;
    }
    

    /**
     * @return the rawRankString
     */
    public String getRawRankString() {
        return rawRankString;
    }

    /**
     * @param rawRankString the rawRankString to set
     */
    public void setRawRankString(String rawRankString) {
        this.rawRankString = rawRankString;
    }

    /**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaxonConcept [acceptedConceptGuid=");
		builder.append(this.acceptedConceptGuid);
		builder.append(", author=");
		builder.append(this.author);
		builder.append(", authorYear=");
		builder.append(this.authorYear);
		builder.append(", guid=");
		builder.append(this.guid);
		builder.append(", id=");
		builder.append(this.id);
		builder.append(", nameGuid=");
		builder.append(this.nameGuid);
		builder.append(", nameString=");
		builder.append(this.nameString);
		builder.append(", parentGuid=");
		builder.append(this.parentGuid);
		builder.append(", parentId=");
		builder.append(this.parentId);
		builder.append(", publishedIn=");
		builder.append(this.publishedIn);
		builder.append(", publishedInCitation=");
		builder.append(this.publishedInCitation);
		builder.append(", rankString=");
		builder.append(this.rankString);
		builder.append(", rawRankString=");
        builder.append(this.rawRankString);
		builder.append(", isPreferred=");
		builder.append(this.isPreferred);
		builder.append(", isDraft=");
		builder.append(this.isDraft);
		builder.append(", isProtologue=");
		builder.append(this.isProtologue);
		builder.append(", referencedIn=");
		builder.append(this.referencedIn);
		builder.append(", referencedInGuid=");
		builder.append(this.referencedInGuid);
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