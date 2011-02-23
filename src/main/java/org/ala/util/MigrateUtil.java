package org.ala.util;

import java.util.List;

import org.ala.dao.CassandraScanner;
import org.ala.dao.Scanner;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.SlicePredicate;
import org.wyki.cassandra.pelops.Mutator;
import org.wyki.cassandra.pelops.Pelops;
import org.wyki.cassandra.pelops.Policy;
import org.wyki.cassandra.pelops.Selector;


/**
 * This only works for keyspaces uses supercolumns
 */
public class MigrateUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

        if(args.length!=5){
            System.out.println("Usage: <sourceHost> <targetHost> <keyspace> <columnFamily> <column>");
            System.exit(1);
        }

        String sourceHost = args[0];
        String targetHost = args[1];
        String keyspace = args[2];
        String columnFamily = args[3];
        String column = args[4];

        System.out.println("Source: "+sourceHost);
        System.out.println("Target: "+targetHost);
        System.out.println("Keyspace: "+keyspace);
        System.out.println("Column family: "+columnFamily);

		//get connection pool for OLD cassandra
		Pelops.addPool("cassandra-source", new String[]{sourceHost}, 9160, false, keyspace, new Policy());

		//get connection pool for NEW cassandra
		Pelops.addPool("cassandra-target", new String[]{targetHost}, 9160, false, keyspace, new Policy());
		
		//get scanner
		Scanner scanner = new CassandraScanner(Pelops.getDbConnPool("cassandra-old").getConnection().getAPI(),
                keyspace, columnFamily, column);
		
		//for each row
		byte[] rowKey = scanner.getNextGuid();
		SlicePredicate slicePredicate = Selector.newColumnsPredicateAll(true, 10000);
		int counter = 0;
		while (rowKey!=null){
			counter++;
			//get all subcolumns for row from OLD
			Selector selector = Pelops.createSelector("cassandra-source", keyspace);
			String rowKeyAsString = new String(rowKey);
			List<Column> columns = selector.getSubColumnsFromRow(rowKeyAsString,columnFamily,columnFamily.getBytes(),slicePredicate,ConsistencyLevel.ONE);
			
			//write all subcolumns to NEW
			Mutator mutator = Pelops.createMutator("cassandra-target", keyspace);
			for(Column col: columns){
				//write them back
				mutator.writeSubColumn(rowKeyAsString, columnFamily, columnFamily, col);
			}
			mutator.execute(ConsistencyLevel.ONE);
			rowKey = scanner.getNextGuid();
			if(counter % 1000 == 0){ System.out.println("Counter: "+counter+", Last GUID: "+rowKeyAsString); };
		}
		//end
		System.out.println("FINISHED. Counter: "+counter);
	}
}