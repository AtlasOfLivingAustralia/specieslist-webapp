/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.dto;

import org.ala.model.GeoRegion;
import org.ala.model.TaxonConcept;

/**
 * A DTO for a georegion 
 * 
 * @author Dave Martin
 */
public class ExtendedGeoRegionDTO {

	protected GeoRegion geoRegion;
	protected TaxonConcept birdEmblem;
	protected TaxonConcept animalEmblem;
	protected TaxonConcept plantEmblem;
	protected TaxonConcept marineEmblem;
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
	/**
	 * @return the marineEmblem
	 */
	public TaxonConcept getMarineEmblem() {
		return marineEmblem;
	}
	/**
	 * @param marineEmblem the marineEmblem to set
	 */
	public void setMarineEmblem(TaxonConcept marineEmblem) {
		this.marineEmblem = marineEmblem;
	}
}
