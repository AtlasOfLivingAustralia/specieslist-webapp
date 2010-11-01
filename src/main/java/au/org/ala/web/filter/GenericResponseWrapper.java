/* *************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package au.org.ala.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet filter utility class. Taken from
 * http://download.oracle.com/docs/cd/B32110_01/web.1013/b28959/filters.htm
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class GenericResponseWrapper extends HttpServletResponseWrapper {

	private ByteArrayOutputStream output;
	private int contentLength;
	private String contentType;

	public GenericResponseWrapper(HttpServletResponse response) {
		super(response);

		output = new ByteArrayOutputStream();
	}

	public byte[] getData() {
		return output.toByteArray();
	}

	public ServletOutputStream getOutputStream() {
		return new FilterServletOutputStream(output);
	}

	public PrintWriter getWriter() {
		return new PrintWriter(getOutputStream(), true);
	}

	public void setContentLength(int length) {
		this.contentLength = length;
		super.setContentLength(length);
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentType(String type) {
		this.contentType = type;
		super.setContentType(type);
	}

	public String getContentType() {
		return contentType;
	}
}