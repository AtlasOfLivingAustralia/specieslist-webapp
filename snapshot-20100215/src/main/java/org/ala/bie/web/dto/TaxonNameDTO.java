/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package org.ala.bie.web.dto;

/**
 * DTO bean to represent the FC Taxon Name object
 *
 * @author dos009
 */
public class TaxonNameDTO {
    /* Fields */
    String pid;
    String title;
    String nameComplete;
    String rank;
    String guid;
    String source;

    /**
     * Constructor for setting just PID field
     *
     * @param pid
     */
    public TaxonNameDTO(String pid) {
        this.pid = pid;
    }

    /**
     * Constructor for setting all 4 fields
     *
     * @param pid
     * @param title
     * @param nameComplete
     * @param rank
     */
    public TaxonNameDTO(String pid, String title, String nameComplete, String rank, String guid, String source) {
        this.pid = pid;
        this.title = title;
        this.nameComplete = nameComplete;
        this.rank = rank;
        this.guid = guid;
        this.source = source;
    }

    /**
     * Custom toString method
     *
     * @return string representation of this object
     */
    @Override
    public String toString() {
        return "pid: "+pid+"; title: "+title+"; nameComplete: "+nameComplete+"; rank: "+rank;
    }

    /*
     * Getters & Setters
     */

    public String getNameComplete() {
        return nameComplete;
    }

    public void setNameComplete(String nameComplete) {
        this.nameComplete = nameComplete;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}