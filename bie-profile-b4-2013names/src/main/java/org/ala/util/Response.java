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
package org.ala.util;
/**
 * A simple POJO that contains the results of a request.
 * 
 * @author Dave Martin
 */
public class Response {

	/** The URL from whence the content came */
	protected String responseUrl;
	/** The mime type, as retrieved from HTTP headers */
	protected String contentType;
	/** The raw content retrieve as bytes */
	protected byte[] responseAsBytes;
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**
	 * @return the responseAsBytes
	 */
	public byte[] getResponseAsBytes() {
		return responseAsBytes;
	}
	/**
	 * @param responseAsBytes the responseAsBytes to set
	 */
	public void setResponseAsBytes(byte[] responseAsBytes) {
		this.responseAsBytes = responseAsBytes;
	}
	/**
	 * @return the responseUrl
	 */
	public String getResponseUrl() {
		return responseUrl;
	}
	/**
	 * @param responseUrl the responseUrl to set
	 */
	public void setResponseUrl(String responseUrl) {
		this.responseUrl = responseUrl;
	}
}
