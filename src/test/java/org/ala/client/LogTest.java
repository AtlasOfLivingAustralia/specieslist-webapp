package org.ala.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.ala.client.appender.RestLevel;
import org.ala.client.appender.RestfulAppender;
import org.ala.client.model.LogEventType;
import org.ala.client.model.LogEventVO;
import org.ala.client.util.RestfulClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

public class LogTest {
	private static Logger logger = Logger.getLogger(LogTest.class);
	
	public LogTest(){
		super();
	}	
			
	/**
	 * URL: http://152.83.198.112:8080/ala-logger/service/logger/
	 * METHOD: "POST"
	 * 
	 * Expected json input format:
	 * {
   	 * "eventTypeId": 12345,
     * "comment": "For doing some research with..",
     * "userEmail" : "David.Martin@csiro.au",
     * "userIP": "123.123.123.123",
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
    	System.out.println(Calendar.getInstance().get(Calendar.YEAR));
  	    	
    	long start = System.currentTimeMillis();
    	for(int i = 0; i < 10; i++){
	    	sb.append("{\"eventTypeId\": 2,");
	    	sb.append("\"comment\": \"For doing some research with..\",");
	    	sb.append("\"userEmail\" : \"waiman.mok@csiro.au\",");
	    	sb.append("\"userIP\" : \"123.11.01.112\",");
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
	    	LogEventVO vo = new LogEventVO(LogEventType.DEBUG, "waiman.mok@csiro.au", "For doing some research with", "127.0.1.1", recordCounts);
	    	logger.log(RestLevel.REMOTE, vo);
    	}
    	System.out.println("time taken: " + (System.currentTimeMillis() - start)/1000);
    	LogManager.shutdown();
/*    	
	   	 RestfulClient restfulClient = new RestfulClient();
	   	 for(int i = 0; i < 10; i++){
			 Object[] ar;
			try {
				ar = restfulClient.restGet("http://logger.ala.org.au/ala-logger-service/service/logger/get.json?q=dp123&year=2010&eventTypeId=12345");
				System.out.println("Status Code: " + ar[0] + ", jsonContent: " + ar[1]);
				
				ar = restfulClient.restPost("http://collections.ala.org.au/lookup/citation", "['co123','in23','dp45']");
				System.out.println("Status Code: " + ar[0] + ", jsonContent: " + ar[1]);
				
		        ObjectMapper deserMapper = new ObjectMapper();
		        deserMapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		        String[] rootNode = deserMapper.readValue((String)ar[1], String[].class);
		        for(int j = 0; j < rootNode.length; j++){
		        	System.out.println("citations: " + rootNode[j]);
		        }
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			 
	   	 }
	   	 */
	   	 
	}
}
