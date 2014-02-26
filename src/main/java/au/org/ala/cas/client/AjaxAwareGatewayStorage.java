package au.org.ala.cas.client;

import org.jasig.cas.client.authentication.GatewayResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The AjaxAwareGatewayStorage class is a copy of the CAS DefaultGatewayResolverImpl with one difference:
 * if the request is determined to be an ajax request, the next call to hasGatewayedAlready() will return false as the
 * browser will not be follow the redirect to CAS due to domain origin restrictions.
 * Note that the mechanism used to detect the ajax request relies on the default jquery behaviour of setting the
 * X-Requested-With header.
 */
public class AjaxAwareGatewayStorage implements GatewayResolver {

    public static final String CONST_CAS_GATEWAY = "_const_cas_gateway_";

    public boolean hasGatewayedAlready(final HttpServletRequest request,
                                       final String serviceUrl) {
        final HttpSession session = request.getSession(false);

        if (session == null) {
            return false;
        }


        final boolean result = session.getAttribute(CONST_CAS_GATEWAY) != null;
        session.removeAttribute(CONST_CAS_GATEWAY);
        return result;
    }

    public String storeGatewayInformation(final HttpServletRequest request,
                                          final String serviceUrl) {

        if (!isAjax(request)) {
            request.getSession(true).setAttribute(CONST_CAS_GATEWAY, "yes");
        }
        return serviceUrl;
    }

    /**
     * Checks the X-Requested-With header for XMLHttpRequest (which is set by jquery).
     * @param request the request to check.
     * @return true if the request was initiated via ajax.
     */
    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
