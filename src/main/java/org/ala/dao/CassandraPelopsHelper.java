
package org.ala.dao;
import java.util.ArrayList;
import java.util.List;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.SuperColumn;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.wyki.cassandra.pelops.Mutator;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;
import org.wyki.cassandra.pelops.Selector;

/**
 * A StoreHelper implementation for Cassandra that uses Pelops over the
 * top of Thrift.
 * @author Natasha
 */
public class CassandraPelopsHelper implements StoreHelper  {
	protected static Logger logger = Logger.getLogger(CassandraPelopsHelper.class);

	protected static String keySpace = "bie";

	protected String host = "localhost";

        protected String pool = "ALA";

	protected int port = 9160;

	protected String charsetEncoding = "UTF-8";

        @Override
	public void init() throws Exception {
		//set up the connection pool
            Pelops.addPool(pool, new String[]{host}, port, false, keySpace, new Policy());
	}

    @Override
    public Comparable get(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
        logger.debug("Pelops get table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
        Selector selector = Pelops.createSelector(pool, keySpace);
        Column col = null;
        try{
            col = selector.getSubColumnFromRow(guid, columnFamily, columnFamily, columnName, ConsistencyLevel.ONE);
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        }
        //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//read the existing value
		if(col!=null){
			String value = new String(col.value,charsetEncoding);
//			logger.info(value);
			return (Comparable) mapper.readValue(value, theClass);
		} else {
			return null;
		}
    }

    @Override
    public List<Comparable> getList(String table, String columnFamily, String columnName, String guid, Class theClass) throws Exception {
        logger.debug("Pelops getList table: " + table + " colFamily: " +columnFamily + " guid: " + guid);
        Selector selector = Pelops.createSelector(pool, keySpace);
        Column col = null;
        try{ 
            col=selector.getSubColumnFromRow(guid, columnFamily, columnFamily, columnName, ConsistencyLevel.ONE);
        }
        catch(Exception e){
            //expected behaviour. current thrift API doesnt seem
            //to support a retrieve null getter
        }
         //initialise the object mapper
		ObjectMapper mapper = new ObjectMapper();
//		mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		mapper.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//read the existing value
		List<Comparable> objectList = null;
		if(col!=null){
			String value = new String(col.value, charsetEncoding);
//			logger.info(value);
			objectList = mapper.readValue(value, TypeFactory.collectionType(ArrayList.class, theClass));
		} else {
			objectList = new ArrayList<Comparable>();
		}
		return objectList;
    }

    @Override
    public boolean putSingle(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean put(String table, String columnFamily, String columnName, String guid, Comparable object) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean putList(String table, String columnFamily, String columnName, String guid, List<Comparable> object, boolean append) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Scanner getScanner(String table, String columnFamily, String column) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
