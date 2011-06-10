/**************************************************************************
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

package org.ala.client.model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * VO for JSON
 * 
 * @author MOK011
 *
 */

public class LogEventVO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String comment = "";

	private int eventTypeId = 0;

	private String userIP = "";
	
	private Map<String, Integer> recordCounts = new Hashtable<String, Integer>();

	private String userEmail = "";
	
	private String month = "";
	
	public LogEventVO() {
    }

    public LogEventVO(LogEventType eventType, String userEmail, String comment, String userIP, Map<String, Integer> recordCounts) {
    	this(eventType.getId(), userEmail, comment, userIP, null, recordCounts);
    }
    
    public LogEventVO(LogEventType eventType, String userEmail, String comment, String userIP, String month, Map<String, Integer> recordCounts) {
    	this(eventType.getId(), userEmail, comment, userIP, month, recordCounts);
    }
    
    private LogEventVO(int eventTypeId, String userEmail, String comment, String userIP, String month, Map<String, Integer> recordCounts) {
    	this.eventTypeId = eventTypeId;
    	if(userEmail != null){
    		this.userEmail = userEmail;
    	}
    	if(comment != null){
    		this.comment = comment;
    	}
    	if(userIP != null){
    		this.userIP = userIP;
    	}
    	if(month != null){
    		this.month = month;
    	}
    	if(recordCounts != null){
    		this.recordCounts = recordCounts;
    	}    	
    }
    
	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getEventTypeId() {
		return this.eventTypeId;
	}

	public void setEventTypeId(int eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public Map<String, Integer> getRecordCounts() {
		return this.recordCounts;
	}

	public void setRecordCount(Map<String, Integer> recordCounts) {
		this.recordCounts = recordCounts;
	}

	public String getUserEmail() {
		return this.userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}


	public String getUserIP() {
		return this.userIP;
	}

	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}
	
    public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

    /**
     * To-string method.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
	
	/*
	private String userId;

	private int month;

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getMonth() {
		return this.month;
	}

	public void setMonth(int month) {
		this.month = month;
	}	
*/	
}