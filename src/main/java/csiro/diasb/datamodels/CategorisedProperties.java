package csiro.diasb.datamodels;

import java.util.LinkedHashMap;
import java.util.Map;
/**
 * A simple bean holding a list of properties that have been 
 * associated with a category and rank.
 * 
 * @author Dave Martin
 */
public class CategorisedProperties {

//	public String name;
//	public int rank;
	private Category category;
	private Map<String, String> propertyMap = new LinkedHashMap<String, String>();
	
//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}
//	public int getRank() {
//		return rank;
//	}
//	public void setRank(int rank) {
//		this.rank = rank;
//	}
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
}
