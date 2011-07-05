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
package au.org.ala.commonui.headertails;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

import au.org.ala.cas.util.AuthenticationCookieUtils;
import au.org.ala.util.WebUtils;

/**
 * Simple tag that writes out the banner menu list for an ALA web application.
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 */
public class BannerMenuTag extends TagSupport {

    private static final long serialVersionUID = -6406031197753714478L;
    protected static Logger logger = Logger.getLogger(BannerMenuTag.class);
    
    private String returnUrlPath = "";
    
    protected String defaultCasServer = "https://auth.ala.org.au";
    protected String defaultCentralServer = "http://www.ala.org.au";
    protected String defaultSearchServer = "http://bie.ala.org.au";
    protected String searchPath = "/search";
    protected final String BANNER_HTML_URL = "http://www2.ala.org.au/datasets/banner.xml";
    protected String centralServerTag = "::centralServer::";
    protected String casServerTag = "::casServerR::";
    protected String loginLogoutListItemTag = "::loginLogoutListItem::";
    protected String searchServerTag = "::searchServer::";
    protected String searchPathTag = "::searchPath::";
    protected String queryTag = "::query::";
    
    private boolean populateSearchBox = true;
    
    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException {
        try {
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            
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
            if(!populateSearchBox || queryAvoid!=null || query==null || "".equals(query.trim()) ){
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
            
            // Check authentication status
            Principal principal = request.getUserPrincipal();
            boolean loggedIn;
            if (principal != null) {
                loggedIn = true;
            } else {
                loggedIn = AuthenticationCookieUtils.isUserLoggedIn(request);
            }

            String html = HeaderAndTailUtil.getHeader(loggedIn, centralServer, casServer, searchServer, returnUrlPath, query);
//                WebUtils.getUrlContentAsString(BANNER_HTML_URL);
//            
//            html = html.replaceAll(centralServerTag, centralServer);
//            html = html.replaceAll(casServerTag, casServer);
//            html = html.replaceAll(loginLogoutListItemTag, loginLogoutListItem);
//            html = html.replaceAll(searchServerTag, searchServer);
//            html = html.replaceAll(searchPathTag, searchPath);
//            html = html.replaceAll(queryTag, query);
            
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

    /**
     * @param populateSearchBox the populateSearchBox to set
     */
    public void setPopulateSearchBox(boolean populateSearchBox) {
        this.populateSearchBox = populateSearchBox;
    }

	/**
	 * @return the searchPath
	 */
	public String getSearchPath() {
		return searchPath;
	}

	/**
	 * @param searchPath the searchPath to set
	 */
	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}
}