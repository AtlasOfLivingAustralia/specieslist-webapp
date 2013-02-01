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
package org.ala.repository;
/**
 * A triple encapsulates an assertion about a thing. 
 * 
 * @author Dave Martin
 *
 * @param <Subject>
 * @param <Predicate>
 * @param <TheObject>
 */
public class Triple<Subject, Predicate, TheObject> {

	protected Subject subject;
	protected Predicate predicate;
	protected TheObject object;
	
	public Triple() {}
	
	public Triple(Subject subject, Predicate predicate, TheObject object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	/**
	 * @return the predicate
	 */
	public Predicate getPredicate() {
		return predicate;
	}
	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}
	/**
	 * @return the object
	 */
	public TheObject getObject() {
		return object;
	}
	/**
	 * @param object the object to set
	 */
	public void setObject(TheObject object) {
		this.object = object;
	}
}