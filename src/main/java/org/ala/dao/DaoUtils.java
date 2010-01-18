package org.ala.dao;

import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;

public class DaoUtils {

	public static String getField(RowResult rowResult, String column) {
		Cell cell = rowResult.get(column);
		if(cell!=null && cell.getValue()!=null){
			return new String(rowResult.get(column).getValue());	
		}
		return null;
	}
}
