package org.ala.web;

public class StoredRanking {

	protected String uri;
	protected boolean positive;
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null){
			if(obj instanceof StoredRanking){
				StoredRanking sr = (StoredRanking) obj;
				if(sr.getUri()!=null
					&& sr.getUri().equals(uri)
					){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	/**
	 * @return the positive
	 */
	public boolean isPositive() {
		return positive;
	}
	/**
	 * @param positive the positive to set
	 */
	public void setPositive(boolean positive) {
		this.positive = positive;
	}
}
