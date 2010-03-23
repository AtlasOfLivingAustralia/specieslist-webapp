/***************************************************************************
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

import java.util.List;
import java.util.Map;

import org.ala.model.Term;
import org.ala.model.Vocabulary;
import org.ala.util.StatusType;

/**
 * An interface for a Vocabulary data access object (DAO)
 *
 * @author "Tommy Wang <tommy.wang@csiro.au>"
 */
public interface VocabularyDAO {
    /**
     * Get an Vocabulary object for an vocabulary id
     * 
     * @param vocabularyId
     * @return
     */
    public Vocabulary getById(final int vocabularyId);

    /**
     * Get a list of all vocabulary Ids
     *
     * @return vocabularyIds
     */
    public List<Integer> getIdsforAll();
    
    /**
     * Retrieve the mapped terms for the supplied raw value,
     * coming from the supplied infosource, with the supplied predicate.
     * 
     * @param infosourceId id of the infosource from which the string has been derived
     * @param predicate e.g. "hasConservationStatus"
     * @param rawValue the raw string value
     * @return list of terms this string has been mapped to.
     */
    public List<Term> getPreferredTermsFor(int infosourceId, String predicate, String rawValue);

    /**
     * Get a list of terms for a given status type (pest, conservation)
     *
     * @param statusType
     * @return list of terms
     */
    public List<String> getTermsForStatusType(StatusType statusType);

    /**
     * Get a Map of status terms for a given status type (pest, conservation)
     *
     * @param statusType 
     * @return map of terms
     */
    public Map<String, Integer> getTermMapForStatusType(StatusType statusType);
}
