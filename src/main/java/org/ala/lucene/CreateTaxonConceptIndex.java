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
package org.ala.lucene;


import org.ala.dao.TaxonConceptDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Creates a basic lucene index for the taxon concepts.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class CreateTaxonConceptIndex {

	public static void main(String[] args) throws Exception {
		//TaxonConceptDao tcDao = new TaxonConceptDao();
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring.xml");
        // TODO: fix so that xml file is read from this project and not bie-repository's classpath
        TaxonConceptDao tcDao = (TaxonConceptDao) context.getBean(TaxonConceptDao.class);
		tcDao.createIndex();
	}
}
