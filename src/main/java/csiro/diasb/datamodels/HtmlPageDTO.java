/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package csiro.diasb.datamodels;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DTO bean to represent the FC HTML Page objects
 *
 * @author dos009
 */
public class HtmlPageDTO {
    /* Fields */
    String pid;
    String guid;
    String title;
    String url;
    String scientificName;
    String source;
    Map<String, String> rdfProperties = new HashMap<String, String>();

    /**
     * No args constructor
     */
    public HtmlPageDTO() {}

    /**
     * Constructor for setting just PID field
     *
     * @param pid
     */
    public HtmlPageDTO(String pid) {
        this.pid = pid;
    }

    /**
     * Constructor for setting 3 fields
     *
     * @param pid
     * @param title
     * @param guid 
     */
    public HtmlPageDTO(String pid, String title, String guid) {
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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPid() {
        return pid;
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

    public Map<String, String> getRdfProperties() {
        return rdfProperties;
    }

    public void setRdfProperties(Map<String, String> rdfProperties) {
        this.rdfProperties = rdfProperties;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        // if a URL, pull out just the host portion
    	if(source!=null){
	        URI uri;
	        try {
	            uri = new URI(source);
	            String host = uri.getHost();
	            if (!host.isEmpty()) 
	            	source = host;
	        } catch (URISyntaxException ex) {
	            Logger.getLogger(HtmlPageDTO.class.getName()).log(Level.WARNING, null, ex);
	        }
    	}
        this.source = source;
    }
}