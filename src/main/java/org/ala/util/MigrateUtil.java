//package org.ala.util;
//
//import java.util.List;
//
//import org.ala.dao.CassandraScanner;
//import org.ala.dao.Scanner;
//import org.apache.cassandra.thrift.Column;
//import org.apache.cassandra.thrift.ConsistencyLevel;
//import org.apache.cassandra.thrift.SlicePredicate;
//import org.apache.cassandra.thrift.SuperColumn;
//import org.wyki.cassandra.pelops.Mutator;
//import org.wyki.cassandra.pelops.Pelops;
//import org.wyki.cassandra.pelops.Policy;
//import org.wyki.cassandra.pelops.Selector;
//
//
///**
// * This only works for keyspaces uses supercolumns
// */
//public class MigrateUtil {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) throws Exception {
//
//        if(args.length < 6){
//            System.out.println("Usage: <sourceHost> <sourcePort> <targetHost> <targetPort> <keyspace> <columnFamily> <column>");
//            System.exit(1);
//        }
//
//        String sourceHost = args[0];
//        String sourcePort = args[1];
//        String targetHost = args[2];
//        String targetPort = args[3];
//        String keyspace = args[4];
//        String columnFamily = args[5];
//        String column = null;
//        if(args.length > 6){
//        	column = args[6];
//        }
//        
//        System.out.println("Source: "+sourceHost);
//        System.out.println("Target: "+targetHost);
//        System.out.println("Keyspace: "+keyspace);
//        System.out.println("Column family: "+columnFamily);
//
//		//get connection pool for OLD cassandra
//		Pelops.addPool("cassandra-source", new String[]{sourceHost}, Integer.valueOf(sourcePort), false, keyspace, new Policy());
//
//		//get connection pool for NEW cassandra
//		Pelops.addPool("cassandra-target", new String[]{targetHost}, Integer.valueOf(targetPort), false, keyspace, new Policy());
//		
//		//get scanner
//		Scanner scanner = null;
//		if(args.length > 6){
//			scanner = new CassandraScanner(Pelops.getDbConnPool("cassandra-source").getConnection().getAPI(),
//                keyspace, columnFamily, column);
//		}
//		else{
//			scanner = new CassandraScanner(Pelops.getDbConnPool("cassandra-source").getConnection().getAPI(),
//	                keyspace, columnFamily);
//		}
//		
//		//for each row
//		byte[] rowKey = scanner.getNextGuid();
//		SlicePredicate slicePredicate = Selector.newColumnsPredicateAll(true, 10000);
//		int counter = 0;
//				
//		while (rowKey!=null){
//			//write all subcolumns to NEW
//			Mutator mutator = Pelops.createMutator("cassandra-target", keyspace);
//			counter++;
//			//get all subcolumns for row from OLD
//			Selector selector = Pelops.createSelector("cassandra-source", keyspace);
//			String rowKeyAsString = new String(rowKey);
//			if("rk".equalsIgnoreCase(columnFamily)){
//				if(args.length > 6){
//					List<Column> columns = selector.getSubColumnsFromRow(rowKeyAsString,columnFamily, column.getBytes(),slicePredicate,ConsistencyLevel.ONE);	
//					
//					for(Column col: columns){
//						//write them back
//						mutator.writeSubColumn(rowKeyAsString, columnFamily, columnFamily, col);
//					}
//				}
//				else{
//					List<SuperColumn> superColumns = selector.getSuperColumnsFromRow(rowKeyAsString,columnFamily,slicePredicate,ConsistencyLevel.ONE);				
//					
//					for(SuperColumn sc : superColumns){
//						List<Column> columns = selector.getSubColumnsFromRow(rowKeyAsString,columnFamily, sc.getName(),slicePredicate,ConsistencyLevel.ONE);	
//						
//						for(Column col: columns){
//							//write them back
//							mutator.writeSubColumn(rowKeyAsString, columnFamily, columnFamily, col);
//						}
//					}					
//				}
//			}
//			else{
//				List<Column> columns = selector.getSubColumnsFromRow(rowKeyAsString,columnFamily,columnFamily.getBytes(),slicePredicate,ConsistencyLevel.ONE);				
//				
//				for(Column col: columns){
//					//write them back
//					mutator.writeSubColumn(rowKeyAsString, columnFamily, columnFamily, col);
//				}
//				
//			}
//			mutator.execute(ConsistencyLevel.ONE);
//			rowKey = scanner.getNextGuid();
//			if(counter % 1000 == 0){ System.out.println("Counter: "+counter+", Last GUID: "+rowKeyAsString); };
//		}
//		//end
//		System.out.println("FINISHED. Counter: "+counter);
//		Pelops.shutdown();
//		System.exit(0);
//	}
//}