/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package org.ala.dao;

import javax.inject.Inject;

import org.ala.model.Ranking;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Simple ranking DAO implementation
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("rankingDao")
public class RankingDaoImpl implements RankingDao {

	private final static Logger logger = Logger.getLogger(RankingDaoImpl.class);
	@Inject
	protected StoreHelper storeHelper;
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	/**
	 * @see org.ala.dao.RankingDao#rankImageForTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, boolean)
	 */
	@Override
	public boolean rankImageForTaxon(
			String userIP,
			String userId,
			String taxonGuid, 
			String scientificName, 
			String imageUri,
			Integer imageInfoSourceId, 
			boolean positive) throws Exception {
		
		Ranking r = new Ranking();
		r.setUri(imageUri);
		r.setUserId(userId);
		r.setUserIP(userIP);
		r.setPositive(positive);
		//store the ranking event
		logger.debug("Storing the rank event...");
		storeHelper.put("rk", "rk", "image", ""+System.currentTimeMillis(), taxonGuid, r);
		logger.debug("Updating the images...");
		taxonConceptDao.setRankingOnImage(taxonGuid, imageUri, positive);
		logger.debug("Finished updating ranking");
		return true;
	}

	/**
	 * @param storeHelper the storeHelper to set
	 */
	public void setStoreHelper(StoreHelper storeHelper) {
		this.storeHelper = storeHelper;
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
