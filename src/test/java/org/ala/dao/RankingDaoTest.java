package org.ala.dao;

import org.ala.util.SpringUtils;

import junit.framework.TestCase;

public class RankingDaoTest extends TestCase {

	public void testRanking() throws Exception {
		
		RankingDao rankingDao = SpringUtils.getContext().getBean(RankingDao.class);
		
		boolean created = rankingDao.rankImageForTaxon(
				"12.12.12.12", 
				"David.Martin@csiro.au", 
				"Dave Martin",
				"urn:lsid:biodiversity.org.au:apni.taxon:296831", 
				"Acacia dealbata", 
				"http://upload.wikimedia.org/wikipedia/commons/d/df/Acacia_dealbata_AF.jpg", 
				1036, 
				true);
		
		created = rankingDao.rankImageForTaxon(
				"12.12.12.12", 
				"David.Martin@csiro.au",
				"Dave Martin", 
				"urn:lsid:biodiversity.org.au:apni.taxon:296831", 
				"Acacia dealbata", 
				"http://upload.wikimedia.org/wikipedia/commons/8/82/Acacia_dealbata-1.jpg",
				1036, 
				false);
		
		System.out.println("Loaded:"+created);
	}
}
