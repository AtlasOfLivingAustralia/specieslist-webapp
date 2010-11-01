/* *************************************************************************
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

package au.org.ala.web.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * JSONP servlet filter to wrap JSON output with a JS callback name.
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class JsonpFilter implements Filter {
    private final static Logger logger = Logger.getLogger(JsonpFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        @SuppressWarnings("unchecked")
		Map<String, String[]> parms = request.getParameterMap();

		if (parms.containsKey("callback")) {
			logger.debug("Wrapping response with JSONP callback '" + parms.get("callback")[0] + "'");

			OutputStream out = response.getOutputStream();

			GenericResponseWrapper wrapper = new GenericResponseWrapper((HttpServletResponse) response);

			chain.doFilter(request, wrapper);

			out.write((parms.get("callback")[0] + "(").getBytes());
			out.write(wrapper.getData());
			out.write(");".getBytes());

			wrapper.setContentType("text/javascript;charset=UTF-8");

			out.close();
		} else {
			chain.doFilter(request, response);
		}
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void destroy()  {}

}
