/**************************************************************************
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.ala.model.Term;
import org.ala.model.Vocabulary;
import org.ala.util.StatusType;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;

/**
 * JDBC implementation of a VocabularyDAO
 *
 * @see org.ala.dao.VocabularyDAO
 * @author "Tommy Wang <tommy.wang@csiro.au>"
 */
@Component("vocabularyDAO")
public class VocabularyDAOImpl extends JdbcDaoSupport implements VocabularyDAO {

	protected static final Logger logger = Logger.getLogger(VocabularyDAO.class);
	
	public static final String SELECT_ALL_VOC_IDS = "select id from vocabulary";

    public static final String SELECT_PEST_TERMS = "SELECT DISTINCT t.term_string FROM term t " +
            "INNER JOIN vocabulary v ON v.id = t.vocabulary_id " +
            "INNER JOIN term_mapping tm ON t.id = tm.target_term_id " +
            "WHERE v.predicate_id = 1";

    public static final String SELECT_CONSERVATION_TERMS = "SELECT DISTINCT t.term_string FROM term t " +
            "INNER JOIN vocabulary v ON v.id = t.vocabulary_id " +
            "INNER JOIN term_mapping tm ON t.id = tm.target_term_id " +
            "WHERE v.predicate_id = 2";

	public static final String SELECT_BY_ID = "select voc.id, voc.name, voc.uri, voc.description, voc.infosource_id, p.predicate " +
	            "from vocabulary voc " +
	            "left join ON voc.predicate_id=p.id " +
	            "where voc.id=?";

	public static final String GET_PREFERRED_TERMS="select t.id as 'target_id', t.term_string as 'target_term', target_predicate.predicate as 'target_predicate' " + 
			"from term s " + 
			"inner join term_mapping tm ON s.id = tm.source_term_id " + 
			"inner join term t ON t.id = tm.target_term_id " + 
			"inner join vocabulary v ON v.id = s.vocabulary_id " + 
			"inner join predicate p ON p.id = v.predicate_id " + 
			"inner join vocabulary target_voc ON target_voc.id = t.vocabulary_id " + 
			"inner join predicate target_predicate ON target_predicate.id = target_voc.predicate_id " + 
			"where s.term_string like ? " + 
			"and p.predicate=? " + 
			"and v.infosource_id=?";
	
	public static final String GET_TERMS_BY_INFOSOURCE_ID="select s.id as 'source_id', s.term_string as 'source_term', t.term_string as 'target_term', p.predicate as 'predicate' " + 
			"from term s " + 
			"inner join vocabulary v ON v.id = s.vocabulary_id " + 
			"inner join predicate p ON p.id = v.predicate_id " + 
			"inner join term_mapping tm ON tm.source_term_id = s.id " +
			"inner join term t ON t.id = tm.target_term_id " +
			"where v.infosource_id=?";
	 
    /**
     * Constructor to set DataSource via DI
     *
     * @param dataSource the DataSource to inject
     */
    @Inject
    public VocabularyDAOImpl(DataSource dataSource) {
        this.setDataSource(dataSource);
        logger.debug("Vocabulary initialising... " + dataSource.toString());
    }

    /**
     * Default Constructor
     */
     public VocabularyDAOImpl() {}

    /**
     * @see org.ala.dao.VocabularyDAO#getIdsforAll()
     *
     * @return VocabularyIds
     */
     public List<Integer> getIdsforAll() {
         return (List<Integer>) getJdbcTemplate().queryForList(SELECT_ALL_VOC_IDS, null, Integer.class);
     }

     /**
      * @see org.ala.dao.VocabularyDAO#getTermMapForStatusType(StatusType)
      *
      * @param statusType
      * @return
      */
     @Override
     public Map<String, Integer> getTermMapForStatusType(StatusType statusType) {
         Map<String, Integer> termMap = new HashMap<String, Integer>();

         for (String status : getTermsForStatusType(statusType)) {
             termMap.put(status, 1);
         }

         return termMap;
     }

     /**
      * Get a list of status terms for a given status type
      *
      * @param statusType
      * @return
      */
     public List<String> getTermsForStatusType(StatusType statusType) {
         switch(statusType) {
            case PEST:
                return (List<String>) getJdbcTemplate().queryForList(SELECT_PEST_TERMS, null, String.class);
            case CONSERVATION:
                return (List<String>) getJdbcTemplate().queryForList(SELECT_CONSERVATION_TERMS, null, String.class);
         }

         throw new IllegalArgumentException("Unknown StatusType: "+statusType);
     }

     /**
     * @see org.ala.dao.VocabularyDAO#getById(int)
     *
     * @param VocabularyId
     * @return
     */
    public Vocabulary getById(int VocabularyId) {
        List<Vocabulary> results = (List<Vocabulary>) getJdbcTemplate().query(
            SELECT_BY_ID,
            new Object[]{VocabularyId},
            vocabularyRowMapper
		);
		if (results.size() == 0) {
			return null;
		} 
		
		return results.get(0);
    }

    /**
     * @see org.ala.dao.VocabularyDAO#getPreferredTermsFor(int, java.lang.String, java.lang.String)
     */
	@Override
	public List<Term> getPreferredTermsFor(int infosourceId, String predicate, String rawValue) {
        return (List<Term>) getJdbcTemplate().query(
                GET_PREFERRED_TERMS,
                new Object[]{rawValue, predicate,infosourceId},
                termRowMapper
    	);
	}
	
	/**
     * @see org.ala.dao.VocabularyDAO#getTermsByInfosourceId(int)
     */
	@Override
	public List<Map<String,Object>> getTermsByInfosourceId(int infosourceId) {
        return (List<Map<String,Object>>) getJdbcTemplate().queryForList(
                GET_TERMS_BY_INFOSOURCE_ID,
                new Object[]{infosourceId}
    	);
	}
	
    final RowMapper<Term> termRowMapper = new RowMapper<Term>(){
        public Term mapRow(ResultSet resultSet, int rowId) throws SQLException  {
            Term term = new Term();
            term.setId(resultSet.getInt(1));
            term.setTermString(resultSet.getString(2));
            term.setPredicate(resultSet.getString(3));
            return term;
        }
    };	
    
    final RowMapper<Vocabulary> vocabularyRowMapper = new RowMapper<Vocabulary>(){
        @Override
        public Vocabulary mapRow(ResultSet resultSet, int rowId) throws SQLException  {
            Vocabulary voc = new Vocabulary();
            voc.setId(resultSet.getInt(1));
            voc.setName(resultSet.getString(2));
            voc.setWebsiteUrl(resultSet.getString(3));
            voc.setDescription(resultSet.getString(4));
            voc.setInfosourceId(resultSet.getInt(5));
            voc.setPredicate(resultSet.getString(6));
            return voc;
        }
    };
}
