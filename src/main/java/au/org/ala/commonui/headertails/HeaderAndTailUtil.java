package au.org.ala.commonui.headertails;

import au.org.ala.cas.util.AuthenticationCookieUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import java.io.InputStream;
import java.security.Principal;
import java.util.Properties;

/**
 * Util class to hold common code for both the banner/header and footer tag classes
 *
 * @author Nick dos Remedios (nick.dosremedios@csiro.au)
 */
public class HeaderAndTailUtil {
    // these fields can be overrided by a properties file (see below)
    protected static String bannerHtmlUrl = "http://www2.ala.org.au/commonui/banner.html";
    protected static String menuHtmlUrl = "http://www2.ala.org.au/commonui/menu.html";
    protected static String footerHtmlUrl = "http://www2.ala.org.au/commonui/footer.html";
    protected static String googleAnalyticsHtmlUrl = "http://www2.ala.org.au/commonui/analytics.html";
    // dynamic fields
    protected PageContext pageContext;
    protected Boolean populateSearchBox = true;
    protected String returnUrlPath = "";
    protected String returnLogoutUrlPath = "";
    protected Boolean loggedIn = false;
    // template-style substitution variables
    protected static String returnPathNullTag = "::returnPathNull::";
    protected static String centralServerTag = "::centralServer::";
    protected static String casServerTag = "::casServerR::";
    protected static String loginLogoutListItemTag = "::loginLogoutListItem::";
    protected static String searchServerTag = "::searchServer::";
    protected static String searchPathTag = "::searchPath::";
    protected static String queryTag = "::query::";
    protected static String googleAnalyticsKeyTag = "::googleAnalyticsKey::";
    protected static String hideSearchFormTag = "id=\"header-search\"";
    // replacement variables
    protected static String googleAnalyticsKey = "UA-4355440-1";
    protected static String defaultCasServer = "https://auth.ala.org.au";
    protected static String defaultCentralServer = "http://www.ala.org.au";
    protected static String defaultSearchServer = "http://bie.ala.org.au";
    protected static String searchPath = "/search";
    protected static String defaultQuery = "Search the Atlas";
    protected static String hideSearchForm = "id=\"header-search\" style=\"display:none;\"";
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

    /**
     * Constructor for login/logout tags
     *
     * @param pageContext
     * @param returnUrlPath
     * @param returnLogoutUrlPath
     */
    public HeaderAndTailUtil(PageContext pageContext, String returnUrlPath, String returnLogoutUrlPath, Boolean populateSearchBox) {
        this.pageContext = pageContext;
        this.returnUrlPath = returnUrlPath;
        this.returnLogoutUrlPath = returnLogoutUrlPath;
        this.populateSearchBox = populateSearchBox;
        this.readPropsFromContext();
    }

    /**
     * Contructor for non login/logout tags
     *
     * @param pageContext
     */
    public HeaderAndTailUtil(PageContext pageContext) {
        this.pageContext = pageContext;
        this.readPropsFromContext();
    }

    /**
     * Init method to ovveride some fields via init params in web.xml
     */
    public void readPropsFromContext() {
        // Read some properties from the web.xml file via servlet context
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        String searchServer = pageContext.getServletContext().getInitParameter("searchServerName");
        if (StringUtils.isNotBlank(searchServer)) {
            defaultSearchServer = searchServer;
        }

        String casServer = pageContext.getServletContext().getInitParameter("casServerName");
        if (StringUtils.isNotBlank(casServer)) {
            defaultCasServer = casServer;
        }

        String centralServer = pageContext.getServletContext().getInitParameter("centralServer");
        if (StringUtils.isNotBlank(centralServer)) {
            defaultCentralServer = centralServer;
        }

        String query = request.getParameter("q");
        String queryAvoid = request.getParameter("xq");
        if (!populateSearchBox || StringUtils.isNotBlank(queryAvoid) || StringUtils.isBlank(query)) {
            defaultQuery = "Search the Atlas";
        }

        // if a return path isn't supplied, construct one from current request
        if (StringUtils.isBlank(returnUrlPath)) {
            StringBuffer requestURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null || "".equals(queryString)) {
                requestURL.append('?');
                requestURL.append(queryString.replaceAll("\\+", "%2B"));
            }
            returnUrlPath = requestURL.toString();
        }

        // if no returnLogoutUrlPath , then use the returnUrlPath
        if (this.returnLogoutUrlPath == null || this.returnLogoutUrlPath.equals("")) {
            this.returnLogoutUrlPath = this.returnUrlPath;
        }

        // Check authentication status
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            loggedIn = true;
        } else {
            loggedIn = AuthenticationCookieUtils.isUserLoggedIn(request);
        }

    }

    /**
     * Get header HTML
     *
     * @deprecated
     * @param loggedIn
     * @param returnUrlPath
     * @return
     * @throws Exception
     */
    public static String getHeader(boolean loggedIn, String returnUrlPath) throws Exception {
        String output = null;
        
        output = getHeader(loggedIn, defaultCentralServer, defaultCasServer, defaultSearchServer, returnUrlPath, defaultQuery);
        
        return output;
    }

    /**
     * Get banner HTML
     *
     * @return
     * @throws Exception
     */
    public String getBanner() throws Exception {
        return getBanner(loggedIn, defaultCentralServer, defaultCasServer, defaultSearchServer, returnUrlPath, returnLogoutUrlPath, defaultQuery, populateSearchBox);
    }

    /**
     * Get header HTML (no longer used directly)
     *
     * @deprecated
     * @param loggedIn
     * @param centralServer
     * @param casServer
     * @param searchServer
     * @param returnUrlPath
     * @param query
     * @return
     * @throws Exception
     */
    public static String getHeader(boolean loggedIn, String centralServer, String casServer, String searchServer, String returnUrlPath, String query) throws Exception {
        return getHeader(loggedIn, centralServer, casServer, searchServer, returnUrlPath, null, query);
    }

    /**
     * Get header HTML
     *
     * @deprecated
     * @param loggedIn
     * @param centralServer
     * @param casServer
     * @param searchServer
     * @param returnUrlPath
     * @param returnLogoutUrlPath
     * @param query
     * @return
     * @throws Exception
     */
    public static String getHeader(boolean loggedIn, String centralServer, String casServer, String searchServer, String returnUrlPath, String returnLogoutUrlPath, String query) throws Exception {
        String banner = getBanner(loggedIn, centralServer, casServer, searchServer, returnUrlPath, returnLogoutUrlPath, query, true);
        String menu = getMenu(centralServer);
        
        return banner + menu;
    }

    /**
     * Get banner HTML
     *
     * @param loggedIn
     * @param centralServer
     * @param casServer
     * @param searchServer
     * @param returnUrlPath
     * @param returnLogoutUrlPath
     * @param query
     * @return
     * @throws Exception
     */
    public static String getBanner(boolean loggedIn, String centralServer, String casServer, String searchServer,
            String returnUrlPath, String returnLogoutUrlPath, String query, Boolean populateSearchBox) throws Exception {
        if (StringUtils.isEmpty(returnLogoutUrlPath)) {
            returnLogoutUrlPath = returnUrlPath;
        }

        String loginLogoutListItem;
        if (loggedIn) {
            loginLogoutListItem = "<a href='" + casServer + "/cas/logout?url=" + returnLogoutUrlPath + "'>Log out</a>";
        } else {
            loginLogoutListItem = "<a href='" + casServer + "/cas/login?service=" + returnUrlPath + "'>Log in</a>";
        }


        // load the top banner
        String banner = GetWebContent.getInstance().getContent(bannerHtmlUrl);

        banner = banner.replaceAll(centralServerTag, centralServer);
        banner = banner.replaceAll(casServerTag, casServer);
        banner = banner.replaceAll(loginLogoutListItemTag, loginLogoutListItem);
        banner = banner.replaceAll(searchServerTag, searchServer);
        banner = banner.replaceAll(searchPathTag, searchPath);
        banner = banner.replaceAll(queryTag, query);

        if (!populateSearchBox) {
            // hide search form
            banner = banner.replaceAll(hideSearchFormTag, hideSearchForm);
        }

        return banner;
    }

    /**
     * Get menu HTML
     *
     * @return
     * @throws Exception
     */
    public String getMenu() throws Exception {
        return getMenu(defaultCentralServer);
    }

    /**
     * Get menu HTML
     *
     * @param centralServer
     * @return
     * @throws Exception
     */
    public static String getMenu(String centralServer) throws Exception {
        // load the menu
        String menu = GetWebContent.getInstance().getContent(menuHtmlUrl);
        menu = menu.replaceAll(centralServerTag, centralServer);

        return menu;
    }

    /**
     * Get footer HTML
     *
     * @return
     * @throws Exception
     */
    public String getFooterHtml() throws Exception {
        return getFooterHtml(defaultCentralServer);
    }

    /**
     * Get footer JS HTML
     *
     * @return
     * @throws Exception
     */
    public String getFooterJs() throws Exception {
        return getFooterJs(defaultCentralServer);
    }

    /**
     * Get footer HTML
     *
     * @param centralServer
     * @return
     * @throws Exception
     */
    public static String getFooterHtml(String centralServer) throws Exception {
        String output = GetWebContent.getInstance().getContent(footerHtmlUrl);
        output = output.replaceAll(centralServerTag, centralServer);
        
        return output;
    }

    /**
     * Get footer JS HTML
     *
     * @param centralServer
     * @return
     * @throws Exception
     */
    public static String getFooterJs(String centralServer) throws Exception {
        String analytics = GetWebContent.getInstance().getContent(googleAnalyticsHtmlUrl);
        analytics = analytics.replaceAll(googleAnalyticsKeyTag, googleAnalyticsKey);

        return analytics;
    }

    /**
     * Get footer HTML (both footer and analytics JS)
     *
     * @deprecated
     * @param centralServer
     * @return
     * @throws Exception
     */
    public static String getFooter(String centralServer) throws Exception {
        return getFooterHtml(centralServer) + getFooterJs(centralServer);
    }
}
