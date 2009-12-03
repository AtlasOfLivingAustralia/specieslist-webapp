/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: jia020 $
  * @version $Id: FedoraAPI.java 807 2009-06-26 10:00:00Z hwa002 $
  */

package csiro.diasb.fedora;

import csiro.diasb.datamodels.SearchResult;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.rpc.*;

import org.apache.axis.types.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.util.*;
import org.apache.log4j.*;

import fedora.client.*;
import fedora.server.access.*;
import fedora.server.management.*;
import fedora.server.types.gen.*;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

/**
 * Class that wraps a number of Fedora Commons web service related
 * functionalities.
 *
 * @author jia020
 *
 * @since v0.1
 */
public class FedoraAPI {

 /**
	 * The logger for this class.
   *
   * Using fully qualified name to indicate that this is a Apache Log 4 J
   * Logger, as opposed to JRE's built-in Logger.
   *
	 */
	private static final org.apache.log4j.Logger logger =
    Logger.getLogger(FedoraAPI.class.getName());

  // Default amount of time for operations to back off before retrying.
  // An example is to wait for Fedora Commons' Locked Object Exception to get
  // resolved.
  // Individual methods DO NOT need to confine to this time amount; they
  // should make note of changes.
  // Unit of time is in milliseconds.
  private final int DEFAULT_BACKOFF_TIME = 500;

  // Maximum amount of times an operation should retry before aborting.
  private int maxOpRetries = 10;

  /**
   * Holds parameters that might change from installation to installation.
   * Set by Spring framework.
   */
  private FedoraAPIConfig fedoraAPIConfig;

  // Reference to Fedora Commons' client side .jar file.
  // Being used as a proxy to communicate with Fedora Commons itself, rather
  // than re-generating stub files from WSDL.
	private FedoraClient fedoraClient=null;

    CommonsHttpSolrServer solrServer = null;
    FacetQuery currentFacetQuery = null;

	/**
   * @return the fedoraClient
	 * @throws IOException 
   */
  public FedoraClient getFedoraClient() throws IOException {
    if(fedoraClient==null) {
      init();
    }
    return fedoraClient;
  }

  /**
   * Initialisation.  Setups the various references to web services client
   * objects to communicate with Fedora Commons.
   *
   * @throws {@link java.io.IOException} On error.  See wrapped Exception
   * for details.
   *
   * @since v0.1
   */
  public void init() throws IOException {
    try {
      
      // Creates the URL to Fedora Commons
      String m_baseURL = "http://" +
                         getFedoraAPIConfig().getHost() + ":" +
                         getFedoraAPIConfig().getPort() +
                         "/fedora/";

      FedoraAPI.logger.info("Initialising web service client to connect " +
        "to Fedora Commons located at " +
        this.getFedoraAPIConfig().getHost() + ":" +
        this.getFedoraAPIConfig().getPort() + "\n");

      // Instantiates a new reference to Fedora Commons' client .jar file.
      // This will be used to communicate with Fedora Commons.
      fedoraClient = new FedoraClient(m_baseURL,
                                      getFedoraAPIConfig().getUserName(),
                                      getFedoraAPIConfig().getUserPass());


    } catch (MalformedURLException ex) {

      FedoraAPI.logger.error("Failed to create connection to Fedora Commons. \n" +
        ex.getMessage(), ex);

      throw new IOException("Failed to create connection to Fedora Commons. \n" +
        ex.getMessage(), ex);
    }
    
  } // End of `FedoraAPI.init` method.

public CommonsHttpSolrServer initSolrServer() {
    try
    {
        if (solrServer==null)
        {
          String baseURL = "http://" + getFedoraAPIConfig().getHost() + ":" +
          getFedoraAPIConfig().getPort() + getFedoraAPIConfig().getSOLRSearchPath();
          solrServer = new CommonsHttpSolrServer(baseURL);

        }
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
    finally
    {
        return solrServer;
    }
 }

  /**
   * A helper class to simulate a file upload without actually having a file.
   * @author fri096
   */
  public class FakeFilePartSource implements PartSource {

    /**
     * The content as a byte array.
     */
    private byte[] content;

    /**
     * Creates a new PartSource for content <code>content</code>.
     * @param content The content of the PartSoruce.
     * @throws UnsupportedEncodingException
     */
    public FakeFilePartSource(String content) throws UnsupportedEncodingException {
      // this.content=content.getBytes();
      this.content = content.getBytes("UTF-8");
    }

    /**
     * @return An input stream based on the content byte array
     * @throws IOException The if input stream could not be created.
     * @see org.apache.commons.httpclient.methods.multipart.PartSource#createInputStream()
     */
    @Override
    public InputStream createInputStream() throws IOException {
      return new ByteArrayInputStream(content);
    }

    /**
     * @return always the hard-coded string dataStramContent. Seems irrelevant what it actually is.
     * @see org.apache.commons.httpclient.methods.multipart.PartSource#getFileName()
     */
    @Override 
    public String getFileName() {
      return "dataStramContent";
    }

    /**
     * @return the length of the content array
     * @see org.apache.commons.httpclient.methods.multipart.PartSource#getLength()
     */
    @Override
    public long getLength() {
      return content.length;
    }
  }

  /**
   * Returns the Java Messaging Service (JMS) Provider's URL of
   * the Fedora Commons repository.
   *
   * @return The URL of Fedora Commons' JMS Provider service.
   *
   * @since v0.3
   */
  public String getJMSProviderURL() {
    String URL = "tcp://" + getFedoraAPIConfig().getHost() + ":" + getFedoraAPIConfig().getJMSPort();
    return URL;
  } // End of `FedoraAPI.getJMSProviderURL` method.

	/**
   * Sets a reference to {@link csiro.diasb.repository.FedoraAPIConfig}
   * object.  JavaBean method for Spring Framework.
   *
	 * @param fedoraAPIConfig New reference to {@link csiro.diasb.repository.FedoraAPIConfig}
   * object.
   *
   * @since v0.1
	 */
	public void setFedoraAPIConfig(FedoraAPIConfig fedoraAPIConfig) {
		this.fedoraAPIConfig = fedoraAPIConfig;   
	} // End of `FedoraAPI.setFedoraAPIConfig`

/**
 * Constructs singleton instance of the FedoraAPI class
 */
  public FedoraAPI() {
  	logger.info("FedoraAPI created");
  }

/**
 * Gets information that describes the repository.
 * @return String repositoryName - The name of the Repository. Set in fedora.fcfg. Default "Fedora Repository"
 * String repositoryVersion - The version of Fedora running. Fedora 3.0 returns "3.0"
 * String repositoryBaseURL - The repository base url set in fedora.fcfg. Default "http://localhost:8080/fedora"
 * String repositoryPIDNamespace - The prefix to use for newly generated PIDs
 * String defaultExportFormat
 * String OAINamespace - The oai namespace. Default "example.org"
 * String[] adminEmailList - The email to the administrator. Default "bob@example.org" and "sally@example.org". Defined in fedora.fcfg.
 * String samplePID - An example pid, to show how to refer to objects. "doms:100"
 * String sampleOAIIdentifier - An example oai identifier, to show how to refer to records. Example: "oai:example.org:doms:100"
 * String sampleSearchURL - The url to the search service for the repository. Default "http://localhost:8080/fedora/search"
 * String sampleAccessURL - The url to an example object in the repository. Default "http://localhost:8080/fedora/get/demo:5"
 * String sampleOAIURL - The url to an oai record. Default "http://localhost:8080/fedora/oai?verb=Identify"
 * String[] retainPIDs - The list of pid prefixes, that cause the pid to not be autogenerated.
 * @throws IOException
 *
*/
	public RepositoryInfo describeRepository() throws IOException {
		RepositoryInfo result = getPortA().describeRepository();
		logger.info("Result = " + result);
		return result;
	}
	
/**
 * Gets the content of a datastream.
 * 
 * @param pid The PID of the object.
 * @param dsID The datastream ID.
 * @return The contents of the stream
 * @throws IOException
 */
  public String getDataStreamDissemination(String pid, String dsID) throws IOException {
    String dissemination = null;
      String asOfDateTime = "";
      MIMETypedStream result = getPortA().getDatastreamDissemination(pid, dsID, null);
      dissemination = new String(result.getStream(), "UTF-8");
      //System.out.println("Getdatastrean Result = " + dissemination);

      return dissemination;
    
  } // End of `FedoraAPI.getDataStreamDissemination` method.

  /**
   * Replace newlines with the given string.
   */
  private static String replaceNewlines(String in, String replaceWith) {
    return in.replaceAll("\r", replaceWith).replaceAll("\n", replaceWith);
  }

/**
 *
 * @return
 * @throws java.io.IOException
 */
  private String getUploadURL() throws IOException {
  	if(fedoraClient==null) {
  		init();
  	}
		return fedoraClient.getUploadURL();
	}

  /**
   * Send the data from the stream to the server. This is less efficient than
   * <i>upload(File)</i>, but if you already have a stream, it's convenient.
   * This method takes care of temporarily making a File out of the stream,
   * making the request, and removing the temporary file. Having a File source
   * for the upload is necessary because the content-length must be sent along
   * with the request as per the HTTP Multipart POST protocol spec.
   *
   * @since v0.1
   */
  private String uploadStream(String content) throws IOException {
    PostMethod post = null;
    // Create an instance of HttpClient.
    HttpClient client = new HttpClient();

    AuthScope authScope = new AuthScope(getFedoraAPIConfig().getHost(),
        getFedoraAPIConfig().getPort(), AuthScope.ANY_REALM);
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
        getFedoraAPIConfig().getUserName(), getFedoraAPIConfig().getUserPass());

    client.getState().setCredentials(authScope, creds);
    client.getParams().setAuthenticationPreemptive(true);

    // prepare the post method
    post = new PostMethod(getUploadURL());
    post.setDoAuthentication(true);
    post.getParams().setParameter("Connection", "Keep-Alive");

    // chunked encoding is not required by the Fedora server,
    // but makes uploading very large files possible
    post.setContentChunked(true);

    // add the file part
    try {
      Part[] parts = { new FilePart("file", new FakeFilePartSource(content)) };
      // Fedora require MultipartRequestEntity
      MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
//      StringRequestEntity entity = new StringRequestEntity(content, "application/octet-stream", "ISO-8859-1");

      post.setRequestEntity(entity);

      // execute and get the response
      int responseCode = client.executeMethod(post);
      String body = null;
      body = post.getResponseBodyAsString();
      if (body == null) {
        body = "[empty response body]";
      }
      body = body.trim();
      if (responseCode != HttpStatus.SC_CREATED) {
        logger.error("Upload failed: " + responseCode);
        throw new IOException("Upload failed: " + responseCode);
      }

      return replaceNewlines(body, "");
    } catch (IOException ex) {
      logger.error("Upload file failed: " + ex.getMessage(), ex);
      throw ex;
    } finally {
      post.releaseConnection();
    }
  }

///**
//   * Creates a new managed-content Datastream in the object.
//   *
//   * @param pid The PID of the object.
//   * @param dsLabel The label for the datastream. Can be null.
//   * @param dsMIME The mime-type of the datastream. Can be null.
//   * @param stream Location of managed datastream content
//   * @return The datastreamID of the newly added datastream.
//   */
//  public String addDatastream(String pid, String dsLabel, String dsMIME, InputStream stream) throws IOException {
//    if ((pid == null) || (dsLabel == null) || (dsMIME == null) || (stream == null)) {
//      return null;
//    }
//
//    boolean versionable = true;
//    ArrayOfString altIDs = null;
//    String formatURI = "";
//    String dsLocation = null;
//
//    dsLocation = uploadStream(stream);
//
//    String dsControlGroup = "M";
//    String dsState = "A";
//    String logMessage = "added new datastream: HUHU";
//
//    // make the SOAP call on API-M using the connection stub
//    // TODO: Allow users to specify checksum
//    String datastreamID = null;
//    String dsID = dsLabel;
//      datastreamID = getPortM().addDatastream(pid,
//              dsID,
//              altIDs,
//              dsLabel,
//              versionable,
//              dsMIME,
//              formatURI,
//              dsLocation,
//              dsControlGroup,
//              dsState,
//              null,
//              null,
//              logMessage);
//    return datastreamID;
//  }

  /**
   * Creates a new Datastream in a Digital Object.
   *
   * @param pid The <code>PID</code> of the object.
   * @param dsLabel The label for the datastream. Can be <code>null</code>
   * @param dsMIME The mime-type of the datastream. Can be <code>null</code>
   * @param streamContent The content of the new data stream
   * @param storeInline Whther to store the content Inline or as Managed Content.
   * See Fedora Commons' documentation for details.
   * @param createCheckSum whether to create a checksum on the stream.
   *
   * @return The ID of the newly added datastream.
   *
   * @throws {@link java.io.IOException} If a network or repository error
   * occured while adding the data stream.
   *
   * @since v0.2
   */
  public String addDatastream(String pid, String dsLabel, String dsMIME, String streamContent, boolean storeInline, boolean createCheckSum) throws IOException {
    // Validate the inputs.
    if ((pid == null) || (dsLabel == null) || (dsMIME == null) || (streamContent == null)) {
      throw new IllegalArgumentException("pid, dsLabel, dsMIME, and streamContent must all be non-null!");
    }

    if(storeInline&&createCheckSum) {
      throw new IllegalArgumentException("Can't use storeInline and createChecksum at the same time due to a Fedora bug!");
    }
    
    boolean versionable = true;
    String[] altIDs = null;
    String formatURI = "";
    String dsLocation = null;

    dsLocation = uploadStream(streamContent);

    String dsControlGroup = storeInline ? "X" : "M";
    String dsState = "A";
    String logMessage = "added new datastream: "+pid+"/"+dsLabel;

    // make the SOAP call on API-M using the connection stub
    // TODO: Allow users to specify checksum
    String datastreamID = null;
    String dsID = dsLabel;

    // Flag to retry the operation again.
    boolean retryOp = true;
    // Current number of tries
    int retryCount = 0;

    while (retryOp) {
      try {
        datastreamID = getPortM().addDatastream(pid, dsID,
                altIDs, dsLabel,
                versionable,
                dsMIME,
                formatURI,
                dsLocation,
                dsControlGroup,
                dsState,
                createCheckSum? "MD5" : null,
                null,
                logMessage);

        // No Exceptions thrown.  Operation was successful.  Reset retry flag.
        retryOp = false;
        
      } catch (IOException addDsErr) {
        
        // Throw Exception as soon as maximum retries is reached.
        if (retryCount == this.maxOpRetries) {
          throw new IOException(
            "Operation Timed Out - Failed to add new datastream to " +
            "Digital Object with PID " + pid, addDsErr);
        }

        // Determines if `purgeErr` is a Fedora Common's Locked Object
        // Exception.
        if (this.isFcLockedObjectException(addDsErr)) {
          // Locked Object exception encountered.  Wait and retry.
          try {

            FedoraAPI.logger.info(
              "`addDatastream` method - " +
              "Encountered fedora.server.errors.ObjectLockedException. " +
              "Retrying operation again after " + this.DEFAULT_BACKOFF_TIME + "ms\n");

            Thread.sleep(this.DEFAULT_BACKOFF_TIME);
          } catch (InterruptedException wakedUpErr) {
            // Do nothing if Thread's sleep is interrupted.
          }
          // Increment the retry count.
          retryCount++;
          
        } else {
          // Not a Locked Object Exception.. Wrap and throw.
          retryOp = false;
          throw new IOException(
            "Failed to add new datastream to  Digital Object with PID " + pid,
            addDsErr);

        } // End of checking for Locked Object Exception
      } // End of try...catch of operation's Exception.
    } // End of loop - while `retryOp` is true.

    return datastreamID;
  }

//  /**
//   * Change the referenced location of a managed-content datastream.
//   * @param pid The PID of the object.
//   * @param dsID The datastream ID.
//   * @param dsMIME  The mime type.
//   * @param stream Location of managed datastream content.
//   * @return The timestamp of the operation according to the server, in ISO8601 format.
//   */
//
//  public String modifyDatastream(String pid, String dsID, String dsMIME, InputStream stream) throws IOException {
////modifyDatastreamByValue
////Modifies an existing Datastream in an object, by value. This operation is only valid for Inline XML Datastreams (i.e. controlGroup "X").
//    if ((pid == null) || (dsID == null) || (dsMIME == null) || (stream == null)) {
//      return null;
//    }
//    boolean versionable = true;
//    ArrayOfString altIDs = null;
//    String formatURI = "";
//    String dsLocation = null;
//      dsLocation = uploadStream(stream);
////    String dsControlGroup = "M";
////    String dsState = "A";
//    String logMessage = "Modified datastream: HUHU";
//    String checksumType = null;
//    String checksum = null;
//    boolean force = true;
//    // make the SOAP call on API-M using the connection stub
//    // TODO: Allow users to specify checksum
//    String datastreamID = null;
//    String dsLabel = dsID;
//      datastreamID = getPortM().modifyDatastreamByReference (pid,
//              dsID,
//              altIDs,
//              dsLabel,
//              dsMIME,
//              formatURI,
//              dsLocation,
//              checksumType,
//              checksum,
//              logMessage,
//              force);
//    return datastreamID;
//  }

  /**
   * Modifies the datastream of a Digital Object.
   *
   * Uploads the content of an {@java.io.InputStream} to Fedora Commons,
   * then stores a reference to its location within the repository as
   * the given datastream.
   *
   * @param pid The <code>PID</code> of the Digital Object to modify the
   * datastream.
   * @param dsID The datastream's ID.
   * @param dsMIME The MIME type of the datastream.
   * @param newStreamContent The new datastream's content
   *
   * @return The timestamp of the operation according to the Fedora Commons,
   * in ISO8601 format.  E.g., <code>2009-06-28 21:30Z</code>
   *
   * @throws {@link java.io.IOException} Errors encountered.  See wrapped
   * Exception for details.
   */
  public String modifyDatastream(String pid, String dsID,
    String dsMIME, String newStreamContent) throws IOException {

    // Simple argument validations.
    if ((pid == null) || (dsID == null) || (dsMIME == null) || (newStreamContent == null)) {
      throw new NullPointerException(
        "Reference to one of the required argument is null.");
    }

    String[] altIDs = null;
    String formatURI = "";

    // Default log message.
    String logMessage = "Modified datastream " + dsID +
      " of Digital Object with PID " + pid;

    String checksumType = null;
    String checksum = null;
    boolean force = true;

    // Temporary uploads the data to Fedora Commons to obtain an
    // Interent addressable URL to the new datastream.
    String dsLocation = uploadStream(newStreamContent);

    // make the SOAP call on API-M using the connection stub
    // TODO: Allow users to specify checksum
    String datastreamID = null;
    String dsLabel = dsID;

    // Flag to retry the operation again.
    boolean retryOp = true;
    // Current number of tries
    int retryCount = 0;

    while (retryOp) {
      try {
        datastreamID = getPortM().modifyDatastreamByReference (pid, dsID,
                altIDs, dsLabel, dsMIME,
                formatURI,
                dsLocation,
                checksumType,
                checksum,
                logMessage,
                force);

        // Operation was successful.  No Exceptions were thrown.  Reset the
        // retry flag.
        retryOp = false;
        
      } catch (IOException modDsErr) {

        // Throw Exception as soon as maximum retries is reached.
        if (retryCount == this.maxOpRetries) {
          throw new IOException(
            "Operation Timed Out - Failed to modify datastream " + dsID +
            " of Digital Object with PID " + pid,
            modDsErr);
        }

        // Determines if `modDsErr` is a Fedora Common's Locked Object
        // Exception.
        if (this.isFcLockedObjectException(modDsErr)) {
          // Locked Object exception encountered.  Wait and retry.
          try {
            FedoraAPI.logger.info(
              "`modifyDatastream` method - " +
              "Encountered fedora.server.errors.ObjectLockedException. " +
              "Retrying operation again after " + this.DEFAULT_BACKOFF_TIME + "ms\n");

            Thread.sleep(this.DEFAULT_BACKOFF_TIME);
          } catch (InterruptedException wakedUpErr) {
            // Do nothing if Thread's sleep is interrupted.
          }
          // Increments the retry count.
          retryCount++;

        } else {
          // Not a Locked Object Exception.. Wrap and throw.
          retryOp = false;
          throw new IOException(
            "Failed to modify datastream " + dsID +
            " of Digital Object with PID " + pid,
            modDsErr);

        } // End of checking for Locked Object Exception
      } // End of try...catch of IOException of operation.
    } // End of while `retryOp` is true.

    return datastreamID;

  } // End of `FedoraAPI.modifyDatastream` method.

  /**
   * Adds a <code>RDF</code> relationship between a Digital Object and its
   * Content Model Digital Object.
   *
   * @param pid <code>PID</code> of the Digital Object.
   * @param pidCM <code>PID</code> of the Content Model Digital Object.
   *
   * @throws {@link java.io.IOException} A network or repository error occued.
   * See wrapped exception for details.
   *
   * @since v0.3.
   */
  public void addCMRelationship(String pid, String pidCM) throws IOException {

    final String HAS_MODEL_RDF_PROPERTY =
      "info:fedora/fedora-system:def/model#hasModel";

    // Some simple method arguments' validations.
    if (pid == null) {
      throw new NullPointerException(
        "Reference to PID of Digital Object is null.");
    }

    if (pid.length() == 0) {
      throw new IllegalArgumentException(
        "Supplied PID of Digital Object is empty.");
    }

    if (pidCM == null) {
      throw new NullPointerException(
        "Reference to PID of Content Model Digital Object is null.");
    }

    if (pidCM.length() == 0) {
      throw new IllegalArgumentException(
        "Supplied PID of Content Model Digital Object is empty.");
    }

    // Flag to retry the operation again.
    boolean retryOp = true;
    // Current number of tries
    int retryCount = 0;

    while (retryOp) {
      try {
        getPortM().addRelationship(pid, HAS_MODEL_RDF_PROPERTY,
          "info:fedora/" + pidCM,
          false, null);

        // No Exceptions thrown.  Operation was successful.  Reset retry flag.
        retryOp = false;

      } catch (IOException addRdfErr) {

        // Throw Exception as soon as maximum retries is reached.
        if (retryCount == this.maxOpRetries) {
          throw new IOException(
            "Operation Timed Out - " +
            "Failed to add Content Model Relationship between " +
            "Digital Object with PID " + pid + " and Content Model " +
            "Digital Object with PID " + pidCM, addRdfErr);
        }

        // Determines if `addRdfErr` is a Fedora Common's Locked Object
        // Exception.
        if (this.isFcLockedObjectException(addRdfErr)) {
          // Go to sleep is a Locked Object Exception is encountered.
           try {
             FedoraAPI.logger.info(
               "`addCMRelationship` method - " +
               "Encountered fedora.server.errors.ObjectLockedException. " +
               "Retrying operation again after " + this.DEFAULT_BACKOFF_TIME + "ms\n");

              Thread.sleep(this.DEFAULT_BACKOFF_TIME);
              
           } catch (InterruptedException wakeUpErr) {
             // Ignore the thread waking up exception.
           }
           // Incremet the retry count
           retryCount++;

        } else {
          // Error is not a Locked Object Exception.
          retryOp = false;
          throw new IOException(
            "Failed to add Content Model Relationship between " +
            "Digital Object with PID " + pid + " and Content Model " +
            "Digital Object with PID " + pidCM,
            addRdfErr);

        } // End of if... conditional to check for Locked Object Exception.
      } // End of try...catch for IOException from operation.
    } // End of while `retryOp` is true.
    
  } // End of `FedoraAPI.addCMRelationship` method.

  /**
   * Creates a new relationship in the object. Adds the specified relationship
   * to the Digital Object's <code>RELS-EXT</code> Datastream.
   * If the Fedora Common's Resource Index is enabled, the
   * relationship will be added to the Resource Index (e.g., Mulgara).
   * 
   * A <code>RDF</code> tuple consist of an object (the subject),
   * having a predicate relating it to a target (the object).
   *
   * The object can either be a literal value, or a Fedora Digital object.
   *
   * @param pid The <code>PID</code> of the RDF's Object.
   * @param relationship The RDF's predicate.
   * @param object The RDF's object (target).
   * @param isLiteral Indicates whether the RDF's Object is a literal.
   * @param datatype The datatype of the literal. Optional.  <code>null</code>
   * to ignore.
   * @return boolean <code>true</code> if and only if the relationship was added.
   *
   * @throws java.io.IOException A network or repository error occurs. See
   * wrapped Exception for details.
   *
   * @since v0.3
   */
  public boolean addRelationship(String pid, String relationship, String object,
          boolean isLiteral, String datatype) throws IOException
  {

    // Simple argument validations.
    if ( (pid == null) || (relationship == null) || (object == null))
    {
      throw new NullPointerException(
        "Reference to one of the RDF triple's value is null.");
    }

    if ( (pid.length() == 0) ||
         (relationship.length() == 0) ||
         (object.length() == 0))
    {
      throw new IllegalArgumentException(
        "One of the values for RDF triple is empty.");
    }

    if (datatype == null)
    {
      throw new NullPointerException(
        "Reference to datatype is null.");
    }

    if (datatype.length() == 0)
    {
      throw new NullPointerException(
        "Supplied datatype is empty.");
    }

    // Flag to retry the operation again.
    boolean retryOp = true;
    // Current number of tries
    int retryCount = 0;

    boolean addRelRet = false;

    while (retryOp) {
      try {
        addRelRet = getPortM().addRelationship(pid, relationship, object,
          isLiteral, datatype);

        // No Exceptions thrown.  Operation was successful.  Reset retry flag.
        retryOp = false;

      } catch (IOException addRelErr) {

        // Throw Exception as soon as maximum retries is reached.
        if (retryCount == this.maxOpRetries) {
          throw new IOException(
            "Operation Timed Out - Failed to add RDF relationship " +
            pid + " " + relationship + " " + object,
            addRelErr);
        }

        // Determines if `addRelErr` is a Fedora Common's Locked Object
        // Exception.
        if (this.isFcLockedObjectException(addRelErr)) {
          // Go to sleep is a Locked Object Exception is encountered.
          try {

            FedoraAPI.logger.info(
              "`addRelationship` method - " +
              "Encountered fedora.server.errors.ObjectLockedException. " +
              "Retrying operation again after " + this.DEFAULT_BACKOFF_TIME + "ms\n");

              Thread.sleep(this.DEFAULT_BACKOFF_TIME);

          } catch (InterruptedException wakeUpErr) {
              // Ignore the thread waking up exception.
          }
          // Increments the retry count.
          retryCount++;

        } else {
          // Error is not a Locked Object Exception.
          retryOp = false;
          throw new IOException(
            "Failed to add RDF relationship " +
            pid + " " + relationship + " " + object,
            addRelErr);
        } // End of if... conditional to check for Locked Object Exception.
      } // End of try...catch of operation's main Exception.      
    } // End of while `retryOp` is true.

    return addRelRet;

  } // End of `FedoraAPI.addRelationship` method.

  /**
   * Wrapper for Fedora Commons' API-M <code>addRelationship</code>
   * web service.
   *
   * Creates a new <i>literal</i> value relationship in the Fedora
   * Digital Object specified by <code>pid</code> parameter.
   * 
   * Adds the specified relationship to the Digital Object's
   * <code>RELS-EXT</code> Datastream.
   *
   * If the Resource Index is enabled in Fedora Commons, the relationship will
   * be added to the Resource Index.
   * 
   * A RDF tuple consist of an object (the subject), having a predicate
   * relating it to a target (the object).
   *
   * The object must be a literal value; that is, not a reference.
   *
   * @param pid The PID of the Digital Object to add relationship to.
   * @param relationship RDF's predicate.
   * @param value RDF's object (target).
   *
   * @return boolean <code>true</code> if and only if the relationship was added.
   * @throws java.io.IOException If a network or repository error occurs.
   */
  public boolean addLiteralRelationship(String pid, String relationship, String value) throws IOException
  {
      return getPortM().addRelationship(pid, relationship, value, true, null);
  }

  /**
   * Remove all specified relationships from the <code>RELS-EXT</code> datastream
   * of a Digital Object.
   *
   * If the Resource Index is enabled, this will also delete the corresponding
   * triples from the Resource Index (e.g., from Mulgara).
   *
   * @param pid The <code>PID</code> of the Digital Object.
   * @throws {@link java.io.IOException} On error.
   * See wrapped Exception for details.
   */
  public void purgeAllRelationships(String pid) throws IOException {

    RelationshipTuple[] list = getRelationships(pid, null);

    if (list == null) {
      throw new NullPointerException(
        "References to RDF ralationships for Digital Object with PID " +
        pid + " are null.");
    }

    // Flag to retry the operation again.
    boolean retryOp = true;
    // Amount of time to wait before retrying operation.
    // Overriding default; in this case, double the original.
    final int WAIT_TIME_MS = this.DEFAULT_BACKOFF_TIME * 2;
    // Current number of tries
    int retryCount = 0;

    for (RelationshipTuple rel: list) {

      // Perform purge operation if the Digital Object is of type
      // Fedora Commons' Digital Object.
      // By default, this is true for all Digital Objects created by
      // Fedora Commons.
      if (!rel.getObject().equals("info:fedora/fedora-system:FedoraObject-3.0")) {

        while (retryOp) {
          try {
            getPortM().purgeRelationship(rmNS(rel.getSubject()),
                                         rel.getPredicate(),
                                         rmNS(rel.getObject()),
                                         rel.isIsLiteral(), rel.getDatatype());
            // No Exceptions thrown.  Reset the retry flag.
            retryOp = false;

          } catch (IOException purgeRelErr) {

            // Throw Exception as soon as maximum retries is reached.
            if (retryCount == this.maxOpRetries) {
              throw new IOException(
                "Operation Timed Out - Failed to purge all RDF relationships " +
                "from Digital Object " +
                "with PID " + pid, purgeRelErr);
            }

            // Determines if `purgeRelErr` is a Fedora Common's Locked Object
            // Exception.
            if (this.isFcLockedObjectException(purgeRelErr)) {
              // Go to sleep is a Locked Object Exception is encountered.
              try {

                FedoraAPI.logger.info(
                  "`purgeAllRelationships` method - " +
                  "Encountered fedora.server.errors.ObjectLockedException. " +
                  "Retrying operation again after " + WAIT_TIME_MS + "ms\n");

                Thread.sleep(WAIT_TIME_MS);

              } catch (InterruptedException wakeUpErr) {
              // Ignore the thread waking up exception.
              } // End of try...catch of Thread sleeping.
              // Increments the retry counter.
              retryCount++;
              
            } else {
              // Error is not a Locked Object Exception.
              retryOp = false;
              throw new IOException(
                "Failed to purge all RDF relationships from Digital Object " +
                "with PID " + pid,
                purgeRelErr);
            } // End of checking for Locked Object Exception.

          } // End of try...catch of IOException from operation.
        } // End of while `retryOp` is true.

      } // End of comparison to default content model string.
    } // End of iterating over relationship list

  } // End of `FedoraAPI.purgeAllRelationships` method.

  /**
   * Removes Fedora Common's namespace from a String.
   *
   * Typically, a Fedora Commons' PID looks like:
   * <code>info:fedora/fedora-system:FedoraObject-3.0</code>
   *
   * After running through this method, the returned String will look like
   * <code>fedora-system:FedoraObject-3.0</code>
   *
   * @param str String in fully qualified Fedora Commons PID format.
   *
   * @return String without Fedora Common's namespace.
   */
  private String rmNS(String str)
  {
    // Splits the String on first instance of forward slash (/) character. 
    if(str.startsWith("info:fedora/"))
      return str.substring("info:fedora/".length());
    return str;
  } // End of `FedoraAPI.rmNS` method.

//  /**
//   * This function creates a new object in the fedora repository. 
//   * The basic idea here is to first create a very simple object and put it into the repsitory.
//   * The object is very basic so it is recommended that the function modify DC should be
//   * called afterwards.
//   * @return The PID of the created item. Should be identical to the provided pid.
//   * 
//   * @throws IOException If the object creation in the repository failed.
//   */
//  public String createBasicNewObject(String pid, String lbl, String oId, boolean active) throws IOException {
//
//    String ownerId = oId;
//    String label = lbl;
//    StringBuffer foxml = new StringBuffer();
//    foxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//    foxml.append("\n");
//
//    foxml.append("<foxml:digitalObject PID=\""+pid+"\" VERSION=\"1.1\"");
//    foxml.append("\n");
//
//    foxml.append("  xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"");
//    foxml.append("\n");
//
//    foxml.append("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
//    foxml.append("\n");
//
//    foxml.append("  xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml#");
//    foxml.append(" http://www.fedora.info/definitions/1/0/foxml1-1.xsd\">");
//    foxml.append("\n");
//
//    foxml.append("  <foxml:objectProperties>\n");
//
//    if (active) {
//      foxml.append("      <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>\n");
//    } else {
//      foxml.append("      <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"I\"/>\n");
//    }
//    foxml.append("      <foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\"" + ownerId + "\"/>\n");
//    foxml.append("      <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"" + label + "\"/>\n");
//    foxml.append("   </foxml:objectProperties>\n");
//    foxml.append("</foxml:digitalObject>\n");
//
//    String objXML = foxml.toString();
//    String newPID = null;
//
//    try {
//      newPID = getPortM().ingest(objXML.getBytes("UTF-8"),
//                                 FOX_FORMAT,
//                                 "Created with FedoraAPI");
//
//    } catch (SOAPFaultException ingestErr) {
//      logger.error("Ingest operation failed.", ingestErr);
//      throw new IOException(ingestErr);
//    }
//    
//    return newPID;
//    
//  } // End of `FedoraAPI.createBasicNewObject` instance method.

//  public String getObject(String pid) {
//
//    return null;
//  }

/**
   * Get the relationships asserted in the object's RELS-EXT Datastream that match the given criteria
   * @param pid The PID of the object.
    * @param relationship The predicate to match. A null value matches all predicates
   * @return RelationshipTuple[]** String subject - The subject of the relation. The object on which the method was called.
String predicate - The predicate relating the subject and the object. Includes the namespace of the relation.
String object - The PID of the object (target) of the relation
boolean isLiteral - If true, the subject should be read as a literal value, not a PID
 * String datatype - If the subject is a literal, the datatype to parse the value as. Optional.
 * @throws IOException
   */
 
  public RelationshipTuple[] getRelationships(String pid, String relationship) throws IOException {
      RelationshipTuple[] result = this.getPortM().getRelationships(pid, relationship);
      return result;
  }
  /**
   * 
   * @param pid The PID of the object.
   * @param relationship The predicate.
   * @param object The object (target). 
   * @return True if and only if the this triple has already been added to RELS_EXT
   * @throws java.io.IOException
   */
  public boolean hasRelationship(String pid, String relationship, String object) throws IOException
  {      
      RelationshipTuple[] rel = getRelationships(pid, relationship);
      if (rel == null) return false;
      for (RelationshipTuple r: rel)
      {
          if (r.getObject().equals(object)) return true;
      }
      return false;
  }
  /**
   * 
   * @param pid The PID of the object.
   * @param relationship The predicate. 
   * @return True if and only if a similar relationship has already been added to RELS_EXT
   * @throws java.io.IOException
   */
  public boolean hasRelationship(String pid, String relationship) throws IOException
  {
    RelationshipTuple[] rel = getRelationships(pid, relationship);
      return (rel!=null && rel.length>0);
  }
  /**
   * Find all the content models listed in this objects RELS-EXT datastream
   * @param pid Fedora <code>PID</code> of the object to query
   *
   * @return List of content models to which the digital object conforms
   *
   * @throws java.io.IOException On error.
   */
  public List<String> getContentModels(String pid) throws IOException
  {
      List<String> contentModels = new ArrayList<String>();
      RelationshipTuple[] result =
        this.getPortM().getRelationships(pid,
          "info:fedora/fedora-system:def/model#hasModel");

      if (result != null) {
        for (RelationshipTuple cm : result) {
          if (!cm.getObject().equals("info:fedora/fedora-system:FedoraObject-3.0"))
            contentModels.add(cm.getObject());
        }
      }
      return contentModels;
      
  } // End of `FedoraAPI.getContentModels` method.

  /**
   * Checks if object with this fedora PID is already in the repository.
   * @param pid PID of object
   * @throws IOException If a network or Fedora repository error occurred.
   * @return true if an object with this Fedora PID already exists in the repository, false otherwise
   */
  public boolean doesPidExist(String pid) throws IOException {  
		String[] resultFields = {"pid"};
		Condition cond = new Condition("pid", ComparisonOperator.eq, pid);
		FieldSearchQuery fsq = new FieldSearchQuery(new Condition[] {cond}, null);
		NonNegativeInteger bi = new NonNegativeInteger("1");

		FieldSearchResult soapRes = getPortA().findObjects(resultFields, bi, fsq);

		if(soapRes.getResultList() ==null ||  soapRes.getResultList().length ==0)
			return false;

		return true;
  }

// Carsten: This code is wrong as it currently is: One would need to actually analyse the 
//          Exception and determine whether the reason for the exception is non-existane and
//          not some other error. Only then could false be return with confidence!
//   public boolean doesObjectExist(String pid)
//   {
//       ObjectProfile prof;
//        try {
//            prof = getPortA().getObjectProfile(pid, null);
//        } catch (Exception ex) {
//            return false;
//        }
//       return true;
//   }

  /**
   * Permanently removes a Fedora Commons Digital Object from the repository.
   *
   * @param pid The <code>PID</code> of the object.
   *
   * @param logMessage A log message to supply to Fedora Commons for this
   * action.  If <code>null</code> is supplied, a default message will be
   * generated.
   *
   * @throws {@link java.io.IOException} If the digital object can not be deleted.
   *
   * @since v0.3
   */
    public void purgeObject(String pid, String logMessage) throws IOException {
      
      // Carsten 18/05/2009: This is too slow and causes unclosed
      // DB connections in Fedora (due to a fedora bug)
      // if (!this.doesPidExist(pid)) return false;

      if (pid == null) {
        throw new NullPointerException(
          "Reference to PID of Digital Object to purge is null.");
      }

      if (pid.length() == 0) {
        throw new IllegalArgumentException(
          "Supplied PID of Digital Object to purge is empty.");
      }

      // Creates a custom log message to pass to Fedora Commons if one has
      // not been supplied.
      String purgeLogMsg = null;
      if ( (logMessage == null) || (logMessage.length() == 0)) {
        purgeLogMsg = "Purging Digial Object with PID " + pid;
      } else {
        purgeLogMsg = logMessage;
      }


      // Flag to retry the operation again.
      boolean retryOp = true;
      // Amount of time (ms) to wait before retrying operation.
      // Overriding default wait time; in this case, double.
      final int WAIT_TIME_MS = this.DEFAULT_BACKOFF_TIME * 2;
      // Current number of tries
      int retryCount = 0;

      while (retryOp) {

        try {
          // Attempt to purge the Digital Object.
          getPortM().purgeObject(pid, purgeLogMsg, false);

          // No Exceptions encountered.  Terminate the loop.
          retryOp = false;
          
        } catch (IOException purgeErr) {

          // Throw Exception as soon as maximum retries is reached.
          if (retryCount == this.maxOpRetries) {
            throw new IOException(
              "Operation Timed Out - Failed to purge Digital Object with PID " +
              pid, purgeErr);
          }

          // Determines if `purgeErr` is a Fedora Common's Locked Object
          // Exception.
          if (this.isFcLockedObjectException(purgeErr)) {
            // Go to sleep is a Locked Object Exception is encountered.
            try {
              
              FedoraAPI.logger.info(
                "`purgeObject` method - " +
                "Encountered fedora.server.errors.ObjectLockedException. " +
                "Retrying operation again after " + WAIT_TIME_MS + "ms\n");

              Thread.sleep(WAIT_TIME_MS);
              
            } catch (InterruptedException wakeUpErr) {
              // Ignore the thread waking up exception.
            }
            // Increment the retry count.
            retryCount++;

          } else {
            // Error is not a Locked Object Exception.
            retryOp = false;
            throw new IOException(
              "Failed to purge Digital Object with PID " + pid,
              purgeErr);

          } // End of if... conditional to check for Locked Object Exception.
        } // End of try...catch IOException from `purgeObject` method.
      } // End of while retryOp is true.
      
    } // End of `FedoraAPI.purgeObject` method.

    /**
     * Permanently removes all versions of a Datastream from a Digital Object
     *
     * @param pid The <code>PID</code> of the Digital Object.
     * @param dsID The datastream ID. 
     * @param logMessage A log message to supply to Fedora Commons for this
     * action.  If <code>null</code> is supplied, a default message will be
     * generated.
     *
     * @throws {@link java.io.IOException} On error.  See wrapped exception
     * for details.
     */
    public void purgeDatastream(String pid, String dsID, String logMessage) throws IOException
    {

      // Checks for parameters' validity.
      if (pid == null) {
        throw new NullPointerException(
          "Reference to PID of Digital Object is null.");
      }

      if (pid.length() == 0) {
        throw new IllegalArgumentException(
          "Supplied PID is empty.");
      }

      if (dsID == null) {
        throw new NullPointerException(
          "Reference to datastream's ID is null.");
      }

      if (dsID.length() == 0) {
        throw new IllegalArgumentException(
          "Supplied datastream's ID is empty.");
      }

      // Creates a default log message to pass to Fedora Commons if one has
      // not been supplied.
      String purgeLogMsg = null;
      if ( (logMessage == null) || (logMessage.length() == 0)) {
        purgeLogMsg = "Purging Datastream with ID " +
          dsID + " from Digial Object with PID " + pid;
      } else {
        purgeLogMsg = logMessage;
      }

      // Flag to retry the operation again.
      boolean retryOp = true;
      // Amount of time to wait before retrying operation.
      // Overriding default time; in this case, double it.
      final int WAIT_TIME_MS = this.DEFAULT_BACKOFF_TIME * 2;
      // Current number of tries
      int retryCount = 0;

      while (retryOp) {
        try {
          getPortM().purgeDatastream(pid, dsID, null, null, purgeLogMsg, false);

          // No Exceptions thrown.  Operation was successful.  Set retry flag to false.
          retryOp = false;
          
        } catch (IOException purgeDsErr) {

          // Throw Exception as soon as maximum retries is reached.
          if (retryCount == this.maxOpRetries) {
            throw new IOException(
              "Operation Timed Out - Failed to purge Datastream with ID " + dsID +
              " from Digial Object with PID " + pid, purgeDsErr);
          }

          // Determine whether operation is a Fedora Common's locked object
          // Exception or not.
          if (this.isFcLockedObjectException(purgeDsErr)) {

            // Locked Object Exception encountered.  Wait and retry.
            FedoraAPI.logger.info(
              "`purgeDatastream` method - " +
              "Encountered fedora.server.errors.ObjectLockedException. " +
              "Retrying operation again after " + WAIT_TIME_MS + "ms\n");
            
            try {
              Thread.sleep(WAIT_TIME_MS);
            } catch (InterruptedException wakedUpErr) {
              // Do nothing if thread's sleep is interrupted.
            }
            // Increments the retry count.
            retryCount++;

          } else {
            // NOT a Locked Object Exception, wrap and throw it to caller.
            retryOp = false;
            throw new IOException(
              "Failed to purge Datastream with ID " + dsID +
              " from Digial Object with PID " + pid, purgeDsErr);

          } // End of checking for Locked Object Exception
        } // End of try...catch for operation.
      } // End of while `retryOp` is true.
        
    } // End of `FedoraAPI.purgeDatastream` method.

    /**
   * Find object by PID in Fedora.
   * @param pid The PID of the object. 
   * @return pid if the object was found, null if the object did not exist in the repository
   */
    public String findObjectByPID(String pid)
    {
      return pid;
    }
    /**
   * Find object by GUID in Fedora.
   * @param guid The GUID of the object.
   * @return pid if the object was found, null if the object did not exist in the repository
   */
    public String findObjectByDCIdentifier(String guid)
    {
      String pid = null;
      return pid;
    }

    /**
     * Retrieves an object profile from Fedora
     * @param guid The GUID of the object.
     * @param asOfDateTime The date for which the profile is to be returned.
     * @return The profile for the objet.
     * @throws IOException 
     */
		public ObjectProfile getObjectProfile(String guid, String asOfDateTime) throws IOException {
			return getPortA().getObjectProfile(guid, asOfDateTime);
		}

		/**
		 * Returns a list of the datastreams for object <code>pid</code>.
		 * @param pid The object for which the datastreams are to be retrieved.
		 * @param asOfDateTime The date for which the datastreams are to be retrieved.
		 * @return a list of the datastreams for object <code>pid</code>.
		 * @throws IOException 
		 */
		public DatastreamDef[] listDataStreams(String pid, String asOfDateTime) throws IOException {
			return getPortA().listDatastreams(pid, asOfDateTime);
		}
   
   
        /**
         *
         * @return the total number of digital objects currently in the repository
     * @throws IOException if a network or Fedora Server error occured.
         */
        public int countRepositoryObjects() throws IOException
        {
            Collection<String> allObjectPids = findObjectsWherePidContains("ala:*");
            return allObjectPids.size();
        }
        
  /**
   * 
   * @return the total number of datastreams for all objects currently in the
   *         repository
   * @throws IOException if a network or Fedora Server error occured.
   */
  public int countRepositoryDatastreams() throws IOException {
    Collection<String> allObjectPids = findObjectsWherePidContains("ala:*");
    Iterator<String> aopi = allObjectPids.iterator();
    int nDataStreams = 0;
    while (aopi.hasNext()) {
      String pid = aopi.next();
      DatastreamDef[] dl = listDataStreams(pid, null);
      nDataStreams += (dl==null ? 0 : dl.length);
    }
    return nDataStreams;
  }
   /**
    * Finds all the objects in the Fedora Repository with a pid that contains a given pattern
    * @param pattern the String pattern to match
    * @return the Fedora Repository ids (pids) of all matching objects
    * @throws java.io.IOException
    */
	public Collection<String> findObjectsWherePidContains(String pattern) throws IOException {
	   String[] resultFields = {"pid"};
	    Condition cond = new Condition("pid", ComparisonOperator.has, pattern);
	    FieldSearchQuery fsq = new FieldSearchQuery(new Condition[] {cond}, null);
	    NonNegativeInteger bi = new NonNegativeInteger("500");

	    FieldSearchResult soapRes = getPortA().findObjects(resultFields, bi, fsq);


		ArrayList<String> res = new ArrayList<String>();
		while (true) {
			for (ObjectFields objectField : soapRes.getResultList()) {
				res.add(objectField.getPid());
			}
			if (soapRes.getListSession() == null) {
				break;
			}
			String token = soapRes.getListSession().getToken();
			soapRes = getPortA().resumeFindObjects(token);
		}
		return res;
	}
    /**
     * Finds all the objects in the Fedora Repository with a pid that contains a given pattern modified since a given date
     * @param pattern pattern the String pattern to match
     * @param modifiedSince timestamp of earliest time/date to match
     * @return the Fedora Repository ids (pids) of all matching objects
     * @throws java.io.IOException
     */
    /**
     * Find repository objects matching a given pattern and modifed after a given time
     * @param pattern
     * @param modifiedSince
     * @return A list of matching repository items
     * @throws java.io.IOException
     */
    public Collection<String> findObjectsWherePidContains(String pattern, String modifiedSince) throws IOException {
	   String[] resultFields = {"pid","mDate"};
	    Condition cond1 = new Condition("pid", ComparisonOperator.has, pattern);
        Condition cond2 = new Condition("mDate", ComparisonOperator.gt, modifiedSince);
       // Condition cond2 = new Condition("mDate", ComparisonOperator.gt, "2009-06-22T16:48:02");

	    FieldSearchQuery fsq = new FieldSearchQuery(new Condition[] {cond1, cond2}, null);
	    NonNegativeInteger bi = new NonNegativeInteger("500");

	    FieldSearchResult soapRes = getPortA().findObjects(resultFields, bi, fsq);


		ArrayList<String> res = new ArrayList<String>();
		while (true) {
			for (ObjectFields objectField : soapRes.getResultList()) {
				res.add(objectField.getPid());
			}
			if (soapRes.getListSession() == null) {
				break;
			}
			String token = soapRes.getListSession().getToken();
			soapRes = getPortA().resumeFindObjects(token);
		}
		return res;
	}
    /**
     * Returns the DC stream identifier of this PID, excluding the PID itself
     * Note that this ID needs to be unique, as it is currently how we found the authoritative
     * GUID of an object eg TaxonName GUID. If more than one identifier is found (other than the PID)
     * only the first will be returned, and an error will be logged
     * @param PID The PID of the object the method is to be applied to.
     * @return the (first) dc:identifier of the object
     * @throws java.io.IOException
     */
    public String findIdentifier(String PID) throws IOException {
	   String[] resultFields = {"pid","identifier"};
	    Condition cond = new Condition("pid", ComparisonOperator.eq, PID);
	    FieldSearchQuery fsq = new FieldSearchQuery(new Condition[] {cond}, null);
	    NonNegativeInteger bi = new NonNegativeInteger("1");

	    FieldSearchResult soapRes = getPortA().findObjects(resultFields, bi, fsq);

        ArrayList<String>ids = new ArrayList<String>();
        ObjectFields objectField = soapRes.getResultList()[0];

        for (String id : objectField.getIdentifier())
            if (!id.equals(PID)) ids.add(id);
		if (ids.size()>1)
            logger.error("The object with PID "+PID+" has more than one identifier (other than its PID).\nThe system currently relies on this identifier being unique and returns only the first one");
		return ids.get(0);
	}
	/**
     * Deletes all objects in the Fedora Repository whose pid contains the geiven String pattern
     * @param pattern The String pattern to match
     * @throws java.io.IOException
     */
  public void deleteObjectsWherePidContains(String pattern) throws IOException {
    for (String pid : findObjectsWherePidContains(pattern)) {
      getPortM().purgeObject(pid, "ALA Purge All Operation", false);
    }
	}
    
  /**
   * Lists all the methods that the object supports.Each method can take a number
   * of paramethers. Each parameter for a method has a name, and a type. The
   * possible values of a parameter depends on its type. It can be bound to a
   * datastream in the object, it can have a hardcoded value or it can be defined
   * by the caller.Each parameter is defined to be passed by reference or passed by value.
   * @param pid The pid of the object.
   * @return ObjectMethodDef[]** String PID The pid of the data object
    String serviceDefinitionPID The pid of the service definition object
    String methodName the name of the method
    MethodParmDef[] methodParmDefs An array of the method parameters
    String parmName The name of the parameter.
    String parmType The type of the parameter. Restricted to "fedora:datastreamInputType", "fedora:userInputType" or "fedora:defaultInputType"
    String parmDefaultValue If the parmType is default, this is the value that will be used. Null if other type.
    String[] parmDomainValues If the parameter can be defined by the user, these are the possible values. If Null, the parameter can take any value. Null if other type.
    boolean parmRequired False, if this parameter can be left out of a call.
    String parmLabel The label for the parameter. Can be null.
    String parmPassBy The method of passing the paramenter. Restricted to "URL_REF" (if the parameter is pass by reference - by an url) and "VALUE" (if the parameter is pass by value)
    String asOfDate The timestamp/version of the method definition
   * @throws java.io.IOException
   */
  public ObjectMethodsDef[] listMethods(String pid) throws IOException {
		return getPortA().listMethods(pid, null);
	}

	/**
	 * Retrieved a method based dissemination.
	 * @param pid The PID of the object the method is to be applied to.
	 * @param methodName The name of the method to call.
	 * @param serviceDefPid The PID of the corresponsing sDef. (Why is this necessary???)
	 * @param parameters Optional parameters for the method call.
	 * @param asOfDate Optionally the date determining what version of the method to call.
	 * @return The results of the dissementaiton from Fedora.
	 * @throws IOException if a network or Fedora repository error occured.
	 */
	public MIMETypedStream getDissemination(String pid, String methodName, String serviceDefPid, Property[] parameters, String asOfDate) throws IOException {
		return getPortA().getDissemination(pid, serviceDefPid, methodName, parameters, asOfDate);
	}
/**
 * Creates a new digital object in the repository. If the XML document does not specify
 * the PID attribute of the root element, the repository will generate and return a
 * new pid for the object resulting from this request. That pid will have the namespace
 * of the repository. If the XML document specifies a pid, it will be assigned to the
 * digital object provided that 1. it conforms to the Fedora pid Syntax, 2. it uses a
 * namespace that matches the "retainPIDs" value configured for the repository, and 3.
 * it does not collide with an existing pid of an object in the repository.
 * @param digiObj The digital object in an XML submission format.
 * @param format The XML format of objectXML, one of "info:fedora/fedora-system:FOXML-1.1",
 * "info:fedora/fedora-system:FOXML-1.0", "info:fedora/fedora-system:METSFedoraExt-1.1",
 * "info:fedora/fedora-system:METSFedoraExt-1.0", "info:fedora/fedora-system:ATOM-1.1",
 * or "info:fedora/fedora-system:ATOMZip-1.1".
 * @param logMessage A log message
 * @return
 * @throws java.io.IOException
 */
  public String ingest(byte[] digiObj, String format, String logMessage) throws IOException {
   return getPortM().ingest(digiObj, format, logMessage);
  }

  /**
   * Gets the serialization of the digital object to XML appropriate for persistent
   * storage in the repository, ensuring that any URLs that are relative to the local
   * repository are stored with the Fedora local URL syntax. The Fedora local URL
   * syntax consists of the string "local.fedora.server" standing in place of the
   * actual "hostname:port" on the URL). Managed Content (M) datastreams are stored
   * with internal identifiers in dsLocation. Also, within selected inline XML
   * datastreams (i.e., WSDL and SERVICE_PROFILE) any URLs that are relative to the
   * local repository will also be stored with the Fedora local URL syntax.
   * @param pid The PID of the object.
   * @return The digital object in Fedora's internal storage format.
   * @throws java.io.IOException
   */
  public byte[] getObjectXML(String pid) throws IOException {
    return getPortM().getObjectXML(pid);
  }
/**
 * Gets the specified datastream.
 * @param pid The pid of the object.
 * @param dsID The datastream ID.
 * @param asOfDateTime The date/time stamp specifying the desired version of the object.
 * If null, the current version of the object (the most recent time) is assumed.
 * @return Datastream:
    DatastreamControlGroup controlGroup - String restricted to the values of "X", "M", "R", or "E" (InlineXML,Managed Content,Redirect, or External Referenced).
    String ID - The datastream ID (64 characters max).
    String versionID - The ID of the most recent datastream version
    String[] altIDs - Alternative IDs for the datastream, if any.
    String label - The Label of the datastream.
    boolean versionable - Whether the datastream is versionable.
    String MIMEType - The mime-type for the datastream, if set.
    String formatURI - The format uri for the datastream, if set.
    String createDate - The date the first version of the datastream was created.
    long size - The size of the datastream in Fedora. Not the size of any referenced contents, but only the fedora stored xml. TODO: What for Managed?
    String state - The state of the datastream. Will be "A" (active), "I" (inactive) or "D" (deleted).
    String location - If the datastream is an external reference or redirect, the url to the contents. TODO: Managed?
    String checksumType - The algorithm used to compute the checksum. One of "DEFAULT", "DISABLED", "MD5", "SHA-1", "SHA-256", "SHA-385", "SHA-512", "HAVAL", "TIGER", "WHIRLPOOL".
    String checksum - The value of the checksum represented as a hexadecimal string.

 * @throws java.io.IOException
 */
  public Datastream getDatastream(String pid, String dsID,
      String asOfDateTime) throws IOException {
    return getPortM().getDatastream(pid, dsID, asOfDateTime);
  }

  /**
   *
   * @return the user name being used to access the Fedora repository
   */
  public String getUserName() {
    return getFedoraAPIConfig().getUserName();
  }
      /**
     * Searches the Fedora Resource Index for all objects with matching property value and contentModel
     * @param propertyName type of property to search for
     * @param propertyValue value of the property
     * @param contentModel restrict the objects returned to those matching this model
     * @return the Fedora PIDS of any matching objects
     * @throws org.apache.commons.httpclient.URIException
     */
   Collection<String> findObjectsByProperty(String propertyName, String propertyValue, String contentModel) throws  IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = constructQuery(propertyName, propertyValue, contentModel,queryLang);
    return findObjectsByQuery(query,queryLang);
  }
  /**
   * Searches the Fedora Resource Index for all objects with matching property values
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @return the Fedora PIDS of any matching objects
   * @throws org.apache.commons.httpclient.URIException
   */
  Collection<String> findObjectsByProperty(String propertyName, String propertyValue) throws IOException, FedoraException
  {
    String queryLang = "sparql";
    String query = constructQuery(propertyName, propertyValue,queryLang);
    return findObjectsByQuery(query,queryLang);
  }
  /**
   * Searches the Fedora Resource Index for all objects that matches the supplied query
   * @param query in ITQL or sparql query language
   * @param queryLang must be "itql" or "sparql"
   * @return the Fedora PIDS of any matching objects
   * @throws IOException if there's a problem in the http method
   * @throws FedoraException If an invalid query language selected, or a problem in encoding it
   */
  public  Collection<String> findObjectsByQuery(String query, String queryLang) throws IOException, FedoraException
  {
      if (!queryLang.equalsIgnoreCase("itql") && !queryLang.equalsIgnoreCase("sparql"))
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
    ArrayList<String> res = new ArrayList<String>();
    HttpClient client = getFedoraClient().getHttpClient();

    BufferedReader br = null;

    String baseURL = "http://" + getFedoraAPIConfig().getHost() + ":" +
            getFedoraAPIConfig().getPort() + getFedoraAPIConfig().getRISearchPath();
    PostMethod method = new PostMethod(baseURL);

    String prefix = baseURL+"?type=tuples&lang="+queryLang+"&format=CSV&query=";
    String encodedQuery="";
    try {
        encodedQuery = URIUtil.encodeQuery(query);
    } catch (URIException ex) {
        String msg = "Error encoding query "+query;
        throw new FedoraException(msg,ex);
    }

    method.setQueryString(prefix+encodedQuery);

    try{
      int returnCode = client.executeMethod(method);

      if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
        logger.warn("The Post method is not implemented by this URI");
        // still consume the response body
        method.getResponseBodyAsString();
      } else {
        br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
        String readLine = br.readLine();//ignore heading
        int count=0;
        if (!readLine.isEmpty())while(((readLine = br.readLine()) != null)) {
          logger.info("Query result "+count+": "+readLine);
          String obName;
          int slashPos = readLine.indexOf('/');

          int commaPos = readLine.indexOf(',');
          if (commaPos == -1)
            obName = readLine.substring(slashPos+1);
          else
            obName = readLine.substring(slashPos+1, commaPos);
          res.add(obName);
          count++;
        }
      }
    } catch (IOException ex) {
        String msg = "Error executing method in find objects";
        throw new IOException(msg,ex);
    }

    finally {
      method.releaseConnection();
      if(br != null) br.close(); 
    }
    return res;
  }
    /**
   * Searches the Fedora Resource Index for all objects that matches the supplied query
   * @param query in ITQL or sparql query language
   * @param queryLang must be "itql" or "sparql"
   * @return Matching objecst in SearchResult format
   * @throws IOException if there's a problem in the http method
   * @throws FedoraException If an invalid query language selected, or a problem in encoding it
   */
   public  Collection<SearchResult> findRIObjectsByQuery(String query, String queryLang) throws IOException, FedoraException
  {
      if (!queryLang.equalsIgnoreCase("itql") && !queryLang.equalsIgnoreCase("sparql"))
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
     ArrayList<SearchResult> res = new ArrayList<SearchResult>();
     HttpClient client = getFedoraClient().getHttpClient();

    String baseURL = "http://" + getFedoraAPIConfig().getHost() + ":" +
            getFedoraAPIConfig().getPort() + getFedoraAPIConfig().getRISearchPath();
    PostMethod method = new PostMethod(baseURL);

    String prefix = baseURL+"?type=tuples&lang="+queryLang+"&format=CSV&query=";
    String encodedQuery="";
    try {
        encodedQuery = URIUtil.encodeQuery(query);
    } catch (URIException ex) {
        String msg = "Error encoding query "+query;
        throw new FedoraException(msg,ex);
    }

    method.setQueryString(prefix+encodedQuery);

    try{
      int returnCode = client.executeMethod(method);

      if (returnCode == HttpStatus.SC_OK)
      {
          res = this.parseRIResponse(method.getResponseBodyAsStream());
      }
      else if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
        logger.warn("The Post method is not implemented by this URI");
        // still consume the response body
        method.getResponseBodyAsString();
      } else {
        logger.warn("HTTP Request was unsuccessful: Error code "+returnCode);
        // still consume the response body
        method.getResponseBodyAsString();
      }
    } catch (IOException ex) {
        String msg = "Error executing method in find objects";
        throw new IOException(msg,ex);
    }

    finally {
      method.releaseConnection();
  //    if(br != null) br.close();
    }
    return res;
  }
   /**
    * Transforms a response from a Fedora Resource Index search to an array of SearchResults objects
    * @param response Query response returned from a Resource Index search
    * @return The reponse as an array of SearchResults
    * @throws java.io.IOException
    * @throws csiro.diasb.fedora.FedoraException
    */
   public  ArrayList<SearchResult> parseRIResponse(InputStream response) throws IOException, FedoraException
  {
    ArrayList<SearchResult> res = new ArrayList<SearchResult>();

    BufferedReader br = null;
    br = new BufferedReader(new InputStreamReader(response, "UTF-8"));
    String readLine = br.readLine();//ignore heading

    int count=0;
    while(((readLine = br.readLine()) != null)) {
      logger.info("Query result "+count+": "+readLine);

      String s[] = readLine.split(",");
      //This only works if the results returned are in order - object, title and contentModel
      //This depends on the query
      String pid = s[0].substring(s[0].indexOf('/')+1);
      String contentModel = s[1].substring(s[1].indexOf('/')+1);

      if (s.length>3) s[2] += ","+s[3];
      //strip extra inverted commas from the beginnning and end of the title
      String title = s[2];
      int icPos = s[2].indexOf("\"");
      if (icPos>-1)
      {
          if (icPos==0) title = s[2].substring(1);
          icPos = title.indexOf("\"");
          if (icPos>-1) title = title.substring(0,icPos);
      }

      //It's currently returning two items for each object, but with 2 different content models
      if (contentModel.startsWith("ala"))
      {
        SearchResult sr = new SearchResult(pid,title,contentModel);
        res.add(sr);
      }
      count++;
    }
    if(br != null) br.close();
    return res;
  }

  /**
   * Sets the number of retries to perform.
   * 
   * @param count Number of retries to perform.  Has to be larger than one.
   * Else, the default number of count of 10 is set.
   *
   * @return The current number of retries.
   * 
   * @since v0.3
   */
  public int setRetryCount(int count) {

    if (count < 1) {
      return this.maxOpRetries;
    } else {
      this.maxOpRetries = count;
    }

    return this.maxOpRetries;

  } // End of `FedoraAPI.setRetryCount` method.

  /**
   * Returns the number of retries each operation should perform.
   *
   * @return The number of retries each operation should perform.
   *
   * @since v0.3
   */
  public int getRetryCount() {
    return this.maxOpRetries;

  } // End of `FedoraAPI.getRetryCount` method.

  /**
   * Checks whether an Exception is of of type
   * {@link fedora.server.errors.ObjectLockedException} object.
   *
   * @param err Original Exception to check.
   *
   * @return <code>true</code> is Exception is of type
   * {@link fedora.server.errors.ObjectLockedException}, <code>false</code>
   * otherwise.
   *
   * @since v0.3
   */
  protected boolean isFcLockedObjectException(Exception err) {

    final boolean DEBUG = true;

    final String AXIS_FC_LOCKED_OBJ_ERR_PREFIX =
      "fedora.server.errors.ObjectLockedException";

    /*
    if (err.getCause() == null) {
      FedoraAPI.logger.info("Reference to original Exception is null.  " +
        "Returning false.");

      return false;
    }
    */

    if (DEBUG) {
      FedoraAPI.logger.debug("Comparing error message `" +
        err.getMessage() + "`" + " to " + "`" +
        AXIS_FC_LOCKED_OBJ_ERR_PREFIX + "`" + "\n");
    }
    
    if (err.getMessage().startsWith(AXIS_FC_LOCKED_OBJ_ERR_PREFIX)) {
      return true;
    } else {
      return false;
    }
    
    // return err.getCause() == null? false : isFcLockedObjectException((Exception) err.getCause());

  } // End of `FedoraAPI.isFcLockedObjectException` method.

  /**
   * Constructs an ITQL or SPARQL query from the supplied parameters
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query  format
   */
  private static String constructQuery(String propertyName, String propertyValue, String queryLang) throws FedoraException
  {
      String query;
      if (queryLang.equalsIgnoreCase("itql"))      
        query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+"'";
      else if (queryLang.equalsIgnoreCase("sparql"))
      {
      //  query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+"' }";          
      //    StringBuffer bf = new StringBuffer("SELECT ?object $title ?rel ?rank $contentModel $guid from <#ri> where {");
         StringBuffer bf = new StringBuffer("SELECT ?object $title $contentModel from <#ri> where {");
          bf.append("?object ?rel ?rank");    
       //   bf.append("  . ?object <dc:identifier> $pid");
       //   bf.append("  . ?object <dc:identifier> $guid ");
          bf.append("  . ?object <dc:title> $title");
          bf.append("  . $object <fedora-model:hasModel> $contentModel");

   //       bf.append("FILTER( regex($rank, '"+propertyValue+"')&& regex($pid,\"^ala\") && ($pid != $guid))");
          bf.append("FILTER( regex($rank, '"+propertyValue+"'))");
          query = bf.toString();
      }
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
  }

  /**
   * Constructs an ITQL or SPARQL query from the supplied parameters
   * @param propertyName type of property to search for
   * @param propertyValue value of the property
   * @param contentModel restrict the objects returned to those matching this content model
   * @param queryLang must be "itql" or "sparql"
   * @return The constructed query as a String
   */
  private static String constructQuery(String propertyName,  String propertyValue, String contentModel, String queryLang) throws FedoraException
  {
      String query;
      if (queryLang.equalsIgnoreCase("itql"))
          query = "select $object from <#ri> where $object <"+propertyName+"> '"+propertyValue+
              "' and $object <fedora-model:hasModel> <"+contentModel+">";
      else if (queryLang.equalsIgnoreCase("sparql"))
          query = "select ?object from <#ri> where { ?object <"+propertyName+"> '"+propertyValue+
              "' . ?object <fedora-model:hasModel> <"+contentModel+"> . }";
      else
      {
          String msg = "Invalid query language: must be itql or sparql";
          throw new FedoraException(msg);
      }
      return query;
      
  } // End of `FedoraAPI.constructQuery` method.

  /**
   * Returns a reference to {@link csiro.diasb.repository.FedoraAPIConfig}
   * class.
   *
   * {@link csiro.diasb.repository.FedoraAPIConfig} is instantiated at runtime
   * by Spring Framework.
   *
	 * @return Reference to a {@link csiro.diasb.repository.FedoraAPIConfig}
   * object instantiated by Spring Framework.
	 */
	private FedoraAPIConfig getFedoraAPIConfig() {
		return fedoraAPIConfig;
	} // End of `FedoraAPI.getFedoraAPIConfig` method.

  /**
   * Returns the Access API (API-A) of Fedora Commons Web Service.
   *
   * @return Reference to {@link fedora.server.access.FedoraAPIA} object.
   * This object has methods for API-A operations.
   *
   * @throws {@link java.io.IOException} On Error.
   */
  private FedoraAPIA getPortA() throws IOException {

    if(fedoraClient == null) {
  		init();
  	}
    try {
      return fedoraClient.getAPIA();

    } catch (ServiceException e) {
      // TODO: Why is this exception being wrapped inside an IOException?
      throw new IOException(e.getMessage(),e);
    }

  } // End of `FedoraAPI.getPortA` method.

  /**
   * Returns a reference to the management API (API-M) of Fedora Commons'
   * web service.
   *
   * @return Reference to management API (API-M) object.  Actual object is of
   * type {@link fedora.server.management.FedoraAPIM}
   *
   * @throws{@link java.io.IOException} On errror.
   */
  private FedoraAPIM getPortM() throws IOException {
  	if(this.fedoraClient==null) {
  		init();
  	}

    try {
      return this.fedoraClient.getAPIM();

    } catch (ServiceException e) {

      // TODO: Why is this being wrapped inside an IOException?
      throw new IOException(
        "Failed to obtain management API.  " + e.getMessage()
        ,e);
    }

  } // End of `FedoraAPI.getPortM` method.
/**
 * Search the Fedora Repository using the GSearch REST interface
 * @param query
 * @return The matched objects in SearchResults format
 * @throws java.io.IOException
 * @throws csiro.diasb.fedora.FedoraException
 */
   public  Collection<SearchResult> findGSearchObjectsByQuery(String query) throws IOException, FedoraException
  {

     ArrayList<SearchResult> res = new ArrayList<SearchResult>();
     HttpClient client = getFedoraClient().getHttpClient();

    String baseURL = "http://" + getFedoraAPIConfig().getHost() + ":" +
            getFedoraAPIConfig().getPort() + "/fedoragsearch/rest";
    PostMethod method = new PostMethod(baseURL);

    String prefix = baseURL+"?operation=gfindObjects&query=";
    String encodedQuery="";
    try {
        encodedQuery = URIUtil.encodeQuery(query);
    } catch (URIException ex) {
        String msg = "Error encoding query "+query;
        throw new FedoraException(msg,ex);
    }

    method.setQueryString(prefix+encodedQuery);

    try{
      int returnCode = client.executeMethod(method);

      if (returnCode == HttpStatus.SC_OK)
      {
          res = this.parseRIResponse(method.getResponseBodyAsStream());
      }
      else if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
        logger.warn("The Post method is not implemented by this URI");
        // still consume the response body
        method.getResponseBodyAsString();
      } else {
        logger.warn("HTTP Request was unsuccessful: Error code "+returnCode);
        // still consume the response body
        method.getResponseBodyAsString();
      }
    } catch (IOException ex) {
        String msg = "Error executing method in find objects";
        throw new IOException(msg,ex);
    }

    finally {
      method.releaseConnection();
    }
    return res;
  }

} // End of `FedoraAPI` class.

