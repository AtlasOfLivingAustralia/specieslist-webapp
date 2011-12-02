/* *************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 * 
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package org.ala.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.model.Document;
import org.ala.model.InfoSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/**
 * JDBC implementation of a InfoSourceDAO
 *
 * @see org.ala.dao.InfoSourceDAO
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
@Component("infoSourceDAO")
public class InfoSourceDAOImpl extends JdbcDaoSupport implements InfoSourceDAO {
	
	private static final Logger logger = Logger.getLogger(InfoSourceDAOImpl.class);
	
    private static final String SELECT_ALL_IDS = "select id from infosource";
    private static final String SELECT_ALL_IDS_UIDS = "select id, uid from infosource";
    private static final String SELECT_UID_BY_INFOSOURCE_ID = "select uid from infosource where id=?";
	private static final String GET_BY_ID = "select inf.id, inf.name, uri, logo_url, description, connection_params, hv.class, " +
	            "document_mapper from infosource inf " +
	            "LEFT JOIN harvester hv ON hv.id=inf.harvester_id " +
	            "where inf.id=?";
	private static final String GET_BY_URI = "select inf.id, inf.name, uri, logo_url, description, connection_params, hv.class, " +
	            "document_mapper from infosource inf " +
	            "LEFT JOIN harvester hv ON hv.id=inf.harvester_id " +
	            "where inf.uri=?";
    private static final String GET_BY_DATASET_TYPE = "SELECT ins.id, ins.name, ins.dataset_type, ins.uri, " +
            "ins.logo_url, ins.description, count( d.id ) AS doc_count " +
            "FROM infosource ins " +
            "LEFT JOIN document d ON d.infosource_id = ins.id " +
            "GROUP BY ins.id " +
            "ORDER BY ins.dataset_type, name";
    
    @Override
    public void insertUidByName(final String name, final String uid){
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(
                            "update infosource set " +
                            "uid=? " +
                            "where name like ?");
                    ps.setString(1, uid);
                    ps.setString(2, name);
                    return ps;
                }
            }
        );
    }
    
    @Override
    public String getUidByInfosourceId (final String infosourceId) {
        List<String> uidList = (List<String>) getJdbcTemplate().queryForList(SELECT_UID_BY_INFOSOURCE_ID, new Object[] {infosourceId}, String.class);
        
        if (uidList.size() == 1) {
            return uidList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Constructor to set DataSource via DI
     *
     * @param dataSource the DataSource to inject
     */
    @Inject
    public InfoSourceDAOImpl(DataSource dataSource) {
        this.setDataSource(dataSource);
        logger.debug("InfoSource initialising... " + dataSource.toString());
    }

    /**
     * Default Constructor
     */
     public InfoSourceDAOImpl() {}

    /**
     * @see org.ala.dao.InfoSourceDAO#getIdsforAll()
     *
     * @return infoSourceIds
     */
     @Override
     public List<Integer> getIdsforAll() {
         List<Integer> infoSourceIds = (List<Integer>) getJdbcTemplate().queryForList(SELECT_ALL_IDS, null, Integer.class);
         return infoSourceIds;
     }

     /**
      * @see org.ala.dao.InfoSourceDAO#getByUri(java.lang.String)
      */
     @Override
     public InfoSource getByUri(String uri) {
         List<InfoSource> results = (List<InfoSource>) getJdbcTemplate().query(
                 GET_BY_URI,
                 new Object[]{uri},
                 rowMapper
     		);
 		if (results.size() == 0) {
 			return null;
 		} else if (results.size()>1) {
 			logger.warn("Found multiple InfoSources with URI [" + uri + "]");
 		}
 		return results.get(0);
     }
     
     /**
     * @see org.ala.dao.InfoSourceDAO#getById(int)
     *
     * @param infoSourceId
     * @return infoSource
     */
    @Override
    public InfoSource getById(int infoSourceId) {
        List<InfoSource> results = (List<InfoSource>) getJdbcTemplate().query(
            GET_BY_ID,
            new Object[]{infoSourceId},
            rowMapper
		);
		if (results.size() == 0) {
			return null;
		} else if (results.size()>1) {
			logger.warn("Found multiple InfoSources with id[" + infoSourceId + "]");
		}
		return results.get(0);
    }

    /**
     * @see org.ala.dao.InfoSourceDAO#getByDatasetType(int)
     *
     * @return infoSources
     */
    @Override
    public List<InfoSource> getAllByDatasetType() {
        List<InfoSource> infoSources = (List<InfoSource>) getJdbcTemplate().query(
                 GET_BY_DATASET_TYPE,
                 new Object[]{},
                 datasetTypeRowMapper
     		);
        return infoSources;
    }
    
    /**
     * Get the map between infosource Uid and infosource id
     *
     * @return uidInfosourceIDMap
     */
    @Override
    public Map<String, String> getInfosourceIdUidMap() {
        Map<String, String> uidInfosourceIDMap = new HashMap<String, String>();
        
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) getJdbcTemplate().queryForList(SELECT_ALL_IDS_UIDS);
        
        for (Map map : mapList) {
            uidInfosourceIDMap.put(String.valueOf(map.get("id")), String.valueOf(map.get("uid")));
        }
        
//        List<Integer> infosourceIdList = getIdsforAll();
//        
//        for (Integer infosourceId : infosourceIdList) {
//            String uid = getUidByInfosourceId(infosourceId.toString());
//            uidInfosourceIDMap.put(infosourceId.toString(), uid);
//        }
        
        return uidInfosourceIDMap;
    }

    final RowMapper<InfoSource> rowMapper = new RowMapper<InfoSource>(){
        @Override
        public InfoSource mapRow(ResultSet resultSet, int rowId) throws SQLException  {
            InfoSource is = new InfoSource();
            is.setId(resultSet.getInt(1));
            is.setName(resultSet.getString(2));
            is.setWebsiteUrl(resultSet.getString(3));
            is.setLogoUrl(resultSet.getString(4));
            is.setTheAbstract(resultSet.getString(5));
            Map<String, String> connParams = parseConnectionParams(resultSet.getString(6));
            is.setConnectionParams(connParams);
            is.setHarvester(resultSet.getString(7));
            //is.setDocumentMapper(createDocumentMapper(resultSet.getString(8)));
            is.setDocumentMapper(resultSet.getString(8));
            return is;
        }

        /**
         * Serialise JSON to java.util.Map using Jackson API
         */
        private Map<String, String> parseConnectionParams(String json) {
        	if(json==null || json.length()==0){
        		return new HashMap<String,String>();
        	}
        	
            Map<String, String> result = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
            } catch (JsonParseException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (JsonMappingException ex) {
                logger.error(ex.getMessage(), ex);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
            return result;
        }
    };

    final RowMapper<InfoSource> datasetTypeRowMapper = new RowMapper<InfoSource>(){
        @Override
        public InfoSource mapRow(ResultSet resultSet, int rowId) throws SQLException  {
            InfoSource is = new InfoSource();
            is.setId(resultSet.getInt(1));
            is.setName(resultSet.getString(2));
            is.setDatasetType(resultSet.getString(3));
            is.setWebsiteUrl(resultSet.getString(4));
            is.setLogoUrl(resultSet.getString(5));
            is.setTheAbstract(resultSet.getString(6));
            is.setDocumentCount(resultSet.getInt(7));
            //Map<String, String> connParams = parseConnectionParams(resultSet.getString(6));
            //is.setConnectionParams(connParams);
            //is.setHarvester(resultSet.getString(7));
            //is.setDocumentMapper(createDocumentMapper(resultSet.getString(8)));
            //is.setDocumentMapper(resultSet.getString(8));
            return is;
        }
    };
}
