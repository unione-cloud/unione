package com.unione.cloud.beetsql.builder;

import lombok.Data;

@Data
public class SqlField {
	
	/**
	 * 	字段列名
	 */
	private String column;
	
	/**
	 * 	字段别名
	 */
	private String alias;
	
	/**
	 * 	字段类型
	 */
	private String type;
	
	/**
	 * 	是否主键
	 */
	private boolean isPk;
}
