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

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;

/**
 * Simple tag that writes out a logged in user's name.
 * 
 * @author Peter Flemming
 */
public class UserNameTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(UserNameTag.class);
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

		String userName = "";
		if (principal != null) {
			userName = principal.getAttributes().get("firstname").toString() + " " + principal.getAttributes().get("lastname").toString();
		}
		
		try {
			pageContext.getOut().print(userName);
		} catch (Exception e) {
			logger.error("UserNameTag: " + e.getMessage(), e);
			throw new JspTagException("UserNameTag: " + e.getMessage());
		}
		
		return super.doStartTag();
	}

}