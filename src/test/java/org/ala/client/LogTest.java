package org.ala.client;

import java.util.Hashtable;
import java.util.Map;

import org.ala.client.appender.RestLevel;
import org.ala.client.model.LogEventVO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LogTest {
	private static Logger logger = Logger.getLogger(LogTest.class);

	/**
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/
	 * METHOD: "POST"
	 * 
	 * Expected json input format:
	 * {
   	 * "eventTypeId": 12345,
     * "comment": "For doing some research with..",
     * "userEmail" : "David.Martin@csiro.au",
     * "recordCounts" : {
     * "dp123": 32,
     * "dr143": 22,
     *  "ins322": 55
   	 * }
	 * }
	 * 
	*/
	public static void main(String[] args) {
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append("{\"eventTypeId\": 123,");
    	sb.append("\"comment\": \"For doing some research with..\",");
    	sb.append("\"userEmail\" : \"waiman.mok@csiro.au\",");
    	sb.append("\"recordCounts\" : {");
    	sb.append("\"dp123\": 32,");
    	sb.append("\"dr143\": 22,");
    	sb.append("\"ins322\": 55 } }");
    	logger.debug(sb.toString());
    	logger.warn(sb.toString());
    	logger.info(sb.toString());
    	
    	//log to remote ala-logger
    	logger.log(RestLevel.REMOTE, sb.toString());
    	        	
    	Map<String, Integer> recordCounts = new Hashtable<String, Integer>();
    	// entityUid, record_count
    	recordCounts.put("dp123", 32);
    	recordCounts.put("dr143", 22);
    	recordCounts.put("ins322", 55);
    	LogEventVO vo = new LogEventVO(123, "waiman.mok@csiro.au", "For doing some research with", recordCounts);
    	logger.log(RestLevel.REMOTE, vo);
    	    	
    	LogManager.shutdown();

    	return;
		
	}

}
