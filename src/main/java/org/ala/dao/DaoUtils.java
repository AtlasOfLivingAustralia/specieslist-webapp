package org.ala.dao;

import org.apache.hadoop.hbase.io.Cell;
import org.apache.hadoop.hbase.io.RowResult;

public class DaoUtils {

	public static byte[] serialiseArray(String[] values){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<values.length; i++){
			if(i>0){
				sb.append(">>>");
			}			
			sb.append(values[i]);
		}
		return sb.toString().getBytes();
	}
	
	public static String getField(RowResult rowResult, String column) {
		Cell cell = rowResult.get(column);
		if(cell!=null && cell.getValue()!=null){
			return new String(rowResult.get(column).getValue());	
		}
		return null;
	}
}
