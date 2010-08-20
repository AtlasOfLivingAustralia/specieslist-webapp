package org.ala.client;

import org.ala.client.appender.RestLevel;
import org.junit.Test;
import org.apache.log4j.*;

public class AppenderTest {
	private static Logger logger = Logger.getLogger(AppenderTest.class);
    

     @Test
    public void testLogger(){
    	StringBuffer sb = new StringBuffer();
    	for(int i = 0; i < 1; i++){
	    	sb.append("{\"eventTypeId\": 123,");
	    	sb.append("\"comment\": \"For doing some research with..\",");
	    	sb.append("\"userEmail\" : \"waiman.mok@csiro.au\",");
	    	sb.append("\"userIP\" : \"123.11.01.112\",");
	    	sb.append("\"recordCounts\" : {");
	    	sb.append("\"dp123\": 32,");
	    	sb.append("\"dr143\": 22,");
	    	sb.append("\"ins322\": 55 } }");
	//    	logger.debug(sb.toString());
	//    	logger.warn(sb.toString());
	//    	logger.info(sb.toString());
	    	
	    	//log to remote ala-logger
	    	logger.log(RestLevel.REMOTE, sb.toString());
    	}
    	LogManager.shutdown();

    	return;
    }    
}

