/***************************************************************************
 * Copyright (C) 2009 Atlas of Living Australia
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
package org.ala.vocabulary;

import java.util.List;

import javax.inject.Inject;

import org.ala.dao.VocabularyDAO;
import org.ala.model.ConservationStatus;
import org.ala.model.PestStatus;
import org.ala.model.Term;
import org.ala.util.StatusType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * The implementation of the Vocabulary API. Currently making use
 * of the vocabulary DAO which backs on to MySQL database.
 * 
 * @author Tommy Wang tommy.wang@csiro.au
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("vocabulary")
public class VocabularyImpl implements Vocabulary {

	protected static Logger logger = Logger.getLogger(VocabularyImpl.class);
	
	@Inject
	private VocabularyDAO vocabularyDao;

	/**
	 * @see org.ala.vocabulary.Vocabulary#findPreferredTerm(int, java.lang.String, java.lang.String)
	 */
	@Override
	public Term findPreferredTerm(int infosourceId, String predicate,
			String rawValue) {
		
		List<Term> terms = vocabularyDao.getPreferredTermsFor(infosourceId, predicate, rawValue);
		if(terms.size()==1){
			return terms.get(0);
		}
		if(terms.size()>1){
			logger.error("Multiple vocabulary terms matched for infosource ["
					+infosourceId
					+"] with raw value"
					+ rawValue
					+". Please check the Returning null....");
//			throw new Exception();
		}
		return null;
	}	

	/**
	 * @see org.ala.vocabulary.Vocabulary#getConservationStatusFor(int, java.lang.String)
	 */
	@Override
	public ConservationStatus getConservationStatusFor(int infosourceId, String rawValue) {
		
		List<Term> terms = vocabularyDao.getPreferredTermsFor(infosourceId, "hasConservationStatus", rawValue);
		
		if(terms.size()==0){
			return null;
		}
		
		ConservationStatus conservationStatus = new ConservationStatus();
		for(Term term: terms){
			//map the region term if present
			if(term.getPredicate().endsWith("hasConservationStatus")){
				conservationStatus.setStatus(term.getTermString());
			}
			//map the status term if present
			if(term.getPredicate().endsWith("hasRegion")){
				conservationStatus.setRegion(term.getTermString());
			}
		}
		return conservationStatus;
	}

	/**
	 * @see org.ala.vocabulary.Vocabulary#getPestStatusFor(int, java.lang.String)
	 */
	@Override
	public PestStatus getPestStatusFor(int infosourceId, String rawValue) {
		
		List<Term> terms = vocabularyDao.getPreferredTermsFor(infosourceId, "hasPestStatus", rawValue);
		
		if(terms.size()==0){
			return null;
		}
		
		PestStatus pestStatus = new PestStatus();
		for(Term term: terms){
			//map the region term if present
			if(term.getPredicate().endsWith("hasPestStatus")){
				pestStatus.setStatus(term.getTermString());
			}
			//map the status term if present
			if(term.getPredicate().endsWith("hasRegion")){
				pestStatus.setRegion(term.getTermString());
			}
		}
		return pestStatus;
	}

	/**
	 * @see org.ala.vocabulary.Vocabulary#getTermsForStatusType(org.ala.util.StatusType)
	 */
	@Override
	public List<String> getTermsForStatusType(StatusType statusType) {
		return vocabularyDao.getTermsForStatusType(statusType);
	}	
	
	
//	@Override
//	public Term findPreferredTerm(int infosourceId, String predicate, String rawValue) {
//		
//		//String preferredTerm = null;
//		int preferredTermId = 0;
//		String tmpTermStr = new String();
//		Term preferredTermObj = new Term();
//
//		List<Integer> vocabularyIdList = vocabularyDao.getIdsforAll();
//
//		// get preferredTermId
//		for (Integer vocabularyId : vocabularyIdList) {
//			//System.out.println("ID!" + vocabularyId);
//			List<org.ala.model.Vocabulary> vocabularyList = vocabularyDao.getById(vocabularyId.intValue());
//			//System.out.println(vocabulary.getId() + " " + vocabulary.getInfosourceId() + " " + vocabulary.getPredicate());
//
//			//if (vocabularyList != null) {
//			for (org.ala.model.Vocabulary vocabulary : vocabularyList) {
//				if (infosourceId == vocabulary.getInfosourceId() && predicate.equals(vocabulary.getPredicate()) && rawValue.equals(vocabulary.getTermString())) {
//					preferredTermId = vocabulary.getPreferredTermId();
//					//System.out.println(preferredTermId);
//					break;
//				} else if (infosourceId == vocabulary.getInfosourceId() && predicate.equals(vocabulary.getPredicate()) && rawValue.contains(vocabulary.getTermString())) {
//					
//					/* 
//					 * As some of the hasConservationStatus values contains multiple term information in a single string, e.g.
//					 * different AU state can have different conservation status for a certain species for Reptiles Down Under,
//					 * we are temporarily mapping only the term strings contained in the raw values without considering the 
//					 * state information. In addition, currently only the first conservation status string in a raw value string 
//					 * is mapped.
//					 */
//					
//					if (tmpTermStr.contains(vocabulary.getTermString())) {
//						continue;
//					} else if (vocabulary.getTermString().contains(tmpTermStr)) {
//						preferredTermId = vocabulary.getPreferredTermId();
//						tmpTermStr = vocabulary.getTermString();
//					}
//					//System.out.println(preferredTermId);
//					
//				} 
//			}
//
//			//}
//		}
//		
//		// get the actual preferred term 
//		for (Integer vocabularyId : vocabularyIdList) {
//			//System.out.println("ID!" + vocabularyId);
//			List<org.ala.model.Vocabulary> vocabularyList = vocabularyDao.getById(vocabularyId.intValue());
//		
//			for (org.ala.model.Vocabulary vocabulary : vocabularyList) {
//				if (preferredTermId == vocabulary.getTermId()) {
//					preferredTermObj.setPredicate(vocabulary.getPredicate());
//					preferredTermObj.setTermString(vocabulary.getTermString());
//					preferredTermObj.setVocabularyId(vocabulary.getId());
//					
//				}
//			}
//
//		}
//
//		return preferredTermObj;
//	}
}
