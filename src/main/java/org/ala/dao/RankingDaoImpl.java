package org.ala.dao;

import javax.inject.Inject;

import org.ala.model.Ranking;
import org.springframework.stereotype.Component;

@Component("rankingDao")
public class RankingDaoImpl implements RankingDao {

	@Inject
	protected StoreHelper storeHelper;
	
	@Inject
	protected TaxonConceptDao taxonConceptDao;
	
	@Override
	public boolean rankImageForTaxon(
			String userIP,
			String userId,
			String taxonGuid, 
			String scientificName, 
			String imageUri,
			Integer imageInfoSourceId, 
			boolean positive) throws Exception {
		
		/*
get <ksp>.<cf>['<key>']['<super>']['<col>'] 

bie.annotation.species-guid.imageAnnotation.url
	- value = { rating:positive, user:anonymous }
	- value = { rating:negative, user:davejmartin }
	
set bie.rk['urn:lsid:catalogueoflife.org:taxon:da23ee16-29c1-102b-9a4a-00304854f820:ac2010'].['image'].['http://myimage.com/123'] = '0'	
*/
		
		Ranking r = new Ranking();
		r.setUri(imageUri);
		r.setUserId(userId);
		r.setUserIP(userIP);
		r.setPositive(positive);
		
		storeHelper.put("rk", "rk", "image", ""+System.currentTimeMillis(), taxonGuid, r);

		//does the ranking exist?
		
		
		taxonConceptDao.setRankingOnImage(taxonGuid, imageUri, positive);
		
		
		// add a ranking
		return true;
		
		// add the ranking to the taxon concept row		
		
		
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
