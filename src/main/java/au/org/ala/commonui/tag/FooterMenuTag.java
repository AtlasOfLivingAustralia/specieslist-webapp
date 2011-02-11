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

import au.org.ala.cas.util.CookieUtils;

/**
 * Simple tag that writes out the footer menu list for an ALA web application.
 * 
 * @author Peter Flemming
 */
public class FooterMenuTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(FooterMenuTag.class);
	private static final String GOOGLE_ANALYTICS_KEY = "UA-4355440-1";
	protected String defaultCentralServer = "http://www.ala.org.au";
	private String returnUrlPath = "";
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		String centralServer = pageContext.getServletContext().getInitParameter("centralServer");
		if(centralServer==null){
			centralServer = defaultCentralServer;
		}

		StringBuilder html = new StringBuilder(
			"<div id='footer-nav'>" +
				"<ul id='menu-footer-site'>" +
					"<li id='menu-item-5064' class='menu-item menu-item-type-post_type menu-item-5064'><a href='"+centralServer+"'>Home</a></li>" +
                    "<li id='menu-item-8093' class='menu-item menu-item-type-post_type current-menu-item page_item page-item-883 current_page_item menu-item-8093'><a href='"+centralServer+"/explore/'>Explore</a></li>" +
					"<li id='menu-item-5065' class='menu-item menu-item-type-post_type menu-item-5065'><a href='"+centralServer+"/tools-services/'>Tools</a></li>" +
                    "<li id='menu-item-8092' class='menu-item menu-item-type-post_type menu-item-8092'><a href='"+centralServer+"/share/'>Share</a></li>" +
					"<li id='menu-item-1066' class='menu-item menu-item-type-post_type menu-item-1066'><a href='"+centralServer+"/support/'>Support</a></li>" +
					"<li id='menu-item-1067' class='menu-item menu-item-type-post_type menu-item-1067'><a href='"+centralServer+"/support/contact-us/'>Contact Us</a></li>" +
                    "<li id='menu-item-5068' class='menu-item menu-item-type-post_type menu-item-5068'><a href='"+centralServer+"/about/'>About the Atlas</a></li>");
		
        if (returnUrlPath.equals("")) {
            // Note: has a last class inserted
            html.append("<li id='menu-item-10433' class='last menu-item menu-item-type-post_type menu-item-10433'><a href='"+centralServer+"/my-profile/'>My Profile</a></li>");
        } else {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			String casServer = pageContext.getServletContext().getInitParameter("casServerName");

			String loginLogoutAnchor;
			if (CookieUtils.alaAuthCookieExists(request)) {
                loginLogoutAnchor = "<a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a>";
			} else {
                loginLogoutAnchor = "<a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a>";
			}

			html.append(
					"<li id='menu-item-10433' class='menu-item menu-item-type-post_type menu-item-10433'><a href='"+centralServer+"/my-profile/'>My Profile</a></li>" +
                    "<li id='menu-item-1052' class='last menu-item menu-item-type-custom menu-item-1052'>" + loginLogoutAnchor + "</li>");
		}

		html.append(
				"</ul>" +
				"<ul id='menu-footer-legal'>" +
					"<li id='menu-item-1045' class='menu-item menu-item-type-post_type menu-item-1045'><a href='"+centralServer+"/about/terms-of-use/'>Terms of Use</a></li>" +
					"<li id='menu-item-1042' class='menu-item menu-item-type-post_type menu-item-1042'><a href='"+centralServer+"/about/terms-of-use/citing-the-atlas/'>Citing the Atlas</a></li>" +
					"<li id='menu-item-12256' class='menu-item menu-item-type-post_type menu-item-12256'><a href='"+centralServer+"/about/privacy-policy'>Privacy Policy</a></li>" +
					"<li id='menu-item-3090' class='last menu-item menu-item-type-post_type menu-item-3090'><a href='"+centralServer+"/site-map/'>Site Map</a></li>" +
				"</ul>" +
			"</div>" +
            "<div class='copyright'>" +
                "<p><a href='http://creativecommons.org/licenses/by/3.0/au/' title='External link to Creative Commons' class='left no-pipe'><img src='"+centralServer+"/wp-content/themes/ala/images/creativecommons.png' width='88' height='31' alt=''></a>This " +
                    "site is licensed under a <a href='http://creativecommons.org/licenses/by/3.0/au/' title='External link to Creative Commons'>Creative Commons Attribution 3.0 Australia License</a>" +
                "</p>" +
                "<p>" +
                    "Provider content may be covered by other <span class='asterisk-container'><a href='"+centralServer+"/about/terms-of-use/' title='Terms of Use'>Terms of Use</a>.</span>" +
                "</p>" +
            "</div>" +
			"<script type='text/javascript'> " +
                "var gaJsHost = (('https:' == document.location.protocol) ? 'https://ssl.' : 'http://www.');" +
                "document.write(unescape('%3Cscript src=\"' + gaJsHost + 'google-analytics.com/ga.js\" type=\"text/javascript\"%3E%3C/script%3E'));" +
            "</script> " +
            "<script type='text/javascript'> " +
            	"try{"+
                "var pageTracker = _gat._getTracker('" + GOOGLE_ANALYTICS_KEY + "');" +
                "pageTracker._initData();" +
                "pageTracker._trackPageview();" +
                "} catch(err) {}" +
            "</script>\n" );
		
		try {
			pageContext.getOut().print(html.toString());
		} catch (Exception e) {
			logger.error("FooterMenuTag: " + e.getMessage(), e);
			throw new JspTagException("FooterMenuTag: " + e.getMessage());
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