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

import au.org.ala.cas.util.PatternMatchingUtils;

/**
 * Filter that provides filtering based on a list URI regex patterns. If a URI matches one
 * of the regex patterns then the underlying filter (defined by the <code>&lt;init-param&gt; filterClass</code>)
 * is invoked.<p>
 * The list of regex's are delimited by commas and are defined in the <code>&lt;context-param&gt; uriFilterPattern</code>.
 * <p>
 * An example of usage is shown in the following web.xml fragment,
 * <p><pre>
     ...
     &lt;context-param&gt;
         &lt;param-name&gt;uriFilterPattern&lt;/param-name&gt;
         &lt;param-value&gt;/, /occurrences/\d+&lt;/param-value&gt;
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
	
	private Filter filter;
	private List<Pattern> inclusionPatterns;
	
	public void init(FilterConfig filterConfig) throws ServletException {

		String includedUrlPattern = filterConfig.getServletContext().getInitParameter("uriFilterPattern");
		logger.debug("Read includedUrlPattern = '" + includedUrlPattern + "'");
		this.inclusionPatterns = PatternMatchingUtils.getPatternList(includedUrlPattern);

		String className = filterConfig.getInitParameter("filterClass");
		try {
			Class<?> c = Class.forName(className);
			filter = (Filter) c.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		filter.init(filterConfig);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		String requestUri = ((HttpServletRequest) request).getRequestURI();
		if (filter instanceof AuthenticationFilter) {
			logger.debug("Request Uri = '" + requestUri + "'");
		}
		
		if (PatternMatchingUtils.matches(requestUri, inclusionPatterns)) {
			if (filter instanceof AuthenticationFilter) {
				logger.debug("Forwarding URI '" + requestUri + "' to CAS authentication filters because it matches uriFilterPattern");
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
