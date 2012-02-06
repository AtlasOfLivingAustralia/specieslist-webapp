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

/**
 * Simple tag that writes out the footer menu list for an ALA web application.
 * 
 * @author Tommy Wang (tommy.wang@csiro.au)
 * @author Nick dos Remedios (nick.dosremedios@csiro.au)
 */
public class FooterMenuTag extends TagSupport {

    private static final long serialVersionUID = -6406031197753714478L;
    protected static Logger logger = Logger.getLogger(FooterMenuTag.class);
    protected String defaultCentralServer = "http://www.ala.org.au";
    private String returnUrlPath = "";

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException {
        // Tags are handled by Servlet container so to use Spring we need to grab the application context
//        // via the RequestContextAwareTag class (in place of TagSupport)
//        RequestContext context = super.getRequestContext();
//        WebApplicationContext webAppContext = context.getWebApplicationContext();
//        HeaderAndTailUtil headerAndTailUtil = webAppContext.getBean("headerAndTailUtil", HeaderAndTailUtil.class);

        String centralServer = pageContext.getServletContext().getInitParameter("centralServer");
        if (centralServer==null){
            centralServer = defaultCentralServer;
        }

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String casServer = pageContext.getServletContext().getInitParameter("casServerName");

        // Check authentication status
        Principal principal = request.getUserPrincipal();
        boolean loggedIn;
        if (principal != null) {
            loggedIn = true;
        } else {
            loggedIn = AuthenticationCookieUtils.isUserLoggedIn(request);
        }



        String html = "<div>Footer placeholder</div>";
        try {
            html = HeaderAndTailUtil.getFooter(loggedIn, centralServer, casServer, centralServer);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            //e1.printStackTrace();
            logger.error("FooterMenuTag: " + e1.getMessage(), e1);
        }

        try {
            pageContext.getOut().print(html);
            logger.debug(html.toString());
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