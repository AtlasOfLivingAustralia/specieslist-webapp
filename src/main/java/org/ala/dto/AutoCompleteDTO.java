
package org.ala.dto;

import java.util.List;

/**
 * The DTO for storing the auto complete information
 * @author Natasha
 */
public class AutoCompleteDTO {

    protected String guid;
    protected String name;
    protected Integer occurrenceCount;
    protected Integer georeferencedCount;
    protected List<String> scientificNameMatches;
    protected List<String> commonNameMatches;
    protected String commonName;
    protected Integer rankId;
    protected String rankString;
    protected Integer left;
    protected Integer right;

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public List<String> getCommonNameMatches() {
        return commonNameMatches;
    }

    public void setCommonNameMatches(List<String> commonNameMatches) {
        this.commonNameMatches = commonNameMatches;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public List<String> getScientificNameMatches() {
        return scientificNameMatches;
    }

    public void setScientificNameMatches(List<String> scientificNameMatches) {
        this.scientificNameMatches = scientificNameMatches;
    }
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the georeferencedCount
	 */
	public Integer getGeoreferencedCount() {
		return georeferencedCount;
	}

	/**
	 * @param georeferencedCount the georeferencedCount to set
	 */
	public void setGeoreferencedCount(Integer georeferencedCount) {
		this.georeferencedCount = georeferencedCount;
	}

	/**
	 * @return the rankId
	 */
	public Integer getRankId() {
		return rankId;
	}

	/**
	 * @param rankId the rankId to set
	 */
	public void setRankId(Integer rankId) {
		this.rankId = rankId;
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
}
