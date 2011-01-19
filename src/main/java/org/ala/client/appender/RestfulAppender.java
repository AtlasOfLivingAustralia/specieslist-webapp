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

package org.ala.client.appender;

import org.ala.client.model.LogEventVO;
import org.ala.client.util.RestfulClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Log4J appender for JSON based REST Web Service.
 * 
 * @author MOK011
 *
 */
public class RestfulAppender extends AppenderSkeleton {
	
	private String urlTemplate;
	private String username;
	private String password;
	private int timeout;

	private ObjectMapper serMapper;
	private ObjectMapper deserMapper;
	private RestfulClient restfulClient;
	
	public RestfulAppender(){
		super();
		restfulClient = new RestfulClient(timeout);
		        
        serMapper = new ObjectMapper();
        serMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        deserMapper = new ObjectMapper();
        deserMapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public void setUrlTemplate(String urlTemplate) {
		this.urlTemplate = urlTemplate;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	protected void append(LoggingEvent event) {
		if (!checkEntryConditions()) {
			return;
		}

		if (!isAsSevereAsThreshold(event.getLevel())){
			return;
		}
		sendRestRequest(event);
	}

	private boolean checkEntryConditions() {
		if (urlTemplate == null) {
			LogLog.error("No 'urlTemplate' for [" + name + "]");
			return false;						
		}				
		return true;
	}
	
	
	private int sendRestRequest(LoggingEvent event) {
		PostMethod post = null;
		int statusCode = 0;
		String message = null;
		LogEventVO vo = null;
		
        try {
        	Object object = event.getMessage();
        	if(object instanceof LogEventVO){       		
        		//convert to JSON
        		message = serMapper.writeValueAsString(object); 
        	}
        	else if(event.getMessage() instanceof String){
        		message = (String)object;
        		//validate json string
        		vo = deserMapper.readValue(message, LogEventVO.class);        		
        	}
        	
        	if(restfulClient == null){
        		restfulClient = new RestfulClient(timeout);
        	}
        	Object[] array = restfulClient.restPost(urlTemplate, message);
        	if(array != null && array.length > 0){
        		statusCode = (Integer)array[0];
        	}
        } 
        catch(Exception e) {
        	statusCode = HttpStatus.SC_NOT_ACCEPTABLE;
	        LogLog.error("Could not send message from RestfulAppender [" + name + "],\nMessage: " + event.getMessage(), e);
        } finally {
        	vo = null; //waiting for gc.
        	if(post != null){
        		post.releaseConnection();
        	}
        }
        return statusCode;
	}	

	public void close() {
		restfulClient = null;
		this.close();
	}

	public boolean requiresLayout() {
		return false;
	}
	
}
