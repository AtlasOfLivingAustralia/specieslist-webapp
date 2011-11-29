/**************************************************************************
 *  Copyright (C) 2011 Atlas of Living Australia
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
package org.ala.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.ala.dao.FulltextSearchDao;
import org.ala.dto.SearchResultsDTO;
import org.ala.dto.SearchTaxonConceptDTO;
import org.ala.util.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * 
 * @author mok011
 * 
 * implemented as singleton object for future use.
 * 
 * ClassificationRank is cache up all 'Kingdom' & 'Phylum' objects.
 * used in SearchResultsDTO getClassificationByLeftNS(int leftNSValue, int rightNSValue) throws Exception function 
 * to increase performance.
 * 
 * @see FulltextSearchDao.SearchResultsDTO getClassificationByLeftNS(int leftNSValue, int rightNSValue) throws Exception;
 */
@Component("classificationRank")
public class ClassificationRank {
	private static ClassificationRank instance = null;
	/** Logger initialisation */
    private final static Logger logger = Logger.getLogger(ClassificationRank.class);    
	private static List<SearchTaxonConceptDTO> kingdomList = new ArrayList<SearchTaxonConceptDTO>();
	private static List<SearchTaxonConceptDTO> pyhlumList = new ArrayList<SearchTaxonConceptDTO>();
	@Inject
	protected  FulltextSearchDao searchDao;
    private ClassificationRank(){
    	init();
    }
 
    public static ClassificationRank getInstance(){
		if(instance == null){
			instance = new ClassificationRank();
		}
		return instance;
	}  
    
    
    private void init() {
    	SearchResultsDTO results;
		try {
			//NC: NEED to use DI to get the searchDao otherwise a complete scan is performed
	    	//FulltextSearchDao searchDao = SpringUtils.getContext().getBean(FulltextSearchDao.class);
			results = searchDao.getAllRankItems("kingdom");
			kingdomList = results.getResults();
			Collections.sort(kingdomList, new SearchTaxonConceptComparator());
	    	results = searchDao.getAllRankItems("phylum"); 
	    	pyhlumList = results.getResults();
	    	Collections.sort(pyhlumList, new SearchTaxonConceptComparator());
		} catch (Exception e) {
			logger.error(e);
		}
    }
    
    public SearchTaxonConceptDTO getPhylum(Integer left, Integer right){
    	SearchTaxonConceptDTO dto = null;
    	SearchTaxonConceptDTO input =  new SearchTaxonConceptDTO();
    	input.setLeft(left);
    	input.setRight(right);
    	if(pyhlumList != null && pyhlumList.size() > 0){
    		int i = Collections.binarySearch(pyhlumList, input, new SearchTaxonConceptComparator());
    		if(i >= 0){
    			dto = pyhlumList.get(i);
    		}
    	}
    	else{
    		instance = null;
    	}
    	return dto;
    }
    
    public SearchTaxonConceptDTO getKingdom(Integer left, Integer right){
    	SearchTaxonConceptDTO dto = null;
    	SearchTaxonConceptDTO input =  new SearchTaxonConceptDTO();
    	input.setLeft(left);
    	input.setRight(right);
    	if(kingdomList != null && kingdomList.size() > 0){
    		int i = Collections.binarySearch(kingdomList, input, new SearchTaxonConceptComparator());
    		if(i >= 0){
    			dto = kingdomList.get(i);
    		}
    	}
    	else{
    		instance = null;
    	}
    	return dto;
    }    
}

//==========
/**
 * comparator for classification tree.
 */
class SearchTaxonConceptComparator implements Comparator<SearchTaxonConceptDTO>{			
	public int compare(SearchTaxonConceptDTO item, SearchTaxonConceptDTO input) {
		if(item.getLeft() != null && item.getRight() != null && input.getLeft() != null && input.getRight() != null){
			if(item.getLeft() < input.getLeft() && item.getRight() > input.getRight()){
				return 0;
			}
			else if(item.getLeft() < input.getLeft()){
				return -1;
			}
			else{
				return 1;
			}
		}
		else{
			if(item.getLeft() == null && item.getRight() == null && input.getLeft() == null && input.getRight() == null){
				return 0;
			}
			else {
				return -1;
			}
		}
	}
}

