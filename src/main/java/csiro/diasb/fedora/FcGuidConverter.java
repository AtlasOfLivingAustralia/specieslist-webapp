package csiro.diasb.fedora;

import java.math.BigInteger;
import java.security.*;
import org.apache.log4j.*;

/**
 * Utility class that converts <code>GUID</code> of a document and
 * <code>PID</code> of a Fedora Commons' Digital object.
 *
 * Heavily modified version of code from <code>DiasIngester</code> project.
 *
 * @author hwa002
 *
 * @version 0.1
 */
public class FcGuidConverter {
  
  // Logger for this class.
  private final Logger logger = Logger.getLogger(this.getClass().getName());


  // Prefix String. to Fedora Commons' PID.
  private final static String repoGUIDPrefix = "ala:";

  /**
   * Implementation of
   * {@link csiro.diasb.util.GUIDConverter#toRepositoryGUID toRepositoryGUID}
   * method.
   *
   * @param docGUID Harvested document's GUID in {@link String} format.
   * <b>Note: </b> the encoding of this {@link String} is assumed to be
   * standard JRE's encoding, which is UTF-8 at the time of writing.
   *
   * @return A {@link String} representation of a Fedora Commons' ID.
   */
  public static String toRepositoryGUID(String docGUID) {

    // Add prefix and return new String.
    // return (repoGUIDPrefix + FCGUIDConverterImpl.genBase64RepoId(docGUID));

    return (repoGUIDPrefix + FcGuidConverter.genMd5RepoId(docGUID));

  } // End of `FCGUIDConverterImpl.toRepositoryGUID` method.

  /**
   * Generates a one-way conversion from a document's GUID to the
   * repository's ID.  The one-way aspect is the use of MD5 message digest
   * to transform the document's GUID to repository's ID.
   *
   * @param docGUID Harvested document's GUID in {@link String} format.
   *
   * @return MD5 message digest of <code>docGUID</code>.  <code>null</code>
   * on error.
   */
  private static String genMd5RepoId(String docGUID) {
		try {
			MessageDigest digestGenerator = MessageDigest.getInstance("MD5");
			digestGenerator.update(docGUID.getBytes(), 0, docGUID.length());
			return (new BigInteger(1, digestGenerator.digest()).toString(16));
		} catch (NoSuchAlgorithmException e) {
			//
			// Only throw a runtime exception as don't expect that ever to be an issue.
			throw new RuntimeException(e.getMessage(), e);
		}
    
	} // End of `FCGUIDConverterImpl.genMd5RepoId` method.
  
} // End of `FCGUIDConverterImpl` class
