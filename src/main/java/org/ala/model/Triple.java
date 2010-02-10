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
