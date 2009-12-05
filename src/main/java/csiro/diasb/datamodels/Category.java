package csiro.diasb.datamodels;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * This class categorises the properties currently contained in the SOLR index.
 * 
 * Categories are ranked to help order for UI display purposes.
 * 
 * TODO Implement as an enum???
 * 
 * @author Dave Martin
 */
public final class Category {

	private final String name;
	private final int rank;
	private final List<String> propertyNames;
	public static final Category taxonomic = new Category("Taxonomic", 1, 
			new String[]{
			"hasScientificName", 
			"hasScientificDescriptionAuthor",
			"genusPart",
			"nameComplete",
			"uninomial",
			"hasUninomial",
			"hasKingdom",
			"hasPhylum",
			"hasSubPhylum",
			"hasClass",
			"hasOrder",
			"hasSuperFamily",
			"hasFamily",
			"hasGenus",
			"hasSpecificEpithet",
			"specificEpithet",
			"hasCommonName",
			"hasSynonym"
	});
	public static final Category description = new Category("Status", 2, 
			new String[]{
			"hasPestStatus",
			"hasSimilarSpecies",
			"description", //should be hasDescriptiveText
			"hasDescription", //should be hasDescriptiveText
			"hasDescriptiveText",
			"hasThreatsText",
			"hasConservationStatus",
			"hasFloweringSeason",
			"hasMorphologicalText",
			"hasPopulationEstimate"
	});
	public static final Category geospatial = new Category("Geospatial", 3, 
			new String[]{
			"hasOccurrencesInRegion",
			"hasRegion",
			"hasDistributionText",
			"hasHabitatText"
	});	
	public static final Category media = new Category("Media", 4, 
			new String[]{
			"hasImage",
			"hasImageUrl",
			"hasImagePageUrl",
			"hasVideoUrl",
			"hasVideoPageUrl",
			"hasDistributionMapPageUrl",
			"hasDistributionMapImageUrl"
	});		
	public static final Category attribution = new Category("Attribution", 5, 
			new String[]{
			"hasCitation",
			"hasReference",
			"hasReference",
			"hasPublicationType",
			"publishedIn"
	});		
	public static final Category services = new Category("Web services", 6, 
			new String[]{
			"hasDistributionWFS",
			"hasDistributionWMS"
	});	
	public static final Category[] categories = 
		new Category[]{taxonomic,description,geospatial,media,attribution,services};  
	
	/**
	 * Private constructor.
	 */
	private Category(String name, int rank, String[] propertyNames) {
		this.name = name;
		this.rank = rank;
		this.propertyNames = new ArrayList<String>();
		//upper case it
		for(int i=0; i<propertyNames.length;i++){
			propertyNames[i] = propertyNames[i].toUpperCase();
		}
		CollectionUtils.addAll(this.propertyNames, propertyNames);
	}
	
	/**
	 * Is this property in this category
	 * 
	 * @param property
	 * @return
	 */
	public boolean isInCategory(String property){
		int idx = property.indexOf('.');
		if(idx>=0){
			property = property.substring(idx+1);
		}
		return this.propertyNames.contains(property.toUpperCase());
	}
	
	/**
	 * Is this property in this category
	 * 
	 * @param property
	 * @return
	 */
	public int getIndexInCategory(String property){
		int idx = property.indexOf('.');
		if(idx>=0){
			property = property.substring(idx+1);
		}
		return this.propertyNames.indexOf(property.toUpperCase());
	}	
	
	/**
	 * Retrieve the correct category for this property
	 * 
	 * @param property
	 * @return
	 */
	public static Category getCategoryForProperty(String property){
		for(Category category: categories){
			if(category.isInCategory(property))
				return category;
		}
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRank() {
		return rank;
	}
	
	public List<String> getPropertyNames() {
		return propertyNames;
	}	
}
