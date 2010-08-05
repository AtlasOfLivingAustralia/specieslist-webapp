package org.ala.model;
/**
 * A simple ranking POJO
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class Ranking implements Comparable<Ranking> {

	String userIP;
	String userId;
	String uri;
	boolean positive;
	
	@Override
	public int compareTo(Ranking o) {
		return 0;
	}

	/**
	 * @return the userIP
	 */
	public String getUserIP() {
		return userIP;
	}

	/**
	 * @param userIP the userIP to set
	 */
	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
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
}
