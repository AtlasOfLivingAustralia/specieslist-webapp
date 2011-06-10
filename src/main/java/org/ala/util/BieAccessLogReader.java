/**************************************************************************
 *  Copyright (C) 2011 Atlas of Living Australia
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

package org.ala.util;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.text.*;

import javax.inject.Inject;

import org.ala.client.model.LogEventType;
import org.ala.client.model.LogEventVO;
import org.ala.client.util.RestfulClient;
import org.ala.dao.InfoSourceDAO;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Upload apache bie-access-log image data into logger service.
 * 
 * @author mok011
 *
 */

@Component
public class BieAccessLogReader {
    
	@Inject
	protected InfoSourceDAO infoSourceDao;
	protected Map<String, String> uidInfosourceIDMap = null;
	private RestfulClient restfulClient = null;
	private ObjectMapper serMapper = null;
	private DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss Z");
	private long ctr = 0;
	private Map<String, Integer> recordCounts = new Hashtable<String, Integer>();
	
	protected void processLine(String aLine, String url) throws Exception {	
		String month = "";
		ctr++;
		if(aLine != null && aLine.indexOf('[') + 1 < aLine.indexOf(']')){
			String timestamp = aLine.substring(aLine.indexOf('[') + 1, aLine.indexOf(']'));
			Date date = (Date)formatter.parse(timestamp);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int mth = cal.get(Calendar.MONTH) + 1;
	    	month = cal.get(Calendar.YEAR) + "" + (mth > 9?""+mth: "0"+mth) + "" + (cal.get(Calendar.DATE) > 9? "" + cal.get(Calendar.DATE): "0" + cal.get(Calendar.DATE)); 
		}
		
		if(aLine != null && aLine.contains("GET /repo/")){
        	String tmp = aLine.substring(aLine.indexOf("GET /repo/") + "GET /repo/".length());
        	String repo = tmp.substring(0, tmp.indexOf('/'));
        	
        	addImageSourceMap(repo);        	
		}
		else{
			if(!recordCounts.isEmpty()){
				//send message to logger service
        		sendLoggerMessage(url, month);
		        recordCounts.clear();
			}
		}
	}

	private int sendLoggerMessage(String url, String month) throws Exception{
		Integer statusCode = -1;
        LogEventVO vo = new LogEventVO(LogEventType.IMAGE_VIEWED, "", "apache bie-access-log", "127.0.0.1", month, recordCounts);
        String json = serMapper.writeValueAsString(vo);
        Object[] array = restfulClient.restPost(url, json);
        if(array != null && array.length > 0){
    		statusCode = (Integer)array[0];    		
    	}
        
        if(ctr%100 == 0){
        	System.out.println("****** line ctr :" + ctr + " , statusCode: " + statusCode + " , json:" + json);
        }
        return statusCode;
	}
	
	public final void processFile(String aFileName, String url) throws FileNotFoundException{
		Scanner scanner = new Scanner(new File(aFileName));
		try {
			if(uidInfosourceIDMap == null){
				uidInfosourceIDMap = infoSourceDao.getInfosourceIdUidMap();
			}
			if(restfulClient == null){
				restfulClient = new RestfulClient();
			}
			if(serMapper == null){
				serMapper = new ObjectMapper();
		        serMapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
			}
			// first use a Scanner to get each line
			while (scanner.hasNextLine()) {
				try {
					processLine(scanner.nextLine(), url);
				} catch (Exception e) {
					//do nothing
					e.printStackTrace();
				}
			}
		} finally {
			// ensure the underlying stream is always closed
			// this only has any effect if the item passed to the Scanner
			// constructor implements Closeable (which it does in this case).
			try {
				//check & clean recordCounts.
				processLine("", url);
				System.out.println("****** PROCESS END. line ctr :" + ctr);
			} catch (Exception e) {
				//do nothing
			}
			scanner.close();
			restfulClient = null;
		}
	}

    private Map<String, Integer> createImageSourceMap(String repoString){
    	Map<String, Integer> recordCounts = new Hashtable<String, Integer>();
    	if(repoString != null && uidInfosourceIDMap.get(repoString) != null && uidInfosourceIDMap.get(repoString).length() > 0){
    		recordCounts.put(uidInfosourceIDMap.get(repoString), 1);
    	}
    	return recordCounts;
    }

    private void addImageSourceMap(String repoString){
    	String key = uidInfosourceIDMap.get(repoString);
    	if(repoString != null && key != null && key.length() > 0){
    		if(recordCounts.containsKey(key)){
    			recordCounts.put(key, recordCounts.get(key) + 1);
    		}
    		else{
    			recordCounts.put(key, 1);
    		}
    	}    	
    }

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("usage: java BieAccessLogReader"	+ " fileLocation" + " url");
			System.exit(0);
		}
		try{
			ApplicationContext context = SpringUtils.getContext();
			BieAccessLogReader bieAccessLogReader = context.getBean(BieAccessLogReader.class);	
			bieAccessLogReader.processFile(args[0], args[1]);
			System.exit(1);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
}
