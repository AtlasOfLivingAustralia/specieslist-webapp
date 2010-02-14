/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package org.ala.bie.web.dto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DTO bean to represent the FC Image objects
 *
 * @author dos009
 */
public class ImageDTO {
    /* Fields */
    String pid;
    String guid;
    String title;
    String description;
    String country;
    String region;
    String scientificName;
    String latitude;
    String longitude;
    String photoPage;
    String photoSourceUrl;
    String source;

    /**
     * No args constructor
     */
    public ImageDTO() {}

    /**
     * Constructor for setting just PID field
     *
     * @param pid
     */
    public ImageDTO(String pid) {
        this.pid = pid;
    }

    /**
     * Constructor for setting 3 fields
     *
     * @param pid
     * @param title
     * @param guid
     */
    public ImageDTO(String pid, String title, String guid) {
        this.pid = pid;
        this.title = title;
        this.guid = guid;
    }

    /**
     * Custom toString method
     *
     * @return string representation (summary) of this object
     */
    @Override
    public String toString() {
        return "pid: "+pid+"; guid: "+guid+"; title: "+title;
    }

    /*
     * Getters & Setters
     */

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPhotoPage() {
        return photoPage;
    }

    public void setPhotoPage(String photoPage) {
        this.photoPage = photoPage;
    }

    public String getPhotoSourceUrl() {
        return photoSourceUrl;
    }

    public void setPhotoSourceUrl(String photoSourceUrl) {
        this.photoSourceUrl = photoSourceUrl;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        // if a URL, pul out just the host portion
        URI uri;
        try {
            uri = new URI(source);
            String host = uri.getHost();
            if (!host.isEmpty()) source = host;
        } catch (URISyntaxException ex) {
            Logger.getLogger(HtmlPageDTO.class.getName()).log(Level.WARNING, null, ex);
        }

        this.source = source;
    }
}