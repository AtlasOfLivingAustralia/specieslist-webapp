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

import org.ala.model.Document;
import java.util.List;
import java.util.Map;

/**
 * An interface for an Document data access object (DAO)
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public interface DocumentDAO {

    /**
     * Retrieve Document by the ID.
     *
     * @param uri
     * @return
     */
    Document getById(final int documentId);

    /**
     * Retrieve Document by the URI.
     *
     * @param uri
     * @return
     */
    Document getByUri(final String uri);
    
    /**
     * Retrieve Documens by the infosource id.
     *
     * @param infosourceId
     * @return
     */
    List<Document> getByInfosourceId(final int infosourceId);

    /**
     * @param docs
     */
    List<Map<String, Object>> getUrlsForInfoSource(final int infoSourceId);
    
    /**
     * Change infosource id by document ID
     *
     * @param documentId, infosourceId
     */
    void changeInfosourceIdByDocumentId(final int documentId, final int infosourceId);

    /**
     * Save this document to the repository.
     *
     * @param docs
     */
    void save(final Document doc);

    /**
     * @param docs
     */
    void save(final List<Document> docs);

    /**
     * @param doc
     */
    void update(final Document doc);
}
