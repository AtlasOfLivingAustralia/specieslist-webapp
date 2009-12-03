/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: oak021 $
  * @version $Id: SolrQueryException.java 144 2009-08-24 18:00:00Z hwa002 $
  */

package csiro.diasb.fedora;

/**
 * Exceptions thrown by {@link csiro.diasb.repository.FedoraObjectHandler
 * FedoraObjectHandler} class.
 *
 * @author hwa002
 */
public class SolrQueryException extends Exception {

  /**
   * Default constructor.
   *
   * @param msg Error message.
   */
  public SolrQueryException(String msg) {
    super(msg);
  } 
	/**
     * Constructs a new SolrQueryException with the specified detail message. The cause is
     * not initialized, and may subsequently be initialized by a call to 
     * Throwable.initCause(java.lang.Throwable). 

     * @param msg the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause  the cause (which is saved for later retrieval by the Throwable.getCause() method). 
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
        public SolrQueryException(String msg, Throwable cause) {
		super(msg, cause);
	} 

}
