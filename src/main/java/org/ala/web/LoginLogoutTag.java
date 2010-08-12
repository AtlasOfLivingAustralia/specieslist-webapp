/**************************************************************************
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
package org.ala.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;

/**
 * Simple tag that writes out a login/logout anchor element.
 * 
 * @author Peter Flemming
 */
public class LoginLogoutTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(LoginLogoutTag.class);
	
	private String returnUrlPath = "";
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() throws JspException {
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		Principal principal = request.getUserPrincipal();
		String casServer = pageContext.getServletContext().getInitParameter("casServerName");

		String html;
		if (principal == null) {
			html = "<a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a>\n";
		} else {
			html = "<a href='" + casServer + "/cas/logout'>Log out</a>\n";
		}
		
		try {
			pageContext.getOut().print(html);
		} catch (Exception e) {
			logger.error("LoginLogoutTag: " + e.getMessage(), e);
			throw new JspTagException("LoginLogoutTag: " + e.getMessage());
		}
		
		return super.doStartTag();
	}

	public String getReturnUrlPath() {
		return returnUrlPath;
	}

	public void setReturnUrlPath(String returnUrlPath) {
		this.returnUrlPath = returnUrlPath;
	}
}