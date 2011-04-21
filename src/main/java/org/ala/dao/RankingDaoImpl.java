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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ala.model.Ranking;
import org.apache.log4j.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.ala.util.ColumnType;
import org.ala.util.RankingType;
import org.ala.util.SpringUtils;
import org.ala.model.BaseRanking;
import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.ala.dto.SearchResultsDTO;

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
	
	public boolean rankImageForTaxon(
			String userIP,
			String userId,
			String fullName,
			String taxonGuid, 
			String scientificName, 
			String imageUri,
			Integer imageInfoSourceId,
			boolean positive) throws Exception {
		return this.rankImageForTaxon(userIP, userId, fullName, taxonGuid, scientificName, imageUri, imageInfoSourceId, false, positive);
	}
	
	/**
	 * @see org.ala.dao.RankingDao#rankImageForTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, boolean)
	 */
	@Override
	public boolean rankImageForTaxon(
			String userIP,
			String userId,
			String fullName,
			String taxonGuid, 
			String scientificName, 
			String imageUri,
			Integer imageInfoSourceId,
			boolean blackList,
			boolean positive) throws Exception {
			
		Ranking r = new Ranking();
		if(blackList){
			r.setBlackListed(blackList);
		}
		else{
			r.setPositive(positive);
		}
		
		r.setUri(imageUri);
		r.setUserId(userId);
		r.setFullName(fullName);
		r.setUserIP(userIP);
		
		//store the ranking event
		logger.debug("Storing the rank event...");
		storeHelper.put("rk", "rk", "image", ""+System.currentTimeMillis(), taxonGuid, r);
		logger.debug("Updating the images ranking...");
		taxonConceptDao.setRankingOnImage(taxonGuid, imageUri, positive, blackList);
		logger.debug("Finished updating ranking");
		
		return true;
	}
        /**
         * @see org.ala.dao.RankingDao#reloadImageRanks() 
         */
        public void reloadImageRanks(){
            try{
                System.out.println("Initialising the scanner...");
                Scanner scanner = storeHelper.getScanner("bie", "rk", "image");
                byte[] guidAsBytes = null;
                while((guidAsBytes = scanner.getNextGuid()) != null){
                    String guid = new String(guidAsBytes);
                    //TODO in the future when LSIDs have potentially changed we will need to look up in an index to find the LSID that is being used
                    logger.debug("Processing Image ranks for " +guid);
                    //get the rankings for the current guid
                    Map<String, Object> subcolumns =  storeHelper.getSubColumnsByGuid("rk", "image",guid);
                    Map<String, Integer[]> counts = new java.util.HashMap<String, Integer[]>();
                    
                    for(String key : subcolumns.keySet()){
                        //logger.debug(guid + " :  key : "+ key);
                        List<Ranking> lr = (List<Ranking>) subcolumns.get(key);
                        //sort the rankings based on the URI for the image
                        Collections.sort(lr, new Comparator<Ranking>() {

                            @Override
                            public int compare(Ranking o1, Ranking o2) {
                                return o1.getUri().compareTo(o2.getUri());
                            }
                        });
                        
                        for(Ranking r : lr){
                            Integer[] c = counts.get(r.getUri());
                            if(c == null){
                                c = new Integer[]{0,0};
                                counts.put(r.getUri(), c);
                            }
                            c[0] = c[0]+1;
                            if(r.isPositive())
                                c[1] = c[1] +1;
                            else
                                c[1] = c[1] -1;
                
                            
                        }
                    }
                    for(String uri: counts.keySet()){
                        Integer[] c = counts.get(uri);
                        logger.debug("Updating guid: "+ guid + " for image: " + uri + " with count " + c[0] + " overall rank " + c[1]);
                        taxonConceptDao.setRankingOnImages(guid, counts);
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
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
	
	/** ======================================
	 * Ranking functions
	 * 
	 * ColumnFamily = 'rk'
	 =========================================*/
	@Inject
	private FulltextSearchDao searchDao;
	
	@Inject
	protected SolrUtils solrUtils;

	public static String RK_COLUMN_FAMILY = "rk";	
	protected SolrServer solrServer = null;

	/**
	 * @see org.ala.dao.RankingDao#rankImageForTaxon(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, boolean)
	 */
	public boolean rankingForTaxon(String guid, ColumnType columnType, BaseRanking baseRanking) throws Exception {
		RankingType rankingType = RankingType.getRankingTypeByTcColumnType(columnType);		
		
		String key = "" + System.currentTimeMillis();
		//save rk table	
		storeHelper.put(rankingType.getColumnFamily(), rankingType.getColumnFamily(), rankingType.getSuperColumnName(), key, guid, baseRanking);
		// save tc table
		taxonConceptDao.setRanking(guid, rankingType.getColumnType(), baseRanking);
		// update 'rk' solr index
		addRankingIndex(rankingType, baseRanking, key, guid);

		return true;
	}	
		
	/**
	 * @see org.ala.dao.TaxonConceptDao#createIndex()
	 */
	public void createIndex() throws Exception {
		long start = System.currentTimeMillis();

		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}		
        solrServer.deleteByQuery("idxtype:"+IndexedTypes.RANKING); // delete everything!
    	
    	int i = 0;
    	int j = 0;
    	
		Scanner scanner = storeHelper.getScanner(RK_COLUMN_FAMILY, RK_COLUMN_FAMILY);		
		byte[] guidAsBytes = null;		
		while ((guidAsBytes = scanner.getNextGuid())!=null) {    		
			String guid = new String(guidAsBytes);
			i++;
			
			if(i%1000==0){
				logger.info("Indexed records: "+i+", current guid: "+guid);
			}
    		
    		//get taxon concept details
			List<String> list = storeHelper.getSuperColumnsByGuid(guid, RK_COLUMN_FAMILY);
			for(String superColumnName : list){
				RankingType rankingType = RankingType.getRankingTypeByColumnName(superColumnName);
//				Map<String, List<Comparable>> columnList = storeHelper.getColumnList(RK_COLUMN_FAMILY, superColumnName, guid, rankingType.getClazz());
				Map<String, List<Comparable>> columnList = storeHelper.getColumnList(RK_COLUMN_FAMILY, superColumnName, guid, BaseRanking.class);
				Set<String> keys = columnList.keySet();
				Iterator<String> itr = keys.iterator();
				while(itr.hasNext()){
					String key = itr.next();
					List<Comparable> rankingList = columnList.get(key);
					for(Comparable c : rankingList){
						BaseRanking br = (BaseRanking)c;
			    		SolrInputDocument doc = new SolrInputDocument();
			    		doc.addField("idxtype", IndexedTypes.RANKING);
			    		doc.addField("userId", br.getUserId());
			    		doc.addField("userIP", br.getUserIP());
			    		doc.addField("id", key);
			    		doc.addField("guid", guid);
			    		doc.addField("superColumnName", superColumnName);
			            solrServer.add(doc);
			            solrServer.commit();
			            j++;
						if(j%1000==0){
							logger.info("Indexed records: "+j+", current guid: "+guid);
						}
					}
				}
			}
    	}
    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds with  species: " + i + ", column items: " + j);
	}
	
	private void addRankingIndex(RankingType rankingType, BaseRanking baseRanking, String columnName, String guid) throws Exception{
		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}
		
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("idxtype", IndexedTypes.RANKING);
		doc.addField("userId", baseRanking.getUserId());
		doc.addField("userIP", baseRanking.getUserIP());
		doc.addField("id", columnName); //timestamp
		doc.addField("guid", guid);
		doc.addField("superColumnName", rankingType.getSuperColumnName());
        solrServer.add(doc);
        solrServer.commit();		
	}
	
	private static void printFacetResult(Collection collection){
		Iterator itr = collection.iterator();
		while(itr.hasNext()){
			FacetResultDTO dto = (FacetResultDTO)itr.next();
			System.out.println("****** fieldName: " + dto.getFieldName());
			List<FieldResultDTO> l = dto.getFieldResult();
			for(FieldResultDTO fieldResultDTO : l){
				System.out.println("****** fieldValue: " + fieldResultDTO.getLabel());
				System.out.println("****** fieldCount: " + fieldResultDTO.getCount() + "\n");
			}
		}
	}
			
	public static void main(String[] args){
		try {
			ApplicationContext context = SpringUtils.getContext();
			RankingDao rankingDao = context.getBean(RankingDaoImpl.class);
			FulltextSearchDao searchDao = context.getBean(FulltextSearchDaoImplSolr.class);
			
			//create ranking solr index of 'rk' columnFamily
//			rankingDao.createIndex();
			
			String guid = "urn:lsid:biodiversity.org.au:afd.taxon:f6e0d6c9-80fe-4355-bbe9-d94b426ab52e";
			String userId = "waiman.mok@csiro.au";
			
			BaseRanking baseRanking = new BaseRanking();
			baseRanking.setUserId(userId);
			baseRanking.setUserIP("127.0.0.1");
			baseRanking.setFullName("hello");
			baseRanking.setBlackListed(false);
			baseRanking.setPositive(true);
			
			Map<String, String> map = new Hashtable<String, String> ();
			
			//ranking common name....
//			baseRanking.setUri("http://www.environment.gov.au/biodiversity/abrs/online-resources/fauna/afd/taxa/47d3bff0-5df7-41d9-b682-913428aed5f0");			
			map.put(RankingType.RK_COMMON_NAME.getCompareFieldName()[0], "Fine-spotted Porcupine-fish");
			baseRanking.setCompareFieldValue(map);
//			boolean b = rankingDao.rankingForTaxon(guid, ColumnType.VERNACULAR_COL, baseRanking); 
			
			//ranking image....
//			baseRanking.setUri("http://upload.wikimedia.org/wikipedia/commons/d/de/Ameisenigel_Unterseite-drawing.jpg");
			map.clear();
			map.put(RankingType.RK_IMAGE.getCompareFieldName()[0], "http://upload.wikimedia.org/wikipedia/commons/d/de/Ameisenigel_Unterseite-drawing.jpg");
			baseRanking.setCompareFieldValue(null);
//			b = rankingDao.rankingForTaxon(guid, ColumnType.IMAGE_COL, baseRanking);
			
			//search ranking info....
			Collection result = searchDao.getRankingFacetByUserIdAndGuid(userId, null);
			printFacetResult(result);
			System.out.println("\n===========================\n");
			result = searchDao.getRankingFacetByUserIdAndGuid(userId, guid);
			printFacetResult(result);
			System.out.println("\n===========================\n");
			result = searchDao.getUserIdFacetByGuid(guid);
			printFacetResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}				
}
