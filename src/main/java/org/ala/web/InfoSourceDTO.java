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

package org.ala.web;

import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * DTO Bean to represent an Info Source
 *
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class InfoSourceDTO implements Comparable<InfoSourceDTO> {
    private String infoSourceName;
    private String infoSourceURL;
    private Integer infoSourceId;
    private String text;
    private Set<String> sections;
    private final static Logger logger = Logger.getLogger(InfoSourceDTO.class);

    public InfoSourceDTO(String infoSourceName, String infoSourceURL, Integer infoSourceId) {
        this.infoSourceName = infoSourceName;
        this.infoSourceURL = infoSourceURL;
        this.infoSourceId = infoSourceId;
    }

    public InfoSourceDTO() {}

    @Override
    public boolean equals(Object obj) {
        if(obj!=null && obj instanceof InfoSourceDTO){
            InfoSourceDTO other = (InfoSourceDTO) obj;
            if(infoSourceName!=null && infoSourceName.equalsIgnoreCase(other.getInfoSourceName())){
                //compare urls if not null
                if(other.getInfoSourceURL()!=null && infoSourceURL!=null){
                    return other.getInfoSourceURL().equals(infoSourceURL);
                }
                //return true as the names are the same
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.infoSourceName != null ? this.infoSourceName.hashCode() : 0);
        hash = 43 * hash + (this.infoSourceURL != null ? this.infoSourceURL.hashCode() : 0);
        hash = 43 * hash + (this.infoSourceId != null ? this.infoSourceId.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(InfoSourceDTO o) {
        //check the infosources
        if(o.getInfoSourceId()!=null && infoSourceId!=null){
            return infoSourceId.compareTo(o.getInfoSourceId());
        }
        if(o.getInfoSourceName()!=null && infoSourceName!=null){
            return infoSourceName.compareTo(o.getInfoSourceName());
        }
        return -1;
    }

    public Integer getInfoSourceId() {
        return infoSourceId;
    }

    public void setInfoSourceId(Integer infoSourceId) {
        this.infoSourceId = infoSourceId;
    }

    public void setInfoSourceId(String infoSourceId) {
        if (infoSourceId == null) {
            this.infoSourceId = 999999;
        } else {
            try {
                this.infoSourceId = Integer.parseInt(infoSourceId);
            } catch (NumberFormatException numberFormatException) {
                logger.error("Error setting Integer from String: "+numberFormatException.getLocalizedMessage(), numberFormatException);
                this.infoSourceId = 999999;
            }
        }

    }

    public String getInfoSourceName() {
        return infoSourceName;
    }

    public void setInfoSourceName(String infoSourceName) {
        this.infoSourceName = infoSourceName.trim();
    }

    public String getInfoSourceURL() {
        return infoSourceURL;
    }

    public void setInfoSourceURL(String infoSourceURL) {
        this.infoSourceURL = infoSourceURL.trim();
    }

    public Set<String> getSections() {
        return sections;
    }

    public void setSections(Set<String> sections) {
        this.sections = sections;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}