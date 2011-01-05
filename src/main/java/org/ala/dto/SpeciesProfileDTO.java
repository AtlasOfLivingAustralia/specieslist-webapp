package org.ala.dto;

import java.util.ArrayList;
import java.util.List;

import org.ala.model.Habitat;

public class SpeciesProfileDTO {

	private String guid;
	private String scientificName;
	private String commonName;
	private List<String> habitats = new ArrayList<String>();
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
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}
	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	/**
	 * @return the habitats
	 */
	public List<String> getHabitats() {
		return habitats;
	}
	/**
	 * @param habitats the habitats to set
	 */
	public void setHabitats(List<String> habitats) {
		this.habitats = habitats;
	}
}
