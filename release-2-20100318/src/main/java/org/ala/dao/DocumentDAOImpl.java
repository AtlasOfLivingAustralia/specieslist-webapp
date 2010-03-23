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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.model.Document;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

/**
 * The Document Dao.
 *
 * @author Dave Martin (David.Martin@csiro.au)
 */
@Component("documentDAO")
public class DocumentDAOImpl extends JdbcDaoSupport implements DocumentDAO {
    /**
     * Constructor to set DataSource via DI
     *
     * @param dataSource the DataSource to inject
     */
    @Inject
    public DocumentDAOImpl(DataSource dataSource) {
        this.setDataSource(dataSource);
        this.setJdbcTemplate(this.createJdbcTemplate(dataSource));
    }

    /**
     * Default Constructor
     */
     public DocumentDAOImpl() {}

	/**
	 * Save this document to the repository.
	 * 
	 * @param doc
	 */
	public void save(final Document doc){
		KeyHolder keyHolder = new GeneratedKeyHolder();
		getJdbcTemplate().update(
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
					PreparedStatement ps = conn.prepareStatement(
							"insert into document " +
							"(infosource_id,uri,file_path,mime_type,parent_document_id,created,modified) " +
							"values (?,?,?,?,?,?,?)");
					ps.setInt(1, doc.getInfoSourceId());
					ps.setString(2, doc.getUri());
					ps.setString(3, doc.getFilePath());
					ps.setString(4, doc.getMimeType());
					ps.setObject(5, doc.getParentDocumentId());
					ps.setObject(6, new Date());
					ps.setObject(7, new Date());
					return ps;
				}
			},
			keyHolder
		);
		
		doc.setId(keyHolder.getKey().intValue());
	}
	
	/**
	 * Save this document to the repository.
	 * 
	 * @param docs
	 */
	public void update(final Document doc){
		getJdbcTemplate().update(
			new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
					PreparedStatement ps = conn.prepareStatement(
							"update document set " +
							"file_path=?,mime_type=?,modified=? " +
							"where id=?");
					ps.setString(1, doc.getFilePath());
					ps.setString(2, doc.getMimeType());
					ps.setObject(3, new Date());
					ps.setInt(4, doc.getId());
					return ps;
				}
			}
		);
	}
	
	/**
	 * Retrieve Document by the ID.
	 * 
	 * @param uri
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Document getById(final int documentId){
		List<Document> results = (List<Document>) getJdbcTemplate().query(
				//"select id, infosource_id, uri, file_path, mime_type from document where id=?",
                "select d.id, d.infosource_id, ifs.name, ifs.uri, d.uri, d.file_path, d.mime_type from " +
                "document d INNER JOIN infosource ifs ON ifs.id = d.infosource_id where d.id=?",
				new Object[]{documentId}, 
				rowMapper
		);
		if (results.size() == 0) {
			return null;
		} else if (results.size()>1) {
			logger.warn("Found multiple Document with id[" + documentId + "]");
		}
		return results.get(0);
	}	
	
	/**
	 * Retrieve Document by the URI.
	 * 
	 * @param uri
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Document getByUri(final String uri){
		List<Document> results = (List<Document>) getJdbcTemplate().query(
				//"select id, infosource_id, uri, file_path, mime_type from document where uri=?",
                "select d.id, d.infosource_id, ifs.name, ifs.uri, d.uri, d.file_path, d.mime_type from " +
                "document d INNER JOIN infosource ifs ON ifs.id = d.infosource_id where d.uri=?",
				new Object[]{uri}, 
				rowMapper
		);
		if (results.size() == 0) {
			return null;
		} else if (results.size()>1) {
			logger.warn("Found multiple Document with uri[" + uri + "]");
		}
		return results.get(0);
	}	
	
	final RowMapper rowMapper = new RowMapper(){
		@Override
		public Object mapRow(ResultSet resultSet, int rowId) throws SQLException {
			Document doc = new Document();
			doc.setId(resultSet.getInt(1));
			doc.setInfoSourceId(resultSet.getInt(2));
            doc.setInfoSourceName(resultSet.getString(3));
            doc.setInfoSourceUri(resultSet.getString(4));
			doc.setUri(resultSet.getString(5));
			doc.setFilePath(resultSet.getString(6));
			doc.setMimeType(resultSet.getString(7));
			return doc;
		}
	};
	
	/**
	 * @param docs
	 */
	public void save(final List<Document> docs){
		getJdbcTemplate().batchUpdate("insert into document (infosource_id, uri, file_path, mime_type) values (?,?,?,?)", 
				new BatchPreparedStatementSetter(){
			@Override
			public int getBatchSize() {
				return docs.size();
			}
			@Override
			public void setValues(PreparedStatement pstmt, int rowId)
					throws SQLException {
				pstmt.setString(1, docs.get(rowId).getUri());
				pstmt.setString(2, docs.get(rowId).getFilePath());
				pstmt.setString(3, docs.get(rowId).getMimeType());
			}
		});
	}
	
	/**
	 * @param docs
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getUrlsForInfoSource(final int infoSourceId){
		return (List<Map<String, Object>>) getJdbcTemplate().queryForList("select id, uri from document");
	}
    
}