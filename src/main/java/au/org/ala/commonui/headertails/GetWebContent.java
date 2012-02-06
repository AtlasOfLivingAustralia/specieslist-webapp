/**************************************************************************
 *  Copyright (C) 2012 Atlas of Living Australia
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

import au.org.ala.util.WebUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of GetWebContent (as singleton - can't wire up via DI/Spring due to Taglib)
 *
 * User: dos009
 * Date: 2/02/12
 * Time: 5:08 PM
 */
public class GetWebContent {
    protected static Logger logger = Logger.getLogger(GetWebContent.class);
    protected Map<String, String> contentMap = new HashMap<String, String>();
    protected Long cacheTimeoutInMilliseconds = 4 * 60 * 60 * 1000L; //  4 hours
    protected Map<String, Long> lastChecked = new HashMap<String, Long>();

    /**
     * Private constructor
     */
    private GetWebContent() {
        if (InnerLoader.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    /**
     * Is the cache still current?
     *
     * @return boolean
     */
    private Boolean isCacheCurrent(String url) {
        Boolean isCurrent = false;
        Long now = System.currentTimeMillis();
        
        if (lastChecked.containsKey(url) && (now - lastChecked.get(url)) < cacheTimeoutInMilliseconds) {
            isCurrent = true;
        }
        logger.debug("isCacheCurrent = " + isCurrent + " - cache key is " + lastChecked.containsKey(url));

        return isCurrent;
    }

    /**
     * Get the string content for the requested URL
     *
     * @param url
     * @return
     * @throws Exception
     */
    protected String getContent(String url) throws Exception {

        if (url == null) {
            return null; // otherwise will cause NPE in map lookup
        }

        String content = contentMap.get(url);  // null if key not set

        if (StringUtils.isBlank(content) || !isCacheCurrent(url)) {
            logger.debug("Updating cache for " + url + " at: " + System.currentTimeMillis());
            content = WebUtils.getUrlContentAsString(url); // grab new copy of content
            contentMap.put(url, content); // update cache
            lastChecked.put(url,System.currentTimeMillis()); // update timestamp
        }

        return content;
    }

    /**
     * Return the (single) instance of this class
     *
     * @return
     */
    public static GetWebContent getInstance() {
        return InnerLoader.INSTANCE;
    }

    /**
     * Inner class to (lazy) load singleton
     */
    private static class InnerLoader {
        private static final GetWebContent INSTANCE = new GetWebContent();
    }
}
