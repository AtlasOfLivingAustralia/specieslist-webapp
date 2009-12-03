/**
  * Copyright (c) CSIRO Australia, 2009
  *
  * @author $Author: oak021 $
  * @version $Id: RepositoryFactory.java 849 2009-07-06 02:12:40Z oak021 $
  */

package csiro.diasb.fedora;

import csiro.diasb.util.*;

/**
 * This class is used to create instances of
 * {@link csiro.diasb.repository.Repository Repository} which can be used to
 * communicate with
 * the repository.
 *
 * @see csiro.diasb.repository.Repository
 * @author fri096
 */
public class RepositoryFactory  extends SpringFactory {

	/**
	 * Reference to the facotry singleton.
	 */
	private static RepositoryFactory repositoryFactory;
	
	/**
	 * Private constructor to enforce singleton. Calls init to load the Spring configuration.
	 */
	private RepositoryFactory() {
		//empty
	}
	
	/**
	 * @return reference to the repository factory singleton.
	 */
	public static RepositoryFactory getRepositoryFactory() {
		if(repositoryFactory==null) {
			repositoryFactory= new RepositoryFactory();
		}
		return repositoryFactory;
	}
	
	/**
	 * Reference to the repository singleton.
	 */
	private FedoraRepository repository;
	
	/**
	 * @return the Repository singleton. The actual implementation is determined by the Spring 
	 * application context.
	 */
	public FedoraRepository getRepository() {
		if(repository==null) {
			repository = (FedoraRepository) getContext().getBean("repository");
		}
		return repository;
	}
}
