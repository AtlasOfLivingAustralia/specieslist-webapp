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
package org.ala.vocabulary;

import org.ala.model.ConservationStatus;
import org.ala.model.PestStatus;
import org.ala.model.Term;

/**
 * An interface for accessing vocabularies
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface Vocabulary {

	/**
	 * Perform a lookup on a simple relational database and return the preferred term 
	 * 
	 * @param infosourceId
	 * @param predicate
	 * @param rawValue
	 * @return
	 */
	public Term findPreferredTerm(int infosourceId, String predicate, String rawValue);
	
	/**
	 * Retrieve the conservation status associated with this string.
	 * 
	 * @param infosourceId
	 * @param rawValue
	 * @return the conservation status object, null if no terms have been found that match
	 */
	public ConservationStatus getConservationStatusFor(int infosourceId, String rawValue);
	
	/**
	 * Retrieve the pest status associated with this string
	 * 
	 * @param infosourceId
	 * @param rawValue
	 * @return the pest status object, null if no terms have been found that match
	 */
	public PestStatus getPestStatusFor(int infosourceId, String rawValue);
}
