/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ala.util.ColumnType;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Enum class to store and retrieve ranking-types
 *
 * @author mok011
 */

public enum RankingType {
//	RK_IMAGE("rk", "image", ColumnType.IMAGE_COL, "identifier", ImageRanking.class),
//	RK_IMAGE("rk", "image", ColumnType.IMAGE_COL, BaseRanking.class), 
//	RK_COMMON_NAME("rk", "commonName", ColumnType.VERNACULAR_COL, BaseRanking.class);

	RK_IMAGE("rk", "image", ColumnType.IMAGE_COL, new String[]{"identifier"}), 
	RK_COMMON_NAME("rk", "commonName", ColumnType.VERNACULAR_COL, new String[]{"nameString"});

	private String superColumnName;
	private String columnFamily;
	private ColumnType columnType;
	private String[] compareFieldName; 
//	private Class clazz;
			
	private static final Map<String, RankingType> columnNameLookup = new HashMap<String, RankingType>();
	private static final Map<ColumnType, RankingType> tcColumnTypeLookup = new HashMap<ColumnType, RankingType>();
	
	static {		
		for (RankingType rt : EnumSet.allOf(RankingType.class)) {
			columnNameLookup.put(rt.getSuperColumnName(), rt);
		}
		for (RankingType rt : EnumSet.allOf(RankingType.class)) {
			tcColumnTypeLookup.put(rt.getColumnType(), rt);
		}
	}

	public static Set<RankingType> getAll(){
		return EnumSet.allOf(RankingType.class);
	}
	
    public static RankingType getRankingTypeByColumnName(String columnName) {
    	return columnNameLookup.get(columnName);
    }
 
    public static RankingType getRankingTypeByTcColumnType(ColumnType columnType) {
    	return tcColumnTypeLookup.get(columnType);
    }
    
	/**
	 * Constructor to set mimeType
	 * 
	 * @param mimeType
	 */
	private RankingType(String columnFamily, String superColumnName, ColumnType columnType, String[] compareFieldName) {
		this.superColumnName = superColumnName;
		this.columnFamily = columnFamily;
		this.columnType = columnType;
		this.compareFieldName = compareFieldName;
//		this.clazz = clazz;
	}

	public String getSuperColumnName() {
		return superColumnName;
	}

	public String getColumnFamily() {
		return columnFamily;
	} 
	
	public String[] getCompareFieldName() {
		return compareFieldName;
	}
	
	public ColumnType getColumnType() {
		return columnType;
	}
	
//	public Class getClazz() {
//		return clazz;
//	}
	
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
	
}
