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

package org.ala.client.util;

import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

/**
 * Restful Web Service Client.
 * 
 * @author MOK011
 *
 */
public class RestfulClient {
	private static MultiThreadedHttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
	private static Logger logger = Logger.getLogger(RestfulClient.class);
	private static final String JSON_MIME_TYPE = "application/json";
	private static final String ENCODE_TYPE = "utf-8";
	
	private HttpClient client;
	
	//client connection timeout.
	public int timeout;
	
	public RestfulClient(){
		this(5000);
	}

	public RestfulClient(int timeout){
		super();
		this.timeout = timeout;
        //create the client to call the logger REST api
        client = new HttpClient(connManager);
        //set connection timeout
        client.getParams().setSoTimeout(timeout);		
	}
	
	/**
	 * Makes a POST request to the specified URL and passes the provided JSON Object
	 * 
	 * @param url URL Endpoint
	 * @param jsonRequestBody JSON Object to post to URL
	 * @return [0]: status code; [1]: a JSON encoded response
	 */
	public Object[] restPost(String url, String jsonRequestBody){
		PostMethod post = null;
		String resp = null;
		int statusCode = 0;
		
        try {        	
	        post = new PostMethod(url);
	        RequestEntity entity = new StringRequestEntity(jsonRequestBody, JSON_MIME_TYPE, ENCODE_TYPE); 
	        post.setRequestEntity(entity); 
        
        	statusCode = client.executeMethod(post);
        	if(statusCode != HttpStatus.SC_OK){
        		resp = post.getResponseBodyAsString();
        	}
        } 
        catch(SocketTimeoutException se){
        	statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
        	logger.error("Could not send message!!, jsonRequestBody: " + jsonRequestBody, se);
        }
        catch(Exception e) {
        	statusCode = HttpStatus.SC_NOT_ACCEPTABLE;
        	logger.error("Could not send message!!, jsonRequestBody: " + jsonRequestBody, e);
        } finally {
        	if(post != null){
        		post.releaseConnection();
        	}
        }
    	Object[] o = new Object[]{statusCode, resp};        	      
        return o;		
	}
	
	/**
	 * Makes a GET request to the specified url and returns a JSON Object.
	 * 
	 * @param url URL Endpoint with request parameters.
	 * @return [0]: status code; [1]: a JSON encoded response.
	 */
	public Object[] restGet(String url){
		GetMethod get = null;
		String resp = "";
		int statusCode = 0;
		
        try {        	
        	get = new GetMethod(url);        
        	statusCode = client.executeMethod(get);
        	resp = get.getResponseBodyAsString();
        } 
        catch(SocketTimeoutException se){
        	statusCode = HttpStatus.SC_REQUEST_TIMEOUT;
        	logger.error("Could not send message!!, url: " + url, se);
        }
        catch(Exception e) {
        	statusCode = HttpStatus.SC_NOT_ACCEPTABLE;
        	logger.error("Could not send message!!, url: " + url, e);
        } finally {
        	if(get != null){
        		get.releaseConnection();
        	}
        }
    	Object[] o = new Object[]{statusCode, resp};        	
        return o;		
	}	
}
