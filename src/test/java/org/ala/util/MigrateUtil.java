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

public class MigrateUtil {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//get connection pool for OLD cassandra
		Pelops.addPool("cassandra-old", new String[]{"localhost"}, 9160, false, "bie", new Policy());

		//get connection pool for NEW cassandra
		Pelops.addPool("cassandra-new", new String[]{"localhost"}, 9161, false, "bie", new Policy());
		
		//get scanner
		Scanner scanner = new CassandraScanner(Pelops.getDbConnPool("cassandra-old").getConnection().getAPI(), "bie", "tc", "taxonConcept");
		
		//for each row
		byte[] rowKey = scanner.getNextGuid();
		SlicePredicate slicePredicate = Selector.newColumnsPredicateAll(true, 10000);
		int counter = 0;
		while (rowKey!=null){
			counter++;
			//get all subcolumns for row from OLD
			Selector selector = Pelops.createSelector("cassandra-old", "bie");
			String rowKeyAsString = new String(rowKey);
			List<Column> columns = selector.getSubColumnsFromRow(rowKeyAsString,"tc","tc".getBytes(),slicePredicate,ConsistencyLevel.ONE);
			
			//write all subcolumns to NEW
			Mutator mutator = Pelops.createMutator("cassandra-new", "bie");
			for(Column col: columns){
				//write them back
				mutator.writeSubColumn(rowKeyAsString, "tc", "tc", col);
			}
			mutator.execute(ConsistencyLevel.ONE);
			rowKey = scanner.getNextGuid();
			if(counter % 1000 == 0){ System.out.println("Counter: "+counter+", Last GUID: "+rowKeyAsString); };
		}
		//end
		System.out.println("FINISHED. Counter: "+counter);
	}
}