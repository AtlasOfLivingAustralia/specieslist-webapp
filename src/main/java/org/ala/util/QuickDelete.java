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
package org.ala.util;

import javax.inject.Inject;

import org.ala.dao.TaxonConceptDao;
import org.springframework.stereotype.Component;

/**
 * Utility for deleting content from the BIE
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("quickDelete")
public class QuickDelete {

	@Inject
	protected TaxonConceptDao taxonConceptDao;

	/**
	 * Takes a list of infosource ids.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		QuickDelete qd = SpringUtils.getContext().getBean(QuickDelete.class);
		if(args.length>0){
			System.out.println("Starting delete for infosources: "+args);
			qd.deleteForInfoSources(args);
		} else {
			System.out.println("Please run with infosource id arguments");
		}
	}
	
	private void deleteForInfoSources(String[] infoSourceIds) throws Exception {
		taxonConceptDao.deleteForInfosources(infoSourceIds);
	}

	/**
	 * @param taxonConceptDao the taxonConceptDao to set
	 */
	public void setTaxonConceptDao(TaxonConceptDao taxonConceptDao) {
		this.taxonConceptDao = taxonConceptDao;
	}
}
