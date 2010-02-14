package org.ala.bie.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dave Martin
 */
public class OrderedPropertyDTO {

	/** The Category for this property */
	private Category category;
	/** The name of the property */
	private String propertyName;
	/** The value of the property as a string ready for display*/
	private String propertyValue;

	private List<SourceDTO> sources = new ArrayList<SourceDTO>();	
	
	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}
	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	/**
	 * @return the propertyValue
	 */
	public String getPropertyValue() {
		return propertyValue;
	}
	/**
	 * @param propertyValue the propertyValue to set
	 */
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
	/**
	 * @return the category
	 */
	public Category getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(Category category) {
		this.category = category;
	}
	/**
	 * @return the sources
	 */
	public List<SourceDTO> getSources() {
		return sources;
	}
	/**
	 * @param sources the sources to set
	 */
	public void setSources(List<SourceDTO> sources) {
		this.sources = sources;
	}
}
