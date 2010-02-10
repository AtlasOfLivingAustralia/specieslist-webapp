package org.ala.model;
/**
 * Model object for a Common Name.
 * 
 * @author Dave Martin
 */
public class CommonName extends AttributableObject {

	/** Nullable GUID for this common name */
	public String guid; 
	public String nameString;

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof CommonName){
			CommonName tc = (CommonName) obj;
			if(tc.getNameString()!=null && nameString!=null && nameString.equalsIgnoreCase(nameString)){
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