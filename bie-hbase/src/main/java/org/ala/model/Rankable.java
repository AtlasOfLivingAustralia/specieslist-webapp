/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.model;

/**
 * Implementing this interface indicates that the implementation
 * is a rankable object.
 * 
 * @see Image
 * @see CommonName
 * 
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Rankable {

	/**
	 * @return the ranking
	 */
	Integer getRanking();

	/**
	 * @param ranking the ranking to set
	 */
	void setRanking(Integer ranking);

	/**
	 * @return the noOfRankings
	 */
	Integer getNoOfRankings();

	/**
	 * @param noOfRankings the noOfRankings to set
	 */
	void setNoOfRankings(Integer noOfRankings);
    
    /**
     * Is this black listed
     * 
     * @return
     */
    boolean getIsBlackListed();

    /**
     * @param isBlackListed
     */
	void setIsBlackListed(boolean isBlackListed);
}