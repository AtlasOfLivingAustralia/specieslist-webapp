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
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;

/**
 * Filter that wraps the HttpServletRequestWrapper to override the following methods,
 * <ul>
 * <li>{@link HttpServletRequest#getUserPrincipal()}</li>
 * <li>{@link HttpServletRequest#getRemoteUser()}</li>
 * <li>{@link HttpServletRequest#isUserInRole(String)}</li>
 * </ul><p>
 * This code has been shamelessly copied from {@link org.jasig.cas.client.util.HttpServletRequestWrapperFilter}
 * since that class is final and cannot be extended.
 * <p>
 * Only the <code>isUserInRole()</code> needed to
 * be overridden to accommodate a csv list of roles in the returned user attributes.
 * 
 * @author peter.flemming@csiro.au
 *
 */
public class AlaHttpServletRequestWrapperFilter extends AbstractConfigurationFilter {
	
	private String roleAttribute;
	private boolean ignoreCase;

	public void init(FilterConfig filterConfig) throws ServletException {
	    filterConfig = new AlaFilterConfig(filterConfig);
        this.roleAttribute = getPropertyFromInitParams(filterConfig, "roleAttribute", "authority");
        this.ignoreCase = Boolean.parseBoolean(getPropertyFromInitParams(filterConfig, "ignoreCase", "true"));
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
        final AttributePrincipal principal = retrievePrincipalFromSessionOrRequest(request);

        chain.doFilter(new AlaHttpServletRequestWrapper((HttpServletRequest) request, principal), response);
	}

    private AttributePrincipal retrievePrincipalFromSessionOrRequest(final ServletRequest servletRequest) {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpSession session = request.getSession(false);
        final Assertion assertion = (Assertion) (session == null ? request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));

        return assertion == null ? null : assertion.getPrincipal();
    }

    public void destroy() {
		// Do nothing
	}

    class AlaHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final AttributePrincipal principal;

        AlaHttpServletRequestWrapper(final HttpServletRequest request, final AttributePrincipal principal) {
            super(request);
            this.principal = principal;
        }

        public Principal getUserPrincipal() {
            return this.principal;
        }

        public String getRemoteUser() {
            return principal != null ? this.principal.getName() : null;
        }

        public boolean isUserInRole(final String role) {
            if (CommonUtils.isBlank(role)) {
                log.debug("No valid role provided.  Returning false.");
                return false;
            }

            if (this.principal == null) {
                log.debug("No Principal in Request.  Returning false.");
                return false;
            }

            if (CommonUtils.isBlank(roleAttribute)) {
                log.debug("No Role Attribute Configured. Returning false.");
                return false;
            }

            String roles = (String) this.principal.getAttributes().get(roleAttribute);
            
    		if (roles != null && !roles.equals("")) {
    			for (String roleValue : roles.split(",")) {
    				if (rolesEqual(role, roleValue.trim())) {
                        log.debug("User [" + getRemoteUser() + "] is in role [" + role + "]: " + true);
                        return true;
    				}
    			}
    		} else {
    			log.debug("No role values in attributes. Returning false.");
    			return false;
    		}

            return false;
        }
        
        /**
         * Determines whether the given role is equal to the candidate
         * role attribute taking into account case sensitivity.
         *
         * @param given  Role under consideration.
         * @param candidate Role that the current user possesses.
         *
         * @return True if roles are equal, false otherwise.
         */
        private boolean rolesEqual(final String given, final String candidate) {
            return ignoreCase ? given.equalsIgnoreCase(candidate) : given.equals(candidate);
        }
    }
}
