/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package org.ala.bie.web.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO bean to represent the FC Taxon Concept object
 *
 * @author dos009
 */
public class TaxonConceptDTO {
    /* Fields */
    String pid;
    String title;
    String scientificName;
    String taxonNameGuid;
    String rank;
    String guid;
    String source;
    List<String> childTaxa = new ArrayList<String>();
    List<String> parentTaxa = new ArrayList<String>();

    /**
     * No-args contructor
     */
    public TaxonConceptDTO() {}

    /**
     * Constructor for setting just PID field
     *
     * @param pid
     */
    public TaxonConceptDTO(String pid) {
        this.pid = pid;
    }
    
    /**
     * Constructor for setting all 4 fields
     *
     * @param pid
     * @param title
     */
    public TaxonConceptDTO(String pid, String title) {
        this.pid = pid;
        this.title = title;
    }

    /**
     * Custom toString method
     *
     * @return string representation of this object
     */
    @Override
    public String toString() {
        return "pid: "+pid+"; title: "+title+"; scientificName: "+scientificName+"; rank: "+rank;
    }
    
    /*
     * Getters & Setters
     */

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
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
        /*
         * Expect a LSID and pull out domain "part".
         * E.g. urn:lsid:biodiversity.org.au:afd.taxon:00cd8be7-c939-49af-907a-52d267099d31
         * to afd
         */
        String[] parts = source.split(":");
        if (parts.length > 3) {
            this.source = parts[3];
        } else {
            this.source = source;
        }
    }

    public List<String> getChildTaxa() {
        return childTaxa;
    }

    public void setChildTaxa(List<String> childTaxa) {
        this.childTaxa = childTaxa;
    }

    public List<String> getParentTaxa() {
        return parentTaxa;
    }

    public void setParentTaxa(List<String> parentTaxa) {
        this.parentTaxa = parentTaxa;
    }

    public String getTaxonNameGuid() {
        return taxonNameGuid;
    }

    public void setTaxonNameGuid(String taxonNameGuid) {
        this.taxonNameGuid = taxonNameGuid;
    }

}