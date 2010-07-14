package org.ala.dto;

import org.ala.model.GeoRegion;
import org.ala.model.TaxonConcept;

public class ExtendedGeoRegionDTO {

	protected TaxonConcept birdEmblem;
	protected TaxonConcept animalEmblem;
	protected TaxonConcept plantEmblem;
	protected GeoRegion geoRegion;
	/**
	 * @return the birdEmblem
	 */
	public TaxonConcept getBirdEmblem() {
		return birdEmblem;
	}
	/**
	 * @param birdEmblem the birdEmblem to set
	 */
	public void setBirdEmblem(TaxonConcept birdEmblem) {
		this.birdEmblem = birdEmblem;
	}
	/**
	 * @return the animalEmblem
	 */
	public TaxonConcept getAnimalEmblem() {
		return animalEmblem;
	}
	/**
	 * @param animalEmblem the animalEmblem to set
	 */
	public void setAnimalEmblem(TaxonConcept animalEmblem) {
		this.animalEmblem = animalEmblem;
	}
	/**
	 * @return the plantEmblem
	 */
	public TaxonConcept getPlantEmblem() {
		return plantEmblem;
	}
	/**
	 * @param plantEmblem the plantEmblem to set
	 */
	public void setPlantEmblem(TaxonConcept plantEmblem) {
		this.plantEmblem = plantEmblem;
	}
	/**
	 * @return the geoRegion
	 */
	public GeoRegion getGeoRegion() {
		return geoRegion;
	}
	/**
	 * @param geoRegion the geoRegion to set
	 */
	public void setGeoRegion(GeoRegion geoRegion) {
		this.geoRegion = geoRegion;
	}
}
