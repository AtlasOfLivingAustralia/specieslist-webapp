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

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.RankingDao;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Simple controller used to rank an image
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Controller("imageRankController")
public class ImageRankController {

	private final static Logger logger = Logger.getLogger(SpeciesController.class);
	
	@Inject
	RankingDao rankingDao;
//	
//	@RequestMapping(value = "/isRanked*", method = RequestMethod.GET)
//	public String isRankedByUser(
//			@RequestParam(value="guid", required=true) String guid,
//			@RequestParam(value="uri", required=true) String uri,
//			HttpServletRequest request,
//			HttpServletResponse response,
//			Model model) throws Exception {
//
//		Cookie[] cookies = request.getCookies();
//		
//		String cookieValue = getCookieValue(guid, uri);
//		
//		for(Cookie cookie: cookies){
//			if(cookie.getValue()!=null && cookie.getValue().startsWith(cookieValue)){
//				//return true
//				model.addAttribute("isRanked", "true");
//				return "ranked";
//			}
//		}
//		model.addAttribute("isRanked", "false");
//		
//		return "ranked";
//	}

	/*
	public boolean rankImageForTaxon(
			String userIp,
			String userId,
			String taxonGuid,
			String scientificName, 
			String imageUri, 
			Integer imageInfoSourceId, 
			boolean positive) throws Exception;
	 */
	
	
	@RequestMapping(value = "/rankTaxonImage*", method = RequestMethod.GET)
	public void rankTaxonImageByUser(
			@RequestParam(value="guid", required=true) String guid,
			@RequestParam(value="name", required=true) String name,
			@RequestParam(value="uri", required=true) String uri,
			@RequestParam(value="infosourceId", required=true) Integer infosourceId,
			@RequestParam(value="positive", required=true) boolean positive,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.info("Rank image for : "+guid+", URI: "+uri+", value: "+positive);
		rankingDao.rankImageForTaxon(request.getRemoteHost(), request.getRemoteUser(), guid, name, uri, infosourceId, positive);
		//create a cookie value
		String cookieValue = RankingCookieUtils.getCookieValue(guid, uri, positive);
		Cookie cookie = new Cookie(Long.toString(System.currentTimeMillis()), cookieValue);
		cookie.setMaxAge(60*60*24*365);
		response.addCookie(cookie);
	}

	/**
	 * @param rankingDao the rankingDao to set
	 */
	public void setRankingDao(RankingDao rankingDao) {
		this.rankingDao = rankingDao;
	}	
}
