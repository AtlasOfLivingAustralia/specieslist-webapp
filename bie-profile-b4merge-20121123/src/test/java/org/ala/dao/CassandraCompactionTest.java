package org.ala.dao;

import java.io.File;

import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.commons.io.FileUtils;
import org.scale7.cassandra.pelops.Cluster;
import org.scale7.cassandra.pelops.Pelops;
import org.scale7.cassandra.pelops.Mutator;

public class CassandraCompactionTest {


	protected static String keySpace = "bie";

	protected static String host = "localhost";

	protected static String pool = "ALA";

	protected static int port = 9160;

	protected static String charsetEncoding = "UTF-8";	

	public static void main(String[] args) throws Exception  {
		add(100);
		remove(100);
		System.exit(1);
	}
	
	public static void add(int count) throws Exception{
	    Pelops.addPool(pool, new Cluster(host,port), keySpace);
		
		Mutator mutator = Pelops.createMutator(pool);
		String myString = FileUtils.readFileToString(new File("/tmp/test"));
		
		for(int i=0; i< count; i++){
			//insert into table
			try{
				mutator.writeSubColumn(i+"", "rk", "rk", mutator.newColumn("test", myString));
				mutator.execute(ConsistencyLevel.ONE);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		System.out.println("Rows added");
		Pelops.shutdown();
	}
	
	public static void remove(int count) throws Exception{
	    Pelops.addPool(pool, new Cluster(host,port), keySpace);
		Mutator mutator = Pelops.createMutator(pool);
		for(int i=0; i< count; i++){
			try{
				mutator.deleteSubColumn(i+"", "rk", "rk", "test");
				mutator.execute(ConsistencyLevel.ONE);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		System.out.println("Rows removed");
		Pelops.shutdown();
	}
}
