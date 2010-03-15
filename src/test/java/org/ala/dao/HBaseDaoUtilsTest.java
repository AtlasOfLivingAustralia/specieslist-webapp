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

	private static final String MACROPUS_RUFUS = "urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537";
	private static final String REGION_TYPE = "regionType";
	private static final String REGION_NAME = "regionName";
	private static final String REGION_ID = "regionId";
	protected static Logger logger  = Logger.getLogger("HBaseDaoUtilsTest");

	public void testPutComplexObject() throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDaoImpl();
		Region region = new Region(REGION_ID, REGION_NAME, REGION_TYPE, 20);
		List<Region> regionsOut = new ArrayList<Region>();
		regionsOut.add(region);
		
		// Put region to Macropus Rufus
		tcDao.addRegions(MACROPUS_RUFUS, regionsOut);
		
		// Retrieve region list
		List<Region> regionsIn = tcDao.getRegions(MACROPUS_RUFUS);
		if (!regionsOut.get(0).equals(regionsIn.get(0)))
			fail("Region objects differ");
	}
}
