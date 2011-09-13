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

import org.ala.dto.SearchResultsDTO;
import org.ala.model.BaseRanking;
import org.ala.util.ColumnType;
import org.ala.util.RankingType;

/**
 * A simple ranking Dao for ranking artefacts within the BIE.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public interface RankingDao {

	/**
	 * Rank this image for this taxon.
	 * 
	 * @param userIp
	 * @param userId
	 * @param taxonGuid
	 * @param scientificName
	 * @param imageUri
	 * @param imageInfoSourceId
	 * @param positive
	 * @return
	 * @throws Exception
	 */
	public boolean rankImageForTaxon(
			String userIp,
			String userId,
			String fullName,
			String taxonGuid,
			String scientificName, 
			String imageUri, 
			Integer imageInfoSourceId,
			boolean positive) throws Exception;
	
	public boolean rankImageForTaxon(
			String userIp,
			String userId,
			String fullName,
			String taxonGuid,
			String scientificName, 
			String imageUri, 
			Integer imageInfoSourceId,
			boolean blackList,
			boolean positive) throws Exception;
	
        /**
         * Reloads the image ranks from the rk columnFamily to the tc columnFamily
         */
        public void reloadImageRanks();
        
    	/** ======================================
    	 * Ranking functions
    	 * 
    	 * ColumnFamily = 'rk'
    	 =========================================*/
        public void createIndex() throws Exception;
        public boolean rankingForTaxon(String guid, ColumnType columnType, BaseRanking baseRanking) throws Exception;
        public boolean loadCAAB() throws Exception;
        public boolean reloadAllRanks() throws Exception;
        public boolean optimiseIndex() throws Exception;
}
