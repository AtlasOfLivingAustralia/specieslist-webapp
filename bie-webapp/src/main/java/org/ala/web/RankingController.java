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

import java.security.Principal;
import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ala.dao.RankingDao;
import org.ala.model.BaseRanking;
import org.ala.util.ColumnType;
import org.ala.util.RankingType;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Simple controller used to ranking based on ImageRankController
 *
 * @author mok011
 */
@Controller("RankingController")
public class RankingController {

	private final static Logger logger = Logger.getLogger(SpeciesController.class);
	
	@Inject
	RankingDao rankingDao;

	/**
	 * @param rankingDao the rankingDao to set
	 */
	public void setRankingDao(RankingDao rankingDao) {
		this.rankingDao = rankingDao;
	}	
	
	/**
	 * Store an ranking for an image.
	 * 
	 * @param guid
	 * @param name
	 * @param uri
	 * @param infosourceId
	 * @param positive
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = {"rankTaxonImage*","/rankTaxonImageWithUser*"}, method = RequestMethod.GET)
	public void rankTaxonImageByUser(
			@RequestParam(value="guid", required=true) String guid,
			@RequestParam(value="name", required=true) String name,
			@RequestParam(value="uri", required=true) String uri,
			@RequestParam(value="infosourceId", required=true) Integer infosourceId,
			@RequestParam(value="blackList", required=true) boolean blackList,
			@RequestParam(value="positive", required=true) boolean positive,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		BaseRanking baseRanking = populateUserInfo(request);
		baseRanking.setBlackListed(blackList);
		baseRanking.setPositive(positive);
//		baseRanking.setUri(uri);
		
		//extra value checking
		Map<String, String> map = new Hashtable<String, String> ();
		// CommonName object field name
		map.put(RankingType.RK_IMAGE.getCompareFieldName()[0], uri);
		baseRanking.setCompareFieldValue(map);		
		rankingDao.rankingForTaxon(guid, ColumnType.IMAGE_COL, baseRanking);
		
		String cookieValue = RankingCookieUtils.getCookieValue(guid, uri, positive);
		Cookie cookie = new Cookie(Long.toString(System.currentTimeMillis()), cookieValue);
		cookie.setMaxAge(60*60*24*365);
		response.addCookie(cookie);		
	}

	
	/**
	 * Store an ranking for an commonName.
	 * 
	 * @param guid
	 * @param name
	 * @param uri
	 * @param commonName
	 * @param infosourceId
	 * @param positive
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = {"/rankTaxonCommonName*","/rankTaxonCommonNameWithUser*"}, method = RequestMethod.GET)
	public void rankTaxonCommonNameByUser(
			@RequestParam(value="guid", required=true) String guid,
			@RequestParam(value="name", required=true) String name,
			@RequestParam(value="blackList", required=true) boolean blackList,
			@RequestParam(value="positive", required=true) boolean positive,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		BaseRanking baseRanking = populateUserInfo(request);
		baseRanking.setBlackListed(blackList);
		baseRanking.setPositive(positive);
//		baseRanking.setUri(uri);
		
		//extra value checking
		Map<String, String> map = new Hashtable<String, String> ();
		// CommonName object field name
		map.put(RankingType.RK_COMMON_NAME.getCompareFieldName()[0], name);
		baseRanking.setCompareFieldValue(map);
		
		rankingDao.rankingForTaxon(guid, ColumnType.VERNACULAR_COL, baseRanking);
		
		//cookie can't handle multiple words. special treatment required
		String str = name.replace(" ", "");
		str = str.replace(",", "");
		String cookieValue = RankingCookieUtils.getCookieValue(guid, str, positive);
		Cookie cookie = new Cookie(Long.toString(System.currentTimeMillis()), cookieValue);
		cookie.setMaxAge(60*60*24*365);
		response.addCookie(cookie);		
	}	
	
	private BaseRanking populateUserInfo(HttpServletRequest request){
		BaseRanking baseRanking = new BaseRanking();
		
		Principal p = request.getUserPrincipal();
		String remoteUser = request.getRemoteUser();
		String fullName = null;
		if(p!=null){
			remoteUser = p.getName();
			if(p instanceof AttributePrincipal){
				AttributePrincipal ap = (AttributePrincipal) p;
				if(ap.getAttributes().get("firstname")!=null && ap.getAttributes().get("lastname")!=null){
					fullName = ap.getAttributes().get("firstname").toString() + " " + ap.getAttributes().get("lastname").toString();
				}
			}
		}
		baseRanking.setUserId(remoteUser);
		baseRanking.setUserIP(request.getRemoteHost());
		baseRanking.setFullName(fullName);	
		
		return baseRanking;
	}	
}

