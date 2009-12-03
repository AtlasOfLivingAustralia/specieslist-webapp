/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: hwa002 $
  * @version $Id: FedoraObjectHandlerException.java 144 2009-04-16 18:00:00Z hwa002 $
  */

package csiro.diasb.fedora;

/**
 * Exceptions thrown by {@link csiro.diasb.repository.FedoraObjectHandler
 * FedoraObjectHandler} class.
 *
 * @author hwa002
 */
public class FedoraException extends Exception {

  /**
   * Default constructor.
   *
   * @param msg Error message.
   */
  public FedoraException(String msg) {
    super(msg);
  } 
	/**
     * Constructs a new FedoraException with the specified detail message. The cause is 
     * not initialized, and may subsequently be initialized by a call to 
     * Throwable.initCause(java.lang.Throwable). 

     * @param msg the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause  the cause (which is saved for later retrieval by the Throwable.getCause() method). 
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
        public FedoraException(String msg, Throwable cause) {
		super(msg, cause);
	} 

}
