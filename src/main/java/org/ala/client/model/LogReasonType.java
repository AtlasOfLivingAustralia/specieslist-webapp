package org.ala.client.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


public enum LogReasonType {
	DOWNLOAD_REASON_CONSERVATION(0, "logger.download.reason.conservation", "conservation management/planning"),
	DOWNLOAD_REASON_BIOSECURITY(1, "logger.download.reason.biosecurity", "biosecurity management, planning"),
	DOWNLOAD_REASON_ENVIROMENTAL(2, "logger.download.reason.environmental", "environmental impact, site assessment"),
	DOWNLOAD_REASON_EDUCATION(3, "logger.download.reason.education", "education"),
	DOWNLOAD_REASON_RESEARCH(4, "logger.download.reason.research", "scientific research"),
	DOWNLOAD_REASON_COLLECTION_MGMT(5, "logger.download.reason.collection.mgmt", "collection management"),	
	DOWNLOAD_REASON_OTHER(6, "logger.download.reason.other", "other");
	
	private int id;
	private String key;
	private String name;

    private static final Map<String, LogReasonType> logReasonTypeLookup = new HashMap<String, LogReasonType>();
    private static final Map<Integer, LogReasonType> logReasonTypeIdLookup = new HashMap<Integer, LogReasonType>();
   
    static {
         for (LogReasonType mt : EnumSet.allOf(LogReasonType.class)) {
        	 logReasonTypeLookup.put(mt.getName().trim().toUpperCase(), mt);
        	 logReasonTypeIdLookup.put(mt.getId(), mt);
         }
    }
	
    private LogReasonType(int id, String key, String name){
    	this.name = name;
    	this.id = id;
    	this.key = key;
    } 
    
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
 
	public String getKey() {
		return key;
	}
	
	public String getInsertSql() {
		return String.format("INSERT INTO log_reason_type (id, rkey, name) VALUES (%d, %s, %s)", this.id, this.key, this.name);
	}
	
    public static LogReasonType getLogReasonType(String name) {
        return logReasonTypeLookup.get(name.toUpperCase());
    }

    public static LogReasonType getLogReasonType(int id) {
        return logReasonTypeIdLookup.get(id);
    }
    
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }		
}
