package org.ala.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.ala.model.Region;
import org.apache.log4j.Logger;

/**
 * Note - this test assumes an HBase table 'taxonConcepts' is available and that the Red Kangaroo is contained therein.
 * 
 * @author peterflemming
 *
 */
public class HBaseDaoUtilsTest extends TestCase {

	private static final String TEST_TCDAO_GUID = "urn:lsid:afd:taxon:123";;
	private static final String TAXON_ID = "taxonId";
	private static final String REGION_TYPE = "regionType";
	private static final String REGION_NAME = "regionName";
	private static final String REGION_ID = "regionId";
	protected static Logger logger  = Logger.getLogger("HBaseDaoUtilsTest");

	public void testPutComplexObject() throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDaoImpl();
		Region region = new Region(TAXON_ID, REGION_ID, REGION_NAME, REGION_TYPE, 20);
		List<Region> regionsOut = new ArrayList<Region>();
		regionsOut.add(region);
		
		// Put region to test taxon concept
		if (tcDao.addRegions(TEST_TCDAO_GUID, regionsOut)) {
			// Retrieve region list
			List<Region> regionsIn = tcDao.getRegions(TEST_TCDAO_GUID);
			if (!regionsOut.get(0).equals(regionsIn.get(0)))
				fail("Region objects differ");
		} else {
			fail("Error adding region");
		}
	}
}
