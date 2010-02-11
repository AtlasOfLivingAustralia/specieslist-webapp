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
package org.ala.model;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * Simple immutable triple class.
 * 
 * @author Dave Martin
 */
public final class Triple {

	public final String subject;
	public final String predicate;
	public final String object;
	
	/**
	 * Initialise triple with subject, predicate and object.TaxonRelationship
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public Triple(String subject, String predicate, String object){
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
