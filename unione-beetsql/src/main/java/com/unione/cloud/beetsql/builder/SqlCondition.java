package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
class SqlCondition {

	private SqlFun fun=SqlFun.AND;
	
	/**
	 * 	字段名称
	 */
	private String column;
	
	/**
	 * 	比较方式
	 */
	private SqlAction action;
	
	/**
	 * 	参数名称
	 */
	private String name;
	
	/**
	 * 	(括号复杂查询)
	 */
	private List<SqlCondition> childrens=new ArrayList<>();
}
