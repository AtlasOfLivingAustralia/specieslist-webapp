package org.ala.hbase;

import org.ala.dao.TaxonConceptDao;

/**
 * Remove the properties associated with the concepts in the system.
 * 
 * @author Dave Martin
 */
public class ClearRawTriples {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDao();
		tcDao.clearRawProperties();
	}
}