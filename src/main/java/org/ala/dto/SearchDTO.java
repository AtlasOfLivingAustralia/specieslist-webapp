package org.ala.dto;

/**
 * A generic search dto intended to be subclasses.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SearchDTO {

	protected String guid;
	protected String name;
	protected String idxType;
	
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the idxType
	 */
	public String getIdxType() {
		return idxType;
	}
	/**
	 * @param idxType the idxType to set
	 */
	public void setIdxType(String idxType) {
		this.idxType = idxType;
	}
}
