
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
    protected List<String> scientificNameMatches;
    protected List<String> commonNameMatches;
    protected String commonName;


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

    

}
