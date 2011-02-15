/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ala.model.ConservationStatus;
import org.ala.model.SensitiveStatus;
/**
 * A simple DTO exposing some elements of a species profile.
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class SpeciesProfileDTO {

	private String guid;
	private String scientificName;
	private String commonName;
	private List<String> habitats = new ArrayList<String>();
        private String left;
        private String right;
        private List<ConservationStatus> conservationStatus = new ArrayList<ConservationStatus>();
        private List<SensitiveStatus> sensitiveStatus;
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

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

        public List<ConservationStatus> getConservationStatus() {
            return conservationStatus;
        }

        public void setConservationStatus(List<ConservationStatus> conservationStatus) {
            this.conservationStatus = conservationStatus;
        }

        public List<SensitiveStatus> getSensitiveStatus() {
            return sensitiveStatus;
        }

        public void setSensitiveStatus(List<SensitiveStatus> sensitiveStatus) {
            this.sensitiveStatus = sensitiveStatus;
        }



}
