package org.ala.util;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * 
 * @author MOK011
 *
 */

public class CassandraClientPool {
	public static final int DEFAULT_MAX_ACTIVE = 50;
	public static final int DEFAULT_MAX_IDLE = 5;
	public static final long DEFAULT_MAX_WAIT = 15 * 1000;
	public static final byte DEFAULT_EXHAUSTED_ACTION = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;

	protected Logger logger = Logger.getLogger(this.getClass());
	
	private GenericObjectPoolFactory poolFactory = null;
	private GenericObjectPool cassandraPool = null;
	private PoolableClientFactory clientFactory = null;

	private String host = "localhost";
	private int port = 9160;
	private int maxActive;
	private int maxIdle;
	private byte exhaustedAction;
	private long maxWait;

	public CassandraClientPool() {
		this("localhost", 9160, null, DEFAULT_MAX_ACTIVE,
				DEFAULT_EXHAUSTED_ACTION, DEFAULT_MAX_WAIT,
				DEFAULT_MAX_IDLE);
	}

	public CassandraClientPool(String host, int port){
		this(host, port, null, DEFAULT_MAX_ACTIVE,
				DEFAULT_EXHAUSTED_ACTION, DEFAULT_MAX_WAIT,
				DEFAULT_MAX_IDLE);
	}
	
	public CassandraClientPool(String host, int port,
			PoolableObjectFactory clientfactory, int maxActive,
			byte exhaustedAction, long maxWait, int maxIdle) {
		this.host = host;
		this.port = port;
		this.maxActive = maxActive;
		this.maxWait = maxWait;
		this.maxIdle = maxIdle;

		// if not give a client factory, will create the default
		// PoolableClientFactory
		if (clientfactory == null) {
			clientFactory = new PoolableClientFactory();
		}
		else{
			clientFactory = (PoolableClientFactory)clientfactory;
		}

		poolFactory = new GenericObjectPoolFactory(clientFactory, maxActive,
				exhaustedAction, maxWait, maxIdle);
		cassandraPool = (GenericObjectPool) poolFactory.createPool();
	}

	private Cassandra.Client createClient(String serviceURL, int port)
			throws TTransportException, TException {
		TTransport tr = new TSocket(serviceURL, port);
		TProtocol proto = new TBinaryProtocol(tr);
		Cassandra.Client client = new Cassandra.Client(proto);
		tr.open();

		return client;
	}

	public Cassandra.Client getClient() throws Exception {
		long t = System.currentTimeMillis();
		Cassandra.Client cc = (Cassandra.Client) cassandraPool.borrowObject();
		System.out.println("**** getClient time taken: " + (System.currentTimeMillis() - t) + 
				" getNumActive:" + cassandraPool.getNumActive() + 
				" getNumIdle:" + cassandraPool.getNumIdle());
		return cc;
	}

	public void returnClient(Cassandra.Client client) throws Exception {
		cassandraPool.returnObject(client);
	}
	
	public void close() {
		try {
			cassandraPool.close() ;
		} catch (Exception e) {
			logger.error("close client pool error",e);
		}
	}
	
	public int getNumIdleClient() {
		return cassandraPool.getNumIdle();
	}
		
	public int getActiveNumClient() {
		return cassandraPool.getNumActive();
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public byte getExhaustedAction() {
		return exhaustedAction;
	}

	public long getMaxWaitWhenBlockByExhausted() {
		return maxWait;
	}

	/*---------------- inner class ------------------------*/
	public class PoolableClientFactory extends BasePoolableObjectFactory
			implements PoolableObjectFactory {

		@Override
		public void destroyObject(Object obj) throws Exception {
			logger.debug("close destroyObject " + obj.toString());

			Cassandra.Client client = (Cassandra.Client) obj;
			client.getInputProtocol().getTransport().close();
			client.getOutputProtocol().getTransport().close();
		}

		@Override
		public Object makeObject() throws Exception {
			Cassandra.Client cc = null;
			try {				
				long t = System.currentTimeMillis();
				cc = createClient(host, port);
				logger.debug("**** makeObject_createClient(): " + (System.currentTimeMillis() - t));
				return cc;
			} catch (TTransportException e) {
				logger.error("create client error:", e);
				throw e;
			} catch (TException e) {
				logger.error("create client error:", e);
				throw e;
			}
			
		}

		@Override
		public boolean validateObject(Object obj) {
			return true;
		}
	}
}
