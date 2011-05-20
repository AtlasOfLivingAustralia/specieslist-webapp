/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
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
package au.org.ala.cas.client;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AuthenticationFilter;

import au.org.ala.cas.util.AuthenticationCookieUtils;
import au.org.ala.cas.util.PatternMatchingUtils;

/**
 * Meta filter that provides filtering based on URI patterns that require authentication (and thus redirection) to the CAS server.
 * <p>
 * The list of URI patterns is specified as a comma delimited list of regular expressions in a &lt;context-param&gt;, <code>uriFilterPattern</code>.
 * <p>
 * So if a request's path matches one of the URI patterns then the filter specified by the <code>filterClass</code> &lt;init-param&gt; is invoked.
 * <p>
 * The <code>contextPath</code> parameter value (if present) is prefixed to each URI pattern defined in the <code>uriFilterPattern</code> list.
 * <p>
 * An example of usage is shown in the following web.xml fragment,
 * <p><pre>
     ...
     &lt;context-param&gt;
         &lt;param-name&gt;contextPath&lt;/param-name&gt;
         &lt;param-value&gt;/biocache-webapp&lt;/param-value&gt;
     &lt;/context-param&gt;

     &lt;context-param&gt;
         &lt;param-name&gt;uriFilterPattern&lt;/param-name&gt;
         &lt;param-value&gt;/occurrences/\d+&lt;/param-value&gt;
     &lt;/context-param&gt;

     &lt;!-- CAS Authentication Service filters --&gt;
     &lt;filter&gt;
         &lt;filter-name&gt;CAS Authentication Filter&lt;/filter-name&gt;
         &lt;filter-class&gt;au.org.ala.cas.client.UriFilter&lt;/filter-class&gt;
         &lt;init-param&gt;
             &lt;param-name&gt;filterClass&lt;/param-name&gt;
             &lt;param-value&gt;org.jasig.cas.client.authentication.AuthenticationFilter&lt;/param-value&gt;
         &lt;/init-param&gt;
         &lt;init-param&gt;
             &lt;param-name&gt;casServerLoginUrl&lt;/param-name&gt;
             &lt;param-value&gt;https://auth.ala.org.au/cas/login&lt;/param-value&gt;
         &lt;/init-param&gt;
         &lt;init-param&gt;
             &lt;param-name&gt;gateway&lt;/param-name&gt;
             &lt;param-value&gt;true&lt;/param-value&gt;
         &lt;/init-param&gt;
     &lt;/filter&gt;
     ...
 * </pre>
 * @author peter flemming
 */
public class UriFilter implements Filter {

    private final static Logger logger = Logger.getLogger(UriFilter.class);

    private static final String URI_FILTER_PATTERN = "uriFilterPattern";
    private static final String URI_EXCLUSION_FILTER_PATTERN = "uriExclusionFilterPattern";
    private static final String AUTHENTICATE_ONLY_IF_LOGGED_IN_FILTER_PATTERN = "authenticateOnlyIfLoggedInFilterPattern";

    private Filter filter;
    private String contextPath;
    private List<Pattern> uriInclusionPatterns;
    private List<Pattern> authOnlyIfLoggedInPatterns;
    private List<Pattern> uriExclusionPatterns;

    public void init(FilterConfig filterConfig) throws ServletException {

        //
        // Get contextPath parameter
        //
        this.contextPath = filterConfig.getServletContext().getInitParameter("contextPath");
        if (this.contextPath == null) {
            this.contextPath = "";
        } else {
            logger.debug("Context path = '" + contextPath + "'");
        }

        //
        // Get URI inclusion filter patterns
        //
        String includedUrlPattern = filterConfig.getServletContext().getInitParameter(URI_FILTER_PATTERN);
        if (includedUrlPattern == null) {
            includedUrlPattern = "";
        }
        logger.debug("Included URI Pattern = '" + includedUrlPattern + "'");
        this.uriInclusionPatterns = PatternMatchingUtils.getPatternList(contextPath, includedUrlPattern);

        //
        // Get URI exclusion filter patterns
        //
        String excludedUrlPattern = filterConfig.getServletContext().getInitParameter(URI_EXCLUSION_FILTER_PATTERN);
        if (excludedUrlPattern == null) {
            excludedUrlPattern = "";
        }
        logger.debug("Excluded URI Pattern = '" + excludedUrlPattern + "'");
        this.uriExclusionPatterns = PatternMatchingUtils.getPatternList(excludedUrlPattern);

        //
        // Get Authenticate Only if Logged in filter patterns
        //
        String authOnlyIfLoggedInPattern = filterConfig.getServletContext().getInitParameter(AUTHENTICATE_ONLY_IF_LOGGED_IN_FILTER_PATTERN);
        if (authOnlyIfLoggedInPattern == null) {
            authOnlyIfLoggedInPattern = "";
        }
        logger.debug("Authenticate Only if Logged in Pattern = '" + authOnlyIfLoggedInPattern + "'");
        this.authOnlyIfLoggedInPatterns = PatternMatchingUtils.getPatternList(contextPath, authOnlyIfLoggedInPattern);

        //
        // Get target filter class name
        //
        String className = filterConfig.getInitParameter("filterClass");
        try {
            Class<?> c = Class.forName(className);
            filter = (Filter) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        filter.init(filterConfig);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        String requestUri = ((HttpServletRequest) request).getRequestURI();
        if (filter instanceof AuthenticationFilter) {
            logger.debug("Request Uri = '" + requestUri + "'");
        }

        if (PatternMatchingUtils.matches(requestUri, uriExclusionPatterns)) {
            if (filter instanceof AuthenticationFilter) {
                logger.debug("Ignoring URI because it matches " + URI_EXCLUSION_FILTER_PATTERN);
            }
            chain.doFilter(request, response);
        } else if (PatternMatchingUtils.matches(requestUri, uriInclusionPatterns)) {
            if (filter instanceof AuthenticationFilter) {
                logger.debug("Forwarding URI '" + requestUri + "' to CAS authentication filters because it matches " + URI_FILTER_PATTERN);
            }
            filter.doFilter(request, response, chain);
        } else if (PatternMatchingUtils.matches(requestUri, authOnlyIfLoggedInPatterns) &&
                    AuthenticationCookieUtils.isUserLoggedIn((HttpServletRequest) request)) {
            if (filter instanceof AuthenticationFilter) {
                logger.debug("Forwarding URI '" + requestUri + "' to CAS authentication filters because it matches " + AUTHENTICATE_ONLY_IF_LOGGED_IN_FILTER_PATTERN + " and ALA-Auth cookie exists");
            }
            filter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        filter.destroy();
    }
}
