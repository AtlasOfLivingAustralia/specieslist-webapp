package org.ala.dto;

public class SearchRegionDTO extends SearchDTO {

	protected String acronym;
	protected String regionTypeName;
	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}
	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	/**
	 * @return the regionTypeName
	 */
	public String getRegionTypeName() {
		return regionTypeName;
	}
	/**
	 * @param regionTypeName the regionTypeName to set
	 */
	public void setRegionTypeName(String regionTypeName) {
		this.regionTypeName = regionTypeName;
	}
}
