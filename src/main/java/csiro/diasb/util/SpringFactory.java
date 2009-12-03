/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: hwa002 $
  * @version $Id: SpringFactory.java 311 2009-04-21 03:59:15Z hwa002 $
  */


package csiro.diasb.util;

import java.net.*;
import java.util.*;

import org.apache.log4j.*;
import org.springframework.context.*;
import org.springframework.context.support.*;

/**
 * Generic class that can load a spring context depending on hostname.
 * @author fri096
 */
public class SpringFactory {
	/**
	 * The logger for this class.
	 */
	static private Logger logger = Logger.getLogger(SpringFactory.class);

	/**
	 * Utility method to query all hostnames this machine is known under.
   *
   * Currently ignores IPv6 addresses and use IPv4 address.  The
   * colon sign <code>:</code> is causing problems with
   * parsing.  <b>NOTE: </b> The DNS hostname associated with the IPv6
   * address is <i>not</i> ignored, just the raw IPv6 address.
	 * 
	 * @return All hostnames this machine is known under.
	 */
	static private Collection<String> getHostNames() {
			HashSet<String> hostNames = new HashSet<String>();
			try {
				Enumeration<NetworkInterface> enu = NetworkInterface
						.getNetworkInterfaces();
				while (enu.hasMoreElements()) {
					NetworkInterface ni =  enu.nextElement();
					Enumeration<InetAddress> iaenum = ni.getInetAddresses();
					while (iaenum.hasMoreElements()) {
					InetAddress ia = iaenum.nextElement();
					if (!ia.getCanonicalHostName().contains(":"))
						hostNames.add(ia.getCanonicalHostName().toUpperCase());
					if (!ia.getHostAddress().contains(":"))
						hostNames.add(ia.getHostAddress().toUpperCase());
					if (!ia.getHostName().contains(":"))
						hostNames.add(ia.getHostName().toUpperCase());
				}
				}
			} catch (SocketException e1) {
				logger.error("Error getting hostnames"); //$NON-NLS-1$
			}

			for (String name : hostNames) {
				logger.info("Hostname: " + name);
			}

			return hostNames;
	}

	/**
	 * The Spring application context.
	 */
	private ApplicationContext context;
	
	/**
	 * @return the context
	 */
	public ApplicationContext getContext() {
		if(context==null) {
			initContext();
		}
		return context;
	}

	/**
	 * Loads the Spring application context.
	 */
	private void initContext() {

		Collection<String> hostnames = getHostNames();
		ArrayList<String> locList = new ArrayList<String>();
		locList.add("config/default/ingesterContext.xml");

    // 2009-04-17 hwa002
    // Has some problems with IPv6 addresses, ignoring for now.
		for (String hostname : hostnames) {
		  locList.add("config/**/"+hostname+"/ingesterContext.xml");
		}
		context = new ClassPathXmlApplicationContext(locList.toArray(new String[0]));
	}
}
