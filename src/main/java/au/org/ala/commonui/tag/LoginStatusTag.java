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
package au.org.ala.commonui.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 * Simple tag that writes out principal name if logged on or a login link if not.
 * 
 * @author Peter Flemming
 */
public class LoginStatusTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(LoginStatusTag.class);
	
	private String returnUrlPath = "";
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
		String casServer = pageContext.getServletContext().getInitParameter("casServerName");

		String html;
		if (principal == null) {
			html = "<p>You are not logged in.  <a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a></p>\n";
		} else {
			String userId = principal.getName();
			String email = principal.getAttributes().get("email").toString();
			html = "<p>Logged in as <b>" + userId + "</b></p>\n" +
					"<input type='hidden' name='creator' value='" + userId + "'/>\n" +
					"<input type='hidden' name='creator-email' value='" + email + "'/>\n";
		}
		
		try {
			pageContext.getOut().print(html);
		} catch (Exception e) {
			logger.error("LoginStatusTag: " + e.getMessage(), e);
			throw new JspTagException("LoginStatusTag: " + e.getMessage());
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