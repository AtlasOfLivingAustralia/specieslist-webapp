package org.ala.dao;

public interface RankingDao {

	public boolean rankImageForTaxon(
			String userIp,
			String userId,
			String taxonGuid,
			String scientificName, 
			String imageUri, 
			Integer imageInfoSourceId, 
			boolean positive) throws Exception;
}
