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

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ala.model.Ranking;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.ala.util.ColumnType;
import org.ala.util.RankingType;
import org.ala.util.ReadOnlyLock;
import org.ala.util.SpringUtils;
import org.ala.model.BaseRanking;
import org.ala.dto.FacetResultDTO;
import org.ala.dto.FieldResultDTO;
import org.gbif.file.CSVReader;

import au.org.ala.data.model.LinnaeanRankClassification;

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
		if(!ReadOnlyLock.getInstance().isReadOnly()){			
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
		return false;
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
		if(!ReadOnlyLock.getInstance().isReadOnly()){
			RankingType rankingType = RankingType.getRankingTypeByTcColumnType(columnType);		
			
			String key = "" + System.currentTimeMillis();
			//save rk table	
			storeHelper.put(rankingType.getColumnFamily(), rankingType.getColumnFamily(), rankingType.getSuperColumnName(), key, guid, baseRanking);
	/*
			// save tc table
			taxonConceptDao.setRanking(guid, rankingType.getColumnType(), baseRanking);
			// update 'rk' solr index
			addRankingIndex(rankingType, baseRanking, key, guid);
	*/
			// update tc & rk solr index in separate thread
			Thread thread1 = new Thread(new RankingUpdateSolrThread(guid, rankingType, baseRanking, key)); 
			thread1.start();
			return true;
		}	
		return false;
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
        solrServer.commit(true, true);        
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
			if(args.length ==1){
		        try{
		            if(args[0].equals("-caab"))
		            	rankingDao.loadCAAB();
		        }
		        catch(Exception e){
		            e.printStackTrace();
		        }
		        
		        try{
		            if(args[0].equals("-reload"))
		            	rankingDao.reloadAllRanks();
		        }
		        catch(Exception e){
		            e.printStackTrace();
		        }
		        
		        try{
		            if(args[0].equals("-optimise"))
		            	rankingDao.optimiseIndex();
		        }
		        catch(Exception e){
		            e.printStackTrace();
		        }
	        }
		    else{
		    	System.out.println("Please provide args: -reload or -caab");
		    }

/*	
//below code for debug only .......

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
*/				
		} 
		catch (Exception e) {
			e.printStackTrace();
		}	
		System.exit(0);		
	}	
	
	/*
	 *  inner runnable class for solr index update thread 
	 */
	class RankingUpdateSolrThread implements Runnable {
		private String guid = null;
		private RankingType rankingType = null;
		private BaseRanking baseRanking = null;
		private String key = null;
		
		public RankingUpdateSolrThread(String guid, RankingType rankingType, BaseRanking baseRanking, String key) throws Exception {
			this.guid = guid;
			this.rankingType = rankingType;
			this.baseRanking = baseRanking;
			this.key = key;
		}

		@Override
		public void run() {
		    try {
		    	if(taxonConceptDao != null && guid != null && rankingType != null && baseRanking != null && key != null){
		    		taxonConceptDao.setRanking(guid, rankingType.getColumnType(), baseRanking);
		    		addRankingIndex(rankingType, baseRanking, key, guid);		    		
		    	}
			} catch (Exception e) {
				logger.error(e);
			}     		
		}
	}
	
	private void resetRanks() throws Exception{
		int ctr = 0;
		//reset image ranking to zero
		logger.debug("Reset Image Rank...");
        Scanner scanner;
		scanner = storeHelper.getScanner("bie", RankingType.RK_IMAGE.getColumnFamily(), RankingType.RK_IMAGE.getSuperColumnName());
        byte[] guidAsBytes = null;
        try{
	        while((guidAsBytes = scanner.getNextGuid()) != null){
	            String guid = new String(guidAsBytes);
	            logger.debug("Processing Image ranks for " +guid);
	            taxonConceptDao.resetRanking(guid, ColumnType.IMAGE_COL, 0);
	            ctr++;
	        }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        
        //reset commonName ranking to zero
        logger.debug("Reset common Name Rank...");
        scanner = storeHelper.getScanner("bie", RankingType.RK_COMMON_NAME.getColumnFamily(), RankingType.RK_COMMON_NAME.getSuperColumnName());
        try{
	        while((guidAsBytes = scanner.getNextGuid()) != null){
	            String guid = new String(guidAsBytes);
	            logger.debug("Processing Image ranks for " +guid);
	            taxonConceptDao.resetRanking(guid, ColumnType.VERNACULAR_COL, 0);
	            ctr++;
	        }
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        logger.debug("Reset Ranks finished... records count = " + ctr);
	}
		
	private void reloadRanks() throws Exception {
		SolrServer solrServer = solrUtils.getSolrServer();
		long start = System.currentTimeMillis();
		Map<String, String> compareFieldValue = new HashMap<String, String>();
    	int i = 0;
    	int j = 0;
    	logger.debug("reload Ranks...");
		Scanner scanner = storeHelper.getScanner(RK_COLUMN_FAMILY, RK_COLUMN_FAMILY);		
		byte[] guidAsBytes = null;		
		while ((guidAsBytes = scanner.getNextGuid())!=null) {    		
			String guid = new String(guidAsBytes);
			i++;
			
			if(i%1000==0){
				logger.info("Indexed records: "+i+", current guid: "+guid);
			}
			try{			
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
							if("rk".equalsIgnoreCase(superColumnName)){
								//old record convert to baseRanking
								compareFieldValue.clear();
								compareFieldValue.put("identifier", br.getUri());
								br.setCompareFieldValue(compareFieldValue);	
								rankingType = RankingType.RK_IMAGE;
							}
							// defaultNameValue
							else if(RankingType.RK_NAME_VALUE.getSuperColumnName().equalsIgnoreCase(superColumnName)){
								compareFieldValue.clear();
								compareFieldValue.put("nameString", key);
								compareFieldValue.put("identifier", br.getUri());
								compareFieldValue.put("defaultValue", "100000");
								
								br.setCompareFieldValue(compareFieldValue);							
								rankingType = RankingType.RK_COMMON_NAME;
							}
							//no reindex by each common name						
							taxonConceptDao.setRanking(guid, rankingType.getColumnType(), br, false);
				            
							j++;
							if(j%1000==0){
								logger.info("Indexed records: "+j+", current guid: "+guid);
							}
						}
					}
				}
				try{
					//reindex whole row record
					List<SolrInputDocument> docList = taxonConceptDao.indexTaxonConcept(guid);
					if(solrServer == null){
						solrServer = solrUtils.getSolrServer();
					}
					if(solrServer != null && docList != null && docList.size() > 0){
						solrServer.add(docList);
					}	
				}
				catch(Exception e){
					logger.error("***** add solr record failed. guid: " + guid + " ," + e);
				}
			}
			catch(Exception ex){
				logger.error("***** guid: " + guid + " ," + ex);
			}
    	}
		if(solrServer == null){
			solrServer = solrUtils.getSolrServer();
		}
		if(solrServer != null){
			solrServer.commit();
		}
		
    	long finish = System.currentTimeMillis();
    	logger.info("Index created in: "+((finish-start)/1000)+" seconds with  species: " + i + ", column items: " + j);
    	logger.debug("reload Ranks finished...");
	}
	
	public boolean optimiseIndex() throws Exception{
		Calendar ticket = null;
		boolean completed = false;
		
		try{
			if(!ReadOnlyLock.getInstance().isReadOnly()){
				ticket = Calendar.getInstance(); 			
				if(ReadOnlyLock.getInstance().setLock(ticket)){	
					logger.info("**** setLock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
					if(solrServer == null){
						solrServer = solrUtils.getSolrServer();
					}
					if(solrServer != null){
						solrServer.optimize();
						completed = true;
					}
				}
				else{
					logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
				}
				logger.info("optimiseIndex in (sec): "+((Calendar.getInstance().getTimeInMillis() - ticket.getTimeInMillis())/1000));
			}
			else{
				logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
			}			
		}
		finally{
			if(ReadOnlyLock.getInstance().isReadOnly()){
				ReadOnlyLock.getInstance().setUnlock(ticket);
				logger.info("**** setUnlock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
			}			
		}
		return completed;
	}
	
	public boolean reloadAllRanks() throws Exception{
		Calendar ticket = null;
		boolean completed = false;
		
		try{
			if(!ReadOnlyLock.getInstance().isReadOnly()){ 
				ticket = Calendar.getInstance(); 			
				if(ReadOnlyLock.getInstance().setLock(ticket)){			
					logger.info("**** setLock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
					
					// reset ranking to zero in tc[guid][hasImage] & tc[guid][hasVernacularConcept]
					resetRanks();
					// reapply all ranks based on bie 'rk' table
					reloadRanks();	
					completed = true;
				}
				else{
					logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
				}
				logger.info("reloadAllRanks in (sec): "+((Calendar.getInstance().getTimeInMillis() - ticket.getTimeInMillis())/1000));
			}
			else{
				logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
			}			
		}
		finally{
			if(ReadOnlyLock.getInstance().isReadOnly()){
				ReadOnlyLock.getInstance().setUnlock(ticket);
				logger.info("**** setUnlock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
			}			
		}
		return completed;
	}

	/** copied from StandardNameLoader.java **/
    // apply commonName default value = 100000 into 'rk' table
    public boolean loadCAAB() throws Exception{
    	Calendar ticket = null;
    	boolean completed = false;
    	try{
    		if(!ReadOnlyLock.getInstance().isReadOnly()){
    			ticket = Calendar.getInstance(); 			
				if(ReadOnlyLock.getInstance().setLock(ticket)){	
					logger.info("**** setLock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
					
			    	File file = new File("/data/bie-staging/vernacular/caab/caab-fishes-standard-names-20101209.csv");
			        CSVReader reader = CSVReader.buildReader(file, "UTF-8", ',', '"', 1);
			        while(reader.hasNext()){
			            String[] values = reader.readNext();
			            if(values != null && values.length>=7){
			                //only want to handle the "australian" region for now
			                //Tony Rees comment:
			                /*
			                 FYI the "List Status Code" = "A" for fishes quoted as occurring in
			                 Australian waters (not including sub/antarctic territories e.g. Macquarie I,
			                 heard/MacDonald, and AAT), plus a few "R" which is in the Australian region
			                 of interest adjacent to the EEZ but not actually in it
			                 (maybe a bit north, or Tasman Sea). I have left these in the list
			                 in case they are needed.
			                 */
			                if(values[0].equals("A")){
			                    String identifier = "http://www.marine.csiro.au/caabsearch/caab_search.caab_report?spcode=" + values[1];
			                    LinnaeanRankClassification cl = new LinnaeanRankClassification(null, null, null, null, values[6], null, values[2]);
			                    String name = values[4];
			                    if(StringUtils.isEmpty(name)){
			                        if(!values[5].startsWith("["))
			                            name = values[5];
			                    }
			                    if(!StringUtils.isEmpty(name)){
			                        loadCommonName(cl, name, identifier);
			                    }	
			                }		
			            }
			        }
			        taxonConceptDao.reportStats(System.out, "Final CAAB name stats: ");
			        completed = true;
				}
				else{
					logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
				}
				logger.info("loadCAAB in (sec): "+((Calendar.getInstance().getTimeInMillis() - ticket.getTimeInMillis())/1000));
    		}
			else{
				logger.info("**** isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());
			}
    	}
    	finally{
			if(ReadOnlyLock.getInstance().isReadOnly()){
				ReadOnlyLock.getInstance().setUnlock(ticket);
				logger.info("**** setUnlock-isReadOnly: " + ReadOnlyLock.getInstance().isReadOnly());				
			}    		
    	}        
    	return completed;
    }   
    
    private void loadCommonName(LinnaeanRankClassification cl,String commonName, String identifier) throws Exception{    	    	
    	String guid = taxonConceptDao.findLsidByName(cl.getScientificName(), cl, null);
        if(guid != null && commonName != null && commonName.length() > 0){
        	BaseRanking br = new BaseRanking();
        	br.setUri(identifier);
        	
			//save rk table	
    		storeHelper.put(RankingType.RK_NAME_VALUE.getColumnFamily(), RankingType.RK_NAME_VALUE.getColumnFamily(), RankingType.RK_NAME_VALUE.getSuperColumnName(), commonName.trim(), guid, br);
        }
        else{
            logger.info("Unable to locate " + cl.getScientificName());
        }
    }
		    
}
