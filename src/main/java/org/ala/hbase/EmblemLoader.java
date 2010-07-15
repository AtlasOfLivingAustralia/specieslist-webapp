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
package org.ala.hbase;

import java.io.FileReader;

import javax.inject.Inject;

import org.ala.dao.GeoRegionDao;
import org.ala.model.TaxonConcept;
import org.ala.util.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loads the emblems for each state.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("emblemLoader")
public class EmblemLoader {

	public static String STATE_EMBLEMS = "/data/bie-staging/emblems/stateEmblems.txt";
	
	@Inject
	protected GeoRegionDao geoRegionDao;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SpringUtils.getContext();
		EmblemLoader l = context.getBean(EmblemLoader.class);
		l.load();
	}
	
	public void load() throws Exception {
		CSVReader r = new CSVReader(new FileReader(STATE_EMBLEMS), '\t');
		String[] cols = r.readNext();
		while(cols!=null){
			
			String stateGuid = cols[0];
			String name = cols[1];
			String emblemType = cols[2];
			String guid = cols[3];
			TaxonConcept tc = new TaxonConcept();
			tc.setGuid(guid);
			tc.setNameString(name);
			if("bird".equalsIgnoreCase(emblemType)){
				geoRegionDao.addBirdEmblem(stateGuid, tc);
			} else if ("plant".equalsIgnoreCase(emblemType)){
				geoRegionDao.addPlantEmblem(stateGuid, tc);
			} else if ("animal".equalsIgnoreCase(emblemType)){
				geoRegionDao.addAnimalEmblem(stateGuid, tc);
			}
			cols = r.readNext();
		}
		r.close();
	}

	/**
	 * @param geoRegionDao the geoRegionDao to set
	 */
	public void setGeoRegionDao(GeoRegionDao geoRegionDao) {
		this.geoRegionDao = geoRegionDao;
	}
}
