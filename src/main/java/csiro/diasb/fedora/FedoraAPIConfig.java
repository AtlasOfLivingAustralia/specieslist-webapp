/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: oak021 $
  * @version $Id: FedoraAPIConfig.java 849 2009-07-06 02:12:40Z oak021 $
  */


package csiro.diasb.fedora;

/**
 * Interface for accessing current Fedora configuration parameters
 * @author fri096
 */
public class FedoraAPIConfig {
  private String userName = "fedoraAdmin";
  private String userPass = "fedoraAdmin";
  private String host = "localhost";
  private int port = 8080;
  private String pathM = "/fedora/wsdl?api=API-M";
  private String pathA = "/fedora/wsdl?api=API-A";
  private String RISearchPath = "/fedora/risearch";
  private String SOLRSearchPath = "/solr";
  private int JMSPort = 61616;

    public String getSOLRSearchPath() {
        return SOLRSearchPath;
    }

    public void setSOLRSearchPath(String SOLRSearchPath) {
        this.SOLRSearchPath = SOLRSearchPath;
    }

    /**
     * returns the Java Messaging Service (JMS) port number
     * @return the port number
    */
	public int getJMSPort() {
        return JMSPort;
    }
    /**
     * Sets the Java Messaging Service (JMS) port number
     * @param JMSPort
     */
    public void setJMSPort(int JMSPort) {
        this.JMSPort = JMSPort;
    }
    /**
    * The Fedora Resource Index search path
    * @return the search path
    */
    public String getRISearchPath() {
        return RISearchPath;
    }
   /**
   * sets the Fedora Resource Index Search path
   * @param RISearchPath
   */
    public void setRISearchPath(String RISearchPath) {
        this.RISearchPath = RISearchPath;
    }

   /**
	 * @return the FedoraAPI userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userPass
	 */
	public String getUserPass() {
		return userPass;
	}

	/**
	 * @param userPass the userPass to set
	 */
	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the pathM
	 */
	public String getPathM() {
		return pathM;
	}

	/**
	 * @param pathM the pathM to set
	 */
	public void setPathM(String pathM) {
		this.pathM = pathM;
	}

	/**
	 * @return the pathA
	 */
	public String getPathA() {
		return pathA;
	}

	/**
	 * @param pathA the pathA to set
	 */
	public void setPathA(String pathA) {
		this.pathA = pathA;
	}

}
