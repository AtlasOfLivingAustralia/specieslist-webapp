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
	
	protected String defaultCasServer = "https://auth.ala.org.au";
	protected String defaultCentralServer = "http://www.ala.org.au";
	protected String defaultSearchServer = "http://bie.ala.org.au";
	
	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			Principal principal = request.getUserPrincipal();
			
			String searchServer = pageContext.getServletContext().getInitParameter("searchServerName");
			if(searchServer==null){
				searchServer = defaultSearchServer;
			}
			
			String casServer = pageContext.getServletContext().getInitParameter("casServerName");
			if(casServer==null){
				casServer = defaultCasServer;
			}
			
			String centralServer = pageContext.getServletContext().getInitParameter("centralServer");		
			if(centralServer==null){
				centralServer = defaultCentralServer;
			}
			
			String query = request.getParameter("q");
			String queryAvoid = request.getParameter("xq");
			if(queryAvoid!=null || query==null || "".equals(query.trim()) ){
				query = "Search the Atlas";
			}
			
			// if a return path isnt supplied, construct one from current request 
			if (returnUrlPath == null || returnUrlPath.equals("")) {
				StringBuffer requestURL = request.getRequestURL();
				String queryString = request.getQueryString();
				if(queryString!=null || "".equals(queryString)){
					requestURL.append('?');
					requestURL.append(queryString.replaceAll("\\+", "%2B"));
				}
				returnUrlPath = requestURL.toString();
			}
			
			logger.debug("Return path URL: "+returnUrlPath);
			
			String loginLogoutListItem = "";
			if (principal == null) {
				loginLogoutListItem = "<li class='nav-login nav-right'><a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a></li>";
			} else {
				loginLogoutListItem = "<li class='nav-logout nav-right'><a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a></li>";
			}
	
			String html =
			"<div id='banner'>" + 
			"<div id='logo'>" + 
			"<a href='"+centralServer+"' title='Atlas of Living Australia home'><img src='"+centralServer+"/wp-content/themes/ala/images/ala_logo.png' width='215' height='80' alt='Atlas of Living Australia logo'/></a>" + 
			"</div><!--close logo-->" + 
			"<div id='nav'>" +
				"<!-- WP Menubar 4.7: start menu nav-site, template Superfish, CSS  -->" +
				"<ul class='sf'>" +
					"<li class='nav-home'><a href='" + centralServer + "/'><span>Home</span></a></li>" +
					"<li class='nav-explore'><a href='" + centralServer + "/explore/'><span>Explore</span></a>" +
						"<ul>" +
							"<li><a href='http://biocache.ala.org.au/explore/your-area'><span>Your Area</span></a></li>" +
							"<li><a href='http://bie.ala.org.au/regions/'><span>Regions</span></a></li>" +
							"<li><a href='" + centralServer + "/explore/species-maps/'><span>Species Maps</span></a></li>" +
							"<li><a href='http://collections.ala.org.au/public/map'><span>Natural History Collections</span></a></li>" +
							"<li><a href='" + centralServer + "/explore/themes/'><span>Themes &amp; Highlights </span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-tools'><a href='" + centralServer + "/tools-services/'><span>Tools</span></a>" +
						"<ul>" +
							"<li><a href='" + centralServer + "/tools-services/species-name-services/'><span>Taxon Web Services</span></a></li>" +
							"<li><a href='" + centralServer + "/tools-services/sds/'><span>Sensitive Data Service</span></a></li>" +
							"<li><a href='" + centralServer + "/tools-services/spatial-analysis/'><span>Spatial Analysis</span></a></li>" +
							"<li><a href='" + centralServer + "/tools-services/citizen-science/'><span>Citizen Science</span></a></li>" +
							"<li><a href='" + centralServer + "/tools-services/identification-tools/'><span>Identification Tools</span></a></li>" +
							"<li><a href='" + centralServer + "/tools-services/onlinedesktop-tools-review/'><span>Online &amp; Desktop Tools Review</span></a></li>" +
						"</ul>" +
					"</li>" +
                    "<li class='nav-share'><a href='" + centralServer + "/share/' title='Share - links, images, images, literature, your time'><span>Share</span></a>" +
                        "<ul>" +
                            "<li><a href='" + centralServer + "/share/share-links/'><span>Share links, ideas, information</span></a></li>" +
                            "<li><a href='" + centralServer + "/share/share-data/'><span>Share Datasets</span></a></li>" +
                            "<li><a href='" + centralServer + "/share/about-sharing/'><span>About Sharing</span></a></li>" +
                        "</ul>" +
                    "</li>" +
					"<li class='nav-support'><a href='" + centralServer + "/support/'><span>Support</span></a>" +
						"<ul>" +
							"<li><a href='" + centralServer + "/support/contact-us/'><span>Contact Us</span></a></li>" +
                            "<li><a href='" + centralServer + "/support/get-started/'><span>Get Started</span></a></li>" +
							"<li><a href='" + centralServer + "/support/user-feedback/'><span>User Feedback</span></a></li>" +
							"<li><a href='" + centralServer + "/support/faq/'><span>Frequently Asked Questions</span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-contact'><a href='" + centralServer + "/contact-us/'><span>Contact Us</span></a></li>" +
					"<li class='nav-about'><a href='" + centralServer + "/about/'><span>About the Atlas</span></a>" +
						"<ul>" +
							"<li><a href='" + centralServer + "/about/proviso/'><span>A Work in Progress</span></a></li>" +
							"<li><a href='" + centralServer + "/about/atlas-partners/'><span>Atlas Partners</span></a></li>" +
							"<li><a href='" + centralServer + "/about/people/'><span>Working Together</span></a></li>" +
							"<li><a href='" + centralServer + "/about/contributors/'><span>Atlas Contributors</span></a></li>" +
							"<li><a href='" + centralServer + "/about/project-time-line/'><span>Project Time Line</span></a></li>" +
							"<li><a href='" + centralServer + "/about/program-of-projects/'><span>Atlas Projects</span></li>" +
							"<li><a href='" + centralServer + "/about/international-collaborations/'><span>Associated Projects</span></a></li>" +
							"<li><a href='" + centralServer + "/about/communications-centre/'><span>Communications Centre</span></a></li>" +
							"<li><a href='" + centralServer + "/about/governance/'><span>Atlas Governance</span></a></li>" +
							"<li><a href='" + centralServer + "/about/terms-of-use/'><span>Terms of Use</span></a></li>" +
						"</ul>" +
					"</li>" +
					"<li class='nav-myprofile nav-right'><a href='" + casServer + "/cas/login?service="+centralServer+"/wp-login.php?redirect_to="+centralServer+"/my-profile/'><span>My Profile</span></a></li>" +
					loginLogoutListItem + 
				"</ul>" +
				"<!-- WP Menubar 4.7: end menu nav-site, template Superfish, CSS  -->" +
			"</div><!--close nav-->" +
			"<div id='wrapper_search'>" + 
			"<form id='search-form' action='"+searchServer+"/search' method='get' name='search-form'>" + 
			"<label for='search'>Search</label>" + 
			"<input type='text' class='filled' id='search' name='q' value='"+query+"'/>" + 
			"<span class='search-button-wrapper'><input type='submit' class='search-button' id='search-button' alt='Search' value='Search' /></span>" + 
			"</form>" + 
			"</div><!--close wrapper_search-->" + 
			"</div><!--close banner-->"; 
			
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