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
package org.ala.model;

import java.util.Date;
import java.util.Map;

/**
 * An InfoSource model object, influenced by the EML metadata profile described
 * here: http://code.google.com/p/ala-bie/wiki/MetadataProfile
 * 
 * @author Dave Martin
 */
public class InfoSource {

	/** Dataset properties */ 
	protected int id;
	protected String acronym; // e.g. NIMPIS 
	protected String name; 	// e.g.
	protected String theAbstract; //human readable description
	protected String datasetType;
	protected String websiteUrl;
	protected String logoUrl;
	
	/** Geospatial properties */
	protected String states;
	protected String geographicDescription;
	protected String wkt;
	protected String northCoordinate;
	protected String southCoordinate;
	protected String eastCoordinate;
	protected String westCoordinate;
	
	/** Temporal properties */ 
	protected Date singleDate;
	protected Date startDate;
	protected Date endDate;
	
	/** Taxonomic properties */ 	
	protected String scientificNames;
    
	/** Harvesting properties */
	protected Map<String,String> connectionParams;
	protected String harvester;
    protected String documentMapper;
    protected int documentCount;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the websiteUrl
	 */
	public String getWebsiteUrl() {
		return websiteUrl;
	}
	/**
	 * @param websiteUrl the websiteUrl to set
	 */
	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}
	/**
	 * @return the logoUrl
	 */
	public String getLogoUrl() {
		return logoUrl;
	}
	/**
	 * @param logoUrl the logoUrl to set
	 */
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	/**
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}
	/**
	 * @param acronym the acronym to set
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}
	/**
	 * @return the theAbstract
	 */
	public String getTheAbstract() {
		return theAbstract;
	}
	/**
	 * @param theAbstract the theAbstract to set
	 */
	public void setTheAbstract(String theAbstract) {
		this.theAbstract = theAbstract;
	}
	/**
	 * @return the datasetType
	 */
	public String getDatasetType() {
		return datasetType;
	}
	/**
	 * @param datasetType the datasetType to set
	 */
	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}
	/**
	 * @return the states
	 */
	public String getStates() {
		return states;
	}
	/**
	 * @param states the states to set
	 */
	public void setStates(String states) {
		this.states = states;
	}
	/**
	 * @return the geographicDescription
	 */
	public String getGeographicDescription() {
		return geographicDescription;
	}
	/**
	 * @param geographicDescription the geographicDescription to set
	 */
	public void setGeographicDescription(String geographicDescription) {
		this.geographicDescription = geographicDescription;
	}
	/**
	 * @return the wkt
	 */
	public String getWkt() {
		return wkt;
	}
	/**
	 * @param wkt the wkt to set
	 */
	public void setWkt(String wkt) {
		this.wkt = wkt;
	}
	/**
	 * @return the northCoordinate
	 */
	public String getNorthCoordinate() {
		return northCoordinate;
	}
	/**
	 * @param northCoordinate the northCoordinate to set
	 */
	public void setNorthCoordinate(String northCoordinate) {
		this.northCoordinate = northCoordinate;
	}
	/**
	 * @return the southCoordinate
	 */
	public String getSouthCoordinate() {
		return southCoordinate;
	}
	/**
	 * @param southCoordinate the southCoordinate to set
	 */
	public void setSouthCoordinate(String southCoordinate) {
		this.southCoordinate = southCoordinate;
	}
	/**
	 * @return the eastCoordinate
	 */
	public String getEastCoordinate() {
		return eastCoordinate;
	}
	/**
	 * @param eastCoordinate the eastCoordinate to set
	 */
	public void setEastCoordinate(String eastCoordinate) {
		this.eastCoordinate = eastCoordinate;
	}
	/**
	 * @return the westCoordinate
	 */
	public String getWestCoordinate() {
		return westCoordinate;
	}
	/**
	 * @param westCoordinate the westCoordinate to set
	 */
	public void setWestCoordinate(String westCoordinate) {
		this.westCoordinate = westCoordinate;
	}
	/**
	 * @return the singleDate
	 */
	public Date getSingleDate() {
		return singleDate;
	}
	/**
	 * @param singleDate the singleDate to set
	 */
	public void setSingleDate(Date singleDate) {
		this.singleDate = singleDate;
	}
	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	/**
	 * @return the scientificNames
	 */
	public String getScientificNames() {
		return scientificNames;
	}
	/**
	 * @param scientificNames the scientificNames to set
	 */
	public void setScientificNames(String scientificNames) {
		this.scientificNames = scientificNames;
	}
	/**
	 * @return the connectionParams
	 */
	public Map<String, String> getConnectionParams() {
		return connectionParams;
	}
	/**
	 * @param connectionParams the connectionParams to set
	 */
	public void setConnectionParams(Map<String, String> connectionParams) {
		this.connectionParams = connectionParams;
	}
    /**
     * @return the harvesterId
     */
    public String getHarvester() {
        return harvester;
    }
    /**
     * @param harvester the harvesterId to set
     */
    public void setHarvester(String harvester) {
        this.harvester = harvester;
    }
    /**
     * @return the documentMapper
     */
    public String getDocumentMapper() {
        return documentMapper;
    }
    /**
     * @param documentMapper the documentMapper to set
     */
    public void setDocumentMapper(String documentMapper) {
        this.documentMapper = documentMapper;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }
    
    @Override
    public String toString() {
        return "InfoSource [" + "id " + id + "; " + "name " + name + "; " + "acronym " + acronym + "; " +
               "docCount " + documentCount + "; " + "documentMapper " + documentMapper + "; " + "websiteUrl " + websiteUrl + "]";
    }
}
