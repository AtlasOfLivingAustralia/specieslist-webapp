package org.ala.lucene;

import org.ala.dao.TaxonConceptDao;

/**
 * Creates a basic lucene index for the taxon concepts.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class CreateTaxonConceptIndex {

	public static void main(String[] args) throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDao();
		tcDao.createIndex();
	}
}
