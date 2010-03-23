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
package org.ala.documentmapper;

import org.ala.repository.Predicates;
/**
 * A POJO used to encapulate a mapping from a document using a
 * query string to one or more predicates for triple creation.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Mapping {
		
	public final MappingType mappingType;
	public final String subject;
	public final String queryString;
	public final Predicates[] predicates;
	public final boolean isGuid;

	public Mapping(String queryString, String subject, Predicates[] predicates, MappingType mappingType) {
		this.subject = subject;
		this.queryString = queryString;
		this.predicates = predicates;
		this.mappingType = mappingType;
		this.isGuid = false;
	}
	
	public Mapping(String queryString, String subject, Predicates[] predicates) {
		this.subject = subject;
		this.queryString = queryString;
		this.predicates = predicates;
		this.isGuid = false;
		this.mappingType = MappingType.XPATH;
	}
	
	public Mapping(String queryString, String subject, Predicates[] predicates, boolean isGuid) {
		this.subject = subject;
		this.queryString = queryString;
		this.predicates = predicates;
		this.isGuid = isGuid;
		this.mappingType = MappingType.XPATH;
	}

	public Mapping(String queryString, String subject, Predicates predicate) {
		this.subject = subject;
		this.queryString = queryString;
		this.predicates = new Predicates[] {predicate};
		this.isGuid = false;
		this.mappingType = MappingType.XPATH;
	}
	
	public Mapping(String queryString, String subject, Predicates predicate, MappingType mappingType) {
		this.subject = subject;
		this.queryString = queryString;
		this.predicates = new Predicates[] {predicate};
		this.isGuid = false;
		this.mappingType = mappingType;
	}

	/**
	 * @return the xpath
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @return the targetQNames
	 */
	public Predicates[] getPredicates() {
		return predicates;
	}

	/**
	 * @return the isGuid
	 */
	public boolean isGuid() {
		return isGuid;
	}
}
