package com.unione.cloud.beetsql.builder;

import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.core.model.BaseField;

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
	
	/**
	 * 	是否为基础字段，baseField!=null 是，baseField==null 否
	 */
	private BaseField stsField;

	/**
	 * 字段脱敏
	 */
	private SqlSensitive sensitive;
	
	/**
	 * 查询忽略
	 */
	private UniQueryIgnore queryIgnore;
}
