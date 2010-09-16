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

/**
 * Simple tag that writes out the banner menu list for an ALA web application.
 * 
 * @author Peter Flemming
 */
public class BannerMenuTag extends TagSupport {

	private static final long serialVersionUID = -6406031197753714478L;
	protected static Logger logger = Logger.getLogger(BannerMenuTag.class);
	
	private String returnUrlPath = "";
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		String loginLogoutListItem;
		if (returnUrlPath.equals("")) {
			loginLogoutListItem = "";
		} else {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			Principal principal = request.getUserPrincipal();
			String casServer = pageContext.getServletContext().getInitParameter("casServerName");

			if (principal == null) {
				loginLogoutListItem = "<li class='nav-login nav-right'><a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a></li>";
			} else {
				loginLogoutListItem = "<li class='nav-logout nav-right'><a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a></li>";
			}
		}

		String html =
			"<div id='nav'>" +
				"<!-- WP Menubar 4.7: start menu nav-site, template Superfish, CSS  -->" +
				"<ul class='sf'>" +
					"<li class='nav-home'><a href='http://test.ala.org.au/'><span>Home</span></a></li>" +
					"<li class='nav-explore'><a href='http://test.ala.org.au/explore/'><span>Explore</span></a>" +
						"<ul>" +
							"<li><a href='http://biocache.ala.org.au/explore/your-area'><span>Your Area</span></a></li>" +
							"<li><a href='http://bie.ala.org.au/regions/'><span>States &amp; Territories</span></a></li>" +
							"<li><a href='http://test.ala.org.au/explore/species-maps/'><span>Species Maps</span></a></li>" +
							"<li><a href='http://collections.ala.org.au/public/map'><span>Natural History Collections</span></a></li>" +
							"<li><a href='http://test.ala.org.au/explore/themes/'><span>Themes &amp; Highlights </span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-tools'><a href='http://test.ala.org.au/tools-services/'><span>Tools</span></a>" +
						"<ul>" +
							"<li><a href='http://test.ala.org.au/tools-services/creative-commons-licensing/'><span>Creative Commons licensing</span></a></li>" +
							"<li><a href='http://test.ala.org.au/tools-services/community-science/'><span>Citizen Science</span></a></li>" +
							"<li><a href='http://test.ala.org.au/tools-services/identification-tools/'><span>Identification Tools</span></a></li>" +
							"<li><a href='http://test.ala.org.au/tools-services/for-developers/'><span>For Developers</span></a></li>" +
							"<li><a href='http://test.ala.org.au/tools-services/sharing-data-through-the-atlas/'><span>Sharing data through the Atlas</span></a></li>" +
							"<li><a href='http://test.ala.org.au/tools-services/taxonomic-databases-working-group/'><span>TDWG</span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-contribute'><a href='http://test.ala.org.au/contribute/' title='Contribute - links, images, images, literature, your time'><span>Contribute</span></a>" +
						"<ul>" +
							"<li><a href='http://test.ala.org.au/contribute/general-contribute/'><span>General Comments</span></a></li>" +
							"<li><a href='http://test.ala.org.au/contribute/sighting/'><span>Record Sightings</span></a></li>" +
							"<li><a href='http://test.ala.org.au/contribute/share-links/'><span>share links, ideas, information</span></a></li>" +
							"<li><a href='http://test.ala.org.au/contribute/share-images/'><span>Share Photos</span></a></li>" +
							"<li><a href='http://test.ala.org.au/contribute/share-data/'><span>Upload Data Sets</span></a></li>" +
							"<li><a href='http://test.ala.org.au/contribute/share-analogue-data/'><span>Non-digital Information</span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-support'><a href='http://test.ala.org.au/support/'><span>Support</span></a>" +
						"<ul>" +
							"<li><a href='http://test.ala.org.au/support/get-started/'><span>Get Started</span></a></li>" +
							"<li><a href='http://test.ala.org.au/support/forum/'><span>Forum</span></a></li>" +
							"<li><a href='http://test.ala.org.au/support/faq/'><span>Frequently Asked Questions</span></a></li>" +
							"<li><a href='http://test.ala.org.au/support/how-to/'><span>How To</span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-contact'><a href='http://test.ala.org.au/contact-us/'><span>Contact Us</span></a></li>" +
					"<li class='nav-about'><a href='http://test.ala.org.au/about/'><span>About the Atlas</span></a>" +
						"<ul>" +
							"<li><a href='http://test.ala.org.au/about/people/'><span>Working Together</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/portfolio-of-projects/'><span>Projects</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/project-time-line/'><span>Project Time Line</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/governance/'><span>Governance</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/media-centre/'><span>Media Centre</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/newsevents/'><span>News &amp; Events</span></a></li>" +
							"<li><a href='http://test.ala.org.au/about/resources/'><span>Resources</span></a></li>" +
						"</ul>" +
					"</li>" +
					loginLogoutListItem + 
				"</ul>" +
				"<!-- WP Menubar 4.7: end menu nav-site, template Superfish, CSS  -->" +
			"</div><!--close nav-->";
		
		try {
			pageContext.getOut().print(html);
		} catch (Exception e) {
			logger.error("BannerMenuTag: " + e.getMessage(), e);
			throw new JspTagException("BannerMenuTag: " + e.getMessage());
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