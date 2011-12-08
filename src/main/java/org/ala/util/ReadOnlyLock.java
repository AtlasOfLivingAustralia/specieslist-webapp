package org.ala.util;

import org.apache.log4j.Logger;

public class ReadOnlyLock {
	private static ReadOnlyLock instance = null;
	private static boolean isReadOnly = false;
	private static Object owner = null;
	
	private final static Logger logger = Logger.getLogger(ReadOnlyLock.class);
	
	private ReadOnlyLock(){}
	
	public static ReadOnlyLock getInstance(){
		if(instance == null){
			instance = new ReadOnlyLock();
		}
		return instance;
	}
	
	public boolean isReadOnly() {
		return isReadOnly;
	}

	public boolean setLock(Object ticket) {
		if(!isReadOnly && ticket != null && owner == null){
			owner = ticket;
			isReadOnly = true;
		}
		logger.debug("after setLock - isReadOnly: " + isReadOnly + " , owner: " + owner + " , caller: " + ticket);
		return isReadOnly;
	}
	
	public boolean setUnlock(Object ticket) {
		if(isReadOnly && ticket != null && owner != null && owner == ticket){
			owner = null;
			isReadOnly = false;
		}
		logger.debug("after setLock - isReadOnly: " + isReadOnly + " , owner: " + owner + " , caller: " + ticket);
		return isReadOnly;
	}
	
	public boolean forceUnlock(String password){
		boolean ok = false;
		if("P@33w0rd".equals(password)){
			owner = null;
			isReadOnly = false;
		}
		return ok;
	}
}
