package org.ala.model;

import java.util.List;

import junit.framework.TestCase;

import org.ala.dao.TaxonConceptDao;
import org.ala.dao.TaxonConceptDaoImpl;
import org.ala.dto.RegionTypeDTO;
import org.apache.log4j.Logger;

/**
 * Note - this test assumes an HBase table 'taxonConcepts' is available and that the Red Kangaroo is contained therein.
 * 
 * @author peterflemming
 *
 */
public class RegionTest extends TestCase {

	private static final String MACROPUS_RUFUS = "urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537";
	protected static Logger logger  = Logger.getLogger("HBaseDaoUtilsTest");

	public void testGetRegionsByType() throws Exception {
		TaxonConceptDao tcDao = new TaxonConceptDaoImpl();
		List<Region> regions = tcDao.getRegions(MACROPUS_RUFUS);
		
		List<RegionTypeDTO> regionTypes = Region.getRegionsByType(regions);
		System.out.println("No of RegionTypes " + regionTypes.size());
	}
}
