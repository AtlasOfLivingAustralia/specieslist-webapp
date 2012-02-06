package au.org.ala.commonui.headertails;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Util class to hold common code for both the banner/header and footer tag classes
 *
 * @author Nick dos Remedios (nick.dosremedios@csiro.au)
 */
public class HeaderAndTailUtil {
    // these fields can be overrided by a properties file (see below)
    protected static String googleAnalyticsKey = "UA-4355440-1";
    protected static String headerHtmlUrl = "http://www2.ala.org.au/datasets/banner.xml";
    protected static String footerHtmlUrl = "http://www2.ala.org.au/datasets/footer.xml";
    // template-style substitution variables
    protected static String returnPathNullTag = "::returnPathNull::";
    protected static String centralServerTag = "::centralServer::";
    protected static String casServerTag = "::casServerR::";
    protected static String loginLogoutListItemTag = "::loginLogoutListItem::";
    protected static String searchServerTag = "::searchServer::";
    protected static String searchPathTag = "::searchPath::";
    protected static String queryTag = "::query::";
    protected static String googleAnalyticsKeyTag = "::googleAnalyticsKey::";
    // replacement variables
    protected static String defaultCasServer = "https://auth.ala.org.au";
    protected static String defaultCentralServer = "http://www.ala.org.au";
    protected static String defaultSearchServer = "http://bie.ala.org.au";
    protected static String searchPath = "/search";
    protected static String defaultQuery = "Search the Atlas";
    // logging
    private final static Logger logger = Logger.getLogger(HeaderAndTailUtil.class);

    /**
     * Look for field overrides in properties file
     */
    static {
        Properties prop = new Properties();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("headerTails.properties");

        try {
            prop.load(in);
            if (prop.getProperty("include.headerUrl") != null) {
                headerHtmlUrl = prop.getProperty("include.headerUrl");
            }
            if (prop.getProperty("include.footerUrl") != null) {
                footerHtmlUrl = prop.getProperty("include.footerUrl");
            }
            if (prop.getProperty("googleAnalyticsKey") != null) {
                googleAnalyticsKey = prop.getProperty("googleAnalyticsKey");
            }
            in.close();
        } catch (Exception e) {
            logger.debug("Error loading properties file: " + e, e);
        }
    }
    
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
        
        //output = WebUtils.getUrlContentAsString(headerHtmlUrl);
        output = GetWebContent.getInstance().getContent(headerHtmlUrl);
        
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

        //output = WebUtils.getUrlContentAsString(footerHtmlUrl);
        output = GetWebContent.getInstance().getContent(footerHtmlUrl);
        
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

            output = output.replaceAll(googleAnalyticsKeyTag, googleAnalyticsKey);
        }
        
        return output;
    }
}
