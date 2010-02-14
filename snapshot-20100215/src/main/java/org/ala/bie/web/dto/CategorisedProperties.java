package org.ala.bie.web.dto;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * A simple bean holding a list of properties that have been 
 * associated with a category.
 * 
 * @author Dave Martin
 */
public class CategorisedProperties {

	/** The category associated with these properties */
	private Category category;
	/** The key/value map of properties */
	private Map<String, String> propertyMap = new LinkedHashMap<String, String>();
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
	 * @return the propertyMap
	 */
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	/**
	 * @param propertyMap the propertyMap to set
	 */
	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}
}
