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
	protected String uid;
	protected String acronym; // e.g. NIMPIS 
	protected String name; 	// e.g.
	protected String theAbstract; //human readable description
	protected String datasetType;
	protected String websiteUrl;
	protected String logoUrl;
	protected String rights;
	protected String citation;
	protected String basisOfRecord;
	
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
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}
	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
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
    
    /**
	 * @return the rights
	 */
	public String getRights() {
		return this.rights;
	}
	
	/**
	 * @param rights the rights to set
	 */
	public void setRights(String rights) {
		this.rights = rights;
	}
	
	/**
	 * @return the citation
	 */
	public String getCitation() {
		return this.citation;
	}
	
	/**
	 * @param citation the citation to set
	 */
	public void setCitation(String citation) {
		this.citation = citation;
	}
	
	/**
	 * @return the basisOfRecord
	 */
	public String getBasisOfRecord() {
		return this.basisOfRecord;
	}
	
	/**
	 * @param basisOfRecord the basisOfRecord to set
	 */
	public void setBasisOfRecord(String basisOfRecord) {
		this.basisOfRecord = basisOfRecord;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InfoSource [id=");
		builder.append(this.id);
		builder.append(", name=");
		builder.append(this.name);
		builder.append(", acronym=");
		builder.append(this.acronym);
		builder.append(", basisOfRecord=");
		builder.append(this.basisOfRecord);
		builder.append(", citation=");
		builder.append(this.citation);
		builder.append(", connectionParams=");
		builder.append(this.connectionParams);
		builder.append(", datasetType=");
		builder.append(this.datasetType);
		builder.append(", documentCount=");
		builder.append(this.documentCount);
		builder.append(", documentMapper=");
		builder.append(this.documentMapper);
		builder.append(", eastCoordinate=");
		builder.append(this.eastCoordinate);
		builder.append(", endDate=");
		builder.append(this.endDate);
		builder.append(", geographicDescription=");
		builder.append(this.geographicDescription);
		builder.append(", harvester=");
		builder.append(this.harvester);
		builder.append(", logoUrl=");
		builder.append(this.logoUrl);
		builder.append(", northCoordinate=");
		builder.append(this.northCoordinate);
		builder.append(", rights=");
		builder.append(this.rights);
		builder.append(", scientificNames=");
		builder.append(this.scientificNames);
		builder.append(", singleDate=");
		builder.append(this.singleDate);
		builder.append(", southCoordinate=");
		builder.append(this.southCoordinate);
		builder.append(", startDate=");
		builder.append(this.startDate);
		builder.append(", states=");
		builder.append(this.states);
		builder.append(", theAbstract=");
		builder.append(this.theAbstract);
		builder.append(", websiteUrl=");
		builder.append(this.websiteUrl);
		builder.append(", westCoordinate=");
		builder.append(this.westCoordinate);
		builder.append(", wkt=");
		builder.append(this.wkt);
		builder.append("]");
		return builder.toString();
	}
    
    /**
     * @param infoSource
     * @param id
     * @return SQL Insert statement
     */
    public String toSqlInsertString(int id) {
		StringBuilder sqlStmt = new StringBuilder("INSERT INTO infosource SET id=" + id);
		
		if (this.getAcronym() != null) {
			sqlStmt.append(",acronym='" + this.getAcronym() + "'");
		}
		if (this.getName() != null) {
			sqlStmt.append(",name='" + this.getName() + "'");
		}
		if (this.getTheAbstract() != null) {
			sqlStmt.append(",description='" + this.getTheAbstract() + "'");
		}
		if (this.getWebsiteUrl() != null) {
			sqlStmt.append(",website_url='" + this.getWebsiteUrl() + "'");
		}
		if (this.getLogoUrl() != null) {
			sqlStmt.append(",logo_url='" + this.getLogoUrl() + "'");
		}
		if (this.getDatasetType() != null) {
			sqlStmt.append(",dataset_type=" + this.getDatasetType());
		}
		if (this.getRights() != null) {
			sqlStmt.append(",rights='" + this.getRights() + "'");
		}
		if (this.getCitation() != null) {
			sqlStmt.append(",citation='" + this.getCitation() + "'");
		}
		if (this.getBasisOfRecord() != null) {
			sqlStmt.append(",basis_of_record='" + this.getBasisOfRecord() + "'");
		}
		if (this.getStates() != null) {
			sqlStmt.append(",states='" + this.getStates() + "'");
		}
		if (this.getGeographicDescription() != null) {
			sqlStmt.append(",geographic_description='" + this.getGeographicDescription() + "'");
		}
		if (this.getWkt() != null) {
			sqlStmt.append(",well_known_text='" + this.getWkt() + "'");
		}
		if (this.getNorthCoordinate() != null) {
			sqlStmt.append(",north_coordinate=" + this.getNorthCoordinate());
		}
		if (this.getSouthCoordinate() != null) {
			sqlStmt.append(",south_coordinate=" + this.getSouthCoordinate());
		}
		if (this.getEastCoordinate() != null) {
			sqlStmt.append(",east_coordinate=" + this.getEastCoordinate());
		}
		if (this.getWestCoordinate() != null) {
			sqlStmt.append(",west_coordinate=" + this.getWestCoordinate());
		}
		if (this.getSingleDate() != null) {
			sqlStmt.append(",single_date='" + this.getSingleDate() + "'");
		}
		if (this.getStartDate() != null) {
			sqlStmt.append(",start_date='" + this.getStartDate() + "'");
		}
		if (this.getEndDate() != null) {
			sqlStmt.append(",end_date='" + this.getEndDate() + "'");
		}
		if (this.getScientificNames() != null) {
			sqlStmt.append(",scientific_names='" + this.getScientificNames() + "'");
		}
		if (this.getName() != null) {
			sqlStmt.append(",name='" + this.getName() + "'");
		}
		if (this.getHarvester() != null) {
			sqlStmt.append(",harvester_id=" + this.getHarvester());
		}
		if (this.getDocumentMapper() != null) {
			sqlStmt.append(",document_mapper='" + this.getDocumentMapper() + "'");
		}
		
    	return sqlStmt.toString();
    }

}
