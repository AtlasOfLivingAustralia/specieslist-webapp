package org.ala.dao;

import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;

public class DaoUtils {

	/**
	 * Retrieve the value for this column handling the possibility
	 * the column doesnt exist.
	 * 
	 * @param rowResult
	 * @param column
	 * @return the string value for this column
	 */
	public static String getField(RowResult rowResult, String column) {
		Cell cell = rowResult.get(column);
		if(cell!=null && cell.getValue()!=null){
			return new String(rowResult.get(column).getValue());	
		}
		return null;
	}
}
