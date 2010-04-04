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
/**
 * Model object for a Common Name.
 * 
 * @author Dave Martin
 */
public class CommonName extends AttributableObject implements Comparable<CommonName> {

	/** Nullable GUID for this common name */
	public String guid; 
	public String nameString;

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommonName o) {
		//check the infosources
		if(o.getNameString()!=null && nameString!=null){
			return nameString.compareTo(o.getNameString());
		}
		return -1;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof CommonName){
			CommonName tc = (CommonName) obj;
			if(nameString!=null && nameString.equalsIgnoreCase(tc.getNameString())){
				//compare guids if not null
				if(tc.getGuid()!=null && guid!=null){
					return tc.getGuid().equals(guid);
				}
				if(documentId!=null && tc.getDocumentId()!=null){
					return documentId.equals(tc.getDocumentId());
				}
			}
		}
		return false;
	}
	
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
	 * @return the nameString
	 */
	public String getNameString() {
		return nameString;
	}
	/**
	 * @param nameString the nameString to set
	 */
	public void setNameString(String nameString) {
		this.nameString = nameString;
	}
}