/**************************************************************************
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
package org.ala.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

/**
 * Utilities for reading/handling cookies.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class RankingCookieUtils {

	private final static Logger logger = Logger.getLogger(RankingCookieUtils.class);
	
	public static final String propertyDelimiter = "@@@@@@@@@@";
	public static final String RANKING_SESSION_COOKIES = "rankingCookies";

	
	/**
	 * Retrieve a cookie value
	 * @param guid
	 * @param uri
	 * @param positive
	 * @return
	 * @throws Exception
	 */
	public static String getCookieValue(String guid, String uri, boolean positive) throws Exception {
		return guid + propertyDelimiter + uri + propertyDelimiter + positive;
	}
	
	/**
	 * Get the ranked images for this taxon guid.
	 * 
	 * @param cookies
	 * @param guid
	 * @return
	 */
	public static List<StoredRanking> getRankedImageUris(Cookie[] cookies, String guid){
        List<StoredRanking> rankedImageUris = new ArrayList<StoredRanking>();
		if(cookies!=null){
	        for(Cookie cookie: cookies){
	        	String value = cookie.getValue();
	        	logger.debug("Retrieved cookie encoded value = "+value);
	        	try {
					String temp = URLDecoder.decode(value, "UTF-8");
					value = temp;
				} catch (UnsupportedEncodingException e) {
					logger.info(e);
				}
	        	logger.debug("Retrieved cookie decoded value = "+value);
	        	String[] parts = value.split(propertyDelimiter);
	        	logger.debug("Retrieved parts = "+parts.length);
	        	if(parts.length==3){
		        	if(guid.equals(parts[0])){
		        		StoredRanking sf = new StoredRanking();
		        		sf.setUri(parts[1]);
		        		sf.setPositive(Boolean.parseBoolean(parts[2]));
		        		rankedImageUris.add(sf);
		        	}
	        	}
	        	logger.debug("Cookie value:"+value);
	        }
		}
    	logger.debug("Number of cookies found :"+rankedImageUris.size());
        return rankedImageUris;
	}
}
