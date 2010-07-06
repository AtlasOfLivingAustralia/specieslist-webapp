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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.dao.GeoRegionDao;
import org.ala.model.GeoRegion;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
/**
 * This class loads the BIE with geographic regions.
 * 
 * This is currently connecting to the PostGIS database regionlookup
 * but it is intended to be replaced by harvesting this data out of the
 * gazetteer.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
public class GeoRegionLoader {
	
	protected Logger logger = Logger.getLogger(GeoRegionLoader.class);

	protected ApplicationContext context;
	/** Datasource for Postgres DB */
	@Inject
	protected DataSource gisDataSource;
	/** JDBC Template for Postgres DB */
	protected JdbcTemplate gisTemplate;
	/** The DAO for creating Geographic Regions */
	@Inject
	protected GeoRegionDao geoRegionDao;
	
	/**
	 * Run the loader, pulling geo regions from the backend
	 * of the gazetteer.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		GeoRegionLoader g = new GeoRegionLoader();
		g.init();
		g.sync();
		System.exit(1);
	}
	
	/**
	 * Initialise connections.
	 */
	private void init() {
		String[] locations = {
				"classpath:spring-external-ds.xml",
				"classpath:spring-profiler.xml", 
				"classpath:spring.xml"};
		context = new ClassPathXmlApplicationContext(locations);
		gisDataSource = (DataSource) context.getBean("gisDataSource");
		gisTemplate = new JdbcTemplate(gisDataSource);
		geoRegionDao = (GeoRegionDao) context.getBean("geoRegionDao");
	}
	
	/**
	 * Page through all geo_region definitions in postgres and sync with MySQL.
	 * 
	 * @throws Exception
	 */
	public void sync() throws Exception{
		System.out.println("Starting Postgis sync to BIE");
		syncGeoRegions();
		System.out.println("Finished Postgis sync to BIE");
	}

	/**
	 * Sync the geo regions.
	 * 
	 * @throws SQLException
	 */
	private void syncGeoRegions() throws SQLException {
		Connection gisConn = gisDataSource.getConnection();
		Statement stmt = gisConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = stmt.executeQuery("select gr.id, gr.name, gr.source, gr.acronym, gr.region_type, grt.name as grt_name " +
				"from geo_region gr " +
				"inner join geo_region_type grt ON grt.id=gr.region_type " +
				"where grt.id<6000");
		
		int i=0;
		long startTime = System.currentTimeMillis();
		while (rs.next()) {
			//geo region properties
			int id = rs.getInt("id");
			String name = rs.getString("name");
			String source = rs.getString("source");
			String acronym = rs.getString("acronym");
			String regionType = rs.getString("region_type");
			String regionTypeName = rs.getString("grt_name");
			//store this geo region
			try{
				handleRegion(id, name, acronym, source, regionType, regionTypeName);
			} catch(Exception e){
				logger.error(e.getMessage(), e);
			}
			i++;
			if(i % 100 == 0)
				System.err.println("No of regions synced indexed: "+i);
		}
		long finishTime = System.currentTimeMillis();
		System.out.println("Geo regions synchronized in "+ (finishTime-startTime)/1000 +" seconds.");
		rs.close();
		stmt.close();
		gisConn.close();
	}
	
	/**
	 * Sync a single region to the portal DB and calculates a bounding box for the area.
	 * 
	 * @param id
	 * @param name
	 * @param source
	 * @param regionType
	 * @throws Exception
	 */
	private void handleRegion(int id, String name, String acronym, String source, String regionType, String regionTypeName) throws Exception {

		//retrieve a single full string for describing this region in WKT format
		List<Map<String, Object>> wktResults  = gisTemplate.queryForList(
				"select AsText(ST_Union(the_geom)) as envelope " +
				"from geo_region_polygon " +
				"where region_id=? " +
				"group by region_id, the_geom", new Object[]{id});
		
		String fullWktString = (String) wktResults.get(0).get("envelope");

		List<Map<String, Object>> results = gisTemplate.queryForList(
				"select AsText(ST_Envelope(the_geom)) as envelope " +
				"from geo_region_polygon " +
				"where region_id=? " +
				"group by region_id, the_geom", new Object[]{id});
		
		Double minX = null;
		Double minY = null;
		Double maxX = null;
		Double maxY = null;
		
		for(Map<String, Object> result: results){
			String wktString = (String) result.get("envelope");
			Geometry geom =  new WKTReader().read(wktString);
			Coordinate[] coords = null;
			if (geom instanceof Polygon) {
				coords =  ((Polygon)geom).getCoordinates();
				if(minX==null || coords[0].x<minX)
					minX = coords[0].x;
				if(minY==null || coords[0].y<minY)
					minY = coords[0].y;
				if(maxX==null || coords[2].x>maxX)
					maxX = coords[2].x;
				if(maxY==null || coords[2].y>maxY)
					maxY = coords[2].y;
			}
		}
		
		GeoRegion geoRegion = new GeoRegion();
		geoRegion.setId(Integer.toString(id));
		geoRegion.setGuid(source+"/"+name);
		geoRegion.setName(name);
		geoRegion.setAcronym(acronym);
		geoRegion.setRegionType(regionType);
		geoRegion.setRegionTypeName(regionTypeName);
		geoRegion.setBounds(StringUtils.join(new Double[]{minX, minY, maxX, maxY}, ","));
		geoRegion.setWellKnownText(fullWktString);
		logger.info("GeoRegion "+geoRegion.getGuid());
		geoRegionDao.create(geoRegion);
	}

	/**
	 * @param gisDataSource the gisDataSource to set
	 */
	public void setGisDataSource(DataSource gisDataSource) {
		this.gisDataSource = gisDataSource;
	}

	/**
	 * @param geoRegionDao the geoRegionDao to set
	 */
	public void setGeoRegionDao(GeoRegionDao geoRegionDao) {
		this.geoRegionDao = geoRegionDao;
	}
}
