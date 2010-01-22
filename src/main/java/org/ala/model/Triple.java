package org.ala.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public final class Triple {

	public final String subject;
	public final String predicate;
	public final String object;
	
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
