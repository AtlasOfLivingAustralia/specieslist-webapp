/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  SearchResult.java 697 2009-08-01 00:23:45Z oak021 $
 */
package org.ala.bie.web.dto;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.ala.bie.web.util.ContentModelEnum;

/**
 * Encapsulates results of a Fedora repository search
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class SearchResultDTO {

    String pid;
    String guid;
    String contentModel;
    String rank;
    String rankId;
    String title;
    String highlighting;
    Set<Entry<String, List<String>>> highLights;

    /**
     * Constructor with 3 String args
     *
     * @param pid
     * @param title
     * @param contentModel
     */
    public SearchResultDTO(String pid, String title, String contentModel) {
        this.pid = pid;
        this.title = title;
        if (contentModel != null)
            this.contentModel = contentModel.replace('.',':');
    }

    /**
     * Constructor with 4 String args
     * 
     * @param pid
     * @param title
     * @param rank
     * @param rankId
     */
    public SearchResultDTO(String pid, String title, String rank, String rankId) {
        this.pid = pid;
        this.title = title;
        this.rank = rank;
        this.rankId = rankId;
    }

    /**
     * Contructor with no args
     */
    public SearchResultDTO() {}

    /**
     * Custom toString method
     * 
     * @return string representation of object
     */
    @Override
    public String toString() {
        return "SearchResultDTO [" + "contentModel: " + contentModel + " " + "guid: " + guid + " " + "pid: " + pid + " " + "rank: " + rank + " " + "rankId: " + rankId + " " + "title: " + title + "]";
    }


    /*
     * Getters & Setters
     */
    public String getContentModel() {
        return contentModel;
    }

    public void setContentModel(String contentModel) {
        if (contentModel != null)
            this.contentModel = contentModel.replace('.',':');
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPid() {
        return pid;
    }

    public void setHighLights(Set<Entry<String, List<String>>> entrySet) {
        this.highLights = entrySet;
    }

    @JsonIgnore
    public Set<Entry<String, List<String>>> gethighLights() {
        return highLights;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public String getHighlighting() {
        return highlighting;
    }

    public void setHighlighting(String highlighting) {
        this.highlighting = highlighting;
    }

    public String getUrlMapper() {
        String urlMapper = null;
        
        if (contentModel == null) {
            urlMapper = ContentModelEnum.getForId("").getName();
        } else {
            urlMapper = ContentModelEnum.getForId(contentModel).getName();
        }
        
        return urlMapper;
    }

    public String getContentModelInitial() {
        String code = null;

        if (contentModel == null) {
            code = ContentModelEnum.getForId("").getCode();
        } else {
            code = ContentModelEnum.getForId(contentModel).getCode();
        }

        return code;
    }

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String rankId) {
        this.rankId = rankId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
    
}

