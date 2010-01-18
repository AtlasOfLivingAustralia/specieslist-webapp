package org.ala.model;

public class CommonName implements Comparable<CommonName>{

	/** Nullable GUID for this common name */
	public String guid; 
	public String nameString;
	public String infoSourceId;
	public String documentId;

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof CommonName){
			CommonName tc = (CommonName) obj;
			if(tc.getNameString()!=null && nameString!=null){
				if(tc.getGuid().equals(guid)){
					if(documentId!=null && tc.getDocumentId()!=null){
						return documentId.equals(tc.getDocumentId());
					}
				}
			}
		}
		return false;
	}	
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CommonName cn) {
		if(cn==null || this.getNameString()==null || cn.getNameString()==null)
			return -1;
		return getNameString().compareTo(cn.getNameString());
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
	/**
	 * @return the infoSourceId
	 */
	public String getInfoSourceId() {
		return infoSourceId;
	}
	/**
	 * @param infoSourceId the infoSourceId to set
	 */
	public void setInfoSourceId(String infoSourceId) {
		this.infoSourceId = infoSourceId;
	}
	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}
	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
}
