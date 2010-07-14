package org.ala.dao;

import junit.framework.TestCase;

import org.ala.dto.ExtendedGeoRegionDTO;
import org.ala.util.SpringUtils;

public class GeoRegionDaoTest extends TestCase {
	
	public void testGeoRegionTest() throws Exception {
		
		GeoRegionDao grDao = SpringUtils.getContext().getBean(GeoRegionDao.class);
		
		ExtendedGeoRegionDTO grDTO = grDao.getExtendedGeoRegionByGuid("aus_states/Victoria");
		
		System.out.println(grDTO.getGeoRegion().getName());
		System.out.println(grDTO.getBirdEmblem().getNameString());
		System.out.println(grDTO.getAnimalEmblem().getNameString());
		System.out.println(grDTO.getPlantEmblem().getNameString());
	}
}
