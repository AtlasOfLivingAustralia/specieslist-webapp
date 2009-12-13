/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: oak021 $
 * @version $Id:  SearchResult.java 697 2009-08-01 00:23:45Z oak021 $
 */
package csiro.diasb.datamodels;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Encapsulates results of a Fedora repository search
 * @author oak021
 */
public class SearchResult {

    String pid;
    String guid;
    String contentModel;
    String rank;
    String rankId;
    String title;
    String highlighting;
    Set<Entry<String, List<String>>> highLights;

    public SearchResult(String pid, String title, String contentModel) {
        this.pid = pid;
        this.title = title;
        if (contentModel != null)
            this.contentModel = contentModel.replace('.',':');
    }

    public SearchResult(String pid, String title, String rank, String rankId) {
        this.pid = pid;
        this.title = title;
        this.rank = rank;
        this.rankId = rankId;
    }

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

    public String getHighlighting() {
        return highlighting;
    }

    public void setHighlighting(String highlighting) {
        this.highlighting = highlighting;
    }

    public String getUrlMapper() {
        if (contentModel == null) {
            return "datastream";
        }

        if (contentModel.equals("ala:TaxonConceptContentModel")) {
            return "species";  // taxa
        } else if (contentModel.equals("ala:TaxonNameContentModel")) {
            return "name";
        } else if (contentModel.equals("ala:PublicationContentModel")) {
            return "pub";
        } else if (contentModel.equals("ala:ImageContentModel")) {
            return "image";
        } else if (contentModel.equals("ala:HtmlPageContentModel")) {
            return "html";
        }
        return "datastream";
    }

    public String getContentModelInitial() {
        if (contentModel == null) {
            return "-";
        }

        if (contentModel.equals("ala:TaxonConceptContentModel")) {
            return "TC";
        } else if (contentModel.equals("ala:TaxonNameContentModel")) {
            return "TN";
        } else if (contentModel.equals("ala:PublicationContentModel")) {
            return "PUB";
        } else if (contentModel.equals("ala:ImageContentModel")) {
            return "IMG";
        } else if (contentModel.equals("ala:HtmlPageContentModel")) {
            return "HTML";
        } else if (contentModel.equals("ala:InfoSourceContentModel")) {
            return "IS";
        }
        return "-";
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

