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
    protected static String bannerHtmlUrl = "http://www2.ala.org.au/datasets/banner.html";
    protected static String menuHtmlUrl = "http://www2.ala.org.au/datasets/menu.html";
    protected static String footerHtmlUrl = "http://www2.ala.org.au/datasets/footer.html";
    protected static String googleAnalyticsHtmlUrl = "http://www2.ala.org.au/datasets/googleAnayltics.html";
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
    protected static String googleAnalyticsKey = "UA-4355440-1";
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
            if (prop.getProperty("include.bannerUrl") != null) {
                bannerHtmlUrl = prop.getProperty("include.bannerUrl");
            }
            if (prop.getProperty("include.menuUrl") != null) {
                menuHtmlUrl = prop.getProperty("include.menuUrl");
            }
            if (prop.getProperty("include.footerUrl") != null) {
                footerHtmlUrl = prop.getProperty("include.footerUrl");
            }
            if (prop.getProperty("include.googleAnalytics") != null) {
                googleAnalyticsHtmlUrl = prop.getProperty("include.googleAnalytics");
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
        String banner = null;
        String menu = null;
        
        String loginLogoutListItem;
        if (loggedIn) {
            loginLogoutListItem = "<a href='" + casServer + "/cas/logout?url=" + returnUrlPath + "'>Log out</a>";
        } else {
            loginLogoutListItem = "<a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a>";
        }
        
        // load the top banner
        banner = GetWebContent.getInstance().getContent(bannerHtmlUrl);
        
        banner = banner.replaceAll(centralServerTag, centralServer);
        banner = banner.replaceAll(casServerTag, casServer);
        banner = banner.replaceAll(loginLogoutListItemTag, loginLogoutListItem);
        banner = banner.replaceAll(searchServerTag, searchServer);
        banner = banner.replaceAll(searchPathTag, searchPath);
        banner = banner.replaceAll(queryTag, query);

        // load the menu
        menu = GetWebContent.getInstance().getContent(menuHtmlUrl);
        menu = menu.replaceAll(centralServerTag, centralServer);
        
        return banner + menu;
    }
    
    public static String getFooter() throws Exception {
        String output = null;
        
        output = getFooter(defaultCentralServer);
        
        return output;
    }
    
    public static String getFooter(String centralServer) throws Exception {
        String output = GetWebContent.getInstance().getContent(footerHtmlUrl);
        String analytics = GetWebContent.getInstance().getContent(googleAnalyticsHtmlUrl);
        
        output = output.replaceAll(centralServerTag, centralServer);
        analytics = analytics.replaceAll(googleAnalyticsKeyTag, googleAnalyticsKey);

        return output + analytics;
    }
}
