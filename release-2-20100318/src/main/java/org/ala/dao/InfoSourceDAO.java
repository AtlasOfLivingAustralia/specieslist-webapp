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
import org.ala.model.InfoSource;

/**
 * An interface for an InfoSource data access object (DAO)
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public interface InfoSourceDAO {
    /**
     * Get an InfoSource object for an infoSource id
     * 
     * @param infoSourceId
     * @return
     */
    public InfoSource getById(final int infoSourceId);
    
    /**
     * Get an InfoSource object for an infoSource id
     * 
     * @param infoSourceId
     * @return
     */
    public InfoSource getByUri(final String uri);

    /**
     * Get a list of all info source Ids
     *
     * @return infoSourceIds
     */
    public List<Integer> getIdsforAll();

    /**
     * Get a list of info sources for a given dataset type
     *
     * @return
     */
    public List<InfoSource> getAllByDatasetType();
}
