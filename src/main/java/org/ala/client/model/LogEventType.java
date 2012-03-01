package org.ala.client.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


public enum LogEventType {
	OCCURRENCE_RECORDS_VIEWED(1000, "OCCURRENCE_RECORDS_VIEWED"),
	OCCURRENCE_RECORDS_VIEWED_ON_MAP(1001, "OCCURRENCE_RECORDS_VIEWED_ON_MAP"),
	OCCURRENCE_RECORDS_DOWNLOADED(1002, "OCCURRENCE_RECORDS_DOWNLOADED"),
	IMAGE_VIEWED(2000, "IMAGE_VIEWED");
	
	private int id;
	private String name;

    private static final Map<String, LogEventType> logEventTypeLookup = new HashMap<String, LogEventType>();
    private static final Map<Integer, LogEventType> logEventTypeIdLookup = new HashMap<Integer, LogEventType>();
   
    static {
         for (LogEventType mt : EnumSet.allOf(LogEventType.class)) {
        	 logEventTypeLookup.put(mt.getName().trim().toUpperCase(), mt);
        	 logEventTypeIdLookup.put(mt.getId(), mt);
         }
    }
	
    private LogEventType(int id, String name){
    	this.name = name;
    	this.id = id;
    } 
    
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
    
    public static LogEventType getLogEventType(String name) {
        return logEventTypeLookup.get(name.toUpperCase());
    }

    public static LogEventType getLogEventType(int id) {
        return logEventTypeIdLookup.get(id);
    }
    
    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
	public String getInsertSql() {
		return String.format("INSERT INTO log_event_type (id, rkey, name) VALUES (%d, %s)", this.id, this.name);
	}    
}
