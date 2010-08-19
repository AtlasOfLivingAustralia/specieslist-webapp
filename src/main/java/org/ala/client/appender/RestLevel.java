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

import org.apache.log4j.Level;

/**
 * Custom Level for Log4J (higher than FATAL level)
 * 
 * @author MOK011
 *
 */
public class RestLevel extends Level {
	private static final long serialVersionUID = -3158526197894230134L;
	
	//representing my log level
	public static final RestLevel REMOTE = new RestLevel(60000, "REMOTE", 0);
	
	/**
	 * Constructor
	 * 
	 * @param level
	 * @param levelStr
	 * @param syslogEquivalent
	 */
	public RestLevel(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}
	
	/** 
	* 
	* @see Level#toLevel(int, org.apache.log4j.Level) 
	*/ 
	public static RestLevel toLevel(int val, Level defaultLevel) {
		return REMOTE;
	}
	
	/** 
	*
	* @see Level#toLevel(java.lang.String, org.apache.log4j.Level) 
	*/  
	public static RestLevel toLevel(String sArg, Level defaultLevel) {
		return REMOTE;
	}
}
