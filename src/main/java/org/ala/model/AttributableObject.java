package org.ala.model;

public class AttributableObject implements Comparable<AttributableObject>{
	//attribution
	public String infoSourceId;
	public String documentId;
	
	@Override
	public int compareTo(AttributableObject o) {
		
		//check the infosources
		if(o.getInfoSourceId()!=null && infoSourceId!=null){
			return o.getInfoSourceId().compareTo(infoSourceId);
		}
		return -1;
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
