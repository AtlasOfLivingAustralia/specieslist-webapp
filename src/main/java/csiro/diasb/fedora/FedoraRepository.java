/**
 * Copyright 2009 CSIRO.
 * All rights reserved.
 */
package csiro.diasb.fedora;

import java.io.*;

import org.apache.commons.httpclient.util.*;
import org.apache.log4j.*;

/**
 * @author fri096
 * 
 */
public class FedoraRepository {
  /**
   * The logger for this class.
   */
  final static Logger logger = Logger.getLogger(FedoraRepository.class);
  /**
   * Reference to the fedora api.
   */
  private FedoraAPI fedoraAPI;

  /**
   * @return the fedoraAPI
   */
  public FedoraAPI getFedoraAPI() {
    return fedoraAPI;
  }

  /**
   * Does a REST query against fedora and returns the result as a string.
   * 
   * @param locator The location of the REST request.
   * @return The web application result as a string.
   * @throws IOException If something goes wrong.
   */
  public String get(String locator) throws IOException {
    String resString = getFedoraAPI().
                       getFedoraClient().getResponseAsString(locator, true, true);
    String toRet = new String(resString.getBytes(), "UTF-8");
    return toRet;

  }

  /**
   * @param fedoraAPI the fedoraAPI to set
   */
  public void setFedoraAPI(FedoraAPI fedoraAPI) {
    this.fedoraAPI = fedoraAPI;
  }

  /**
   * Returns the content model of a Fedora Commons digital object.
   * 
   * @param pid The <code>PID</code>s of Fedora Commons digital object to get
   *          its Content Model.
   * 
   * @return Array of {@link java.lang.String} that contains the Content Model
   *         of the Fedora Commons Digital Object specified by <code>pid</code>
   * 
   * @throws Exception
   * @since v0.3
   */
  public String[] getContentModelPids(String pid) throws Exception {
    final String queryUri = "/risearch?type=tuples&lang=itql&format=csv&distinct=on&query=";
    StringBuffer queryBuilder = new StringBuffer();
    queryBuilder.append("select $model from <#ri> ");
    queryBuilder.append("where ");
    queryBuilder.append("<info:fedora/" + pid + "> <fedora-model:hasModel> ");
    queryBuilder.append("$model");
    final String query = queryBuilder.toString();
    logger.info("Un-encoded iTQL query is:\n" + query + "\n");
    final String encodedQuery = URIUtil.encodeQuery(query);
    final String getDsUrl = queryUri + encodedQuery;
    logger.info("iTQL query URL is: " + getDsUrl + "\n");
    String responseString = get(getDsUrl);
    logger
        .info("Normalising/Flattening CSV data into a single dimension collection.");
    String[] lines = responseString.split("\n");
    if (lines.length == 0) {
      throw new Exception("No data is present after flattening of CSV data.\n");
    }
    if (lines.length < 2) {
      throw new Exception("No data.  " + "Only header " + "`" + lines[0] + "`"
          + " was found.\n");
    }
    // Constructs the String to return, removing the first String from
    // previous tokenising operation, which contains the heading.
    int orgIndex = 1;
    int targetIndex = 0;
    String[] cmIdsArray = new String[lines.length - 1];
    while (orgIndex < lines.length) {
      cmIdsArray[targetIndex] = removeTrailingNewLine(lines[orgIndex]);
      orgIndex++;
      targetIndex++;
    }
    logger.info("Returning " + cmIdsArray.length
        + " Content Model PIDs to return." + " Contents are:");
    targetIndex = 0;
    while (targetIndex < cmIdsArray.length) {
      String currentCMId = cmIdsArray[targetIndex];
      logger.info("Index " + targetIndex + " " + "Length "
          + currentCMId.length() + " - " + "`" + currentCMId + "`");
      targetIndex++;
    }
    return cmIdsArray;
  } // End of `getContentModelPids` method.

  /**
   * Removes the trailing New Line character from the last String column of a
   * row in a Comma Separated Values (CSV) String.
   * 
   * For example, in the CSV format <span> Col1,Col2,Col3 R1C1,R1C2,R1C3
   * R2C1,R2C2,R2c3 </span>
   * 
   * <code>Col3</code>, <code>R1C3</code>, and <code>R2c3</code> have trailing
   * New Line (Return Carriage) characters that needs to be removed.
   * 
   * @param src Last String of a row in a CSV formatted document that contains a
   *          trailing New Line character.
   * 
   * @return <code>src</code> without trailing New Line Character.
   * 
   * @since 0.3
   */
  private String removeTrailingNewLine(String src) {
    return src.substring(0, src.length() - 1);
  } // End of `removeTrailingNewLine` method.
}
