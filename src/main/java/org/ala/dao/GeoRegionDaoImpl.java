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
package org.ala.dao;

import java.util.List;

import org.ala.model.GeoRegion;
import org.springframework.stereotype.Component;

/**
 * An implementation of the GeoRegionDao interface.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("geoRegionDao")
public class GeoRegionDaoImpl implements GeoRegionDao {

	protected static final String GR_TABLE = "geoRegion";
	protected static final String GR_COL_FAMILY = "gr";
	
	private static final String GEOREGION_COL = "geoRegion";
	
	protected StoreHelper storeHelper;
	
	/**
	 * @see org.ala.dao.GeoRegionDao#create(org.ala.model.GeoRegion)
	 */
	@Override
	public boolean create(GeoRegion geoRegion) throws Exception {
		if(geoRegion==null){
			throw new IllegalArgumentException("Supplied GeoRegion was null.");
		}
		if(geoRegion.getGuid()==null){
			throw new IllegalArgumentException("Supplied geoRegion has a null Guid value.");
		}
		return storeHelper.putSingle(GR_TABLE, GR_COL_FAMILY, GEOREGION_COL, geoRegion.getGuid(), geoRegion);
	}

	/**
	 * @see org.ala.dao.GeoRegionDao#getAll()
	 */
	@Override
	public List<GeoRegion> getAll() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.ala.dao.GeoRegionDao#getByGuid(java.lang.String)
	 */
	@Override
	public GeoRegion getByGuid(String guid) throws Exception {
		return (GeoRegion) storeHelper.get(GR_TABLE, GR_COL_FAMILY, GEOREGION_COL, guid, GeoRegion.class);
	}

	/**
	 * @see org.ala.dao.GeoRegionDao#getByType(java.lang.String)
	 */
	@Override
	public List<GeoRegion> getByType(String regionType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param storeHelper the storeHelper to set
	 */
	public void setStoreHelper(StoreHelper storeHelper) {
		this.storeHelper = storeHelper;
	}
}
