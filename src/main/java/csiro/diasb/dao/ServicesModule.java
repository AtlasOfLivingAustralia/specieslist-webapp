/**
 * Copyright (c) CSIRO Australia, 2009
 *
 * @author $Author: dos009 $
 * @version $Id:  $
 */

package csiro.diasb.dao;

import com.google.inject.AbstractModule;
import csiro.diasb.dao.solr.FedoraDAOImpl;

/**
 * Guice module to define binding for DAO service classes
 * 
 * @author "Nick dos Remedios <Nick.dosRemedios@csiro.au>"
 */
public class ServicesModule extends AbstractModule {
    @Override
    protected void configure() {

     /*
      * This tells Guice that whenever it sees a dependency on a TransactionLog,
      * it should satisfy the dependency using a DatabaseTransactionLog.
      */
    bind(FedoraDAO.class).to(FedoraDAOImpl.class);

    }
}