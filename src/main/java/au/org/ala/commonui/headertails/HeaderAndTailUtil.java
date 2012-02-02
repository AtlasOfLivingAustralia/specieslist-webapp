package au.org.ala.commonui.headertails;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import au.org.ala.cas.util.AuthenticationCookieUtils;
import au.org.ala.util.WebUtils;

public class HeaderAndTailUtil {
    protected static final String BANNER_HTML_URL = "http://www2.ala.org.au/datasets/banner.xml";
    protected static final String FOOTER_HTML_URL = "http://www2.ala.org.au/datasets/footer.xml";
    protected static final String GOOGLE_ANALYTICS_KEY = "UA-4355440-1";

    protected static String returnPathNullTag = "::returnPathNull::";
    protected static String centralServerTag = "::centralServer::";
    protected static String casServerTag = "::casServerR::";
    protected static String loginLogoutListItemTag = "::loginLogoutListItem::";
    protected static String searchServerTag = "::searchServer::";
    protected static String searchPathTag = "::searchPath::";
    protected static String queryTag = "::query::";
    protected static String googleAnalyticsKeyTag = "::googleAnalyticsKey::";
    
    protected static String defaultCasServer = "https://auth.ala.org.au";
    protected static String defaultCentralServer = "http://www.ala.org.au";
    protected static String defaultSearchServer = "http://bie.ala.org.au";
    protected static String searchPath = "/search";
    protected static String defaultQuery = "Search the Atlas";
    
    public static String getHeader(boolean loggedIn, String returnUrlPath) throws Exception {
        String output = null;
        
        output = getHeader(loggedIn, defaultCentralServer, defaultCasServer, defaultSearchServer, returnUrlPath, defaultQuery);
        
        return output;
    }
    
    public static String getHeader(boolean loggedIn, String centralServer, String casServer, String searchServer, String returnUrlPath, String query) throws Exception {
        String output = null;
        
        String loginLogoutListItem;
        if (loggedIn) {
            loginLogoutListItem = "<li class='nav-logout nav-right'><a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a></li>";
        } else {
            loginLogoutListItem = "<li class='nav-login nav-right'><a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a></li>";
        }
        
        output = WebUtils.getUrlContentAsString(BANNER_HTML_URL);
        
        output = output.replaceAll(centralServerTag, centralServer);
        output = output.replaceAll(casServerTag, casServer);
        output = output.replaceAll(loginLogoutListItemTag, loginLogoutListItem);
        output = output.replaceAll(searchServerTag, searchServer);
        output = output.replaceAll(searchPathTag, searchPath);
        output = output.replaceAll(queryTag, query);
        
        
        return output;
    }
    
    public static String getFooter(boolean loggedIn, String returnUrlPath) throws Exception {
        String output = null;
        
        output = getFooter(loggedIn, defaultCentralServer, defaultCasServer, returnUrlPath);
        
        return output;
    }
    
    public static String getFooter(boolean loggedIn, String centralServer, String casServer, String returnUrlPath) throws Exception {
        String output = null;
        
        output = WebUtils.getUrlContentAsString(FOOTER_HTML_URL);
        
        output = output.replaceAll(centralServerTag, centralServer);

        if (returnUrlPath.equals("")) {
            // Note: has a last class inserted
            output = output.replaceAll(returnPathNullTag, "<li id='menu-item-10433' class='last menu-item menu-item-type-post_type menu-item-10433'><a href='"+centralServer+"/my-profile/'>My Profile</a></li>");
        } else {
            // Check authentication status

            String loginLogoutAnchor;
            if (loggedIn) {
                loginLogoutAnchor = "<a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a>";
            } else {
                loginLogoutAnchor = "<a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a>";
            }

            output = output.replaceAll(returnPathNullTag,
                    "<li id='menu-item-10433' class='menu-item menu-item-type-post_type menu-item-10433'><a href='"+centralServer+"/my-profile/'>My Profile</a></li>" +
                    "<li id='menu-item-1052' class='last menu-item menu-item-type-custom menu-item-1052'>" + loginLogoutAnchor + "</li>");

            output = output.replaceAll(googleAnalyticsKeyTag, GOOGLE_ANALYTICS_KEY);
        }
        
        return output;
    }
}
