package com.unione.cloud.codegen;

import org.beetl.sql.gen.Attribute;

import lombok.Data;

@Data
public class PojoAttribute extends Attribute {

	/**
	 * 	数据库列，数据长度
	 */
	private Integer colSize;
	
}
